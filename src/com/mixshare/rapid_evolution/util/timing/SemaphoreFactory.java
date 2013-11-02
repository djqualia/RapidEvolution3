package com.mixshare.rapid_evolution.util.timing;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.mixshare.rapid_evolution.RE3Properties;

public class SemaphoreFactory {

	static public long DEFAULT_TIMEOUT = RE3Properties.getLong("semaphore_factory_default_timeout_millis");
	
	private Map<Integer, Semaphore> semMap = new HashMap<Integer, Semaphore>();		
	private Semaphore internalSem = new Semaphore(1, true);
	
	public void acquire(int key) throws InterruptedException {
		try {
			internalSem.tryAcquire(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
			Semaphore sem = getSem(key);
			sem.acquire();
		} catch (Exception e) { } finally {
			internalSem.release();
		}
	}
	
	public void release(int key) {
		try {
			internalSem.tryAcquire(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
			Semaphore sem = getSem(key);
			sem.release();
			semMap.remove(sem);
		} catch (Exception e) { } finally {
			internalSem.release();
		}
	}
	
	private Semaphore getSem(int key) {
		Semaphore sem = semMap.get(key);
		if (sem == null) {
			sem = new Semaphore(1);
			semMap.put(key, sem);
		}
		return sem;
	}	
	
}
