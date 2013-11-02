package com.mixshare.rapid_evolution.workflow;

import java.io.Serializable;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;

abstract public class CommonTask extends AbstractTask implements Runnable, Serializable, DataConstants, Comparable<CommonTask> {

	static private Logger log = Logger.getLogger(CommonTask.class);	
	static private final AtomicLong seq = new AtomicLong();	
		
	////////////
	// FIELDS //
	////////////
	
	private TaskResultListener resultListener;
	private boolean isDone = false;
	protected boolean isCancelled = false;
	private Vector<TaskProgressListener> listeners = new Vector<TaskProgressListener>(1);
	private float currentProgress = 0.0f;
	
	private final long seqNum;	
		
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public CommonTask() { 
		seqNum = seq.getAndIncrement();
	}
	public CommonTask(TaskResultListener resultListener) {
		seqNum = seq.getAndIncrement();
		this.resultListener = resultListener;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public boolean isDone() { return isDone; }
		
	public boolean isReady() { return true; }
	
	public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }
	
	public Object getResult() { return null; }
	
	public boolean isCancelled() { return isCancelled; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setTaskResultListener(TaskResultListener resultListener) {
		this.resultListener = resultListener;		
	}

	public void setCancelled(boolean isCancelled) { this.isCancelled = isCancelled; }

	/////////////
	// METHODS //
	/////////////
	
	public boolean equals(Object o) {
		if (o instanceof CommonTask) {
			CommonTask t = (CommonTask)o;
			return seqNum == t.seqNum;
		}
		return false;
	}
	
	public int hashCode() { return (int)seqNum; }
	
	public void cancel() {
		isCancelled = true;
		log.warn("cancel(): task cancelled=" + toString());
	}
	
	public void run() {
		try {
			isDone = false;
			execute();
		} catch (Exception e) {
			log.error("run(): error", e);
		} finally {
			isDone = true;
			for (TaskProgressListener listener : listeners)
				listener.isComplete();		
			listeners.clear();
		}
		if (log.isTraceEnabled())
			log.trace("run(): finished executing, resultListener=" + resultListener);
		if (resultListener != null)			
			resultListener.processResult(getResult());		
	}
	
	public int compareTo(CommonTask task) {
		if (getTaskPriority() > task.getTaskPriority())
			return -1;
		if (getTaskPriority() < task.getTaskPriority())
			return 1;
		// tie breaker is which task was created first
		if (seqNum < task.seqNum)
			return -1;
		if (seqNum > task.seqNum)
			return 1;		
		return 0;
	}
	
	public boolean isIndefiniteTask() {
		return false;
	}
	public boolean isCancellable() {
		return true;
	}
	
	public float getCurrentProgress() { return currentProgress; }
	public void setProgress(float percentDone) {
		currentProgress = percentDone;
		if (currentProgress > 1.0f)
			currentProgress = 1.0f;
		if (currentProgress < 0.0f)
			currentProgress = 0.0f;
		for (TaskProgressListener listener : listeners)
			listener.setProgress(percentDone);
	}
	
	public void addProgressListener(TaskProgressListener listener) {
		listeners.add(listener);
	}
	public void removeProgressListener(TaskProgressListener listener) {
		for (int i = 0; i < listeners.size(); ++i) {
			if (listeners.get(i) == listener) {
				listeners.remove(i);
				return;
			}
		}
	}
	
}
