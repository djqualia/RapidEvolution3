package test.com.mixshare.rapid_evolution.data.mined.lastfm.release;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.lastfm.artist.LastfmArtistProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.release.LastfmReleaseProfile;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;

public class LastfmReleaseProfileTest extends RE3TestCase {

    public void testLastfmReleaseProfileBasic() {
    	try {
    		LastfmReleaseProfile profile = new LastfmReleaseProfile("Aphex Twin", "Classics");
    		
    		checkProfile(profile);

			XMLSerializer.saveData(profile, "data/junit/temp/lastfm-release.xml");
			profile = (LastfmReleaseProfile)XMLSerializer.readData("data/junit/temp/lastfm-release.xml");			    		
    		
    		checkProfile(profile);    		
    		
    	} catch (Exception e) {
    		fail("testLastfmReleaseProfileBasic(): error=" + e);
    	}
    }
    
    private void checkProfile(LastfmReleaseProfile profile) {
		if (profile.getNumListeners() == 0.0f)
			fail("incorrect # listeners");
		if (profile.getUrl() == null)
			fail("incorrect url");    		    		
		if (profile.getImageURL() == null)
			fail("image url missing");    		
		if (profile.getReleasedDate() == null)
			fail("missing release date");
		if (profile.getNumSongs() != 13)
			fail("missing songs");
		if (profile.getPlayCount() == 0.0f)
			fail("0 play count");
		if (profile.getReachForSong("Digeridoo") == 0)
			fail("incorrect reach for album song");    	
    }
	
}
