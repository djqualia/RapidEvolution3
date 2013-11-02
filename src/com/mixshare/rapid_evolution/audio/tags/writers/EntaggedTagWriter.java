package com.mixshare.rapid_evolution.audio.tags.writers;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey;
import org.jaudiotagger.tag.vorbiscomment.util.Base64Coder;

import com.mixshare.rapid_evolution.audio.AudioFileTypes;
import com.mixshare.rapid_evolution.audio.tags.util.TagUtil;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

import entagged.audioformats.AudioFile;
import entagged.audioformats.AudioFileIO;
import entagged.audioformats.Tag;
import entagged.audioformats.ogg.util.OggTagField;

public class EntaggedTagWriter extends BaseTagWriter implements AudioFileTypes {
    
    private static Logger log = Logger.getLogger(EntaggedTagWriter.class);
        
    private String filename = null;
    private AudioFile audiofile = null;
    private Tag tag = null;
    private boolean write_extended_info = false;
    
    public EntaggedTagWriter(String filename, int mode) {
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("EntaggedTagWriter(): filename=" + filename);            
            audiofile = AudioFileIO.read(new File(filename));
            if (audiofile != null) {
                tag = audiofile.getTag();
                if (log.isDebugEnabled()) log.debug("EntaggedTagWriter(): tag=" + tag);
            }
            int file_type = AudioUtil.getAudioFileType(filename);
            if ((file_type == AUDIO_FILE_TYPE_FLAC) || (file_type == AUDIO_FILE_TYPE_OGG)) {
                write_extended_info = true;
            }
        } catch (Exception e) {
            log.error("EntaggedTagWriter(): error Exception", e);
        }
    }
    
    public boolean save() {
        boolean success = false;
        try {
            if (audiofile != null) {
                AudioFileIO.write(audiofile);
                success = true;
            }
        } catch (Exception e) {
            log.error("save(): error saving tag, filename=" + filename, e);
        }
        return success;
    }
    
    public void setAlbum(String album) {
        if (tag != null) {
            tag.setAlbum(album);
        }
    }
    
    /*
    public void setAlbumCover(String filename, String album) {
        try {
            if (tag != null) {
            	RandomAccessFile imageFile = new RandomAccessFile(new File(filename),"r");
            	byte[] imagedata = new byte[(int)imageFile.length()];
            	imageFile.read(imagedata);
            	char[] testdata = Base64Coder.encode(imagedata);
            	String base64image = new String(testdata);
            	tag.set(new OggTagField(TAG_COVER_ART, base64image));
            	tag.set(new OggTagField(TAG_COVER_ART_MIME, "image/png"));
            }
        } catch (Exception e) {
            log.error("setAlbumCover(): error Exception", e);
        }
    }     
    */
    
    public void setArtist(String artist) {
        if (tag != null) {
            tag.setArtist(artist);
        }        
    }
    
    public void setBeatIntensity(int beat_intensity) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_BEAT_INTENSITY.toUpperCase(), String.valueOf(beat_intensity)));
        }        
    }
    
    public void setBpm(int bpm) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_BPM.toUpperCase(), String.valueOf(bpm)));
        }                
    }
    
    public void setBpmFloat(float bpm) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_BPM.toUpperCase(), String.valueOf(bpm)));            
        }
    }
    
    public void setBpmAccuracy(int accuracy) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_BPM_ACCURACY.toUpperCase(), String.valueOf(accuracy)));
        }                        
    }
    
    public void setBpmStart(float start_bpm) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_BPM_START.toUpperCase(), String.valueOf(start_bpm)));
        }                                
    }
    
    public void setBpmEnd(float end_bpm) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_BPM_END.toUpperCase(), String.valueOf(end_bpm)));
        }                                        
    }
    
    public void setComments(String comments) {
        if (tag != null) {
            tag.setComment(comments);
            tag.set(new OggTagField(TAG_COMMENTS.toUpperCase(), comments));            
        }                
    }
    
    public void setContentGroupDescription(String content_group_description) {
        if (write_extended_info) {
            tag.set(new OggTagField(TAG_CONTENT_GROUP_DESCRIPTION.toUpperCase(), content_group_description));
        }                                        
    }
    
    public void setContentType(String content_type) {
        if (write_extended_info) {
            tag.set(new OggTagField(TAG_CONTENT_TYPE.toUpperCase(), content_type));
        }                                                
    }
    
    public void setEncodedBy(String encoded_by) {
        if (write_extended_info) {
            tag.set(new OggTagField(TAG_ENCODED_BY.toUpperCase(), encoded_by));
        }                                                        
    }
    
    public void setFileType(String file_type) {
        if (write_extended_info) {
            tag.set(new OggTagField(TAG_FILE_TYPE.toUpperCase(), file_type));
        }                                                                
    }
    
    public void setGenre(String genre) {
        if (tag != null) {
            tag.setGenre(genre);
        }
    }
    
    public void setKey(String key) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_INITIALKEY.toUpperCase(), key));
            tag.set(new OggTagField(TXXX_KEY.toUpperCase(), key));
        }                                                                        
    }
    
    public void setKeyAccuracy(int accuracy) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_KEY_ACCURACY.toUpperCase(), String.valueOf(accuracy)));
        }                                                                                
    }
    
    public void setKeyStart(String start_key) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_KEY_START.toUpperCase(), start_key));
        }                                                                                
    }
    
    public void setKeyEnd(String end_key) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_KEY_END.toUpperCase(), end_key));
        }        
    }
    
    public void setLanguages(String languages) {
        if (write_extended_info) {
            tag.set(new OggTagField(TAG_LANGUAGES.toUpperCase(), languages));
        }                                                                                
    }
    public void setPublisher(String publisher) {
        if (write_extended_info) {
            tag.set(new OggTagField(TAG_PUBLISHER.toUpperCase(), publisher));
        }                                                                                        
    }
    
    public void setRating(int rating) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_RATING.toUpperCase(), String.valueOf(rating)));
        }                                                                                                
    }
    
    public void setRemix(String remix) {
        if (write_extended_info) {
            tag.set(new OggTagField(TAG_REMIX.toUpperCase(), remix));
        }                                                                                                        
    }
    
    public void setReplayGain(float value) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_REPLAYGAIN_TRACK_GAIN.toUpperCase(), String.valueOf(value) + " dB"));
        }                                                                                                
    }        
    
    public void setSizeInBytes(int size) {
        if (write_extended_info) {
            tag.set(new OggTagField(TAG_SIZE_IN_BYTES.toUpperCase(), String.valueOf(size)));        
        }
    }
    
    public void setStyles(Vector<DegreeValue> styles) {
        if (write_extended_info) {
            // remove existing styles
            int s = 1;
            String identifier = TagUtil.getStyleTagId(s).toUpperCase();
            while (tag.hasField(identifier)) {
                tag.set(new OggTagField(identifier, ""));
                ++s;
                identifier = TagUtil.getStyleTagId(s).toUpperCase();
            }
            // set styles
            for (s = 0; s < styles.size(); ++s) {
                String style = styles.get(s).getName();
                float degree = styles.get(s).getPercentage();
                identifier = TagUtil.getStyleTagId(s + 1);
                tag.set(new OggTagField(identifier.toUpperCase(), style));
                identifier = TagUtil.getStyleTagDegree(s + 1);
                tag.set(new OggTagField(identifier.toUpperCase(), String.valueOf(degree)));
                
            }
        }
    }

    public void setTags(Vector<DegreeValue> tags) {
        if (write_extended_info) {
            // remove existing tags
            int s = 1;
            String identifier = TagUtil.getTagTagId(s).toUpperCase();
            while (tag.hasField(identifier)) {
                tag.set(new OggTagField(identifier, ""));
                ++s;
                identifier = TagUtil.getTagTagId(s).toUpperCase();
            }
            // set tags
            for (s = 0; s < tags.size(); ++s) {
                String tagName = tags.get(s).getName();
                float degree = tags.get(s).getPercentage();
                identifier = TagUtil.getTagTagId(s + 1);
                tag.set(new OggTagField(identifier.toUpperCase(), tagName));
                identifier = TagUtil.getTagTagDegree(s + 1);
                tag.set(new OggTagField(identifier.toUpperCase(), String.valueOf(degree)));
                
            }
        }
    }
    
    public void setTime(String time) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_TIME.toUpperCase(), time));
        }        
    }
    
    public void setTimeSignature(String time_sig) {
        if (write_extended_info) {
            tag.set(new OggTagField(TXXX_TIME_SIGNATURE.toUpperCase(), time_sig));
        }        
    }
    
    public void setTitle(String title) {
        if (tag != null) {
            tag.setTitle(title);
        }        
    }
    
    public void setTrack(String track, Integer total_tracks) {
        if (tag != null) {
            if (total_tracks != null)
                tag.setTrack(track + "/" + total_tracks);
            else
                tag.setTrack(track);            
        }
    }
    
    public void setUserField(String fieldName, String user1) {
        if (write_extended_info) {
            tag.set(new OggTagField(TagUtil.convertToValidTagId(fieldName), user1));
        }
    }

    public void setYear(String year) {
        if (tag != null) {
            tag.setYear(year);
        }
    }
    
}
