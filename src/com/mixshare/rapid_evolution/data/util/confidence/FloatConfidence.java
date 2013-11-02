package com.mixshare.rapid_evolution.data.util.confidence;

import java.io.Serializable;

public class FloatConfidence implements Serializable {

    static private final long serialVersionUID = 0L;

    private float value;
    private float confidence;
    
    public FloatConfidence() { }
    public FloatConfidence(float value, float confidence) {
    	this.value = value;
    	this.confidence = confidence;
    }
    
    public float getValue() { return value; }
    public float getConfidence() { return confidence; }
    
	public void setValue(float value) {
		this.value = value;
	}
	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}
    
}
