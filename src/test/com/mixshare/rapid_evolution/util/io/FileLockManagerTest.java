package test.com.mixshare.rapid_evolution.util.io;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.util.io.FileLockManager;

public class FileLockManagerTest extends RE3TestCase {
	
    static private Logger log = Logger.getLogger(FileLockManagerTest.class);    
	
	public void testFileLockManager() {
		try {			
			FileLockManager.startFileRead("test");
			FileLockManager.startFileRead("test");
			FileLockManager.endFileRead("test");
			FileLockManager.endFileRead("test");	
			if (FileLockManager.getSemMapSize() != 0)
				fail("sem not cleared");
			
			tryFinallyLock();
			if (FileLockManager.getSemMapSize() != 0)
				fail("sem not cleared");
			
		} catch (Exception e) {
			log.error("testFileLockManager(): error", e);
			fail(e.getMessage());
		}
	}
	
	public void tryFinallyLock() {
		try {			
			FileLockManager.startFileRead("test");
			return;
		} catch (Exception e) {
		} finally {
			FileLockManager.endFileRead("test");
		}
	}	

}
