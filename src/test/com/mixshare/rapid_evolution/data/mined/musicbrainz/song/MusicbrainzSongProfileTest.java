package test.com.mixshare.rapid_evolution.data.mined.musicbrainz.song;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.musicbrainz.song.MusicbrainzSongProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class MusicbrainzSongProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(MusicbrainzSongProfileTest.class);
	
	public void testMusicbrainzSongProfileBasic() {
		try {
			MusicbrainzSongProfile songProfile = new MusicbrainzSongProfile("Boards of Canada", "Roygbiv");

			checkProfile(songProfile);
			
			XMLSerializer.saveData(songProfile, "data/junit/temp/musicbrainz-song.xml");
			songProfile = (MusicbrainzSongProfile)XMLSerializer.readData("data/junit/temp/musicbrainz-song.xml");			    		
			
			checkProfile(songProfile);
			
		} catch (Exception e) {
			log.error("testMusicbrainzSongProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void checkProfile(MusicbrainzSongProfile songProfile) {
		if (!songProfile.getMbId().equals("874060fd-9cd9-4019-ae66-bfb3548fe1da"))
			fail("incorrect mbid, is=" + songProfile.getMbId());
		
		if (songProfile.getTagDegree("idm") == 0.0f)
			fail("incorrect tags?"); 
		if (songProfile.getTagDegree("country") > 0.0f)
			fail("incorrect tags?"); 
		
		if (songProfile.getNumRaters() == 0)
			fail("did not get # raters");
		if (songProfile.getAvgRating() < 3.0f)
			fail("avg rating incorrect");
		
		if (songProfile.getDuration() != 148000)
			fail("duration incorrect, is=" + songProfile.getDuration());
		
		if (!songProfile.containsPuid("3911aabf-8392-48a7-dbdf-62fbd76b5495"))
			fail("didn't fetch puids");
		
		if (!songProfile.containsReleaseTitle("Boc Maxima"))
			fail("didn't fetch release titles");		
	}
	
}
