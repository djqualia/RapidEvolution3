package com.mixshare.rapid_evolution.music.beatintensity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.util.Translations;

/**
 * This class represents a beat intensity value (0 to 100), which is automatically detected.
 * 
 * Utilizes the fly-weight pattern due to finite range...  this class must remain immutable!
 */
public class BeatIntensityDescription implements Comparable<BeatIntensityDescription>, Serializable {
	
    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(BeatIntensityDescription.class);
        
    ////////////
    // FIELDS //
    ////////////
    
	private byte beatIntensityValue; // 0 to 100
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	// for serialization
	public BeatIntensityDescription() { }
	
	private BeatIntensityDescription(byte beatIntensityValue) { this.beatIntensityValue = beatIntensityValue; }
	private BeatIntensityDescription(int beatIntensityValue) { this.beatIntensityValue = (byte)beatIntensityValue; }

	/////////////
	// GETTERS //
	/////////////
	
	public byte getBeatIntensityValue() { return beatIntensityValue; }
		
	/////////////
	// SETTERS //
	/////////////

	// for serialization
	public void setBeatIntensityValue(byte beatIntensityValue) { this.beatIntensityValue = beatIntensityValue; }
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		int max = 100;
		int increment = BeatIntensity.LOGICAL_BEAT_INTENSITY_RANGE / 5;
		if (beatIntensityValue != 0) {
			max -= increment;
			if (beatIntensityValue > max)
				return Translations.get("beat_intensity_very_high");
			max -= increment;
			if (beatIntensityValue > max)
				return Translations.get("beat_intensity_high");
			max -= increment;
			if (beatIntensityValue > max)
				return Translations.get("beat_intensity_medium");
			max -= increment;
			if (beatIntensityValue > max)
				return Translations.get("beat_intensity_low");
			return Translations.get("beat_intensity_very_low");			
		}
		return "";
	}
	
	public int compareTo(BeatIntensityDescription b) {
		if (beatIntensityValue < b.beatIntensityValue)
			return 1;
		if (beatIntensityValue > b.beatIntensityValue)
			return -1;			
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o instanceof BeatIntensityDescription) {
			BeatIntensityDescription b = (BeatIntensityDescription)o;
			return (beatIntensityValue == b.beatIntensityValue);
		}
		return false;
	}
	
	public int hashCode() { return beatIntensityValue; }
		
    ////////////////////////
    // FLY WEIGHT PATTERN //
    ////////////////////////
    
    static public BeatIntensityDescription getBeatIntensityDescription(byte beatIntensityValue) {
    	return checkFactory(beatIntensityValue);	        
    }
    static public BeatIntensityDescription getBeatIntensityDescription(int beatIntensityValue) {
    	return checkFactory((byte)beatIntensityValue);	        
    }
    
    static private Map<String, BeatIntensityDescription> beatIntensityFlyWeights = new HashMap<String, BeatIntensityDescription>();
    
    static private String calculatePrimaryKey(byte beatIntensityValue) {
        return String.valueOf(beatIntensityValue);
    }    
    
    static private BeatIntensityDescription checkFactory(byte beatIntensityValue) {
        String primaryKey = calculatePrimaryKey(beatIntensityValue);
        BeatIntensityDescription result = (BeatIntensityDescription)beatIntensityFlyWeights.get(primaryKey);
        if (result == null) {
            if (log.isTraceEnabled())
            	log.trace("checkFactory(): no existing beat intensity found");
            result = new BeatIntensityDescription(beatIntensityValue);
            beatIntensityFlyWeights.put(primaryKey, result);
        }
        return result;
    }
    
}
