package com.mixshare.rapid_evolution.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class StreamGobbler extends Thread {
        
    private InputStream is;
    private Logger log;
    private StringBuffer output = new StringBuffer();
    
    public StreamGobbler(InputStream is, Logger log) {
        this.is = is;
        this.log = log;
    }
    
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
            	if (output.length() > 0)
            		output.append("\n");
            	output.append(line);
                if ((log != null) && log.isDebugEnabled())
                	log.debug("StreamGobbler.run(): " + line);
            }
            br.close();
            isr.close();
        } catch (IOException ioe) {
        	if (log != null)
        		log.error("StreamGobbler.run(): error Exception", ioe);
        	else
        		ioe.printStackTrace();
        }
    }
    
    public String getOutput() { return output.toString(); }
    
    public String toString() { return getOutput(); }
}