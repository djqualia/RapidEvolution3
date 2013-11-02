package com.mixshare.rapid_evolution.ui.model.profile;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;

public class ProfileArtistsModel implements ModelPopulatorInterface {

	static private Logger log = Logger.getLogger(ProfileArtistsModel.class);	
		
    ////////////
    // FIELDS //
    ////////////
    
    private Profile relativeProfile;
    private Vector<Integer> artistIds;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public ProfileArtistsModel(Profile relativeProfile) {
    	this.relativeProfile = relativeProfile;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
	public int getSize() {
		if (artistIds == null)
			update();
		return artistIds.size();
	}
	
	public Iterator<Integer> getIdsIterator() {
		if (artistIds == null)
			update();
		return artistIds.iterator();
	}
    
	public float getDegree(String artistName) {
    	if (relativeProfile instanceof LabelProfile) {
    		LabelProfile labelProfile = (LabelProfile)relativeProfile;
    		return labelProfile.getArtistDegree(artistName);
    	}
    	return 0.0f;
	}
	
	/////////////
	// METHDOS //
	/////////////
	
	public void update() {	
    	if (relativeProfile instanceof LabelProfile) {
    		artistIds = new Vector<Integer>();
    		LabelProfile labelProfile = (LabelProfile)relativeProfile;
			// code below makes sure to filter duplicates (from merges)
    		for (Integer artistId : labelProfile.getArtistIds()) {
    			ArtistRecord artistRecord = Database.getArtistIndex().getArtistRecord(artistId);
    			if (artistRecord != null) {
    				if (!artistIds.contains(artistRecord.getUniqueId()))
    					artistIds.add(artistRecord.getUniqueId());
    			}
    		}
    	} else if (relativeProfile instanceof FilterProfile) {
    		artistIds = new Vector<Integer>();
    		FilterProfile filterProfile = (FilterProfile)relativeProfile;
			// code below makes sure to filter duplicates (from merges)
    		for (SearchResult artist : filterProfile.getArtistRecords()) {
    			ArtistRecord artistRecord = (ArtistRecord)artist.getRecord();
    			if (artistRecord != null) {
    				if (!artistIds.contains(artistRecord.getUniqueId()))
    					artistIds.add(artistRecord.getUniqueId());
    			}
    		}    		
    	}
	}	
	
}
