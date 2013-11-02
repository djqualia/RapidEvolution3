package com.mixshare.rapid_evolution.music.bpm;

import java.io.Serializable;

/**
 * BPM = beats per minute
 * This class includes helper methods for computing the difference between BPM values.
 */
public class Bpm implements Comparable<Bpm>, Serializable {
	
    static private final long serialVersionUID = 0L;    
	
    static public Bpm NO_BPM = new Bpm(0.0f);
    
    ////////////
    // FIELDS //
    ////////////
    
	float bpmValue;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	// for serialization
	public Bpm() { }
	
	public Bpm(float bpmValue) { this.bpmValue = bpmValue; }
	public Bpm(double bpmValue) { this.bpmValue = (float)bpmValue; }
	public Bpm(Float bpmValue) { this.bpmValue = bpmValue == null ? 0.0f : bpmValue; }
	
	/////////////
	// GETTERS //
	/////////////
	
	public float getBpmValue() { return bpmValue; }

	public float getDifference(Bpm value) { return getBpmDifference(bpmValue, value.getBpmValue()); }
	public float getDifference(float value) { return getBpmDifference(bpmValue, value); }
	
	public boolean isValid() { return bpmValue != 0.0f; }
	
    /**
     * This method will determine the percentage difference between two BPM values.
     * Since some BPMs are equivalent (i.e. 50bpm == 100bpm), the space covering
     * all the powers of 2 must be searched for the lowest possible difference.
     * 
     * For example:
     *   where sourceBpm=50 and targetBpm=105, the BPM difference is +5(%)
     */
    static public Double getBpmDifference(double sourceBpm, double targetBpm) {
        if ((sourceBpm == 0.0) || (targetBpm == 0.0))
        	return null;
        if (sourceBpm == targetBpm) return 0.0;
        double diff_before_shift = targetBpm / sourceBpm * 100.0; // avoids calculating twice
        double diff = diff_before_shift - 100.0;
        if (diff < 0.0) {
            double diff2 = diff_before_shift * 2.0 - 100.0;
            if (diff2 == 0.0) return 0.0;
            else if (diff2 > 0) {
                if (-diff < diff2) return diff;
                else return diff2;
            } else {
                return getBpmDifference(sourceBpm, targetBpm * 2.0);
            }
        } else { // diff > 0.0
            double diff2 = diff_before_shift / 2.0 - 100.0;
            if (diff2 == 0.0) return 0.0;
            else if (diff2 < 0) {
                if (diff < -diff2) return diff;
                else return diff2;
            } else {
                return getBpmDifference(sourceBpm, targetBpm / 2.0);
            }
        }
    }    
    static public Float getBpmDifference(float sourceBpm, float targetBpm) {
        if ((sourceBpm == 0.0f) || (targetBpm == 0.0f))
        	return null;
        if (sourceBpm == targetBpm) return 0.0f;
        float diff_before_shift = targetBpm / sourceBpm * 100.0f; // avoids calculating twice
        float diff = diff_before_shift - 100.0f;
        if (diff < 0.0f) {
            float diff2 = diff_before_shift * 2.0f - 100.0f;
            if (diff2 == 0.0f) return 0.0f;
            else if (diff2 > 0.0f) {
                if (-diff < diff2) return diff;
                else return diff2;
            } else {
                return getBpmDifference(sourceBpm, targetBpm * 2.0f);
            }
        } else { // diff > 0.0f
            float diff2 = diff_before_shift / 2.0f - 100.0f;
            if (diff2 == 0.0f) return 0.0f;
            else if (diff2 < 0.0f) {
                if (diff < -diff2) return diff;
                else return diff2;
            } else {
                return getBpmDifference(sourceBpm, targetBpm / 2.0f);
            }
        }    	
    }
    
	/////////////
	// SETTERS //
	/////////////
	
	public void setBpmValue(float bpmValue) { this.bpmValue = bpmValue; }
	
	/////////////
	// METHODS //
	/////////////
	
	public float getSimilarityWith(Bpm otherBpm) { return getSimilarity(bpmValue, otherBpm.bpmValue); }
	static public float getSimilarity(float bpmValue1, float bpmValue2) {
		if ((bpmValue1 == 0.0f) || (bpmValue2 == 0.0f))
			return 0.0f;
		float absDiff = Math.abs(bpmValue1 - bpmValue2);
		float total = bpmValue1 + bpmValue2;
		float similarity = (total - absDiff * 2.0f) / total;
		return similarity;		
	}
	
	public String toString() { return toString(false); }

	public String toString(boolean rounded) {
		if (bpmValue != 0.0f) {
			if (rounded)
				return String.valueOf(Math.round(bpmValue));
			else
				return String.valueOf(bpmValue);
		}
		return "";
	}
	
	public int compareTo(Bpm b) {
		if ((bpmValue != 0.0f) && (b.bpmValue != 0.0f)) {
			if (bpmValue < b.bpmValue)
				return -1;
			if (bpmValue > b.bpmValue)
				return 1;			
			return 0;
		}
		if (bpmValue != 0.0f)
			return -1;
		if (b.bpmValue != 0.0f)
			return 1;
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Bpm) {
			Bpm b = (Bpm)o;
			return (bpmValue == b.bpmValue);
		}
		return false;
	}
	
	public int hashCode() { return (int)(bpmValue * 100); }		
    
}
