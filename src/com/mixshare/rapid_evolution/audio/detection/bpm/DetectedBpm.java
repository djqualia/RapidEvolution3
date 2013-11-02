package com.mixshare.rapid_evolution.audio.detection.bpm;

import com.mixshare.rapid_evolution.music.bpm.Bpm;

public class DetectedBpm {
	
    private double bpm;
    private double accuracy;
    private int intensity;
    
    public DetectedBpm(double bpm, double accuracy, int intensity) {
        this.bpm = bpm;
        this.accuracy = accuracy;
        this.intensity = intensity;
    }
        
    public Bpm getBpm() { return new Bpm((float)bpm); }
    public double getAccuracy() { return accuracy; }
    public int getBeatIntensity() { return intensity; }
    
    public boolean isValid() {
    	return (bpm != 0.0f) || (accuracy > 0.0);
    }
    
    public String toString() {
    	StringBuffer result = new StringBuffer();
    	result.append("[bpm: ");
    	result.append(bpm);
    	result.append(", accuracy: ");
    	result.append(accuracy);
    	result.append(", intensity: ");
    	result.append(intensity);
    	result.append("]");
    	return result.toString();
    }    
}
