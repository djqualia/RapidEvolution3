package com.mixshare.rapid_evolution.audio;

public class AudioBuffer {

	////////////
	// FIELDS //
	////////////
	
    private double[][] samples;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public AudioBuffer(int size, int num_channels) {
        samples = new double[num_channels][size];
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public int getNumChannels() { return samples.length; }    
    
    public double[] getSampleData(int channel) { return samples[channel]; }
    public double[][] getSampleData() { return samples; }
    
    public int getSize() { return samples[0].length; }    
    
    /////////////
    // SETTERS //    
    /////////////
    
    public void setSampleValue(int index, int channel, double value) { samples[channel][index] = value; }
    
}
