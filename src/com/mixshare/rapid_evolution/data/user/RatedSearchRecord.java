package com.mixshare.rapid_evolution.data.user;

import java.io.Serializable;

import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.music.rating.Rating;

public class RatedSearchRecord implements Serializable {

	static private final long serialVersionUID = 0L;
	
	private SearchRecord searchRecord;
	private Rating rating;
	private float weight = 1.0f;
			
	public RatedSearchRecord() { }
	public RatedSearchRecord(SearchRecord searchRecord, Rating rating) {
		this.searchRecord = searchRecord;
		this.rating = rating;
	}
	public RatedSearchRecord(SearchRecord searchRecord, Rating rating, float weight) {
		this.searchRecord = searchRecord;
		this.rating = rating;
		this.weight = weight;
	}
	
	public SearchRecord getSearchRecord() { return searchRecord; }
	public Rating getRating() { return rating; }
	public float getWeight() { return weight; }

	public void setSearchRecord(SearchRecord searchRecord) { this.searchRecord = searchRecord; }
	public void setRating(Rating rating) { this.rating = rating; }	
	public void setWeight(float weight) { this.weight = weight; }
	
}
