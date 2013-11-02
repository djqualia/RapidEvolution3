package test.com.mixshare.rapid_evolution.data.mined.musicbrainz.artist;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.musicbrainz.artist.MusicbrainzArtistProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class MusicbrainzArtistProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(MusicbrainzArtistProfileTest.class);
	
	public void testMusicbrainzArtistProfileBasic() {
		try {
			MusicbrainzArtistProfile artistProfile = new MusicbrainzArtistProfile("Minilogue");

			checkProfile(artistProfile);
			
			artistProfile = new MusicbrainzArtistProfile("Daft Punk");
			
			checkProfile2(artistProfile);
						
		} catch (Exception e) {
			log.error("testMusicbrainzArtistProfileBasic(): error", e);
			fail(e.getMessage());
		}
	}
	
	private void checkProfile(MusicbrainzArtistProfile artistProfile) {
		if (!artistProfile.getMbId().equals("56a70482-5528-4bef-8da2-a33cf098ce9b"))
			fail("incorrect mbid");
		
		if (artistProfile.getUrlsMap().size() == 0)
			fail("didn't fetch urls");
		if (!artistProfile.getUrls("OfficialHomepage").get(0).equals("http://www.minilogue.com/"))
			fail("incorrect url"); 

		/*
		if (artistProfile.getTagDegree("minimal") < 0.25f)
			fail("incorrect tags, are=" + artistProfile.getTagDegrees()); 
		if (artistProfile.getTagDegree("tech house") < 0.25f)
			fail("incorrect tags, are=" + artistProfile.getTagDegrees()); 
		if (artistProfile.getTagDegree("country") > 0.0f)
			fail("incorrect tags, are=" + artistProfile.getTagDegrees()); 
		*/
		
		if (artistProfile.getReleaseProfiles().size() == 0)
			fail("did not fetch release profiles");

		if (!artistProfile.getRelatedArtists().contains("Marcus Henriksson"))
			fail("did not fetch related artists");
		if (!artistProfile.getRelatedArtists().contains("Sebastian Mullaert"))
			fail("did not fetch related artists");
		
	}
	
	private void checkProfile2(MusicbrainzArtistProfile artistProfile) {
		if (!artistProfile.getMbId().equals("056e4f3e-d505-4dad-8ec1-d04f521cbb56"))
			fail("incorrect mbid");

		if (artistProfile.getTagDegree("electronic") < 0.1f)
			fail("incorrect tags, are=" + artistProfile.getTagDegrees()); 
		if (artistProfile.getTagDegree("france") == 0.0f)
			fail("incorrect tags, are=" + artistProfile.getTagDegrees()); 
		if (artistProfile.getTagDegree("country") > 0.0f)
			fail("incorrect tags, are=" + artistProfile.getTagDegrees()); 		
		
	}
	
	
}
