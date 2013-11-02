package test.com.mixshare.rapid_evolution.data.mined.echonest.artist;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.echonest.artist.EchonestArtistProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class EchonestArtistProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(EchonestArtistProfileTest.class);
	
	public void testEchonestArtistProfileBasic() {
		try {
			RE3Properties.setPropertyForTest("echonest_max_similar_per_query", 10);
			RE3Properties.setPropertyForTest("echonest_num_similar_to_retrieve", 100);

			EchonestArtistProfile artistProfile = new EchonestArtistProfile("Daft Punk");
			
			checkProfile(artistProfile);

			XMLSerializer.saveData(artistProfile, "data/junit/temp/echonest-artist.xml");
			artistProfile = (EchonestArtistProfile)XMLSerializer.readData("data/junit/temp/echonest-artist.xml");
			
			checkProfile(artistProfile);
			
		} catch (Exception e) {
			log.error("testEchonestArtistProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void checkProfile(EchonestArtistProfile artistProfile) {
		if (artistProfile.getSimilarityWith("Thomas bangalter") == 0.0f)
			fail("incorrect similarity, is=" + artistProfile.getSimilarityWith("Thomas bangalter"));
		if (artistProfile.getSimilarityWith("Garth Brooks") != 0.0f)
			fail("incorrect similarity");
		assertTrue(artistProfile.getSimilarArtists().size() > 10);
		
		if (artistProfile.getHotness() == 0.0f)
			fail("invalid hotness, is=" + artistProfile.getHotness());
		if (artistProfile.getFamiliarity() < 0.5f)
			fail("invalid familiarity");
		
		if (artistProfile.getUrl("mb_url") == null)
			fail("did not retrieve url");
		if (artistProfile.getUrl("itunes_url") == null)
			fail("did not retrieve url");

		if (artistProfile.getAudio().size() == 0)
			fail("failed to fetch audio items");

		assertTrue(artistProfile.getTags().size() > 0);
		
		if (RE3Properties.getBoolean("echonest_enable_videos_query")) {
			if (artistProfile.getVideos().size() == 0)
				fail("failed to fetch video items");
		}
	}
	
}
