package com.mixshare.rapid_evolution.workflow;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;

public class TaskManager {
	
	static private Logger log = Logger.getLogger(TaskManager.class);

	static private long THREAD_WAIT_SLEEP_INTERVAL = 500;
	
	static private int numActiveProcessors = Runtime.getRuntime().availableProcessors(); // TODO: this value should be polled apparently		
	
	static private PriorityBlockingQueue<Task> backgroundQueue = new PriorityBlockingQueue<Task>();
	static private PriorityBlockingQueue<Task> foregroundQueue = new PriorityBlockingQueue<Task>();
	static private ThreadGroup backgroundGroup = new ThreadGroup("Background Tasks");
	static private ThreadGroup foregroundGroup = new ThreadGroup("Foreground Tasks");	
	static private Vector<TaskExecutor> backgroundExecutors = new Vector<TaskExecutor>();
	static private Vector<TaskExecutor> foregroundExecutors = new Vector<TaskExecutor>();
	
	//static private ExecutorService backgroundThreadPool = Executors.newFixedThreadPool(Math.min(Math.max(numActiveProcessors, 2), RE3Properties.getInt("max_background_tasks")), new ThreadFactory() {
	static private ExecutorService backgroundThreadPool = Executors.newFixedThreadPool(RE3Properties.getInt("max_background_tasks"), new ThreadFactory() {
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(backgroundGroup, runnable);			
			thread.setDaemon(true);
			return thread;			
		}
	});	
	//static private ExecutorService foregroundThreadPool = Executors.newFixedThreadPool(Math.min(Math.max(numActiveProcessors, 2), RE3Properties.getInt("max_foreground_tasks")), new ThreadFactory() {
	static private ExecutorService foregroundThreadPool = Executors.newFixedThreadPool(RE3Properties.getInt("max_foreground_tasks"), new ThreadFactory() {
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(foregroundGroup, runnable);			
			thread.setDaemon(true);
			return thread;
		}
	});	
	
	static {
		backgroundGroup.setMaxPriority(Thread.NORM_PRIORITY - 1);		
		foregroundGroup.setMaxPriority(Thread.MAX_PRIORITY);
		if (!RE3Properties.getBoolean("junit_test_mode")) {
			if (log.isDebugEnabled())
				log.debug("TaskManager(): starting thread executors, # active processors=" + numActiveProcessors);
			for (int i = 0; i < Math.min(Math.max(numActiveProcessors, 2), RE3Properties.getInt("max_background_tasks")); ++i) {
				TaskExecutor backgroundTaskExecutor = new TaskExecutor(backgroundQueue, true);
				backgroundThreadPool.execute(backgroundTaskExecutor);
				backgroundExecutors.add(backgroundTaskExecutor);
			}
			for (int i = 0; i < Math.min(Math.max(numActiveProcessors, 2), RE3Properties.getInt("max_foreground_tasks")); ++i) {
				TaskExecutor foregroundTaskExecutor = new TaskExecutor(foregroundQueue, false);
				foregroundThreadPool.execute(foregroundTaskExecutor);
				foregroundExecutors.add(foregroundTaskExecutor);
			}
		}
	}
	
	static public void runBackgroundTask(Task task) {
		if (log.isTraceEnabled())
			log.trace("runBackgroundTask(): running task=" + task.toString() + ", priority=" + task.getTaskPriority());
		backgroundQueue.put(task);
	}
	static public void removeBackgroundTask(Task task) {
		backgroundQueue.remove(task);
	}

	static public void runForegroundTask(Task task) {
		if (log.isTraceEnabled())
			log.trace("runForegroundTask(): running task=" + task.toString() + ", priority=" + task.getTaskPriority());
		foregroundQueue.put(task);
	}
	
	static public boolean isPaused() { return RE3Properties.getBoolean("pause_background_tasks"); }
	static public void setPaused(boolean value) {
		if (value)
			RE3Properties.setProperty("pause_background_tasks", "true");
		else
			RE3Properties.setProperty("pause_background_tasks", "false");
	}
	
	static public int getNumQueuedForegroundTasks() {
		return foregroundQueue.size();
	}
	static public int getNumQueuedBackgroundTasks() {
		return backgroundQueue.size();
	}
	static public Vector<String> getCurrentForegroundStatuses() {
		Vector<String> result = new Vector<String>();
		for (TaskExecutor executor : foregroundExecutors)
			result.add(executor.getLatestStatus());
		return result;
	}
	static public Task getCurrentForegroundTask(int slot) {
		return foregroundExecutors.get(slot).getLatestTask();
	}
	static public Vector<String> getCurrentBackgroundStatuses() {
		Vector<String> result = new Vector<String>();
		for (TaskExecutor executor : backgroundExecutors)
			result.add(executor.getLatestStatus());
		return result;
	}
	static public Task getCurrentBackgroundTask(int slot) {
		return backgroundExecutors.get(slot).getLatestTask();
	}
	static public Vector<Task> getCurrentBackgroundTasks() {
		Vector<Task> result = new Vector<Task>();
		for (int i = 0; i < backgroundExecutors.size(); ++i) {
			Task task = backgroundExecutors.get(i).getLatestTask();
			if (task != null)
				result.add(task);
		}
		return result;
	}
	
	static public int getNumBackgroundExecutors() { 
		return backgroundExecutors.size();
	}
	static public int getNumForegroundExecutors() { 
		return foregroundExecutors.size();
	}
	
	static public void shutdown() {
		try {
			RapidEvolution3.isTerminated = true;
			if (RE3Properties.getBoolean("pause_background_tasks"))
				backgroundQueue.clear(); // if we don't clear background when background tasks are paused, shutting down will never clear the queue...
			for (Task task : getCurrentBackgroundTasks())
				task.cancel();
			long startTime = System.currentTimeMillis();
			if (log.isDebugEnabled())
				log.debug("shutdown(): waiting for tasks to clear, timeout=" + RE3Properties.getInt("shutdown_timeout_seconds") + "s");
			while (((System.currentTimeMillis() - startTime) < RE3Properties.getInt("shutdown_timeout_seconds") * 1000) && ((getNumQueuedForegroundTasks() > 0) || (getNumQueuedBackgroundTasks() > 0)))
				Thread.sleep(1000);
			if (log.isDebugEnabled())
				log.debug("shutdown(): shutting down thread pool");
			foregroundThreadPool.shutdown();			
			backgroundThreadPool.shutdown();
			if (log.isDebugEnabled())
				log.debug("shutdown(): clearing queues");
			backgroundQueue.clear();
			foregroundQueue.clear();
			TaskExecutor.STOP_FLAG = true;
			if (log.isDebugEnabled())
				log.debug("shutdown(): awaiting foreground termination");
			foregroundThreadPool.awaitTermination(RE3Properties.getInt("shutdown_timeout_seconds"), TimeUnit.SECONDS);
			if (log.isDebugEnabled())
				log.debug("shutdown(): awaiting background termination");
			backgroundThreadPool.awaitTermination(RE3Properties.getInt("shutdown_timeout_seconds"), TimeUnit.SECONDS);
			if (log.isDebugEnabled())
				log.debug("shutdown(): done shutting down task manager");
		} catch (Exception e) {
			log.error("shutdown(): error", e);
		}
	}	
	
}
