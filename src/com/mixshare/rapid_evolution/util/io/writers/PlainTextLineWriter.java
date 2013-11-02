package com.mixshare.rapid_evolution.util.io.writers;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.util.StringUtil;

public class PlainTextLineWriter extends AbstractLineWriter {

    private static Logger log = Logger.getLogger(PlainTextLineWriter.class);
    
    private String filename;
    private FileWriter outputstream;
    private BufferedWriter outputbuffer;
    
    public PlainTextLineWriter(String _filename) {
        filename = _filename;
        try {
            outputstream = new FileWriter(filename);
            outputbuffer = new BufferedWriter(outputstream);
        } catch (java.io.IOException e) {
            log.error("PlainTextFileWriter(): error creating outputstream for filename: " + filename);
        }
    }

    public void writeLine(boolean value) { writeLine(String.valueOf(value)); }
    public void writeLine(int value) { writeLine(String.valueOf(value)); }
    public void writeLine(long value) { writeLine(String.valueOf(value)); }
    public void writeLine(byte value) { writeLine(String.valueOf(value)); }
    public void writeLine(float value) { writeLine(String.valueOf(value)); }
    public void writeLine(double value) { writeLine(String.valueOf(value)); }
    public void writeLine(short value) { writeLine(String.valueOf(value)); }
    
    public void writeLine(Object text, String annotation) {
    	writeLine(String.valueOf(text));
    }
    
    public void writeLine(String text) {
        try {
        	if (text == null)
        		text = "";
            outputbuffer.write(StringUtil.removeNewLines(text));
            outputbuffer.newLine();
        } catch (java.io.IOException e) {
            log.error("writeLine(): error writing line to outputstream: " + text);
        }
    }
    
    public void flush() {
    	try {
    		outputbuffer.flush();
        } catch (java.io.IOException e) {
            log.error("flush(): error flushing=" + e);
        }    		
    }
    
    public void close() {
        try {
            outputbuffer.close();
            outputstream.close();
        } catch (java.io.IOException e) {
            log.error("close(): error closing output buffer/stream");
        }
    }
    
}
