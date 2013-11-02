package test.com.mixshare.rapid_evolution.data.mined.idiomag.artist;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.idiomag.artist.IdiomagArtistProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class IdiomagArtistProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(IdiomagArtistProfileTest.class);
	
	public void testIdiomagArtistProfileBasic() {
		try {
			IdiomagArtistProfile artistProfile = new IdiomagArtistProfile("Minilogue");
			
			checkProfile(artistProfile);
			
			XMLSerializer.saveData(artistProfile, "data/junit/temp/idiomag-artist.xml");
			artistProfile = (IdiomagArtistProfile)XMLSerializer.readData("data/junit/temp/idiomag-artist.xml");			
			
			checkProfile(artistProfile);
			
		} catch (Exception e) {
			log.error("testIdiomagArtistProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}

	private void checkProfile(IdiomagArtistProfile artistProfile) {
		if (!artistProfile.isValid())
			fail("invalid artist profile");
		
		if (artistProfile.getSimilarityWith("audiofly") == 0.0f)
			fail("incorrect similarity");
		if (artistProfile.getSimilarityWith("guy gerber") == 0.0f)
			fail("incorrect similarity");
		if (artistProfile.getSimilarityWith("Garth Brooks") != 0.0f)
			fail("incorrect similarity");
		
		if (RE3Properties.getBoolean("idiomag_enable_videos_query")) {
			if (artistProfile.getVideos().size() == 0)
				fail("didn't fetch videos");
		}
		if (artistProfile.getArticles().size() == 0)
			fail("didn't fetch articles");
		if (artistProfile.getPhotoURLs().size() == 0)
			fail("didn't fetch photos");

		artistProfile = new IdiomagArtistProfile("Boards of Canada");
		
		if (artistProfile.getTagDegree("ambient") == 0.0f)
			fail("didn't fetch tags");
		if (artistProfile.getTagDegree("blarg") != 0.0f)
			fail("fetched bogus tag?");
		if (artistProfile.getURLs().size() == 0)
			fail("didn't fetch urls");
		
	}
	
}
