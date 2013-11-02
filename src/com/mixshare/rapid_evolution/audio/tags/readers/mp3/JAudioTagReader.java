package com.mixshare.rapid_evolution.audio.tags.readers.mp3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.ID3v11Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;
import org.jaudiotagger.tag.id3.framebody.FrameBodyCOMM;
import org.jaudiotagger.tag.id3.framebody.FrameBodyRVA2;
import org.jaudiotagger.tag.id3.framebody.FrameBodyRVAD;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTALB;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTBPM;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTCOM;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTCON;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTDRC;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTENC;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTFLT;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTIME;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTIT1;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTIT2;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTKEY;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTLAN;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTLEN;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTOPE;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTPE1;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTPE4;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTPUB;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTRCK;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTSIZ;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTYER;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.tags.readers.BaseTagReader;
import com.mixshare.rapid_evolution.audio.tags.util.AlbumCoverUtil;
import com.mixshare.rapid_evolution.audio.tags.util.TagUtil;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.util.StringUtil;

public class JAudioTagReader extends BaseTagReader {

    private static Logger log = Logger.getLogger(JAudioTagReader.class);
             
    private String filename = null;
    private ID3v1Tag id3v1 = null;
    private ID3v11Tag id3v11 = null;
    private AbstractID3v2Tag id3v2 = null;
    
    public JAudioTagReader(String filename) {
        try {
            MP3File.logger.setLevel(Level.OFF);            
            this.filename = filename;
            if (log.isTraceEnabled())
            	log.trace("JAudioTagReader(): filename=" + filename);
            MP3File mp3file;
            try {
                //mp3file = new MP3File(filename);
                mp3file = new MP3File(new File(filename), MP3File.LOAD_ALL, true);
            } catch (ReadOnlyFileException roe) {
                mp3file = new MP3File(new File(filename), MP3File.LOAD_ALL, true);
            }
            if (log.isTraceEnabled()) 
            	log.trace("JAudioTagReader(): mp3file=" + mp3file.displayStructureAsPlainText());
            id3v1 = mp3file.getID3v1Tag();
            id3v2 = mp3file.getID3v2TagAsv24();
            if (id3v2 != null) {
                String identifier = id3v2.getIdentifier();
                if (log.isTraceEnabled()) 
                	log.trace("JAudioTagReader(): id3v2 identifier=" + identifier);                 
            	Iterator seratoFrames = id3v2.getFrameOfType("GEOB");
                while (seratoFrames.hasNext()) {
                    AbstractID3v2Frame seratoFrame = (AbstractID3v2Frame)seratoFrames.next();
                    if (log.isDebugEnabled())
                    	log.debug("JAudioTagWriter(): found serato frame=" + seratoFrame);
                }
                
            }
            if (id3v1 instanceof ID3v11Tag) {
                id3v11 = (ID3v11Tag)id3v1;
            }
            
            //if (log.isDebugEnabled())
               //log.debug("JAudioTagReader(): GEOB frames=" + getFrames(FRAME_GENERAL));
            
        } catch (java.lang.OutOfMemoryError e) {
            log.error("JAudioTagReader(): error out of memory, filename=" + filename, e);
			if (RapidEvolution3UI.instance != null)
				RapidEvolution3UI.instance.notifyOutOfMemory("reading the tags (Jaudiotagger) from file=" + filename);            
        } catch (Exception e) {
            log.error("JAudioTagReader(): error Exception, filename=" + filename, e);
        }
    }
    
    public boolean isFileSupported() {
        return ((id3v1 != null) || (id3v2 != null));
    }
    
