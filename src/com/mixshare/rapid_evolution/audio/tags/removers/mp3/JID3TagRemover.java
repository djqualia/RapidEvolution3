package com.mixshare.rapid_evolution.audio.tags.removers.mp3;

import java.io.File;

import org.apache.log4j.Logger;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.MediaFile;

import com.mixshare.rapid_evolution.audio.tags.TagRemover;

public class JID3TagRemover implements TagRemover {

    private static Logger log = Logger.getLogger(JID3TagRemover.class);
    
    private String filename = null;
    
    public JID3TagRemover(String filename) {
        this.filename = filename;
    }
    
    public boolean removeTags() {
        boolean success = false;
        try {
            File source_file = new File(filename);
            MediaFile media_file = new MP3File(source_file);
            media_file.removeID3V1Tag();
            media_file.removeID3V2Tag();
            media_file.sync();            
            success = true;
    	} catch (Exception e) { log.error("removeTag(): error Exception", e); }
    	return success;                
    }
}
