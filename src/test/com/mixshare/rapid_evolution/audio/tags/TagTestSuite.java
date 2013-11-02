package test.com.mixshare.rapid_evolution.audio.tags;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TagTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test suite for tag reading/writing");
        //$JUnit-BEGIN$
        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.tags.TagManagerTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.tags.TagReaderTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.tags.TagWriterTest.class);           

        //$JUnit-END$
        return suite;
    }
	
}
