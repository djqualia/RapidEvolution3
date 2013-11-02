package com.mixshare.rapid_evolution.audio.detection.rga;

import java.nio.ByteBuffer;

public class RGAData implements Comparable<RGAData> {

	static public RGAData NO_RGA = new RGAData();
    
	////////////
	// FIELDS //
	////////////
	
    private float difference = Float.MAX_VALUE;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public RGAData() { }
    public RGAData(float difference) {
        this.difference = difference;
    }
    public RGAData(double difference) {
        this.difference = (float)difference;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public float getDifference() { return difference; }
    
    public boolean isValid() { return difference != Float.MAX_VALUE; }
    
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        if (!isValid())
        	return "";
        float roundedDifference = ((float)((int)(difference * 100.0f))) / 100.0f;
        StringBuffer result = new StringBuffer();
        if (roundedDifference > 0)
            result.append("+");
        result.append(String.valueOf(roundedDifference));
        result.append(" dB");
        return result.toString();
    }
    
    public int compareTo(RGAData oD) {
        if (difference < oD.difference)
            return -1;
        if (difference > oD.difference)
            return 1;
        return 0;
    }
    
    public boolean equals(Object o) {
    	if (o instanceof RGAData) {
            RGAData oD = (RGAData)o;
            return oD.difference == difference;
    	}
    	return false;
    }
    
    public int hashCode() {
    	return (int)(difference * 10000);
    }    
    
    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        // TODO: code        
        return buffer;
    }
        
}
