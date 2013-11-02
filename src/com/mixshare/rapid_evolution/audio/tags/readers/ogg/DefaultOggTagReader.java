package com.mixshare.rapid_evolution.audio.tags.readers.ogg;

import java.io.File;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import com.mixshare.rapid_evolution.audio.tags.readers.BaseTagReader;

public class DefaultOggTagReader extends BaseTagReader {
    
    private static Logger log = Logger.getLogger(DefaultOggTagReader.class);
    
    private Map properties = null;
    
    public DefaultOggTagReader(String filename) {
        try {
            if (log.isDebugEnabled()) log.debug("DefaultOggTagReader(): filename=" + filename);
            File file = new File(filename);
            AudioInputStream in = AudioSystem.getAudioInputStream(file);                        
            AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(in);
            if (baseFileFormat instanceof TAudioFileFormat) {
                properties = ((TAudioFileFormat)baseFileFormat).properties();
            }
            in.close();
        } catch (Exception e) {
            log.error("DefaultOggTagReader(): error Exception", e);
        }
    }
    
    public boolean isFileSupported() {
        return (properties != null);
    }
    
    public String getAlbum() {
        if (properties != null) {
            return (String)properties.get("album");
        }
        return null;
    }
    
    public String getArtist() {
        if (properties != null) {
            return (String)properties.get("author");
        }
        return null;
    }
        
    public String getComments() {
        if (properties != null) {
            return (String)properties.get("comment");
        }
        return null;
    }
        
    public Integer getNominalBitrate() {
        if (properties != null) {        
            int nominalbitrate = ((Integer)properties.get("ogg.bitrate.nominal.bps")).intValue();
            if (nominalbitrate != 0) return new Integer(nominalbitrate);
        }
        return null;
    }
        
    public Integer getSamplingRate() {
        if (properties != null) {        
            int rate = ((Integer)properties.get("ogg.frequency.hz")).intValue();
            if (rate != 0) return new Integer(rate);
        }
        return null;
    }
        
    public String getTitle() {
        if (properties != null) {        
            return (String)properties.get("title");
        }
        return null;
    }
        
    public Integer getTimeInSeconds() {
        if (properties != null) {
            int length = (int) Math.round((((Long)properties.get("duration")).longValue())/1000000);
            if (length != 0) return new Integer(length);
        }
        return null;
    }
    
    public String getTrack() {
        if (properties != null) {
            return (String)properties.get("ogg.comment.track");
        }
        return null;
    }
    
}
