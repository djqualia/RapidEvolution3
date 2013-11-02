package test.com.mixshare.rapid_evolution.data.index.search.label;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.index.search.label.LabelIndex;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class LabelIndexTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(LabelIndexTest.class);    
	
	public void testDuplicateLabels() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.setLabelName("warp");
						
			// song 2
			SubmittedSong submittedSong2 = new SubmittedSong("afx", "some compilation album", "10", "heliosphere", "original mix");
			submittedSong2.setLabelName("warp records");
						
			// add both songs
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);
			SongProfile songProfile2 = Database.getSongIndex().addSong(submittedSong2);
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 2) 
				fail("incorrect # artists");
			if (Database.getLabelIndex().getIds().size() != 2)
				fail("incorrect # labels");			
			if (Database.getReleaseIndex().getIds().size() != 2)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");			
			
			// get the label profiles and merge them
			LabelProfile labelProfile1 = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			LabelProfile labelProfile2 = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp records"));
			
			Database.mergeProfiles(labelProfile1, labelProfile2);
			
			// check the state of the db
			if (Database.getArtistIndex().getIds().size() != 2)
				fail("incorrect # artists");
			if (Database.getLabelIndex().getIds().size() != 1)
				fail("incorrect # labels");			
			if (Database.getReleaseIndex().getIds().size() != 2)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");				
			
			// get each song and make sure the label descriptions are updated
			songProfile1 = Database.getSongIndex().getSongProfile(submittedSong1.getSongIdentifier());			
			songProfile2 = Database.getSongIndex().getSongProfile(submittedSong2.getSongIdentifier());
			
			if (!songProfile1.getLabelsDescription().equals("warp"))
				fail("incorrect label description");
			if (!songProfile2.getLabelsDescription().equals("warp"))
				fail("incorrect label description");

			// make sure the new primary label contains both songs
			labelProfile1 = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			labelProfile2 = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp records"));
			if (!labelProfile1.getIdentifier().equals(labelProfile2.getIdentifier()))
				fail("old label being returned");
			
			if (!labelProfile1.containsSong(submittedSong1.getSongIdentifier()))
				fail("label does not contain song");
			if (!labelProfile1.containsSong(submittedSong2.getSongIdentifier()))
				fail("label does not contain song");
			
			// check that releases behave correctly			
			ReleaseProfile release1 = Database.getReleaseIndex().getReleaseProfile(submittedSong1.getSubmittedRelease().getReleaseIdentifier());
			ReleaseProfile release2 = Database.getReleaseIndex().getReleaseProfile(submittedSong2.getSubmittedRelease().getReleaseIdentifier());
			
			if (!release1.getLabelsDescription().equals("warp"))
				fail("incorrect label name on release, is=" + release1.getLabelsDescription());
			if (!release2.getLabelsDescription().equals("warp"))
				fail("incorrect label name on release, is=" + release2.getLabelsDescription());	
			
		} catch (Exception e) {
			log.error("testDuplicateLabels(): error", e);
			fail(e.getMessage());			
		}
	}

	/**
	 * This method tests updating an label's name
	 */
	public void testLabelUpdate() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.setLabelName("warp");

			// add the song (and indirectly create the label)
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);			
			if (!songProfile1.getLabelsDescription().equals("warp"))
				fail("incorrect label on song");
			
			LabelProfile labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			if (!labelProfile.getLabelName().equals("warp"))
				fail("incorrect label name");

			ReleaseProfile releaseProfile = Database.getReleaseIndex().getReleaseProfile(submittedSong1.getSubmittedRelease().getReleaseIdentifier());
			if (!releaseProfile.getLabelsDescription().equals("warp"))
				fail("incorrect label name on release");

			ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (artistProfile.getLabelIds().length != 1)
				fail("incorrect # of labels on artist");
			if (artistProfile.getLabelDegree("warp") != 1.0f)
				fail("incorrect label degree on artist, is=" + artistProfile.getLabelDegree("warp"));
			
			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
			// now change the label's name
			labelProfile.setLabelName("warp records");

			// check state of db
			if (Database.getLabelIndex().getIds().size() != 1) 
				fail("incorrect # labels");
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	

			// ensure the new label names shows up in all the related profiles
			if (!labelProfile.getLabelName().equals("warp records"))
				fail("incorrect label name");
			
			if (!songProfile1.getLabelsDescription().equals("warp records"))
				fail("incorrect label on song");
			
			if (!releaseProfile.getLabelsDescription().equals("warp records"))
				fail("incorrect label on song");
			
			if (artistProfile.getLabelDegree("warp records") != 1.0f)
				fail("incorrect label name on artist");
			if (artistProfile.getLabelDegree("warp") != 0.0f)
				fail("incorrect label name on artist");
			
			if (Database.getLabelIndex().getRecord(new LabelIdentifier("warp records")) == null)
				fail("couldn't find by new label name");
			if (Database.getLabelIndex().getRecord(labelProfile.getUniqueId()) == null)
				fail("couldn't find by unique id");
			
		} catch (Exception e) {
			log.error("testLabelUpdate(): error", e);
			fail(e.getMessage());
		}
	}
	
	public void testCustomSerialization() {
		try {
			testLabelUpdate();
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/labelindex.jso");			
			Database.getLabelIndex().write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/labelindex.jso");
			new LabelIndex(lineReader);
			lineReader.close();
		} catch (Exception e) {
			log.error("testCustomSerialization(): error", e);
			fail(e.getMessage());	
		}
	}
	
}
