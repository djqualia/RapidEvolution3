package com.mixshare.rapid_evolution.ui.model.profile;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;

public class ProfileLabelsModel implements ModelPopulatorInterface {

	static private Logger log = Logger.getLogger(ProfileLabelsModel.class);	
		
    ////////////
    // FIELDS //
    ////////////
    
    private Profile relativeProfile;
    private Vector<Integer> labelIds;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public ProfileLabelsModel(Profile relativeProfile) {
    	this.relativeProfile = relativeProfile;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
	public int getSize() {
		if (labelIds == null)
			update();
		return labelIds.size();
	}
	
	public Iterator<Integer> getIdsIterator() {
		if (labelIds == null)
			update();
		return labelIds.iterator();
	}
    
	public float getDegree(String labelName) {
    	if (relativeProfile instanceof ArtistProfile) {
    		ArtistProfile artistProfile = (ArtistProfile)relativeProfile;
    		return artistProfile.getLabelDegree(labelName);
    	}
    	return 0.0f;
	}	
	
	/////////////
	// METHDOS //
	/////////////
	
	public void update() {	
    	if (relativeProfile instanceof ArtistProfile) {
    		labelIds = new Vector<Integer>();
    		ArtistProfile artistProfile = (ArtistProfile)relativeProfile;
    		for (Integer labelId : artistProfile.getLabelIds()) {
    			LabelRecord labelRecord = Database.getLabelIndex().getLabelRecord(labelId);
    			if (labelRecord != null) {
    				if (!labelIds.contains(labelRecord.getUniqueId()))
    					labelIds.add(labelRecord.getUniqueId());
    			}
    		}    		
    	} else if (relativeProfile instanceof FilterProfile) {
    		labelIds = new Vector<Integer>();
    		FilterProfile filterProfile = (FilterProfile)relativeProfile;
    		for (SearchResult label : filterProfile.getLabelRecords()) {
    			LabelRecord labelRecord = (LabelRecord)label.getRecord();
    			if (labelRecord != null) {
    				if (!labelIds.contains(labelRecord.getUniqueId()))
    					labelIds.add(labelRecord.getUniqueId());
    			}
    		}
    	}
	}
	
}
