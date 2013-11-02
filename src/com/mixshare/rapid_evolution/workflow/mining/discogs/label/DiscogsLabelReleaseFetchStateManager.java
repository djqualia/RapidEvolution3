package com.mixshare.rapid_evolution.workflow.mining.discogs.label;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.discogs.DiscogsCommonProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabelProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.workflow.mining.discogs.DiscogsCommonReleaseFetchStateManager;

public class DiscogsLabelReleaseFetchStateManager extends DiscogsCommonReleaseFetchStateManager {

    static private Logger log = Logger.getLogger(DiscogsLabelReleaseFetchStateManager.class);
    static private final long serialVersionUID = 0L;    		
	
    ////////////
    // FIELDS //
    ////////////
    
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DiscogsLabelReleaseFetchStateManager() {
		super();
	}
	
	/**
	 * This constructor can be used to process a single label and then quit
	 */
	public DiscogsLabelReleaseFetchStateManager(Profile currentProfile, DiscogsCommonProfile currentDiscogsProfile, int taskPriority) {
		super(currentProfile, currentDiscogsProfile, taskPriority);
	}
	
	/////////////
	// GETTERS //
	/////////////	
	
	protected Vector<Integer> getDiscogsProfilesForUpdating() {
		LabelSearchParameters searchParams = (LabelSearchParameters)Database.getLabelIndex().getNewSearchParameters();
		searchParams.setInternalItemsOnly(true);
		searchParams.setMinedDiscogsReleasesCutoff(System.currentTimeMillis() - getMinimumTimeBetweenQueries());
		searchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_RATING, CommonSearchParameters.SORT_BY_NUM_PLAYS, CommonSearchParameters.SORT_BY_INTERNAL_ITEMS });		
		Vector<SearchResult> searchResult = Database.getLabelIndex().searchRecords(searchParams);		
		Vector<Integer> result = new Vector<Integer>(searchResult.size());
		for (SearchResult record : searchResult)
			result.add(record.getRecord().getUniqueId());
		return result;
	}
	
	protected SearchProfile getSearchProfile(Integer profileId) { return Database.getLabelIndex().getLabelProfile(profileId); }
	
	/////////////
	// METHODS //
	/////////////

	public boolean finishProfile() {
		DiscogsLabelProfile currentDiscogsLabelProfile = (DiscogsLabelProfile)currentDiscogsProfile;
		LabelProfile currentLabelProfile = (LabelProfile)currentProfile;
		
		if (log.isDebugEnabled())
			log.debug("finishProfile(): fetched discogs releases for label=" + currentLabelProfile);
		
		// done processing this label...
		currentDiscogsLabelProfile.calculateMetadata(currentReleases);
	
		if (currentDiscogsLabelProfile.getName().equalsIgnoreCase(currentLabelProfile.getDiscogslLabelName())) {		
			currentLabelProfile.addMinedProfile(currentDiscogsLabelProfile);
			currentDiscogsLabelProfile.setLastFetchedReleaseIds();			
			currentLabelProfile.save();
			return true;
		} else {
			log.warn("finishProfile(): discogs label name doesn't match (changed by user?), skipping update of profile");
		}
		return false;
	}	
	
}
