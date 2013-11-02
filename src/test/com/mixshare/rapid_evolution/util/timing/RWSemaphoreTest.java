package test.com.mixshare.rapid_evolution.util.timing;

import java.io.IOException;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.util.timing.RWSemaphore;

public class RWSemaphoreTest extends RE3TestCase {

	public void testBasicCounters() {
		try {
			RWSemaphore sem = new RWSemaphore(-1);
			if (sem.getReadLockCount() != 0)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 0)
				fail("incorrect write count");
			sem.startRead("test");
			if (sem.getReadLockCount() != 1)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 0)
				fail("incorrect write count");
			sem.startRead("test");
			if (sem.getReadLockCount() != 2)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 0)
				fail("incorrect write count");
			sem.endRead();
			if (sem.getReadLockCount() != 1)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 0)
				fail("incorrect write count");
			sem.endRead();
			if (sem.getReadLockCount() != 0)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 0)
				fail("incorrect write count");
			
			sem.startWrite("test");
			if (sem.getReadLockCount() != 0)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 1)
				fail("incorrect write count");
			sem.startWrite("test");
			if (sem.getReadLockCount() != 0)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 2)
				fail("incorrect write count");
			sem.endWrite();
			if (sem.getReadLockCount() != 0)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 1)
				fail("incorrect write count");
			sem.endWrite();
			if (sem.getReadLockCount() != 0)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 0)
				fail("incorrect write count");
			
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	public void testWriteWaitScenario() {
		try {
			RWSemaphore sem = new RWSemaphore(-1);
			if (sem.getReadLockCount() != 0)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 0)
				fail("incorrect write count");
			
			sem.startRead("test");
			if (sem.getReadLockCount() != 1)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 0)
				fail("incorrect write count");
			
			sem.startRead("test");
			if (sem.getReadLockCount() != 2)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 0)
				fail("incorrect write count");
			
			sem.endRead();
			if (sem.getReadLockCount() != 1)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 0)
				fail("incorrect write count");
			
			new SemLocker(sem).start();
			Thread.sleep(1000);
			
			if (sem.getReadLockCount() != 1)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 1)
				fail("incorrect write count, is=" + sem.getWriteHoldCount());
			
			Thread.sleep(3000);
			
			if (sem.getReadLockCount() != 1)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 1)
				fail("incorrect write count, is=" + sem.getWriteHoldCount());

			sem.endRead();			
			if (sem.getReadLockCount() != 0)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 1)
				fail("incorrect write count");
			
			Thread.sleep(4000);
			if (sem.getReadLockCount() != 0)
				fail("incorrect read count");
			if (sem.getWriteHoldCount() != 0)
				fail("incorrect write count");
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}

	private class SemLocker extends Thread {
		RWSemaphore sem;
		public SemLocker(RWSemaphore sem) {
			this.sem = sem;
		}
		public void run() {
			try {
				sem.startWrite("test");
				Thread.sleep(2000);
				sem.endWrite();
			} catch (Exception e) { }
		}
	}
	
}
