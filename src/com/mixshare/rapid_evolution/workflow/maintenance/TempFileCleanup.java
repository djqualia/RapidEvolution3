package com.mixshare.rapid_evolution.workflow.maintenance;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;

public class TempFileCleanup extends CommonTask {

	static private Logger log = Logger.getLogger(TempFileCleanup.class);
    static private final long serialVersionUID = 0L;
	    
	public String toString() {
		return "Temporary file cleanup";
	}
    
	public void execute() {
		try {
			log.debug("execute(): starting...");
			
			// clean up temp dir
	        File dir = new File(OSHelper.getWorkingDirectory() + "/temp/");
	        File[] tempFiles = dir.listFiles();
	        if (tempFiles != null)
	        	for (File tempFile : tempFiles)
	        		tempFile.delete();	        

			log.debug("execute(): finished...");
			
			SandmanThread.putBackgroundTaskToSleep(this, 1000*60*5);
		} catch (Exception e) {
			log.error("execute(): error", e);			
		}
	}
	
	public Object getResult() { return null; }
	
	public int getTaskPriority() {
		return RE3Properties.getInt("default_task_priority") + 5;
	}
	
	public boolean isIndefiniteTask() { return true; }
	
}
