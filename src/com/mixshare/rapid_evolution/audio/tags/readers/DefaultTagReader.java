package com.mixshare.rapid_evolution.audio.tags.readers;


public class DefaultTagReader extends BaseTagReader {

    private String filename;
    
    public DefaultTagReader(String filename) {
        this.filename = filename;
    }
    
    public String getFilename() { return filename; }
        
}
