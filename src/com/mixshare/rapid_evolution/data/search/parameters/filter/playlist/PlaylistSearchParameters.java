package com.mixshare.rapid_evolution.data.search.parameters.filter.playlist;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.search.parameters.filter.FilterSearchParameters;

public class PlaylistSearchParameters extends FilterSearchParameters {

    static private Logger log = Logger.getLogger(PlaylistSearchParameters.class);
	
    ////////////
    // FIELDS //
    ////////////
    

    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public PlaylistSearchParameters() { }

    public PlaylistSearchParameters(PlaylistSearchParameters copy) {
    	super(copy);
    }
    
    /////////////
    // GETTERS //
    /////////////
    	    
    public byte getDataType() { return DATA_TYPE_PLAYLISTS; }
    
    public String getUniqueHash() {
    	StringBuffer result = new StringBuffer();
    	result.append(super.getUniqueHash());
    	// insert class specific here
    	//result.append(queryKeySeperator);
    	
    	return result.toString();
    }       
    
    public Index getIndex() { return Database.getPlaylistIndex(); }
    	
    /////////////
    // SETTERS //
    /////////////
    
    	
	/////////////
	// METHODS //
	/////////////
	
	public float matches(Record record, boolean fullCheck) {	
		float superScore = super.matches(record, fullCheck);
		if (superScore > 0.0f) {
			// type specific matches here
			return superScore;
		} else {
			return 0.0f;		
		}
	}

	public boolean isEmpty() {
		if (!super.isEmpty())
			return false;
		// type specific checks here
		return true;
	}	

	public void addSearchFields(Vector<String> searchFields) {
		super.addSearchFields(searchFields);
		searchFields.add("name");
	}
	
}
