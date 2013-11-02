package test.com.mixshare.rapid_evolution.data;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllDatabaseTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Database test suite for Rapid Evolution 3");
        //$JUnit-BEGIN$
        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.DatabaseTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.HierarchicalIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.filter.FilterIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.filter.style.StyleIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.filter.tag.TagIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.filter.playlist.PlaylistIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.search.artist.ArtistIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.search.label.LabelIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.search.release.ReleaseIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.search.song.SongIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.search.SearchParserTest.class);         
        
        //$JUnit-END$
        return suite;
    }
	
}
