package com.mixshare.rapid_evolution.music.beatintensity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;

/**
 * This class represents a beat intensity value (0 to 100), which is automatically detected.
 * 
 * Utilizes the fly-weight pattern due to finite range...  this class must remain immutable!
 */
public class BeatIntensity implements Comparable<BeatIntensity>, Serializable {
	
    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(BeatIntensity.class);
	
    static public int LOGICAL_BEAT_INTENSITY_RANGE = RE3Properties.getInt("logical_beat_intensity_range");

    static private Map<String, BeatIntensity> beatIntensityFlyWeights = new HashMap<String, BeatIntensity>();
    
    static public BeatIntensity NO_BEAT_INTENSITY = getBeatIntensity(0); 
    
    static public int MAX_VALUE = 100;
    static public int MIN_VALUE = 0;
    
    ////////////
    // FIELDS //
    ////////////
    
	private byte beatIntensityValue; // 0 to 100
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	// for serialization
	public BeatIntensity() { }
	
	private BeatIntensity(byte beatIntensityValue) { this.beatIntensityValue = beatIntensityValue; }
	private BeatIntensity(int beatIntensityValue) { this.beatIntensityValue = (byte)beatIntensityValue; }

	/////////////
	// GETTERS //
	/////////////
	
	public byte getBeatIntensityValue() { return beatIntensityValue; }
		
	public boolean isValid() { return (beatIntensityValue != 0); }

	/////////////
	// SETTERS //
	/////////////
	
    // for serialization
	public void setBeatIntensityValue(byte beatIntensityValue) { this.beatIntensityValue = beatIntensityValue; }
	
	/////////////
	// METHODS //
	/////////////
	
	public float getSimilarityWith(BeatIntensity otherBI) {
		if ((otherBI.beatIntensityValue == 0) || (beatIntensityValue == 0))
			return 0.0f;
		int diff = Math.abs(otherBI.beatIntensityValue - beatIntensityValue);
		float similarity = (LOGICAL_BEAT_INTENSITY_RANGE - diff) / LOGICAL_BEAT_INTENSITY_RANGE;
		if (similarity > 1.0f)
			similarity = 1.0f;
		if (similarity < 0.0f)
			similarity = 0.0f;
		return similarity;
	}
	
	public String toString() {
		if (beatIntensityValue != 0)
			return String.valueOf(beatIntensityValue);
		return "";
	}
	
	public int compareTo(BeatIntensity b) {
		if (beatIntensityValue < b.beatIntensityValue)
			return 1;
		if (beatIntensityValue > b.beatIntensityValue)
			return -1;			
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o instanceof BeatIntensity) {
			BeatIntensity b = (BeatIntensity)o;
			return (beatIntensityValue == b.beatIntensityValue);
		}
		return false;
	}
	
	public int hashCode() { return beatIntensityValue; }
		
    ////////////////////////
    // FLY WEIGHT PATTERN //
    ////////////////////////
    
    static public BeatIntensity getBeatIntensity(byte beatIntensityValue) {
    	return checkFactory(beatIntensityValue);	        
    }
    static public BeatIntensity getBeatIntensity(int beatIntensityValue) {
    	return checkFactory((byte)beatIntensityValue);	        
    }
        
    static private String calculatePrimaryKey(byte beatIntensityValue) {
        return String.valueOf(beatIntensityValue);
    }    
    
    static private BeatIntensity checkFactory(byte beatIntensityValue) {
        String primaryKey = calculatePrimaryKey(beatIntensityValue);
        BeatIntensity result = (BeatIntensity)beatIntensityFlyWeights.get(primaryKey);
        if (result == null) {
            if (log.isTraceEnabled())
            	log.trace("checkFactory(): no existing beat intensity found");
            result = new BeatIntensity(beatIntensityValue);
            beatIntensityFlyWeights.put(primaryKey, result);
        }
        return result;
    }
    
}
