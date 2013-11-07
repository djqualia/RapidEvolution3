package com.mixshare.rapid_evolution.workflow.mining.discogs.release;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;

public class DiscogsReleaseProfileFetchResultTrigger implements TaskResultListener {
	
    static private Logger log = Logger.getLogger(DiscogsReleaseProfileFetchResultTrigger.class);    
	
	private ReleaseProfile releaseProfile;
	
	public DiscogsReleaseProfileFetchResultTrigger(ReleaseProfile releaseProfile) {
		this.releaseProfile = releaseProfile;
	}
	
	public void processResult(Object result) {
		if (log.isDebugEnabled())
			log.debug("processResult(): result=" + result);
		if (result != null)
			TaskManager.runBackgroundTask(new DiscogsReleaseRecommendationsFetchStateManager(releaseProfile, (DiscogsReleaseProfile)result, RE3Properties.getInt("discogs_mining_task_priority") + 5));
	}
	
}
