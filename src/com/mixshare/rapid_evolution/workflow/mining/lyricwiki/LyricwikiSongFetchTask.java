package com.mixshare.rapid_evolution.workflow.mining.lyricwiki;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.lyricwiki.song.LyricwikiSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class LyricwikiSongFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(LyricwikiSongFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private SongProfile songProfile;
	private LyricwikiSongProfile lyricwikiProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public LyricwikiSongFetchTask() { }
	
	public LyricwikiSongFetchTask(SongProfile songProfile, int taskPriority) {
		this.songProfile = songProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_LYRICWIKI);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("lyricwiki_song_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() {
		if ((lyricwikiProfile != null) && (lyricwikiProfile.isValid()))
			return lyricwikiProfile;
		return null;
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getLyricwikiAPI(); }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		songProfile = (SongProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching lyricwiki song profile=" + songProfile);
		try {
			lyricwikiProfile = (LyricwikiSongProfile)MiningAPIFactory.getLyricwikiAPI().getSongProfile(songProfile);		
			if ((lyricwikiProfile != null) && lyricwikiProfile.isValid()) {			
				songProfile.addMinedProfile(lyricwikiProfile);			
				songProfile.save();
			} else {
				songProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_LYRICWIKI), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for lyricwiki");
		}				
			
	}	
	
	public String toString() { return "LyricwikiSongFetchTask()=" + songProfile; }
	
}