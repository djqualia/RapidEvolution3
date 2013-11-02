package com.mixshare.rapid_evolution.music.key;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.util.StringUtil;

/**
 * This class represents a musical key.
 * 
 * Since there are a finite number of keys, a fly-weight pattern is used to conserve memory.  Therefore, this
 * class must remain immutable.  Methods which change the value of the key will return a new copy of a Key.
 * Furthermore, factory methods must be used to retrieve new values (i.e. getKey(...)).
 */
public class Key implements Serializable, Comparable<Key> {

    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(Key.class);

    ///////////////
    // CONSTANTS //
    ///////////////
    
    // 100 cents = 1 semitone = 1 half step    
    static public byte semiTonesPerOctave = 12;
    // the percent BPM shift equivalent to a change in 1 semitone pitch (a half-step, or 100 cents)
    static public float bpmDifferencePerSemitone = (float)(Math.pow(Math.E, Math.log(2) / semiTonesPerOctave) * 100.0f - 100.0f);
    
    // supported scale types/modes
    static public byte SCALE_UNKNOWN = -1;
    static public byte SCALE_IONIAN = 0; // standard major
    static public byte SCALE_AEOLIAN = 1; // natural minor
    static public byte SCALE_LYDIAN = 2; // major with raised 4th
    static public byte SCALE_MIXOLYDIAN = 3; // major with lowered 7th
    static public byte SCALE_DORIAN = 4; // minor with raised 6th
    static public byte SCALE_PHRYGIAN = 5; // minor with lowered 2nd
    static public byte SCALE_LOCRIAN = 6;
    
    static public float ROOT_UNKNOWN = Float.NEGATIVE_INFINITY;
    
    static public String[] wellFormedKeys = {
        "A", "Am", "A#", "Bb", "A#m", "Bbm", "B", "Bm", "C", "Cm", "Db", "C#", "Dbm", "C#m", "D", "Dm", "Eb", "D#", "Ebm", "D#m", "E", "Em",
        "F", "Fm", "Gb", "F#", "Gbm", "F#m", "G", "Gm", "Ab", "G#", "Abm", "G#m"
    };

    static public String[] advancedModes = {
        "ionian", "aeolian", "lydian", "mixolydian", "dorian", "phrygian", "locrian"
    };

    static public String[] keycodeSuffixes = {
        "A", "B", "I", "L", "M", "D", "P", "C"
    };
    
    static boolean useFlatNotation = false;    
    
    static public Key NO_KEY = new Key(ROOT_UNKNOWN, SCALE_UNKNOWN);
    
    ////////////
    // FIELDS //
    ////////////
    
    private float rootValue = ROOT_UNKNOWN; // ~0-12 range, corresponds with each note on the keyboard
    private byte scaleType = -1; // i.e. modal variation
    private ShiftedKeyNotation shiftedNotation; // the root value is exact and might be in between keys, this variable is used to display the shifted notation (i.e. Am+30)
        
    //////////////////
    // CONSTRUCTORS //
    //////////////////    
    
    // for serialization only
    public Key() { }
    
