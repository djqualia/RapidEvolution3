package com.mixshare.rapid_evolution.music.rating;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Utilizes the fly-weight pattern due to finite range...  this class must remain immutable!
 */
public class Rating implements Comparable<Rating>, Serializable {
	
	static private Logger log = Logger.getLogger(Rating.class);
    static private final long serialVersionUID = 0L;    

    static private Map<String, Rating> ratingValueFlyWeights = new HashMap<String, Rating>();
	
	static public Rating NO_RATING = Rating.getRating(0);
	
    ////////////
    // FIELDS //
    ////////////
    
	private byte ratingValue;  // 0 to 100, can be translated to 1-5 stars
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	// for serialization
	public Rating() { }
	
	private Rating(byte ratingValue) { this.ratingValue = ratingValue; }	
	private Rating(int ratingValue) { this.ratingValue = (byte)ratingValue; }

	/////////////
	// GETTERS //
	/////////////
	
    public byte getRatingStars() {
    	if (ratingValue > 80)
    		return 5;
    	if (ratingValue > 60)
    		return 4;
    	if (ratingValue > 40)
    		return 3;
    	if (ratingValue > 20)
    		return 2;
    	if (ratingValue > 0)
    		return 1;
    	return 0;
    	//return (byte)Math.round((float)ratingValue / 20.0f);
    }	
    public float getRatingStarsFloat() { return ((float)ratingValue) / 20.0f; }	
    public float getRatingNormalized() { return ((float)ratingValue) / 100.0f; }
	public byte getRatingValue() { return ratingValue; }	
	
	public boolean isValid() { return ratingValue != 0; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setRatingValue(byte ratingValue) { this.ratingValue = ratingValue; }	
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		if (ratingValue != 0)
			return String.valueOf(ratingValue);
		return "";
	}
	
	public int compareTo(Rating b) {
		if (ratingValue < b.ratingValue)
			return 1;
		if (ratingValue > b.ratingValue)
			return -1;			
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Rating) {
			Rating b = (Rating)o;
			return (ratingValue == b.ratingValue);
		}
		return false;
	}
	
	public int hashCode() { return ratingValue; }	
	
    ////////////////////////
    // FLY WEIGHT PATTERN //
    ////////////////////////
    
    static public Rating getRating(byte ratingValue) {
    	return checkFactory(ratingValue);	        
    }
    static public Rating getRating(int ratingValue) {
    	return checkFactory((byte)ratingValue);	        
    }
        
    static private String calculatePrimaryKey(byte ratingValue) {
        return String.valueOf(ratingValue);
    }    
    
    static private Rating checkFactory(byte ratingValue) {
        String primaryKey = calculatePrimaryKey(ratingValue);
        Rating result = (Rating)ratingValueFlyWeights.get(primaryKey);
        if (result == null) {
            if (log.isTraceEnabled())
            	log.trace("checkFactory(): no existing beat intensity found");
            result = new Rating(ratingValue);
            ratingValueFlyWeights.put(primaryKey, result);
        }
        return result;
    }	

}
