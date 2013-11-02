package com.mixshare.rapid_evolution.data.util.similarity;

import java.util.Vector;

public class PearsonSimilarity {

	/**
	 * Computes the pearson similarity coefficient for 2 arrays of id/degree values.  The id/degree arrays are assumed to be sorted from greatest
	 * to least before this method is called.  For performance reasons, the max # of IDs to check and minimum degree threshold can be provided.
	 * 
	 * The resulting value is between -1.0 and 1.0 (or 0.0 and 1.0 if the degrees are all positive)
	 * 
	 * @param ids1
	 * @param degrees1
	 * @param ids2
	 * @param degrees2
	 * @param maxIdsToCheck
	 * @param minDegreeThreshold
	 * @return float between 0.0 and 1.0
	 */
	static public float computeSimilarity(int[] ids1, float[] degrees1, int[] ids2, float[] degrees2) { return computeSimilarity(ids1, degrees1, ids2, degrees2, Integer.MAX_VALUE, 0.0f); }
    static public float computeSimilarity(int[] ids1, float[] degrees1, int[] ids2, float[] degrees2, int maxIdsToCheck, float minDegreeThreshold) {
        float numerator = 0.0f;
        float denom1 = 0.0f;
        float denom2 = 0.0f;
        int amt = Math.min((ids1 != null) ? ids1.length : 0, maxIdsToCheck);
        int amt2 = Math.min((ids2 != null) ? ids2.length : 0, maxIdsToCheck);
        int s = 0;
        boolean stop = false;
        while ((s < amt) && !stop) {
        	if (degrees1[s] >= minDegreeThreshold) {
	            boolean found = false;
	            int s2 = 0;
	            while ((s2 < amt2) && !found) {            	
	                if (ids1[s] == ids2[s2]) {
	                    numerator += degrees1[s] * degrees2[s2];
	                    denom1 += degrees1[s] * degrees1[s];
	                    denom2 += degrees2[s2] * degrees2[s2];
	                    found = true;
	                }
	                ++s2;
	            }
	            if (!found) {
	                denom1 += degrees1[s] * degrees1[s];
	            }
        	} else {
        		stop = true;
        	}
        	++s;
        }
        s = 0;
        stop = false;
        while ((s < amt2) && !stop) {
        	if (degrees2[s] >= minDegreeThreshold) {
	        	boolean alreadyProcessed = false; 
	            int s2 = 0;
	            while ((s2 < amt) && !alreadyProcessed) {
	                if (ids1[s2] == ids2[s])
	                    alreadyProcessed = true;
	                ++s2;
	            }
	            if (!alreadyProcessed) {
	                denom2 += degrees2[s] * degrees2[s];
	            }
        	} else {
        		stop = true;
        	}
        	++s;
        }
        float similarity = (float)(numerator / Math.sqrt(denom1 * denom2));
        return similarity;
    }  
    
    /**
     * Same as the method above only it assumes the degrees are all 1.0f (for when this information is not available).
     */
    static public float computeSimilarity(int[] ids1, int[] ids2) { return computeSimilarity(ids1, ids2, Integer.MAX_VALUE); }
    static public float computeSimilarity(int[] ids1, int[] ids2, int maxIdsToCheck) {
        float numerator = 0.0f;
        float denom1 = 0.0f;
        float denom2 = 0.0f;
        int amt = Math.min((ids1 != null) ? ids1.length : 0, maxIdsToCheck);
        int amt2 = Math.min((ids2 != null) ? ids2.length : 0, maxIdsToCheck);
        int s = 0;
        while (s < amt) {
            boolean found = false;
            int s2 = 0;
            while ((s2 < amt2) && !found) {            	
                if (ids1[s] == ids2[s2]) {
                    numerator += 1.0f;
                    denom1 += 1.0f;
                    denom2 += 1.0f;
                    found = true;
                }
                ++s2;
            }
            if (!found) {
                denom1 += 1.0f;
            }
        	++s;
        }
        s = 0;
        while (s < amt2) {
        	boolean alreadyProcessed = false; 
            int s2 = 0;
            while ((s2 < amt) && !alreadyProcessed) {
                if (ids1[s2] == ids2[s])
                    alreadyProcessed = true;
                ++s2;
            }
            if (!alreadyProcessed) {
                denom2 += 1.0f;
            }
        	++s;
        }
        float similarity = (float)(numerator / Math.sqrt(denom1 * denom2));
        return similarity;
    }  
    
    static public float computeSimilarity(Vector<String> ids1, Vector<String> ids2) {
    	return computeSimilarity(ids1, ids2, Integer.MAX_VALUE);
    }
    static public float computeSimilarity(Vector<String> ids1, Vector<String> ids2, int maxIdsToCheck) {
        float numerator = 0.0f;
        float denom1 = 0.0f;
        float denom2 = 0.0f;
        int amt = Math.min((ids1 != null) ? ids1.size() : 0, maxIdsToCheck);
        int amt2 = Math.min((ids2 != null) ? ids2.size() : 0, maxIdsToCheck);
        int s = 0;
        while (s < amt) {
            boolean found = false;
            int s2 = 0;
            while ((s2 < amt2) && !found) {            	
                if (ids1.get(s).equals(ids2.get(s2))) {
                    numerator += 1.0f;
                    denom1 += 1.0f;
                    denom2 += 1.0f;
                    found = true;
                }
                ++s2;
            }
            if (!found) {
                denom1 += 1.0f;
            }
        	++s;
        }
        s = 0;
        while (s < amt2) {
        	boolean alreadyProcessed = false; 
            int s2 = 0;
            while ((s2 < amt) && !alreadyProcessed) {
                if (ids1.get(s2).equals(ids2.get(s)))
                    alreadyProcessed = true;
                ++s2;
            }
            if (!alreadyProcessed) {
                denom2 += 1.0f;
            }
        	++s;
        }
        float similarity = (float)(numerator / Math.sqrt(denom1 * denom2));
        return similarity;
    }      
    
    
}
