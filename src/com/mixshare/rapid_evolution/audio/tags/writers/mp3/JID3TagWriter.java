package com.mixshare.rapid_evolution.audio.tags.writers.mp3;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.MediaFile;
import org.blinkenlights.jid3.v1.ID3V1Tag;
import org.blinkenlights.jid3.v1.ID3V1_0Tag;
import org.blinkenlights.jid3.v1.ID3V1Tag.Genre;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.blinkenlights.jid3.v2.ContentType;
import org.blinkenlights.jid3.v2.ID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
import org.blinkenlights.jid3.v2.TBPMTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TCOMTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TCONTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TENCTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TFLTTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TIT1TextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TKEYTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TLANTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TOPETextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TPE4TextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TPUBTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TSIZTextInformationID3V2Frame;
import org.blinkenlights.jid3.v2.TXXXTextInformationID3V2Frame;

import com.mixshare.rapid_evolution.audio.tags.util.TagUtil;
import com.mixshare.rapid_evolution.audio.tags.writers.BaseTagWriter;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;

public class JID3TagWriter extends BaseTagWriter {
    
    private static Logger log = Logger.getLogger(JID3TagWriter.class);
    
    private String filename = null;
    private MediaFile media_file = null;
    private ID3V1Tag id3v1_tag = null;
    private ID3V2Tag id3v2_tag = null;
    private ID3V2_3_0Tag id3v230_tag = null;
    private TXXXTextInformationID3V2Frame[] txxx_frames = null;
    private ID3V2Frame[] frames = null;
    
