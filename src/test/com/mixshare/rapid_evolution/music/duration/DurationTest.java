package test.com.mixshare.rapid_evolution.music.duration;

import java.util.Vector;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;

public class DurationTest extends RE3TestCase {
	
    static private Logger log = Logger.getLogger(DurationTest.class);    
	
	public void testDuration() {
		Duration duration = new Duration("10:35");
		if (!duration.getDurationAsString().equals("10:35"))
			fail("getDurationAsString failed");
		if (duration.getDurationInMillis() != 635000)
			fail("getDurationInMillis failed");
		if (duration.getDurationInSeconds() != 635)
			fail("getDurationInSeconds failed");

		duration = new Duration(635000);
		if (!duration.getDurationAsString().equals("10:35"))
			fail("getDurationAsString failed");
		if (duration.getDurationInMillis() != 635000)
			fail("getDurationInMillis failed");
		if (duration.getDurationInSeconds() != 635)
			fail("getDurationInSeconds failed");

		Duration duration2 = new Duration("4:30");
		if (duration.compareTo(duration2) < 0)
			fail("compareTo failed");
		if (duration2.compareTo(duration) > 0)
			fail("compareTo failed");
		
		duration = new Duration("4:30");
		if (duration.compareTo(duration2) != 0)
			fail("compareTo failed");
		if (duration2.compareTo(duration) != 0)
			fail("compareTo failed");
		
	}
	
	/**
	 * This is not really a test but I was curious how long a sort would take on X amount of objects...
	 */
	public void testSortTime() {
		int numDurations = 21000;
		Vector<Duration> durations = new Vector<Duration>(numDurations);
		for (int i = 0; i < numDurations; ++i)
			durations.add(new Duration((int)(Math.random() * 1000 * 600)));
		long timeBefore = System.currentTimeMillis();
		java.util.Collections.sort(durations);
		long sortTime = System.currentTimeMillis() - timeBefore;
		log.debug("sort time for " + numDurations + " durations is=" + sortTime + "ms");
		
	}

}
