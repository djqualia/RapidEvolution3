package com.mixshare.rapid_evolution.audio.tags.readers;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.audio.tags.util.TagUtil;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.util.StringUtil;

import entagged.audioformats.AudioFile;
import entagged.audioformats.AudioFileIO;
import entagged.audioformats.Tag;

public class EntaggedTagReader extends BaseTagReader {
    
    private static Logger log = Logger.getLogger(EntaggedTagReader.class);
        
    private String filename = null;
    private AudioFile audiofile = null;
    private Tag tag = null;
    
    public EntaggedTagReader(String filename) {
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("EntaggedTagReader(): filename=" + filename);            
            audiofile = AudioFileIO.read(new File(filename));
            if (audiofile != null) {
                tag = audiofile.getTag();
                if (log.isDebugEnabled()) log.debug("EntaggedTagReader(): tag=" + tag);
            }
        } catch (Exception e) {
            log.error("EntaggedTagReader(): error Exception", e);
        }
    }
    
    public boolean isFileSupported() {
        return (tag != null);
    }    
    
    public String getAlbum() {
        String value = null;
        if (tag != null) {
            value = tag.getFirstAlbum();
        }
        return value;
    }
    
    public String getArtist() {
        String value = null;
        if (tag != null) {
            value = tag.getFirstArtist();
        }
        return value;        
    }

    public Integer getBeatIntensity() {
        List result = tag.get(TXXX_BEAT_INTENSITY.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return new Integer(Integer.parseInt(result.get(0).toString().trim()));
        }
        return null;        
    }
    
    public Integer getBpmAccuracy() {
        List result = tag.get(TXXX_BPM_ACCURACY.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return new Integer(Integer.parseInt(result.get(0).toString().trim()));
        }
        return null;                
    }
    
    public Float getBpmStart() {
        List result = tag.get(TXXX_BPM_START.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            try {
                return new Float(Float.parseFloat(result.get(0).toString().trim()));
            } catch (NumberFormatException e) {
                log.debug("getBpmStart(): error number format exception=" + result);
            }
        }
        result = tag.get(TXXX_BPM.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            try {
                return new Float(Float.parseFloat(result.get(0).toString().trim()));
            } catch (NumberFormatException e) {
                log.debug("getBpmStart(): error number format exception=" + result);
            }
        }
        return null;                        
    }
    
    public Float getBpmEnd() {
        List result = tag.get(TXXX_BPM_END.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            try {
                return new Float(Float.parseFloat(result.get(0).toString().trim()));
            } catch (NumberFormatException e) {
                log.debug("getBpmStart(): error number format exception=" + result);
            }
        }
        return null;
    }
    
    public String getComments() {
        String value = null;
        if (tag != null) {
            value = tag.getFirstComment();
        }
        return value;        
    }
    
    public String getContentGroupDescription() {
        List result = tag.get(TAG_CONTENT_GROUP_DESCRIPTION.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;                
    }
    
    public String getContentType() {
        List result = tag.get(TAG_CONTENT_TYPE.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;                        
    }
    
    public String getEncodedBy() {
        List result = tag.get(TAG_ENCODED_BY.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;                        
    }

    public String getFilename() {
        return filename;
    }

    public String getFileType() {
        List result = tag.get(TAG_FILE_TYPE.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;        
    }
    
    public String getGenre() {
        String value = null;
        if (tag != null) {
            value = tag.getFirstGenre();
        }
        return value;                
    }
    
    public Integer getKeyAccuracy() {
        List result = tag.get(TXXX_KEY_ACCURACY.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return new Integer(Integer.parseInt(result.get(0).toString().trim()));
        }
        return null;                        
    }
    
    public String getKeyStart() {
        List result = tag.get(TXXX_KEY_START.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            String key = result.get(0).toString().trim();
            if (StringUtil.isValid(key)) return key;
        }
        result = tag.get(TXXX_KEY.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;         
    }
    
    public String getKeyEnd() {
        List result = tag.get(TXXX_KEY_END.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            String key = result.get(0).toString().trim();
            if (StringUtil.isValid(key)) return key;
        }
        return null;
    }
    
    public String getLanguages() {
        List result = tag.get(TAG_LANGUAGES.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;                
    }
    
    public String getPublisher() {
        List result = tag.get(TAG_PUBLISHER.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;                
    }
    
    public Integer getRating() {
        List result = tag.get(TXXX_RATING.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return new Integer(Integer.parseInt(result.get(0).toString().trim()));
        }
        return null;                           
    }
    
    public String getRemix() {
        List result = tag.get(TAG_REMIX.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;     
    }
    
    public Float getReplayGain() {
        Float replayGain = null;
        List result = tag.get(TXXX_REPLAYGAIN_TRACK_GAIN.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            replayGain = StringUtil.parseNumericalPrefix(result.get(0).toString().trim());
        }
        return replayGain;                           
    }    
    
    public Integer getSizeInBytes() {
        List result = tag.get(TAG_SIZE_IN_BYTES.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return new Integer(Integer.parseInt(result.get(0).toString().trim()));
        }
        return null;     
    }
    
    public Vector<DegreeValue> getStyles() {
        Vector<DegreeValue> styles = new Vector<DegreeValue>();
        int s = 1;
        String identifier = TagUtil.getStyleTagId(s).toUpperCase();
        String degreeIdentifier = TagUtil.getStyleTagDegree(s).toUpperCase();
        while (tag.hasField(identifier)) {
            List result = tag.get(identifier);            
            if ((result != null) && (result.size() > 0)) {
                String style = result.get(0).toString();
                float degree = 1.0f;
                List degreeResult = tag.get(degreeIdentifier);
                if ((degreeResult != null) && (degreeResult.size() > 0))
                	degree = Float.parseFloat(degreeResult.get(0).toString());
                if (StringUtil.isValid(style))
                	styles.add(new DegreeValue(style, degree, DATA_SOURCE_FILE_TAGS));
            }
            ++s;
            identifier = TagUtil.getStyleTagId(s).toUpperCase();
            degreeIdentifier = TagUtil.getStyleTagDegree(s).toUpperCase();
        }        
        return styles;        
    }

    public Vector<DegreeValue> getTags() {
        Vector<DegreeValue> tags = new Vector<DegreeValue>();
        int s = 1;
        String identifier = TagUtil.getTagTagId(s).toUpperCase();
        String degreeIdentifier = TagUtil.getTagTagDegree(s).toUpperCase();
        while (tag.hasField(identifier)) {
            List result = tag.get(identifier);            
            if ((result != null) && (result.size() > 0)) {
                String tagName = result.get(0).toString();
                float degree = 1.0f;
                List degreeResult = tag.get(degreeIdentifier);
                if ((degreeResult != null) && (degreeResult.size() > 0))
                	degree = Float.parseFloat(degreeResult.get(0).toString());
                if (StringUtil.isValid(tagName))
                	tags.add(new DegreeValue(tagName, degree, DATA_SOURCE_FILE_TAGS));
            }
            ++s;
            identifier = TagUtil.getTagTagId(s).toUpperCase();
            degreeIdentifier = TagUtil.getTagTagDegree(s).toUpperCase();
        }        
        return tags;        
    }
    
    public String getTime() {
        try {
            List result = tag.get(TXXX_TIME.toUpperCase());
            if ((result != null) && (result.size() > 0)) {
                return result.get(0).toString().trim();
            }
        } catch (Exception e) {
            log.error("getTime(): error Exception", e);
        }
        return super.getTime();
    }
    public String getTimeSignature() {
        List result = tag.get(TXXX_TIME_SIGNATURE.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;           
    }
    
    public String getTitle() {
        String value = null;
        if (tag != null) {
            value = tag.getFirstTitle();
        }
        return value;                
    }
    
    public String getTrack() {
        String value = null;
        if (tag != null) {
            value = tag.getFirstTrack();
        }
        return value;        
        
    }
    
    public String getUserField(String fieldName) {
        List result = tag.get(TagUtil.convertToValidTagId(fieldName));
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;
    }

    public String getYear() {
        String value = null;
        if (tag != null) {
            value = tag.getFirstYear();
        }
        return value;                
    }
        
}
