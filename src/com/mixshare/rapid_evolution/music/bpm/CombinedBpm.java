package com.mixshare.rapid_evolution.music.bpm;

import java.io.Serializable;

/**
 * Combines start and end BPM values for use in a column.
 */
public class CombinedBpm implements Comparable<CombinedBpm>, Serializable {
	
    static private final long serialVersionUID = 0L;    
	
    ////////////
    // FIELDS //
    ////////////
    
	private float startBpmValue;
	private float endBpmValue;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public CombinedBpm() { }
	public CombinedBpm(float startBpmValue, float endBpmValue) {
		this.startBpmValue = startBpmValue;
		this.endBpmValue = endBpmValue;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public float getStartBpmValue() { return startBpmValue; }
	public float getEndBpmValue() { return endBpmValue; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setStartBpmValue(float startBpmValue) { this.startBpmValue = startBpmValue; }
	public void setEndBpmValue(float endBpmValue) { this.endBpmValue = endBpmValue; }
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() { return toString(false); }
		
	public String toString(boolean rounded) {
		StringBuffer result = new StringBuffer();	
		if (startBpmValue != 0.0f)
			result.append(rounded ? String.valueOf((int)Math.round(startBpmValue)) : String.valueOf(startBpmValue));
		if (endBpmValue != 0.0f) {
			if (result.length() > 0)
				result.append("->");
			result.append(rounded ? String.valueOf((int)Math.round(endBpmValue)) : String.valueOf(endBpmValue));
		}
		return result.toString();
	}
	
	public int compareTo(CombinedBpm b) {
		if ((startBpmValue != 0.0f) && (b.startBpmValue != 0.0f)) {
			if (startBpmValue < b.startBpmValue)
				return -1;
			if (startBpmValue > b.startBpmValue)
				return 1;			
			if (endBpmValue < b.endBpmValue)
				return -1;
			if (endBpmValue > b.endBpmValue)
				return 1;
		} else if (startBpmValue != 0.0f) {
			return -1;
		} else if (b.startBpmValue != 0.0f) {
			return 1;
		}
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o instanceof CombinedBpm) {
			CombinedBpm b = (CombinedBpm)o;
			return ((startBpmValue == b.startBpmValue) && (endBpmValue == b.endBpmValue));
		}
		return false;
	}
	
	public int hashCode() { return (int)(startBpmValue * 100 + endBpmValue * 100); }
		    
}
