package com.mixshare.rapid_evolution.ui.model.column.comparables;

public class Percentage implements Comparable<Percentage> {

	static public Percentage ZERO_PERCENT = new Percentage(0.0f);
	
	protected float percent; // 0.0 to 1.0
	
	public Percentage(float value) {
		percent = value;
	}
	
	public float getPercentage() { return percent; }
	
	public String toString() {
		return String.valueOf((int)(percent * 100.0f)) + "%";
	}
	
	public int compareTo(Percentage p) {
		if (percent > p.percent)
			return -1;
		if (percent < p.percent)
			return 1;
		return 0;
	}
	
}
