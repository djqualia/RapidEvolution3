package test.com.mixshare.rapid_evolution.data.mined.discogs;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllDiscogsRetrieverTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Discogs data miners test suite");
        //$JUnit-BEGIN$
                
        // discogs retrieval
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabelTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseRatingsTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseSearchTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.song.DiscogsSongTest.class);
                        
        //$JUnit-END$
        return suite;
    }
		
}
