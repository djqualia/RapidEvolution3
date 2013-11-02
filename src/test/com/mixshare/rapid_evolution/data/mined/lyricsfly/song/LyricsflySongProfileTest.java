package test.com.mixshare.rapid_evolution.data.mined.lyricsfly.song;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.lyricsfly.LyricsFlyAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.lyricsfly.song.LyricsflySongProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class LyricsflySongProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(LyricsflySongProfileTest.class);
	
	public void testLyricsflySongProfileBasic() {
		try {
			// this API doesn't seem to be available any more
			/*
			LyricsflySongProfile songProfile = LyricsFlyAPIWrapper.getLyricsflySongProfile("Beck", "Loser");
			
			checkProfile(songProfile);
			
			XMLSerializer.saveData(songProfile, "data/junit/temp/lyricsfly-song.xml");
			songProfile = (LyricsflySongProfile)XMLSerializer.readData("data/junit/temp/lyricsfly-song.xml");			    		
			
			checkProfile(songProfile);
			*/			
		} catch (Exception e) {
			log.error("testLyricsflySongProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void checkProfile(LyricsflySongProfile songProfile) {
		if (!songProfile.getLyricsText().toLowerCase().startsWith("in the time of chimpanzees"))
			fail("lyrics incorrect");
		
	}
	
}
