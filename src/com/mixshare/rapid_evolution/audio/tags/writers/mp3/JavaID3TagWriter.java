package com.mixshare.rapid_evolution.audio.tags.writers.mp3;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.farng.mp3.AbstractMP3FragmentBody;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagConstant;
import org.farng.mp3.TagOptionSingleton;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.AbstractID3v2Frame;
import org.farng.mp3.id3.AbstractID3v2FrameBody;
import org.farng.mp3.id3.FrameBodyAPIC;
import org.farng.mp3.id3.FrameBodyTBPM;
import org.farng.mp3.id3.FrameBodyTCON;
import org.farng.mp3.id3.FrameBodyTENC;
import org.farng.mp3.id3.FrameBodyTFLT;
import org.farng.mp3.id3.FrameBodyTIT1;
import org.farng.mp3.id3.FrameBodyTKEY;
import org.farng.mp3.id3.FrameBodyTLAN;
import org.farng.mp3.id3.FrameBodyTOPE;
import org.farng.mp3.id3.FrameBodyTPE4;
import org.farng.mp3.id3.FrameBodyTPUB;
import org.farng.mp3.id3.FrameBodyTSIZ;
import org.farng.mp3.id3.FrameBodyTXXX;
import org.farng.mp3.id3.ID3v1;
import org.farng.mp3.id3.ID3v1_1;
import org.farng.mp3.id3.ID3v2_3Frame;
import org.farng.mp3.id3.ID3v2_4;
import org.farng.mp3.id3.ID3v2_4Frame;

import com.mixshare.rapid_evolution.audio.tags.util.TagUtil;
import com.mixshare.rapid_evolution.audio.tags.writers.BaseTagWriter;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;

/**
 * Old and not maintained since early 2008
 */
public class JavaID3TagWriter extends BaseTagWriter {

    private static Logger log = Logger.getLogger(JavaID3TagWriter.class);
    
    private String filename = null;
    private MP3File mp3file = null;
    private ID3v1 id3v1 = null;
    private AbstractID3v2 id3v2 = null;
    private int id3v2_version = -1;

