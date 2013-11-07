package test.com.mixshare.rapid_evolution.data.mined.discogs.release;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseRatings;

public class DiscogsReleaseRatingsTest extends RE3TestCase {

	public void testEquals() {
		
		DiscogsReleaseRatings ratings = MiningAPIFactory.getDiscogsAPI().getRelease("52211").getRatings();		
		if (ratings.getRatingForUser("LNFmusic.com") != 5)
			fail("incorrect rating");
		if (ratings.getRatingForUser("Say_Vegin") != 4)
			fail("incorrect rating");
		if (ratings.getRatingForUser("some unknown user") != -1)
			fail("false rating");
				
	}
	
}
