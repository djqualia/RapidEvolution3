package com.mixshare.rapid_evolution.data.mined.echonest.song;

import java.io.Serializable;

import com.echonest.api.v4.TimedEvent;

public class EchonestTimedEvent implements Serializable {

    static private final long serialVersionUID = 0L;

    private double confidence;
	private double duration;
    private double start;
    
    public EchonestTimedEvent() { }
    public EchonestTimedEvent(TimedEvent timedEvent) {
    	confidence = timedEvent.getConfidence();
    	duration = timedEvent.getDuration();
    	start = timedEvent.getStart();
    }
    
    public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	public double getDuration() {
		return duration;
	}
	public void setDuration(double duration) {
		this.duration = duration;
	}
	public double getStart() {
		return start;
	}
	public void setStart(double start) {
		this.start = start;
	}
	
}
