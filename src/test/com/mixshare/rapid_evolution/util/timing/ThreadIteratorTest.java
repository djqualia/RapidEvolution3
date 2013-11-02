package test.com.mixshare.rapid_evolution.util.timing;

import java.util.Vector;

import test.RE3TestCase;

/**
 * This is just a class I used to experiment with concurrent modification exceptions...  do not include this in any test suites.
 */
public class ThreadIteratorTest extends RE3TestCase {
	
	public void testThreadIterator() {
		try {
			Vector<Integer> collection = new Vector<Integer>(1000);
			for (int i = 0; i < 1000; ++i)
				collection.add((int)(Math.random() * 1000));
			for (int i = 0; i < 5; ++i)
				new ThreadIterator(collection).start();
			while (true) {
				Thread.sleep(15000);
				//collection.add(2);
			}
		} catch (Exception e) {
			fail("testThreadIterator(): " + e);
		}
	}
	
	
	private class ThreadIterator extends Thread {
		private Vector<Integer> collection;
		public ThreadIterator(Vector<Integer> collection) { this.collection = collection; }
		public void run() {
			try {
				int step = 0;
				while (true) {
					for (Integer i : collection) {
						step = i;
					}
				}
			} catch (Exception e) {
				fail("run(): " + e);
			}
		}
	}
	
}
