package com.mixshare.rapid_evolution.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.log4j.Logger;

/**
 * This is a helper class which will read a text file from the file system
 * and return it as a more usable String.
 */
public class TextFileReader {

    static private Logger log = Logger.getLogger(TextFileReader.class);

    private String text;
    
    public TextFileReader() { }
    public TextFileReader(String filename) { setFilename(filename); }
    
    public void setFilename(String filename) {
        try {
            if (filename != null) {
                File file = new File(filename);
                if (file.exists()) {
                    StringBuffer textBuffer = new StringBuffer();
                    FileReader inputstream = new FileReader(filename);
                    BufferedReader inputbuffer = new BufferedReader(inputstream);
                    String line;
                    do {
                        line = inputbuffer.readLine();
                        if (line != null) {
                            textBuffer.append(line);
                            textBuffer.append("\n");
                        }
                    } while (line != null);
                    text = textBuffer.toString();
                    inputbuffer.close();
                    inputstream.close();
                } else {
                	log.warn("setFilename(): file does not exist=" + filename);
                }
            }
        } catch (Exception e) {
            log.error("setFilename(): error Exception", e);
        }        
    }
    
    /**
     * Returns the contents of the text file as a String.
     */
    public String getText() { return text; }
    
}
