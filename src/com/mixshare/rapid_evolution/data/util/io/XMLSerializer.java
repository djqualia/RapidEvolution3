package com.mixshare.rapid_evolution.data.util.io;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.FileLockManager;

public class XMLSerializer {

    static private Logger log = Logger.getLogger(XMLSerializer.class);   
    
    static public boolean saveData(Object data, String filePath) {
    	boolean result = false;
        try {        	
        	if (log.isTraceEnabled())
        		log.trace("saveData(): data=" + data + ", filePath=" + filePath);
        	filePath = StringUtil.checkFullPathLength(filePath);
        	FileLockManager.startFileWrite(filePath);
        	if (filePath != null) {        		
	        	File file = new File(filePath);
	        	if (!file.isDirectory()) {
	            	FileOutputStream os = new FileOutputStream(filePath);
	            	XMLEncoder encoder = new XMLEncoder(os);
	        		encoder.setExceptionListener(new ExceptionListener() {
	        			public void exceptionThrown(Exception exception) {
	        				log.error("readData(): error", exception);
	        			}
	        		});	            	
	            	encoder.writeObject(data);
	            	encoder.close(); 
		            result = true;
	        	} else {
	        		log.warn("saveData(): attempted to save over directory, filePath=" + filePath + ", data=" + data);        		
	        	}
        	}
        } catch (Error e) {
            log.error("saveData(): error", e);           
        } catch (Exception e) {
            log.error("saveData(): xception", e);           
        } finally {
        	FileLockManager.endFileWrite(filePath);
        }
    	if (log.isTraceEnabled())
    		log.trace("saveData(): done saving...");
        return result;
    }
    
    static public byte[] getCompressedBytes(Object data) {
    	byte[] result = null;
        try {        	
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOut = new GZIPOutputStream(baos);           
        	XMLEncoder encoder = new XMLEncoder(gzipOut);
        	encoder.writeObject(data);
        	encoder.close(); 
        	result = baos.toByteArray();
        	baos.close();
        } catch (Exception e) {
            log.error("getBytes(): exception", e);           
        }
        return result;
    }

    static public byte[] getBytes(Object data) {
    	byte[] result = null;
        try {        	
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	XMLEncoder encoder = new XMLEncoder(baos);
        	encoder.writeObject(data);
        	encoder.close(); 
        	result = baos.toByteArray();
        	baos.close();
        } catch (Exception e) {
            log.error("getBytes(): exception", e);           
        }
        return result;
    }
    
    static public String getXMLText(Object data) {
    	return new String(getBytes(data));
    }
    
    static public Object parseXMLText(String text) {
    	return readBytes(text.getBytes());
    }
    
    static public Object readData(String filePath) {
    	Object result = null;
        try {
        	filePath = StringUtil.checkFullPathLength(filePath);
        	FileLockManager.startFileRead(filePath);
        	if (isValid(filePath)) {
        		FileInputStream os = new FileInputStream(filePath);
        		XMLDecoder decoder = new XMLDecoder(os);
        		decoder.setExceptionListener(new ExceptionListener() {
        			public void exceptionThrown(Exception exception) {
        				log.error("readData(): error", exception);
        			}
        		});
        		result = decoder.readObject();
        		decoder.close(); 
        	}
        } catch (java.io.FileNotFoundException fnfe) {
            log.trace("readData(): file not found exception, filePath=" + filePath);
        } catch (Error e) {
            log.error("readData(): error, filePath=" + filePath, e);
        } catch (Exception e) {
            log.error("readData(): exception, filePath=" + filePath, e);
        } finally {
        	FileLockManager.endFileRead(filePath);
        }
        return result;
    }
    
    static public Object readCompressedBytes(byte[] bytes) {
    	Object result = null;
        try {        	
            ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);            
            GZIPInputStream gzipIn = new GZIPInputStream(byteIn);            
    		XMLDecoder decoder = new XMLDecoder(gzipIn);
    		result = decoder.readObject();
    		decoder.close(); 
        } catch (Exception e) {
            log.error("readData(): exception", e);           
        }
        return result;
    }    
    
    static public Object readBytes(byte[] bytes) {
    	Object result = null;
        try {        	
            ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);            
    		XMLDecoder decoder = new XMLDecoder(byteIn);
    		result = decoder.readObject();
    		decoder.close(); 
        } catch (Exception e) {
            log.error("readData(): exception", e);           
        }
        return result;
    }  
    
    ////////////////////
    // HELPER METHODS //
    ////////////////////
    
    static private boolean isValid(String filePath) {
    	if (filePath == null)
    		return false;
    	File file = new File(filePath);
    	if (file.getName().equalsIgnoreCase("con")) // windows didn't like a particular file with that name once, weird...
    		return false;
    	return true;
    }        
    
}
