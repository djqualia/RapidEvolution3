package com.mixshare.rapid_evolution.workflow.mining.bbc;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.bbc.artist.BBCArtistProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class BBCArtistFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(BBCArtistFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private ArtistProfile artistProfile;
	private BBCArtistProfile bbcProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public BBCArtistFetchTask() { }
	
	public BBCArtistFetchTask(ArtistProfile artistProfile, int taskPriority) {
		this.artistProfile = artistProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_BBC);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("bbc_artist_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() {
		if ((bbcProfile != null) && (bbcProfile.isValid()))
			return bbcProfile;
		return null;
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getBBCAPI(); }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		artistProfile = (ArtistProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching bbc artist profile=" + artistProfile);
		try {
			bbcProfile = (BBCArtistProfile)MiningAPIFactory.getBBCAPI().getArtistProfile(artistProfile);		
			if ((bbcProfile != null) && bbcProfile.isValid())  {			
				artistProfile.addMinedProfile(bbcProfile);
				artistProfile.save();
			} else {
				artistProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_BBC), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for bbc");
		}
	}	
	
	public String toString() { return "BBCArtistFetchTask()=" + artistProfile; }
	
}
