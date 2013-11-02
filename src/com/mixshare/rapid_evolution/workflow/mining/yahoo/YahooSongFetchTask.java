package com.mixshare.rapid_evolution.workflow.mining.yahoo;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.mined.yahoo.song.YahooSongProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class YahooSongFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(YahooSongFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private SongProfile songProfile;
	private YahooSongProfile yahooProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public YahooSongFetchTask() { }
	
	public YahooSongFetchTask(SongProfile songProfile, int taskPriority) {
		this.songProfile = songProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_YAHOO);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("yahoo_song_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() {
		if ((yahooProfile != null) && (yahooProfile.isValid()))
			return yahooProfile;
		return null;
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getYahoomusicAPI(); }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		songProfile = (SongProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching yahoo song profile=" + songProfile);
		try {
			yahooProfile = (YahooSongProfile)MiningAPIFactory.getYahoomusicAPI().getSongProfile(songProfile);		
			if ((yahooProfile != null) && yahooProfile.isValid()) {			
				songProfile.addMinedProfile(yahooProfile);
				songProfile.save();
			} else {
				songProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_YAHOO), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for yahoo");
		}				
	}	
	
	public String toString() { return "YahooSongFetchTask()=" + songProfile; }
	
}
