package com.mixshare.rapid_evolution.audio.dsp;

public class EllipticalFilter {
	
	double[] a = null;
	double[] b = null;	
	double[] past_a = null;
	double[] past_b = null;
	
	public EllipticalFilter(double[] in_a, double[] in_b) {
	    a = in_a;
	    b = in_b;
	    past_a = new double[in_a.length];
	    past_b = new double[in_b.length];
	    for (int i = 0; i < past_a.length; ++i)
	    	past_a[i] = 0.0;
	    for (int i = 0; i < past_b.length; ++i)
	    	past_b[i] = 0.0;
	}
		
	public double process(double val) {
		for (int i = 1; i < past_b.length; ++i)
			past_b[i] = past_b[i - 1];
	    past_b[0] = val;
	    double newval = 0.0;
	    for (int k = 0; k < b.length; ++k)
	      newval += b[k] * past_b[k];	    
	    for (int k = 0; k < a.length; ++k)
	      newval += a[k] * past_a[k];	    
	    for (int i = 1; i < past_a.length; ++i) 
	    	past_a[i] = past_a[i - 1];
	    past_a[0] = newval;
	    return newval;
	}
	
}
