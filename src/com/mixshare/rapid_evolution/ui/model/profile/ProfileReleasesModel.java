package com.mixshare.rapid_evolution.ui.model.profile;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.ReleaseGroupProfile;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;

public class ProfileReleasesModel implements ModelPopulatorInterface {

	static private Logger log = Logger.getLogger(ProfileReleasesModel.class);	
		
    ////////////
    // FIELDS //
    ////////////
    
    private ReleaseGroupProfile relativeProfile;
    private Vector<Integer> releaseIds;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public ProfileReleasesModel(ReleaseGroupProfile relativeProfile) {
    	this.relativeProfile = relativeProfile;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
	public int getSize() {
		if (releaseIds == null)
			update();
		return releaseIds.size(); }
	
	public Iterator<Integer> getIdsIterator() {
		if (releaseIds == null)
			update();
		return releaseIds.iterator(); 
	}
    
	/////////////
	// METHODS //
	/////////////
	
	public void update() {	
    	// TODO: there's got to be some better way to typecast the vectors so this isn't necessary
    	Vector<Integer> releaseIds = relativeProfile.getReleaseIds();
    	this.releaseIds = new Vector<Integer>(releaseIds.size());
    	for (Integer releaseId : releaseIds) {
    		ReleaseRecord release = Database.getReleaseIndex().getReleaseRecord(releaseId);
    		if ((release != null) && !this.releaseIds.contains(release.getUniqueId()) && !release.isDisabled())
    			this.releaseIds.add(release.getUniqueId());		
    	}
	}
	
}
