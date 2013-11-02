package com.mixshare.rapid_evolution.audio.tags.writers.mp4;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Vector;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4TrackField;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import com.mixshare.rapid_evolution.audio.tags.writers.BaseTagWriter;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.util.OSHelper;

/**
 * This will support writing MP4 and OGGs, however, more coding needs to be done before used for OGGs.
 */
public class JAudioMP4TagWriter extends BaseTagWriter {

    private static Logger log = Logger.getLogger(JAudioMP4TagWriter.class);
    
    private String filename = null;
    private AudioFile audioFile = null;
    private Tag tag = null;
    private Mp4Tag mp4tag;
    private VorbisCommentTag vorbistag;

    public JAudioMP4TagWriter(String filename, int mode) {
        try {
            MP3File.logger.setLevel(Level.OFF);
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("JAudioTagWriter(): filename=" + filename);
            File file = new File(filename);
            audioFile = AudioFileIO.read(file);
            tag = audioFile.getTag();
            if (tag instanceof Mp4Tag)
                mp4tag = (Mp4Tag)tag;
            if (tag instanceof VorbisCommentTag)
                vorbistag = (VorbisCommentTag)tag;
            
        } catch (Exception e) {
            log.error("JAudioTagWriter(): error Exception, filename=" + filename, e);
        }
    }
    
    public boolean save() {
        boolean success = false;
        try {
            if (audioFile != null) {
                audioFile.commit();
                success = true;                
            }
        } catch (Exception e) {
            log.error("save(): could not save tag to filename=" + filename, e);
        }
        return success;        
    }    
    
    public void setAlbum(String album) {
        try {
            if (mp4tag != null) {
            	mp4tag.setField(mp4tag.createField(Mp4FieldKey.ALBUM, album));
            }
        } catch (Exception e) {
            log.error("setAlbum(): error Exception", e);
        }
    }
    
    public void setAlbumCover(String filename, String album) {
        try {
            if (mp4tag != null) {
                RandomAccessFile imageFile = new RandomAccessFile(OSHelper.getWorkingDirectory() + "/" + filename,"r");
                byte[] imagedata = new byte[(int)imageFile.length()];
                imageFile.read(imagedata);                
                mp4tag.setField(mp4tag.createArtworkField(imagedata));            
            }
        } catch (Exception e) {
            log.error("setAlbumCover(): error Exception", e);
        }
    }
    
    public void setArtist(String artist) {
        try {
            if (mp4tag != null) {
            	mp4tag.setField(mp4tag.createField(Mp4FieldKey.ARTIST, artist));
            }
        } catch (Exception e) {
            log.error("setArtist(): error Exception", e);
        }
    }
    
    public void setBeatIntensity(int beat_intensity) {
        try {
            if (mp4tag != null) {
            	//mp4tag.setField(new Mp4TagTextField(TXXX_BEAT_INTENSITY, String.valueOf(beat_intensity)));
            }
        } catch (Exception e) {
            log.error("setBeatIntensity(): error Exception", e);
        }
    }
    
    public void setBpm(int bpm) {
        try {
            if (mp4tag != null) {
            	mp4tag.setField(mp4tag.createField(Mp4FieldKey.BPM, String.valueOf(bpm)));        
            }
        } catch (Exception e) {
            log.error("setBpm(): error Exception", e);
        }
    }
    
    public void setBpmFloat(float bpm) {        
        try {
            if (mp4tag != null) {
            	mp4tag.setField(mp4tag.createField(Mp4FieldKey.TEMPO, String.valueOf(bpm)));        
            }
        } catch (Exception e) {
            log.error("setBpm(): error Exception", e);
        }
    }
    
    public void setBpmAccuracy(int accuracy) {
        //if (vorbistag != null) {
            //vorbistag.set(vorbistag.createTagField(BaseTagWriter.TXXX_BPM_ACCURACY, String.valueOf(accuracy)));
        //}
    }
    
    public void setBpmStart(float start_bpm) {
        try {
            if (mp4tag != null) {
            	//mp4tag.setField(new Mp4TagTextNumberField(TXXX_BPM_START, String.valueOf(start_bpm)));        
            }
        } catch (Exception e) {
            log.error("setBpm(): error Exception", e);
        }    	
    }

    public void setBpmEnd(float end_bpm) {
    }
    
    public void setComments(String comments) {
        try {
            if (mp4tag != null) {
            	mp4tag.setField(mp4tag.createField(Mp4FieldKey.COMMENT, comments));
            }
        } catch (Exception e) {
            log.error("setComments(): error Exception", e);
        }
    }
    
    public void setContentGroupDescription(String content_group_description) {
        try {
            if (mp4tag != null) {
            	mp4tag.setField(mp4tag.createField(Mp4FieldKey.GROUPING, content_group_description));
            }
        } catch (Exception e) {
            log.error("setContentGroupDescription(): error Exception", e);
        }
    }
    
    public void setContentType(String content_type) {
        try {
            if (mp4tag != null) {
            	mp4tag.createField(Mp4FieldKey.CONTENT_TYPE, content_type);
            }
        } catch (Exception e) {
            log.error("setContentType(): error Exception", e);
        }
    }
    
