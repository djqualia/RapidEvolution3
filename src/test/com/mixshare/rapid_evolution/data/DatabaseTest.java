package test.com.mixshare.rapid_evolution.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.index.filter.style.StyleIndex;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.data.util.table.UniqueIdTable;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.filter.style.StyleModelManager;

public class DatabaseTest extends RE3TestCase {
	
    static private Logger log = Logger.getLogger(DatabaseTest.class);    
    
    ///////////
    // TESTS //
    ///////////
    
    /**
     * This tests adding a song, and making sure the related artist/release/label records are added.
     * It also tests that when removing a song, if there are no more associated songs for the related
     * artist/release/labels that they are removed.
     */
	public void testBasicRelations() {
		try {
			// song 1
			SubmittedSong song = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			song.setLabelName("rephlex");
						
			// song 2			
			SubmittedSong song2 = new SubmittedSong("aphex twin", "selected ambient works", "b2", "i", "");
			song2.setLabelName("rephlex");
			
			// add song 1 and make sure the related artist/label/releases are added			
			Database.add(song);

			if (!Database.getArtistIndex().doesExist(new ArtistIdentifier("aphex twin")))
				fail("artist was not created");
			if (!Database.getLabelIndex().doesExist(new LabelIdentifier("rephlex")))
				fail("label was not created");
			if (!Database.getReleaseIndex().doesExist(new ReleaseIdentifier("aphex twin", "selected ambient works")))
				fail("release was not created");
			if (!Database.getSongIndex().doesExist(song.getSongIdentifier()))
				fail("song was not created");
			
			// delete song 1 and make sure the related artist/label/releases are removed
			Database.delete(song.getSongIdentifier());
			
			if (Database.getArtistIndex().doesExist(new ArtistIdentifier("aphex twin")))
				fail("artist was not deleted");
			if (Database.getLabelIndex().doesExist(new LabelIdentifier("rephlex")))
				fail("label was not deleted");
			if (Database.getReleaseIndex().doesExist(new ReleaseIdentifier("aphex twin", "selected ambient works")))
				fail("release was not deleted");
			if (Database.getSongIndex().doesExist(song.getSongIdentifier()))
				fail("song was not deleted");			
			
			// now add both songs and then remove just 1			
			Database.add(song);
			Database.add(song2);
			
			// first make sure everything was created properly
			if (!Database.getArtistIndex().doesExist(new ArtistIdentifier("aphex twin")))
				fail("artist was not created");
			if (!Database.getLabelIndex().doesExist(new LabelIdentifier("rephlex")))
				fail("label was not created");
			if (!Database.getReleaseIndex().doesExist(new ReleaseIdentifier("aphex twin", "selected ambient works")))
				fail("release was not created");
			if (!Database.getSongIndex().doesExist(song.getSongIdentifier()))
				fail("song was not created");
			if (!Database.getSongIndex().doesExist(song2.getSongIdentifier()))
				fail("song2 was not created");
			
			if (Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin")).getReleaseIds().size() != 1)
				fail("incorrect releases for artist");

			if (Database.getLabelIndex().getLabelProfile(new LabelIdentifier("rephlex")).getReleaseIds().size() != 1)
				fail("incorrect releases for label");
			
			if (Database.getReleaseIndex().getReleaseProfile(song.getSubmittedRelease().getReleaseIdentifier()).getSongIds().size() != 2)
				fail("incorrect songs for release");
			
			
			// delete just 1, make sure artist/label/release remain (since another song reference still exists)
			Database.delete(song.getSongIdentifier());
						
			if (!Database.getArtistIndex().doesExist(new ArtistIdentifier("aphex twin")))
				fail("artist was deleted");
			if (!Database.getLabelIndex().doesExist(new LabelIdentifier("rephlex")))
				fail("label was deleted");
			if (!Database.getReleaseIndex().doesExist(new ReleaseIdentifier("aphex twin", "selected ambient works")))
				fail("release was deleted");
			if (Database.getSongIndex().doesExist(song.getSongIdentifier()))
				fail("song was not deleted");
			if (!Database.getSongIndex().doesExist(song2.getSongIdentifier()))
				fail("song2 was deleted");
			
			if (Database.getReleaseIndex().getReleaseProfile(song.getSubmittedRelease().getReleaseIdentifier()).getSongIds().size() != 1)
				fail("incorrect songs for release");
			
			// remove the remaining song and make sure all is cleared			
			Database.delete(song2.getSongIdentifier());
			
			if (Database.getArtistIndex().doesExist(new ArtistIdentifier("aphex twin")))
				fail("artist was not deleted");
			if (Database.getLabelIndex().doesExist(new LabelIdentifier("rephlex")))
				fail("label was not deleted");
			if (Database.getReleaseIndex().doesExist(new ReleaseIdentifier("aphex twin", "selected ambient works")))
				fail("release was not deleted");
			if (Database.getSongIndex().doesExist(song2.getSongIdentifier()))
				fail("song was not deleted");		
			
		} catch (Exception e) {
			log.error("testBasicRelations(): error", e);
			fail(e.getMessage());
		}
	}
	
	/**
	 * This tests that when a song is added, its associated styles and tags are also created.
	 * It also tests that when a song is removed, if the related styles/tags have no more associated songs,
	 * that they are removed as well.
	 */
	public void testStyleAndTagRelations() {
		try {
			// song 1
			SubmittedSong song = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			song.setLabelName("rephlex");
			song.addStyleDegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN);
			song.addStyleDegreeValue("Downtempo", 0.75f, DATA_SOURCE_UNKNOWN);
			song.addStyleDegreeValue("Electronica", 1.0f, DATA_SOURCE_UNKNOWN);
			song.addTagDegreeValue("AFX", 1.0f, DATA_SOURCE_UNKNOWN);
			song.addTagDegreeValue("Chill", 0.75f, DATA_SOURCE_UNKNOWN);
			song.addTagDegreeValue("Trippy", 0.5f, DATA_SOURCE_UNKNOWN);
			
			// song 2			
			SubmittedSong song2 = new SubmittedSong("aphex twin", "selected ambient works", "b2", "i", "");
			song2.setLabelName("rephlex");
			song2.addStyleDegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN);
			song2.addStyleDegreeValue("Thinking Music", 0.75f, DATA_SOURCE_UNKNOWN);
			song2.addStyleDegreeValue("Electronica", 1.0f, DATA_SOURCE_UNKNOWN);
			song2.addTagDegreeValue("AFX", 1.0f, DATA_SOURCE_UNKNOWN);
			song2.addTagDegreeValue("Chill", 0.75f, DATA_SOURCE_UNKNOWN);
			song2.addTagDegreeValue("Meditation", 0.5f, DATA_SOURCE_UNKNOWN);
						
			// add song 1 and make sure styles/tags were created
			Database.add(song);
			
			if (!Database.getStyleIndex().doesExist(new StyleIdentifier("Ambient")))
				fail("style was not created");
			if (!Database.getStyleIndex().doesExist(new StyleIdentifier("Downtempo")))
				fail("style was not created");
			if (!Database.getStyleIndex().doesExist(new StyleIdentifier("Electronica")))
				fail("style was not created");
			if (!Database.getTagIndex().doesExist(new TagIdentifier("AFX")))
				fail("tag was not created");
			if (!Database.getTagIndex().doesExist(new TagIdentifier("Chill")))
				fail("tag was not created");
			if (!Database.getTagIndex().doesExist(new TagIdentifier("Trippy")))
				fail("tag was not created");
						
			// add song 2 and make sure the new styles/tags are created
			Database.add(song2);

			if (!Database.getStyleIndex().doesExist(new StyleIdentifier("Thinking Music")))
				fail("style was not created");
			if (!Database.getTagIndex().doesExist(new TagIdentifier("Meditation")))
				fail("tag was not created");						
			
			// test after deleting song1 if styles/tags with no remaining songs are deleted, and the others remain...			
			Database.delete(song.getSongIdentifier());
			
			if (!Database.getStyleIndex().doesExist(new StyleIdentifier("Ambient")))
				fail("style was deleted");
			if (Database.getStyleIndex().doesExist(new StyleIdentifier("Downtempo")))
				fail("style was not deleted");
			if (!Database.getStyleIndex().doesExist(new StyleIdentifier("Electronica")))
				fail("style was deleted");
			if (!Database.getTagIndex().doesExist(new TagIdentifier("AFX")))
				fail("tag was deleted");
			if (Database.getTagIndex().doesExist(new TagIdentifier("Trippy")))
				fail("tag was not deleted");
			if (!Database.getTagIndex().doesExist(new TagIdentifier("Chill")))
				fail("tag was deleted");			
			if (!Database.getStyleIndex().doesExist(new StyleIdentifier("Thinking Music")))
				fail("style was deleted");
			if (!Database.getTagIndex().doesExist(new TagIdentifier("Meditation")))
				fail("tag was deleted");				
			
			// delete the remaining song and make sure all is cleared
			Database.delete(song2.getSongIdentifier());
			
			if (Database.getStyleIndex().doesExist(new StyleIdentifier("Ambient")))
				fail("style was not deleted");
			if (Database.getStyleIndex().doesExist(new StyleIdentifier("Electronica")))
				fail("style was not deleted");
			if (Database.getTagIndex().doesExist(new TagIdentifier("AFX")))
				fail("tag was not deleted");
			if (Database.getTagIndex().doesExist(new TagIdentifier("Trippy")))
				fail("tag was not deleted");			
			if (Database.getStyleIndex().doesExist(new StyleIdentifier("Thinking Music")))
				fail("style was not deleted");
			if (Database.getTagIndex().doesExist(new TagIdentifier("Meditation")))
				fail("tag was not deleted");
			
		} catch (Exception e) {
			log.error("testStyleAndTagRelations(): error", e);
			fail(e.getMessage());
		}
	}						
	
	
}
