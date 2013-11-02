package com.mixshare.rapid_evolution.data.mined.yahoo;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

abstract public class YahooCommonProfile extends MinedProfile {

    ////////////
    // FIELDS //
    ////////////
    
    protected boolean isValid = false;
    protected Vector<String> categories = new Vector<String>();
	
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public YahooCommonProfile() {
    	super();
    }
    public YahooCommonProfile(byte dataType) {
    	super(new MinedProfileHeader(dataType, DATA_SOURCE_YAHOO));
    }
        
    /////////////
    // GETTERS //
    /////////////
    
    public Vector<DegreeValue> getStyleDegrees() {
    	Vector<DegreeValue> result = new Vector<DegreeValue>(categories.size());
    	for (String category : categories)
    		result.add(new DegreeValue(category, 1.0f, DATA_SOURCE_YAHOO));
    	return result;
    }
    
	public void setCategories(Vector<String> categories) {
		this.categories = categories;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}    
    
    /////////////
    // METHODS //
    /////////////
    
    public boolean isValid() { return isValid; }
	public boolean containsCategory(String category) { return categories.contains(category); }
	public Vector<String> getCategories() {
		return categories;
	}
	
}
