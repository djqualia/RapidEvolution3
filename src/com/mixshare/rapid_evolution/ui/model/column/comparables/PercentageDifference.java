package com.mixshare.rapid_evolution.ui.model.column.comparables;

public class PercentageDifference extends Percentage {

	public PercentageDifference(float value) {
		super(value);
	}
	
	public String toString() {
		if (percent > 0.0f)
			return "+" + String.valueOf((int)(percent * 100.0f)) + "%";
		return String.valueOf((int)(percent * 100.0f)) + "%";
	}
	
}
