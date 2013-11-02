package com.mixshare.rapid_evolution.workflow.mining.discogs.artist;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.discogs.DiscogsCommonProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.workflow.mining.discogs.DiscogsCommonReleaseFetchStateManager;

public class DiscogsArtistReleaseFetchStateManager extends DiscogsCommonReleaseFetchStateManager {

    static private Logger log = Logger.getLogger(DiscogsArtistReleaseFetchStateManager.class);
    static private final long serialVersionUID = 0L;    		
	
    ////////////
    // FIELDS //
    ////////////
    
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DiscogsArtistReleaseFetchStateManager() {
		super();
	}
	
	/**
	 * This constructor can be used to process a single label and then quit
	 */	
	public DiscogsArtistReleaseFetchStateManager(Profile currentProfile, DiscogsCommonProfile currentDiscogsProfile, int taskPriority) {
		super(currentProfile, currentDiscogsProfile, taskPriority);
	}
		
	/////////////
	// GETTERS //
	/////////////	
		
	protected Vector<Integer> getDiscogsProfilesForUpdating() {
		ArtistSearchParameters searchParams = (ArtistSearchParameters)Database.getArtistIndex().getNewSearchParameters();
		searchParams.setInternalItemsOnly(true);
		searchParams.setMinedDiscogsReleasesCutoff(System.currentTimeMillis() - getMinimumTimeBetweenQueries());
		searchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_RATING, CommonSearchParameters.SORT_BY_NUM_PLAYS, CommonSearchParameters.SORT_BY_INTERNAL_ITEMS });		
		Vector<SearchResult> searchResult = Database.getArtistIndex().searchRecords(searchParams);		
		Vector<Integer> result = new Vector<Integer>(searchResult.size());
		for (SearchResult record : searchResult)
			result.add(record.getRecord().getUniqueId());
		return result;
	}
	
	protected SearchProfile getSearchProfile(Integer profileId) { return Database.getArtistIndex().getArtistProfile(profileId); }
	
	/////////////
	// METHODS //
	/////////////

	public boolean finishProfile() {
		DiscogsArtistProfile currentDiscogsArtistProfile = (DiscogsArtistProfile)currentDiscogsProfile;
		ArtistProfile currentArtistProfile = (ArtistProfile)currentProfile;
		
		if (log.isDebugEnabled())
			log.debug("finishProfile(): fetched discogs releases for artist=" + currentArtistProfile);
		
		// done processing this artist...
		currentDiscogsArtistProfile.calculateMetadata(currentReleases);
		
		if (currentDiscogsArtistProfile.getName().equalsIgnoreCase(currentArtistProfile.getDiscogsArtistName())) { // this check prevents updating the artist if the user just corrected the discogs name..
			currentArtistProfile.addMinedProfile(currentDiscogsArtistProfile);				
			currentDiscogsArtistProfile.setLastFetchedReleaseIds();			
			currentArtistProfile.save();
			return true;
		} else {
			log.warn("finishProfile(): discogs artist name doesn't match (changed by user?), skipping update of profile");
		}
		return false;
	}	
	
}
