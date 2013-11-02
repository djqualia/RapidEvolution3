package com.mixshare.rapid_evolution.audio.tags.readers.mp3;

import java.io.File;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;
import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import com.mixshare.rapid_evolution.audio.tags.readers.BaseTagReader;
import com.mixshare.rapid_evolution.music.duration.Duration;

public class JavaZoomTagReader extends BaseTagReader {

    private static Logger log = Logger.getLogger(JavaZoomTagReader.class);
    
    private Map properties = null;
    
    public JavaZoomTagReader(String filename) {
        try {
	        if (log.isDebugEnabled()) log.debug("JavaZoomTagReader(): filename=" + filename);
	        File file = new File(filename);
	        AudioFileFormat baseFileFormat = null;
	        AudioFormat baseFormat = null;
            AudioInputStream in = AudioSystem.getAudioInputStream(file);                        
	        baseFileFormat = AudioSystem.getAudioFileFormat(in);
	        baseFormat = baseFileFormat.getFormat();
	        // TAudioFileFormat properties
	        if (baseFileFormat instanceof TAudioFileFormat) {
	            properties = ((TAudioFileFormat)baseFileFormat).properties();
	        }
	        // TAudioFormat properties
	        if (baseFormat instanceof TAudioFormat) {
	            properties = ((TAudioFormat)baseFormat).properties();
	        }
	        if (log.isDebugEnabled()) log.debug("JavaZoomTagReader(): properties=" + properties);
            in.close();
        } catch (Exception e) {
            log.error("JavaZoomTagReader(): error Exception, filename=" + filename, e);
        }
    }
    
    public String getAlbum() {
        return (String)properties.get("album");        
    }
    
    public String getArtist() {
        return (String)properties.get("author");
    }
    
    public String getComments() {
        return (String)properties.get("comment");        
    }
    
    public String getGenre() {
        return (String)properties.get("mp3.id3tag.genre");
    }
    
    public String getTime() {
        return new Duration((int)(((Long)properties.get("duration")).longValue() / 1000)).getDurationAsString();
    }

    public String getTitle() {
        return (String)properties.get("title");
    }
    
    public String getTrack() {
        return (String)properties.get("mp3.id3tag.track");
    }
    
    public String getYear() {
        return (String)properties.get("year");        
    }

}
