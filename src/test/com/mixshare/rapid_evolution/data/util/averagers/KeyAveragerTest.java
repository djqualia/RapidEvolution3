package test.com.mixshare.rapid_evolution.data.util.averagers;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.util.averagers.KeyAverager;
import com.mixshare.rapid_evolution.music.key.Key;

public class KeyAveragerTest extends RE3TestCase {

    private static Logger log = Logger.getLogger(KeyAveragerTest.class);
	
	public void testKeyAverager() {
		try {
			// test grouping based on key code root
			KeyAverager key = new KeyAverager();
			key.addValue(Key.getKey("Bm"), System.currentTimeMillis(), 100);
			Thread.sleep(10);
			key.addValue(Key.getKey("B"), System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			key.addValue(Key.getKey("D"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("D")))
				fail("incorrect mode value=" + key.getModeValue());
			
			// test grouping based on key code root
			key = new KeyAverager();
			key.addValue(Key.getKey("D"), System.currentTimeMillis(), 100);
			Thread.sleep(10);
			key.addValue(Key.getKey("B"), System.currentTimeMillis(), 100);			
			Thread.sleep(10);
			key.addValue(Key.getKey("Bm"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("Bm")))
				fail("incorrect mode value=" + key.getModeValue());
			
			// test tie breaker based on adjacent key code root counts
			key = new KeyAverager();
			key.addValue(Key.getKey("Fm"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("Cm"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("Gm"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("Cm")))
				fail("incorrect mode value=" + key.getModeValue());

			// test tie breaker of adjacent root scale types...
			key = new KeyAverager();
			key.addValue(Key.getKey("Fm"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("Cm"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("Gm"), System.currentTimeMillis(), 100);			
			key.addValue(Key.getKey("Eb"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("Cm")))
				fail("incorrect mode value=" + key.getModeValue());

			// test tie breaker based on adjacent key code root counts
			key = new KeyAverager();
			key.addValue(Key.getKey("Fm"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("Fm"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("Cm"), System.currentTimeMillis(), 100);			
			key.addValue(Key.getKey("Am"), System.currentTimeMillis(), 100);			
			key.addValue(Key.getKey("Am"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("Fm")))
				fail("incorrect mode value=" + key.getModeValue());
			
			
			// test key code roots with different scale types
			key = new KeyAverager();
			key.addValue(Key.getKey("Fm"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("Gm"), System.currentTimeMillis(), 100);			
			key.addValue(Key.getKey("Eb"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("Eb")))
				fail("incorrect mode value=" + key.getModeValue());
			
			// test last modified wins in tie
			key = new KeyAverager();
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 100);
			Thread.sleep(10);
			key.addValue(Key.getKey("G#"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("G#"))) {
				key.debugOutput();
				fail("incorrect mode value=" + key.getModeValue());
			}

			// test most used trumps most recent
			key = new KeyAverager();
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 100);
			Thread.sleep(10);
			key.addValue(Key.getKey("G#"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("G#m"))) {
				key.debugOutput();
				fail("incorrect mode value=" + key.getModeValue());
			}
			
			// test circle of 5ths stuff
			key = new KeyAverager();
			key.addValue(Key.getKey("D"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("G"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("A"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("D")))
				fail("incorrect mode value=" + key.getModeValue());

			// circle of 5ths + most recent
			key = new KeyAverager();
			key.addValue(Key.getKey("D"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("G"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("G"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("A"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("G")))
				fail("incorrect mode value=" + key.getModeValue());
			
			key = new KeyAverager();
			key.addValue(Key.getKey("D"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("G"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("A"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("F#m"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("A")))
				fail("incorrect mode value=" + key.getModeValue());

			key = new KeyAverager();
			key.addValue(Key.getKey("Fm"), System.currentTimeMillis(), 100);
			Thread.sleep(100);
			key.addValue(Key.getKey("Cm"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("Bm"), System.currentTimeMillis(), 100);
			if (!key.getModeValue().equals(Key.getKey("Cm")))
				fail("incorrect mode value=" + key.getModeValue());

			// test no value
			key = new KeyAverager();
			if (!key.getModeValue().equals(Key.NO_KEY))
				fail("incorrect mode value=" + key.getModeValue());
			
			// test adding/ignore empty values
			key = new KeyAverager();
			key.addValue(Key.getKey("Fm"), System.currentTimeMillis(), 100);
			Thread.sleep(10);
			key.addValue(Key.NO_KEY, System.currentTimeMillis(), 100);
			key.addValue(Key.NO_KEY, System.currentTimeMillis(), 100);
			if (!key.getModeValue().equals(Key.getKey("Fm")))
				fail("incorrect mode value=" + key.getModeValue());

			// test using empty values
			key = new KeyAverager(false);
			key.addValue(Key.getKey("Fm"), System.currentTimeMillis(), 100);
			Thread.sleep(10);
			key.addValue(Key.NO_KEY, System.currentTimeMillis(), 100);
			key.addValue(Key.NO_KEY, System.currentTimeMillis(), 100);
			if (!key.getModeValue().equals(Key.NO_KEY))
				fail("incorrect mode value=" + key.getModeValue());

			// test higher accuracy trumps most frequent
			key = new KeyAverager();
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 50);
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 50);
			Thread.sleep(10);
			key.addValue(Key.getKey("G#"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("G#"))) {
				key.debugOutput();
				fail("incorrect mode value=" + key.getModeValue());
			}

			// test higher accuracy trumps most frequent
			key = new KeyAverager();
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 50);
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 50);
			Thread.sleep(10);
			key.addValue(Key.getKey("G#"), System.currentTimeMillis(), 100);			
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 50);			
			if (!key.getModeValue().equals(Key.getKey("G#"))) {
				key.debugOutput();
				fail("incorrect mode value=" + key.getModeValue());
			}

			// test higher accuracy only are used
			key = new KeyAverager();
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 50);
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 50);
			Thread.sleep(10);
			key.addValue(Key.getKey("G#"), System.currentTimeMillis(), 100);			
			key.addValue(Key.getKey("Fm"), System.currentTimeMillis(), 100);			
			if (!key.getModeValue().equals(Key.getKey("Fm"))) {
				key.debugOutput();
				fail("incorrect mode value=" + key.getModeValue());
			}

			// test support for off shift keys
			key = new KeyAverager();
			key.addValue(Key.getKey("G#m+50"), System.currentTimeMillis(), 50);
			if (!key.getModeValue().equals(Key.getKey("G#m+50"))) {
				key.debugOutput();
				fail("incorrect mode value=" + key.getModeValue());
			}

			key = new KeyAverager();
			key.addValue(Key.getKey("Am-50"), System.currentTimeMillis(), 50);
			if (!key.getModeValue().equals(Key.getKey("Am-50"))) {
				key.debugOutput();
				fail("incorrect mode value=" + key.getModeValue());
			}
			
			key = new KeyAverager();
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 50);
			Thread.sleep(100);
			key.addValue(Key.getKey("G#m+50"), System.currentTimeMillis(), 50);
			if (!key.getModeValue().equals(Key.getKey("G#m+50"))) {
				key.debugOutput();
				fail("incorrect mode value=" + key.getModeValue());
			}
			
			key = new KeyAverager();
			key.addValue(Key.getKey("G#m"), System.currentTimeMillis(), 50);
			Thread.sleep(10);
			key.addValue(Key.getKey("G#m+25"), System.currentTimeMillis(), 50);
			Thread.sleep(10);
			key.addValue(Key.getKey("G#m+25"), System.currentTimeMillis(), 50);
			Thread.sleep(10);
			key.addValue(Key.getKey("G#m+50"), System.currentTimeMillis(), 50);
			if (!key.getModeValue().equals(Key.getKey("G#m+25"))) {
				key.debugOutput();
				fail("incorrect mode value=" + key.getModeValue());
			}			

			/*
			key = new KeyAverager();
			key.addValue(Key.getKey("E"), System.currentTimeMillis(), 100);
			key.addValue(Key.getKey("Am"), System.currentTimeMillis(), 100);
			if (!key.getModeValue().equals(Key.getKey("Em"))) {
				key.debugOutput();
				fail("incorrect mode value=" + key.getModeValue());
			}	
			*/		
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("testKeyAverager(): " + e);			
		}
	}
	
}
