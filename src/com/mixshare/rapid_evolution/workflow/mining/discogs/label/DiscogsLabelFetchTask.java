package com.mixshare.rapid_evolution.workflow.mining.discogs.label;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabelProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class DiscogsLabelFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(DiscogsLabelFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private LabelProfile labelProfile;
	private DiscogsLabelProfile discogsProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DiscogsLabelFetchTask() { }

	public DiscogsLabelFetchTask(LabelProfile labelProfile, int taskPriority, TaskResultListener resultListener) {
		this.labelProfile = labelProfile;
		this.taskPriority = taskPriority;
		setTaskResultListener(resultListener);
	}	
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_LABELS, DATA_SOURCE_DISCOGS);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("discogs_label_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() { return discogsProfile; }
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getDiscogsAPI(); }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		labelProfile = (LabelProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching discogs label profile=" + labelProfile);
		try {
			discogsProfile = (DiscogsLabelProfile)MiningAPIFactory.getDiscogsAPI().getLabelProfile(labelProfile);
			if ((discogsProfile != null) && discogsProfile.isValid()) {
				// NOTE: releases still need to be fetched before certain metadata is available, which occurs through a different process...				
				if (!discogsProfile.getName().equalsIgnoreCase(labelProfile.getLabelName()))
					labelProfile.setDiscogsLabelName(discogsProfile.getName(), DATA_SOURCE_DISCOGS, false);				
				labelProfile.addMinedProfile(discogsProfile);
				labelProfile.save();				
			} else {
				labelProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_LABELS, DATA_SOURCE_DISCOGS), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for discogs");		
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}	
	
	public String toString() { return "DiscogsLabelFetchTask()=" + labelProfile; }
	
}
