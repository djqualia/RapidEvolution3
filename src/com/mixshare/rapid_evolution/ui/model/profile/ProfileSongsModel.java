package com.mixshare.rapid_evolution.ui.model.profile;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.SongGroupProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;

public class ProfileSongsModel implements ModelPopulatorInterface {

	static private Logger log = Logger.getLogger(ProfileSongsModel.class);	
		
    ////////////
    // FIELDS //
    ////////////
    
    private SongGroupProfile relativeProfile;
    private Vector<Integer> songIds;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public ProfileSongsModel(SongGroupProfile relativeProfile) {
    	this.relativeProfile = relativeProfile;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
	public int getSize() {
		if (songIds == null)
			update();		
		return songIds.size();
	}
	
	public Iterator<Integer> getIdsIterator() {
		if (songIds == null)
			update();		
		return songIds.iterator();
	}
    
	/////////////
	// METHODS //
	/////////////
	
	public void update() {	
    	// TODO: there's got to be some better way to typecast the vectors so this isn't necessary
    	Vector<Integer> songIds = relativeProfile.getSongIds();
    	this.songIds = new Vector<Integer>(songIds.size());
    	for (Integer songId : songIds) {
    		SongRecord song = Database.getSongIndex().getSongRecord(songId);
    		if ((song != null) && !this.songIds.contains(song.getUniqueId()) && !song.isDisabled())
    			this.songIds.add(song.getUniqueId());		
    	}
	}
	
}
