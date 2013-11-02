package com.mixshare.rapid_evolution.audio.tags.readers.mp3;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.farng.mp3.AbstractMP3FragmentBody;
import org.farng.mp3.MP3File;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.AbstractID3v2Frame;
import org.farng.mp3.id3.FrameBodyAPIC;
import org.farng.mp3.id3.FrameBodyTBPM;
import org.farng.mp3.id3.FrameBodyTCON;
import org.farng.mp3.id3.FrameBodyTENC;
import org.farng.mp3.id3.FrameBodyTFLT;
import org.farng.mp3.id3.FrameBodyTIME;
import org.farng.mp3.id3.FrameBodyTIT1;
import org.farng.mp3.id3.FrameBodyTKEY;
import org.farng.mp3.id3.FrameBodyTLAN;
import org.farng.mp3.id3.FrameBodyTLEN;
import org.farng.mp3.id3.FrameBodyTOPE;
import org.farng.mp3.id3.FrameBodyTPE4;
import org.farng.mp3.id3.FrameBodyTPUB;
import org.farng.mp3.id3.FrameBodyTSIZ;
import org.farng.mp3.id3.FrameBodyTXXX;
import org.farng.mp3.id3.ID3v1;
import org.farng.mp3.object.ObjectByteArraySizeTerminated;

import com.mixshare.rapid_evolution.audio.tags.readers.BaseTagReader;
import com.mixshare.rapid_evolution.audio.tags.util.AlbumCoverUtil;
import com.mixshare.rapid_evolution.audio.tags.util.TagUtil;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.util.StringUtil;

public class JavaID3TagReader extends BaseTagReader {

    private static Logger log = Logger.getLogger(JavaID3TagReader.class);
        
    private String filename = null;
    private ID3v1 id3v1 = null;
    private AbstractID3v2 id3v2 = null;
    
