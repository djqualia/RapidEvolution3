package test.com.mixshare.rapid_evolution.data.index.search.artist;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.index.search.artist.ArtistIndex;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class ArtistIndexTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(ArtistIndexTest.class);    
	
	public void testDuplicateArtists() {
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
			
			// get the artist profiles and merge them
			ArtistProfile artistProfile1 = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			ArtistProfile artistProfile2 = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("afx"));
			
			Database.mergeProfiles(artistProfile1, artistProfile2);
			
			// check the state of the db
			if (Database.getArtistIndex().getIds().size() != 1)
				fail("incorrect # artists");
			if (Database.getLabelIndex().getIds().size() != 2)
				fail("incorrect # labels");			
			if (Database.getReleaseIndex().getIds().size() != 2)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 2)
				fail("incorrect # songs");	
			
			// get each song and make sure the artist descriptions are updated
			songProfile1 = Database.getSongIndex().getSongProfile(submittedSong1.getSongIdentifier());			
			songProfile2 = Database.getSongIndex().getSongProfile(submittedSong2.getSongIdentifier());
			
			if (!songProfile1.getArtistsDescription().equals("aphex twin"))
				fail("incorrect artist description");
			if (!songProfile2.getArtistsDescription().equals("aphex twin"))
				fail("incorrect artist description, id=" + songProfile2.getArtistsDescription());
			
			if (!songProfile1.toString().equals("aphex twin - selected ambient works  [d1]  heliosphere"))
				fail("incorrect toString, is=" + songProfile1.toString());
			if (!songProfile2.toString().equals("aphex twin - some compilation album  [10]  heliosphere (original mix)"))
				fail("incorrect toString, is=" + songProfile2.toString());
			
			// make sure the new primary artist contains both songs
			artistProfile1 = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			artistProfile2 = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("afx"));
			if (!artistProfile1.getIdentifier().equals(artistProfile2.getIdentifier()))
				fail("old artist being returned");
			
			if (!artistProfile1.containsSong(submittedSong1.getSongIdentifier()))
				fail("artist does not contain song");
			if (!artistProfile1.containsSong(submittedSong2.getSongIdentifier()))
				fail("artist does not contain song");
			
			// check that releases behave correctly						
			ReleaseProfile release1 = Database.getReleaseIndex().getReleaseProfile(submittedSong1.getSubmittedRelease().getReleaseIdentifier());
			ReleaseProfile release2 = Database.getReleaseIndex().getReleaseProfile(submittedSong2.getSubmittedRelease().getReleaseIdentifier());
			
			if (!release1.getArtistsDescription().equals("aphex twin"))
				fail("incorrect artist name on release, is=" + release1.getArtistsDescription());
			if (!release2.getArtistsDescription().equals("aphex twin"))
				fail("incorrect artist name on release, is=" + release2.getArtistsDescription());
			
			if (!release1.toString().equals("aphex twin - selected ambient works"))
				fail("release toString incorrect");
			if (!release2.toString().equals("aphex twin - some compilation album"))
				fail("release toString incorrect, is=" + release2.toString());
								
		} catch (Exception e) {
			log.error("testDuplicateArtist(): error", e);
			fail(e.getMessage());
		}
	}
	
	/**
	 * This method tests updating an artist's name
	 */
	public void testArtistUpdate() {
		try {
			// song 1
			SubmittedSong submittedSong1 = new SubmittedSong("aphex twin", "selected ambient works", "d1", "heliosphere", "");
			submittedSong1.setLabelName("warp");

			// add the song (and indirectly create the artist)
			SongProfile songProfile1 = Database.getSongIndex().addSong(submittedSong1);			
			if (!songProfile1.getArtistsDescription().equals("aphex twin"))
				fail("incorrect artist on song");
			
			ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(new ArtistIdentifier("aphex twin"));
			if (!artistProfile.getArtistName().equals("aphex twin"))
				fail("incorrect artist name");

			ReleaseProfile releaseProfile = Database.getReleaseIndex().getReleaseProfile(submittedSong1.getSubmittedRelease().getReleaseIdentifier());
			if (!releaseProfile.getArtistsDescription().equals("aphex twin"))
				fail("incorrect artist name on release");

			LabelProfile labelProfile = Database.getLabelIndex().getLabelProfile(new LabelIdentifier("warp"));
			if (labelProfile.getArtistDegree("aphex twin") != 1.0f)
				fail("incorrect artist name on label");
			
			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1) 
				fail("incorrect # artists");
			if (Database.getLabelIndex().getIds().size() != 1)
				fail("incorrect # labels");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	
			
			// now change the artist's name
			artistProfile.setArtistName("afx");

			// check state of db
			if (Database.getArtistIndex().getIds().size() != 1) 
				fail("incorrect # artists");
			if (Database.getLabelIndex().getIds().size() != 1)
				fail("incorrect # labels");			
			if (Database.getReleaseIndex().getIds().size() != 1)
				fail("incorrect # releases");
			if (Database.getSongIndex().getIds().size() != 1)
				fail("incorrect # songs");	

			// ensure the new artist names shows up in all the related profiles
			if (!artistProfile.getArtistName().equals("afx"))
				fail("incorrect artist name");
			
			if (!songProfile1.getArtistsDescription().equals("afx"))
				fail("incorrect artist on song");
			
			if (!releaseProfile.getArtistsDescription().equals("afx"))
				fail("incorrect artist on song");
			
			if (labelProfile.getArtistDegree("afx") != 1.0f)
				fail("incorrect artist name on label");
			if (labelProfile.getArtistDegree("aphex twin") != 0.0f)
				fail("incorrect artist name on label");
			
			if (Database.getArtistIndex().getRecord(new ArtistIdentifier("afx")) == null)
				fail("couldn't find by new artist name");
			if (Database.getArtistIndex().getRecord(artistProfile.getUniqueId()) == null)
				fail("couldn't find by unique id");
			
		} catch (Exception e) {
			log.error("testArtistUpdate(): error", e);
			fail(e.getMessage());	
		}
	}
	
	public void testCustomSerialization() {
		try {
			testArtistUpdate();
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/artistindex.jso");			
			Database.getArtistIndex().write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/artistindex.jso");
			new ArtistIndex(lineReader);
			lineReader.close();
		} catch (Exception e) {
			log.error("testCustomSerialization(): error", e);
			fail(e.getMessage());	
		}
	}
	
}
