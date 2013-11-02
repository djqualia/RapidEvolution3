package com.mixshare.rapid_evolution.music.key;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Combines start and end key code values for use in a column.
 *
 * Utilizes the fly-weight pattern due to finite range...  this class must remain immutable! 
 */
public class CombinedKeyCode implements Comparable<CombinedKeyCode>, Serializable {
	
    static private final long serialVersionUID = 0L;    
	
    ////////////
    // FIELDS //
    ////////////
    
	private KeyCode startKeyCodeValue;
	private KeyCode endKeyCodeValue;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	// for serialization
	public CombinedKeyCode() { }
	
	private CombinedKeyCode(KeyCode startKeyCodeValue, KeyCode endKeyCodeValue) {
		this.startKeyCodeValue = startKeyCodeValue;
		this.endKeyCodeValue = endKeyCodeValue;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public KeyCode getStartKeyCodeValue() { return startKeyCodeValue; }
	public KeyCode getEndKeyCodeValue() { return endKeyCodeValue; }
	
	/////////////
	// SETTERS //
	/////////////
	
    // for serialization
	public void setStartKeyCodeValue(KeyCode startKeyCodeValue) { this.startKeyCodeValue = startKeyCodeValue; }
	public void setEndKeyCodeValue(KeyCode endKeyCodeValue) { this.endKeyCodeValue = endKeyCodeValue; }
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		StringBuffer result = new StringBuffer();			
		if (startKeyCodeValue != null)
			result.append(startKeyCodeValue.toString());
		if ((endKeyCodeValue != null) && (endKeyCodeValue.isValid())) {
			if (result.length() > 0)
				result.append("->");
			result.append(endKeyCodeValue.toString());
		}
		return result.toString();
	}
	
	public int compareTo(CombinedKeyCode b) {
		int subCompare = startKeyCodeValue.compareTo(b.startKeyCodeValue);
		if (subCompare != 0)
			return subCompare;
		return endKeyCodeValue.compareTo(b.endKeyCodeValue);
	}
	
	public boolean equals(Object o) {
		if (o instanceof CombinedKeyCode) {
			CombinedKeyCode b = (CombinedKeyCode)o;
			return ((startKeyCodeValue.equals(b.startKeyCodeValue)) && (endKeyCodeValue.equals(b.endKeyCodeValue)));
		}
		return false;
	}
	
	public int hashCode() { return toString().hashCode(); }
		 
	////////////////////////
	// FLY WEIGHT PATTERN //
	////////////////////////
	
    static public CombinedKeyCode getCombinedKeyCode(KeyCode startKeyCode, KeyCode endKeyCode) {
    	return checkFactory(startKeyCode, endKeyCode);
    }
    
    static private Map<String,CombinedKeyCode> combinedKeyCodeFlyWeights = new HashMap<String,CombinedKeyCode>();
    
    static private String calculatePrimaryKeyCode(KeyCode startKeyCode, KeyCode endKeyCode) {
        StringBuffer result = new StringBuffer();
        result.append(startKeyCode.getKeyValue());
        result.append(",");
        result.append(startKeyCode.getScaleType());
        result.append(",");
        result.append(startKeyCode.getShift());
        result.append(",");
        result.append(endKeyCode.getKeyValue());
        result.append(",");
        result.append(endKeyCode.getScaleType());
        result.append(",");
        result.append(endKeyCode.getShift());
        return result.toString();
    }	
    
    static private CombinedKeyCode checkFactory(KeyCode startKeyCode, KeyCode endKeyCode) {
        String primaryKeyCode = calculatePrimaryKeyCode(startKeyCode, endKeyCode);
        CombinedKeyCode result = (CombinedKeyCode)combinedKeyCodeFlyWeights.get(primaryKeyCode);
        if (result == null) {
            result = new CombinedKeyCode(startKeyCode, endKeyCode);
            combinedKeyCodeFlyWeights.put(primaryKeyCode, result);
        }
        return result;
    }
    
}

