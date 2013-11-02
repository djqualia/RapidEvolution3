package com.mixshare.rapid_evolution.ui.model.column.comparables;

import java.io.Serializable;

public class SmartDate implements Comparable<SmartDate>, Serializable {

    static private final long serialVersionUID = 0L;    	
	
	////////////
	// FIELDS //
	////////////
	
	private String label;
	private long time;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public SmartDate() { }
	public SmartDate(String label, long time) {
		this.label = label;
		this.time = time;
	}

	/////////////
	// GETTERS //
	/////////////
	
	public String getLabel() {
		return label;
	}
	public long getTime() {
		return time;
	}

	/////////////
	// SETTERS //
	/////////////
	
	public void setLabel(String label) {
		this.label = label;
	}
	public void setTime(long time) {
		this.time = time;
	}	
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() { return label; }
	
	public int compareTo(SmartDate d) {
		if (time < d.time)
			return 1;
		if (time > d.time)
			return -1;
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o instanceof SmartDate) {
			SmartDate d = (SmartDate)o;
			return time == d.time;
		}
		return false;
	}
	
	public int hashCode() { return (int)time; }
		
}
