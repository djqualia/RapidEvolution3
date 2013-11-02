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
public class BeatIntensityVarianceDescription implements Comparable<BeatIntensityVarianceDescription>, Serializable {
	
    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(BeatIntensityVarianceDescription.class);
        
    ////////////
    // FIELDS //
    ////////////
    
	private byte beatIntensityVarianceValue = -1;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	// for serialization
	public BeatIntensityVarianceDescription() { }
	
	private BeatIntensityVarianceDescription(byte beatIntensityVarianceValue) { this.beatIntensityVarianceValue = beatIntensityVarianceValue; }
	private BeatIntensityVarianceDescription(int beatIntensityVarianceValue) { this.beatIntensityVarianceValue = (byte)beatIntensityVarianceValue; }

	/////////////
	// GETTERS //
	/////////////
	
	public byte getBeatIntensityVarianceValue() { return beatIntensityVarianceValue; }
		
	/////////////
	// SETTERS //
	/////////////

	// for serialization
	public void setBeatIntensityVarianceValue(byte beatIntensityVarianceValue) { this.beatIntensityVarianceValue = beatIntensityVarianceValue; }
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		int max = BeatIntensityVariance.LOGICAL_BEAT_INTENSITY_VARIANCE_RANGE;
		int increment = BeatIntensityVariance.LOGICAL_BEAT_INTENSITY_VARIANCE_RANGE / 5;
		if (beatIntensityVarianceValue != -1) {
			max -= increment;
			if (beatIntensityVarianceValue > max)
				return Translations.get("beat_intensity_very_high");
			max -= increment;
			if (beatIntensityVarianceValue > max)
				return Translations.get("beat_intensity_high");
			max -= increment;
			if (beatIntensityVarianceValue > max)
				return Translations.get("beat_intensity_medium");
			max -= increment;
			if (beatIntensityVarianceValue > max)
				return Translations.get("beat_intensity_low");
			return Translations.get("beat_intensity_very_low");			
		}
		return "";
	}
	
	public int compareTo(BeatIntensityVarianceDescription b) {
		if (beatIntensityVarianceValue < b.beatIntensityVarianceValue)
			return 1;
		if (beatIntensityVarianceValue > b.beatIntensityVarianceValue)
			return -1;			
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o instanceof BeatIntensityVarianceDescription) {
			BeatIntensityVarianceDescription b = (BeatIntensityVarianceDescription)o;
			return (beatIntensityVarianceValue == b.beatIntensityVarianceValue);
		}
		return false;
	}
	
	public int hashCode() { return beatIntensityVarianceValue; }
		
    ////////////////////////
    // FLY WEIGHT PATTERN //
    ////////////////////////
    
    static public BeatIntensityVarianceDescription getBeatIntensityVarianceDescription(float beatIntensityVarianceValue) {
    	if (Float.isNaN(beatIntensityVarianceValue))
    		return checkFactory((byte)-1);
    	return checkFactory((byte)beatIntensityVarianceValue);	        
    }
    
    static private Map<String, BeatIntensityVarianceDescription> beatIntensityVarianceFlyWeights = new HashMap<String, BeatIntensityVarianceDescription>();
    
    static private String calculatePrimaryKey(byte beatIntensityVarianceValue) {
        return String.valueOf(beatIntensityVarianceValue);
    }    
    
    static private BeatIntensityVarianceDescription checkFactory(byte beatIntensityVarianceValue) {
        String primaryKey = calculatePrimaryKey(beatIntensityVarianceValue);
        BeatIntensityVarianceDescription result = (BeatIntensityVarianceDescription)beatIntensityVarianceFlyWeights.get(primaryKey);
        if (result == null) {
            if (log.isTraceEnabled())
            	log.trace("checkFactory(): no existing beat intensity found");
            result = new BeatIntensityVarianceDescription(beatIntensityVarianceValue);
            beatIntensityVarianceFlyWeights.put(primaryKey, result);
        }
        return result;
    }
    
}
