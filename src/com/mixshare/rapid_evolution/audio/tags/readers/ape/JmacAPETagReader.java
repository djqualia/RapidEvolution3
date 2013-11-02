package com.mixshare.rapid_evolution.audio.tags.readers.ape;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.audio.tags.readers.BaseTagReader;
import com.mixshare.rapid_evolution.audio.tags.readers.flac.DefaultFlacTagReader;

import davaguine.jmac.info.APEInfo;
import davaguine.jmac.info.APETag;
import davaguine.jmac.info.APETagField;

public class JmacAPETagReader extends BaseTagReader {

    private static Logger log = Logger.getLogger(DefaultFlacTagReader.class);
    
    private APEInfo info;
    private APETag tag;
    
    public JmacAPETagReader(String filename) {
        try {
            info = new APEInfo(new File(filename));
            tag = info.getApeInfoTag();
        } catch (Exception e) {
            log.error("JmacAPETagReader(): error", e);
        }        
    }

    public String getAlbum() { return getStringValue(APETag.APE_TAG_FIELD_ALBUM); }
    
    public String getAlbumCoverFilename() {
        // TODO: implement w/APE_TAG_FIELD_COVER_ART_FRONT
        return null;
    }
    
    public String getArtist() { return getStringValue(APETag.APE_TAG_FIELD_ARTIST); }
    
    public String getComments() { return getStringValue(APETag.APE_TAG_FIELD_COMMENT); }
    
    public String getGenre() {
        String genre = getStringValue(APETag.APE_TAG_FIELD_GENRE);
        if (!APETag.APE_TAG_GENRE_UNDEFINED.equals(genre))
            return genre;
        return null;
    }
    
    public Float getReplayGain() {
        // TODO: implement w/APE_TAG_FIELD_REPLAY_GAIN_RADIO
        return null;
    }
    
    public String getTitle() { return getStringValue(APETag.APE_TAG_FIELD_TITLE); }
    
    public String getTrack() { return getStringValue(APETag.APE_TAG_FIELD_TRACK); }
    
    public String getYear() { return getStringValue(APETag.APE_TAG_FIELD_YEAR); }
    
    private String getStringValue(String tagType) {
        try {
            if (tag != null) {
                APETagField tagField = tag.GetTagField(tagType);
                if (tagField != null)
                    return new String(tagField.GetFieldValue(), "UTF8");
            }
        } catch (Exception e) {
            log.error("getStringValue(): error", e);
        }
        return null;
    }
}
