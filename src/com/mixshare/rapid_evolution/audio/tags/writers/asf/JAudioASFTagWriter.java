package com.mixshare.rapid_evolution.audio.tags.writers.asf;

import java.io.File;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.asf.AsfTag;

import com.mixshare.rapid_evolution.audio.tags.writers.JAudioGenericTagWriter;

public class JAudioASFTagWriter extends JAudioGenericTagWriter {
   
    private static Logger log = Logger.getLogger(JAudioASFTagWriter.class);
        
    protected AsfTag asfTag = null;
    
    public JAudioASFTagWriter(String filename, int mode) {
    	super();
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("JAudioASFTagWriter(): filename=" + filename);            
            audiofile = AudioFileIO.read(new File(filename));            
            if (audiofile != null) {
            	tag = audiofile.getTag();            
            	asfTag = (AsfTag)tag;
                if (log.isDebugEnabled()) log.debug("JAudioASFTagWriter(): tag=" + tag);
            }
        } catch (Exception e) {
            log.error("JAudioASFTagWriter(): error Exception", e);
        }
    }
    
}
