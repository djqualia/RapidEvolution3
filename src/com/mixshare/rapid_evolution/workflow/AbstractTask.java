package com.mixshare.rapid_evolution.workflow;

abstract public class AbstractTask implements Task {

	abstract public void execute();	
	abstract public Object getResult();
	abstract public int getTaskPriority();
	abstract public boolean isReady();	
	abstract public void cancel();
	abstract public boolean isCancellable();
	
	abstract public boolean isIndefiniteTask();
	
}
