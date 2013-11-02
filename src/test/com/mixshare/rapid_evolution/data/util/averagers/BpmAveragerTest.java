package test.com.mixshare.rapid_evolution.data.util.averagers;


import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.util.averagers.BpmAverager;

public class BpmAveragerTest extends RE3TestCase {

    private static Logger log = Logger.getLogger(BpmAveragerTest.class);
	
	public void testBpmAverager() {
		try {
			BpmAverager bpm = new BpmAverager();
			bpm.addValue(120.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(121.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(122.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 121.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());
			
			bpm = new BpmAverager();
			bpm.addValue(120.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(121.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(122.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(75.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 121.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());			

			bpm = new BpmAverager();
			bpm.addValue(120.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(121.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(119.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(60.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 120.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());				
			
			bpm = new BpmAverager();
			bpm.addValue(141.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(139.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(70.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 140.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());	
	
			bpm = new BpmAverager();
			bpm.addValue(141.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(139.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(70.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(71.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 70.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());				
		
			bpm = new BpmAverager();
			bpm.addValue(70.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(80.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(90.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(81.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 81.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());		
		
			bpm = new BpmAverager();
			bpm.addValue(70.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(80.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(90.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(71.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 71.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());	
			
			bpm = new BpmAverager();
			bpm.addValue(70.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(80.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(90.0f, System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			bpm.addValue(91.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 90.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());	
			
			bpm = new BpmAverager();
			bpm.addValue(70.0f, System.currentTimeMillis(), 100);
			Thread.sleep(100);
			bpm.addValue(80.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 80.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());	
			
			bpm = new BpmAverager();
			bpm.addValue(80.0f, System.currentTimeMillis(), 100);
			Thread.sleep(100);
			bpm.addValue(70.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 70.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());	
			
			bpm = new BpmAverager();
			bpm.addValue(70.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(70.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(80.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 70.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());	
			
			bpm = new BpmAverager();
			bpm.addValue(80.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(80.0f, System.currentTimeMillis(), 100);
			Thread.sleep(10);
			bpm.addValue(70.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 80.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());				
			
			bpm = new BpmAverager();
			bpm.addValue(70.0f, System.currentTimeMillis(), 50);
			Thread.sleep(10);
			bpm.addValue(70.0f, System.currentTimeMillis(), 50);
			Thread.sleep(10);
			bpm.addValue(80.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 80.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());	
			
			bpm = new BpmAverager();
			bpm.addValue(80.0f, System.currentTimeMillis(), 50);
			Thread.sleep(10);
			bpm.addValue(80.0f, System.currentTimeMillis(), 50);
			Thread.sleep(10);
			bpm.addValue(70.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 70.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());
			
			bpm = new BpmAverager();
			bpm.addValue(80.0f, System.currentTimeMillis(), 50);
			Thread.sleep(10);
			bpm.addValue(0.0f, System.currentTimeMillis(), 50);
			Thread.sleep(10);
			bpm.addValue(0.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 80.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());

			bpm = new BpmAverager(false);
			bpm.addValue(80.0f, System.currentTimeMillis(), 50);
			Thread.sleep(10);
			bpm.addValue(0.0f, System.currentTimeMillis(), 50);
			Thread.sleep(10);
			bpm.addValue(0.0f, System.currentTimeMillis(), 100);			
			if (bpm.getAverageValue() != 0.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());
			
			bpm = new BpmAverager();
			if (bpm.getAverageValue() != 0.0f)
				fail("incorrect mode value=" + bpm.getAverageValue());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("testBpmAverager(): " + e);			
		}
	}
	
}
