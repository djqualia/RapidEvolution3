package com.mixshare.rapid_evolution.ui.model.column.comparables;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Vector;

/**
 * This class is used in such a way that strings can be sorted alphabetically, 
 * while putting null/empty values at the end, rather than beginning.
 */
public class SmartString implements Comparable<SmartString>, Serializable, Comparator {
		
    static private final long serialVersionUID = 0L;    	
	
    static public SmartString EMPTY_STRING = new SmartString("");
    
	////////////
	// FIELDS //
	////////////
	
	private String value;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SmartString() { }
	public SmartString(String value) {
		this.value = (value == null) ? "" : value;
	}
	public SmartString(Vector<?> values) {
		StringBuffer result = new StringBuffer();
		for (Object value : values) {
			if (result.length() > 0)
				result.append(", ");
			result.append(value.toString());
		}
		this.value = result.toString();
	}	
	public SmartString(float[] values) {
		StringBuffer result = new StringBuffer();
		for (float value : values) {
			if (result.length() > 0)
				result.append(", ");
			result.append(String.valueOf(value));
		}
		this.value = result.toString();		
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() { return value; }
	
	
	static public int compareStrings(String value, String svalue) {
		if (value == null)
			value = "";
		if (svalue == null)			
			svalue = "";
	    if ((value.length() == 0) && (svalue.length() == 0))
	    	return 0;
	    if (svalue.length() == 0)
	    	return -1;
	    if (value.length() == 0)
	    	return 1;
	    return (value.compareToIgnoreCase(svalue));		
	}
	
	public int compare(Object o1, Object o2) {		
		String value = o1.toString();
		String svalue = o2.toString();
	    if ((value.length() == 0) && (svalue.length() == 0))
	    	return 0;
	    if (svalue.length() == 0)
	    	return -1;
	    if (value.length() == 0)
	    	return 1;
	    return (value.compareToIgnoreCase(svalue));		
	}
	
	public int compareTo(SmartString s) {
	    if ((value.length() == 0) && (s.value.length() == 0))
	    	return 0;
	    if (s.value.length() == 0)
	    	return -1;
	    if (value.length() == 0)
	    	return 1;
	    return (value.compareToIgnoreCase(s.value));
	}
	
	public boolean equals(SmartString s) {
		return value.equalsIgnoreCase(s.value);
	}
	
	public int hashCode() {
		return value.toLowerCase().hashCode();
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
