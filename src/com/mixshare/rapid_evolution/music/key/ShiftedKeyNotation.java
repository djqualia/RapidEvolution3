package com.mixshare.rapid_evolution.music.key;

import java.io.Serializable;
import org.apache.log4j.Logger;

public class ShiftedKeyNotation implements Serializable {

    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(ShiftedKeyNotation.class);
    
    ////////////
    // FIELDS //
    ////////////
    
    private byte rootNote;
    private float shift;
        
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public ShiftedKeyNotation() { }
    
    public ShiftedKeyNotation(float root_value) {
        if (log.isTraceEnabled()) log.trace("ShiftedKeyNotation(): root_value=" + root_value);
        for (byte i = 0; i < 12; ++i) {
            float diff = root_value - (float)i;
            if (diff >= 11) diff -= 12;
            if (diff <= -11) diff += 12;
            if (Math.abs(diff) <= 0.5) {
                rootNote = i;
                shift = diff;
            }
        }        
    }
    
    public ShiftedKeyNotation(byte rootNote, float shift) {
        this.rootNote = rootNote;
        this.shift = shift;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public byte getRootNote() { return rootNote; }    
    public float getShift() { return shift; }
    public int getShiftInCents() { return (int)(100.0 * shift); }

	public byte getRoot_note() { return rootNote; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setRootNote(byte rootNote) { this.rootNote = rootNote; }
	public void setShift(float shift) { this.shift = shift; }    
    
}
