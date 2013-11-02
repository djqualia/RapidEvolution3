package test.com.mixshare.rapid_evolution.data.mined.lyricwiki.song;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.lyricwiki.song.LyricwikiSongProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class LyricwikiSongProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(LyricwikiSongProfileTest.class);
	
	public void testLyricwikiSongProfileBasic() {
		try {
			LyricwikiSongProfile songProfile = new LyricwikiSongProfile("Beck", "Loser");
			
			checkProfile(songProfile);
			
			XMLSerializer.saveData(songProfile, "data/junit/temp/lyricwiki-song.xml");
			songProfile = (LyricwikiSongProfile)XMLSerializer.readData("data/junit/temp/lyricwiki-song.xml");			    		
			
			checkProfile(songProfile);
			
		} catch (Exception e) {
			log.error("testLyricwikiSongProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}

	private void checkProfile(LyricwikiSongProfile songProfile) {
		if (!songProfile.getLyricsText().toLowerCase().startsWith("in the time of chimpanzees"))
			fail("lyrics incorrect");		
	}
	
}
