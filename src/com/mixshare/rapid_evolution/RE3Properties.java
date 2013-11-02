package com.mixshare.rapid_evolution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.event.RE3PropertiesChangeListener;
import com.mixshare.rapid_evolution.ui.util.Color;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.encryption.StringEncrypter;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.readers.PlainTextLineReader;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

public class RE3Properties {

    static private Logger log = Logger.getLogger(RE3Properties.class);
	
    ////////////
    // FIELDS //
    ////////////
    
    static private String propertiesFilename = "re3.properties";  
    static private String userPropertiesFilename = "user_settings.properties";
    static private Properties properties = null;
    static private Properties userProperties = null;
    static private long lastModified;
    static private long lastChecked = 0;
    static private long checkAtMostInterval = 30000; // 30 seconds

    static private Vector<RE3PropertiesChangeListener> changeListeners = new Vector<RE3PropertiesChangeListener>();
    
	static private String encryptionKey = "g843mjtyddfj260sdkgnj58fgh3w01lsv9t6j6g78d";
	static private String encryptionScheme = StringEncrypter.DESEDE_ENCRYPTION_SCHEME;
    static private StringEncrypter encrypter;

    static private Semaphore checkSem = new Semaphore(1);
    
    static {
    	try {
    		encrypter = new StringEncrypter(encryptionScheme, encryptionKey);
    	} catch (Exception e) {
    		log.error("static(): error", e);
    	}
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    static public String getPropertyFilename() { return propertiesFilename; }    
    
    static public String getEncryptedProperty(String key) {
    	try {
    		return encrypter.decrypt(getProperty(key));
    	} catch (Exception e) {
    		log.error("getEncryptedProperty(): error", e);
    	}
    	return null;
    }
    static public String getProperty(String key) {
		check();		
		if ((userProperties != null) && userProperties.containsKey(key))
			return userProperties.getProperty(key);
		if (!properties.containsKey(key)) {
			if (log.isTraceEnabled())
				log.trace("getProperty(): missing=" + key);
		}
		return properties.getProperty(key);
    }   
        
    static public boolean getBoolean(String key) { return "true".equalsIgnoreCase(getProperty(key)); }
    static public short getShort(String key) { return Short.parseShort(getProperty(key).trim()); }
    static public int getInt(String key) { return Integer.parseInt(getProperty(key).trim()); }
    static public long getLong(String key) { return Long.parseLong(getProperty(key).trim()); }
    static public float getFloat(String key) { return Float.parseFloat(getProperty(key).trim()); }
    static public double getDouble(String key) { return Double.parseDouble(getProperty(key).trim()); }
    static public Color getColor(String key) {
    	String value = getProperty(key);
    	int index1 = value.indexOf(",");
    	int index2 = value.indexOf(",", index1 + 1);
    	float red = Float.parseFloat(value.substring(1, index1));
    	float green = Float.parseFloat(value.substring(index1 + 1, index2));
    	float blue = Float.parseFloat(value.substring(index2 + 1, value.length() - 1));
    	return new Color(red, green, blue);
    }
    
    /////////////
    // SETTERS //
    /////////////

    static public void setPropertyFilename(String filename) { propertiesFilename = filename; }
    static public void setProperty(Object key, Object value) {
    	checkUserProperties();
    	userProperties.put(key, value);
    }
    
    static public void setPropertyForTest(String key, Object value) {
    	setPropertyForTest(key, String.valueOf(value));
    }
    static public void setPropertyForTest(String key, String value) {
    	check();
    	properties.put(key,  value);
    }
    
    static public void addChangeListener(RE3PropertiesChangeListener listener) {
    	changeListeners.add(listener);
    }
    
    /////////////
    // METHODS //
    /////////////
    
    static private void check() {
    	boolean changed = false;
    	try {
    		checkSem.acquire();
    		File file = new File(propertiesFilename);
    		long timeSinceLastChecked = System.currentTimeMillis() - lastChecked;
    		if ((properties == null) || ((timeSinceLastChecked > checkAtMostInterval) && (file.lastModified() > lastModified))) {
    			properties = new Properties();
    			//properties.load(new FileInputStream(propertiesFilename));
    			customLoadProperties(properties, propertiesFilename);
    			while (properties.size() == 0) {
    				log.warn("check(): loaded empty properties file?=" + propertiesFilename + ", retrying...");
    				Thread.sleep(500);
    				customLoadProperties(properties, propertiesFilename);
    			}
    			lastModified = file.lastModified();    			
    			if (log.isDebugEnabled())
    				log.debug("check(): properties loaded...");  
    			changed = true; 			
    		}
    		lastChecked = System.currentTimeMillis();
    	} catch (Exception e) {
    		log.error("check(): could not load properties file=" + propertiesFilename);
    	} finally {
	    	checkSem.release();
    	}
    	if (changed)
    		fireChangedEvent();
    }
    
    static public void fireChangedEvent() {
		for (RE3PropertiesChangeListener changeListener : changeListeners)
			changeListener.propertiesChanged();
    }
    
    static private void checkUserProperties() {
    	if (userProperties == null)
    		loadUserProperties();    	
    }
    
    static public void customLoadProperties(Properties properties, String filename) {
    	try {
	    	File file = new File(filename);
	    	if (!file.exists())
	    		return;
	    	LineReader lineReader = new PlainTextLineReader(filename);
	    	String line = lineReader.getNextLine();
	    	while (line != null) {
	    		if (!line.startsWith("#") || !line.startsWith(";")) {
	    			int index = line.indexOf("=");
	    			if (index > 0) {
	    				String key = line.substring(0, index).trim();
	    				String value = line.substring(index + 1).trim();
	    				properties.setProperty(key, value);
	    			}
	    		}
	    		line = lineReader.getNextLine();
	    	}
	    	lineReader.close();
	    	if (log.isDebugEnabled())
	    		log.debug("customLoadProperties(): read from file...");
    	} catch (Exception e) {
    		log.error("customLoadProperties(): error", e);
    	}
    }
    
    static public void loadUserProperties() {
		if (userProperties == null) {
			try {
				// only load user properties once
				userProperties = new Properties();
				String filename = OSHelper.getWorkingDirectory() + "/" + userPropertiesFilename;
				File file = new File(filename);
				if (file.exists())
					userProperties.load(new FileInputStream(filename));
			} catch (FileNotFoundException fnfe) {
			} catch (Exception e) {
				log.error("loadUserProperties(): error", e);
			}
		}    	
    }
    
    static public void loadUserProperties(String filename) {
		try {
			if (userProperties == null)
				userProperties = new Properties();
			userProperties.load(new FileInputStream(filename));
		} catch (FileNotFoundException fnfe) {
		} catch (Exception e) {
			log.error("loadUserProperties(): error", e);
		}
    }
    
    static public void save() {
    	if ((userProperties != null) && (userProperties.size() > 0)) {
    		try {
    			FileOutputStream out = new FileOutputStream(OSHelper.getWorkingDirectory() + "/" + userPropertiesFilename);
    			userProperties.store(out, "---RE3 USER SETTINGS---");
    			out.close();
    		} catch (Exception e) {
    			log.error("save(): error", e);
    		}
    	}
    }
    
    static public void main(String[] args) {
    	try {
    		RapidEvolution3.loadLog4J();
    		
    		String APIKEY = "7f8ad0f5e1";
    		log.info("encrypted=" + encrypter.encrypt(APIKEY));
    		log.info("decrypted=" + encrypter.decrypt(encrypter.encrypt(APIKEY)));
    		
    		log.info("decrypted value=" + encrypter.decrypt("9YN3hBW1Y7Yyr7TEvfzeYJcWDZdQ1FWu2004W6Ol47jPxN6KIwQyCQ=="));
    		
    	} catch (Exception e) {
    		log.error("main(): error", e);
    	}
    }
    
}
