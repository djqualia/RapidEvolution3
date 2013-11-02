package test.com.mixshare.rapid_evolution.data.mined.bbc.artist;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.bbc.artist.BBCArtistProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class BBCArtistProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(BBCArtistProfileTest.class);
	
	public void testBBCArtistProfileBasic() {
		try {
			BBCArtistProfile artistProfile = new BBCArtistProfile("Boards of Canada");
			
			testBBCArtistProfileBasicSub(artistProfile);
			
			XMLSerializer.saveData(artistProfile, "data/junit/temp/bbc-artist.xml");
			artistProfile = (BBCArtistProfile)XMLSerializer.readData("data/junit/temp/bbc-artist.xml");
				
			testBBCArtistProfileBasicSub(artistProfile);

		} catch (Exception e) {
			log.error("testBBCArtistProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void testBBCArtistProfileBasicSub(BBCArtistProfile artistProfile) {
		if (!artistProfile.isValid())
			fail("invalid profile retrieved");
		
		if (artistProfile.getWikipediaContent().toLowerCase().indexOf("mike sandison") < 0)
			fail("invalid wikipedia content");		
	}
	
}
