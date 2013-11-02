package test.com.mixshare.rapid_evolution.data.util.cache;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.util.cache.LRUCache;

public class LRUCacheTest extends RE3TestCase {

    public void testLRUCache() {
        try {
	        LRUCache cache = new LRUCache(2);
	        cache.add(new Integer(1), new Integer(1));
	        Thread.sleep(100);
	        cache.add(new Integer(2), new Integer(2));
	        Thread.sleep(100);
	        cache.add(new Integer(3), new Integer(3));
	        Thread.sleep(100);
	        assertTrue(((Integer)cache.get(new Integer(3))).intValue() == 3);
	        Thread.sleep(100);
	        assertTrue(((Integer)cache.get(new Integer(2))).intValue() == 2);
	        Thread.sleep(100);
	        assertTrue(cache.get(new Integer(1)) == null);
	        Thread.sleep(100);
	        cache.add(new Integer(1), new Integer(1));
	        Thread.sleep(100);
	        assertTrue(((Integer)cache.get(new Integer(1))).intValue() == 1);
	        Thread.sleep(100);
	        assertTrue(((Integer)cache.get(new Integer(2))).intValue() == 2);
	        Thread.sleep(100);
	        assertTrue(cache.get(new Integer(3)) == null);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
}
