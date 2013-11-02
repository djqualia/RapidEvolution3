package com.mixshare.rapid_evolution.audio.tags.writers;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import com.mixshare.rapid_evolution.audio.AudioFileTypes;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.util.OSHelper;

public class JAudioGenericTagWriter extends BaseTagWriter implements AudioFileTypes {
    
    private static Logger log = Logger.getLogger(JAudioGenericTagWriter.class);
        
    protected String filename = null;
    protected AudioFile audiofile = null;
    protected Tag tag = null;
    
    public JAudioGenericTagWriter() { }
    public JAudioGenericTagWriter(String filename, int mode) {
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("JAudioGenericTagWriter(): filename=" + filename);            
            audiofile = AudioFileIO.read(new File(filename));
            if (audiofile != null) {
                tag = audiofile.getTag();
                if (log.isDebugEnabled()) log.debug("JAudioGenericTagWriter(): tag=" + tag);
            }
        } catch (org.jaudiotagger.audio.exceptions.CannotReadException e) {
        	if (log.isDebugEnabled())
        		log.debug("JAudioGenericTagWriter(): cannot read=" + e);
        } catch (Exception e) {
            log.error("JAudioGenericTagWriter(): error Exception", e);
        }
    }
    
    public boolean save() {
        boolean success = false;
        try {
            if (audiofile != null) {
            	audiofile.commit();                
                success = true;
            }
        } catch (Exception e) {
            log.error("save(): error saving tag, filename=" + filename, e);
        }
        return success;
    }
    
    public void setAlbum(String album) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.ALBUM, album));
    		}
    	} catch (Exception e) { }
    }
    
    public void setAlbumCover(String filename, String album) {
        try {
            if (tag != null) {
            	tag.createField(Artwork.createArtworkFromFile(new File(OSHelper.getWorkingDirectory() + "/" + filename)));
            	/*
            	RandomAccessFile imageFile = new RandomAccessFile(new File(filename),"r");
            	byte[] imagedata = new byte[(int)imageFile.length()];
            	imageFile.read(imagedata);
            	char[] testdata = Base64Coder.encode(imagedata);
            	String base64image = new String(testdata);
            	tag.setField(tag.createField(FieldKey.COVER_ART, base64image));
            	*/
            }
        } catch (Exception e) {
            log.error("setAlbumCover(): error Exception", e);
        }
    }    
    
    public void setArtist(String artist) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.ARTIST, artist));
    		}
    	} catch (Exception e) { }
    }
    
    public void setBeatIntensity(int beat_intensity) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TXXX_BEAT_INTENSITY.toUpperCase(), String.valueOf(beat_intensity)));
    		}
    	} catch (Exception e) { }
    }
    
    public void setBpm(int bpm) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.BPM, String.valueOf(bpm)));
    		}
    	} catch (Exception e) { }
    }
    
    public void setBpmFloat(float bpm) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.BPM, String.valueOf(bpm)));
    		}
    	} catch (Exception e) { }
    }
    
    public void setBpmAccuracy(int accuracy) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TXXX_BPM_ACCURACY.toUpperCase(), String.valueOf(accuracy)));
    		}
    	} catch (Exception e) { }
    }
    
    public void setBpmStart(float start_bpm) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TXXX_BPM_START.toUpperCase(), String.valueOf(start_bpm)));
    		}
    	} catch (Exception e) { }
    }
    
    public void setBpmEnd(float end_bpm) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TXXX_BPM_END.toUpperCase(), String.valueOf(end_bpm)));
    		}
    	} catch (Exception e) { }                                    
    }
    
    public void setComments(String comments) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.COMMENT, comments));
    		}
    	} catch (Exception e) { }
    }
    
    public void setContentGroupDescription(String content_group_description) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.GROUPING, content_group_description));
    		}
    	} catch (Exception e) { }
    }
    
    public void setContentType(String content_type) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TAG_CONTENT_TYPE.toUpperCase(), content_type));
    		}
    	} catch (Exception e) { } 
    }
    
    public void setEncodedBy(String encoded_by) { }
    
    public void setFileType(String file_type) { }
    
    public void setGenre(String genre) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.GENRE, genre));
    		}
    	} catch (Exception e) { }
    }
    
    public void setKey(String key) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.KEY, key));
    		}
    	} catch (Exception e) { }                                                                    
    }
    
    public void setKeyAccuracy(int accuracy) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TXXX_KEY_ACCURACY.toUpperCase(), String.valueOf(accuracy)));
    		}
    	} catch (Exception e) { }
    }
    
    public void setKeyStart(String start_key) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TXXX_KEY_START.toUpperCase(), String.valueOf(start_key)));
    		}
    	} catch (Exception e) { }
    }
    
    public void setKeyEnd(String end_key) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TXXX_KEY_END.toUpperCase(), String.valueOf(end_key)));
    		}
    	} catch (Exception e) { }
    }
    
    public void setLanguages(String languages) { }
    
    public void setLyrics(String lyrics) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.LYRICS, lyrics));
    		}
    	} catch (Exception e) { }    
    }
    
    public void setPublisher(String publisher) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.RECORD_LABEL, publisher));
    		}
    	} catch (Exception e) { }    
    }
    
    public void setRating(int rating) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TXXX_RATING.toUpperCase(), String.valueOf(rating)));
    		}
    	} catch (Exception e) { }
    }
    
    public void setRemix(String remix) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.REMIXER, remix));
    		}
    	} catch (Exception e) { }                                                                                                           
    }
    
    public void setReplayGain(float value) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TXXX_REPLAYGAIN_TRACK_GAIN.toUpperCase(), String.valueOf(value)));
    		}
    	} catch (Exception e) { }
    }        
    
    public void setSizeInBytes(int size) { }
    
    public void setStyles(Vector<DegreeValue> styles) { }
    
    public void setTime(String time) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TXXX_TIME.toUpperCase(), time));
    		}
    	} catch (Exception e) { }
    }
    
    public void setTimeSignature(String time_sig) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TXXX_TIME_SIGNATURE.toUpperCase(), time_sig));
    		}
    	} catch (Exception e) { }
    }
    
    public void setTitle(String title) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.TITLE, title));
    		}
    	} catch (Exception e) { }         
    }
    
    public void setTrack(String track, Integer total_tracks) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.TRACK, track));
    			if (total_tracks != null)
    				tag.setField(tag.createField(FieldKey.TRACK_TOTAL, String.valueOf(total_tracks)));
    		}
    	} catch (Exception e) { }   
    }
    
    public void setUserField(String fieldName, String user1) {
    	try {
    		if (tag != null) {
    			//tag.setField(tag.createField(TagUtil.convertToValidTagId(fieldName), user1));
    		}
    	} catch (Exception e) { }  
    }

    public void setYear(String year) {
    	try {
    		if (tag != null) {
    			tag.setField(tag.createField(FieldKey.YEAR, year));
    		}
    	} catch (Exception e) { }    
    }
    
}
