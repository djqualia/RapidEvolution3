package com.mixshare.rapid_evolution.workflow.mining.musicbrainz;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.song.MusicbrainzSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class MusicbrainzSongFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(MusicbrainzSongFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private SongProfile songProfile;
	private MusicbrainzSongProfile musicbrainzProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public MusicbrainzSongFetchTask() { }
	
	public MusicbrainzSongFetchTask(SongProfile songProfile, int taskPriority) {
		this.songProfile = songProfile;
		this.taskPriority = taskPriority;
	}
		
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_MUSICBRAINZ);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("musicbrainz_song_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
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
		songProfile = (SongProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching musicbrainz song profile=" + songProfile);
		try {
			if (songProfile != null) {
				musicbrainzProfile = (MusicbrainzSongProfile)MiningAPIFactory.getMusicbrainzAPI().getSongProfile(songProfile);		
				if ((musicbrainzProfile != null) && musicbrainzProfile.isValid()) {			
					// musicbrainz profile
					songProfile.addMinedProfile(musicbrainzProfile);
					songProfile.save();
				} else {
					songProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_MUSICBRAINZ), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
				}
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for musicbrainz");
		}							
	}	
	
	public String toString() { return "MusicbrainzSongFetchTask()=" + songProfile; }
	
}