package com.mixshare.rapid_evolution.workflow.mining.lyricsfly;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.lyricsfly.song.LyricsflySongProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class LyricsflySongFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(LyricsflySongFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private SongProfile songProfile;
	private LyricsflySongProfile lyricsflyProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public LyricsflySongFetchTask() { }

	public LyricsflySongFetchTask(SongProfile songProfile, int taskPriority) {
		this.songProfile = songProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_LYRICSFLY);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("lyricsfly_song_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() {
		if ((lyricsflyProfile != null) && (lyricsflyProfile.isValid()))
			return lyricsflyProfile;
		return null;
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getLyricsflyAPI(); }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		songProfile = (SongProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching lyricsfly song profile=" + songProfile);
		try {
			lyricsflyProfile = (LyricsflySongProfile)MiningAPIFactory.getLyricsflyAPI().getSongProfile(songProfile);		
			if ((lyricsflyProfile != null) && lyricsflyProfile.isValid()) {						
				songProfile.addMinedProfile(lyricsflyProfile);			
				songProfile.save();
			} else {
				songProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_LYRICSFLY), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for lyricsfly");
		}							
	}	
	
	public String toString() { return "LyricsflySongFetchTask()=" + songProfile; }
	
}