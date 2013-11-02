package com.mixshare.rapid_evolution.ui.dialogs.taskprogress;

import com.mixshare.rapid_evolution.workflow.Task;

public class TaskProgressLauncher extends Thread {

	private Task task = null;
	
	public TaskProgressLauncher(Task task) {
		this.task = task;
	}
	
	public void run() {
		if (!task.isDone()) {
			TaskProgressDialog dialog = new TaskProgressDialog(task);
			if (!task.isDone())
				dialog.show();
		}
	}
	
}
