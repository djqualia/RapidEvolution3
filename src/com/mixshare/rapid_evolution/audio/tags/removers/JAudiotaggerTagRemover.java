package com.mixshare.rapid_evolution.audio.tags.removers;

import java.io.File;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;

import com.mixshare.rapid_evolution.audio.tags.TagRemover;

public class JAudiotaggerTagRemover implements TagRemover {

    private static Logger log = Logger.getLogger(JAudiotaggerTagRemover.class);
    
    private String filename = null;
    
    public JAudiotaggerTagRemover(String filename) {
        this.filename = filename;
    }
    
    public boolean removeTags() {
        boolean success = false;
        try {
        	AudioFile f = AudioFileIO.read(new File(filename));
        	new AudioFileIO().deleteTag(f);
        	//f.commit();
            success = true;
    	} catch (Exception e) { log.error("removeTags(): error Exception", e); }
    	return success;                
    }
}
