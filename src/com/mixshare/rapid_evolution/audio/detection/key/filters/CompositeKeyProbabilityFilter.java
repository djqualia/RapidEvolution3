package com.mixshare.rapid_evolution.audio.detection.key.filters;

public class CompositeKeyProbabilityFilter implements KeyProbabilityFilter {

    private KeyProbabilityFilter filter1;
    private KeyProbabilityFilter filter2;
    private double weight1;
    private double weight2;
    
    public CompositeKeyProbabilityFilter(KeyProbabilityFilter filter1, KeyProbabilityFilter filter2,
            double weight1, double weight2) {
        this.filter1 = filter1;
        this.filter2 = filter2;
        this.weight1 = weight1;
        this.weight2 = weight2;
    }
    
    public void add(double[] values) {
        filter1.add(values);
        filter2.add(values);
    }

    public void add(int[] values) {
        filter1.add(values);
        filter2.add(values);        
    }    
        
    public KeyProbabilitySet getNormalizedProbabilities() {
        KeyProbabilitySet set1 = filter1.getNormalizedProbabilities();
        KeyProbabilitySet set2 = filter2.getNormalizedProbabilities();
        double[] set3 = new double[12];
        for (int i = 0; i < 12; ++i)
            set3[i] = set1.getProbability(i) * weight1 + set2.getProbability(i) * weight2;        
        return new KeyProbabilitySet(set3, set1.isMajor(), set1.getType());
    }
    
    public boolean isAllZeros() {
        return (filter1.isAllZeros() && filter2.isAllZeros());
    }
    
}
