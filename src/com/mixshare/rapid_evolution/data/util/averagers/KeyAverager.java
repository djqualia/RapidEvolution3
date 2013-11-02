package com.mixshare.rapid_evolution.data.util.averagers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.music.key.Key;

public class KeyAverager {

	static private Logger log = Logger.getLogger(KeyAverager.class);
	
	private Vector<KeyEntry> entries = null;
    private boolean ignore_empty = true;
    private int maxAccuracy = 0;
	
    public KeyAverager() {
    	entries = new Vector<KeyEntry>();
    }    
    public KeyAverager(int size) {
    	entries = new Vector<KeyEntry>(size);
    }
    public KeyAverager(boolean ignore_empty_counts) {
    	entries = new Vector<KeyEntry>();
    	ignore_empty = ignore_empty_counts;
    }
    public KeyAverager(int size, boolean ignore_empty_counts) {
    	entries = new Vector<KeyEntry>(size);
    	ignore_empty = ignore_empty_counts;
    }

    public class KeyEntry {
    	private Key value;
    	private long timestamp;
    	private int accuracy;
    	public KeyEntry(Key value, long timestamp, int accuracy) {
    		this.value = value;
    		this.timestamp = timestamp;
    		this.accuracy = accuracy;
    	}
    	public Key getKey() { return value; }
    	public long getTimestamp() { return timestamp; }
    	public int getAccuracy() { return accuracy; }
    	public String toString() { return value.toStringExact() + ", " + timestamp + ", " + accuracy; }
    }
    
    public void addValue(Key value, long timestamp, int accuracy) {
    	if (value == null)
    		return;
    	if ((ignore_empty) && (value == Key.NO_KEY))
    		return;
    	if (accuracy > maxAccuracy) {
    		maxAccuracy = accuracy;
    		entries.clear();
    	}
    	if (accuracy == maxAccuracy)
    		entries.add(new KeyEntry(value, timestamp, accuracy));
    }

    public Key getModeValue() {
    	if (entries.size() == 0)
    		return Key.NO_KEY;    	
    	Map<Integer, Integer> rootCount = new HashMap<Integer, Integer>();
    	Map<Integer, Long> rootModified = new HashMap<Integer, Long>();
    	for (KeyEntry entry : entries) {
    		int root = entry.getKey().getKeyCode().getKeyValue();
    		Integer count = (Integer)rootCount.get(root);
    		Long modified = (Long)rootModified.get(root);
    		if (count == null) {
    			count = new Integer(1);
    			modified = new Long(entry.getTimestamp());
    		} else {
    			count = new Integer(1 + count.intValue());
    			modified = new Long(Math.max(entry.getTimestamp(), modified.longValue()));
    		}
    		rootCount.put(root, count);
    		rootModified.put(root, modified);
    	}    	    	
    	Iterator<Entry<Integer, Integer>> rootIter = rootCount.entrySet().iterator();
    	int maxCount = 0;
    	int maxRoot = 0;
    	long maxModified = 0;
    	while (rootIter.hasNext()) {
    		Entry<Integer, Integer> entry = rootIter.next();
    		int count = (Integer)entry.getValue();
    		int rootValue = (Integer)entry.getKey();
    		long modified = (Long)rootModified.get(entry.getKey());
    		boolean set = false;
    		if (count > maxCount) {
    			set = true;
    		} else if (count == maxCount) {
    			// tie breaker: count neighbors of each based on key codes (circle of fifths)
    			int en1 = rootValue - 1;
    			if (en1 < 1) en1 += 12;
    			int en2 = rootValue + 1;
    			if (en2 > 12) en2 -= 12;
    			Integer ent1count = (Integer)rootCount.get(en1);
    			Integer ent2count = (Integer)rootCount.get(en2);
    			int encount = 0;
    			if (ent1count != null)
    				encount += ent1count.intValue();
    			if (ent2count != null)
    				encount += ent2count.intValue();
    			
    			int n1 = maxRoot - 1;
    			if (n1 < 1) n1 += 12;
    			int n2 = maxRoot + 1;
    			if (n2 > 12) n2 -= 12;
    			Integer nt1count = (Integer)rootCount.get(n1);
    			Integer nt2count = (Integer)rootCount.get(n2);
    			int ncount = 0;
    			if (nt1count != null)
    				ncount += nt1count.intValue();
    			if (nt2count != null)
    				ncount += nt2count.intValue();
    			if (encount > ncount) {
    				set = true;    			
    			} else if (encount == ncount) {
    				// final tie breaker: last modified
    				if (modified > maxModified)
    					set = true;
    			}
    		}
    		if (set) {
    			maxCount = count;
    			maxRoot = rootValue;
    			maxModified = modified;
    		}
    	}
    	Map<Key, Integer> typeCount = new HashMap<Key, Integer>();
    	Map<Key, Long> typeModified = new HashMap<Key, Long>();
    	Map<Byte, Integer> scaleCount = new HashMap<Byte, Integer>();
    	for (KeyEntry entry : entries) {
    		int entryRoot = entry.getKey().getKeyCode().getKeyValue();
    		if (entryRoot == maxRoot) {
    			Key key = entry.getKey();
        		Integer count = (Integer)typeCount.get(key);
        		Long modified = (Long)typeModified.get(key);
        		if (count == null) {
        			count = new Integer(1);
        			modified = new Long(entry.getTimestamp());
        		} else {
        			count = new Integer(1 + count.intValue());
        			modified = new Long(Math.max(entry.getTimestamp(), modified.longValue()));
        		}
        		typeCount.put(key, count);
        		typeModified.put(key, modified);    			
    		}
    		// tell type counts..
			int n1 = entryRoot - 1;
			if (n1 < 1) n1 += 12;
			int n2 = entryRoot + 1;
			if (n2 > 12) n2 -= 12;    		
    		if ((entryRoot == maxRoot) || (n1 == maxRoot) || (n2 == maxRoot)) {
    			Key key = entry.getKey();
    			byte type = key.getScaleType();
    			Integer count = (Integer)scaleCount.get(type);
    			if (count == null) {
    				count = new Integer(1);
    			} else {
    				count = new Integer(1 + count.intValue());
    			}
    			scaleCount.put(type, count);
    		}
    	}    	    	
    	maxCount = 0;
    	maxModified = 0;
    	Key maxKey = null;
    	Iterator typeIter = typeCount.entrySet().iterator();
    	while (typeIter.hasNext()) {
    		Entry entry = (Entry)typeIter.next();
    		int count = (Integer)entry.getValue();
    		Key key = (Key)entry.getKey();
    		long modified = (Long)typeModified.get(entry.getKey());
    		boolean set = false;
    		if (count > maxCount) {
    			set = true;
    		} else {
    			// tie breaker, use scale type counts
    			Integer eCount = (Integer)scaleCount.get(key.getScaleType());
    			Integer mCount = (Integer)scaleCount.get(maxKey.getScaleType());
    			if (eCount.intValue() > mCount.intValue()) {
    				set = true;
    			} else if (eCount.intValue() == mCount.intValue()) {
    				if (modified > maxModified)
    					set = true;    				
    			}
    		}
    		if (set) {
    			maxCount = count;
    			maxKey = key;
    			maxModified = modified;
    		}
    	}
    	return maxKey;
    }
    
    public void debugOutput() {
    	for (KeyEntry entry : entries) {
    		log.debug("debugOutput(): entry=" + entry);
    	}    	
    	log.debug("debugOutput(): mode value=" + getModeValue());
    }

}
