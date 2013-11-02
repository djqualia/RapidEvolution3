package com.mixshare.rapid_evolution.ui.model.profile.trail;

import java.io.Serializable;

import com.mixshare.rapid_evolution.data.record.Record;

public class TrailInstance implements Serializable {
	
    static private final long serialVersionUID = 0L;    	

	private Record record;
	private int position;
	
	public TrailInstance(Record record, int position) {
		this.record = record;
		this.position = position;
	}

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public boolean equals(Object o) {
		if (o instanceof TrailInstance) {
			TrailInstance ti = (TrailInstance)o;
			return (record.equals(ti.getRecord()) && (position == ti.getPosition()));
		}
		return false;
	}
	
	public int hashCode() { return record.hashCode() + position; }
	
}
