package com.mixshare.rapid_evolution.audio.tags.readers.mp4;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.Mp4TagField;
import org.jaudiotagger.tag.mp4.field.Mp4TrackField;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import com.mixshare.rapid_evolution.audio.tags.readers.BaseTagReader;
import com.mixshare.rapid_evolution.audio.tags.util.AlbumCoverUtil;
import com.mixshare.rapid_evolution.music.duration.Duration;

public class JAudioMP4TagReader extends BaseTagReader {
    
    private static Logger log = Logger.getLogger(JAudioMP4TagReader.class);
    
    private AudioFile audioFile;
    private Tag tag;
    private Mp4Tag mp4tag;
    private VorbisCommentTag vorbistag;
    
    public JAudioMP4TagReader(String filename) {
        try {
            File file = new File(filename);
            audioFile = AudioFileIO.read(file);
            if (log.isDebugEnabled()) log.debug("JAudioMP4TagReader(): audioFile=" + audioFile);
            tag = audioFile.getTag();
            if (tag instanceof Mp4Tag)
                mp4tag = (Mp4Tag)tag;
            if (tag instanceof VorbisCommentTag)
                vorbistag = (VorbisCommentTag)tag;
        } catch (org.jaudiotagger.audio.exceptions.CannotReadException cre) {
        	if (log.isDebugEnabled())
        		log.debug("JAudioMP4TagReader(): cannot read exception=" + filename);
        	tag = null;
        } catch (Exception e) {
            log.error("JAudioMP4TagReader(): error Exception", e);
        }
    }
        
    public boolean isFileSupported() {
        return (tag != null);
    }
    
    public String getAlbum() {
        if (mp4tag != null)
            return mp4tag.getFirst(Mp4FieldKey.ALBUM);
        return "";
    }

    public String getArtist() {
        if (mp4tag != null)
            return mp4tag.getFirst(Mp4FieldKey.ARTIST);
        return "";
    }
    
    public Integer getBeatIntensity() {
        if (mp4tag != null) {
        	String bi = mp4tag.getFirst(TXXX_BEAT_INTENSITY);
        	if ((bi != null) && !bi.equals(""))
        		return Integer.parseInt(bi);
        }
        return null;            	
    }    
    
    public Integer getBpmAccuracy() {
        if (vorbistag != null) {
            String accuracy = vorbistag.getFirst(TXXX_BPM_ACCURACY);
            if (accuracy != null)
                return new Integer(Integer.parseInt(accuracy));
        }
        return null;
    }
    
    public Float getBpmStart() {
        Float result = null;
        if (mp4tag != null) {
            String bpm = mp4tag.getFirst(Mp4FieldKey.BPM);
            if ((bpm != null) && !bpm.equals(""))
                result = new Float(bpm);
            if (result == null) {
            	bpm = mp4tag.getFirst(Mp4FieldKey.TEMPO);
                if ((bpm != null) && !bpm.equals(""))
                    result = new Float(bpm);            	
            }
        }
        return result;
    }    
    
    public String getComments() {
        if (mp4tag != null)
            return mp4tag.getFirst(Mp4FieldKey.COMMENT);
        return "";        
    }
    
    public String getContentGroupDescription() {
        if (mp4tag != null)
            return mp4tag.getFirst(Mp4FieldKey.GROUPING);
        return "";            	
    }
    
    public String getKeyStart() {
    	if (mp4tag != null)
    		return mp4tag.getFirst(Mp4FieldKey.KEY);
    	return "";
    }
    
    public String getKeyEnd() {
    	List<TagField> tagFields = mp4tag.get(Mp4FieldKey.KEY);
    	if ((tagFields != null) && (tagFields.size() == 2))
    		return tagFields.get(1).toString();
    	return "";
    }
    
    public Integer getRating() {
    	if (mp4tag != null) {
    		String rating = mp4tag.getFirst(Mp4FieldKey.RATING);
    		if (rating != null)
    			return Integer.parseInt(rating);
    	}
    	return null;
    }    
    
