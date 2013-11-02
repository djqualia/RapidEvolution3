package com.mixshare.rapid_evolution.workflow.mining.musicbrainz;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.label.MusicbrainzLabelProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class MusicbrainzLabelFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(MusicbrainzLabelFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private LabelProfile labelProfile;
	private MusicbrainzLabelProfile musicbrainzProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public MusicbrainzLabelFetchTask() { }
	
	public MusicbrainzLabelFetchTask(LabelProfile labelProfile, int taskPriority) {
		this.labelProfile = labelProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_LABELS, DATA_SOURCE_MUSICBRAINZ);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("musicbrainz_label_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() {
		if ((musicbrainzProfile != null) && (musicbrainzProfile.isValid()))
			return musicbrainzProfile;
		return null;
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getMusicbrainzAPI(); }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		labelProfile = (LabelProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching musicbrainz label profile=" + labelProfile);
		try {
			musicbrainzProfile = (MusicbrainzLabelProfile)MiningAPIFactory.getMusicbrainzAPI().getLabelProfile(labelProfile);		
			if ((musicbrainzProfile != null) && musicbrainzProfile.isValid()) {			
				labelProfile.addMinedProfile(musicbrainzProfile);
				labelProfile.save();
			} else {
				labelProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_LABELS, DATA_SOURCE_MUSICBRAINZ), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for musicbrainz");
		}							
	}	
	
	public String toString() { return "MusicbrainzLabelFetchTask()=" + labelProfile; }
	
}