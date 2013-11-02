package test.com.mixshare.rapid_evolution.audio.detection.beatintensity;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.detection.beatintensity.BeatIntensityDetector;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;

public class BeatIntensityDetectorTest extends RE3TestCase {

	static private Logger log = Logger.getLogger(BeatIntensityDetectorTest.class);	
	
	public void testBeatIntensityDetector() {
		try {
			RE3Properties.setProperty("beatIntensity_detector_quality", "0");
			
			BeatIntensity result = BeatIntensityDetector.detectBeatIntensity("data/test/audio/songs/freaktimebaby.mp3", null);
			if (result == null)
				fail("beatIntensity detector did not return a result");
			if (result.getBeatIntensityValue() != 75)
				fail("incorrect beatIntensity, is=" + result);
			
		} catch (Exception e) {
			log.error("testBeatIntensityDetector(): error", e);
			fail(e.getMessage());
		}
	}	
	
}
