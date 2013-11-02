package test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test suite for Rapid Evolution 3");
        //$JUnit-BEGIN$
        
        // util
        suite.addTestSuite(test.com.mixshare.rapid_evolution.util.StringUtilTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.util.DegreeValueTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.util.DegreeValueSetAveragerTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.util.cache.LRUCacheTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.util.io.SerializerTest.class);  
        suite.addTestSuite(test.com.mixshare.rapid_evolution.util.io.FileLockManagerTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.util.io.writers.XMLLineWriterTest.class);
        
        // music stuff
        suite.addTestSuite(test.com.mixshare.rapid_evolution.music.key.KeyTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.music.duration.DurationTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.music.timesig.TimeSigTest.class);

        // averagers
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.util.averagers.BpmAveragerTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.util.averagers.KeyAveragerTest.class);
        
        // identifiers
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifierTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifierTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifierTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifierTest.class);
        
        // database
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.DatabaseTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.HierarchicalIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.filter.FilterIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.filter.style.StyleIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.filter.playlist.PlaylistIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.filter.tag.TagIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.search.artist.ArtistIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.search.label.LabelIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.search.release.ReleaseIndexTest.class);         
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.index.search.song.SongIndexTest.class);                 
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.record.filter.playlist.DynamicPlaylistRecordTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.record.filter.style.StyleRecordTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.record.filter.tag.TagRecordTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecordTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.record.search.label.LabelRecordTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecordTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.record.search.song.SongRecordTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.record.search.song.MixoutRecordTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.profile.filter.playlist.DynamicPlaylistProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.profile.search.label.LabelProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.profile.search.song.SongProfileTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfileTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.util.table.UniqueIdTableTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.data.search.SearchParserTest.class);         
                
        // audio util, tag writing/reading        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.util.AudioUtilTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.tags.TagManagerTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.tags.TagReaderTest.class);
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.tags.TagWriterTest.class);        
        
        // detectors
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.detection.key.KeyDetectorTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.detection.bpm.BpmDetectorTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.detection.beatintensity.BeatIntensityDetectorTest.class);        
        suite.addTestSuite(test.com.mixshare.rapid_evolution.audio.detection.rga.RGADetectorTest.class);        
        
        // data mining
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
