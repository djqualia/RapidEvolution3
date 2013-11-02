package com.mixshare.rapid_evolution.data.search;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.Record;

public class SearchResult implements Serializable, Comparable<SearchResult> {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(SearchResult.class);
	
	private Record record;
	private float score; // between 0.0 and 1.0
	
	transient private Float similarity; // speeds up similarity detection
	
	public SearchResult(Record record, float score) {
		this.record = record;
		this.score = score;
	}

	public SearchResult(Record record, float score, float similarity) {
		this.record = record;
		this.score = score;
		this.similarity = similarity;
	}
	
	public Record getRecord() { return record; }
	public float getScore() { return score; }
	public Float getSimilarity() { return similarity; }

	public void setRecord(Record record) { this.record = record; }
	public void setScore(float score) { this.score = score; }
	public void setSimilarity(Float similarity) {
		this.similarity = similarity;
	}

	public int compareTo(SearchResult r) {
		if (score > r.getScore())
			return -1;
		else if (score < r.getScore())
			return 1;
		return 0;
	}
	
	public String toString() {
		return record + " (" + (int)(score*100.0f) + "%)";
	}
	
	public boolean equals(Object o) {
		if (o instanceof SearchResult)
			return ((SearchResult)o).getRecord().equals(getRecord());		
		return false;
	}
	
	public int hashCode() { return record.hashCode(); }
	
}
