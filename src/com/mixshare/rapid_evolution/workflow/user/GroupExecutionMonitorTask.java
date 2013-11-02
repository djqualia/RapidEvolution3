package com.mixshare.rapid_evolution.workflow.user;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.Task;
import com.trolltech.qt.gui.QApplication;

public class GroupExecutionMonitorTask extends CommonTask {

    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(GroupExecutionMonitorTask.class);	
	
    static private long SLEEP_INTERVAL = 1000;
    
	private Vector<Task> tasks;
	private String title;
	private boolean done = false;
	
	public GroupExecutionMonitorTask(Vector<Task> tasks, String title) {
		this.title = title;
		this.tasks = tasks;
	}
	
	public String toString() { return title; }
	
	public void execute() {
		try {
        	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));        	            	        	        				
			while (!done) {
				Thread.sleep(SLEEP_INTERVAL);
				float percentTasksComplete = 0.0f;
				for (Task task : tasks) {
					if (task.isDone())
						percentTasksComplete += 1.0f;
					else {
						if (!task.isIndefiniteTask())
							percentTasksComplete += task.getCurrentProgress();
					}
				}
				setProgress(percentTasksComplete / tasks.size());
				if ((int)percentTasksComplete >= tasks.size())
					done = true;
			}
		} catch (Exception e) {
			log.error("execute(): error", e);
		}		
	}
	
	public void cancel() {
		super.cancel();
		done = true;
		for (Task task : tasks)
			task.cancel();		
	}
	
}
