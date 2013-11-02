package com.mixshare.rapid_evolution.ui.model.column.comparables;

import java.io.Serializable;

/**
 * This class makes the default sorting for longegers greatest to least, and controls
 * the output to string (0 values are "")
 */
public class SmartLong implements Comparable<SmartLong>, Serializable {

    static private final long serialVersionUID = 0L;    	
	
	////////////
	// FIELDS //
	////////////
	
	private long value;
	private boolean hideZeroes;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public SmartLong() { }
	public SmartLong(long value) { this.value = value; }
	public SmartLong(long value, boolean hideZeroes) {
		this.value = value;
		this.hideZeroes = hideZeroes;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public long getValue() { return value; }

	public boolean isHideZeroes() {
		return hideZeroes;
	}

	/////////////
	// SETTERS //
	/////////////

	public void setHideZeroes(boolean hideZeroes) {
		this.hideZeroes = hideZeroes;
	}
	public void setValue(long value) {
		this.value = value;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		if (hideZeroes && (value == 0))
			return "";
		return String.valueOf(value); 
	}
	
	public int compareTo(SmartLong s) {
		if (value < s.value)
			return 1;
		if (value > s.value)
			return -1;
		return 0;
	}
	
}
