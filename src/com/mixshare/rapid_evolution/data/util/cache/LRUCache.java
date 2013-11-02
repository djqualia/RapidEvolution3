package com.mixshare.rapid_evolution.data.util.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.util.timing.Semaphore;

/**
 * Text book least recently used cache
 */
public class LRUCache {

    private static Logger log = Logger.getLogger(LRUCache.class);
    
    ////////////
    // FIELDS //
    ////////////
    
    private int max_size;
    private Map<Object, Object> cache = null;
    private Map<Object, Long> cache_hits = null;
    private Object mostRecentlyRemoved = null;
    private Semaphore accessSem = new Semaphore(1);
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public LRUCache(int max_size) {
        this.max_size = max_size;
        cache = new HashMap<Object, Object>(max_size);
        cache_hits = new HashMap<Object, Long>(max_size);
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public Object get(Object key) {
        if (cache_hits.containsKey(key)) {
        	try {
        		accessSem.acquire("get");
        		cache_hits.put(key, System.currentTimeMillis());
        	} catch (Exception e) {
        		log.error("get(): error", e);
        	} finally {
        		accessSem.release();
        	}
        }
        return cache.get(key);
    }    
    
    public Long getTimestamp(Object key) {
    	return cache_hits.get(key);
    }
    
    public Object getFirstValueKeyStartsWith(String keyStartsWith) {
    	Object result = null;
    	try {
    		accessSem.acquire("getFirstValueKeyStartsWith");    	    	
	    	Iterator<Entry<Object,Object>> iter = cache.entrySet().iterator();
	    	while (iter.hasNext()) {
	    		Entry<Object,Object> entry = iter.next();
	    		String key = (String)entry.getKey();
	    		if (key.startsWith(keyStartsWith)) {
	    			result = entry.getValue();
	    			break;
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getFirstValueKeyStartsWith(): error", e);
    	} finally {
    		accessSem.release();
    	}
    	return result;
    }

    public Vector<Object> getAllValues() {
    	Vector<Object> result = new Vector<Object>(cache.size());
    	try {
    		accessSem.acquire("getAllValues");    	
    		Iterator<Object> iter = cache.values().iterator();
    		while (iter.hasNext())
    			result.add(iter.next());
    	} catch (Exception e) {
    		log.error("getAllValues(): error", e);
    	} finally {
    		accessSem.release();
    	}
    	return result;
    }    
    
    public Object getMostRecentlyRemoved() {
    	return mostRecentlyRemoved;
    }
    
    /////////////
    // SETTERS //
    /////////////
    
    public void setMaxSize(int size) {
    	max_size = size;
    	if (cache.size() == 0) {
    		cache = new HashMap<Object, Object>(max_size);
    		cache_hits = new HashMap<Object, Long>(max_size);
    	}
    }
    
    /////////////
    // METHODS //
    /////////////
    
    public void add(Object key, Object value) {
    	try {
    		accessSem.acquire("add");
    		cache.put(key, value);
    		cache_hits.put(key, System.currentTimeMillis());
    	} catch (Exception e) {
    		log.error("add(): error", e);
    	} finally {
    		accessSem.release();
    	}
        if (cache.size() > max_size) removeLeastHitCache();
    }
    
    public boolean containsKey(Object key) {
    	return cache.containsKey(key);
    }
        
    public void remove(Object key) {
    	try {
    		accessSem.acquire("remove");
    		cache_hits.remove(key);
    		cache.remove(key);
    	} catch (Exception e) {
    		log.error("remove(): error", e);
    	} finally {
    		accessSem.release();
    	}
    }
    
    public void removeAll() {
    	try {
    		accessSem.acquire("removeAll");
    		cache_hits.clear();
    		cache.clear();
    	} catch (Exception e) {
    		log.error("removeAll(): error", e);
    	} finally {
    		accessSem.release();
    	}
    }
        
    private void removeLeastHitCache() {
    	try {
    		accessSem.acquire("removeLeastHitCache");
	        if (log.isTraceEnabled()) log.trace("removeLeastHitCache(): max cache size reached: " + max_size);
	        long min_date = Long.MAX_VALUE;
	        Object min_key = null;
	        Iterator<Entry<Object,Long>> iter = cache_hits.entrySet().iterator();
	        while (iter.hasNext()) {
	            Entry<Object,Long> entry = iter.next();
	            //Object key = entry.getKey();
	            //Object cachedValue = cache.get(key);
	            long hit_date = (Long)entry.getValue();
	            if ((min_key == null) || (hit_date < min_date)) {
	                min_key = entry.getKey();
	                min_date = hit_date;
	            }
	        }
	        if (min_key != null) {
	        	mostRecentlyRemoved = cache.get(min_key);
	        	cache_hits.remove(min_key);
	        	cache.remove(min_key);
	        }
    	} catch (Exception e) {
    		log.error("removeLeastHitCache(): error", e);
    	} finally {
    		accessSem.release();
    	}
    }    
    
}
