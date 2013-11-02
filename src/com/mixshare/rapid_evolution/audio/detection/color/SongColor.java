package com.mixshare.rapid_evolution.audio.detection.color;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.music.bpm.Bpm;

public class SongColor {
	
	static public int SongColorVersion = 1;

	static private Logger log = Logger.getLogger(SongColor.class);
	
	////////////
	// FIELDS //
	////////////
	
	private int num_bands = 0;
	private int windowsize = 0; // size of analyzed window
	private float samplerate = 0.0f;
	private double tracktime = 0.0f; // length of song in seconds
	private double[] energy = null; // energy features:

	// surface features:	  
	private float[] avgcentroid = null; // average spectral brightness
	private float[] variancecentroid = null;
	private int[] avgrolloff = null; // average spectral shape
	private int[] variancerolloff = null;
	private float[] avgflux = null; // average spectral change from window to window
	private float[] varianceflux = null;
	private int[] avgzerocrossings = null; // average time domain 0 crossings per window (measure of noise)
	private int[] variancezerocrossing = null;	  
	private float[] lowenergy = null; // percentage of analyzed windows with energy less than the average window

	// rhythm features:	  
	private double period0 = 0.0; // primary detected bpm	  
	private double amplitude0 = 0.0f; // relative amplitude of primary bpm
	private double ratioperiod1 = 0.0f; // ratio of secondary bpm to primary
	private double amplitude1 = 0.0f; // relative amplitude of secondary bpm
	private double ratioperiod2 = 0.0f;
	private double ratioperiod3 = 0.0f;
	private double amplitude2 = 0.0f;
	private double amplitude3 = 0.0f;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////	
	
	public SongColor(int in_windowsize, float in_samplerate, double seconds, int bands) {
		windowsize = in_windowsize;
		samplerate = in_samplerate;
		tracktime = seconds;
		num_bands = bands;
		energy = new double[num_bands];
		avgcentroid = new float[num_bands];
		variancecentroid = new float[num_bands];
		avgrolloff = new int[num_bands];
		variancerolloff = new int[num_bands];
		avgflux = new float[num_bands];
		varianceflux = new float[num_bands];
		avgzerocrossings = new int[num_bands];
		variancezerocrossing = new int[num_bands];
		lowenergy = new float[num_bands];
		for (int i = 0; i < num_bands; ++i) {
			energy[i] = 0.0;
			avgcentroid[i] = 0.0f;
			variancecentroid[i] = 0.0f;
			avgrolloff[i] = 0;
			variancerolloff[i] = 0;
			avgflux[i] = 0.0f;
			varianceflux[i] = 0.0f;
			avgzerocrossings[i] = 0;
			variancezerocrossing[i] = 0;
			lowenergy[i] = 0.0f;
		}
	}

	/////////////
	// GETTERS //
	/////////////
	
	public int getNum_bands() {
		return num_bands;
	}

	public int getWindowsize() {
		return windowsize;
	}

	public float getSamplerate() {
		return samplerate;
	}

	public double getTracktime() {
		return tracktime;
	}

	public double[] getEnergy() {
		return energy;
	}

	public float[] getAvgcentroid() {
		return avgcentroid;
	}

	public float[] getVariancecentroid() {
		return variancecentroid;
	}

	public int[] getAvgrolloff() {
		return avgrolloff;
	}

	public int[] getVariancerolloff() {
		return variancerolloff;
	}

	public float[] getAvgflux() {
		return avgflux;
	}

	public float[] getVarianceflux() {
		return varianceflux;
	}

	public int[] getAvgzerocrossings() {
		return avgzerocrossings;
	}

	public int[] getVariancezerocrossing() {
		return variancezerocrossing;
	}

	public float[] getLowenergy() {
		return lowenergy;
	}

	public double getPeriod0() {
		return period0;
	}

	public double getAmplitude0() {
		return amplitude0;
	}

	public double getRatioperiod1() {
		return ratioperiod1;
	}

	public double getAmplitude1() {
		return amplitude1;
	}

	public double getRatioperiod2() {
		return ratioperiod2;
	}

	public double getRatioperiod3() {
		return ratioperiod3;
	}

	public double getAmplitude2() {
		return amplitude2;
	}

