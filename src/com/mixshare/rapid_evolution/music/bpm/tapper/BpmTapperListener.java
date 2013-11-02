package com.mixshare.rapid_evolution.music.bpm.tapper;

public interface BpmTapperListener {

	public void setBpm(double bpm);
	public void finalBpm(double bpm);
	public void resetBpm();
	
}
