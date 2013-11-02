package com.mixshare.rapid_evolution.workflow.mining.discogs.label;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;

/**
 * This class is used to trigger a retrieval of a label's releases from discogs after the main label info.  This is used ad-hoc to load
 * a label profile that the user is currently viewing...
 */
public class DiscogsLabelFetchResultTrigger implements TaskResultListener {
	
	private LabelProfile labelProfile;
	
	public DiscogsLabelFetchResultTrigger(LabelProfile labelProfile) {
		this.labelProfile = labelProfile;
	}
	
	public void processResult(Object result) {
		if (result != null)
			TaskManager.runBackgroundTask(new DiscogsLabelReleaseFetchStateManager(labelProfile, (DiscogsLabelProfile)result, RE3Properties.getInt("discogs_mining_task_priority") + 5));
	}
	
}
