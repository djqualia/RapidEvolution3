package com.mixshare.rapid_evolution.workflow.maintenance.mined;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.echonest.artist.EchonestArtistProfile;
import com.mixshare.rapid_evolution.data.mined.idiomag.artist.IdiomagArtistProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.song.LastfmSongProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class EmptyMinedProfileRemover extends CommonTask {

	static private Logger log = Logger.getLogger(EmptyMinedProfileRemover.class);
    static private final long serialVersionUID = 0L;    	
		
	public String toString() {
		return "Removing Empty Mined Profiles";
	}    
    
	public void execute() {
		
		for (int artistId : Database.getArtistIndex().getIds()) {
			ArtistProfile artistProfile = (ArtistProfile)Database.getArtistIndex().getArtistProfile(artistId);
			if (artistProfile != null) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				EchonestArtistProfile echonestProfile = (EchonestArtistProfile)artistProfile.getMinedProfile(DATA_SOURCE_ECHONEST);
				if (echonestProfile == null) {					
					if (log.isDebugEnabled())
						log.debug("execute(): removing empty echonest header=" + artistProfile);
					// this will trigger an attempt to re-fetch the profile
					artistProfile.getArtistRecord().removeMinedProfileHeader(DATA_SOURCE_ECHONEST);				
				} else {
					if (log.isDebugEnabled())
						log.debug("execute(): found valid echonest profile=" + artistProfile);
				}
				IdiomagArtistProfile idiomagProfile = (IdiomagArtistProfile)artistProfile.getMinedProfile(DATA_SOURCE_IDIOMAG);
				if (idiomagProfile == null) {					
					if (log.isDebugEnabled())
						log.debug("execute(): removing empty idiomag header=" + artistProfile);
					// this will trigger an attempt to re-fetch the profile
					artistProfile.getArtistRecord().removeMinedProfileHeader(DATA_SOURCE_IDIOMAG);				
				} else {
					if (log.isDebugEnabled())
						log.debug("execute(): found valid idiomag profile=" + artistProfile);
				}
			}
		}

		for (int songId : Database.getSongIndex().getIds()) {
			if (RapidEvolution3.isTerminated || isCancelled())
				return;
			SongProfile songProfile = (SongProfile)Database.getSongIndex().getSongProfile(songId);
			if (songProfile != null) {
				LastfmSongProfile lastfmProfile = (LastfmSongProfile)songProfile.getMinedProfile(DATA_SOURCE_LASTFM);
				if (lastfmProfile != null) {
					if (lastfmProfile.getSimilarSongs().size() == 0) {
						if (log.isDebugEnabled())
							log.debug("execute(): removing empty lastfm profile=" + lastfmProfile);
						songProfile.removeMinedProfile(DATA_SOURCE_LASTFM);
						songProfile.save();
					}
				}
			}
		}
		
	}
		
	public boolean isIndefiniteTask() { return true; }
	
}