    public void setEncodedBy(String encoded_by) {
        try {
            if (mp4tag != null) {
            	mp4tag.createField(Mp4FieldKey.ENCODER, encoded_by);
            }
        } catch (Exception e) {
            log.error("setEncodedBy(): error Exception", e);
        }
    }
    
    public void setFileType(String file_type) {
    
    }
    
    public void setGenre(String genre) {
        try {
            if (mp4tag != null) {
            	mp4tag.setField(mp4tag.createField(Mp4FieldKey.GENRE_CUSTOM, genre));
            	mp4tag.setField(mp4tag.createField(Mp4FieldKey.GENRE, genre));
            }
        } catch (Exception e) {
            log.error("setGenre(): error Exception", e);
        }
    }
    
    public void setKeyAccuracy(int accuracy) {
    
    }

    public void setKey(String key) {
        try {
        	if (mp4tag != null) {
        		mp4tag.setField(mp4tag.createField(Mp4FieldKey.KEY, key));
        	}      
        } catch (Exception e) {
            log.error("setKey(): error Exception", e);
        }
    }
    
    public void setKeyStart(String start_key) {
        try {
        	if (mp4tag != null) {
        		mp4tag.setField(mp4tag.createField(Mp4FieldKey.KEY, start_key));
        	}      
        } catch (Exception e) {
            log.error("setKeyStart(): error Exception", e);
        }       
    }
    
    public void setKeyEnd(String end_key) {
        try {
        	if ((mp4tag != null) && (end_key.length() > 0)) {
        		mp4tag.addField(mp4tag.createField(Mp4FieldKey.KEY, end_key));
        	}      
        } catch (Exception e) {
            log.error("setKeyEnd(): error Exception", e);
        }       
    }
    
    public void setLanguages(String languages) {
    	
    }
    
    public void setLyrics(String lyrics) {
        try {
        	if (mp4tag != null) {
        		mp4tag.setField(mp4tag.createField(Mp4FieldKey.LYRICS, lyrics));
        	}      
        } catch (Exception e) {
            log.error("setPublisher(): error Exception", e);
        }    	
    }
    
    public void setPublisher(String publisher) {
        try {
        	if (mp4tag != null) {
        		mp4tag.setField(mp4tag.createField(Mp4FieldKey.LABEL, publisher));
        	}      
        } catch (Exception e) {
            log.error("setPublisher(): error Exception", e);
        }
    }
    
    public void setRating(int rating) {
        try {
        	if (mp4tag != null)
        		mp4tag.setField(mp4tag.createField(Mp4FieldKey.RATING, String.valueOf(rating)));        	
        } catch (Exception e) {
            log.error("setRating(): error Exception", e);
        }
    }
    
    public void setRemix(String remix) {
        try {
        	if (mp4tag != null)
        		mp4tag.setField(mp4tag.createField(Mp4FieldKey.REMIXER, remix));        	
        } catch (Exception e) {
            log.error("setRemix(): error Exception", e);
        }
    }
    
    public void setReplayGain(float value) {
        try {
        	if (mp4tag != null)
        		mp4tag.setField(mp4tag.createField(Mp4FieldKey.ITUNES_NORM, String.valueOf(value)));        	
        } catch (Exception e) {
            log.error("setRating(): error Exception", e);
        }    	
    }
    
    public void setSizeInBytes(int size) {
       
    }
    
    public void setStyles(Vector<DegreeValue> styles) {
        removeStyles();
        if (styles != null) {
            for (int s = 0; s < styles.size(); ++s) {
                setStyle(styles.get(s).getName(), styles.get(s).getPercentage(), s + 1);
            }
        }
    }

    public void setTags(Vector<DegreeValue> tags) {
        removeTags();
        if (tags != null) {
            for (int s = 0; s < tags.size(); ++s) {
                setTag(tags.get(s).getName(), tags.get(s).getPercentage(), s + 1);
            }
        }
    }
    
    public void setTime(String time) {    }
    
    public void setTimeSignature(String time_sig) {

    }
    
    public void setTitle(String title) {
        try {
            if (mp4tag != null) {
            	mp4tag.setField(mp4tag.createField(Mp4FieldKey.TITLE, title));
            }
        } catch (Exception e) {
            log.error("setTitle(): error Exception", e);
        }
    }
    
    public void setTrack(String track, Integer total_tracks) {
        try {
            if (mp4tag != null) {
            	mp4tag.setField(mp4tag.createField(Mp4FieldKey.TRACK, track));
                if (total_tracks != null)
                	mp4tag.setField(new Mp4TrackField(Integer.parseInt(track),total_tracks));                
            }
        } catch (Exception e) {
            log.error("setTrack(): error Exception", e);
        }                    
    }
    
    public void setUser1(String value) {
    
    }
    
    public void setUser2(String value) {
     
    }
    
    public void setUser3(String value) {
      
    }
    
    public void setUser4(String value) {
       
    }
    
    public void setYear(String year) {
        try {
            if (mp4tag != null) {
            	//mp4tag.setField(mp4tag.createField(Mp4FieldKey.MM_ORIGINAL_YEAR, year));
            }        	
        } catch (Exception e) {
            log.error("setYear(): error Exception", e);
        }  
    }    
    
    private void removeStyles() {     
    }
    
    private void setStyle(String style, float degree, int style_number) {
    }    
    
    private void removeTags() {        
    }
    
    private void setTag(String tag, float degree, int tag_number) {
    }    
    
}
