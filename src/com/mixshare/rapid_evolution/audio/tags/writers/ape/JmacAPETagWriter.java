package com.mixshare.rapid_evolution.audio.tags.writers.ape;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.audio.tags.writers.BaseTagWriter;

import davaguine.jmac.info.APEInfo;
import davaguine.jmac.info.APETag;

/**
 * TODO: this class didn't really work, one day look into?
 */
public class JmacAPETagWriter extends BaseTagWriter {

    private static Logger log = Logger.getLogger(JmacAPETagWriter.class);
 
    private String filename;
    private APEInfo info;
    private APETag tag;
    
    public JmacAPETagWriter(String filename) {
        try {
            this.filename = filename;
            info = new APEInfo(new File(filename));
            tag = info.getApeInfoTag();
        } catch (Exception e) {
            log.error("JmacAPETagWriter(): error", e);
        }        
    }
    
    public boolean save() {
        boolean success = false;
        try {
            if (tag != null) {
                tag.Save();
                success = true;
            }
        } catch (Exception e) {
            log.error("save(): error saving tag, filename=" + filename, e);
        }
        return success;
    }    
    
    public void setArtist(String artist) { setField(APETag.APE_TAG_FIELD_ARTIST, artist); }
    
    private void setField(String id, String value) {
        try {
            if (tag != null)
                tag.SetFieldString(id, value);
        } catch (Exception e) {
            log.error("setField(): error", e);
        }        
    }    
    
}
