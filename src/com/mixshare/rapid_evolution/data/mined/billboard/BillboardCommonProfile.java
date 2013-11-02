package com.mixshare.rapid_evolution.data.mined.billboard;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;

abstract public class BillboardCommonProfile extends MinedProfile {

    ////////////
    // FIELDS //
    ////////////
    
    protected boolean isValid = false;
    protected Vector<ChartEntry> chartEntries = new Vector<ChartEntry>();

    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public BillboardCommonProfile() { super(); }
	public BillboardCommonProfile(byte dataType) {
		super(new MinedProfileHeader(dataType, DATA_SOURCE_BILLBOARD));
	}

    /////////////
    // GETTERS //
    /////////////
    
    public boolean isValid() { return isValid; }
    
    public int getTotalWeeksOn() {
    	int total = 0;
    	for (ChartEntry entry : chartEntries)
    		total += entry.getWeeksOn();
    	return total;
    }
    
    public Vector<ChartEntry> getChartEntries() { return chartEntries; }
    
    public Vector<Integer> getYearsOnCharts() {
    	Vector<Integer> years = new Vector<Integer>(chartEntries.size());
    	for (ChartEntry entry : chartEntries) {
    		int year = entry.getChartIssueYear();
    		if ((year != 0) && (!years.contains(year)))
    			years.add(year);
    	}
    	return years;
    }
    
    /////////////
    // SETTERS //
    /////////////
    
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	public void setChartEntries(Vector<ChartEntry> chartEntries) {
		this.chartEntries = chartEntries;
	}
    
}
