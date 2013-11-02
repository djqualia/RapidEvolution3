package com.mixshare.rapid_evolution.audio.detection.key.filters;

public interface KeyProbabilityFilter {
    
    public void add(double[] values);

    public void add(int[] values);
        
    public KeyProbabilitySet getNormalizedProbabilities();
    
    public boolean isAllZeros();
    
}
