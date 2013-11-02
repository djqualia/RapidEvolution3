package com.mixshare.rapid_evolution.workflow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;

public class SandmanThread extends Thread {

	static private Logger log = Logger.getLogger(SandmanThread.class);
	
	static public long SANDMAN_SLEEP_INTERVAL = RE3Properties.getLong("sandman_sleep_interval_seconds") * 1000;
	
	static public Map<Task, Long> sleepingBackgroundTasks = new LinkedHashMap<Task, Long>();
	static public RWSemaphore backgroundSem = new RWSemaphore(-1);
	
	static public Map<Task, Long> sleepingForegroundTasks = new LinkedHashMap<Task, Long>();
	static public RWSemaphore foregroundSem = new RWSemaphore(-1);
	
	static public int getNumSleepingBackgroundTasks() { return sleepingBackgroundTasks.size(); }
	static public int getNumSleepingForegroundTasks() { return sleepingForegroundTasks.size(); }
	
	static public void putBackgroundTaskToSleep(Task task) { putBackgroundTaskToSleep(task, RE3Properties.getInt("mining_task_no_work_delay_seconds") * 1000); }
	static public void putBackgroundTaskToSleep(Task task, long duration) {
		try {
			backgroundSem.startRead("putTaskToSleep");
			sleepingBackgroundTasks.put(task, System.currentTimeMillis() + duration);
		} catch (Exception e) {
			log.error("putTaskToSleep(): error", e);
		} finally {
			backgroundSem.endRead();
		}
	}
	static public void wakeUpAllBackgroundTasks() {
		try {
			backgroundSem.startWrite("wakeUpEverybody");
			for (Task task : sleepingBackgroundTasks.keySet())
				TaskManager.runBackgroundTask(task);
			sleepingBackgroundTasks.clear();
		} catch (Exception e) {
			log.error("wakeUpEverybody(): error", e);
		} finally {
			backgroundSem.endWrite();
		}
		
	}
	static public void wakeUpBackgroundTasks(Vector<Task> tasks) {
		try {
			backgroundSem.startWrite("wakeUpTasks");
			for (Task task : tasks) {
				TaskManager.runBackgroundTask(task);
				sleepingBackgroundTasks.remove(task);
			}
		} catch (Exception e) {
			log.error("wakeUpTasks(): error", e);
		} finally {
			backgroundSem.endWrite();
		}
	}

	static public void putForegroundTaskToSleep(Task task) { putForegroundTaskToSleep(task, RE3Properties.getInt("mining_task_no_work_delay_seconds") * 1000); }
	static public void putForegroundTaskToSleep(Task task, long duration) {
		try {
			foregroundSem.startRead("putTaskToSleep");
			sleepingForegroundTasks.put(task, System.currentTimeMillis() + duration);
		} catch (Exception e) {
			log.error("putTaskToSleep(): error", e);
		} finally {
			foregroundSem.endRead();
		}
	}
	static public void wakeUpAllForegroundTasks() {
		try {
			foregroundSem.startWrite("wakeUpEverybody");
			for (Task task : sleepingForegroundTasks.keySet())
				TaskManager.runForegroundTask(task);
			sleepingForegroundTasks.clear();
		} catch (Exception e) {
			log.error("wakeUpEverybody(): error", e);
		} finally {
			foregroundSem.endWrite();
		}
		
	}
	static public void wakeUpForegroundTasks(Vector<Task> tasks) {
		try {
			foregroundSem.startWrite("wakeUpTasks");
			for (Task task : tasks) {
				TaskManager.runForegroundTask(task);
				sleepingForegroundTasks.remove(task);
			}
		} catch (Exception e) {
			log.error("wakeUpTasks(): error", e);
		} finally {
			foregroundSem.endWrite();
		}
	}
	
	
	public SandmanThread() {
		setDaemon(true);
		setPriority(Thread.NORM_PRIORITY - 1);
	}
	
	public void run() {
		while (!RapidEvolution3.isTerminated) {
			try {
				Thread.sleep(SANDMAN_SLEEP_INTERVAL);
				// foreground
				Vector<Task> removedForegroundTasks = new Vector<Task>();
				try {
					foregroundSem.startWrite("run");
					for (Entry<Task, Long> entry : sleepingForegroundTasks.entrySet())
						if (System.currentTimeMillis() > entry.getValue())
							removedForegroundTasks.add(entry.getKey());
				} catch (Exception e) {	} finally {
					foregroundSem.endWrite();
				}
				if (removedForegroundTasks.size() > 0)
					wakeUpForegroundTasks(removedForegroundTasks);
				// background
				Vector<Task> removedBackgroundTasks = new Vector<Task>();							
				try {
					backgroundSem.startWrite("run");
					for (Entry<Task, Long> entry : sleepingBackgroundTasks.entrySet())
						if (System.currentTimeMillis() > entry.getValue())
							removedBackgroundTasks.add(entry.getKey());
				} catch (Exception e) { } finally {
					backgroundSem.endWrite();
				}
				if (removedBackgroundTasks.size() > 0)
					wakeUpBackgroundTasks(removedBackgroundTasks);
			} catch (OutOfMemoryError e) {
				log.error("run(): sandman cannot continue, out of memory");
				if (RapidEvolution3UI.instance != null)
					RapidEvolution3UI.instance.notifyOutOfMemory("the sandman task was executing");							
			} catch (Error e) {
				log.error("run(): error", e);			
			} catch (Exception e) {
				log.error("run(): error", e);
			}
		}
	}
	
	
	
}
