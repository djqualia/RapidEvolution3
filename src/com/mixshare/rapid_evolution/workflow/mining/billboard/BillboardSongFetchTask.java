package com.mixshare.rapid_evolution.workflow.mining.billboard;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.billboard.song.BillboardSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class BillboardSongFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(BillboardSongFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private SongProfile songProfile;
	private BillboardSongProfile billboardProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public BillboardSongFetchTask() { }
	
	public BillboardSongFetchTask(SongProfile songProfile, int taskPriority) {
		this.songProfile = songProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_BILLBOARD);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("billboard_song_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() {
		if ((billboardProfile != null) && (billboardProfile.isValid()))
			return billboardProfile;
		return null;
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getBillboardAPI(); }	
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		songProfile = (SongProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching billboard song profile=" + songProfile);
		try {
			billboardProfile = (BillboardSongProfile)MiningAPIFactory.getBillboardAPI().getSongProfile(songProfile);		
			if ((billboardProfile != null) && billboardProfile.isValid()) {			
				songProfile.addMinedProfile(billboardProfile);
				songProfile.save();
			} else {
				songProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_BILLBOARD), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for billboard");
		}	
	}	
	
	public String toString() { return "BillboardSongFetchTask()=" + songProfile; }
	
}
