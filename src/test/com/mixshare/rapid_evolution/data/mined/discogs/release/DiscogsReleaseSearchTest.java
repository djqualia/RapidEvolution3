package test.com.mixshare.rapid_evolution.data.mined.discogs.release;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;

public class DiscogsReleaseSearchTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(DiscogsReleaseSearchTest.class);

    public void testExistentSearches() { 
    	Integer releaseId = MiningAPIFactory.getDiscogsAPI().searchForReleaseId("polygon window", "surfing on sine waves");
    	if ((releaseId == null) || (releaseId.intValue() != 2040259))
    		fail("did not find release, is=" + releaseId);

    	releaseId = MiningAPIFactory.getDiscogsAPI().searchForReleaseId("dj frane", "frane's fantastic boatride");
    	if ((releaseId == null) || (releaseId.intValue() != 52211))
    		fail("did not find release, is=" + releaseId);
    	
    	releaseId = MiningAPIFactory.getDiscogsAPI().searchForReleaseId("Jona", "Ask / Use & Abuse");
    	if ((releaseId == null) || (releaseId.intValue() != 1319065))
    		fail("did not find release, is=" + releaseId);

    	releaseId = MiningAPIFactory.getDiscogsAPI().searchForReleaseId("quasimoto", "bus ride");
    	if ((releaseId == null) || (releaseId.intValue() != 1987885))
    		fail("did not find release, is=" + releaseId);
    
    }
    
    public void testNonExistentSearches() {    	
    	Integer releaseId = MiningAPIFactory.getDiscogsAPI().searchForReleaseId("various", "warp sampler 5");
    	if (releaseId != null)
    		fail("found bad/wrong release");
    }
        
}