	public double getAmplitude3() {
		return amplitude3;
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setNum_bands(int num_bands) {
		this.num_bands = num_bands;
	}

	public void setWindowsize(int windowsize) {
		this.windowsize = windowsize;
	}

	public void setSamplerate(float samplerate) {
		this.samplerate = samplerate;
	}

	public void setTracktime(double tracktime) {
		this.tracktime = tracktime;
	}

	public void setEnergy(double[] energy) {
		this.energy = energy;
	}

	public void setAvgcentroid(float[] avgcentroid) {
		this.avgcentroid = avgcentroid;
	}

	public void setVariancecentroid(float[] variancecentroid) {
		this.variancecentroid = variancecentroid;
	}

	public void setAvgrolloff(int[] avgrolloff) {
		this.avgrolloff = avgrolloff;
	}

	public void setVariancerolloff(int[] variancerolloff) {
		this.variancerolloff = variancerolloff;
	}

	public void setAvgflux(float[] avgflux) {
		this.avgflux = avgflux;
	}

	public void setVarianceflux(float[] varianceflux) {
		this.varianceflux = varianceflux;
	}

	public void setAvgzerocrossings(int[] avgzerocrossings) {
		this.avgzerocrossings = avgzerocrossings;
	}

	public void setVariancezerocrossing(int[] variancezerocrossing) {
		this.variancezerocrossing = variancezerocrossing;
	}

	public void setLowenergy(float[] lowenergy) {
		this.lowenergy = lowenergy;
	}

	public void setPeriod0(double period0) {
		this.period0 = period0;
	}

	public void setAmplitude0(double amplitude0) {
		this.amplitude0 = amplitude0;
	}

	public void setRatioperiod1(double ratioperiod1) {
		this.ratioperiod1 = ratioperiod1;
	}

	public void setAmplitude1(double amplitude1) {
		this.amplitude1 = amplitude1;
	}

	public void setRatioperiod2(double ratioperiod2) {
		this.ratioperiod2 = ratioperiod2;
	}

	public void setRatioperiod3(double ratioperiod3) {
		this.ratioperiod3 = ratioperiod3;
	}

	public void setAmplitude2(double amplitude2) {
		this.amplitude2 = amplitude2;
	}

	public void setAmplitude3(double amplitude3) {
		this.amplitude3 = amplitude3;
	}
	
	/////////////
	// METHODS //
	/////////////

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("window size: ");
		result.append(String.valueOf(windowsize));
		result.append(", samplerate: ");
		result.append(String.valueOf(samplerate));
		result.append(", track time: ");
		result.append(String.valueOf(tracktime));
		result.append(" seconds\n");
		for (int i = 0; i < num_bands; ++i) {
			if (i == 0)
				result.append("lowpass (<200hz):\n");
			else if (i == 1)
				result.append("band1 (200-400hz):\n");
			else if (i == 2)
				result.append("band2 (400-800hz):\n");
			else if (i == 3)
				result.append("band3 (800-1600hz):\n");
			else if (i == 4)
				result.append("band4 (1600-3200hz):\n");
			else if (i == 5)
				result.append("highpass (>3200hz):\n");

			result.append("--> energy: ");
			result.append(String.valueOf(energy[i]));
			result.append("%\n");

			result.append("--> spectral brightness (mean-centroid): ");
			result.append(String.valueOf(avgcentroid[i]));
			result.append(", variance: ");
			result.append(String.valueOf(variancecentroid[i]));
			result.append("\n");
			
			result.append("--> spectral shape (mean-rolloff): ");
			result.append(String.valueOf(avgrolloff[i]));
			result.append(", variance: ");
			result.append(String.valueOf(variancerolloff[i]));
			result.append("\n");
			
			result.append("--> spectral change (mean-flux): ");
			result.append(String.valueOf(avgflux[i]));
			result.append(", variance: ");
			result.append(String.valueOf(varianceflux[i]));
			result.append("\n");
			
			result.append("--> spectral noise (mean-zerocrossings): ");
			result.append(String.valueOf(avgzerocrossings[i]));
			result.append(", variance: ");
			result.append(String.valueOf(variancezerocrossing[i]));
			result.append("\n");
			
			result.append("--> spectral noise (mean-zerocrossings): ");
			result.append(String.valueOf(avgzerocrossings[i]));
			result.append(", variance: ");
			result.append(String.valueOf(variancezerocrossing[i]));
			result.append("\n");
			
			result.append("--> low energy percentage: ");
			result.append(String.valueOf(lowenergy[i]));
			result.append("%\n");			
		}
		result.append("period0 (primary bpm): ");
		result.append(String.valueOf(period0));
		result.append(", amplitude0: ");
		result.append(String.valueOf(amplitude0));
		result.append("%\n");
		
		result.append("ratio period1: ");
		result.append(String.valueOf(ratioperiod1));
		result.append(", amplitude1: ");
		result.append(String.valueOf(amplitude1));
		result.append("%\n");

		result.append("ratio period2: ");
		result.append(String.valueOf(ratioperiod2));
		result.append(", amplitude2: ");
		result.append(String.valueOf(amplitude2));
		result.append("%\n");

		result.append("ratio period3: ");
		result.append(String.valueOf(ratioperiod3));
		result.append(", amplitude3: ");
		result.append(String.valueOf(amplitude3));
		result.append("%");
				
		return result.toString();
	}

