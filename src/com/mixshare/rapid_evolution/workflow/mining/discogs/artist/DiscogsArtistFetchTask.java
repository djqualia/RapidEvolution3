package com.mixshare.rapid_evolution.workflow.mining.discogs.artist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class DiscogsArtistFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(DiscogsArtistFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private ArtistProfile artistProfile;
	private DiscogsArtistProfile discogsProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DiscogsArtistFetchTask() { }
	
	public DiscogsArtistFetchTask(ArtistProfile artistProfile, int taskPriority, TaskResultListener resultListener) {
		this.artistProfile = artistProfile;
		this.taskPriority = taskPriority;
		setTaskResultListener(resultListener);
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getDiscogsAPI(); }	
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_DISCOGS);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("discogs_artist_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() { return discogsProfile; }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		artistProfile = (ArtistProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching discogs artist profile=" + artistProfile);
		try {
			discogsProfile = (DiscogsArtistProfile)MiningAPIFactory.getDiscogsAPI().getArtistProfile(artistProfile);
			if ((discogsProfile != null) && discogsProfile.isValid()) {				
				// NOTE: releases still need to be fetched before certain metadata is available, which occurs through a different process...
				if (!discogsProfile.getName().equalsIgnoreCase(artistProfile.getArtistName()))
					artistProfile.setDiscogsArtistName(discogsProfile.getName(), DATA_SOURCE_DISCOGS, false);				
				artistProfile.addMinedProfile(discogsProfile);
				artistProfile.save();				
			} else {
				artistProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_DISCOGS), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}		
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for discogs");		
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}	
	
	public String toString() { return "DiscogsArtistFetchTask()=" + artistProfile; }
	
}
