package test.com.mixshare.rapid_evolution.data.mined.lastfm.song;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.lastfm.release.LastfmReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.song.LastfmSongProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class LastfmSongProfileTest extends RE3TestCase {

    public void testLastfmSongProfileBasic() {
    	try {
    		LastfmSongProfile profile = new LastfmSongProfile("Aphex Twin", "Xtal");
    		
    		checkProfile(profile);
    		
			XMLSerializer.saveData(profile, "data/junit/temp/lastfm-song.xml");
			profile = (LastfmSongProfile)XMLSerializer.readData("data/junit/temp/lastfm-song.xml");			    		

			checkProfile(profile);
    		
    	} catch (Exception e) {
    		fail("testLastfmSongProfileBasic(): error=" + e);
    	}
    }	

    private void checkProfile(LastfmSongProfile profile) {
		if (profile.getNumListeners() == 0.0f)
			fail("incorrect # listeners");
		
		if (profile.getPlayCount() == 0.0f)
			fail("incorrect play count");
		
		if (profile.getUrl() == null)
			fail("incorrect url");
		    		
		if (profile.getImageURL() == null)
			fail("image url missing");
		
		if (profile.getReleaseName() == null)
			fail("incorrect release name");
		
		if (profile.getDuration() == 0)
			fail("incorrect duration");
		
		// tags
		if (profile.getNumTags() == 0)
			fail("missing top tags");
		if (profile.getTagDegree("ambient") == 0.0f)
			fail("tags incorrect");
		if (profile.getTagDegree("soft") == 0.0f)
			fail("tags incorrect");
		if (profile.getTagDegree("country music") != 0.0f)
			fail("tags incorrect");  
		
		// similar
		if (profile.getSimilarityWith("Aphex Twin", "Tha") == 0.0f)
			fail("incorrect track similarity");
		if (profile.getSimilarityWith("Aphex Twin", "Pulsewidth") == 0.0f)
			fail("incorrect track similarity");
		if (profile.getSimilarityWith("Garth Brooks", "Some country song") != 0.0f)
			fail("incorrect track similarity");    	
    }
    
}
