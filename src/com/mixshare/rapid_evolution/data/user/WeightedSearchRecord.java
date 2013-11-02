package com.mixshare.rapid_evolution.data.user;

import java.io.Serializable;

import com.mixshare.rapid_evolution.data.record.search.SearchRecord;

public class WeightedSearchRecord implements Serializable {

	static private final long serialVersionUID = 0L;
	
	private SearchRecord searchRecord;
	private float weight;
		
	public WeightedSearchRecord() { }
	public WeightedSearchRecord(SearchRecord searchRecord, float weight) {
		this.searchRecord = searchRecord;
		this.weight = weight;
	}
	
	public SearchRecord getSearchRecord() { return searchRecord; }
	public float getWeight() { return weight; }

	public void setSearchRecord(SearchRecord searchRecord) { this.searchRecord = searchRecord; }
	public void setWeight(float weight) { this.weight = weight; }	
	
}
