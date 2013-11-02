package com.mixshare.rapid_evolution.ui.model.column.comparables;

import java.io.Serializable;

/**
 * This class makes the default sorting for integers greatest to least, and controls
 * the output to string (0 values are "")
 */
public class SmartInteger implements Comparable<SmartInteger>, Serializable {

    static private final long serialVersionUID = 0L;    	
	
	////////////
	// FIELDS //
	////////////
	
	private int value;
	private boolean hideZeroes;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public SmartInteger() { }
	public SmartInteger(int value) { this.value = value; }
	public SmartInteger(int value, boolean hideZeroes) {
		this.value = value;
		this.hideZeroes = hideZeroes;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public int getValue() { return value; }

	public boolean isHideZeroes() {
		return hideZeroes;
	}

	/////////////
	// SETTERS //
	/////////////

	public void setHideZeroes(boolean hideZeroes) {
		this.hideZeroes = hideZeroes;
	}
	public void setValue(int value) {
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
	
	public int compareTo(SmartInteger s) {
		if (value < s.value)
			return 1;
		if (value > s.value)
			return -1;
		return 0;
	}
	
}
