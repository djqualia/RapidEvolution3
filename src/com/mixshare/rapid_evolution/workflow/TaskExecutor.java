package com.mixshare.rapid_evolution.workflow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;

public class TaskExecutor implements Runnable {

	static private Logger log = Logger.getLogger(TaskExecutor.class);
	
	static private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");	
	static private int BACKGROUND_POLL_INTERVAL = RE3Properties.getInt("background_task_executor_poll_interval");
	static private int FOREGROUND_POLL_INTERVAL = RE3Properties.getInt("foreground_task_executor_poll_interval");
	
	static public boolean STOP_FLAG = false;
	
	private PriorityBlockingQueue<Task> queue;
	private boolean background;
	private String lastStatus;
	private Task lastTask;
	private Date lastStatusDate;
	
	public TaskExecutor(PriorityBlockingQueue<Task> queue, boolean background) {	
		this.queue = queue;
		this.background = background;
	}
	
	public void run() {
		while (!STOP_FLAG) {
			try {
				Task task = (TaskManager.isPaused() && background) ? null : queue.poll();
				if (task != null) {
					if (task.isReady()) {
						lastStatus = task.toString();
						lastTask = task;
						lastStatusDate = new Date();
						task.run();
						lastStatus = null;
						lastTask = null;
					} else {
						if (log.isTraceEnabled())
							log.trace("TaskExecutor(): task not ready=" + task + ", delaying...");
						if (background)
							SandmanThread.putBackgroundTaskToSleep(task, RE3Properties.getLong("task_not_ready_sleep_delay_seconds") * 1000);
						else
							SandmanThread.putForegroundTaskToSleep(task, RE3Properties.getLong("task_not_ready_sleep_delay_seconds") * 1000);
						Thread.sleep(background ? BACKGROUND_POLL_INTERVAL : FOREGROUND_POLL_INTERVAL);
					}
				} else {
					Thread.sleep(background ? BACKGROUND_POLL_INTERVAL : FOREGROUND_POLL_INTERVAL);
				}
			} catch (OutOfMemoryError e) {
				log.error("run(): task executor cannot continue, out of memory");
				if (RapidEvolution3UI.instance != null) {
					if (lastStatus != null)
						RapidEvolution3UI.instance.notifyOutOfMemory("executing the task=" + lastStatus);
					else
						RapidEvolution3UI.instance.notifyOutOfMemory("executing " + (background ? "background" : "foreground") + " tasks");
				}
			} catch (Error e) {
				log.error("run(): error", e);
			} catch (Exception e) {
				log.error("run(): exception", e);
			}
		}
	}
	
	public String getLatestStatus() {
		if (lastStatus != null) {
			return lastStatus + " (" + timeFormat.format(lastStatusDate) + ")";
		}
		return "No activity";
	}
	
	public Task getLatestTask() {
		return lastTask;
	}
	
}
