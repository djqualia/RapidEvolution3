package test.com.mixshare.rapid_evolution.data.mined.yahoo.artist;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.yahoo.artist.YahooArtistProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class YahooArtistProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(YahooArtistProfileTest.class);
	
	public void testYahooArtistProfileBasic() {
		try {
			YahooArtistProfile artistProfile = new YahooArtistProfile("Squarepusher");

			checkProfile(artistProfile);
			
			XMLSerializer.saveData(artistProfile, "data/junit/temp/yahoo-artist.xml");
			artistProfile = (YahooArtistProfile)XMLSerializer.readData("data/junit/temp/yahoo-artist.xml");			    		
			
			checkProfile(artistProfile);
							
		} catch (Exception e) {
			log.error("testYahooArtistProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void checkProfile(YahooArtistProfile artistProfile) {
		if (!artistProfile.containsCategory("Electronic/Dance"))
			fail("incorrect categories?");
		
		if (artistProfile.getSimilarityWith("wagon christ") == 0.0f)
			fail("incorrect similar artists");
		
		if (artistProfile.getSimilarityWith("garth brooks") != 0.0f)
			fail("incorrect similar artists");		
	}
	
}
