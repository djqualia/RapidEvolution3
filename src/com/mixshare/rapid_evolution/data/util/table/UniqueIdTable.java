package com.mixshare.rapid_evolution.data.util.table;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.IdentifierParser;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

public class UniqueIdTable implements Serializable {

    static private Logger log = Logger.getLogger(UniqueIdTable.class);    
    static private final long serialVersionUID = 0L;    

    ////////////
    // FIELDS //
    ////////////
    
    private Map<Identifier, Integer> identifierMap = new HashMap<Identifier, Integer>();
    private Map<Integer, Identifier> uniqueIdMap = new HashMap<Integer, Identifier>();
    private int nextIntegerValue = 1; //Integer.MIN_VALUE;   
    private boolean checkNextUniqueIdMode = false;
    
    private transient Semaphore sem = new Semaphore(1);
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(UniqueIdTable.class);
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
    
    public UniqueIdTable() { }
    public UniqueIdTable(LineReader lineReader) {
    	String version = lineReader.getNextLine();
    	int idMapSize = Integer.parseInt(lineReader.getNextLine());
    	identifierMap = new HashMap<Identifier, Integer>(idMapSize);
    	for (int i = 0; i < idMapSize; ++i) {
    		Identifier id = IdentifierParser.getIdentifier(lineReader.getNextLine());
    		int uniqueId = Integer.parseInt(lineReader.getNextLine());
    		identifierMap.put(id, uniqueId);
    	}
    	int uniqueMapSize = Integer.parseInt(lineReader.getNextLine());
    	uniqueIdMap = new HashMap<Integer, Identifier>(uniqueMapSize);
    	for (int i = 0; i < uniqueMapSize; ++i) {
    		int uniqueId = Integer.parseInt(lineReader.getNextLine());
    		Identifier id = IdentifierParser.getIdentifier(lineReader.getNextLine());
    		uniqueIdMap.put(uniqueId, id);
    	}
    	nextIntegerValue = Integer.parseInt(lineReader.getNextLine());
    	checkNextUniqueIdMode = Boolean.parseBoolean(lineReader.getNextLine());
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public Identifier getIdentifierFromUniqueId(int uniqueId) {
        return uniqueIdMap.get(uniqueId);
    }
        
    public int getUniqueIdFromIdentifier(Identifier identifier) {
        Integer result = identifierMap.get(identifier);
        if (result != null)
            return result;
        int uniqueId = Integer.MAX_VALUE;
        try {
        	getSemaphore().acquire("UniqueIdTable.getIdFromValue()");
        	uniqueId = nextIntegerValue++;
        	if (checkNextUniqueIdMode) {
        		while (uniqueIdMap.containsKey(uniqueId)) {
        			uniqueId = nextIntegerValue++;
        			if (nextIntegerValue == Integer.MAX_VALUE)
        				nextIntegerValue = Integer.MIN_VALUE;
        		}
        	}
            if (nextIntegerValue == Integer.MAX_VALUE) {
            	nextIntegerValue = Integer.MIN_VALUE;
            	checkNextUniqueIdMode = true;
        		while (uniqueIdMap.containsKey(uniqueId)) {
        			uniqueId = nextIntegerValue++;
        			if (nextIntegerValue == Integer.MAX_VALUE)
        				nextIntegerValue = Integer.MIN_VALUE;
        		}
            }
            identifierMap.put(identifier, uniqueId);
            uniqueIdMap.put(uniqueId, identifier);
        } catch (Exception e) {
        	log.error("getIdFromValue(): error", e);
        } finally {
        	getSemaphore().release();
        }
        return uniqueId;
    }
    
    public int getNextAvailableUniqueId() { return nextIntegerValue; }
        
    private Semaphore getSemaphore() {
    	if (sem == null)
    		sem = new Semaphore(1);
    	return sem;
    }
    
    // for serialization
	public Map<Identifier, Integer> getIdentifierMap() { return identifierMap; }
	public Map<Integer, Identifier> getUniqueIdMap() { return uniqueIdMap; }
	public int getNextIntegerValue() { return nextIntegerValue; }
	public boolean isCheckNextUniqueIdMode() { return checkNextUniqueIdMode; }
	
    /////////////
    // SETTERS //
    /////////////
    
    public void setUniqueIdForIdentifier(int uniqueId, Identifier id) {
        try {
        	getSemaphore().acquire("UniqueIdTable.setUniqueIdForIdentifier()");
        	identifierMap.put(id, uniqueId);
        } catch (Exception e) {
        	log.error("setUniqueIdForIdentifier(): error", e);
        } finally {
        	getSemaphore().release();
        }
    }
        
	// for serialization
	public void setIdentifierMap(Map<Identifier, Integer> identifierMap) { this.identifierMap = identifierMap; }
	public void setUniqueIdMap(Map<Integer, Identifier> uniqueIdMap) { this.uniqueIdMap = uniqueIdMap; }
	public void setNextIntegerValue(int nextIntegerValue) { this.nextIntegerValue = nextIntegerValue; }    
	public void setCheckNextUniqueIdMode(boolean checkNextUniqueIdMode) { this.checkNextUniqueIdMode = checkNextUniqueIdMode; }
    
    /////////////
    // METHODS //
    /////////////
    
    public void updateIdentifier(Identifier newId, Identifier oldId) {
    	try {
    		getSemaphore().acquire("UniqueIdTable.updateIdentifier()");
    		int uniqueId = identifierMap.remove(oldId);
    		identifierMap.put(newId, uniqueId);
    		uniqueIdMap.put(uniqueId, newId); // will remove the oldId value
    	} catch (Exception e) {
    		log.error("updateIdentifier(): error", e);
    	} finally {
    		getSemaphore().release();
    	}
    }

	public void write(LineWriter writer) {
		writer.writeLine(1); // version
		try {
			getSemaphore().acquire("UniqueIdTable.write()");
			writer.writeLine(identifierMap.size());
			for (Entry<Identifier, Integer> entry : identifierMap.entrySet()) {
				writer.writeLine(entry.getKey().getUniqueId());
				writer.writeLine(entry.getValue());
			}
			writer.writeLine(uniqueIdMap.size());
			for (Entry<Integer, Identifier> entry : uniqueIdMap.entrySet()) {
				writer.writeLine(entry.getKey());
				writer.writeLine(entry.getValue().getUniqueId());
			}
		} catch (Exception e) {
			log.error("write(): error", e);
		} finally {
			getSemaphore().release();
		}
		writer.writeLine(nextIntegerValue);
		writer.writeLine(checkNextUniqueIdMode);
	}
	
	public void clearIdentier(Identifier id) {
		identifierMap.remove(id);
	}
    
}
