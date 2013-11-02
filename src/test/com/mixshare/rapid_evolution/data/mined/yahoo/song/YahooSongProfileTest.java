package test.com.mixshare.rapid_evolution.data.mined.yahoo.song;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.yahoo.song.YahooSongProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class YahooSongProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(YahooSongProfileTest.class);
	
	public void testYahooSongProfileBasic() {
		try {
			YahooSongProfile songProfile = new YahooSongProfile("Michael Jackson", "Billie Jean");

			checkProfile(songProfile);
			
			XMLSerializer.saveData(songProfile, "data/junit/temp/yahoo-song.xml");
			songProfile = (YahooSongProfile)XMLSerializer.readData("data/junit/temp/yahoo-song.xml");			    		
			
			checkProfile(songProfile);
			
		} catch (Exception e) {
			log.error("testYahooSongProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void checkProfile(YahooSongProfile songProfile) {
		//if (!songProfile.containsCategory("Pop"))
			//fail("incorrect categories?, are=" + songProfile.getCategories());		
	}
	
}
