package com.mixshare.rapid_evolution.data.search.parameters.filter;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;

abstract public class FilterSearchParameters extends CommonSearchParameters {

    static private Logger log = Logger.getLogger(FilterSearchParameters.class);
	
    ////////////
    // FIELDS //
    ////////////
    

    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public FilterSearchParameters() { }

    public FilterSearchParameters(FilterSearchParameters copy) {
    	super(copy);
    }
    
    /////////////
    // GETTERS //
    /////////////
    	    
    public String getUniqueHash() {
    	StringBuffer result = new StringBuffer();
    	result.append(super.getUniqueHash());
    	// insert class specific here
    	//result.append(queryKeySeperator);
    	
    	return result.toString();
    }       
    	
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

	protected int compareSub(SearchResult r1, SearchResult r2, byte sortType) {
		FilterRecord f1 = (FilterRecord)r1.getRecord();
		FilterRecord f2 = (FilterRecord)r2.getRecord();
		if (sortType == SORT_BY_NUM_ARTISTS) {
			int c1 = f1.getNumArtistRecordsCached();
			int c2 = f2.getNumArtistRecordsCached();
			if (c1 > c2)
				return -1;
			if (c1 < c2)
				return 1;
			return 0;
		}
		if (sortType == SORT_BY_NUM_LABELS) {
			int c1 = f1.getNumLabelRecordsCached();
			int c2 = f2.getNumLabelRecordsCached();
			if (c1 > c2)
				return -1;
			if (c1 < c2)
				return 1;
			return 0;
		}
		if (sortType == SORT_BY_NUM_RELEASES) {
			int c1 = f1.getNumReleaseRecordsCached();
			int c2 = f2.getNumReleaseRecordsCached();
			if (c1 > c2)
				return -1;
			if (c1 < c2)
				return 1;
			return 0;
		}
		if (sortType == SORT_BY_NUM_SONGS) {
			int c1 = f1.getNumSongRecordsCached();
			int c2 = f2.getNumSongRecordsCached();
			if (c1 > c2)
				return -1;
			if (c1 < c2)
				return 1;
			return 0;
		}		
		return super.compareSub(r1, r2, sortType);
	}
}
