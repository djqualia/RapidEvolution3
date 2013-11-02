package test.com.mixshare.rapid_evolution.data.mined;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllMiningTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Data mining test suite for Rapid Evolution 3");
        //$JUnit-BEGIN$
        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.bbc.artist.BBCArtistProfileTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.billboard.artist.BillboardArtistProfileTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.billboard.song.BillboardSongProfileTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabelTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseRatingsTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseSearchTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.discogs.song.DiscogsSongTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.echonest.artist.EchonestArtistProfileTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.echonest.song.EchonestSongProfileTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.idiomag.artist.IdiomagArtistProfileTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.lastfm.artist.LastfmArtistProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.lastfm.release.LastfmReleaseProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.lastfm.song.LastfmSongProfileTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.lyricsfly.song.LyricsflySongProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.lyricwiki.song.LyricwikiSongProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.musicbrainz.artist.MusicbrainzArtistProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.musicbrainz.label.MusicbrainzLabelProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.musicbrainz.release.MusicbrainzReleaseProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.musicbrainz.song.MusicbrainzSongProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.yahoo.artist.YahooArtistProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.yahoo.song.YahooSongProfileTest.class);
        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.mined.util.MiningRateControllerTest.class);
        
        //$JUnit-END$
        return suite;
    }
	
	
}
