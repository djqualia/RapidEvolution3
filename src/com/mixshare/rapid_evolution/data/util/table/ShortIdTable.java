package com.mixshare.rapid_evolution.data.util.table;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class ShortIdTable implements Serializable {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(ShortIdTable.class);    
    
    ////////////
    // FIELDS //
    ////////////
        
    private Map<String, Short> valueTable = new HashMap<String, Short>();
    private Map<Short, String> idTable = new HashMap<Short, String>();
    private short nextShortValue = Short.MIN_VALUE;   
    private boolean checkNextUniqueIdMode = false;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public ShortIdTable() { }
    
    /////////////
    // GETTERS //
    /////////////
    
    public String getValueFromId(short id) {
        return (String)idTable.get(new Short(id));
    }
    
    public short getIdFromValue(String style) {
        if ((style == null) || style.equalsIgnoreCase("ALL"))
            return Short.MAX_VALUE;
        Short result = (Short)valueTable.get(style.toLowerCase());
        if (result != null)
            return result.shortValue();
        short value = nextShortValue++;
    	if (checkNextUniqueIdMode) {
    		while (idTable.containsKey(value)) {
    			value = nextShortValue++;
    			if (nextShortValue == Short.MAX_VALUE)
    				nextShortValue = Short.MIN_VALUE;
    		}
    	}        
        if (nextShortValue == Short.MAX_VALUE) {
        	nextShortValue = Short.MIN_VALUE;
        	checkNextUniqueIdMode = true;        	
    		while (idTable.containsKey(value)) {
    			value = nextShortValue++;
    			if (nextShortValue == Short.MAX_VALUE)
    				nextShortValue = Short.MIN_VALUE;
    		}
        }
        valueTable.put(style.toLowerCase(), new Short(value));
        idTable.put(new Short(value), style.toLowerCase());
        return value;
    }

    // for serialization
	public Map<String, Short> getValueTable() { return valueTable; }
	public Map<Short, String> getIdTable() { return idTable; }
	public short getNextShortValue() { return nextShortValue; }
	public boolean isCheckNextUniqueIdMode() { return checkNextUniqueIdMode; }

	/////////////
	// SETTERS //
	/////////////
	
	// for serialization
	public void setValueTable(Map<String, Short> valueTable) { this.valueTable = valueTable; }
	public void setIdTable(Map<Short, String> idTable) { this.idTable = idTable; }
	public void setNextShortValue(short nextShortValue) { this.nextShortValue = nextShortValue; }
	public void setCheckNextUniqueIdMode(boolean checkNextUniqueIdMode) { this.checkNextUniqueIdMode = checkNextUniqueIdMode; }
    
}
