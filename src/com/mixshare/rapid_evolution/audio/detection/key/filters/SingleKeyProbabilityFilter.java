package com.mixshare.rapid_evolution.audio.detection.key.filters;

import org.apache.log4j.Logger;

public class SingleKeyProbabilityFilter implements KeyProbabilityFilter {

	static private Logger log = Logger.getLogger(SingleKeyProbabilityFilter.class);
    
    static private double decay = 1.0;
    
    private boolean major;
    private String type;
    private double[][] pmatrix = new double[12][12];
    private double[] probability = new double[12];
    
    public SingleKeyProbabilityFilter(boolean major, String type, double[] probabilities) {
        if ((probabilities == null) || (probabilities.length != 12))
            log.error("SingleKeyProbabilityFilter(): probabilities array is incorrect lengh for key=" + type);
        double total = 0.0;
        int num_notes = 0;
        for (int i = 0; i < 12; ++i) {
            if (probabilities[i] > 0.0) ++num_notes;
            total += probabilities[i];
        }
        if (Math.abs(total - 1.0) > 0.0001)
            log.error("SingleKeyProbabilityFilter(): total probability != 1.0 for key=" + type);
        this.major = major;
        this.type = type;
        for (int k = 0; k < 12; ++k) {
            for (int n = 0; n < 12; ++n) {
                int offset = n + k;
                if (offset >= 12) offset -= 12;
                pmatrix[k][offset] = probabilities[n];
            }
        }
    }        
    
    public String getDescription() {
        return type;
    }
    
    public void add(double[] values) {
        for (int k = 0; k < 12; ++k) {
            double total = 0.0;
            for (int n = 0; n < 12; ++n)
                total += pmatrix[k][n] * values[n];           
            probability[k] = total + probability[k];// * decay;
        }
    }

    public void add(int[] values) {
        for (int k = 0; k < 12; ++k) {
            double total = 0.0;
            for (int n = 0; n < 12; ++n)
                total += pmatrix[k][n] * ((double)values[n]);            
            probability[k] = total + probability[k];// * decay;
        }
    }
    
    public double getProbability(int index) {
        return probability[index];
    }
        
    public void clearProbabilities() {
        for (int i = 0; i < 12; ++i)
            probability[i] = 0.0;        
    }    
    
    public KeyProbabilitySet getNormalizedProbabilities() {        
        double[] normalizedProbabilities = new double[12];
        double total = 0.0;
        for (int i = 0; i < 12; ++i)
            total += probability[i];
        for (int i = 0; i < 12; ++i)
            normalizedProbabilities[i] = probability[i] / total;
        return new KeyProbabilitySet(normalizedProbabilities, major, type);
    }
    
    public boolean isAllZeros() {
        for (int i = 0; i < 12; ++i)
            if (probability[i] > 0.0)
            	return false;        
        return true;        
    }    
}
