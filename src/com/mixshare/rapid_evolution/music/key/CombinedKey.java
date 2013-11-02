package com.mixshare.rapid_evolution.music.key;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Combines start and end key values for use in a column.
 * 
 * Utilizes the fly-weight pattern due to finite range...  this class must remain immutable!
 */
public class CombinedKey implements Comparable<CombinedKey>, Serializable {
	
    static private final long serialVersionUID = 0L;    
	
    ////////////
    // FIELDS //
    ////////////
    
	private Key startKeyValue;
	private Key endKeyValue;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	// for serialization
	public CombinedKey() { }
	
	private CombinedKey(Key startKeyValue, Key endKeyValue) {
		this.startKeyValue = startKeyValue;
		this.endKeyValue = endKeyValue;
	}

	/////////////
	// GETTERS //
	/////////////
	
	public Key getStartKeyValue() { return startKeyValue; }
	public Key getEndKeyValue() { return endKeyValue; }
	
	/////////////
	// SETTERS //
	/////////////
	
    // for serialization
	public void setStartKeyValue(Key startKeyValue) { this.startKeyValue = startKeyValue; }
	public void setEndKeyValue(Key endKeyValue) { this.endKeyValue = endKeyValue; }
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		StringBuffer result = new StringBuffer();			
		if (startKeyValue != null)
			result.append(startKeyValue.toString());
		if ((endKeyValue != null) && (endKeyValue.isValid())) {
			if (result.length() > 0)
				result.append("->");
			result.append(endKeyValue.toString());
		}
		return result.toString();
	}
	
	public int compareTo(CombinedKey b) {
		int subCompare = startKeyValue.compareTo(b.startKeyValue);
		if (subCompare != 0)
			return subCompare;
		return endKeyValue.compareTo(b.endKeyValue);
	}
	
	public boolean equals(Object o) {
		if (o instanceof CombinedKey) {
			CombinedKey b = (CombinedKey)o;
			return ((startKeyValue.equals(b.startKeyValue)) && (endKeyValue.equals(b.endKeyValue)));
		}
		return false;
	}
	
	public int hashCode() { return toString().hashCode(); }
	    
	////////////////////////
	// FLY WEIGHT PATTERN //
	////////////////////////
	
    static public CombinedKey getCombinedKey(Key startKey, Key endKey) {
    	return checkFactory(startKey, endKey);
    }
    
    static private Map<String,CombinedKey> combinedKeyFlyWeights = new HashMap<String,CombinedKey>();
    
    static private String calculatePrimaryKey(Key startKey, Key endKey) {
        StringBuffer result = new StringBuffer();
        result.append(String.valueOf((int)(startKey.getRootValue() * 100)));
        result.append(",");
        result.append(startKey.getScaleType());
        result.append(",");
        result.append(String.valueOf((int)(endKey.getRootValue() * 100)));
        result.append(",");
        result.append(endKey.getScaleType());
        return result.toString();
    }	
    
    static private CombinedKey checkFactory(Key startKey, Key endKey) {
        String primaryKey = calculatePrimaryKey(startKey, endKey);
        CombinedKey result = (CombinedKey)combinedKeyFlyWeights.get(primaryKey);
        if (result == null) {
            result = new CombinedKey(startKey, endKey);
            combinedKeyFlyWeights.put(primaryKey, result);
        }
        return result;
    }
    
}

