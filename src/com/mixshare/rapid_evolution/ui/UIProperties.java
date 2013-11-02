package com.mixshare.rapid_evolution.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.util.OSHelper;

/**
 * This is a persistent list of properties that the various UI components can utilize
 * (for example, to remember height/width placement of windows/sliders, etc)
 */
public class UIProperties {

    static private Logger log = Logger.getLogger(UIProperties.class);
	
    ////////////
    // FIELDS //
    ////////////
    
    static private String propertiesFilename = OSHelper.getWorkingDirectory() + "/ui.properties";    
    static private Properties properties = null;

    /////////////
    // GETTERS //
    /////////////
    
    static public String getPropertyFilename() { return propertiesFilename; }    
    
    static public String getProperty(String key) {
		check();
		return properties.getProperty(key);
    }   
        
    static public boolean getBoolean(String key) { return "true".equalsIgnoreCase(getProperty(key)); }
    static public byte getByte(String key) { return Byte.parseByte(getProperty(key).trim()); }
    static public short getShort(String key) { return Short.parseShort(getProperty(key).trim()); }
    static public int getInt(String key) { return Integer.parseInt(getProperty(key).trim()); }
    static public long getLong(String key) { return Long.parseLong(getProperty(key).trim()); }
    static public float getFloat(String key) { return Float.parseFloat(getProperty(key).trim()); }    
    
    static public boolean hasProperty(String key) {
    	check();
    	return properties.containsKey(key);
    }
    
    /////////////
    // SETTERS //
    /////////////

    static public void setPropertyFilename(String filename) { propertiesFilename = filename; }
    static public void setProperty(String key, String value) {
    	check();
    	properties.setProperty(key, value);
    }
    
    /////////////
    // METHODS //
    /////////////

    static public void save() {
    	try {
    		if (properties != null) {
    			FileOutputStream fos = new FileOutputStream(propertiesFilename);
    			properties.store(fos, "RE3 UI Properties");
    			fos.close();
    		}
    	} catch (Exception e) {
    		log.error("save(): error", e);
    	}
    }
    
    static private void check() {
    	try {
    		if (properties == null) {
    			properties = new Properties();
    			properties.load(new FileInputStream(propertiesFilename));             
    			if (log.isDebugEnabled())
    				log.debug("check(): properties loaded...");
    		}
    	} catch (Exception e) {
    		log.debug("check(): could not load properties file=" + propertiesFilename);
    	}
    }
    
    static public void loadFile(String filename) {
    	try {
    		if (properties == null) {
    			properties = new Properties();
    		}
			properties.load(new FileInputStream(filename));             
    	} catch (Exception e) {
    		log.debug("loadFile(): could not load properties file=" + propertiesFilename);
    	}
    }
    
}
