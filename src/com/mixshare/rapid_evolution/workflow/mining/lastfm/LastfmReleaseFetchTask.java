package com.mixshare.rapid_evolution.workflow.mining.lastfm;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.lastfm.release.LastfmReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class LastfmReleaseFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(LastfmReleaseFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private ReleaseProfile releaseProfile;
	private LastfmReleaseProfile lastfmProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public LastfmReleaseFetchTask() { }
	
	public LastfmReleaseFetchTask(ReleaseProfile releaseProfile, int taskPriority) {
		this.releaseProfile = releaseProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_RELEASES, DATA_SOURCE_LASTFM);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("lastfm_release_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() {
		if ((lastfmProfile != null) && (lastfmProfile.isValid()))
			return lastfmProfile;
		return null;
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getLastfmAPI(); }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		releaseProfile = (ReleaseProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching lastfm release profile=" + releaseProfile);
		try {
			lastfmProfile = (LastfmReleaseProfile)MiningAPIFactory.getLastfmAPI().getReleaseProfile(releaseProfile);		
			if ((lastfmProfile != null) && lastfmProfile.isValid()) {
				releaseProfile.addMinedProfile(lastfmProfile);
				releaseProfile.save();
			} else {
				releaseProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_RELEASES, DATA_SOURCE_LASTFM), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for lastfm");
		}							
	}	
	
	public String toString() { return "LastfmReleaseFetchTask()=" + releaseProfile; }
	
}
