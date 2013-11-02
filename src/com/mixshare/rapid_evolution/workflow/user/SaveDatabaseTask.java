package com.mixshare.rapid_evolution.workflow.user;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;

public class SaveDatabaseTask extends CommonTask {
	
	static private Logger log = Logger.getLogger(SaveDatabaseTask.class);
    static private final long serialVersionUID = 0L;    	
    	
    private boolean putToSleepAfterwards = true;
    
	public SaveDatabaseTask() {	}
	public SaveDatabaseTask(boolean putToSleepAfterwards) {
		this.putToSleepAfterwards = putToSleepAfterwards;
	}
	
	public void execute() {
		RapidEvolution3.save();
		if (putToSleepAfterwards)
			SandmanThread.putBackgroundTaskToSleep(this, RE3Properties.getInt("autosave_interval_minutes") * 1000 * 60);
	}
	
	public int getTaskPriority() { return RE3Properties.getInt("autosave_task_priority"); }
	
	public Object getResult() { return null; }
	
	public String toString() { return "Saving database"; }
	
	public boolean isIndefiniteTask() { return true; }

}
