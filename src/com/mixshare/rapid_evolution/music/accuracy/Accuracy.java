package com.mixshare.rapid_evolution.music.accuracy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Utilizes the fly-weight pattern due to finite range...  this class must remain immutable!
 */
public class Accuracy implements Serializable, Comparable<Accuracy> {

    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(Accuracy.class);
	
	////////////
	// FIELDS //
	////////////

    private byte accuracy; // 0 to 100 (representing a %percentage%)
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////

    // for serialization
    public Accuracy() { }
    
    /**
     * @param accuracy value between 0 and 100
     */
    private Accuracy(byte accuracy) {
    	this.accuracy = accuracy;
    }    
    
    /////////////
    // GETTERS //
    /////////////
    
    public byte getAccuracy() { return accuracy; }
    
    public float getAccuracyNormalized() { return ((float)accuracy) / 100.0f; }

    /////////////
    // SETTERS //
    /////////////
    
    // for serialization
	public void setAccuracy(byte accuracy) { this.accuracy = accuracy; }
    
    /////////////
    // METHODS //
    /////////////
    
	public String toString() {
		//if (accuracy != 0)
			return String.valueOf(accuracy) + "%";
		//return "";
	}
	
	public int compareTo(Accuracy a) {		
		if (accuracy < a.accuracy)
			return 1;
		if (accuracy > a.accuracy)
			return -1;			
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Accuracy) {
			Accuracy a = (Accuracy)o;
			return (accuracy == a.accuracy);
		}
		return false;
	}
	
	public int hashCode() { return accuracy; }	
 
    ////////////////////////
    // FLY WEIGHT PATTERN //
    ////////////////////////
    
    static public Accuracy getAccuracy(byte accuracyValue) {
    	return checkFactory(accuracyValue);	        
    }
    static public Accuracy getAccuracy(int accuracyValue) {
    	return checkFactory((byte)accuracyValue);	        
    }
    
    static private Map<String, Accuracy> accuracyFlyWeights = new HashMap<String, Accuracy>();
    
    static private String calculatePrimaryKey(byte accuracyValue) {
        return String.valueOf(accuracyValue);
    }    
    
    static private Accuracy checkFactory(byte accuracyValue) {
        String primaryKey = calculatePrimaryKey(accuracyValue);
        Accuracy result = (Accuracy)accuracyFlyWeights.get(primaryKey);
        if (result == null) {
            if (log.isTraceEnabled())
            	log.trace("checkFactory(): no existing accuracy found");
            result = new Accuracy(accuracyValue);
            accuracyFlyWeights.put(primaryKey, result);
        }
        return result;
    }
    
}