    public String getLyrics() {
    	if (mp4tag != null)
    		return mp4tag.getFirst(Mp4FieldKey.LYRICS);
    	return null;
    }
    
    public String getPublisher() {
    	if (mp4tag != null) 
    		return mp4tag.getFirst(Mp4FieldKey.LABEL);
    	return null;
    }
    
    public double getTimeInSeconds() {
        if (audioFile != null) {
            int milliseconds = audioFile.getAudioHeader().getTrackLength();
            if (vorbistag != null)
                milliseconds *= 1000; // ogg files return seconds not milliseconds for some reason
            return ((double)milliseconds) / 1000.0;
        }
        return 0.0;        
    }
    
    public String getTime() {
        if (audioFile != null) {
            int milliseconds = audioFile.getAudioHeader().getTrackLength();
            if (vorbistag != null)
                milliseconds *= 1000; // ogg files return seconds not milliseconds for some reason
            String time = new Duration(milliseconds / 1000).getDurationAsString();                       
            log.debug("getTime(): time=" + time);
            return time;
        }
        return "";
    }
    
    public String getTitle() {
        if (mp4tag != null)
            return mp4tag.getFirst(Mp4FieldKey.TITLE);
        return "";        
    }
    
    public String getRemix() {
    	if (mp4tag != null)
    		return mp4tag.getFirst(Mp4FieldKey.REMIXER);
        return "";
    }
    
    public Float getReplayGain() {
    	if (mp4tag != null) {
    		String rating = mp4tag.getFirst(Mp4FieldKey.ITUNES_NORM);
    		if (rating != null)
    			return Float.parseFloat(rating);
    	}
    	return null;    	
    }
    
    public String getYear() {
        if (mp4tag != null)
            return mp4tag.getFirst(Mp4FieldKey.MM_ORIGINAL_YEAR);
        return "";                
    }
    
    public String getTrack() {
        if (mp4tag != null) {
            Mp4TrackField trackField = (Mp4TrackField)mp4tag.getFirstField(Mp4FieldKey.TRACK);
            if (trackField != null) {
                return String.valueOf(trackField.getTrackNo());         
            }        	
        }
        return "";        
    }
    
    public Integer getTotalTracks() {
        if (mp4tag != null) {
            Mp4TrackField trackField = (Mp4TrackField)mp4tag.getFirstField(Mp4FieldKey.TRACK);
            if (trackField != null) {
                Short trackTotal = trackField.getTrackTotal();         
                if (trackTotal != null)
                    return new Integer(trackTotal.shortValue());
            }
        }
        return null;
    }        
    
    public String getGenre() {    	
    	if (mp4tag != null) {
    		String genre = mp4tag.getFirst(Mp4FieldKey.GENRE_CUSTOM);
    		if (genre != null)
    			return genre;
    		genre = mp4tag.getFirst(Mp4FieldKey.GENRE);
    		if (genre != null)
    			return genre;
        }
        return "";                        
    }
    
    public String getAlbumCoverFilename() {
        String albumcover_filename = null;
        try {
            if (mp4tag != null) {
                Mp4TagField binaryField = mp4tag.getFirstField(Mp4FieldKey.ARTWORK);
                if (binaryField != null) {
                    if (log.isDebugEnabled())
                        log.debug("getAlbumCoverFilename(): binaryField=" + binaryField + ", raw content=" + binaryField.getRawContent());
                    BufferedImage bi = ImageIO.read(ImageIO.createImageInputStream(new ByteArrayInputStream(binaryField.getRawContent())));
                    albumcover_filename = AlbumCoverUtil.saveAlbumCover(this, bi);
                }
            }
            if (vorbistag != null) {
                byte[] newImageData = vorbistag.getArtworkBinaryData();                
                BufferedImage bi = ImageIO.read(ImageIO.createImageInputStream(new ByteArrayInputStream(newImageData)));
                albumcover_filename = AlbumCoverUtil.saveAlbumCover(this, bi);
            }
        } catch (Exception e) {
            log.error("getAlbumCoverFilename(): error Exception", e);
        }
        return albumcover_filename;
    }
    
}