    public JID3TagWriter(String filename, int mode) {
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("JID3TagWriter(): filename=" + filename);
            File source_file = new File(filename);
            media_file = new MP3File(source_file);
            id3v1_tag = (mode == TAG_MODE_UPDATE) ? media_file.getID3V1Tag() : new ID3V1_0Tag();
            if (log.isDebugEnabled()) log.debug("JID3TagWriter(): id3v1_tag=" + id3v1_tag);
            if (id3v1_tag == null) id3v1_tag = new ID3V1_0Tag();
            id3v2_tag = (mode == TAG_MODE_UPDATE) ? media_file.getID3V2Tag() : new ID3V2_3_0Tag();
            if (log.isDebugEnabled()) log.debug("JID3TagWriter(): id3v2_tag=" + id3v2_tag);            
            if (id3v2_tag == null) id3v2_tag = new ID3V2_3_0Tag();
            if (id3v2_tag instanceof ID3V2_3_0Tag) {
                id3v230_tag = (ID3V2_3_0Tag)id3v2_tag;
                if (log.isDebugEnabled()) log.debug("JID3TagWriter(): tag is id3 v2.3.0");    
                txxx_frames = id3v230_tag.getTXXXTextInformationFrames();
                if ((txxx_frames != null) && log.isDebugEnabled()) log.debug("JID3TagWriter(): # txx_frames=" + txxx_frames.length);            
            }
            if (id3v2_tag != null) {
                frames = id3v2_tag.getSingleFrames();
                if ((frames != null) && log.isDebugEnabled()) log.debug("JID3TagWriter(): # frames=" + frames.length);            
            }
        } catch (java.lang.OutOfMemoryError e) {
            log.error("JID3TagWriter(): error out of memory, filename=" + filename, e);            
			if (RapidEvolution3UI.instance != null)
				RapidEvolution3UI.instance.notifyOutOfMemory("writing the tags (JID3TagWriter) to file=" + filename);                        
        } catch (ID3Exception e) {
            log.error("JID3TagWriter(): error ID3Exception, filename=" + filename, e);
        }
    }
    
    public boolean save() {
        boolean success = false;
        try {
            File source_file = new File(filename);
            if (id3v1_tag != null) {
                media_file.setID3Tag(id3v1_tag);
            }
            if (id3v2_tag != null) {
                media_file.setID3Tag(id3v2_tag);
            }
            if (id3v230_tag != null) {
                media_file.setID3Tag(id3v230_tag);    
            }        
            media_file.sync();            
            success = true;
            if (log.isDebugEnabled()) log.debug("save(): tag successfully written to filename=" + filename);            
        } catch (Exception e) {
            log.error("save(): could not save tag to filename=" + filename, e);
        }
        return success;        
    }
    
    public void setAlbum(String album) {
        if (id3v2_tag != null) {
            try {
                id3v2_tag.setAlbum(album);
            } catch (ID3Exception e) {
                log.debug("setAlbum(): error ID3Exception", e);
            }
        }
        if (id3v1_tag != null) {
            id3v1_tag.setAlbum(album);
        }        
    }
    
    public void setAlbumCover(String filename, String album) {
        if (id3v230_tag != null) {
            try {
	            File file = new File(OSHelper.getWorkingDirectory() + "/" + filename);
	            if (file.exists()) {
		            byte[] buffer = new byte[32 * 1024];
		            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
		            ByteArrayOutputStream imageBinary = new ByteArrayOutputStream();
		            int bytes_read = bufferedInputStream.read(buffer);
		            while (bytes_read != -1) {
		                imageBinary.write(buffer, 0, bytes_read);
		                bytes_read = bufferedInputStream.read(buffer);
		            }
		            String mimeType = "image/" + FileUtil.getExtension(file);
		            APICID3V2Frame apicid3V2Frame = new APICID3V2Frame(mimeType, APICID3V2Frame.PictureType.FrontCover, album, imageBinary.toByteArray()); 
		            id3v230_tag.addAPICFrame(apicid3V2Frame);
	            }
            } catch (Exception e) {
                log.error("setAlbumCover(): error Exception", e);
            }
        }
    }
    
    public void setArtist(String artist) {
        if (id3v2_tag != null) {
            try {
                id3v2_tag.setArtist(artist);
            } catch (ID3Exception e) {
                log.debug("setArtist(): error ID3Exception", e);
            }
        }
        if (id3v230_tag != null) {
            id3v230_tag.setTCOMTextInformationFrame(new TCOMTextInformationID3V2Frame(artist));
            id3v230_tag.setTOPETextInformationFrame(new TOPETextInformationID3V2Frame(artist));            
        }
        if (id3v1_tag != null) {
            id3v1_tag.setArtist(artist);
        }                
    }
    
    public void setBeatIntensity(int beat_intensity) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_BEAT_INTENSITY);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_BEAT_INTENSITY, String.valueOf(beat_intensity)));
            } catch (ID3Exception e) {
                log.debug("setBeatIntensity(): error ID3Exception", e);
            }
        }
    }
    
    public void setBpm(int bpm) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_BPM_FINALSCRATCH);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_BPM_FINALSCRATCH, String.valueOf(bpm)));
            } catch (ID3Exception e) {
                log.debug("setBpmFloat(): error ID3Exception", e);
            }
            id3v230_tag.setTBPMTextInformationFrame(new TBPMTextInformationID3V2Frame(bpm));
        }        
    }    
    
    public void setBpmFloat(float bpm) {        
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_BPM_FINALSCRATCH);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_BPM_FINALSCRATCH, String.valueOf(bpm)));
            } catch (ID3Exception e) {
                log.debug("setBpmFloat(): error ID3Exception", e);
            }
        }
    }    
        
    public void setBpmAccuracy(int accuracy) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_BPM_ACCURACY);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_BPM_ACCURACY, String.valueOf(accuracy)));
            } catch (ID3Exception e) {
                log.debug("setBpmAccuracy(): error ID3Exception", e);
            }
        }        
    }
    
    public void setBpmStart(float start_bpm) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_BPM_START);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_BPM_START, String.valueOf(start_bpm)));
            } catch (ID3Exception e) {
                log.debug("setBpmEnd(): error ID3Exception", e);
            }
        }        
    }
    
    public void setBpmEnd(float end_bpm) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_BPM_END);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_BPM_END, String.valueOf(end_bpm)));
            } catch (ID3Exception e) {
                log.debug("setBpmEnd(): error ID3Exception", e);
            }
        }
    }
    
    public void setCatalogId(String value) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_CATALOG_ID);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_CATALOG_ID, value));
            } catch (ID3Exception e) {
                log.debug("setCatalogId(): error ID3Exception", e);
            }
        }        
    }    
    
    public void setComments(String comments) {
        if (id3v2_tag != null) {
            try {
                id3v2_tag.setComment(comments);
            } catch (ID3Exception e) {
                log.debug("setComments(): error ID3Exception", e);
            }
        }
        if (id3v1_tag != null) {
            id3v1_tag.setComment(comments);
        }                
        
    }
    
    public void setContentGroupDescription(String content_group_description) {
        if (id3v230_tag != null) {
            id3v230_tag.setTIT1TextInformationFrame(new TIT1TextInformationID3V2Frame(content_group_description));
        }
    }
    
    public void setContentType(String content_type) {
        if (id3v230_tag != null) {
            ContentType contentType = new ContentType();
            // TODO: set ContentType.Genre's...
            contentType.setRefinement(content_type);
            TCONTextInformationID3V2Frame frame = new TCONTextInformationID3V2Frame(contentType);
            id3v230_tag.setTCONTextInformationFrame(frame);
        }        
    }
    
    public void setEncodedBy(String encoded_by) {
        if (id3v230_tag != null) {
            id3v230_tag.setTENCTextInformationFrame(new TENCTextInformationID3V2Frame(encoded_by));
        }
    }
    
    public void setFileType(String file_type) {
        if (id3v230_tag != null) {
            id3v230_tag.setTFLTTextInformationFrame(new TFLTTextInformationID3V2Frame(file_type));
        }        
    }
    
    public void setGenre(String genre) {
        if (id3v2_tag != null) {
            try {
                id3v2_tag.setGenre(genre);
            } catch (ID3Exception e) {
                log.debug("setGenre(): error ID3Exception", e);
            }
        }
        if (id3v1_tag != null) {
            try {
                Genre v1genre = Genre.lookupGenre(genre);
                if (v1genre != null) {
                    id3v1_tag.setGenre(v1genre);
                }
            } catch (ID3Exception e) {
                log.error("setGenre(): error setting v1 genre", e);
            }
        }                
    }
    
    public void setKey(String key) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.setTKEYTextInformationFrame(new TKEYTextInformationID3V2Frame(key));
            } catch (ID3Exception e) {
                log.debug("setKeyStart(): error ID3Exception", e);
            }
        }        
    }
    
    public void setKeyAccuracy(int accuracy) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_KEY_ACCURACY);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_KEY_ACCURACY, String.valueOf(accuracy)));
            } catch (ID3Exception e) {
                log.debug("setKeyAccuracy(): error ID3Exception", e);
            }
        }        
    }    
    
    public void setKeyStart(String start_key) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_KEY_START);
                if (!start_key.equals("")) id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_KEY_START, start_key));
            } catch (ID3Exception e) {
                log.debug("setKeyStart(): error ID3Exception", e);
            }
        }
    }
    
    public void setKeyEnd(String end_key) {
        try {
            id3v230_tag.removeTXXXTextInformationFrame(TXXX_KEY_END);
            if (!end_key.equals("")) id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_KEY_END, end_key));
        } catch (ID3Exception e) {
            log.debug("setKeyStart(): error ID3Exception", e);
        }        
    }
    
    public void setLanguages(String languages) {
        if (id3v230_tag != null) {
            id3v230_tag.setTLANTextInformationFrame(new TLANTextInformationID3V2Frame(languages));
        }
    }
    
    public void setPublisher(String publisher) {
        if (id3v230_tag != null) {
            id3v230_tag.setTPUBTextInformationFrame(new TPUBTextInformationID3V2Frame(publisher));
        }        
    }
    
    public void setRating(int rating) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_RATING);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_RATING, String.valueOf(rating)));
            } catch (ID3Exception e) {
                log.debug("setRating(): error ID3Exception", e);
            }
        }        
    }
    
    public void setRemix(String remix)  {
        if (id3v230_tag != null) {
            id3v230_tag.setTPE4TextInformationFrame(new TPE4TextInformationID3V2Frame(remix));
        }
    }
    
    public void setReplayGain(float value) {
        if (log.isTraceEnabled())
            log.trace("setReplayGain(): value=" + value);
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_REPLAYGAIN_TRACK_GAIN);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_REPLAYGAIN_TRACK_GAIN, String.valueOf(value) + " dB"));
            } catch (ID3Exception e) {
                log.debug("setReplayGain(): error ID3Exception", e);
            }
        }
    }    
    
    public void setSizeInBytes(int size) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.setTSIZTextInformationFrame(new TSIZTextInformationID3V2Frame(size));
            } catch (ID3Exception e) {
                log.debug("setSizeInBytes(): error ID3Exception", e);
            }
        }        
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
    
    public void setTime(String time) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_TIME);
                if (!time.equals("")) id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_TIME, time));
            } catch (ID3Exception e) {
                log.debug("setTime(): error ID3Exception", e);
            }
        }
    }
    
    public void setTimeSignature(String time_sig) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TXXX_TIME_SIGNATURE);
                if (!time_sig.equals("")) id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TXXX_TIME_SIGNATURE, time_sig));
            } catch (ID3Exception e) {
                log.debug("setTimeSignature(): error ID3Exception", e);
            }
        }    
    }
    
    public void setTitle(String title) {
        if (id3v2_tag != null) {
            try {
                id3v2_tag.setTitle(title);
            } catch (ID3Exception e) {
                log.debug("setTitle(): error ID3Exception", e);
            }
        }
        if (id3v1_tag != null) {
            id3v1_tag.setTitle(title);
        }        
    }
    
    public void setTrack(String track, Integer total_tracks) {
        if (id3v2_tag != null) {
            try {
                if (total_tracks == null)
                    id3v2_tag.setTrackNumber(Integer.parseInt(track));
                else
                    id3v2_tag.setTrackNumber(Integer.parseInt(track), total_tracks.intValue());
            } catch (java.lang.NumberFormatException e) {
                log.debug("setTrack(): number format exception=" + track, e);
            } catch (ID3Exception e) {
                log.debug("setTrack(): error ID3Exception", e);
            }
        }
    }
    
    public void setUserField(String fieldName, String value) {
        if (id3v230_tag != null) {
            try {
                id3v230_tag.removeTXXXTextInformationFrame(TagUtil.convertToValidTagId(fieldName));
                if (!value.equals("")) id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(TagUtil.convertToValidTagId(fieldName), value));
            } catch (ID3Exception e) {
                log.debug("setUser1(): error ID3Exception", e);
            }
        }
    }
    
    public void setYear(String year) {
        if (id3v2_tag != null) {
            try {
                id3v2_tag.setYear(Integer.parseInt(year));
            } catch (NumberFormatException e) {
                log.debug("setYear(): error number format exception=" + year, e);
            } catch (ID3Exception e) {
                log.debug("setYear(): error ID3Exception", e);
            }
        }
        if (id3v1_tag != null) {
            id3v1_tag.setYear(year);
        }                        
    }
    
    private void removeStyles() {
        if (id3v230_tag != null) {
            Vector remove_ids = new Vector();
            TXXXTextInformationID3V2Frame[] txxx_frames = id3v230_tag.getTXXXTextInformationFrames();
            if (txxx_frames != null) {
                for (int f = 0; f < txxx_frames.length; ++f) {
                    if (txxx_frames[f].getDescription().startsWith(TXXX_STYLES_PREFIX))
                        remove_ids.add(txxx_frames[f].getDescription());
                }
            }
            for (int f = 0; f < remove_ids.size(); ++f)
                id3v230_tag.removeTXXXTextInformationFrame((String)remove_ids.get(f));            
        }        
    }
    
    private void setStyle(String style, float styleDegree, int style_number) {
        if (id3v230_tag != null) {
            String identifier = TagUtil.getStyleTagId(style_number);
            try {
                id3v230_tag.removeTXXXTextInformationFrame(identifier);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(identifier, style));
            } catch (ID3Exception e) {
                log.debug("setStyle(): error ID3Exception", e);
            }
            identifier = TagUtil.getStyleTagDegree(style_number);
            try {
                id3v230_tag.removeTXXXTextInformationFrame(identifier);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(identifier, String.valueOf(styleDegree)));
            } catch (ID3Exception e) {
                log.debug("setStyle(): error ID3Exception", e);
            }

        }        
    }        

    private void removeTags() {
        if (id3v230_tag != null) {
            Vector remove_ids = new Vector();
            TXXXTextInformationID3V2Frame[] txxx_frames = id3v230_tag.getTXXXTextInformationFrames();
            if (txxx_frames != null) {
                for (int f = 0; f < txxx_frames.length; ++f) {
                    if (txxx_frames[f].getDescription().startsWith(TXXX_TAGS_PREFIX))
                        remove_ids.add(txxx_frames[f].getDescription());
                }
            }
            for (int f = 0; f < remove_ids.size(); ++f)
                id3v230_tag.removeTXXXTextInformationFrame((String)remove_ids.get(f));            
        }        
    }
    
    private void setTag(String tag, float tagDegree, int tag_number) {
        if (id3v230_tag != null) {
            String identifier = TagUtil.getTagTagId(tag_number);
            try {
                id3v230_tag.removeTXXXTextInformationFrame(identifier);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(identifier, tag));
            } catch (ID3Exception e) {
                log.debug("setTag(): error ID3Exception", e);
            }
            identifier = TagUtil.getTagTagDegree(tag_number);
            try {
                id3v230_tag.removeTXXXTextInformationFrame(identifier);
                id3v230_tag.addTXXXTextInformationFrame(new TXXXTextInformationID3V2Frame(identifier, String.valueOf(tagDegree)));
            } catch (ID3Exception e) {
                log.debug("setTag(): error ID3Exception", e);
            }

        }        
    }        

    
}
