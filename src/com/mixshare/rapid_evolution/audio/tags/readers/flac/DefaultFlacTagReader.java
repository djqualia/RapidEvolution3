package com.mixshare.rapid_evolution.audio.tags.readers.flac;

import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.metadata.Metadata;
import org.kc7bfi.jflac.metadata.VorbisComment;
import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import com.mixshare.rapid_evolution.audio.tags.readers.BaseTagReader;
import com.mixshare.rapid_evolution.util.StringUtil;

public class DefaultFlacTagReader extends BaseTagReader {

    private static Logger log = Logger.getLogger(DefaultFlacTagReader.class);
    
    private Map properties = null;
    private Metadata[] meta = null;
    
    public DefaultFlacTagReader(String filename) {
        try {
            if (log.isDebugEnabled()) log.debug("DefaultFlacTagReader(): filename=" + filename);
            File file = new File(filename);
            FlacAudioFileReader reader = new FlacAudioFileReader();
            FLACDecoder decoder = new FLACDecoder(reader.getAudioInputStream(file));                      
            meta = decoder.readMetadata();
            AudioInputStream in = AudioSystem.getAudioInputStream(file);            
            AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(in);
            if (baseFileFormat instanceof TAudioFileFormat) {
                properties = ((TAudioFileFormat)baseFileFormat).properties();
            }        
            in.close();
        } catch (Exception e) {
            log.error("DefaultFlacTagReader(): error Exception", e);
        }
    }
    
    public boolean isFileSupported() {
        return (properties != null);
    }    
    
    public String getAlbum() {
        String album = getMetaValue("album");
        if (StringUtil.isValid(album)) return album;
        if (properties != null) {
            album = (String)properties.get("album");
        }
        return album;
    }
    
    public String getArtist() {
        String artist = getMetaValue("artist");
        if (StringUtil.isValid(artist)) return artist;
        if (properties != null) {
            artist = (String)properties.get("author");
        }
        return artist;
    }
        
    public String getComments() {
        String comments = getMetaValue("comment");
        if (StringUtil.isValid(comments)) return comments;
        if (properties != null) {
            comments = (String)properties.get("comment");
        }
        return comments;
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
        String title = getMetaValue("title");
        if (StringUtil.isValid(title)) return title;
        if (properties != null) {        
            title = (String)properties.get("title");
        }
        return title;
    }
    
    public Integer getTimeInSeconds() {
        if (properties != null) {
            int length = (int) Math.round((((Long)properties.get("duration")).longValue())/1000000);
            if (length != 0) return new Integer(length);
        }
        return null;
    }
    
    public String getTrack() {
        return getMetaValue("tracknumber");
    }
    
    public Integer getRating() {
    	try {
    		return new Integer(getMetaValue("rating"));
    	} catch (Exception e) { }
    	return null;
    }
    
    private String getMetaValue(String id) {
        if (meta != null) {
	        for (int i = 0; i < meta.length; ++i) {
	            log.debug("ReadFlacTags(): meta element=" + meta[i]);
	            if (meta[i] instanceof VorbisComment) {
	                VorbisComment vbcomment = (VorbisComment)meta[i];
	                StringTokenizer tokenized = new StringTokenizer(vbcomment.toString(), "\n");
	                int count = 0;
	                while (tokenized.hasMoreTokens()) {
	                    String token = tokenized.nextToken();
	                    while ((token.length() > 0) && (!Character.isLetterOrDigit(token.charAt(0)))) token = token.substring(1);
	                    if (count > 0) {
	                        if (token.toLowerCase().startsWith(id.toLowerCase() + "="))
	                            return token.substring(id.length() + 1);
	                    }
	                    ++count;
	                }
	            }            
	        }
        }
        return null;
    }
    
}
