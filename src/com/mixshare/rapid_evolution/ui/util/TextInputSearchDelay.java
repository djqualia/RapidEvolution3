package com.mixshare.rapid_evolution.ui.util;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

/**
 * On large collections, updating the filter after every letter has changed can cause lags.  This class
 * delays the invalidation of the filter to an interval after the user stops typing...
 */
abstract public class TextInputSearchDelay implements Runnable {

	static private Logger log = Logger.getLogger(TextInputSearchDelay.class);
	
	static public long DELAY_MILLISECONDS = RE3Properties.getLong("text_changed_search_delay");
	static private long SLEEP_INTERVAL = RE3Properties.getLong("text_changed_search_delay") / 20;
	
	///////////////////
	// STATIC FIELDS //
	///////////////////
	
	private long lastChanged;	
	private boolean running = false;
	
	private Semaphore runningSem = new Semaphore(1);
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public void invokeFilter();
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setLastChanged(long lastChanged) { this.lastChanged = lastChanged; } 
	
	/////////////
	// METHODS //
	/////////////
	
	public void searchTextChanged(String text) {
		try {
			runningSem.acquire();
			if (!running) {
				//instance = getSearchDelayInstance();
				setLastChanged(System.currentTimeMillis());			
				new Thread(this).start();			
				running = true;
			} else {
				setLastChanged(System.currentTimeMillis());
			}
		} catch (Exception e) { } finally {
			runningSem.release();
		}
	}
	
	public void run() {
		try {			
			while ((System.currentTimeMillis() - lastChanged) < DELAY_MILLISECONDS)
				Thread.sleep(SLEEP_INTERVAL);
			invokeFilter();			
		} catch (Exception e) {
			log.error("run(): error", e);
		}
		running = false;
	}
	
}
