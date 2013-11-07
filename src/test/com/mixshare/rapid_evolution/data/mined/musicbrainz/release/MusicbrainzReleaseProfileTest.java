package test.com.mixshare.rapid_evolution.data.mined.musicbrainz.release;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.musicbrainz.release.MusicbrainzReleaseProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class MusicbrainzReleaseProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(MusicbrainzReleaseProfileTest.class);
	
	public void testMusicbrainzReleaseProfileBasic() {
		try {
			MusicbrainzReleaseProfile releaseProfile = new MusicbrainzReleaseProfile("Aphex Twin", "Drukqs");

			checkProfile(releaseProfile);
			
			XMLSerializer.saveData(releaseProfile, "data/junit/temp/musicbrainz-release.xml");
			releaseProfile = (MusicbrainzReleaseProfile)XMLSerializer.readData("data/junit/temp/musicbrainz-release.xml");			    		
			
			checkProfile(releaseProfile);
			
		} catch (Exception e) {
			log.error("testMusicbrainzReleaseProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void checkProfile(MusicbrainzReleaseProfile releaseProfile) {
		if (!releaseProfile.getMbId().equals("4d35da90-c4f9-4d37-b68a-027225fca9cd"))
			fail("incorrect mbid, is=" + releaseProfile.getMbId());
		
//		if (releaseProfile.getUrlsMap().size() == 0)
//			fail("didn't fetch urls");
//		if (!releaseProfile.getUrls("AmazonAsin").get(0).equals("http://www.amazon.com/gp/product/B00005Y1TL"))
//			fail("incorrect url"); 

		if (releaseProfile.getTagDegree("electronic") == 0.0f)
			fail("incorrect tags?"); 
		if (releaseProfile.getTagDegree("country") > 0.0f)
			fail("incorrect tags?"); 
		
		if (releaseProfile.getNumRaters() == 0)
			fail("did not get # raters");
		if (releaseProfile.getAvgRating() < 3.0f)
			fail("avg rating incorrect, is=" + releaseProfile.getAvgRating());
		
//		if (!releaseProfile.getAmazonId().equals("B00005Y1TL"))
//			fail("amazon id incorrect");
		
		if (!releaseProfile.getOriginalYearReleased().equals("2001"))
			fail("incorrect original year released, is=" + releaseProfile.getOriginalYearReleased());

		if (releaseProfile.getSongs().size() == 0)
			fail("didn't get songs");
		if (!releaseProfile.getSongs().get(1).getSongName().equals("Vordhosbn"))
			fail("didn't fetch right song");		
	}
	
}
