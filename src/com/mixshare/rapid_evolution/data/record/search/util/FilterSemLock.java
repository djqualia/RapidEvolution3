package com.mixshare.rapid_evolution.data.record.search.util;

import java.util.HashMap;
import java.util.Map;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;
import com.mixshare.rapid_evolution.util.timing.Semaphore;
import com.mixshare.rapid_evolution.util.timing.SemaphoreFactory;

public class FilterSemLock {

	private Map<Integer, RWSemaphore> semMap = new HashMap<Integer, RWSemaphore>();		
	private Semaphore internalSem = new Semaphore(1);
	
	public void startRead(byte indexType, int uniqueId) throws InterruptedException {
		int key = getKey(indexType, uniqueId);
		RWSemaphore sem = getSem(key);
		sem.startRead("FilterSemLock.startRead");
	}
	
	public void endRead(byte indexType, int uniqueId) {
		int key = getKey(indexType, uniqueId);
		RWSemaphore sem = getSem(key);
		sem.endRead();
		try {
			internalSem.tryAcquire("FilterSemLock.endRead", SemaphoreFactory.DEFAULT_TIMEOUT);		
			if (!sem.isUsed())
				semMap.remove(sem);
		} catch (Exception e) { } finally {
			internalSem.release();
		}
	}
	
	public void startWrite(byte indexType, int uniqueId) throws InterruptedException {
		int key = getKey(indexType, uniqueId);
		RWSemaphore sem = getSem(key);
		sem.startWrite("FilterSemLock.startWrite");
	}
	
	public void endWrite(byte indexType, int uniqueId) {
		int key = getKey(indexType, uniqueId);
		RWSemaphore sem = getSem(key);
		sem.endWrite();
		try {
			internalSem.tryAcquire("FilterSemLock.endWrite", SemaphoreFactory.DEFAULT_TIMEOUT);
			if (!sem.isUsed())
				semMap.remove(sem);
		} catch (Exception e) { } finally {
			internalSem.release();
		}
	}
	
	private int getKey(byte indexType, int uniqueId) {
		return (uniqueId << 4) + indexType;
	}
	
	private RWSemaphore getSem(int key) {
		RWSemaphore sem = null;
		try {
			internalSem.tryAcquire("FilterSemLock.getSem", SemaphoreFactory.DEFAULT_TIMEOUT);
			sem = semMap.get(key);
			if (sem == null) {
				sem = new RWSemaphore(RE3Properties.getLong("filter_sem_lock_timeout_millis"));
				semMap.put(key, sem);
			}
		} catch (Exception e) { } finally {
			internalSem.release();
		}
		return sem;
	}
	
}
