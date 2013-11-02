package com.mixshare.rapid_evolution.audio.detection.key;

import com.mixshare.rapid_evolution.music.accuracy.Accuracy;
import com.mixshare.rapid_evolution.music.key.Key;

public class DetectedKey {
	
    private Key startKey = null;
    private Key endKey = null;
    private double accuracy;
    
    public DetectedKey() { }
    public DetectedKey(Key startKey, Key endKey, double accuracy) {
    	this.startKey = startKey;
        this.endKey = endKey;
        this.accuracy = accuracy;
    }
    
    public Key getStartKey() { return startKey; }
    public Key getEndKey() {
    	if (startKey.equals(endKey))
    		return Key.NO_KEY; return endKey;
    }
    public double getAccuracyValue() {
    	return accuracy;
    }
    public Accuracy getAccuracy() {
    	return Accuracy.getAccuracy((byte)(accuracy * 100));
    }
    
    public void setStartKey(Key startKey) { this.startKey = startKey; }
    public void setEndKey(Key endKey) { this.endKey = endKey; }    
    public void setAccuracy(double value) { accuracy = value; }
    
    public boolean isValid() {
    	return ((startKey != null) && (startKey.isValid() || accuracy > 0.0));
    }
    
    public String toString() {
    	StringBuffer result = new StringBuffer();
    	result.append("[key: ");
    	result.append(startKey);
    	result.append("->");
    	result.append(endKey);
    	result.append(", accuracy: ");
    	result.append(accuracy);
    	result.append("]");
    	return result.toString();
    }
}