    public JavaID3TagReader(String filename) {
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("JavaID3TagReader(): filename=" + filename);            
            File sourceFile = new File(filename);
            MP3File mp3file = new MP3File(sourceFile);
            id3v1 = mp3file.getID3v1Tag();
            if (log.isDebugEnabled()) log.debug("JavaID3TagReader(): id3v1=" + id3v1);
            id3v2 = mp3file.getID3v2Tag();
            if (log.isDebugEnabled()) log.debug("JavaID3TagReader(): id3v2=" + id3v2);
            if (id3v2 != null) {
                if (log.isDebugEnabled()) log.debug("JavaID3TagReader(): id3v2 identifier=" + id3v2.getIdentifier());
            }
        } catch (java.lang.OutOfMemoryError e) {
            log.error("JavaID3TagReader(): error out of memory, filename=" + filename, e);
			if (RapidEvolution3UI.instance != null)
				RapidEvolution3UI.instance.notifyOutOfMemory("reading the tags (JavaID3TagReader) from file=" + filename);                        
        } catch (Exception e) {
            log.error("JavaID3TagReader(): error Exception, filename=" + filename, e);
        }
    }
 
    public String getAlbum() {
        String album = null;
        if (id3v2 != null) {
            album = id3v2.getAlbumTitle();
        }
        if (StringUtil.isValid(album)) return album;
        if (id3v1 != null) {
            album = id3v1.getAlbumTitle();
        }
        return album;
    }
    
    public String getAlbumCoverFilename() {
        String albumcover_filename = null;
        if (id3v2 != null) {            
            Iterator frames = id3v2.getFrameIterator();
            while (frames.hasNext()) {
                AbstractID3v2Frame frame = (AbstractID3v2Frame)frames.next();
                if (frame.getIdentifier().startsWith(FRAME_ALBUM_COVER)) {
                    FrameBodyAPIC frame_body = (FrameBodyAPIC)frame.getBody();
                    if (frame_body != null) {
                        if (log.isTraceEnabled()) log.trace("getAlbumCoverFilename(): album cover frame found=" + frame_body);
                        Iterator object_iter = frame_body.getObjectListIterator();
                        String text_encoding = object_iter.next().toString();
                        String mime_type = object_iter.next().toString();
                        String description = object_iter.next().toString().trim();
                        if (description != null) {
                            while (description.startsWith(".")) {
                                description = description.substring(1);
                            }
                        }
                        ObjectByteArraySizeTerminated bytearray = (ObjectByteArraySizeTerminated)object_iter.next();
                        albumcover_filename = AlbumCoverUtil.saveAlbumCover(this, description, mime_type, bytearray.writeByteArray());
                    }                    
                }
            }            
        }        
        return albumcover_filename;
    }
    
    public String getArtist() {
        String artist = null;
        // lead performer
        if (id3v2 != null) {
            artist = id3v2.getLeadArtist();
        }
        if (StringUtil.isValid(artist)) return artist;
        if (id3v1 != null) {
            artist = id3v1.getLeadArtist();
        }
        if (StringUtil.isValid(artist)) return artist;
        // composer
        if (id3v2 != null) {
            artist = id3v2.getAuthorComposer();
        }
        if (StringUtil.isValid(artist)) return artist;
        if (id3v1 != null) {
            artist = id3v1.getAuthorComposer();
        }
        if (StringUtil.isValid(artist)) return artist;
        // original performer
        if (id3v2 != null) {
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_ORIGINAL_PERFORMER);
            if (frame != null) {
                FrameBodyTOPE frame_body = (FrameBodyTOPE)frame.getBody();
                if (frame_body != null) {
                    artist = frame_body.getText();
                }
            }
        }        
        return artist;
    }
    
    public Integer getBeatIntensity() {
        Integer intensity = null;
        if (id3v2 != null) {
            FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_BEAT_INTENSITY);
            if (txxx_frame != null) {
                try {
                    intensity = new Integer(Integer.parseInt(txxx_frame.getText()));
                } catch (java.lang.NumberFormatException e) {
                    log.debug("getBeatIntensity(): number format exception beat_intensity txxx frame=" + txxx_frame.getText());
                }
            }
        }
        return intensity;     
    }
    
    public Integer getBpmAccuracy() {
        Integer accuracy = null;
        if (id3v2 != null) {
            FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_BPM_ACCURACY);
            if (txxx_frame != null) {
                try {
                    accuracy = new Integer(Integer.parseInt(txxx_frame.getText()));
                } catch (java.lang.NumberFormatException e) {
                    log.debug("getBpmAccuracy(): number format exception bpm_accuracy txxx frame=" + txxx_frame.getText());
                }
            }
        }
        return accuracy;        
    }
    
    public Float getBpmStart() {
        Float start_bpm = null;
        if (id3v2 != null) {
            // txxx start bpm
            FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_BPM_START);
            if (txxx_frame != null) {
                try {
                    start_bpm = new Float(Float.parseFloat(txxx_frame.getText()));
                } catch (java.lang.NumberFormatException e) {
                    log.debug("getBpmStart(): number format exception bpm_start txxx frame=" + txxx_frame.getText());
                }
            }
            if (start_bpm != null) return start_bpm;
            // tbpm
	        AbstractID3v2Frame frame = id3v2.getFrame(FRAME_BPM);
	        if (frame != null) {
	            FrameBodyTBPM bpm_frame = (FrameBodyTBPM)frame.getBody();
	            if (bpm_frame != null) {
	                try {
	                    start_bpm = new Float(Float.parseFloat(bpm_frame.getText()));
	                } catch (java.lang.NumberFormatException e) {
	                    log.debug("getBpmStart(): number format exception bpm frame=" + bpm_frame.getText());
	                }
	            }
	        }
            if (start_bpm != null) return start_bpm;
            // txxx bpm
            txxx_frame = getTXXXFrame(TXXX_BPM);
            if (txxx_frame != null) {
                try {
                    start_bpm = new Float(Float.parseFloat(txxx_frame.getText()));
                } catch (java.lang.NumberFormatException e) {
                    log.debug("getBpmStart(): number format exception bpm txxx frame=" + txxx_frame.getText());
                }
            }            
            if (start_bpm != null) return start_bpm;
            // txxx tempo
            txxx_frame = getTXXXFrame(TXXX_TEMPO);
            if (txxx_frame != null) {
                try {
                    start_bpm = new Float(Float.parseFloat(txxx_frame.getText()));
                } catch (java.lang.NumberFormatException e) {
                    log.debug("getBpmStart(): number format exception tempo txxx frame=" + txxx_frame.getText());
                }
            }
            // txxx finalscratch bpm
            txxx_frame = getTXXXFrame(TXXX_BPM_FINALSCRATCH);
            if (txxx_frame != null) {
                try {
                    start_bpm = new Float(Float.parseFloat(txxx_frame.getText()));
                } catch (java.lang.NumberFormatException e) {
                    log.debug("getBpmStart(): number format exception fbpm txxx frame=" + txxx_frame.getText());
                }
            }            
        }
        return start_bpm;
    }
        
    public Float getBpmEnd() {
        Float end_bpm = null;
        if (id3v2 != null) {
            FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_BPM_END);
            if (txxx_frame != null) {
                try {
                    end_bpm = new Float(Float.parseFloat(txxx_frame.getText()));
                } catch (java.lang.NumberFormatException e) {
                    log.debug("getBpmEnd(): number format exception bpm_end txxx frame=" + txxx_frame.getText());
                }
            }
        }
        return end_bpm;
    }
    
    public String getComments() {
        String comments = null;
        if (id3v2 != null) {
            comments = id3v2.getSongComment();
        }
        if (StringUtil.isValid(comments)) return comments;
        if (id3v1 != null) {
            comments = id3v1.getSongComment();
        }
        return comments;        
    }
    
    public String getContentGroupDescription() {
        String group_description = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_CONTENT_GROUP_DESCRIPTION);
            if (frame != null) {
                FrameBodyTIT1 frame_body = (FrameBodyTIT1)frame.getBody();
                if (frame_body != null) {
                    group_description = frame_body.getText();
                }
            }
        }
        return group_description;
    }
    
    public String getContentType() {
        String content_type = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_CONTENT_TYPE);
            if (frame != null) {
                FrameBodyTCON frame_body = (FrameBodyTCON)frame.getBody();
                if (frame_body != null) {
                    content_type = frame_body.getText();
                }
            }
        }
        return content_type;
    }
    
    public String getEncodedBy() {
        String encoded_by = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_ENCODED_BY);
            if (frame != null) {
                FrameBodyTENC frame_body = (FrameBodyTENC)frame.getBody();
                if (frame_body != null) {
                    encoded_by = frame_body.getText();
                }
            }
        }
        return encoded_by;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public String getFileType() {
        String file_type = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_FILE_TYPE);
            if (frame != null) {
                FrameBodyTFLT frame_body = (FrameBodyTFLT)frame.getBody();
                if (frame_body != null) {
                    file_type = frame_body.getText();
                }
            }
        }
        return file_type;
    }
    
    public String getGenre() {
        String genre = null;
        if (id3v2 != null) {
            genre = id3v2.getSongGenre();
        }
        if (StringUtil.isValid(genre)) return genre;
        if (id3v1 != null) {
            genre = id3v1.getSongGenre();
        }
        return genre;        
    }
    
    public Integer getKeyAccuracy() {
        Integer accuracy = null;
        if (id3v2 != null) {
            FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_KEY_ACCURACY);
            if (txxx_frame != null) {
                try {
                    accuracy = new Integer(Integer.parseInt(txxx_frame.getText()));
                } catch (java.lang.NumberFormatException e) {
                    log.debug("getKeyAccuracy(): number format exception key_accuracy txxx frame=" + txxx_frame.getText());
                }
            }
        }
        return accuracy;        
    }
    
    public String getKeyStart() {
        String start_key = null;
        // txxx start key
        FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_KEY_START);
        if (txxx_frame != null) {
            start_key = txxx_frame.getText();
        }
        if (StringUtil.isValid(start_key)) return start_key;
        // tkey
        if (id3v2 != null) {
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_KEY);
            if (frame != null) {
                FrameBodyTKEY frame_body = (FrameBodyTKEY)frame.getBody();
                if (frame_body != null) {
                    start_key = frame_body.getText();
                }
            }
        }
        // txxx key/initialkey/fkey
        txxx_frame = getTXXXFrame(TXXX_KEY);
        if (txxx_frame != null) {
            start_key = txxx_frame.getText();
        }
        if (StringUtil.isValid(start_key)) return start_key;
        txxx_frame = getTXXXFrame(TXXX_KEY_INITIAL);
        if (txxx_frame != null) {
            start_key = txxx_frame.getText();
        }        
        if (StringUtil.isValid(start_key)) return start_key;
        txxx_frame = getTXXXFrame(TXXX_KEY_FINALSCRATCH);
        if (txxx_frame != null) {
            start_key = txxx_frame.getText();
        }        
        return start_key;
    }
    
    public String getKeyEnd() {
        String end_key = null;
        // txxx end key
        FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_KEY_END);
        if (txxx_frame != null) {
            end_key = txxx_frame.getText();
        }
        return end_key;        
    }

    public String getLanguages() {
        String languages = null;        
        if (id3v2 != null) {
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_LANGUAGES);
            if (frame != null) {
                FrameBodyTLAN frame_body = (FrameBodyTLAN)frame.getBody();
                if (frame_body != null) {
                    languages = frame_body.getText();
                }
            }
        }
        return languages;
    }
    
    public String getPublisher() {
        String publisher = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_PUBLISHER);
            if (frame != null) {
                FrameBodyTPUB frame_body = (FrameBodyTPUB)frame.getBody();
                if (frame_body != null) {
                    publisher = frame_body.getText();
                }
            }
        }
        return publisher;
    }
    
    public Integer getRating() {
        Integer rating = null;
        FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_RATING);
        if (txxx_frame != null) {
            try {
                rating = new Integer(Integer.parseInt(txxx_frame.getText()));
            } catch (java.lang.NumberFormatException e) {
                log.debug("getRating(): number format exception rating txxx frame=" + txxx_frame.getText());
            }
        }
        return rating;
    }
    
    public String getRemix() {
        String remix = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_REMIXER);
            if (frame != null) {
                FrameBodyTPE4 frame_body = (FrameBodyTPE4)frame.getBody();
                if (frame_body != null) {
                    remix = frame_body.getText();
                }
            }
        }
        return remix;
    }
    
    public Integer getSizeInBytes() {
        Integer size_in_bytes = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_SIZE);
            if (frame != null) {
                FrameBodyTSIZ frame_body = (FrameBodyTSIZ)frame.getBody();
                if (frame_body != null) {
                    try {
                        size_in_bytes = new Integer(Integer.parseInt(frame_body.getText()));
                    } catch (java.lang.NumberFormatException e) {
                        log.debug("getSizeInBytes(): number format exception=" + frame_body.getText());
                    }
                }
            }
        }
        return size_in_bytes;
    }
    
    public Vector<DegreeValue> getStyles() {
        Vector<DegreeValue> styles = new Vector<DegreeValue>();
        int style_count = 1;
        DegreeValue style = getStyle(style_count);
        while (style != null) {
            styles.add(style);
            style = getStyle(++style_count);
        }
        return styles;
    }    
    
    public String getTime() {
        String time = null;                
        FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_TIME);
        if (txxx_frame != null) {
            time = txxx_frame.getText();
        }
        if (StringUtil.isValid(time)) return time;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_LENGTH);
            if (frame != null) {
                FrameBodyTLEN frame_body = (FrameBodyTLEN)frame.getBody();
                if (frame_body != null) {
                    try {
                        int milliseconds = Integer.parseInt(frame_body.getText());
                        time = new Duration(milliseconds / 1000).getDurationAsString();
                    } catch (Exception e) {
                        log.error("getTime(): error getting length=", e);
                    }
                }
            }
            if (StringUtil.isValid(time)) return time;
            frame = id3v2.getFrame(FRAME_TIME);
            if (frame != null) {
                FrameBodyTIME frame_body = (FrameBodyTIME)frame.getBody();
                if (frame_body != null) {
                    time = frame_body.getText();
                }
            }
        }
        return time;        
    }

    public String getTimeSignature() {
        String time_sig = null;
        FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_TIME_SIGNATURE);
        if (txxx_frame != null) {
            time_sig = txxx_frame.getText();
        }
        return time_sig;        
    }
    
    public String getTitle() {
        String title = null;
        if (id3v2 != null) {
            title = id3v2.getSongTitle();
        }
        if (StringUtil.isValid(title)) return title;
        if (id3v1 != null) {
            title = id3v1.getSongTitle();
        }
        return title;           
    }
    
    public String getTrack() {
        String track = null;
        if (id3v2 != null) {
            track = id3v2.getTrackNumberOnAlbum();
        }
        if (StringUtil.isValid(track)) return track;
        if (id3v1 != null) {
            track = id3v1.getTrackNumberOnAlbum();
        }
        return track;        
    }
    
    public String getUserField(String fieldName) {
        String userfield = null;
        FrameBodyTXXX txxx_frame = getTXXXFrame(TagUtil.convertToValidTagId(fieldName));
        if (txxx_frame != null) {
            userfield = txxx_frame.getText();
        }
        return userfield;          
    }

    public String getYear() {
        String year = null;
        if (id3v2 != null) {
            year = id3v2.getYearReleased();
        }
        if (StringUtil.isValid(year)) return year;
        if (id3v1 != null) {
            year = id3v1.getYearReleased();
        }
        return year;         
    }

    private FrameBodyTXXX getTXXXFrame(String description) {
        if (id3v2 != null) {
            Iterator iter = id3v2.getFrameIterator();
            while (iter.hasNext()) {
                AbstractID3v2Frame iter_frame = (AbstractID3v2Frame)iter.next();
                AbstractMP3FragmentBody frameBody = iter_frame.getBody();
                if (frameBody instanceof FrameBodyTXXX) {
                    FrameBodyTXXX txxx_frame = (FrameBodyTXXX)frameBody;
                    if (description.equals(txxx_frame.getDescription())) return txxx_frame;
                }
            }
        }
        return null;
    }
    
    private DegreeValue getStyle(int style_number) {
        String identifier = TagUtil.getStyleTagId(style_number);
        String degreeIdentifier = TagUtil.getStyleTagDegree(style_number);
        String style = null;
        float degree = 1.0f;
        FrameBodyTXXX txxx_frame = getTXXXFrame(identifier);
        if (txxx_frame != null) {
            style = txxx_frame.getText();
        }
        txxx_frame = getTXXXFrame(degreeIdentifier);
        if (txxx_frame != null) {
        	degree = Float.parseFloat(txxx_frame.getText());
        }
        if (style != null)
        	return new DegreeValue(style, degree, DATA_SOURCE_FILE_TAGS);
        return null;
    }    
    
}
