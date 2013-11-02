package test.com.mixshare.rapid_evolution.data.mined.util;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.mined.util.MiningRateController;

public class MiningRateControllerTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(MiningRateControllerTest.class);
    
	public void testMaxQueriesPerInterval() {
		try {
			int maxQueries = 5000;
			RE3Properties.setProperty("discogs_max_queries_per_second", "-1");
			RE3Properties.setProperty("discogs_max_queries_per_day", String.valueOf(maxQueries));
			MiningRateController.INTERVAL_LENGTH_MILLIS = 1000 * 1; // 1 seconds, for testing purposes, rather than 1 day...
			
			MiningRateController controller = new MiningRateController(DATA_SOURCE_DISCOGS);
			for (int i = 0; i < maxQueries; ++i) {
				if (!controller.canMakeQuery())
					fail("couldn't reach max queries");
				controller.startQuery();
			}
			if (controller.canMakeQuery())
				fail("didn't stop at max queries");
			try {
				controller.startQuery();
				fail("MiningLimitReachedException not thrown");
			} catch (MiningLimitReachedException e) { }
			
			// now wait and make sure can continue to query after interval
			Thread.sleep(MiningRateController.INTERVAL_LENGTH_MILLIS + 1);

			for (int i = 0; i < maxQueries; ++i) {
				if (!controller.canMakeQuery())
					fail("couldn't reach max queries after waiting");
				controller.startQuery();
			}
			if (controller.canMakeQuery())
				fail("didn't stop at max queries after waiting");
			try {
				controller.startQuery();
				fail("MiningLimitReachedException not thrown");
			} catch (MiningLimitReachedException e) { }
			
			// create a new controller, which should persist the old limits 
			controller = new MiningRateController(DATA_SOURCE_DISCOGS);
			if (controller.canMakeQuery())
				fail("didn't stop at max queries after waiting");
			try {
				controller.startQuery();
				fail("MiningLimitReachedException not thrown");
			} catch (MiningLimitReachedException e) { }
			
			// now wait and make sure can continue to query after interval
			Thread.sleep(MiningRateController.INTERVAL_LENGTH_MILLIS + 1);

			for (int i = 0; i < maxQueries; ++i) {
				if (!controller.canMakeQuery())
					fail("couldn't reach max queries after waiting");
				controller.startQuery();
			}
			if (controller.canMakeQuery())
				fail("didn't stop at max queries after waiting");
			try {
				controller.startQuery();
				fail("MiningLimitReachedException not thrown");
			} catch (MiningLimitReachedException e) { }
			
			// now make half the queries, wait the interval, and make sure it resets fully
			Thread.sleep(MiningRateController.INTERVAL_LENGTH_MILLIS + 1);

			for (int i = 0; i < maxQueries / 2; ++i) {
				if (!controller.canMakeQuery())
					fail("couldn't reach max queries after waiting");
				controller.startQuery();
			}
				
			Thread.sleep(MiningRateController.INTERVAL_LENGTH_MILLIS + 1);

			for (int i = 0; i < maxQueries; ++i) {
				if (!controller.canMakeQuery())
					fail("couldn't reach max queries after waiting");
				controller.startQuery();
			}
			
			// lets try the same thing, but restarting the controller in between
			Thread.sleep(MiningRateController.INTERVAL_LENGTH_MILLIS + 1);
			
			for (int i = 0; i < maxQueries / 2; ++i) {
				if (!controller.canMakeQuery())
					fail("couldn't reach max queries after waiting");
				controller.startQuery();
			}
			
			Thread.sleep(MiningRateController.INTERVAL_LENGTH_MILLIS + 1);
			controller = new MiningRateController(DATA_SOURCE_DISCOGS);

			for (int i = 0; i < maxQueries; ++i) {
				if (!controller.canMakeQuery())
					fail("couldn't reach max queries after waiting");
				controller.startQuery();
			}			
			
		} catch (Exception e) {
			log.error("testMaxQueriesPerInterval(): error", e);
			fail(e.getMessage());
		}
	}
	
	public void testMaxQueriesPerSecond() {
		try {
			int maxQueriesPerSecond = 1;
			RE3Properties.setProperty("discogs_max_queries_per_second", String.valueOf(maxQueriesPerSecond));
			RE3Properties.setProperty("discogs_max_queries_per_day", "-1");
			
			MiningRateController controller = new MiningRateController(DATA_SOURCE_DISCOGS);
			controller.startQuery();
			long time = System.currentTimeMillis();
			controller.startQuery();
			long diff = System.currentTimeMillis() - time;
			if (diff < (maxQueriesPerSecond * 1000))
				fail("limit per second not enforced");
			
		} catch (Exception e) {
			log.error("testMaxQueriesPerSecond(): error", e);
			fail(e.getMessage());
		}	
	}
	
}
