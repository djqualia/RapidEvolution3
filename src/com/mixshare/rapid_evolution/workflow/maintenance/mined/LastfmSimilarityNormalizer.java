package com.mixshare.rapid_evolution.workflow.maintenance.mined;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.lastfm.song.LastfmSongProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class LastfmSimilarityNormalizer extends CommonTask {

	static private Logger log = Logger.getLogger(LastfmSimilarityNormalizer.class);
    static private final long serialVersionUID = 0L;
	
    private boolean success = false;
    
    public LastfmSimilarityNormalizer() { }
    
	public String toString() {
		return "Normalizing Lastfm Similarities";
	}
    
	public void execute() {
		try {
			if (log.isDebugEnabled())
				log.debug("execute(): starting...");
			
			/*
			for (int searchId : Database.getArtistIndex().getIds()) {
				if (RapidEvolution3.isTerminated)
					return;
				SearchProfile profile = Database.getArtistIndex().getArtistProfile(searchId);
				if (profile != null) {
					LastfmArtistProfile lastfmProfile = (LastfmArtistProfile)profile.getMinedProfile(DATA_SOURCE_LASTFM);
					if (lastfmProfile != null) {
						if (log.isDebugEnabled())
							log.debug("execute(): fixing artist=" + profile);
						lastfmProfile.normalizeSimilarItems();
						profile.save();
					}
				}
			}
			*/
			
			for (int searchId : Database.getSongIndex().getIds()) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				SearchProfile profile = Database.getSongIndex().getSongProfile(searchId);
				if (profile != null) {
					LastfmSongProfile lastfmProfile = (LastfmSongProfile)profile.getMinedProfile(DATA_SOURCE_LASTFM);
					if (lastfmProfile != null) {
						if (log.isDebugEnabled())
							log.debug("execute(): fixing song=" + profile);
						lastfmProfile.normalizeSimilarItems();
						profile.save();
					}
				}
			}
			
			success = true;
			if (log.isDebugEnabled())
				log.debug("execute(): finished...");

		} catch (Exception e) {
			log.error("execute(): error", e);			
		}
	}
	
	public Object getResult() {
		return success;
	}
	
	public int getTaskPriority() {
		return RE3Properties.getInt("default_task_priority") + 20;
	}

	public boolean isIndefiniteTask() { return true; }
	
}
