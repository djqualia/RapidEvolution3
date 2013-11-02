package com.mixshare.rapid_evolution.audio.dsp;

public class Hanning {

	private double[] values = null;
	
	public Hanning(double samplerate) {
		int n = (int)(samplerate * 0.1);
		values = new double[n];
		for (int i = 0; i < n; ++i)
			values[i] = 0.5 + 0.5 * Math.cos(2.0 * Math.PI * ((double)i) / ((double)(2 * n)));    
	}

	public double[] getValues() { return values; }
	
}
