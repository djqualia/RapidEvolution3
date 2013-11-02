package com.mixshare.rapid_evolution.audio.detection.key.filters;

import java.util.Vector;

import org.apache.log4j.Logger;

public class MultiKeyProbabilityFilter implements KeyProbabilityFilter {

    private static Logger log = Logger.getLogger(KeyProbabilityFilter.class);
    
    private boolean major;
    private String type;
    private Vector<KeyProbabilityFilter> filters = new Vector<KeyProbabilityFilter>();
    private double[] probability = new double[12];    
    
    public MultiKeyProbabilityFilter(boolean major, String type, double[][] probabilities) {
        this.major = major;
        this.type = type;
        for (int p = 0; p < probabilities.length; ++p) {
            SingleKeyProbabilityFilter filter = new SingleKeyProbabilityFilter(major, type, probabilities[p]);
            filters.add(filter);
        }
    }        
    
    public String getDescription() {
        return type; 
    }
    
    public void add(double[] values) {
        double[] add_probabilities = new double[12];
        for (int f = 0; f < filters.size(); ++f) {
            SingleKeyProbabilityFilter filter = (SingleKeyProbabilityFilter)filters.get(f);
            filter.clearProbabilities();
            filter.add(values);
            for (int p = 0; p < 12; ++p)
                if (filter.getProbability(p) > add_probabilities[p])
                    add_probabilities[p] = filter.getProbability(p);            
        }        
        for (int p = 0; p < 12; ++p)
            probability[p] += add_probabilities[p];        
    }

    public void add(int[] values) {
        double[] add_probabilities = new double[12];
        for (int f = 0; f < filters.size(); ++f) {
            SingleKeyProbabilityFilter filter = (SingleKeyProbabilityFilter)filters.get(f);
            filter.clearProbabilities();
            filter.add(values);
            for (int p = 0; p < 12; ++p)
                if (filter.getProbability(p) > add_probabilities[p])
                    add_probabilities[p] = filter.getProbability(p);            
        }        
        for (int p = 0; p < 12; ++p)
            probability[p] += add_probabilities[p];        
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
