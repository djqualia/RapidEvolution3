package com.mixshare.rapid_evolution.music.key;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;

/**
 * A key code is a notation based on the circle of fifths, such that harmonically compatible keys are adjacent when
 * sorted numerically.  The letter for the key code represents the scale type.
 */
public class KeyCode implements Comparable<KeyCode>, Serializable {

    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(KeyCode.class);
    
    static public KeyCode NO_KEYCODE = new KeyCode();
    
    ////////////
    // FIELDS //
    ////////////
    
    private byte keyValue = Byte.MIN_VALUE;
    private char scaleType = Character.MIN_VALUE;
    private byte shift = Byte.MIN_VALUE;
    private String cachedToString;
    private String cachedToStringWithDetails;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    // for serialization only
    public KeyCode() { }
    
    private KeyCode(byte keyValue, char scaleType, byte shift) {
        if (log.isTraceEnabled()) log.trace("KeyCode(): keyValue=" + keyValue + ", scaleType=" + scaleType + ", shift=" + shift);
        this.keyValue = keyValue;
        this.scaleType = scaleType;
        this.shift = shift;
    }

    /////////////
    // GETTERS //
    /////////////
    
    public byte getKeyValue() { return keyValue; }
    public byte getShift() { return shift; }
    public char getScaleType() { return scaleType; }
    
    public boolean isValid() {
        return ((keyValue != Byte.MIN_VALUE) && (scaleType != Character.MIN_VALUE) && (shift != Byte.MIN_VALUE));        
    }
    
    public boolean isMinor() {
        return ((scaleType == 'A') || (scaleType == 'D') || (scaleType == 'P') || (scaleType == 'C'));
    }
        
    /////////////
    // METHODS //
    /////////////
    
    public String toString() { return toString(RE3Properties.getBoolean("show_advanced_key_information")); }    
    public String toString(boolean showDetails) {
        if (showDetails) {
            if (cachedToStringWithDetails == null) {
                cachedToStringWithDetails = calculateKeyCodeString(showDetails);
            }
            return cachedToStringWithDetails;
        } else {
            if (cachedToString == null) {
                cachedToString = calculateKeyCodeString(showDetails);
            }
            return cachedToString;
        }        
    }
        
    public String toFileFriendlyString() {
        String shortKeyCode = toString(false);
        if ((shortKeyCode == null) || shortKeyCode.equals(""))
            return "";
        if (shortKeyCode.length() <= 2)
            return "0" + shortKeyCode;
        return shortKeyCode;
    }
    
    public void invalidate() {
        cachedToString = null;
        cachedToStringWithDetails = null;
    }
    
    public int compareTo(KeyCode oKeyCode) {
    	if (isValid() && oKeyCode.isValid()) {
	        if (keyValue < oKeyCode.keyValue) return -1;
	        if (keyValue > oKeyCode.keyValue) return 1;
	        if (isMinor() && !oKeyCode.isMinor()) return -1;
	        if (!isMinor() && oKeyCode.isMinor()) return 1;
	        if (scaleType < oKeyCode.scaleType) return -1;
	        if (scaleType > oKeyCode.scaleType) return 1;
	        if (shift < oKeyCode.shift) return -1;
	        if (shift > oKeyCode.shift) return 1;
	        return 0;
    	}
    	if (isValid())
    		return -1;
    	if (oKeyCode.isValid())
    		return 1;
    	return 0;
    }        
            
    private String calculateKeyCodeString(boolean showDetails) {
        if (!isValid()) return "";
        else {
	        StringBuffer result = new StringBuffer();
	        if ((keyValue < 10) && RE3Properties.getBoolean("prefix_keycode_values"))
	        	result.append("0");
	        result.append(keyValue);
	        if (showDetails) {
                char displayScaleType = scaleType;
                if (displayScaleType == 'I')
                    displayScaleType = 'B';
	            result.append(displayScaleType);
	        } else {
	            if ((scaleType == 'I') || (scaleType == 'L') || (scaleType == 'M'))
	                result.append('B');
	            else
	                result.append('A');
	        }
	        if (shift > 0) {
	            result.append(" +");
	            result.append(shift);
	        } else if (shift < 0) {
	            result.append(" ");
	            result.append(shift);            
	        }                
	        return result.toString();
        }        
    }
    
    ////////////////////////
    // FLY WEIGHT PATTERN //
    ////////////////////////
        
    static public KeyCode getKeyCode(byte keyValue, char scaleType, byte shift) {
        String primaryKey = calculatePrimaryKey(keyValue, scaleType, shift);
        KeyCode result = (KeyCode)keyCodeFlyWeights.get(primaryKey);
        if (result == null) {
            result = new KeyCode(keyValue, scaleType, shift);
            keyCodeFlyWeights.put(primaryKey, result);
        }
        return result;
    }
    
    static public void invalidateCache() {
        Iterator<KeyCode> iter = keyCodeFlyWeights.values().iterator();
        while (iter.hasNext()) {
            KeyCode keyCode = (KeyCode)iter.next();
            keyCode.invalidate();
        }
    }
    
    static private Map<String, KeyCode> keyCodeFlyWeights = new HashMap<String, KeyCode>();
    
    static private String calculatePrimaryKey(byte keyValue, char scaleType, byte shift) {
        StringBuffer result = new StringBuffer();
        result.append(keyValue);
        result.append(",");
        result.append(scaleType);
        result.append(",");
        result.append(shift);
        return result.toString();
    }

    ///////////////////////
    // FOR SERIALIZATION //
    ///////////////////////
    
	public String getCachedToString() {
		return cachedToString;
	}

	public void setCachedToString(String cachedToString) {
		this.cachedToString = cachedToString;
	}

	public String getCachedToStringWithDetails() {
		return cachedToStringWithDetails;
	}

	public void setCachedToStringWithDetails(String cachedToStringWithDetails) {
		this.cachedToStringWithDetails = cachedToStringWithDetails;
	}

	public void setKeyValue(byte keyValue) {
		this.keyValue = keyValue;
	}

	public void setScaleType(char scaleType) {
		this.scaleType = scaleType;
	}

	public void setShift(byte shift) {
		this.shift = shift;
	}
}
