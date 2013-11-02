package com.mixshare.rapid_evolution.audio.tags.readers.asf;

import java.io.File;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.asf.AsfTag;

import com.mixshare.rapid_evolution.audio.tags.readers.JAudioGenericTagReader;

public class JAudioASFTagReader extends JAudioGenericTagReader {
    
    private static Logger log = Logger.getLogger(JAudioASFTagReader.class);
        
    protected AsfTag asfTag = null;
    
    public JAudioASFTagReader(String filename) {
    	super();
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("JAudioASFTagReader(): filename=" + filename);            
            audiofile = AudioFileIO.read(new File(filename));            
            if (audiofile != null) {
            	tag = audiofile.getTag();            
            	asfTag = (AsfTag)tag;
                if (log.isDebugEnabled()) log.debug("JAudioASFTagReader(): tag=" + tag);
            }
        } catch (Exception e) {
            log.error("JAudioASFTagReader(): error Exception", e);
        }
    }
        
}
