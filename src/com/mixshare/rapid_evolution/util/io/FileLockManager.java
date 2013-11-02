package com.mixshare.rapid_evolution.util.io;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;

/**
 * This class can be used globally by the application to make sure only one thread is trying to write/modify a
 * file at a time.  Multiple readers are allowed, but readers are blocked if a writer is busy, and vice versa...
 */
public class FileLockManager {
	
	static private Logger log = Logger.getLogger(FileLockManager.class);
	
	static private Semaphore internalSem = new Semaphore(1, true);
	
	static public void startFileRead(String filename) {
		filename = FileUtil.unify(filename);
		if (filename.equals(""))
			return;
		if (log.isTraceEnabled())
			log.trace("startFileRead(): filename=" + filename);
		RWSemaphore sem = getRWSemaphore(filename);
		try { sem.startRead("startFileRead"); } catch (InterruptedException ie) { }		
		if (log.isTraceEnabled())
			log.trace("startFileRead(): \tcurrent # read locks=" + sem.getReadLockCount() + ", # write holds=" + sem.getWriteHoldCount());		
	}
	static public void endFileRead(String filename) {
		filename = FileUtil.unify(filename);
		if (filename.equals(""))
			return;
		if (log.isTraceEnabled())
			log.trace("endFileRead(): filename=" + filename);
		RWSemaphore sem = getRWSemaphore(filename); 		
		sem.endRead();
		try {
			internalSem.acquire();
			if (log.isTraceEnabled())
				log.trace("endFileRead(): \tcurrent # read locks=" + sem.getReadLockCount() + ", # write holds=" + sem.getWriteHoldCount());			
			if (!sem.isUsed())
				semMap.remove(filename);
		} catch (Exception e) { } finally {
			internalSem.release();
		}
	}
	
	static public void startFileWrite(String filename) {
		filename = FileUtil.unify(filename);
		if (filename.equals(""))
			return;
		if (log.isTraceEnabled())
			log.trace("startFileWrite(): filename=" + filename);
		RWSemaphore sem = getRWSemaphore(filename);
		// the following lines fixed a deadlock error, when tags were written to a song while it was being played
		// even though the RWSemaphore works fine in a test scenario, the call to startWrite was hanging even though
		// there was no readers/writers present (perhaps it was due to which threads called the read/write locks)
		// waiting for the readers to go away then re-acquiring the semaphore will fetch a fresh semaphore that won't
		// lock the thread... 
		if (sem.getReadLockCount() > 0) {
			while (sem.getReadLockCount() > 0)
				try { Thread.sleep(1); } catch (InterruptedException ie) { }
			sem = getRWSemaphore(filename);
		}
		try { sem.startWrite("startFileWrite"); } catch (InterruptedException ie) { }
		if (log.isTraceEnabled())
			log.trace("startFileWrite(): \tcurrent # read locks=" + sem.getReadLockCount() + ", # write holds=" + sem.getWriteHoldCount());		
	}
	static public void endFileWrite(String filename) {
		filename = FileUtil.unify(filename);
		if (filename.equals(""))
			return;
		if (log.isTraceEnabled())
			log.trace("endFileWrite(): filename=" + filename);
		RWSemaphore sem = getRWSemaphore(filename); 
		sem.endWrite();
		try {
			internalSem.acquire();
			if (log.isTraceEnabled())
				log.trace("endFileWrite(): \tcurrent # read locks=" + sem.getReadLockCount() + ", # write holds=" + sem.getWriteHoldCount());
			if (!sem.isUsed())
				semMap.remove(filename);
		} catch (Exception e) { } finally {
			internalSem.release();
		}
	}
	
	static public int getSemMapSize() { return semMap.size(); }

	static private Map<String, RWSemaphore> semMap = new HashMap<String, RWSemaphore>();
	static private RWSemaphore getRWSemaphore(String filename) {
		RWSemaphore result = null;
		try {
			internalSem.acquire();
			result = semMap.get(filename);
			if (result == null) {
				result = new RWSemaphore(-1);
				semMap.put(filename, result);
				if (log.isTraceEnabled())
					log.trace("getRWSemaphore(): creating new sem for filename=" + filename);
			}
		} catch (Exception e) { } finally {		
			internalSem.release();
		}
		return result;
	}

}
