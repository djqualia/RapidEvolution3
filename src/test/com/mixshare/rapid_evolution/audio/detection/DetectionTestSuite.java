package test.com.mixshare.rapid_evolution.audio.detection;

import junit.framework.Test;
import junit.framework.TestSuite;

public class DetectionTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test suite for audio detection");
        //$JUnit-BEGIN$
        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.detection.key.KeyDetectorTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.detection.bpm.BpmDetectorTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.detection.beatintensity.BeatIntensityDetectorTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.detection.rga.RGADetectorTest.class);       
        
        //$JUnit-END$
        return suite;
    }
	
}
