package com.mixshare.rapid_evolution.util.io.readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.log4j.Logger;

/**
 * This is a helper class which will read a text file from the file system
 * and return it as a more usable String.
 */
public class PlainTextLineReader extends AbstractLineReader {

    static private Logger log = Logger.getLogger(PlainTextLineReader.class);

    private FileReader inputstream ;
    private BufferedReader inputbuffer;
    
    public PlainTextLineReader(String filename) throws FileNotFoundException { setFilename(filename); }
    
    public void setFilename(String filename) throws FileNotFoundException {
        try {
            if (filename != null) {
                File file = new File(filename);
                if (file.exists()) {
                    inputstream = new FileReader(filename);
                    inputbuffer = new BufferedReader(inputstream);
                }
            }
        } catch (FileNotFoundException fnfe) {
        	throw fnfe;
        } catch (Exception e) {
            log.error("setFilename(): error Exception", e);
        }        
    }
    
    public boolean isFileFound() { return (inputbuffer != null); }

    public String getNextLine() {
    	String result = null;
    	try {
    		if (inputbuffer != null) {
	    		result = inputbuffer.readLine();
	        	if (result == null)
	        		close();
    		}
    	} catch (Exception e) {
    		log.error("getNextLine(): error", e);
    	}
    	return result;
    }
    
    public void close() {
    	try {
	    	if (inputbuffer != null) {
	    		inputbuffer.close();
	    		inputstream.close();
	    		inputbuffer = null;
	    		inputstream = null;
	    	}
    	} catch (Exception e) {
    		log.error("close(): error", e);
    	}
    }
        
}