    public JavaID3TagWriter(String filename, int mode) {
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("JavaID3TagWriter(): filename=" + filename);            
            File sourceFile = new File(filename);
            mp3file = new MP3File(sourceFile);
            id3v1 = mp3file.getID3v1Tag();
            if (log.isDebugEnabled()) log.debug("JavaID3TagWriter(): id3v1=" + id3v1);
            if (id3v1 == null) {
                id3v1 = new ID3v1_1();
                mp3file.setID3v1Tag(id3v1);
            }
            id3v2 = mp3file.getID3v2Tag();
            if (log.isDebugEnabled()) log.debug("JavaID3TagWriter(): id3v2=" + id3v2);
            if (id3v2 == null) {
                id3v2 = new ID3v2_4();
                mp3file.setID3v2Tag(id3v2);
            }
            if (id3v2.getIdentifier().startsWith("ID3v2.3")) id3v2_version = ID3_V_2_3;
            else id3v2_version = ID3_V_2_4;
            if (log.isDebugEnabled()) log.debug("JavaID3TagWriter(): id3v2 identifier=" + id3v2.getIdentifier() + " (" + id3v2_version + ")");
            if (mode == TAG_MODE_OVERWRITE)
                TagOptionSingleton.getInstance().setDefaultSaveMode(TagConstant.MP3_FILE_SAVE_OVERWRITE);
            else // TAG_MODE_UPDATE
                TagOptionSingleton.getInstance().setDefaultSaveMode(TagConstant.MP3_FILE_SAVE_WRITE);
        } catch (Exception e) {
            log.error("JavaID3TagWriter(): error Exception, filename=" + filename, e);
        }
    }
    
    public boolean save() {
        boolean success = false;
        try {
            if (mp3file != null) {
                mp3file.save();
                success = true;
                if (log.isDebugEnabled()) log.debug("save(): tag successfully written to filename=" + filename);            
            }
        } catch (Exception e) {
            log.error("save(): could not save tag to filename=" + filename, e);
        }
        return success;        
    }    
    
    public void setAlbum(String album) {
        if (id3v2 != null) {
            id3v2.setAlbumTitle(album);
        }
        if (id3v1 != null) {
            id3v1.setAlbumTitle(album);
        }
    }
    
    public void setAlbumCover(String filename, String album) {
        if (id3v2 != null) {
            try {
                Vector previous_apic_frames = new Vector();
                Iterator frames = id3v2.getFrameIterator();
                while (frames.hasNext()) {
                    AbstractID3v2Frame frame = (AbstractID3v2Frame)frames.next();
                    if (frame.getIdentifier().startsWith(FRAME_ALBUM_COVER)) {
                        previous_apic_frames.add(frame);
                    }
                }
                for (int f = 0; f < previous_apic_frames.size(); ++f) {
                    AbstractID3v2Frame frame = (AbstractID3v2Frame)previous_apic_frames.get(f);
                    id3v2.removeFrame(frame.getIdentifier());
                }
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
		            FrameBodyAPIC frameBody = new FrameBodyAPIC((byte) 0, "image/" + FileUtil.getExtension(file), (byte) 3, album, imageBinary.toByteArray());
	                id3v2.setFrame(getNewFrame(frameBody));
                }
            } catch (Exception e) {
                log.error("setAlbumCover(): error setting album cover filename=" + filename, e);
            }
        }
    }
    
    public void setArtist(String artist) {
        if (id3v2 != null) {
            id3v2.setLeadArtist(artist);
            id3v2.setAuthorComposer(artist);
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_ORIGINAL_PERFORMER);
            if (frame != null) {
                FrameBodyTOPE frame_body = (FrameBodyTOPE)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(artist);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTOPE((byte) 0, artist);
                id3v2.setFrame(getNewFrame(frameBody));
            }
        }
        if (id3v1 != null) {
            id3v1.setLeadArtist(artist);
        }
    }
    
    public void setBeatIntensity(int beat_intensity) {
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = getTXXXFrame(TXXX_BEAT_INTENSITY);
            if (frameBody != null) {
                frameBody.setText(String.valueOf(beat_intensity));
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, TXXX_BEAT_INTENSITY, String.valueOf(beat_intensity));
                id3v2.setFrame(getNewFrame(frameBody));
            }            
        }
    }
    
    public void setBpm(int bpm) {
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_BPM);
            if (frame != null) {
                FrameBodyTBPM bpm_frame = (FrameBodyTBPM)frame.getBody();
                if (bpm_frame != null) {
                    frame_exists = true;
                    bpm_frame.setText(String.valueOf(bpm));
                }
            }        
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTBPM((byte) 0, String.valueOf(bpm));
                id3v2.setFrame(getNewFrame(frameBody));
            }
        }        
    }
    
    public void setBpmFloat(float bpm) {        
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_BPM);
            if (frame != null) {
                FrameBodyTBPM bpm_frame = (FrameBodyTBPM)frame.getBody();
                if (bpm_frame != null) {
                    frame_exists = true;
                    bpm_frame.setText(String.valueOf(bpm));
                }
            }        
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTBPM((byte) 0, String.valueOf(bpm));
                id3v2.setFrame(getNewFrame(frameBody));
            }
        }                
    }    
    
    public void setBpmAccuracy(int accuracy) {
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = getTXXXFrame(TXXX_BPM_ACCURACY);
            if (frameBody != null) {
                frameBody.setText(String.valueOf(accuracy));
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, TXXX_BPM_ACCURACY, String.valueOf(accuracy));
                id3v2.setFrame(getNewFrame(frameBody));
            }            
        }        
    }
    
    public void setBpmStart(float start_bpm) {
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = getTXXXFrame(TXXX_BPM_START);
            if (frameBody != null) {
                frameBody.setText(String.valueOf(start_bpm));
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, TXXX_BPM_START, String.valueOf(start_bpm));
                id3v2.setFrame(getNewFrame(frameBody));
            }
        }
    }

    public void setBpmEnd(float end_bpm) {
        if (id3v2 != null) {        
            FrameBodyTXXX frameBody = getTXXXFrame(TXXX_BPM_END);
            if (frameBody != null) {
                frameBody.setText(String.valueOf(end_bpm));
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, TXXX_BPM_END, String.valueOf(end_bpm));
                id3v2.setFrame(getNewFrame(frameBody));
            }
        }        
    }
    
    public void setComments(String comments) {
        if (id3v2 != null) {
            id3v2.setSongComment(comments);
        }
        if (id3v1 != null) {
            id3v1.setSongComment(comments);
        }
    }
    
    public void setContentGroupDescription(String content_group_description) {
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_CONTENT_GROUP_DESCRIPTION);
            if (frame != null) {
                FrameBodyTIT1 frame_body = (FrameBodyTIT1)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(content_group_description);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTIT1((byte) 0, content_group_description);
                id3v2.setFrame(getNewFrame(frameBody));                
            }
        }
    }
    
    public void setContentType(String content_type) {
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_CONTENT_TYPE);
            if (frame != null) {
                FrameBodyTCON frame_body = (FrameBodyTCON)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(content_type);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTCON((byte) 0, content_type);
                id3v2.setFrame(getNewFrame(frameBody));                
            }
        }        
    }
    
    public void setEncodedBy(String encoded_by) {
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_ENCODED_BY);
            if (frame != null) {
                FrameBodyTENC frame_body = (FrameBodyTENC)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(encoded_by);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTENC((byte) 0, encoded_by);
                id3v2.setFrame(getNewFrame(frameBody));
            }
        }   
    }
    
    public void setFileType(String file_type) {
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_FILE_TYPE);
            if (frame != null) {
                FrameBodyTFLT frame_body = (FrameBodyTFLT)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(file_type);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTFLT((byte) 0, file_type);
                id3v2.setFrame(getNewFrame(frameBody));
            }
        }        
    }
    
    public void setGenre(String genre) {
        if (id3v2 != null) {
            id3v2.setSongGenre(genre);
        }
        if (id3v1 != null) {
            // TODO: convert to ID3v1 genre?
            //id3v1.setSongGenre(genre);
        }        
    }
    
    public void setKeyAccuracy(int accuracy) {
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = getTXXXFrame(TXXX_KEY_ACCURACY);
            if (frameBody != null) {
                frameBody.setText(String.valueOf(accuracy));
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, TXXX_KEY_ACCURACY, String.valueOf(accuracy));
                id3v2.setFrame(getNewFrame(frameBody));
            }            
        }        
    }

    public void setKey(String key) {
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_KEY);
            if (frame != null) {
                FrameBodyTKEY frame_body = (FrameBodyTKEY)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(key);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTKEY((byte) 0, key);
                id3v2.setFrame(getNewFrame(frameBody));
            }
        }        
    }
    
    public void setKeyStart(String start_key) {
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = getTXXXFrame(TXXX_KEY_START);
            if (frameBody != null) {
                frameBody.setText(start_key);
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, TXXX_KEY_START, start_key);
                id3v2.setFrame(getNewFrame(frameBody));
            }            
        }        
    }
    
    public void setKeyEnd(String end_key) {
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = getTXXXFrame(TXXX_KEY_END);
            if (frameBody != null) {
                frameBody.setText(end_key);
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, TXXX_KEY_END, end_key);
                id3v2.setFrame(getNewFrame(frameBody));
            }            
        }
    }
    
    public void setLanguages(String languages) {
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_LANGUAGES);
            if (frame != null) {
                FrameBodyTLAN frame_body = (FrameBodyTLAN)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(languages);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTLAN((byte) 0, languages);
                id3v2.setFrame(getNewFrame(frameBody));
            }
        }        
    }
    
    public void setPublisher(String publisher) {
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_PUBLISHER);
            if (frame != null) {
                FrameBodyTPUB frame_body = (FrameBodyTPUB)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(publisher);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTPUB((byte) 0, publisher);
                id3v2.setFrame(getNewFrame(frameBody));
            }
        }
    }
    
    public void setRating(int rating) {
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = getTXXXFrame(TXXX_RATING);
            if (frameBody != null) {
                frameBody.setText(String.valueOf(rating));
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, TXXX_RATING, String.valueOf(rating));
                id3v2.setFrame(getNewFrame(frameBody));
            }            
        }
    }
    
    public void setRemix(String remix) {
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_REMIXER);
            if (frame != null) {
                FrameBodyTPE4 frame_body = (FrameBodyTPE4)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(remix);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTPE4((byte) 0, remix);
                id3v2.setFrame(getNewFrame(frameBody));
            }
        }
    }
    
    public void setSizeInBytes(int size) {
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = id3v2.getFrame(FRAME_SIZE);
            if (frame != null) {
                FrameBodyTSIZ frame_body = (FrameBodyTSIZ)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(String.valueOf(size));
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTSIZ((byte) 0, String.valueOf(size));
                id3v2.setFrame(getNewFrame(frameBody));
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
    
    public void setTime(String time) {
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = getTXXXFrame(TXXX_TIME);
            if (frameBody != null) {
                frameBody.setText(time);
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, TXXX_TIME, time);
                id3v2.setFrame(getNewFrame(frameBody));
            }            
        }        
    }
    
    public void setTimeSignature(String time_sig) {
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = getTXXXFrame(TXXX_TIME_SIGNATURE);
            if (frameBody != null) {
                frameBody.setText(time_sig);
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, TXXX_TIME_SIGNATURE, time_sig);
                id3v2.setFrame(getNewFrame(frameBody));
            }            
        } 
    }
    
    public void setTitle(String title) {
        if (id3v2 != null) {
            id3v2.setSongTitle(title);
        }
        if (id3v1 != null) {
            id3v1.setSongTitle(title);
        }        
    }
    
    public void setTrack(String track, Integer total_tracks) {
        if (id3v2 != null) {
            if (total_tracks != null) {
                id3v2.setTrackNumberOnAlbum(track + "/" + total_tracks.intValue());
            } else {
                id3v2.setTrackNumberOnAlbum(track);
            }
        }
        if (id3v1 != null) {
            id3v1.setTrackNumberOnAlbum(track);
        }                        
    }
    
    public void setUserField(String fieldName, String value) {
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = getTXXXFrame(TagUtil.convertToValidTagId(fieldName));
            if (frameBody != null) {
                frameBody.setText(value);
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, TagUtil.convertToValidTagId(fieldName), value);
                id3v2.setFrame(getNewFrame(frameBody));
            }            
        }        
    }    
    
    public void setYear(String year) {
        if (id3v2 != null) {
            id3v2.setYearReleased(year);
        }
        if (id3v1 != null) {
            id3v1.setYearReleased(year);
        }                
    }
    
    private AbstractID3v2Frame getNewFrame(AbstractID3v2FrameBody frameBody) {
        if (id3v2_version == ID3_V_2_3) {
            return new ID3v2_3Frame(frameBody);
        }
        // ID3_V_2_4
        return new ID3v2_4Frame(frameBody);
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
    
    private void removeStyles() {
        if (id3v2 != null) {
            Vector remove_ids = new Vector();
            Iterator iter = id3v2.getFrameIterator();
            while (iter.hasNext()) {
                AbstractID3v2Frame iter_frame = (AbstractID3v2Frame)iter.next();
                AbstractMP3FragmentBody frameBody = iter_frame.getBody();
                if (frameBody instanceof FrameBodyTXXX) {
                    FrameBodyTXXX txxx_frame = (FrameBodyTXXX)frameBody;
                    if (txxx_frame.getDescription().startsWith(TXXX_STYLES_PREFIX)) {
                        remove_ids.add(txxx_frame.getIdentifier());
                    }
                }
            }
            for (int f = 0; f < remove_ids.size(); ++f) {
                id3v2.removeFrame((String)remove_ids.get(f));
            }
        }        
    }
    
    private void setStyle(String style, float styleDegree, int style_number) {
        if (id3v2 != null) {
            String identifier = TagUtil.getStyleTagId(style_number);
            FrameBodyTXXX frameBody = getTXXXFrame(identifier);
            if (frameBody != null) {
                frameBody.setText(style);
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, identifier, style);
                id3v2.setFrame(getNewFrame(frameBody));
            }
            identifier = TagUtil.getStyleTagDegree(style_number);
            frameBody = getTXXXFrame(identifier);
            if (frameBody != null) {
                frameBody.setText(String.valueOf(styleDegree));
            } else {
                frameBody = new FrameBodyTXXX((byte) 0, identifier, String.valueOf(styleDegree));
                id3v2.setFrame(getNewFrame(frameBody));
            }            
            
        }        
    }    
    
}
