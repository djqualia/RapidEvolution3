package com.mixshare.rapid_evolution.workflow;

public interface TaskProgressListener {

	/**
	 * @param percentDone between 0.0 and 1.0
	 */
	public void setProgress(float percentDone);
	
	public void isComplete();
	
}
