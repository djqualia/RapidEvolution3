package test.com.mixshare.rapid_evolution.audio.detection.rga;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.audio.detection.rga.RGAData;
import com.mixshare.rapid_evolution.audio.detection.rga.RGADetector;

public class RGADetectorTest extends RE3TestCase {

	static private Logger log = Logger.getLogger(RGADetectorTest.class);	
	
	public void testRGADetector() {
		try {
			RGAData result = new RGADetector("data/test/audio/songs/freaktimebaby.mp3", null).detectRGA();
			if (result == null)
				fail("replay gain detector did not return a result");
			if (result.getDifference() != -8.55f)
				fail("incorrect rga value, is=" + result.getDifference());

			result = new RGADetector("data/test/audio/songs/rga.mp3", null).detectRGA();
			if (result == null)
				fail("replay gain detector did not return a result");
			if (result.getDifference() != -5.14f)
				fail("incorrect rga value, is=" + result.getDifference());
			
		} catch (Exception e) {
			log.error("testRGADetector(): error", e);
			fail(e.getMessage());
		}
	}	
	
}
