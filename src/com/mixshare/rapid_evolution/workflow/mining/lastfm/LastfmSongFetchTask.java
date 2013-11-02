package com.mixshare.rapid_evolution.workflow.mining.lastfm;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.lastfm.song.LastfmSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class LastfmSongFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(LastfmSongFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private SongProfile songProfile;
	private LastfmSongProfile lastfmProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public LastfmSongFetchTask() { }
	
	public LastfmSongFetchTask(SongProfile songProfile, int taskPriority) {
		this.songProfile = songProfile;
		this.taskPriority = taskPriority;
	}	
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_LASTFM);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("lastfm_song_minimum_query_interval_days") * 1000 * 60 * 60 * 24;		
	}
	
	public Object getResult() {
		if ((lastfmProfile != null) && (lastfmProfile.isValid()))
			return lastfmProfile;
		return null;
	}	
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getLastfmAPI(); }
	
	/////////////
	// METHODS //
	/////////////
	
	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		songProfile = (SongProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching lastfm song profile=" + songProfile);
		SongIdentifier songId = songProfile.getSongIdentifier();
		try {
			lastfmProfile = (LastfmSongProfile)MiningAPIFactory.getLastfmAPI().getSongProfile(songProfile);		
			if ((lastfmProfile != null) && lastfmProfile.isValid()) {
				
				if (!songProfile.isExternalItem() && RE3Properties.getBoolean("automatically_add_external_items")) {
					// similar songs
					for (LastfmSongProfile similarSong : lastfmProfile.getSimilarSongs()) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						float similarity = lastfmProfile.getSimilarityWith(similarSong.getArtistName(), similarSong.getSongTitle());
						if (similarity >= RE3Properties.getFloat("lastfm_min_song_similarity_to_fetch")) {										
							SubmittedSong submittedSong = new SubmittedSong(similarSong.getArtistName(), similarSong.getReleaseName(), similarSong.getReleaseTrack(), similarSong.getSongTitle(), "");
							submittedSong.addMinedProfile(similarSong);
							submittedSong.setExternalItem(true);
							try {
								Database.getSongIndex().addOrUpdate(submittedSong);
							} catch (Exception e) {
								log.error("execute(): error adding/updating external song=" + submittedSong, e);
							}
						}
					}
				}
				
				songProfile.addMinedProfile(lastfmProfile);
				songProfile.save();
			} else {
				songProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_LASTFM), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for lastfm");
		}							
	}	
	
	public String toString() { return "LastfmSongFetchTask()=" + songProfile; }
	
}
