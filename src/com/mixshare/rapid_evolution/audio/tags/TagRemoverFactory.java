package com.mixshare.rapid_evolution.audio.tags;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.audio.AudioFileTypes;
import com.mixshare.rapid_evolution.audio.tags.removers.JAudiotaggerTagRemover;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;

public class TagRemoverFactory implements AudioFileTypes {

    private static Logger log = Logger.getLogger(TagRemoverFactory.class);
    
    public static TagRemover getTagRemover(String filename) {
        TagRemover remover = null;
        File test_file = new File(filename);
        if (test_file.exists()) {                
            int audio_file_type = AudioUtil.getAudioFileType(filename);
            if (audio_file_type == AUDIO_FILE_TYPE_MP3) {
                //remover = new JID3TagRemover(filename);
                remover = new JAudiotaggerTagRemover(filename);
            } else {
            	remover = new JAudiotaggerTagRemover(filename);
                //if (log.isDebugEnabled()) log.debug("getTagRemover(): no remover available for filename=" + filename);
            }
        }
        return remover;
    }
    
}
