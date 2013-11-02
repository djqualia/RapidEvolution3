package com.mixshare.rapid_evolution.music.key;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class KeyRelation implements Comparable<KeyRelation>, Serializable {
    
    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(KeyRelation.class);
    
    ///////////////
    // CONSTANTS //
    ///////////////
    
    static public byte RELATION_TONIC = 0;
    static public byte RELATION_TONIC_MODAL = 1;
    static public byte RELATION_DOMINANT = 2;
    static public byte RELATION_DOMINANT_MODAL = 3;
    static public byte RELATION_SUBDOMINANT = 4;
    static public byte RELATION_SUBDOMINANT_MODAL = 5;
    static public byte RELATION_RELATIVE_TONIC = 6;
    static public byte RELATION_RELATIVE_DOMINANT = 7;
    static public byte RELATION_RELATIVE_SUBDOMINANT = 8;
    static public byte RELATION_NONE = Byte.MAX_VALUE;

    static public KeyRelation INVALID_RELATION = new KeyRelation(Float.NEGATIVE_INFINITY, RELATION_NONE);
    
    ////////////
    // FIELDS //
    ////////////
    
    private float difference = Float.NEGATIVE_INFINITY;
    private byte relationship = RELATION_NONE;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public KeyRelation() { }
    public KeyRelation(float difference, byte relationship) {
        if (log.isTraceEnabled()) log.trace("KeyRelation(): difference=" + difference + ", relationship=" + relationship);
        this.difference = difference;
        this.relationship = relationship;
    }
    
    /////////////
    // GETTERS //
    /////////////

    public float getDifference() { return difference; }
    public byte getRelationship() { return relationship; }

    public boolean hasDifference() { return (difference != Float.NEGATIVE_INFINITY); }
    
    public boolean isCompatible() { return isCompatible(true); }
    public boolean isCompatible(boolean strict) { return isCompatible(relationship, strict); }    
    static public boolean isCompatible(byte relationship, boolean strict) {
    	if (strict) {
    		return (relationship != RELATION_NONE) && (relationship != RELATION_RELATIVE_DOMINANT) && (relationship != RELATION_RELATIVE_SUBDOMINANT);
    	}
        return relationship != RELATION_NONE;
    }
    
    public boolean isValid() { return difference != Float.NEGATIVE_INFINITY; }
    
    /////////////
    // SETTERS //
    /////////////

    public void setDifference(float difference) { this.difference = difference; }
	public void setRelationship(byte relationship) { this.relationship = relationship; }
    
    /////////////
    // METHODS //
    /////////////
    
    public int compareTo(KeyRelation oRelation) {
        if (relationship < oRelation.getRelationship()) return -1;
        if (relationship > oRelation.getRelationship()) return 1;
        return 0;
    }    
    
    public String toString() { return toString(relationship); }
    static public String toString(byte relationship) {
        if (relationship == RELATION_NONE) {
            return "";
        } else if (relationship == RELATION_TONIC) {
            return "Tonic";
        } else if (relationship == RELATION_SUBDOMINANT) {
            return "Subdominant";
        } else if (relationship == RELATION_DOMINANT) {
            return "Dominant";            
        } else if (relationship == RELATION_TONIC_MODAL) {
            return "Tonic Modal";
        } else if (relationship == RELATION_SUBDOMINANT_MODAL) {
            return "Subdominant Modal";
        } else if (relationship == RELATION_DOMINANT_MODAL) {
            return "Dominant Modal";            
        } else if (relationship == RELATION_RELATIVE_TONIC) {
            return "Relative Tonic";            
        } else if (relationship == RELATION_RELATIVE_SUBDOMINANT) {
            return "Relative Subdominant";            
        } else if (relationship == RELATION_RELATIVE_DOMINANT) {
            return "Relative Dominant";            
        }
        return null;    	
    }
    
}
