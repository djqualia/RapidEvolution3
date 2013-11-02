package com.mixshare.rapid_evolution.data.mined.billboard;

import java.io.Serializable;

public class ChartEntry implements Serializable {

	static private final long serialVersionUID = 0L;

    private String chartName;
    private String chartIssueDate;
    private String specType;
    private int specId;
    private String artist;
    private String song;
    private String label;
    private int peak;
    private int weeksOn;
	    
    public ChartEntry() { }
    public ChartEntry(String chartName, String chartIssueDate, String specType, int specId, String artist, String song, String label, int peak, int weeksOn) {
    	this.chartName = chartName;
    	this.chartIssueDate = chartIssueDate;
    	this.specType = specType;
    	this.specId = specId;
    	this.artist = artist;
    	this.song = song;
    	this.label = label;
    	this.peak = peak;
    	this.weeksOn = weeksOn;
    }

    public String toString() {
    	return chartName + " (" + song + ", " + chartIssueDate + ")";
    }
    
	public String getChartName() {
		return chartName;
	}

	public String getChartIssueDate() {
		return chartIssueDate;
	}
	
	public int getChartIssueYear() {
		if ((chartIssueDate != null) && (chartIssueDate.length() >= 4))
			return Integer.parseInt(chartIssueDate.substring(0, 4));
		return 0;
	}

	public String getSpecType() {
		return specType;
	}

	public String getArtist() {
		return artist;
	}

	public String getSong() {
		return song;
	}

	public int getPeak() {
		return peak;
	}

	public int getWeeksOn() {
		return weeksOn;
	}

	public int getSpecId() {
		return specId;
	}

	public void setSpecId(int specId) {
		this.specId = specId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setChartName(String chartName) {
		this.chartName = chartName;
	}

	public void setChartIssueDate(String chartIssueDate) {
		this.chartIssueDate = chartIssueDate;
	}

	public void setSpecType(String specType) {
		this.specType = specType;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public void setSong(String song) {
		this.song = song;
	}

	public void setPeak(int peak) {
		this.peak = peak;
	}

	public void setWeeksOn(int weeksOn) {
		this.weeksOn = weeksOn;
	}
	
}