    public String getAlbum() {
        String album = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = getFrame(FRAME_ALBUM);
            if (frame != null) {
                FrameBodyTALB frameBody = (FrameBodyTALB)frame.getBody();
                album = frameBody.getText();
            }
            if (StringUtil.isValid(album)) return album;
        }
        if (id3v1 != null) {            
            album = getFirstValue(id3v1.getAlbum());
        }
        return album;
    }    
    
    private String getFirstValue(List list) {
        if ((list != null) && (list.size() > 0))
            return list.get(0).toString();
        return "";
    }
    
    public String getAlbumCoverFilename() {
        String albumcover_filename = null;
        if (id3v2 != null) {
            Iterator frames = getFrameOfType(FRAME_ALBUM_COVER);
            while (frames.hasNext()) {                
                AbstractID3v2Frame frame = (AbstractID3v2Frame)frames.next();
                FrameBodyAPIC frame_body = (FrameBodyAPIC)frame.getBody();
                if (frame_body != null) {
                    if (log.isTraceEnabled()) log.trace("getAlbumCoverFilename(): album cover frame found=" + frame_body);
                    String mime_type = frame_body.getObject("MIMEType").getValue().toString();
                    String description = frame_body.getDescription();
                    if (description != null) {
                        while (description.startsWith(".")) {
                            description = description.substring(1);
                        }
                    }
                    Object picture_data = frame_body.getObject("PictureData").getValue();
                    albumcover_filename = AlbumCoverUtil.saveAlbumCover(this, description, mime_type, (byte[])picture_data);
                }                    
            }            
        } 
        return albumcover_filename;
    }
    
    public String getArtist() {
        String artist = null;
        // lead performer
        if (id3v2 != null) {
            AbstractID3v2Frame frame = getFrame(FRAME_LEAD_PERFORMER);
            if (frame != null) {
                FrameBodyTPE1 frameBody = (FrameBodyTPE1)frame.getBody();
                artist = frameBody.getText();
            }
            if (StringUtil.isValid(artist)) return artist;            
        }
        if (id3v1 != null) {
            artist = getFirstValue(id3v1.getArtist());
            if (StringUtil.isValid(artist)) return artist;            
        }
        if (id3v2 != null) {
            // composer
            AbstractID3v2Frame frame = getFrame(FRAME_COMPOSER);
            if (frame != null) {
                FrameBodyTCOM frameBody = (FrameBodyTCOM)frame.getBody();
                artist = frameBody.getText();
            }
            if (StringUtil.isValid(artist)) return artist;  
            // original performer
            frame = getFrame(FRAME_ORIGINAL_PERFORMER);
            if (frame != null) {
                FrameBodyTOPE frameBody = (FrameBodyTOPE)frame.getBody();
                artist = frameBody.getText();
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
            AbstractID3v2Frame frame = getFrame(FRAME_BPM);
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
            AbstractID3v2Frame frame = getFrame(FRAME_COMMENTS);
            if (frame != null) {
                FrameBodyCOMM frameBody = (FrameBodyCOMM)frame.getBody();
                comments = frameBody.getText();
            }
            if (StringUtil.isValid(comments)) return comments;
        }
        if (id3v1 != null) {
            comments = getFirstValue(id3v1.getComment());
        }
        return comments;        
    }
    
    public String getContentGroupDescription() {
        String group_description = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = getFrame(FRAME_CONTENT_GROUP_DESCRIPTION);
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
            AbstractID3v2Frame frame = getFrame(FRAME_CONTENT_TYPE);
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
            AbstractID3v2Frame frame = getFrame(FRAME_ENCODED_BY);
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
            AbstractID3v2Frame frame = getFrame(FRAME_FILE_TYPE);
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
            AbstractID3v2Frame frame = getFrame(FRAME_GENRE);
            if (frame != null) {
                FrameBodyTCON frame_body = (FrameBodyTCON)frame.getBody();
                if (frame_body != null) {
                    genre = frame_body.getText();
                }
            }
            if (StringUtil.isValid(genre)) return genre;
        }
        if (id3v1 != null) {
            genre = getFirstValue(id3v1.getGenre());
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
            AbstractID3v2Frame frame = getFrame(FRAME_KEY);
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
            AbstractID3v2Frame frame = getFrame(FRAME_LANGUAGES);
            if (frame != null) {
                FrameBodyTLAN frame_body = (FrameBodyTLAN)frame.getBody();
                if (frame_body != null) {
                    languages = frame_body.getText();
                }
            }
        }
        return languages;
    }
    
    public String getLyrics() {
        String lyrics = null;
        /*
        if (id3v2 != null) {
            AbstractID3v2Frame frame = getFrame(FRAME_LYRICS);
            if (frame != null) {
                FrameBodySYLT frame_body = (FrameBodySYLT)frame.getBody();
                if (frame_body != null) {
                    lyrics = frame_body.getLyrics();
                }
            }            
        }
        */
        return lyrics;
    }
    
    public String getPublisher() {
        String publisher = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = getFrame(FRAME_PUBLISHER);
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
            AbstractID3v2Frame frame = getFrame(FRAME_REMIXER);
            if (frame != null) {
                FrameBodyTPE4 frame_body = (FrameBodyTPE4)frame.getBody();
                if (frame_body != null) {
                    remix = frame_body.getText();
                }
            }
        }
        return remix;
    }
    
    public Float getReplayGain() {
        Float replayGain = null;
        try {
            FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_REPLAYGAIN_TRACK_GAIN);
            if (txxx_frame != null) {
                String replayGainStr = txxx_frame.getText();
                replayGain = StringUtil.parseNumericalPrefix(replayGainStr);
                if (log.isTraceEnabled())
                    log.trace("getReplaygain(): " + TXXX_REPLAYGAIN_TRACK_GAIN + "=" + replayGain);
            }
        } catch (Exception e) {
            log.debug("getReplayGain(): error", e);
        }
        if (replayGain == null) {
            if (id3v2 != null) {
                AbstractID3v2Frame frame = getFrame(FRAME_REPLAYGAIN);
                if (frame != null) {
                    FrameBodyRVA2 frame_body = (FrameBodyRVA2)frame.getBody();
                    if (frame_body != null) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        frame_body.write(outputStream);
                        if (log.isTraceEnabled())
                            log.trace("getReplayGain(): decode_rva2_gain=" + decode_rva2_gain(outputStream.toByteArray()));
                    }
                }
                if (replayGain == null) {
                    frame = getFrame(FRAME_REPLAY_VOLUME_ADJUST);
                    if (frame != null) {
                        FrameBodyRVAD frame_body = (FrameBodyRVAD)frame.getBody();
                        if (frame_body != null) {
                        }
                    }
                }
            }
        }
        return replayGain;        
    }
    
    /* Assume buf points to the beginning of one channel's data in an RVA2
     * frame.  Return the encoded gain, in decibels. */
    static float decode_rva2_gain(byte[] buf)
    {
        if (log.isTraceEnabled()) {
            for (int i = 0; i < buf.length; ++i) {
                log.trace("decode_rva2_gain(): buf[" + i + "]=" + buf[i]);
            }
        }
        int gain_fp; /* fixed-point */
    
        gain_fp = buf[1];
        gain_fp <<= 8;
        gain_fp |= buf[2];   /* second byte of gain */
    
        return gain_fp / 512.0f;
    }    
    
    public Integer getSizeInBytes() {
        Integer size_in_bytes = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = getFrame(FRAME_SIZE);
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

    public Vector<DegreeValue> getTags() {
        Vector<DegreeValue> tags = new Vector<DegreeValue>();
        int tag_count = 1;
        DegreeValue tag = getTag(tag_count);
        while (tag != null) {
            tags.add(tag);
            tag = getTag(++tag_count);
        }
        return tags;
    }    
    
    public String getTime() {
        String time = null;                
        FrameBodyTXXX txxx_frame = getTXXXFrame(TXXX_TIME);
        if (txxx_frame != null) {
            time = txxx_frame.getText();
        }
        if (StringUtil.isValid(time))
        	return time;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = getFrame(FRAME_LENGTH);
            if (frame != null) {
                FrameBodyTLEN frame_body = (FrameBodyTLEN)frame.getBody();
                if (frame_body != null) {
                	String value = frame_body.getText().trim();
                	if ((value != null) && (value.length() > 0)) {
	                    try {
	                        int milliseconds = Integer.parseInt(value);
	                        time = new Duration(milliseconds / 1000).getDurationAsString();
	                    } catch (Exception e) {
	                        log.error("getTime(): error getting length=", e);
	                    }
                	}
                }
            }
            if (StringUtil.isValid(time)) return time;
            frame = getFrame(FRAME_TIME);
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
            AbstractID3v2Frame frame = getFrame(FRAME_TITLE);
            if (frame != null) {
                FrameBodyTIT2 frameBody = (FrameBodyTIT2)frame.getBody();
                title = frameBody.getText();
            }
            if (StringUtil.isValid(title)) return title;
        }
        if (id3v1 != null) {
            title = getFirstValue(id3v1.getTitle());
        }
        return title;           
    }
    
    public Integer getTotalTracks() {
        String track = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = getFrame(FRAME_TRACK);
            if (frame != null) {
                FrameBodyTRCK frameBody = (FrameBodyTRCK)frame.getBody();
                track = frameBody.getText();
            }
        }
        if (!StringUtil.isValid(track)) {
        	if (id3v11 != null) {
        		track = getFirstValue(id3v11.getTrack());
        	}
        }
        if (track != null) {
        	int separator = track.indexOf("/");
        	if (separator > 0)
        		return Integer.parseInt(track.substring(separator + 1));        	
        }
        return null;     	
    }
    
    public String getTrack() {
        String track = null;
        if (id3v2 != null) {
            AbstractID3v2Frame frame = getFrame(FRAME_TRACK);
            if (frame != null) {
                FrameBodyTRCK frameBody = (FrameBodyTRCK)frame.getBody();
                track = frameBody.getText();
            }
        }
        if (!StringUtil.isValid(track)) {
        	if (id3v11 != null) {
        		track = getFirstValue(id3v11.getTrack());
        	}
        }
        if (track != null) {
        	int separator = track.indexOf("/");
        	if (separator > 0) {
        		return track.substring(0, separator);
        	}
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
        	List<TagField> fields = id3v2.getFields(FieldKey.YEAR);
        	if (fields != null) {
        		for (TagField tagField : fields) {
        			if (tagField instanceof AbstractID3v2Frame) {
        				AbstractID3v2Frame frame = ((AbstractID3v2Frame)tagField);
                        if (frame.getBody() instanceof FrameBodyTYER) {
                            FrameBodyTYER frameBody = (FrameBodyTYER)frame.getBody();
                            year = StringUtil.cleanString(frameBody.getText());
    	        			if (StringUtil.isValid(year))
    	        				break;                        	
                        } else if (frame.getBody() instanceof FrameBodyTDRC) {
                        	FrameBodyTDRC frameBody = (FrameBodyTDRC)frame.getBody();
                            year = StringUtil.cleanString(frameBody.getText());
    	        			if (StringUtil.isValid(year))
    	        				break;                        	                        	
                        }
        			}
        		}
        	}
            if (StringUtil.isValid(year)) return year;
        }
        if (id3v1 != null) {
            year = getFirstValue(id3v1.getYear());
        }
        return year;         
    }
    
    private FrameBodyTXXX getTXXXFrame(String description) {
        if (id3v2 != null) {
            Iterator iter = getFrameOfType(FRAME_TXXX);
            while (iter.hasNext()) {
                Object nextObject = iter.next();
                if (nextObject instanceof ArrayList) {
                    ArrayList list = (ArrayList)nextObject;
                    Iterator aiter = list.iterator();
                    while (aiter.hasNext()) {
                        nextObject = aiter.next();
                        if (nextObject instanceof AbstractID3v2Frame) {
                            AbstractID3v2Frame iter_frame = (AbstractID3v2Frame)nextObject;
                            AbstractTagFrameBody frameBody = iter_frame.getBody();
                            if (frameBody instanceof FrameBodyTXXX) {
                                FrameBodyTXXX txxx_frame = (FrameBodyTXXX)frameBody;
                                if (description.equals(txxx_frame.getDescription())) return txxx_frame;
                            }                        
                        }
                    }
                } else if (nextObject instanceof AbstractID3v2Frame) {
                    AbstractID3v2Frame iter_frame = (AbstractID3v2Frame)nextObject;
                    AbstractTagFrameBody frameBody = iter_frame.getBody();
                    if (frameBody instanceof FrameBodyTXXX) {
                        FrameBodyTXXX txxx_frame = (FrameBodyTXXX)frameBody;
                        if (description.equals(txxx_frame.getDescription())) return txxx_frame;
                    }                    
                }
            }
        }
        return null;
    }

    private AbstractID3v2Frame getFrame(String identifier) {
        AbstractID3v2Frame frame = null;
        if (id3v2 != null) {
            Object value = id3v2.getFrame(identifier);
            if (value instanceof AbstractID3v2Frame) {
                frame = (AbstractID3v2Frame)value;
            } else if (value instanceof ArrayList) {
                ArrayList list = (ArrayList)value;
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    value = iter.next();
                    if (value instanceof AbstractID3v2Frame) {
                        frame = (AbstractID3v2Frame)value;
                        return frame;
                    }
                }                
            }
        }
        return frame;
    }

    /*
    private ArrayList getFrames(String identifier) {
        if (id3v2 != null) {
            Iterator iter = id3v2.frameMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry entry = (Entry)iter.next();
                String key = entry.getKey().toString();                
                if (key.equals(identifier)) {
                    if (entry.getValue() instanceof ID3v24Frame) {
                        ID3v24Frame frame = (ID3v24Frame)entry.getValue();
                        log.debug("getFrames(): GEOB frame body=" + frame.getBody());
                    }
                }
            }
            iter = id3v2.getFields();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (next instanceof ID3v24Frame) {
                    ID3v24Frame frame = (ID3v24Frame)next;                    
                    log.debug("getFrames(): field=" + frame.getIdentifier());
                } else {
                    log.debug("getFrames(): field=" + next);
                }
            }
        }        
        return null;
    }
    */
    
    private DegreeValue getStyle(int style_number) {
        String identifier = TagUtil.getStyleTagId(style_number);
        String degreeIdentifier = TagUtil.getStyleTagDegree(style_number);
        String style = null;
        FrameBodyTXXX txxx_frame = getTXXXFrame(identifier);
        if (txxx_frame != null) {
            style = txxx_frame.getText();
        }
        float degree = 1.0f;
        txxx_frame = getTXXXFrame(degreeIdentifier);
        if (txxx_frame != null) {
        	degree = Float.parseFloat(txxx_frame.getText());
        }     
        if (style != null)
        	return new DegreeValue(style, degree, DATA_SOURCE_FILE_TAGS);
        return null;
    }    

    private DegreeValue getTag(int tag_number) {
        String identifier = TagUtil.getTagTagId(tag_number);
        String degreeIdentifier = TagUtil.getTagTagDegree(tag_number);
        String tag = null;
        FrameBodyTXXX txxx_frame = getTXXXFrame(identifier);
        if (txxx_frame != null) {
            tag = txxx_frame.getText();
        }
        float degree = 1.0f;
        txxx_frame = getTXXXFrame(degreeIdentifier);
        if (txxx_frame != null) {
        	degree = Float.parseFloat(txxx_frame.getText());
        }     
        if (tag != null)
        	return new DegreeValue(tag, degree, DATA_SOURCE_FILE_TAGS);
        return null;
    }    
    
    private Iterator getFrameOfType(String identifier) {
        if (id3v2 != null) {
            Iterator iter = id3v2.getFrameOfType(identifier);
            if (iter.hasNext()) {
                Object next = iter.next();
                if (next instanceof ArrayList) return ((ArrayList)next).iterator();
            }
            return id3v2.getFrameOfType(identifier);
        }
        return null;
    }

    static public void main(String[] args) {
    	try {
    		RapidEvolution3.loadLog4J();
    		JAudioTagReader reader = new JAudioTagReader("C:/Users/Jesse/Desktop/3-22 She Has A Way (12_ Version).mp3");
    		log.info("year=" + reader.getYear());
    	} catch (Exception e) {
    		log.error("main(): error", e);
    	}
    }
    
}