    private Key(float rootValue, byte scaleType) {
        this.rootValue = rootValue;
        this.scaleType = scaleType;
        validateRootValue();
        shiftedNotation = new ShiftedKeyNotation(rootValue);
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    // root note
    public float getRootValue() { return rootValue; }
    public String getRootNoteDescription() {
        int root_note = shiftedNotation.getRootNote();
        if (root_note == 0) return "A";
        if (root_note == 1) return useFlatNotation ? "Bb" : "A#";
        if (root_note == 2) return "B";
        if (root_note == 3) return "C";
        if (root_note == 4) return useFlatNotation ? "Db" : "C#";
        if (root_note == 5) return "D";
        if (root_note == 6) return useFlatNotation ? "Eb" : "D#";
        if (root_note == 7) return "E";
        if (root_note == 8) return "F";
        if (root_note == 9) return useFlatNotation ? "Gb" : "F#";
        if (root_note == 10) return "G";
        if (root_note == 11) return useFlatNotation ? "Ab" : "G#";
        return "UNKNOWN";
    }
    
    // scale type
    public byte getScaleType() { return scaleType; }
    public String getScaleTypeDescription() {
        if (scaleType == 0) return "ionian";
        else if (scaleType == 1) return "aeolian";
        else if (scaleType == 2) return "lydian";
        else if (scaleType == 3) return "mixolydian";
        else if (scaleType == 4) return "dorian";
        else if (scaleType == 5) return "phrygian";
        else if (scaleType == 6) return "locrian";
        else return "UNKNOWN";
    }   
    public boolean isMinor() {
        return ((scaleType == SCALE_AEOLIAN) ||
                (scaleType == SCALE_DORIAN) ||
                (scaleType == SCALE_PHRYGIAN) ||
                (scaleType == SCALE_LOCRIAN));
    }            
    
    // shifted key
    public ShiftedKeyNotation getShiftedKeyNotation() { return shiftedNotation; }    
    public String getShiftDescription() {
        float shift = shiftedNotation.getShift();
        int shift_in_cents = (int)(shift * 100.0);
        if (shift_in_cents == 0) return "NONE";
        if (shift_in_cents > 0) return "+" + shift_in_cents;
        else return String.valueOf(shift_in_cents);
    }
         
    public String toString() { return getPreferredKeyNotation(); }
    public String toStringExact() { return getPreferredKeyNotation(true); }
    
    // flat notation
    public String getKeyNotationFlat() { return getKeyNotationFlat(false); }    
    public String getKeyNotationFlat(boolean showDetails) {
        if (rootValue == ROOT_UNKNOWN) return "";
        int root_note = shiftedNotation.getRootNote();
        StringBuffer result = new StringBuffer();        
        if (isMinor()) {
            if (root_note == 0) result.append("Am");
            if (root_note == 1) result.append("Bbm");
            if (root_note == 2) result.append("Bm");
            if (root_note == 3) result.append("Cm");
            if (root_note == 4) result.append("Dbm");
            if (root_note == 5) result.append("Dm");
            if (root_note == 6) result.append("Ebm");
            if (root_note == 7) result.append("Em");
            if (root_note == 8) result.append("Fm");
            if (root_note == 9) result.append("Gbm");
            if (root_note == 10) result.append("Gm");
            if (root_note == 11) result.append("Abm");            
        } else {
            if (root_note == 0) result.append("A");
            if (root_note == 1) result.append("Bb");
            if (root_note == 2) result.append("B");
            if (root_note == 3) result.append("C");
            if (root_note == 4) result.append("Db");
            if (root_note == 5) result.append("D");
            if (root_note == 6) result.append("Eb");
            if (root_note == 7) result.append("E");
            if (root_note == 8) result.append("F");
            if (root_note == 9) result.append("Gb");
            if (root_note == 10) result.append("G");
            if (root_note == 11) result.append("Ab");            
        }
        if (showDetails) {
            appendMode(result);
        }
        appendShift(result);
        return result.toString();
    }
    
    // sharp notation
    public String getKeyNotationSharp() { return getKeyNotationSharp(false); }    
    public String getKeyNotationSharp(boolean showDetails) {
        if (rootValue == ROOT_UNKNOWN) return "";
        int root_note = shiftedNotation.getRootNote();
        StringBuffer result = new StringBuffer();        
        if (isMinor()) {
            if (root_note == 0) result.append("Am");
            if (root_note == 1) result.append("A#m");
            if (root_note == 2) result.append("Bm");
            if (root_note == 3) result.append("Cm");
            if (root_note == 4) result.append("C#m");
            if (root_note == 5) result.append("Dm");
            if (root_note == 6) result.append("D#m");
            if (root_note == 7) result.append("Em");
            if (root_note == 8) result.append("Fm");
            if (root_note == 9) result.append("F#m");
            if (root_note == 10) result.append("Gm");
            if (root_note == 11) result.append("G#m");            
        } else {
            if (root_note == 0) result.append("A");
            if (root_note == 1) result.append("A#");
            if (root_note == 2) result.append("B");
            if (root_note == 3) result.append("C");
            if (root_note == 4) result.append("C#");
            if (root_note == 5) result.append("D");
            if (root_note == 6) result.append("D#");
            if (root_note == 7) result.append("E");
            if (root_note == 8) result.append("F");
            if (root_note == 9) result.append("F#");
            if (root_note == 10) result.append("G");
            if (root_note == 11) result.append("G#");            
        }
        if (showDetails) {
            appendMode(result);
        }
        appendShift(result);
        return result.toString();        
    }    
    
    // custom notation
    public String getCustomKeyNotation() { return getCustomKeyNotation(false); }
    public String getCustomKeyNotation(boolean showDetails) {
        if (rootValue == ROOT_UNKNOWN) return "";
        StringBuffer result = new StringBuffer();        
        result.append(getRootNoteDescription());
        if (isMinor()) result.append("m");
        if (showDetails) {
            appendMode(result);
        }
        appendShift(result);
        return result.toString(); 
    }    
        
    // key code
    public KeyCode getKeyCode() {
        if (rootValue == ROOT_UNKNOWN) {
            return KeyCode.NO_KEYCODE;
        } else {
	        int root_note = shiftedNotation.getRootNote();
	        byte keyCodeValue = -1;
	        boolean isMinor = isMinor();
	        char keyCodeScaleType = isMinor ? 'A' : 'B';
	        if (isMinor) {
	            if (root_note == 0)keyCodeValue = 8;
	            else if (root_note == 1) keyCodeValue = 3;
	            else if (root_note == 2) keyCodeValue = 10;
	            else if (root_note == 3) keyCodeValue = 5;
	            else if (root_note == 4) keyCodeValue = 12;
	            else if (root_note == 5) keyCodeValue = 7;
	            else if (root_note == 6) keyCodeValue = 2;
	            else if (root_note == 7) keyCodeValue = 9;
	            else if (root_note == 8) keyCodeValue = 4;
	            else if (root_note == 9) keyCodeValue = 11;
	            else if (root_note == 10) keyCodeValue = 6;
	            else if (root_note == 11) keyCodeValue = 1;
	        } else {
	            if (root_note == 0) keyCodeValue = 11;
	            else if (root_note == 1) keyCodeValue = 6;
	            else if (root_note == 2) keyCodeValue = 1;
	            else if (root_note == 3) keyCodeValue = 8;
	            else if (root_note == 4) keyCodeValue = 3;
	            else if (root_note == 5) keyCodeValue = 10;
	            else if (root_note == 6) keyCodeValue = 5;
	            else if (root_note == 7) keyCodeValue = 12;
	            else if (root_note == 8) keyCodeValue = 7;
	            else if (root_note == 9) keyCodeValue = 2;
	            else if (root_note == 10) keyCodeValue = 9;
	            else if (root_note == 11) keyCodeValue = 4;
	        }        
	        if (scaleType == SCALE_DORIAN) {
	        	keyCodeScaleType = 'D';
	            ++keyCodeValue;
	        } else if (scaleType == SCALE_PHRYGIAN) {
	        	keyCodeScaleType = 'P';
	            --keyCodeValue;
	        } else if (scaleType == SCALE_LYDIAN) {
	        	keyCodeScaleType = 'L';
	            ++keyCodeValue;
	        } else if (scaleType == SCALE_MIXOLYDIAN) {
	        	keyCodeScaleType = 'M';
	            --keyCodeValue;
	        } else if (scaleType == SCALE_LOCRIAN) {
	        	keyCodeScaleType = 'C';
	            keyCodeValue -= 2;
	        } else if (scaleType == SCALE_IONIAN) {
	        	keyCodeScaleType = 'I';            
	        }
	        if (keyCodeValue < 1) keyCodeValue += 12;
	        if (keyCodeValue > 12) keyCodeValue -= 12;
	        byte shift = (byte)(shiftedNotation.getShift() * 100.0);
	        return KeyCode.getKeyCode(keyCodeValue, keyCodeScaleType, shift);
        }
    }    
    
    public String getGroupSSLString() {
        if (rootValue == ROOT_UNKNOWN) return "";
        StringBuffer result = new StringBuffer();
        result.append(getKeyCode());
        result.append(" ");
        result.append(getKeyNotationSharp());
        return result.toString();
    }

    /**
     * The ID3 safe notation excludes the shift and does not use keycodes or modes.
     */
    public String getID3SafeKeyNotation() { return getKeyNotationSharp(false); }
    
    public String getShortKeyNotation() { return getPreferredKeyNotation(false); }
        
    static public String getPreferredKeyNotation(String key) { return Key.getKey(key).getPreferredKeyNotation(); }    
    public String getPreferredKeyNotation() { return getPreferredKeyNotation(RE3Properties.getBoolean("show_advanced_key_information")); }
    public String getPreferredKeyNotation(boolean showDetails) {
        if (RE3Properties.getProperty("preferred_key_format").equalsIgnoreCase("sharp"))
        	return getKeyNotationSharp(showDetails);
        if (RE3Properties.getProperty("preferred_key_format").equalsIgnoreCase("flat"))
        	return getKeyNotationFlat(showDetails);
        if (RE3Properties.getProperty("preferred_key_format").equalsIgnoreCase("keycode"))
        	return getKeyCode().toString(showDetails);
        return getKeyNotationSharp(showDetails);
    }   
        
    public boolean isValid() {
        return (rootValue != ROOT_UNKNOWN);
    }
    
    static public boolean isWellFormed(String input) {
        if ((input == null) || input.equals("")) return false;
        input = input.toLowerCase();
        // remove shift
        int shift_index = input.indexOf("+");
        if (shift_index >= 0)
            input = input.substring(0, shift_index).trim();
        shift_index = input.indexOf("-");
        if (shift_index >= 0)
            input = input.substring(0, shift_index).trim();
        // remove advanced modes
        for (int i = 0; i < advancedModes.length; ++i) {
            int mode_index = input.indexOf(advancedModes[i]);
            if (mode_index >= 0)
                input = input.substring(0, mode_index).trim();
        }
        // check for normal keys
        for (int i = 0; i < wellFormedKeys.length; ++i) {
            if (input.equalsIgnoreCase(wellFormedKeys[i]))
                return true;
        }
        // check for valid keycode
        if (input.length() > 3) return false;
        if (input.length() < 2) return false;
        for (int i = 0; i < keycodeSuffixes.length; ++i) {
            if (input.endsWith(keycodeSuffixes[i].toLowerCase()))
                input = input.substring(0, input.length() - 1);
        }
        try {
            int value = Integer.parseInt(input);
            if ((value >= 1) && (value <= 12))
                return true;
        } catch (Exception e) { }
        return false;
    }
    
    static public boolean isValid(String key) {
        if (key == null) return false;
        if (key.equals("")) return false;
        boolean val = false;
        if (key.charAt(0) == 'a') val = true;
        if (key.charAt(0) == 'A') val = true;
        if (key.charAt(0) == 'b') val = true;
        if (key.charAt(0) == 'B') val = true;
        if (key.charAt(0) == 'c') val = true;
        if (key.charAt(0) == 'C') val = true;
        if (key.charAt(0) == 'd') val = true;
        if (key.charAt(0) == 'D') val = true;
        if (key.charAt(0) == 'e') val = true;
        if (key.charAt(0) == 'E') val = true;
        if (key.charAt(0) == 'f') val = true;
        if (key.charAt(0) == 'F') val = true;
        if (key.charAt(0) == 'g') val = true;
        if (key.charAt(0) == 'G') val = true;
        if (!val) {
            // check for key code format
            try {
                boolean firstnumeric = Character.isDigit(key.charAt(0));
                boolean secondnumeric = Character.isDigit(key.charAt(1));
                if (firstnumeric && isValidKeyCodeLetter(key.charAt(1))) val = true;
                if (firstnumeric && secondnumeric && isValidKeyCodeLetter(key.charAt(2))) val = true;
            } catch (Exception e) { }
        }
        return val;
    }    
    
    static private boolean isValidKeyCodeLetter(char c) {
        c = Character.toLowerCase(c);
        if (c == 'a') return true;
        if (c == 'b') return true;
        if (c == 'i') return true;
        if (c == 'd') return true;
        if (c == 'p') return true;
        if (c == 'm') return true;
        if (c == 'l') return true;
        if (c == 'c') return true;
        return false;
    }    
        
    /////////////
    // METHODS //
    /////////////
    
    public Key getShiftedKeyByBpmDifference(float bpm_difference) {
        return Key.getKey(rootValue + bpm_difference / bpmDifferencePerSemitone, scaleType);
    }
    
    public Key getShiftedKeyBySemitones(float semitones) {
        return Key.getKey(rootValue + semitones, scaleType);
    }
    
    static public KeyRelation getClosestKeyRelation(float sourceBpm, Key sourceKey, float targetBpm, Key targetKey) {
        float bpmDifference = Bpm.getBpmDifference(sourceBpm, targetBpm);
        return getClosestKeyRelation(sourceBpm, sourceKey, targetBpm, targetKey, bpmDifference);
    }
    
    static public KeyRelation getClosestKeyRelation(float sourceBpm, Key sourceKey, float targetBpm, Key targetKey, float bpmDifference) {
        if ((sourceKey == null) || (targetKey == null) || !sourceKey.isValid() || !targetKey.isValid()) return KeyRelation.INVALID_RELATION;
        Key shiftedSourceKey = sourceKey.getShiftedKeyByBpmDifference(bpmDifference);        
        KeyRelation sourceRelationship = shiftedSourceKey.getKeyRelationTo(targetKey);
        return sourceRelationship;
    }    

    /**
     * This returns the shift required to transform the source scale type to the target scale type.
     * 
     * @param int sourceScaleType
     * @param int targetScaleType
     * @return float shift from source to target
     */
    static public float getRelativeShift(int sourceScaleType, int targetScaleType) {
        // convert source scale type to ionian:
        float sourceDifference = getShiftToIonian(sourceScaleType);
        float targetDifference = getShiftToIonian(targetScaleType);
        return (sourceDifference - targetDifference);        
    }    
    
    /**
     * This returns the shift required to transform the scale type to ionian (major).
     * 
     * @param int scaleType
     * @return float shift to ionian
     */
    static private float getShiftToIonian(int scaleType) {
        float difference = 0.0f;
        if (scaleType == SCALE_AEOLIAN)
            difference = 3.0f;
        else if (scaleType == SCALE_LYDIAN)
            difference = 7.0f;
        else if (scaleType == SCALE_MIXOLYDIAN)
            difference = 5.0f;
        else if (scaleType == SCALE_DORIAN)
            difference = 10.0f;
        else if (scaleType == SCALE_PHRYGIAN)
            difference = 8.0f;
        else if (scaleType == SCALE_LOCRIAN)
            difference = 1.0f;
        return difference;
    }      
    
    // changed to return just byte type for speed
    public KeyRelation getKeyRelationTo(Key targetKey) {
        if ((targetKey == null) || !targetKey.isValid()) return KeyRelation.INVALID_RELATION;
        //if (log.isTraceEnabled()) log.trace("getKeyRelationTo(): this=" + getKeyNotationSharp(true) + ", targetKey=" + targetKey.getKeyNotationSharp(true));
        boolean isRelative = false;
        float compare_rootValue = rootValue;
        float relative_shift = 0.0f;
        if (scaleType != targetKey.scaleType) {
            relative_shift = getRelativeShift(scaleType, targetKey.scaleType);
            compare_rootValue += relative_shift;
            if (compare_rootValue >= 12) compare_rootValue -= 12;
            isRelative = true;
        }
        if (relative_shift < 0.0) relative_shift += 12;
        // check for tonic 1st
        float actual_rootValue = compare_rootValue;
        float difference = targetKey.getRootValue() - actual_rootValue;
        while (difference >= 6.0) difference -= 12.0;
        while (difference < -6.0) difference += 12.0;
        if ((difference <= 0.5) && (difference >= -0.5)) {
            byte relationType = KeyRelation.RELATION_TONIC;
            if (isRelative) {
                int root_difference = shiftedNotation.getRootNote() - targetKey.getShiftedKeyNotation().getRootNote();
                if (root_difference < 0) root_difference += 12;
                if (root_difference >= 12) root_difference -= 12;
                if (root_difference == 0)
                    relationType = KeyRelation.RELATION_TONIC_MODAL;
                else if (root_difference == 7)
                    relationType = KeyRelation.RELATION_DOMINANT_MODAL;
                else if (root_difference == 5)
                    relationType = KeyRelation.RELATION_SUBDOMINANT_MODAL;
                else
                    relationType = KeyRelation.RELATION_RELATIVE_TONIC;
            }
            return new KeyRelation(difference, relationType);
        } else {
	        float minimum_difference = difference;
	        // check for dominant 2nd
	        actual_rootValue = compare_rootValue + 5.0f;
	        if (actual_rootValue >= 12) actual_rootValue -= 12;
	        difference = targetKey.getRootValue() - actual_rootValue;
	        while (difference >= 6.0) difference -= 12.0;
	        while (difference < -6.0) difference += 12.0;
	        if ((difference <= 0.5) && (difference >= -0.5)) {
	            byte relationType = KeyRelation.RELATION_DOMINANT;
	            if (isRelative) {
	                int root_difference = shiftedNotation.getRootNote() - targetKey.getShiftedKeyNotation().getRootNote();
	                if (root_difference < 0) root_difference += 12;
	                if (root_difference >= 12) root_difference -= 12;
	                if (root_difference == 0) {
	                    relationType = KeyRelation.RELATION_TONIC_MODAL;
	                    /*
	                } else if (root_difference == 2) {
	                    if ((getScaleType() == Key.SCALE_PHRYGIAN) &&
	                            ((targetKey.getScaleType() == Key.SCALE_AEOLIAN) || (targetKey.getScaleType() == Key.SCALE_PHRYGIAN)))
	                        relationType = KeyRelation.RELATION_NONE;
	                    else if ((getScaleType() == Key.SCALE_MIXOLYDIAN) &&
	                            ((targetKey.getScaleType() == Key.SCALE_IONIAN) || (targetKey.getScaleType() == Key.SCALE_MIXOLYDIAN)))
	                        relationType = KeyRelation.RELATION_NONE;
	                    else
	                        relationType = KeyRelation.RELATION_RELATIVE_DOMINANT;
	                        */
	                } else if (root_difference == 7) {
	                    relationType = KeyRelation.RELATION_DOMINANT_MODAL;
	                } else if (root_difference == 5) {
	                    relationType = KeyRelation.RELATION_SUBDOMINANT_MODAL;
	                } else {
	                    relationType = KeyRelation.RELATION_RELATIVE_DOMINANT;                
	                }
	            }
	            return new KeyRelation(difference, relationType);
	        } else {
		        if (Math.abs(difference) < Math.abs(minimum_difference))
		            minimum_difference = difference;
		        // check for subdominant 3rd
		        actual_rootValue = compare_rootValue + 7.0f;
		        if (actual_rootValue >= 12) actual_rootValue -= 12;
		        difference = targetKey.getRootValue() - actual_rootValue;
		        while (difference >= 6.0) difference -= 12.0;
		        while (difference < -6.0) difference += 12.0;
		        if ((difference <= 0.5) && (difference >= -0.5)) {
		            byte relationType = KeyRelation.RELATION_SUBDOMINANT;
		            if (isRelative) {
		                int root_difference = shiftedNotation.getRootNote() - targetKey.getShiftedKeyNotation().getRootNote();
		                if (root_difference < 0) root_difference += 12;
		                if (root_difference >= 12) root_difference -= 12;
		                if (root_difference == 0) {
		                    relationType = KeyRelation.RELATION_TONIC_MODAL;
		            	} else if (root_difference == 7) {
		                    relationType = KeyRelation.RELATION_DOMINANT_MODAL;
		                } else if (root_difference == 5) {
		                    relationType = KeyRelation.RELATION_SUBDOMINANT_MODAL;
		                    /*
		                } else if (root_difference == 10) {
		                    if ((targetKey.getScaleType() == Key.SCALE_PHRYGIAN) &&
		                            ((getScaleType() == Key.SCALE_AEOLIAN) || (getScaleType() == Key.SCALE_PHRYGIAN)))
		                        relationType = KeyRelation.RELATION_NONE;
		                    else if ((targetKey.getScaleType() == Key.SCALE_MIXOLYDIAN) &&
		                            ((getScaleType() == Key.SCALE_IONIAN) || (getScaleType() == Key.SCALE_MIXOLYDIAN)))
		                        relationType = KeyRelation.RELATION_NONE;
		                    else
		                        relationType = KeyRelation.RELATION_RELATIVE_SUBDOMINANT;
		                        */
		                } else {
		                    relationType = KeyRelation.RELATION_RELATIVE_SUBDOMINANT;
		                }
		            }
		            return new KeyRelation(difference, relationType);
		        } else {
			        if (Math.abs(difference) < Math.abs(minimum_difference))
			            minimum_difference = difference;
			        // no key relation...
			        return new KeyRelation(minimum_difference, KeyRelation.RELATION_NONE);
		        }
	        }
        }
    }        
    
    public boolean equals(Object object) {
        if (object instanceof Key) {
            Key compare_key = (Key)object;
            return this.getKeyNotationSharp(true).equals(compare_key.getKeyNotationSharp(true));
        }  
        return false;
    }
    public int hashCode() {
        return this.getKeyNotationSharp(true).hashCode();
    }
        
    public int compareTo(Key compareKey) {
    	if (isValid() && compareKey.isValid()) {
	        if (rootValue < compareKey.rootValue)
	            return -1;
	        else if (rootValue > compareKey.rootValue)
	            return 1;
	        else {
	            if (scaleType < compareKey.scaleType)
	                return -1;
	            else if (scaleType > compareKey.scaleType)
	                return 1;
	            return 0;
	        }
    	}
    	if (isValid())
    		return -1;
    	if (compareKey.isValid())
    		return 1;
    	return 0;
    }               
    
    /////////////
    // PRIVATE //
    /////////////
    
    private void validateRootValue() {    
        rootValue = validateRootValue(rootValue);
    }
    
    static private float validateRootValue(float rootValue) {
        if (rootValue == Float.NEGATIVE_INFINITY)
            return rootValue;
        while (rootValue >= 11.5) rootValue -= 12;
        while (rootValue < -0.5) rootValue += 12;
        return rootValue;
    }        

    private void appendMode(StringBuffer result) {
        result.append(" ");
        result.append(getScaleTypeDescription());
    }
    
    private int appendShift(StringBuffer result) {
        int shift = (int)(shiftedNotation.getShift() * 100.0);
        if (shift > 0) {
            result.append(" +");
            result.append(shift);
        } else if (shift < 0) {
            result.append(" ");
            result.append(shift);
        }
        return shift;
    }
        
    ////////////////////////
    // FLY WEIGHT PATTERN //
    ////////////////////////
    
    static public Key getKey(String description) {
        try {
	        if ((description == null) || description.trim().equals("")) return NO_KEY;
	        
	        float rootValue = Key.ROOT_UNKNOWN;
	        byte scaleType = Key.SCALE_UNKNOWN;
	        
	        // parse out the shift from the end:
	        boolean negative_shift = false;
	        float shift = 0.0f;
	        int shift_index = description.indexOf("+");
	        if (shift_index == -1) {
	            shift_index = description.indexOf("-");
	            negative_shift = true;
	        }
	        if (shift_index >= 0) {
	            int start_index = shift_index + 1;
	            while ((start_index < description.length()) && !Character.isDigit(description.charAt(start_index))) {
	                ++start_index;
	            }
	            int end_index = start_index + 1;            
	            while ((end_index < description.length()) && Character.isDigit(description.charAt(end_index))) {
	                ++end_index;
	            }
	            try {
	                // the shift should be in cents, 100 cents = 1 semitone
	                shift = Float.parseFloat(description.substring(start_index, end_index)) / 100.0f;
	                if (negative_shift) shift = -shift;
	            } catch (Exception e) { }
	            description = description.substring(0, shift_index);
	        }
	        
	        description = description.toLowerCase().trim();
	        
	        // look for keycodes:
	        int numericIndex = 0;
	        while ((numericIndex < description.length()) && Character.isDigit(description.charAt(numericIndex))) {
	            ++numericIndex;
	        }
	        if ((numericIndex > 0) && (description.length() > numericIndex)) {
	            int keyCodeNumber = Integer.parseInt(description.substring(0, numericIndex));
		        if (keyCodeNumber == 1) rootValue = 11.0f; // G#            
		        else if (keyCodeNumber == 2) rootValue = 6.0f; // D#            
		        else if (keyCodeNumber == 3) rootValue = 1.0f; // A#           
		        else if (keyCodeNumber == 4) rootValue = 8.0f; // F            
		        else if (keyCodeNumber == 5) rootValue = 3.0f; // C           
		        else if (keyCodeNumber == 6) rootValue = 10.0f; // G          
		        else if (keyCodeNumber == 7) rootValue = 5.0f; // D          
		        else if (keyCodeNumber == 8) rootValue = 0.0f; // A
		        else if (keyCodeNumber == 9) rootValue = 7.0f; // E           
		        else if (keyCodeNumber == 10) rootValue = 2.0f; // B            
		        else if (keyCodeNumber == 11) rootValue = 9.0f; // F#            
		        else if (keyCodeNumber == 12) rootValue = 4.0f; // C#
		        if (rootValue != ROOT_UNKNOWN) {
			        char keyCodeScaleType = description.charAt(numericIndex);
			        if (keyCodeScaleType == 'a') {
			            scaleType = SCALE_AEOLIAN;
			        } else if ((keyCodeScaleType == 'b') || (keyCodeScaleType == 'i')) {
			            scaleType = SCALE_IONIAN;
			            rootValue += 3.0;
			        } else if (keyCodeScaleType == 'd') {
			            scaleType = SCALE_DORIAN;
			            rootValue += 5.0;		            
			        } else if (keyCodeScaleType == 'l') {
			            scaleType = SCALE_LYDIAN;
			            rootValue -= 4.0;
			        } else if (keyCodeScaleType == 'm') {
			            scaleType = SCALE_MIXOLYDIAN;
			            rootValue -= 2.0;		            
			        } else if (keyCodeScaleType == 'p') {
			            scaleType = SCALE_PHRYGIAN;
			            rootValue -= 5.0;
			        } else if (keyCodeScaleType == 'c') {
			            scaleType = SCALE_LOCRIAN;
			            rootValue += 2.0;
			        }
		        }
	        }
	        
	        if (rootValue == ROOT_UNKNOWN) {
	            // for for standard key notation:
		        if (description.startsWith("a")) rootValue = 0.0f;
		        if (description.startsWith("b")) rootValue = 2.0f;
		        if (description.startsWith("c")) rootValue = 3.0f;
		        if (description.startsWith("d")) rootValue = 5.0f;
		        if (description.startsWith("e")) rootValue = 7.0f;
		        if (description.startsWith("f")) rootValue = 8.0f;
		        if (description.startsWith("g")) rootValue = 10.0f;
		        if (rootValue != ROOT_UNKNOWN) {
		            for (int i = 1; i < description.length(); ++i) {
		                if (description.charAt(i) == '#') rootValue += 1.0;
		                if (description.charAt(i) == 'b') rootValue -= 1.0;
		            }
		            if (rootValue < 0) rootValue += 12;
			        // determine the scale type:
			        if (StringUtil.substring("m", description) && 
			                !StringUtil.substring("major", description) &&
			                !StringUtil.substring("mix", description))
			            scaleType = SCALE_AEOLIAN;
			        else
			            scaleType = SCALE_IONIAN;
		        }
	        }
	        
	        if ((rootValue != ROOT_UNKNOWN) && (shift != 0.0)) rootValue += shift;
	        rootValue = validateRootValue(rootValue);
	        
	        // look for non-standard modes:
	        if (StringUtil.substring("dor", description)) scaleType = SCALE_DORIAN;
	        else if (StringUtil.substring("phr", description)) scaleType = SCALE_PHRYGIAN;
	        else if (StringUtil.substring("mix", description)) scaleType = SCALE_MIXOLYDIAN;
	        else if (StringUtil.substring("lyd", description)) scaleType = SCALE_LYDIAN;
	        else if (StringUtil.substring("loc", description)) scaleType = SCALE_LOCRIAN;

	        Key result = checkFactory(rootValue, scaleType);	        
            //if (log.isTraceEnabled()) log.trace("getKey(): result=" + result);            
	        return result;
	        
        } catch (Exception e) {
            log.error("Key(): error Exception", e);
        }   
        return NO_KEY;
    }
    
    static public Key getKey(float rootValue, byte scaleType) {
        Key result = checkFactory(rootValue, scaleType);	        
        return result;
    }                       
    
    static private Map<String, Key> keyFlyWeights = new HashMap<String, Key>();
    
    static private String calculatePrimaryKey(int shifted_key, byte scaleType) {
        StringBuffer result = new StringBuffer();
        result.append(shifted_key);
        result.append(",");
        result.append(scaleType);
        return result.toString();
    }
    
    static private Key checkFactory(float rootValue, byte scaleType) {
        String primaryKey = calculatePrimaryKey((int)(rootValue * 100.0), scaleType);
        Key result = (Key)keyFlyWeights.get(primaryKey);
        if (result == null) {
            if (log.isTraceEnabled())
            	log.trace("checkFactory(): no existing key found");
            result = new Key(rootValue, scaleType);
            keyFlyWeights.put(primaryKey, result);
        }
        return result;
    }

    ///////////////////////
    // FOR SERIALIZATION //    
    ///////////////////////
    
	public float geRrootValue() { return rootValue; }
	public void setRootValue(float rootValue) { this.rootValue = rootValue; }

	public void setScaleType(byte scaleType) { this.scaleType = scaleType; }
	public ShiftedKeyNotation getShiftedNotation() {return shiftedNotation; }
	public void setShiftedNotation(ShiftedKeyNotation shiftedNotation) { this.shiftedNotation = shiftedNotation; }

    
    
}
