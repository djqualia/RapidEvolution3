package com.mixshare.rapid_evolution.audio.tags.readers.mp3;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.MediaFile;
import org.blinkenlights.jid3.v1.ID3V1Tag;
import org.blinkenlights.jid3.v1.ID3V1_1Tag;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
import org.blinkenlights.jid3.v2.TBPMTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TCOMTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TCONTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TENCTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TFLTTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TIMETextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TIT1TextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TKEYTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TLANTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TLENTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TOPETextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TPE4TextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TPUBTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TRCKTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TSIZTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TXXXTextInformationID3V2Frame;

import com.mixshare.rapid_evolution.audio.tags.readers.BaseTagReader;
import com.mixshare.rapid_evolution.audio.tags.util.AlbumCoverUtil;
import com.mixshare.rapid_evolution.audio.tags.util.TagUtil;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.util.StringUtil;

public class JID3TagReader extends BaseTagReader {

    private static Logger log = Logger.getLogger(JID3TagReader.class);
            
    private String filename;
    private ID3V1Tag id3v1_tag = null;
    private ID3V1_1Tag id3v11_tag = null;
    private ID3V2Tag id3v2_tag = null;
    private ID3V2_3_0Tag id3v230_tag = null;
    private TXXXTextInformationID3V2Frame[] txxx_frames = null;
    private ID3V2Frame[] frames = null;
    
