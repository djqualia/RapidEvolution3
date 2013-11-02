package test.com.mixshare.rapid_evolution.data.mined.lastfm.artist;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.lastfm.artist.LastfmArtistProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class LastfmArtistProfileTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(LastfmArtistProfileTest.class);
	
    public void testLastfmArtistProfileBasic() {
    	try {
    		LastfmArtistProfile profile = new LastfmArtistProfile("Aphex Twin");
    		
    		checkProfile(profile);
    		
			XMLSerializer.saveData(profile, "data/junit/temp/lastfm-artist.xml");
			profile = (LastfmArtistProfile)XMLSerializer.readData("data/junit/temp/lastfm-artist.xml");			    		
    		
    		checkProfile(profile);
    		
    	} catch (Exception e) {
    		log.error("testLastfmArtistProfileBasic(): error", e);
    		fail("testLastfmArtistProfileBasic(): error=" + e);
    	}
    }	
    
    private void checkProfile(LastfmArtistProfile profile) {
		if (profile.getNumListeners() == 0.0f)
			fail("incorrect # listeners");
		
		if (profile.getPlayCount() == 0.0f)
			fail("incorrect play count");
		
		if (profile.getUrl() == null)
			fail("incorrect url");
		
		if ((profile.getWikiText() == null) || (profile.getWikiText().length() == 0))
			fail("incorrect wiki text");
		
		if ((profile.getImageURL() == null) || (profile.getImageURL().length() == 0))
			fail("image url missing");
		
		// similar artists
    	if (profile.getNumSimilarArtists() == 0)
    		fail("missing similar artists");
    	if (profile.getSimilarityWith("Autechre") == 0.0f)
    		fail("similar artists incorrect");
    	if (profile.getSimilarityWith("Squarepusher") == 0.0f)
    		fail("similar artists incorrect");
    	if (profile.getSimilarityWith("Garth Brooks") != 0.0f)
    		fail("similar artists incorrect");
    	
    	// top songs
		if (profile.getNumTopSongs() == 0)
			fail("missing top tracks");
		if (profile.getReachForSong("Xtal") == 0.0f)
			fail("top tracks incorrect");
		if (profile.getReachForSong("Avril 14th") == 0.0f)
			fail("top tracks incorrect");
		if (profile.getReachForSong("some nonexistent track") != 0.0f)
			fail("top tracks incorrect");    		
		
		// top releases
		if (profile.getNumTopReleases() == 0)
			fail("missing top albums");
		if (profile.getReachForRelease("Selected Ambient Works 85-92") == 0.0f)
			fail("top albums incorrect");
		if (profile.getReachForRelease("Richard D. James Album") == 0.0f)
			fail("top albums incorrect");
		if (profile.getReachForRelease("some nonexistent album") != 0.0f)
			fail("top albums incorrect");
		
		// tags
		if (profile.getNumTags() == 0)
			fail("missing top tags");
		if (profile.getTagDegree("electronic") == 0.0f)
			fail("tags incorrect");
		if (profile.getTagDegree("idm") == 0.0f)
			fail("tags incorrect");
		if (profile.getTagDegree("country music") != 0.0f)
			fail("tags incorrect");    	
    }
	
}
