package com.mixshare.rapid_evolution.music.beatintensity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class represents a beat intensity variance value (for associated avg. beat intensity of artists/releaeses/etc).
 * 
 * Utilizes the fly-weight pattern due to finite range...  this class must remain immutable!
 */
public class BeatIntensityVariance implements Comparable<BeatIntensityVariance>, Serializable {
	
    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(BeatIntensityVariance.class);
	
    static public int LOGICAL_BEAT_INTENSITY_VARIANCE_RANGE = BeatIntensity.LOGICAL_BEAT_INTENSITY_RANGE;
    
    ////////////
    // FIELDS //
    ////////////
    
	private byte beatIntensityVarianceValue = -1;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////

	// for serialization
	public BeatIntensityVariance() { }
	
	private BeatIntensityVariance(byte beatIntensityVarianceValue) { this.beatIntensityVarianceValue = beatIntensityVarianceValue; }
	private BeatIntensityVariance(int beatIntensityVarianceValue) { this.beatIntensityVarianceValue = (byte)beatIntensityVarianceValue; }

	/////////////
	// GETTERS //
	/////////////
	
	public byte getBeatIntensityVarianceValue() { return beatIntensityVarianceValue; }
		
	public boolean isValid() { return (beatIntensityVarianceValue != -1); }	
	
	/////////////
	// SETTERS //
	/////////////
	
	// for serialization
	public void setBeatIntensityVarianceValue(byte beatIntensityVarianceValue) { this.beatIntensityVarianceValue = beatIntensityVarianceValue; }
	
	/////////////
	// METHODS //
	/////////////
	
	public float getSimilarityWith(BeatIntensityVariance otherBI) {
		if ((otherBI.beatIntensityVarianceValue == -1) || (beatIntensityVarianceValue == -1))
			return 0.0f;
		int diff = Math.abs(otherBI.beatIntensityVarianceValue - beatIntensityVarianceValue);
		float similarity = (LOGICAL_BEAT_INTENSITY_VARIANCE_RANGE - diff) / LOGICAL_BEAT_INTENSITY_VARIANCE_RANGE;
		if (similarity > 1.0f)
			similarity = 1.0f;
		if (similarity < 0.0f)
			similarity = 0.0f;
		return similarity;
	}
	
	public String toString() {
		if (beatIntensityVarianceValue != -1)
			return String.valueOf(beatIntensityVarianceValue);
		return "";
	}
	
	public int compareTo(BeatIntensityVariance b) {
		if (beatIntensityVarianceValue < b.beatIntensityVarianceValue)
			return 1;
		if (beatIntensityVarianceValue > b.beatIntensityVarianceValue)
			return -1;			
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o instanceof BeatIntensityVariance) {
			BeatIntensityVariance b = (BeatIntensityVariance)o;
			return (beatIntensityVarianceValue == b.beatIntensityVarianceValue);
		}
		return false;
	}
	
	public int hashCode() { return beatIntensityVarianceValue; }
		
    ////////////////////////
    // FLY WEIGHT PATTERN //
    ////////////////////////
    
    static public BeatIntensityVariance getBeatIntensityVariance(float beatIntensityVariance) {
    	if (Float.isNaN(beatIntensityVariance))
    		return checkFactory((byte)-1);
    	return checkFactory((byte)beatIntensityVariance);	        
    }
    
    static private Map<String, BeatIntensityVariance> beatIntensityFlyWeights = new HashMap<String, BeatIntensityVariance>();
    
    static private String calculatePrimaryKey(byte beatIntensityValue) {
        return String.valueOf(beatIntensityValue);
    }    
    
    static private BeatIntensityVariance checkFactory(byte beatIntensityValue) {
        String primaryKey = calculatePrimaryKey(beatIntensityValue);
        BeatIntensityVariance result = (BeatIntensityVariance)beatIntensityFlyWeights.get(primaryKey);
        if (result == null) {
            if (log.isTraceEnabled())
            	log.trace("checkFactory(): no existing beat intensity found");
            result = new BeatIntensityVariance(beatIntensityValue);
            beatIntensityFlyWeights.put(primaryKey, result);
        }
        return result;
    }
    
}
