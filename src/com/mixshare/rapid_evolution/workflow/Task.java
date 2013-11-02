package com.mixshare.rapid_evolution.workflow;

public interface Task {

	static public int DEFAULT_TASK_PRIORITY = 10;	
	
	public boolean isReady();
	public void run();
	public void execute();	
	public Object getResult();
	public int getTaskPriority();
	public void cancel();
	public boolean isCancelled();
	public boolean isDone();
	public boolean isCancellable();
	
	public void addProgressListener(TaskProgressListener listener);
	public void removeProgressListener(TaskProgressListener listener);
	public boolean isIndefiniteTask();
	public void setProgress(float progress);
	public float getCurrentProgress();
	
}
