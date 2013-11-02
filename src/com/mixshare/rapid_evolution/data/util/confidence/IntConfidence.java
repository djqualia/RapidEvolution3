package com.mixshare.rapid_evolution.data.util.confidence;

import java.io.Serializable;

public class IntConfidence implements Serializable {

    static private final long serialVersionUID = 0L;

    private int value;
    private float confidence;
    
    public IntConfidence() { }
    public IntConfidence(int value, float confidence) {
    	this.value = value;
    	this.confidence = confidence;
    }
    
    public int getValue() { return value; }
    public float getConfidence() { return confidence; }
    
	public void setValue(int value) {
		this.value = value;
	}
	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}
    
}
