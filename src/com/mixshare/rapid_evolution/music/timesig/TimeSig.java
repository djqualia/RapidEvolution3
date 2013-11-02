package com.mixshare.rapid_evolution.music.timesig;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Utilizes the fly-weight pattern due to finite range...  this class must remain immutable!
 */
public class TimeSig implements Comparable<TimeSig> {

    private static Logger log = Logger.getLogger(TimeSig.class);
    
    ////////////
    // FIELDS //
    ////////////
    
    private byte numerator;
    private byte denominator;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    private TimeSig(String value) {
        boolean success = false;
        try {
            if (value != null) {
                int seperator = value.indexOf("/");
                if (seperator > 0) {
                    numerator = Byte.parseByte(value.substring(0, seperator));
                    denominator = Byte.parseByte(value.substring(seperator + 1, value.length()));
                    if ((numerator != 0) && (denominator != 0))
                        success = true;
                } 
            }
        } catch (Exception e) {
            log.error("TimeSig(): error Exception", e);
        }
        if (!success) {
        	if (log.isDebugEnabled())
        		log.debug("TimeSig(): could not parse time sig=" + value);
        }
    }
    private TimeSig(byte numerator, byte denominator) {
        this.numerator = numerator;
        this.denominator = denominator;        
    }
    
    /////////////
    // GETTERS //
    /////////////

    public byte getNumerator() { return numerator; }
    public byte getDenominator() { return denominator; }

    public boolean isValid() { return (numerator != 0) && (denominator != 0); }
    
    /////////////
    // METHODS //
    /////////////
    
    public float getSimilarityWith(TimeSig timeSig) {
    	if (equals(timeSig))
    		return 1.0f;
    	if (isCompatibleWith(timeSig))
    		return 0.5f;
    	return 0.0f;
    }
    
    public String toString() {
        if ((numerator != 0) && (denominator != 0))
            return String.valueOf(numerator) + "/" + String.valueOf(denominator);
        return "";
    }
        
    public boolean isCompatibleWith(TimeSig sig) {
        // TODO: flesh out
    	float val1 = (float)numerator / denominator;
    	float val2 = (float)sig.numerator / sig.denominator;
    	while (val1 >= 2.0f)
    		val1 /= 2.0f;
    	while (val2 >= 2.0f)
    		val2 /= 2.0f;
    	return (val1 == val2);        
    }
    
    public int compareTo(TimeSig t2) {
    	return toString().compareToIgnoreCase(t2.toString());
    }
    
    public boolean equals(Object o) {
    	if (o instanceof TimeSig) {
    		TimeSig oT = (TimeSig)o;
    		return ((numerator == oT.numerator) && (denominator == oT.denominator));
    	}
    	return false;
    }
    
    public int hashCode() {
    	return ((int)numerator) << 8 + ((int)denominator);
    }
    
    ////////////////////////
    // FLY WEIGHT PATTERN //
    ////////////////////////
    
    static public TimeSig getTimeSig(byte numerator, byte denominator) {
    	return checkFactory(numerator, denominator);	        
    }
    static public TimeSig getTimeSig(int numerator, int denominator) {
    	return checkFactory((byte)numerator, (byte)denominator);
    }
    
    static public TimeSig getTimeSig(String value) {
        if (value != null) {
            int seperator = value.indexOf("/");
            if (seperator > 0) {
                byte numerator = Byte.parseByte(value.substring(0, seperator));
                byte denominator = Byte.parseByte(value.substring(seperator + 1, value.length()));
                if ((numerator != 0) && (denominator != 0))
                    return checkFactory(numerator, denominator);
            }
        }
        return null;
    }
    
    static private Map<String,TimeSig> timeSigFlyWeights = new HashMap<String,TimeSig>();
    
    static private String calculatePrimaryKey(byte numerator, byte denominator) {
        StringBuffer result = new StringBuffer();
        result.append(numerator);
        result.append(",");
        result.append(denominator);
        return result.toString();
    }
    
    
    static private TimeSig checkFactory(byte numerator, byte denominator) {
        String primaryKey = calculatePrimaryKey(numerator, denominator);
        TimeSig result = (TimeSig)timeSigFlyWeights.get(primaryKey);
        if (result == null) {
            if (log.isTraceEnabled())
            	log.trace("checkFactory(): no existing time sig found");
            result = new TimeSig(numerator, denominator);
            timeSigFlyWeights.put(primaryKey, result);
        }
        return result;
    }
    
}

