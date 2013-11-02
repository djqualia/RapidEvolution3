package com.mixshare.rapid_evolution.music.bpm;

import java.text.DecimalFormat;

public class BpmDifference implements Comparable<BpmDifference> {
	
	static private DecimalFormat decimalFormat = new DecimalFormat("0.##");
	
	static public BpmDifference INVALID = new BpmDifference(null);
	
	////////////
	// FIELDS //
	////////////
	
	private Float bpmDiff;
		
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public BpmDifference() { }
	
	public BpmDifference(Float bpmDiff) {
		this.bpmDiff = bpmDiff;
	}
	
	/////////////
	// GETTERS //
	/////////////

	public Float getBpmDiff() { return bpmDiff; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setBpmDiff(Float bpmDiff) { this.bpmDiff = bpmDiff; }	
		
	/////////////
	// METHODS //
	/////////////

	public String toString() {
		if (bpmDiff == null)
			return "";
		if (Float.isNaN(bpmDiff))
			return "";
		if (bpmDiff > 0.0f)
			return "+" + String.valueOf(decimalFormat.format(bpmDiff)) + "%";
		return String.valueOf(decimalFormat.format(bpmDiff)) + "%";
	}

	public int compareTo(BpmDifference b) {
		if ((bpmDiff != null) && (b.bpmDiff != null)) {
			if (bpmDiff < b.bpmDiff)
				return -1;
			if (bpmDiff > b.bpmDiff)
				return 1;
			return 0;
		} else if (bpmDiff != null) {
			return -1;
		} else if (b.bpmDiff != null) {
			return 1;
		}
		return 0;
	}
	
}
