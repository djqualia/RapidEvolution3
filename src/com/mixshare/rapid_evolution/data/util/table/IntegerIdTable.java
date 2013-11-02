package com.mixshare.rapid_evolution.data.util.table;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.CommonRecord;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

public class IntegerIdTable implements Serializable {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(IntegerIdTable.class);    

    ////////////
    // FIELDS //
    ////////////
    
    private Map<String, Integer> valueTable = new HashMap<String, Integer>();
    private Map<Integer, String> idTable = new HashMap<Integer, String>();
    private int nextIntegerValue = Integer.MIN_VALUE;   
    private boolean checkNextUniqueIdMode = false;
    
    private transient Semaphore sem = new Semaphore(1);
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(IntegerIdTable.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("sem")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public IntegerIdTable() { }
        
    /////////////
    // GETTERS //
    /////////////
    
    public String getValueFromId(int id) {
        return (String)idTable.get(new Integer(id));
    }
        
    public int getIdFromValue(String style) {
        if ((style == null) || style.equalsIgnoreCase("ALL"))
            return Integer.MAX_VALUE;
        Integer result = (Integer)valueTable.get(style.toLowerCase());
        if (result != null)
            return result.intValue();
        int value = Integer.MAX_VALUE;
        try {
        	getSemaphore().acquire("IntegerIdTable.getIdFromValue()");
            value = nextIntegerValue++;
        	if (checkNextUniqueIdMode) {
        		while (idTable.containsKey(value)) {
        			value = nextIntegerValue++;
        			if (nextIntegerValue == Integer.MAX_VALUE)
        				nextIntegerValue = Integer.MIN_VALUE;
        		}
        	}
            if (nextIntegerValue == Integer.MAX_VALUE) {
            	nextIntegerValue = Integer.MIN_VALUE;
            	checkNextUniqueIdMode = true;            	
        		while (idTable.containsKey(value)) {
        			value = nextIntegerValue++;
        			if (nextIntegerValue == Integer.MAX_VALUE)
        				nextIntegerValue = Integer.MIN_VALUE;
        		}
            }
            valueTable.put(style.toLowerCase(), new Integer(value));
            idTable.put(new Integer(value), style.toLowerCase());
        } catch (Exception e) {
        	log.error("getIdFromValue(): error", e);
        }
        getSemaphore().release();
        return value;
    }
    
    public Vector<Integer> getIdsForValues(Vector<String> values) {
    	//log.debug("getIdsForValues(): values=" + values);
    	Vector<Integer> result = new Vector<Integer>();
    	try {
    		getSemaphore().acquire("IntegerIdTable.getIdsForValues()");
	    	Iterator<Entry<String,Integer>> tagKeysIter = valueTable.entrySet().iterator();
	    	while (tagKeysIter.hasNext()) {
	    		Entry<String,Integer> entry = tagKeysIter.next();    		
	    		String tag = (String)entry.getKey();
	    		boolean found = false;
	    		int i = 0;
	    		while ((i < values.size()) && !found) {
		    		if (tag.equalsIgnoreCase(values.get(i))) {
		    			result.add(entry.getValue());
		    			found = true;
		    		}
	    			++i;
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getIdsForValues(): error", e);
    	}
    	getSemaphore().release();
    	//log.debug("getIdsForValues(): result=" + result);
    	return result;    	
    }
    
    public Vector<Integer> getIdsForValue(String value) {
    	//log.debug("getIdsForValues(): value=" + value);
    	Vector<Integer> result = new Vector<Integer>();
    	try {
    		getSemaphore().acquire("IntegerIdTable.getIdsForValue()");
	    	Iterator<Entry<String,Integer>> tagKeysIter = valueTable.entrySet().iterator();
	    	while (tagKeysIter.hasNext()) {
	    		Entry<String,Integer> entry = tagKeysIter.next();    		
	    		String tag = (String)entry.getKey();
	    		//if (tag.indexOf(value) >= 0)
	    		if (tag.equalsIgnoreCase(value)) {
	    			result.add(entry.getValue());
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getIdsForValue(): error", e);
    	}
    	getSemaphore().release();
    	//log.debug("getIdsForValues(): result=" + result);
    	return result;
    }
    
    private Semaphore getSemaphore() {
    	if (sem == null)
    		sem = new Semaphore(1);
    	return sem;
    }
    
    // for serialization
	public Map<String, Integer> getValueTable() { return valueTable; }
	public Map<Integer, String> getIdTable() { return idTable; }
	public int getNextIntegerValue() { return nextIntegerValue; }
	public boolean isCheckNextUniqueIdMode() { return checkNextUniqueIdMode; }

	/////////////
	// SETTERS //
	/////////////
	
	// for serialization
	public void setValueTable(Map<String, Integer> valueTable) { this.valueTable = valueTable; }
	public void setIdTable(Map<Integer, String> idTable) { this.idTable = idTable; }
	public void setNextIntegerValue(int nextIntegerValue) { this.nextIntegerValue = nextIntegerValue; }
	public void setCheckNextUniqueIdMode(boolean checkNextUniqueIdMode) { this.checkNextUniqueIdMode = checkNextUniqueIdMode; }
    
}
