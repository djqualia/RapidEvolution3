package com.mixshare.rapid_evolution.ui.model.column.comparables;

import java.io.Serializable;

/**
 * This class makes the default sorting for Floats greatest to least, and controls
 * the output to string (0 values are "")
 */
public class SmartFloat implements Comparable<SmartFloat>, Serializable {

    static private final long serialVersionUID = 0L;    	
	
	////////////
	// FIELDS //
	////////////
	
	private float value;
	private boolean hideZeroes;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public SmartFloat() { }
	public SmartFloat(float value) { this.value = value; }
	public SmartFloat(float value, boolean hideZeroes) {
		this.value = value;
		this.hideZeroes = hideZeroes;
	}

	/////////////
	// GETTERS //
	/////////////
	
	public float getValue() {
		return value;
	}
	public boolean isHideZeroes() {
		return hideZeroes;
	}

	/////////////
	// SETTERS //
	/////////////
	
	public void setValue(float value) {
		this.value = value;
	}
	public void setHideZeroes(boolean hideZeroes) {
		this.hideZeroes = hideZeroes;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		if (hideZeroes && (value == 0))
			return "";
		return String.valueOf(value); 
	}
	
	public int compareTo(SmartFloat s) {
		if (value < s.value)
			return 1;
		if (value > s.value)
			return -1;
		return 0;
	}
		
}
