package test.com.mixshare.rapid_evolution.data.mined.musicbrainz.label;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.musicbrainz.label.MusicbrainzLabelProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class MusicbrainzLabelProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(MusicbrainzLabelProfileTest.class);
	
	public void testMusicbrainzLabelProfileBasic() {
		try {
			MusicbrainzLabelProfile labelProfile = new MusicbrainzLabelProfile("Rephlex");

			checkProfile(labelProfile);
			
			XMLSerializer.saveData(labelProfile, "data/junit/temp/musicbrainz-label.xml");
			labelProfile = (MusicbrainzLabelProfile)XMLSerializer.readData("data/junit/temp/musicbrainz-label.xml");			    		
			
			checkProfile(labelProfile);
			
		} catch (Exception e) {
			log.error("testMusicbrainzLabelProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void checkProfile(MusicbrainzLabelProfile labelProfile) {
		if (!labelProfile.getMbId().equals("820fa04e-9927-40bb-a6c5-1bf883b159c7"))
			fail("incorrect mbid");
		
		if (labelProfile.getUrlsMap().size() == 0)
			fail("didn't fetch urls");
		if (!labelProfile.getUrls("OfficialSite").get(0).equals("http://www.rephlex.com/"))
			fail("incorrect url"); 

		if (labelProfile.getTagDegree("independent") == 0.0f)
			fail("incorrect tags?"); 
		if (labelProfile.getTagDegree("country") > 0.0f)
			fail("incorrect tags?"); 
		
		if (!labelProfile.getCountry().equals("GB"))
			fail("incorrect country code");
		
		if (!labelProfile.getLifespanBegin().equals("1991"))
			fail("incorrect lifespan begin");

		if (!labelProfile.getType().equals("OriginalProduction"))
			fail("incorrect label type");		
	}
	
}