    public JID3TagReader(String filename) {
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("JID3TagReader(): filename=" + filename);
            File source_file = new File(filename);
//            FileSource source_file = new FileSource();
            MediaFile media_file = new MP3File(source_file);
            id3v1_tag = media_file.getID3V1Tag();
            if (log.isDebugEnabled()) log.debug("JID3TagReader(): id3v1_tag=" + id3v1_tag);            
            if (id3v1_tag instanceof ID3V1_1Tag) {
                id3v11_tag = (ID3V1_1Tag)id3v1_tag;
            }
            id3v2_tag = media_file.getID3V2Tag();
            if (log.isDebugEnabled()) log.debug("JID3TagReader(): id3v2_tag=" + id3v2_tag);
            if (id3v2_tag instanceof ID3V2_3_0Tag) {
                id3v230_tag = (ID3V2_3_0Tag)id3v2_tag;
                if (log.isDebugEnabled()) log.debug("JID3TagReader(): tag is id3 v2.3.0");    
                txxx_frames = id3v230_tag.getTXXXTextInformationFrames();
                if ((txxx_frames != null) && log.isDebugEnabled()) {
                    log.debug("JID3TagReader(): # txx_frames=" + txxx_frames.length);
                    for (int t = 0; t < txxx_frames.length; ++t) {
                        log.debug("JID3TagReader(): # txx_frame " + t + "=" + txxx_frames[t]);
                    }
                }                
            }
            if (id3v2_tag != null) {
                frames = id3v2_tag.getSingleFrames();
                if ((frames != null) && log.isDebugEnabled()) log.debug("JID3TagReader(): # frames=" + frames.length);            
            }
        } catch (java.lang.OutOfMemoryError e) {
            log.error("JID3TagReader(): error out of memory, filename=" + filename, e);
			if (RapidEvolution3UI.instance != null)
				RapidEvolution3UI.instance.notifyOutOfMemory("reading the tags (JID3TagReader) from file=" + filename);                        
        } catch (ID3Exception e) {
            log.error("JID3TagReader(): error ID3Exception, filename=" + filename, e);
        }
    }

    public boolean isFileSupported() {
        return ((id3v1_tag != null) || (id3v2_tag != null));
    }
    
    public String getAlbum() {
        String album = null;
        if (id3v2_tag != null) album = id3v2_tag.getAlbum();
        if (StringUtil.isValid(album)) return album;
        if (id3v1_tag != null) album = id3v1_tag.getAlbum();
        return album;        
    }
    
    public String getAlbumCoverFilename() {
        String albumcover_filename = null;
        if (id3v230_tag != null) {
            try {
	            APICID3V2Frame apic_frame = null;
	            APICID3V2Frame[] frames = id3v230_tag.getAPICFrames();
	            if (frames != null) {
	                for (int f = 0; f < frames.length; ++f) {
	                    APICID3V2Frame frame = frames[f];
	                    if (log.isTraceEnabled()) log.trace("getAlbumCoverFilename(): album cover frame found=" + frame);
	                    if ((apic_frame == null) || (frame.getPictureType() == APICID3V2Frame.PictureType.FrontCover))
	                        apic_frame = frame;
	                }
	            }
	            if (apic_frame != null) {
	                albumcover_filename = AlbumCoverUtil.saveAlbumCover(this, apic_frame.getDescription(), apic_frame.getMimeType(), apic_frame.getPictureData());
	            }
            } catch (Exception e) {
                log.error("getAlbumCoverFilename(): error Exception", e);
            }
        }
        return albumcover_filename;
    }
    
    public String getArtist() {
        String artist = null;
        // lead performer 
        if (id3v2_tag != null) artist = id3v2_tag.getArtist();
        if (StringUtil.isValid(artist)) return artist;
        if (id3v1_tag != null) artist = id3v1_tag.getArtist();
        if (StringUtil.isValid(artist)) return artist;                
        // composer
        TCOMTextInformationID3V2Frame com_frame = (TCOMTextInformationID3V2Frame)getID3V2Frame(TCOMTextInformationID3V2Frame.class);
        if (com_frame != null) {
            StringBuffer artist_buffer = new StringBuffer();            
            String[] composers = com_frame.getComposers();
            if (composers != null) {
                for (int c = 0; c < composers.length; ++c) {
                    artist_buffer.append(composers[c]);
                    if (c + 1 < composers.length)
                        artist_buffer.append(", ");
                }
            }
            artist = artist_buffer.toString();
        }
        if (StringUtil.isValid(artist)) return artist;
        // original artist
        TOPETextInformationID3V2Frame ope_frame = (TOPETextInformationID3V2Frame)getID3V2Frame(TOPETextInformationID3V2Frame.class);
        if (ope_frame != null) {
            StringBuffer artist_buffer = new StringBuffer();            
            String[] original_performers = ope_frame.getOriginalPerformers();
            if (original_performers != null) {
                for (int o = 0; o < original_performers.length; ++o) {
                    artist_buffer.append(original_performers[o]);
                    if (o + 1 < original_performers.length)
                        artist_buffer.append(", ");
                }
            }
            artist = artist_buffer.toString();
        }
        return artist;
    }    

    public Integer getBeatIntensity() {
        Integer intensity = null;
        String txxx_intensity = getTXXXValue(new String[] { TXXX_BEAT_INTENSITY });
        if (txxx_intensity != null) {
            try {
                intensity = new Integer(Integer.parseInt(txxx_intensity));
            } catch (Exception e) {
                if (log.isDebugEnabled()) log.debug("getBeatIntensity(): could not parse beat_intensity=" + txxx_intensity);
            }
        }                
        return intensity;
    }
    
    public Integer getBpmAccuracy() {
        Integer accuracy = null;
        String txxx_accuracy = getTXXXValue(new String[] { TXXX_BPM_ACCURACY });
        if (txxx_accuracy != null) {
            try {
                accuracy = new Integer(Integer.parseInt(txxx_accuracy));
            } catch (Exception e) {
                if (log.isDebugEnabled()) log.debug("getBpmAccuracy(): could not parse bpm_accuracy=" + txxx_accuracy);
            }
        }                
        return accuracy;
    }
    
    public Float getBpmStart() {
        Float start_bpm = null;
        // txxx start_bpm
        String txxx_bpm = getTXXXValue(new String[] { TXXX_BPM_START });
        if (txxx_bpm != null) {
            try {
                start_bpm = new Float(Float.parseFloat(txxx_bpm));
            } catch (Exception e) {
                if (log.isDebugEnabled()) log.debug("getBpmStart(): could not parse txxx start bpm=" + txxx_bpm);
            }
        }
        if (start_bpm != null) return start_bpm;
        // tbpm
        TBPMTextInformationID3V2Frame frame = (TBPMTextInformationID3V2Frame)getID3V2Frame(TBPMTextInformationID3V2Frame.class);
        if (frame != null) {
            start_bpm = new Float(frame.getBeatsPerMinute());
        }
        if (start_bpm != null) return start_bpm;
        // txxx bpm/tempo
        txxx_bpm = getTXXXValue(new String[] { TXXX_BPM, TXXX_TEMPO, TXXX_BPM_FINALSCRATCH });
        if (txxx_bpm != null) {
            try {
                start_bpm = new Float(Float.parseFloat(txxx_bpm));
            } catch (Exception e) {
                if (log.isDebugEnabled()) log.debug("getBpmStart(): could not parse txxx bpm=" + txxx_bpm);
            }
        }        
        return start_bpm;
    }
    
    public Float getBpmEnd() {
        Float end_bpm = null;
        String txxx_bpm = getTXXXValue(new String[] { TXXX_BPM_END });
        if (txxx_bpm != null) {
            try {
                end_bpm = new Float(Float.parseFloat(txxx_bpm));
            } catch (Exception e) {
                if (log.isDebugEnabled()) log.debug("getBpmEnd(): could not parse bpm=" + txxx_bpm);
            }
        }                
        return end_bpm;        
    }
    
    public String getComments() {
        String comments = null;
        if (id3v2_tag != null) comments = id3v2_tag.getComment();
        if (StringUtil.isValid(comments)) return comments;
        if (id3v1_tag != null) comments = id3v1_tag.getComment();
        return comments;
    }
    
    public String getContentGroupDescription() {
        String content_group_description = null;
        TIT1TextInformationID3V2Frame frame = (TIT1TextInformationID3V2Frame)getID3V2Frame(TIT1TextInformationID3V2Frame.class);
        if (frame != null) {            
            content_group_description = frame.getContentGroupDescription();
        }
        return content_group_description;
    }
    
    public String getContentType() {
        String content_type = null;
        TCONTextInformationID3V2Frame frame = (TCONTextInformationID3V2Frame)getID3V2Frame(TCONTextInformationID3V2Frame.class);
        if (frame != null) {            
            content_type = frame.getContentType().toString();
        }
        return content_type;
    }
    
    public String getEncodedBy() {
        String encoded_by = null;
        TENCTextInformationID3V2Frame frame = (TENCTextInformationID3V2Frame)getID3V2Frame(TENCTextInformationID3V2Frame.class);
        if (frame != null) {            
            encoded_by = frame.getEncodedBy();
        }
        return encoded_by;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public String getFileType() {
        String file_type = null;
        TFLTTextInformationID3V2Frame frame = (TFLTTextInformationID3V2Frame)getID3V2Frame(TFLTTextInformationID3V2Frame.class);
        if (frame != null) {            
            file_type = frame.getFileType();
        }
        return file_type;
    }

    public String getGenre() {
        String genre = null;
        if (id3v2_tag != null) {
            genre = id3v2_tag.getGenre().toString();            
        }
        if (StringUtil.isValid(genre)) return genre;
        if (id3v1_tag != null) {
            genre = id3v1_tag.getGenre().toString();
        }
        return genre;
    }

    public Integer getKeyAccuracy() {
        Integer accuracy = null;
        String txxx_accuracy = getTXXXValue(new String[] { TXXX_KEY_ACCURACY });
        if (txxx_accuracy != null) {
            try {
                accuracy = new Integer(Integer.parseInt(txxx_accuracy));
            } catch (Exception e) {
                if (log.isDebugEnabled()) log.debug("getKeyAccuracy(): could not parse key_accuracy=" + txxx_accuracy);
            }
        }                
        return accuracy;   
    }
    
    public String getKeyStart() {
        String start_key = null;
        // txxx start key
        start_key = getTXXXValue(new String[] { TXXX_KEY_START });
        if (StringUtil.isValid(start_key)) return start_key;
        // tkey
        TKEYTextInformationID3V2Frame frame = (TKEYTextInformationID3V2Frame)getID3V2Frame(TKEYTextInformationID3V2Frame.class);
        if (frame != null) {
            start_key = frame.getInitialKey();
        }
        if (StringUtil.isValid(start_key)) return start_key;
        // txxx key/initialkey
        start_key = getTXXXValue(new String[] { TXXX_KEY, TXXX_KEY_INITIAL, TXXX_KEY_FINALSCRATCH });
        return start_key;
    }
    
    public String getKeyEnd() {
        String end_key = null;
        end_key = getTXXXValue(new String[] { TXXX_KEY_END });
        return end_key;
    }
    
    public String getLanguages() {
        String languages = null;
        TLANTextInformationID3V2Frame frame = (TLANTextInformationID3V2Frame)getID3V2Frame(TLANTextInformationID3V2Frame.class);
        if (frame != null) {            
            languages = frame.getLanguages();
        }        
        return languages;
    }

    public String getPublisher() {
        String publisher = null;
        TPUBTextInformationID3V2Frame frame = (TPUBTextInformationID3V2Frame)getID3V2Frame(TPUBTextInformationID3V2Frame.class);
        if (frame != null) {            
            publisher = frame.getPublisher();
        }        
        return publisher;
    }
    
    public Integer getRating() {
        Integer rating = null;
        String txxx_rating = getTXXXValue(new String[] { TXXX_RATING });
        if (StringUtil.isValid(txxx_rating)) {
            try {
                rating = new Integer(Integer.parseInt(txxx_rating));
            } catch (Exception e) {
                if (log.isDebugEnabled()) log.debug("getRating(): could not parse rating int=" + txxx_rating);
            }
        }        
        return rating;
    }
    
    public String getRemix() {
        String remix = null;
        TPE4TextInformationID3V2Frame frame = (TPE4TextInformationID3V2Frame)getID3V2Frame(TPE4TextInformationID3V2Frame.class);
        if (frame != null) {
            remix = frame.getModifiedBy();
        }
        if (remix == null)
        	remix = "";
        return remix;
    }
    
    public Float getReplayGain() {
        Float replayGain = null;
        String txxx_replaygain = getTXXXValue(new String[] { TXXX_REPLAYGAIN_TRACK_GAIN });
        replayGain = StringUtil.parseNumericalPrefix(txxx_replaygain);
        if (log.isTraceEnabled())
            log.trace("getReplaygain(): " + TXXX_REPLAYGAIN_TRACK_GAIN + "=" + replayGain);
        return replayGain;
    }
            
    public Integer getSizeInBytes() {
        Integer size_in_bytes = null;
        TSIZTextInformationID3V2Frame frame = (TSIZTextInformationID3V2Frame)getID3V2Frame(TSIZTextInformationID3V2Frame.class);
        if (frame != null) {
            size_in_bytes = new Integer(frame.getSizeInBytes());
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
        time = getTXXXValue(new String[] { TXXX_TIME });        
        if (StringUtil.isValid(time)) return time;        
        TLENTextInformationID3V2Frame tlen_frame = (TLENTextInformationID3V2Frame)getID3V2Frame(TLENTextInformationID3V2Frame.class);
        if (tlen_frame != null) {
            int milliseconds = tlen_frame.getTrackLength();
            time = new Duration(milliseconds / 1000).getDurationAsString();
        }
        if (StringUtil.isValid(time)) return time;        
        TIMETextInformationID3V2Frame time_frame = (TIMETextInformationID3V2Frame)getID3V2Frame(TIMETextInformationID3V2Frame.class);
        if (time_frame != null) {
            time = String.valueOf(time_frame.getHours()) + ":" + String.valueOf(time_frame.getMinutes());
        }
        return time;
    }

    public String getTimeSignature() {
        return getTXXXValue(new String[] { TXXX_TIME_SIGNATURE });        
    }
    
    public String getTitle() {
        String title = null;
        if (id3v2_tag != null) title = id3v2_tag.getTitle();
        if (StringUtil.isValid(title)) return title;
        if (id3v1_tag != null) title = id3v1_tag.getTitle();
        return title;
    }  
    
    public String getTrack() {
        String track = null;
        if (id3v2_tag != null) {
            try {
                track = String.valueOf(id3v2_tag.getTrackNumber());
            } catch (ID3Exception e) {
                if (log.isDebugEnabled()) log.debug("getTrack(): could not read track=" + e);
            }
        }
        if (StringUtil.isValid(track)) { 
        	if ((track.length() == 1) && Character.isDigit(track.charAt(0)))
        		return "0" + track;
        	return track;
        }
        TRCKTextInformationID3V2Frame frame = (TRCKTextInformationID3V2Frame)getID3V2Frame(TRCKTextInformationID3V2Frame.class);
        if (frame != null) {
            track = String.valueOf(frame.getTrackNumber());
        }
        if (StringUtil.isValid(track)) return track;
        // hack alert
        if (id3v1_tag != null) {
            String tag_text = id3v1_tag.toString();
            String search_text = "AlbumTrack = ";
            int index = tag_text.indexOf(search_text);
            if (index >= 0) {
                int final_index = index + search_text.length();
                while ((final_index < tag_text.length()) && Character.isDigit(tag_text.charAt(final_index))) {
                    ++final_index;
                }
                track = tag_text.substring(index + search_text.length(), final_index);
            }
        }
    	if ((track != null) && (track.length() == 1) && Character.isDigit(track.charAt(0)))
    		return "0" + track;        
        return track;
    }

    public Integer getTotalTracks() {
        Integer total_tracks = null;
        if (id3v2_tag != null) {
            try {
                total_tracks = new Integer(id3v2_tag.getTotalTracks());
            } catch (ID3Exception e) {
                if (log.isDebugEnabled()) log.debug("getTotalTracks(): could not read total tracks=" + e);
            }
        }
        if (total_tracks != null) return total_tracks;
        TRCKTextInformationID3V2Frame frame = (TRCKTextInformationID3V2Frame)getID3V2Frame(TRCKTextInformationID3V2Frame.class);
        if (frame != null) {
            try {
                total_tracks = new Integer(frame.getTotalTracks());
            } catch (ID3Exception e) {
                if (log.isDebugEnabled()) log.debug("getTotalTracks(): could not read total tracks=" + e);
            }
        }
        return total_tracks;
    }

    public String getUserField(String fieldName) {
        return getTXXXValue(new String[] { TagUtil.convertToValidTagId(fieldName) });        
    }
    
    public String getYear() {
        String year = null;
        if (id3v2_tag != null) {
            try {
                year = String.valueOf(id3v2_tag.getYear());
            } catch (ID3Exception e) {
                if (log.isDebugEnabled()) log.debug("getYear(): could not read year=" + e);
            }
        }
        if (StringUtil.isValid(year)) return year;
        if (id3v1_tag != null) year = id3v1_tag.getYear();
        return year;
    }
     
    private String getTXXXValue(String description) {
        if (description == null) return null;
        return getTXXXValue(new String[] { description });
    }
    private String getTXXXValue(String[] descriptions) {
        if (descriptions == null) return null;
        if (txxx_frames != null) {
            for (int f = 0; f < txxx_frames.length; ++f) {
                TXXXTextInformationID3V2Frame frame = txxx_frames[f];
                for (int d = 0; d < descriptions.length; ++d) {
                    if (descriptions[d].equalsIgnoreCase(frame.getDescription())) {
                        return frame.getInformation();
                    }                        
                }
            }
        }
        return null;
    }

    private ID3V2Frame getID3V2Frame(Class classType) {
        if (frames != null) {
            for (int f = 0; f < frames.length; ++f) {
                ID3V2Frame frame = frames[f];
                if (classType.isInstance(frame))
                    return frame;
            }
        }
        return null;
    }
    
    private DegreeValue getStyle(int style_number) {
        String identifier = TagUtil.getStyleTagId(style_number);
        String style = getTXXXValue(identifier);
        String degreeIdentifier = TagUtil.getStyleTagDegree(style_number);
        float degree = 1.0f;
        String degreeStr = getTXXXValue(degreeIdentifier);
        if ((degreeStr != null) && !degreeStr.equals(""))
        	degree = Float.parseFloat(degreeStr);
        if (style != null)
        	return new DegreeValue(style, degree, DATA_SOURCE_FILE_TAGS);
        return null;
    }
    
    private DegreeValue getTag(int tag_number) {
        String identifier = TagUtil.getTagTagId(tag_number);
        String tag = getTXXXValue(identifier);
        String degreeIdentifier = TagUtil.getTagTagDegree(tag_number);
        float degree = 1.0f;
        String degreeStr = getTXXXValue(degreeIdentifier);
        if ((degreeStr != null) && !degreeStr.equals(""))
        	degree = Float.parseFloat(degreeStr);
        if (tag != null)
        	return new DegreeValue(tag, degree, DATA_SOURCE_FILE_TAGS);
        return null;
    }
    
}
