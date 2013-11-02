package com.mixshare.rapid_evolution.data.mined.echonest.song;

import java.io.Serializable;

import com.echonest.api.v4.Segment;
import com.mixshare.rapid_evolution.RE3Properties;

public class EchonestSongSegment extends EchonestTimedEvent implements Serializable {

    static private final long serialVersionUID = 0L;

    static public float[] TIMBRE_COEFF_MIN = new float[] { RE3Properties.getFloat("timbre_coeff_1_min"), RE3Properties.getFloat("timbre_coeff_2_min"), RE3Properties.getFloat("timbre_coeff_3_min"),
    	RE3Properties.getFloat("timbre_coeff_4_min"), RE3Properties.getFloat("timbre_coeff_5_min"), RE3Properties.getFloat("timbre_coeff_6_min"),
    	RE3Properties.getFloat("timbre_coeff_7_min"), RE3Properties.getFloat("timbre_coeff_8_min"), RE3Properties.getFloat("timbre_coeff_9_min"), 
    	RE3Properties.getFloat("timbre_coeff_10_min"), RE3Properties.getFloat("timbre_coeff_11_min"), RE3Properties.getFloat("timbre_coeff_12_min") };
    static public float[] TIMBRE_COEFF_MAX = new float[] { RE3Properties.getFloat("timbre_coeff_1_max"), RE3Properties.getFloat("timbre_coeff_2_max"), RE3Properties.getFloat("timbre_coeff_3_max"),
    	RE3Properties.getFloat("timbre_coeff_4_max"), RE3Properties.getFloat("timbre_coeff_5_max"), RE3Properties.getFloat("timbre_coeff_6_max"),
    	RE3Properties.getFloat("timbre_coeff_7_max"), RE3Properties.getFloat("timbre_coeff_8_max"), RE3Properties.getFloat("timbre_coeff_9_max"), 
    	RE3Properties.getFloat("timbre_coeff_10_max"), RE3Properties.getFloat("timbre_coeff_11_max"), RE3Properties.getFloat("timbre_coeff_12_max") };

    private double loudnessMax;
	private double loudnessMaxTime;
    private double loudnessStart;
    private double[] pitches;
    private double[] timbre;
    
    public EchonestSongSegment() { }
    public EchonestSongSegment(Segment segment) {
    	super(segment);    	
    	loudnessMax = segment.getLoudnessMax();
    	loudnessMaxTime = segment.getLoudnessMaxTime();
    	loudnessStart = segment.getLoudnessStart();
    	pitches = segment.getPitches();
    	timbre = segment.getTimbre();    	
    }
    
    public double getLoudnessMax() {
		return loudnessMax;
	}
	public void setLoudnessMax(double loudnessMax) {
		this.loudnessMax = loudnessMax;
	}
	public double getLoudnessMaxTime() {
		return loudnessMaxTime;
	}
	public void setLoudnessMaxTime(double loudnessMaxTime) {
		this.loudnessMaxTime = loudnessMaxTime;
	}
	public double getLoudnessStart() {
		return loudnessStart;
	}
	public void setLoudnessStart(double loudnessStart) {
		this.loudnessStart = loudnessStart;
	}
	public double[] getPitches() {
		return pitches;
	}
	public void setPitches(double[] pitches) {
		this.pitches = pitches;
	}
	public double[] getTimbre() {
		return timbre;
	}
	public void setTimbre(double[] timbre) {
		this.timbre = timbre;
	}
    
}
