package test.com.mixshare.rapid_evolution.data.index.filter.tag;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.index.filter.tag.TagIndex;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class TagIndexTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(TagIndexTest.class);    
	
    /**
     * This tests merging tags and the effects on the related songs.
     */
	public void testDuplicateTagsOnSongs() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.setLabelName("rephlex");
			submittedSong1.addTagDegreeValue("chill", 1.0f, DATA_SOURCE_UNKNOWN);
			
			// song 2
			SubmittedSong submittedSong2 = new SubmittedSong("aphex twin", "some compilation album", "10", "heliosphere", "original mix");
			submittedSong2.setLabelName("warp");
			submittedSong2.addTagDegreeValue("chillout", 1.0f, DATA_SOURCE_UNKNOWN);
			
			// song 3
			SubmittedSong submittedSong3 = new SubmittedSong("aphex twin", "selected ambient works volume 2", "a1", "stone in focus", "");
			submittedSong3.addTagDegreeValue("chill", 1.0f, DATA_SOURCE_UNKNOWN);
			submittedSong3.addTagDegreeValue("chillout", 1.0f, DATA_SOURCE_UNKNOWN);
			
			// add the songs then get the created tag profiles
			Database.add(submittedSong1);
			Database.add(submittedSong2);
			Database.add(submittedSong3);
			
			TagProfile chill = Database.getTagIndex().getTagProfile(new TagIdentifier("chill"));
			TagProfile chillout = Database.getTagIndex().getTagProfile(new TagIdentifier("chillout"));
			
			// merge the tags and then ensure each song now belongs to the primary tag
			Database.mergeProfiles(chill, chillout);

			TagRecord chillRecord = Database.getTagIndex().getTagRecord(new TagIdentifier("chill"));
			TagRecord chilloutRecord = Database.getTagIndex().getTagRecord(new TagIdentifier("chillout"));			
			if (!chillRecord.equals(chilloutRecord))
				fail("old tag record returned");
			
			SongRecord song1 = Database.getSongIndex().getSongRecord(submittedSong1.getSongIdentifier());			
			SongRecord song2 = Database.getSongIndex().getSongRecord(submittedSong2.getSongIdentifier());			
			SongRecord song3 = Database.getSongIndex().getSongRecord(submittedSong3.getSongIdentifier());			
			
			if (!chillRecord.matches(song1))
				fail("song doesn't belong to tag");
			if (!chillRecord.matches(song2))
				fail("song doesn't belong to tag");
			if (!chillRecord.matches(song3))
				fail("song doesn't belong to tag");
			
			// check tag descriptions on songs
			if (!song1.getActualTagDescription().equals("chill (100%)"))
				fail("incorrect tag description, is=" + song1.getActualTagDescription());
			if (!song1.getSourceTagDescription().equals("chill (100%)"))
				fail("incorrect tag description, is=" + song1.getSourceTagDescription());
			if (!song2.getActualTagDescription().equals("chill (100%)"))
				fail("incorrect tag description, is=" + song2.getActualTagDescription());
			if (!song2.getSourceTagDescription().equals("chillout (100%)"))
				fail("incorrect tag description, is=" + song2.getSourceTagDescription());			
			if (!song3.getActualTagDescription().equals("chill (100%)"))
				fail("incorrect tag description, is=" + song3.getActualTagDescription());
			if (!song3.getSourceTagDescription().equals("chill (100%); chillout (100%)"))
				fail("incorrect tag description, is=" + song3.getSourceTagDescription());
			
			// check # exact/actuals
			if (song3.getNumSourceTags() != 2)
				fail("incorrect exact tags");
			if (!song3.getSourceTagDegreeValues().toString().equals("[chill (100%), chillout (100%)]"))
				fail("incorrect exact tags, is=" + song3.getSourceTagDegreeValues());
			if (!song3.containsSourceTag("chillout"))
				fail("incorrect exact tags");
			if (!song3.containsSourceTag("chill"))
				fail("incorrect exact tags");
			
			if (song3.getNumActualTags() != 1)
				fail("incorrect actual tags");
			if (!song3.getActualTagDegreeValues().toString().equals("[chill (100%)]"))
				fail("incorrect exact tags");
			if (!song3.containsActualTag("chillout"))
				fail("incorrect exact tags");
			if (!song3.containsActualTag("chill"))
				fail("incorrect exact tags");					
			
		} catch (Exception e) {
			log.error("testDuplicateTagsOnSongs(): error", e);
			fail(e.getMessage());
		}
	}
	
	public void testCustomSerialization() {
		try {
			testDuplicateTagsOnSongs();
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/tagindex.jso");			
			Database.getTagIndex().write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/tagindex.jso");
			new TagIndex(lineReader);
			lineReader.close();
		} catch (Exception e) {
			log.error("testCustomSerialization(): error", e);
			fail(e.getMessage());	
		}
	}

	
}
