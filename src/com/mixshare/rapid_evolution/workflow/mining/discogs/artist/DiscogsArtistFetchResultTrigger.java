package com.mixshare.rapid_evolution.workflow.mining.discogs.artist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;

/**
 * This class is used to trigger a retrieval of an artist's releases from discogs after the main artist info.  This is used ad-hoc to load
 * an artist profile that the user is currently viewing...
 */
public class DiscogsArtistFetchResultTrigger implements TaskResultListener {
	
    static private Logger log = Logger.getLogger(DiscogsArtistFetchResultTrigger.class);    
	
	private ArtistProfile artistProfile;
	
	public DiscogsArtistFetchResultTrigger(ArtistProfile artistProfile) {
		this.artistProfile = artistProfile;
	}
	
	public void processResult(Object result) {
		if (log.isDebugEnabled())
			log.debug("processResult(): result=" + result);
		if (result != null)
			TaskManager.runBackgroundTask(new DiscogsArtistReleaseFetchStateManager(artistProfile, (DiscogsArtistProfile)result, RE3Properties.getInt("discogs_mining_task_priority") + 5));
	}
	
}
