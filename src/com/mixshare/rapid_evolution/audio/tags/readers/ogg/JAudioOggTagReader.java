package com.mixshare.rapid_evolution.audio.tags.readers.ogg;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;
import org.jaudiotagger.tag.vorbiscomment.util.Base64Coder;

import com.mixshare.rapid_evolution.audio.tags.readers.BaseTagReader;
import com.mixshare.rapid_evolution.audio.tags.util.AlbumCoverUtil;
import com.mixshare.rapid_evolution.audio.tags.util.TagUtil;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.util.StringUtil;

public class JAudioOggTagReader extends BaseTagReader {
    
    private static Logger log = Logger.getLogger(JAudioOggTagReader.class);
        
    protected String filename = null;
    protected AudioFile audiofile = null;
    protected VorbisCommentTag tag = null;
    
    public JAudioOggTagReader() { }
    public JAudioOggTagReader(String filename) {
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("JAudioOggTagReader(): filename=" + filename);            
            audiofile = AudioFileIO.read(new File(filename));            
            if (audiofile != null) {
            	tag = (VorbisCommentTag)audiofile.getTag();
                if (log.isDebugEnabled()) log.debug("JAudioOggTagReader(): tag=" + tag);
            }
        } catch (Exception e) {
            log.error("JAudioOggTagReader(): error Exception", e);
        }
    }
    
    public boolean isFileSupported() {
        return (tag != null);
    }    
    
    public String getAlbum() {
        String value = null;
        if (tag != null) {
            value = tag.getFirst(VorbisCommentFieldKey.ALBUM);
        }
        return value;
    }
    
    public String getArtist() {
        String value = null;
        if (tag != null) {
            value = tag.getFirst(VorbisCommentFieldKey.ARTIST);
        }
        return value;        
    }

    public Integer getBeatIntensity() {
        List result = tag.getFields(TXXX_BEAT_INTENSITY.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return new Integer(Integer.parseInt(result.get(0).toString().trim()));
        }
        return null;        
    }
    
    public Integer getBpmAccuracy() {
        List result = tag.getFields(TXXX_BPM_ACCURACY.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return new Integer(Integer.parseInt(result.get(0).toString().trim()));
        }
        return null;                
    }
    
    public Float getBpmStart() {
        List result = tag.getFields(TXXX_BPM_START.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            try {
                return new Float(Float.parseFloat(result.get(0).toString().trim()));
            } catch (NumberFormatException e) {
                log.debug("getBpmStart(): error number format exception=" + result);
            }
        }
    	String firstBpm = tag.getFirst(VorbisCommentFieldKey.BPM);
    	if ((firstBpm != null) && !firstBpm.equals(""))
    		return Float.parseFloat(firstBpm);
    	result = tag.getFields(TXXX_BPM.toUpperCase());
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
        List result = tag.getFields(TXXX_BPM_END.toUpperCase());
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
            value = tag.getFirst(VorbisCommentFieldKey.COMMENT);
        }
        return value;        
    }
    
    public String getContentGroupDescription() {
        String value = null;
        if (tag != null) {
            value = tag.getFirst(VorbisCommentFieldKey.GROUPING);            
        }
        return value;        
    }
    
    public String getContentType() {
        List result = tag.getFields(TAG_CONTENT_TYPE.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;                        
    }
    
    public String getEncodedBy() {
        List result = tag.getFields(TAG_ENCODED_BY.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;                        
    }

    public String getFilename() {
        return filename;
    }

    public String getFileType() {
        List result = tag.getFields(TAG_FILE_TYPE.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;        
    }
    
    public String getGenre() {
        String value = null;
        if (tag != null) {
            value = tag.getFirst(VorbisCommentFieldKey.GENRE);
        }
        return value;                
    }
    
    public Integer getKeyAccuracy() {
        List result = tag.getFields(TXXX_KEY_ACCURACY.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return new Integer(Integer.parseInt(result.get(0).toString().trim()));
        }
        return null;                        
    }
    
    public String getKeyStart() {
    	String firstKey = tag.getFirst(VorbisCommentFieldKey.KEY);
    	if ((firstKey != null) && !firstKey.equals(""))
    		return firstKey;    	
        List result = tag.getFields(TXXX_KEY_START.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            String key = result.get(0).toString().trim();
            if (StringUtil.isValid(key)) return key;
        }
        result = tag.getFields(TXXX_KEY.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;         
    }
    
    public String getKeyEnd() {
        List result = tag.getFields(TXXX_KEY_END.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            String key = result.get(0).toString().trim();
            if (StringUtil.isValid(key)) return key;
        }
        return null;
    }
    
    public String getLanguages() {
        List result = tag.getFields(TAG_LANGUAGES.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;                
    }
    
    public String getLyrics() {
        String value = null;
        if (tag != null) {
            value = tag.getFirst(VorbisCommentFieldKey.LYRICS);
        }
        return value;   	
    }
    
    public String getPublisher() {
        String value = null;
        if (tag != null) {
            value = tag.getFirst(VorbisCommentFieldKey.LABEL);
        }
        return value;                    
    }
    
    public Integer getRating() {
        List result = tag.getFields(TXXX_RATING.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return new Integer(Integer.parseInt(result.get(0).toString().trim()));
        }
        return null;                           
    }
    
    public String getRemix() {
        String value = null;
        if (tag != null) {
            value = tag.getFirst(VorbisCommentFieldKey.REMIXER);
        }
        return value;        
    }
    
    public Float getReplayGain() {
        Float replayGain = null;
        List result = tag.getFields(TXXX_REPLAYGAIN_TRACK_GAIN.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            replayGain = StringUtil.parseNumericalPrefix(result.get(0).toString().trim());
        }
        return replayGain;                           
    }    
    
    public Integer getSizeInBytes() {
        List result = tag.getFields(TAG_SIZE_IN_BYTES.toUpperCase());
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
            List result = tag.getFields(identifier);            
            if ((result != null) && (result.size() > 0)) {
                String style = result.get(0).toString();
                float degree = 1.0f;
                List degreeResult = tag.getFields(degreeIdentifier);
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
            List result = tag.getFields(identifier);            
            if ((result != null) && (result.size() > 0)) {
                String tagName = result.get(0).toString();
                float degree = 1.0f;
                List degreeResult = tag.getFields(degreeIdentifier);
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
            List result = tag.getFields(TXXX_TIME.toUpperCase());
            if ((result != null) && (result.size() > 0)) {
                return result.get(0).toString().trim();
            }
        } catch (Exception e) {
            log.error("getTime(): error Exception", e);
        }
        return super.getTime();
    }
    public String getTimeSignature() {
        List result = tag.getFields(TXXX_TIME_SIGNATURE.toUpperCase());
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;           
    }
    
    public String getTitle() {
        String value = null;
        if (tag != null) {
            value = tag.getFirst(VorbisCommentFieldKey.TITLE);
        }
        return value;                
    }
    
    public Integer getTotalTracks() {
        String value = null;
        if (tag != null) {
            value = tag.getFirst(VorbisCommentFieldKey.TRACKTOTAL);
        }
        if (value != null)
        	return Integer.parseInt(value);
        return null;
    }
    
    public String getTrack() {
        String value = null;
        if (tag != null) {
            value = tag.getFirst(VorbisCommentFieldKey.TRACKNUMBER);
        }
        return value;        
        
    }
    
    public String getUserField(String fieldName) {
        List result = tag.getFields(TagUtil.convertToValidTagId(fieldName));
        if ((result != null) && (result.size() > 0)) {
            return result.get(0).toString().trim();
        }
        return null;
    }

    public String getYear() {
        String value = null;
        if (tag != null) {
            value = tag.getFirst(VorbisCommentFieldKey.DATE);
        }
        return value;                
    }
    
    public String getAlbumCoverFilename() {
    	String albumcover_filename = null;
    	try {
	    	if (tag != null) {
		    	String imageData = tag.getFirst(VorbisCommentFieldKey.COVERART);
		    	String mimetype  = tag.getFirst(VorbisCommentFieldKey.COVERARTMIME);
		    	BufferedImage bi = ImageIO.read(ImageIO.createImageInputStream(new ByteArrayInputStream(Base64Coder.decode(imageData.toCharArray()))));
		    	albumcover_filename = AlbumCoverUtil.saveAlbumCover(this, bi);
	    	}
    	} catch (Exception e) {
    		log.error("getAlbumCoverFilename(): error", e);
    	}
    	return albumcover_filename;
    }
    
}