	double getColorSimilarity(SongColor color) {
		double similarity = 1.0;

		// track length similarity
		double maxtime = tracktime;
		if (color.tracktime > maxtime) maxtime = color.tracktime;
		double timediff = Math.abs(tracktime - color.tracktime);
		similarity *= (maxtime - timediff) / maxtime;

		for (int i = 0; i < num_bands; ++i) {
			float maxcentroid = avgcentroid[i];
			if (color.avgcentroid[i] > maxcentroid) maxcentroid = color.avgcentroid[i];
			float centroiddiff = Math.abs(avgcentroid[i] - color.avgcentroid[i]);
			similarity *= (maxcentroid - centroiddiff) / maxcentroid;

			maxcentroid = variancecentroid[i];
			if (color.variancecentroid[i] > maxcentroid) maxcentroid = color.variancecentroid[i];
			centroiddiff = Math.abs(variancecentroid[i] - color.variancecentroid[i]);
			similarity *= (maxcentroid - centroiddiff) / maxcentroid;

			int maxrolloff = avgrolloff[i];
			if (color.avgrolloff[i] > maxrolloff) maxrolloff = color.avgrolloff[i];
			int rolldiff = Math.abs(avgrolloff[i] - color.avgrolloff[i]);
			similarity *= ((double)(maxrolloff - rolldiff)) / ((double)maxrolloff);

			maxrolloff = variancerolloff[i];
			if (color.variancerolloff[i] > maxrolloff) maxrolloff = color.variancerolloff[i];
			rolldiff = Math.abs(variancerolloff[i] - color.variancerolloff[i]);
			similarity *= ((double)(maxrolloff - rolldiff)) / ((double)maxrolloff);

			float maxflux = avgflux[i];
			if (color.avgflux[i] > maxflux) maxflux = color.avgflux[i];
			float fluxdiff = Math.abs(avgflux[i] - color.avgflux[i]);
			similarity *= (maxflux - fluxdiff) / maxflux;

			maxflux = varianceflux[i];
			if (color.varianceflux[i] > maxflux) maxflux = color.varianceflux[i];
			fluxdiff = Math.abs(varianceflux[i] - color.varianceflux[i]);
			similarity *= (maxflux - fluxdiff) / maxflux;

			int maxzerocrossings = avgzerocrossings[i];
			if (color.avgzerocrossings[i] > maxzerocrossings) maxzerocrossings = color.avgzerocrossings[i];
			int zerodiff = Math.abs(avgzerocrossings[i] - color.avgzerocrossings[i]);
			similarity *= ((double)(maxzerocrossings - zerodiff)) / ((double)maxzerocrossings);

			maxzerocrossings = variancezerocrossing[i];
			if (color.variancezerocrossing[i] > maxzerocrossings) maxzerocrossings = color.variancezerocrossing[i];
			zerodiff = Math.abs(variancezerocrossing[i] - color.variancezerocrossing[i]);
			similarity *= ((double)(maxzerocrossings - zerodiff)) / ((double)maxzerocrossings);

			float maxlowenergy = lowenergy[i];
			if (color.lowenergy[i] > maxlowenergy) maxlowenergy = color.lowenergy[i];
			float lowdiff = Math.abs(lowenergy[i] - color.lowenergy[i]);
			similarity *= (maxlowenergy - lowdiff) / maxlowenergy;
		}

		similarity *= (1.0 - Math.abs(Bpm.getBpmDifference(period0, color.period0)) / 100.0f);

		double maxamp0 = amplitude0;
		if (color.amplitude0 > maxamp0) maxamp0 = color.amplitude0;
		double ampdiff0 = Math.abs(amplitude0 - color.amplitude0);
		similarity *= (maxamp0 - ampdiff0) / maxamp0;
		
		double maxamp1 = amplitude1;
		if (color.amplitude1 > maxamp1) maxamp1 = color.amplitude1;
		double ampdiff1 = Math.abs(amplitude1 - color.amplitude1);
		similarity *= (maxamp1 - ampdiff1) / maxamp1;

		double maxamp2 = amplitude2;
		if (color.amplitude2 > maxamp2) maxamp2 = color.amplitude2;
		double ampdiff2 = Math.abs(amplitude2 - color.amplitude2);
		similarity *= (maxamp2 - ampdiff2) / maxamp2;

		double maxamp3 = amplitude3;
		if (color.amplitude3 > maxamp3) maxamp3 = color.amplitude3;
		double ampdiff3 = Math.abs(amplitude3 - color.amplitude3);
		similarity *= (maxamp3 - ampdiff3) / maxamp3;

		try {
			double maxratio1 = ratioperiod1;
			if (color.ratioperiod1 > maxratio1) maxratio1 = color.ratioperiod1;
			double ratiodiff1 = Math.abs(ratioperiod1 - color.ratioperiod1);
			similarity *= (maxratio1 - ratiodiff1) / maxratio1;

			double maxratio2 = ratioperiod2;
			if (color.ratioperiod2 > maxratio2) maxratio2 = color.ratioperiod2;
			double ratiodiff2 = Math.abs(ratioperiod2 - color.ratioperiod2);
			similarity *= (maxratio2 - ratiodiff2) / maxratio2;

			double maxratio3 = ratioperiod3;
			if (color.ratioperiod3 > maxratio3) maxratio3 = color.ratioperiod3;
			double ratiodiff3 = Math.abs(ratioperiod3 - color.ratioperiod3);
			similarity *= (maxratio3 - ratiodiff3) / maxratio3;
		} catch (Exception e) {
			log.error("getColorSimilarity(): error", e);
		}
		
		return similarity;
	}
	
}
