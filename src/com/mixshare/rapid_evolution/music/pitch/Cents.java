package com.mixshare.rapid_evolution.music.pitch;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Cents is a measurement of micro-tonal pitch difference.  +100 cents is equivalent to going up 1 semitone.
 * 
 * Utilizes the fly-weight pattern due to finite range...  this class must remain immutable!
 */
public class Cents implements Comparable<Cents> {

    static private Logger log = Logger.getLogger(Cents.class);    

    static public Cents NO_CENTS = new Cents(Integer.MAX_VALUE);
    
    ////////////
    // FIELDS //
    ////////////
    
    private String display;
    private int val;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    private Cents(int val) {
    	this.val = val;
    	if (val == Integer.MAX_VALUE) {
    		display = "";
    	} else {
	        if (val > 0)
	        	display = "+" + val + " cents";
	        else
	        	display = String.valueOf(val) + " cents";
    	}
    }
    
    /////////////
    // METHODS //
    /////////////

    public int compareTo(Cents b) {
        int data1 = val;
        int data2 = b.val;
        if (data1 < data2)
            return -1;
        if (data1 > data2)
            return 1;
        return 0;
    }

    public boolean equals(Object b) {
        if (b instanceof Cents) {
        	Cents mb = (Cents) b;
            if (val == mb.val)
                return true;
        }
        return false;
    }

    public int hashCode() {
        return val;
    }

    public String toString() {
        return display;
    }
    
    ////////////////////////
    // FLY WEIGHT PATTERN //
    ////////////////////////
    
    static public Cents getCents(Float fval) {
    	if (fval == null)
    		return NO_CENTS;
    	return checkFactory((int)(fval * 100.0f));	        
    }
    static public Cents getCents(Integer ival) {
    	if (ival == null)
    		return NO_CENTS;
    	return checkFactory(ival);	        
    }
    
    static private Map<String, Cents> centsFlyWeights = new HashMap<String, Cents>();
    
    static private String calculatePrimaryKey(int centsValue) {
        return String.valueOf(centsValue);
    }    
    
    static private Cents checkFactory(int centsValue) {
        String primaryKey = calculatePrimaryKey(centsValue);
        Cents result = (Cents)centsFlyWeights.get(primaryKey);
        if (result == null) {
            if (log.isTraceEnabled())
            	log.trace("checkFactory(): no existing beat intensity found");
            result = new Cents(centsValue);
            centsFlyWeights.put(primaryKey, result);
        }
        return result;
    }    

};
