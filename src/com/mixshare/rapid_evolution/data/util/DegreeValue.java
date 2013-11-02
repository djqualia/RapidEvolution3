package com.mixshare.rapid_evolution.data.util;

import java.io.Serializable;

import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * This class pairs an object with a degree (a value between 0 and 1.0 indicating a percentage), as
 * well as a source (where the data came from).  This allows style and tag associations to have a degree
 * of how much they apply and therefore more accurate comparisions/similarity.
 */
public class DegreeValue implements Comparable<DegreeValue>, Serializable {
	
	static private final long serialVersionUID = 0L;
    
    ////////////
    // FIELDS //
    ////////////
    
    protected Object object;
    protected float percentage;
	protected byte source;
    
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
    public DegreeValue() { }
	public DegreeValue(Object object, float percentage, byte source) {
        this.object = object;
        this.percentage = percentage;
    	this.source = source;
    }
	public DegreeValue(LineReader lineReader) {
    	int version = Integer.parseInt(lineReader.getNextLine());
    	object = lineReader.getNextLine();
    	percentage = Float.parseFloat(lineReader.getNextLine());
    	source = Byte.parseByte(lineReader.getNextLine());
	}

    /////////////
    // GETTERS //
    /////////////   
    
    public String getName() {
    	if (object == null)
    		return "";
    	return object.toString();
    }
    
    public Object getObject() { return object; }
   
    public float getPercentage() {
    	if (Float.isNaN(percentage)) return 0.0f;
    	if (Float.isInfinite(percentage)) return 0.0f;
    	return percentage;
    }
         
    public byte getSource() { return source; }

    /////////////
    // SETTERS //
    /////////////
    
    public void setPercentage(float value) { percentage = value; }
    public void setSource(byte source) { this.source = source; }
	public void setObject(Object object) { this.object = object; }        
    
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(getName());
        result.append(" (");
        result.append(String.valueOf(Math.round(getPercentage() * 100.0f)));
        result.append("%)");
        return result.toString();        
    }
    
    public int compareTo(DegreeValue oS) {
        if (percentage < oS.getPercentage()) return 1;
        if (percentage > oS.getPercentage()) return -1;
        return object.toString().compareToIgnoreCase(oS.getObject().toString());
    }
    
    public boolean equals(Object o) {
		if (o instanceof DegreeValue) {
			DegreeValue oP = (DegreeValue)o;		
			if (Math.abs(oP.percentage - percentage) > 0.0000001f)
				return false;
			if (!object.equals(oP.object))
				return false;
			return true;
		}
		return false;
	}
    
    public void write(LineWriter writer) {
    	writer.writeLine(1); // version
    	writer.writeLine(object.toString());
    	writer.writeLine(percentage);
    	writer.writeLine(source);
    }
    
}
