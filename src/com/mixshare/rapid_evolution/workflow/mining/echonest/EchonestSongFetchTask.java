package com.mixshare.rapid_evolution.workflow.mining.echonest;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.echonest.song.EchonestSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class EchonestSongFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(EchonestSongFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private SongProfile songProfile;
	private EchonestSongProfile echonestProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public EchonestSongFetchTask() { }
		
	public EchonestSongFetchTask(SongProfile songProfile, int taskPriority) {
		this.songProfile = songProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_ECHONEST);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("echonest_song_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() {
		if ((echonestProfile != null) && (echonestProfile.isValid()))
			return echonestProfile;
		return null;
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getEchonestAPI(); }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		songProfile = (SongProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching echonest song profile=" + songProfile);
		try {
			echonestProfile = (EchonestSongProfile)MiningAPIFactory.getEchonestAPI().getSongProfile(songProfile);		
			if ((echonestProfile != null) && echonestProfile.isValid()) {
				// echonest profile
				songProfile.addMinedProfile(echonestProfile);						
				songProfile.save();
			} else {
				songProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_ECHONEST), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for echonest");
		}	
	}	
	
	public String toString() { return "EchonestSongFetchTask()=" + songProfile; }
	
}
