package com.mixshare.rapid_evolution.audio.tags.writers.mp3;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.ID3v11Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v23Frame;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.ID3v24Frame;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.id3.framebody.AbstractID3v2FrameBody;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;
import org.jaudiotagger.tag.id3.framebody.FrameBodyDeprecated;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTCON;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTENC;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTFLT;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTLAN;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTRCK;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTSIZ;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.tags.util.TagUtil;
import com.mixshare.rapid_evolution.audio.tags.writers.BaseTagWriter;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;

public class JAudioTagWriter extends BaseTagWriter {

	static private Logger log = Logger.getLogger(JAudioTagWriter.class);
    
    static public boolean REMOVE_DEPRACATED_FRAMES = true;
    
    private String filename = null;
    private MP3File mp3file = null;
    private ID3v1Tag id3v1 = null;
    private ID3v11Tag id3v11 = null;
    private AbstractID3v2Tag id3v2 = null;
    private int id3v2_version = 0;
    
    private HashMap txxx_frame_map = new HashMap();

    public JAudioTagWriter(String filename, int mode, int preferredID3Type) {
        try {
            MP3File.logger.setLevel(Level.OFF);
            this.filename = filename;
            if (log.isDebugEnabled())
            	log.debug("JAudioTagWriter(): filename=" + filename);
            mp3file = new MP3File(filename);
            if (log.isDebugEnabled())
            	log.debug("JAudioTagWriter(): mp3file=" + mp3file.displayStructureAsPlainText());
            if (mode == TAG_MODE_OVERWRITE) {
            	if (log.isTraceEnabled())
            		log.trace("JAudioTagWriter(): tag overwrite mode");            	
                id3v1 = new ID3v11Tag();
                mp3file.setID3v1Tag(id3v1);
                id3v2 = new ID3v24Tag();
                mp3file.setID3v2Tag(id3v2);
            } else { // TAG_MODE_UPDATE
            	if (log.isTraceEnabled())
            		log.trace("JAudioTagWriter(): tag update mode");            	
                id3v1 = mp3file.getID3v1Tag();
                if (id3v1 == null) {
                	if (log.isTraceEnabled())
                		log.trace("JAudioTagWriter(): creating id3 v1 tag");            	
                    id3v1 = new ID3v11Tag();
                    mp3file.setID3v1Tag(id3v1);                    
                }
                if (preferredID3Type == ID3_V_2_3) {
                	if (log.isTraceEnabled())
                		log.trace("JAudioTagWriter(): getting v2.3 tag");            	                	
                    id3v2 = mp3file.getID3v2Tag();
                } else {
                	if (log.isTraceEnabled())
                		log.trace("JAudioTagWriter(): getting v2.4 tag");            	                	
                    id3v2 = mp3file.getID3v2TagAsv24();
                }
                if (id3v2 == null) {
                	if (log.isTraceEnabled())
                		log.trace("JAudioTagWriter(): creating new tag");            	                	                	
                    if (preferredID3Type == ID3_V_2_3)
                        id3v2 = new ID3v23Tag();
                    else
                        id3v2 = new ID3v24Tag();                    
                }
                mp3file.setID3v2Tag(id3v2);
                String identifier = id3v2.getIdentifier();
                if (identifier.startsWith("ID3v2.3"))
                	id3v2_version = ID3_V_2_3;
                else if (identifier.startsWith("ID3v2.2"))
                	id3v2_version = ID3_V_2_2;
                else
                	id3v2_version = ID3_V_2_4;
                if (log.isDebugEnabled())
                	log.debug("JAudioTagWriter(): id3v2 identifier=" + identifier + " (" + id3v2_version + ")");
                populateTXXXFrameMap();
                if (id3v1 instanceof ID3v11Tag) {
                    id3v11 = (ID3v11Tag)id3v1;
                }
                if (REMOVE_DEPRACATED_FRAMES) {
                	if (log.isTraceEnabled())
                		log.trace("JAudioTagWriter(): removing deprecated frames");            	                	                	
	                // remove any frames with deprecated body
	                Vector removeFrameIdentifiers = new Vector();
	                Set frameSet = id3v2.frameMap.entrySet();
	                if (frameSet != null) {
	                    Iterator iter = frameSet.iterator();
	                    while (iter.hasNext()) {
	                        Entry entry = (Entry)iter.next();
	                        Object value = entry.getValue();
	                        if (value instanceof ArrayList) {
	                            ArrayList list = (ArrayList)value;
	                            Iterator aiter = list.iterator();
	                            while (aiter.hasNext()) {
	                                AbstractID3v2Frame frame = (AbstractID3v2Frame)aiter.next();
	                                if (frame.getBody() instanceof FrameBodyDeprecated) {
	                                    if (log.isDebugEnabled()) 
	                                    	log.debug("JAudioTagWriter(): found frame with deprecated body=" + frame.getIdentifier());
	                                    removeFrameIdentifiers.add(frame.getIdentifier());
	                                }                                
	                            }
	                        } else if (value instanceof AbstractID3v2Frame) {
	                            AbstractID3v2Frame frame = (AbstractID3v2Frame)value;
	                            if (frame.getBody() instanceof FrameBodyDeprecated) {
	                                if (log.isDebugEnabled())
	                                	log.debug("JAudioTagWriter(): found frame with deprecated body=" + frame.getIdentifier());
	                                removeFrameIdentifiers.add(frame.getIdentifier());
	                            }
	                        }
	                    }                    
	                }
	                for (int f = 0; f < removeFrameIdentifiers.size(); ++f) {
	                    id3v2.removeFrame((String)removeFrameIdentifiers.get(f));
	                }
                }
            }
        } catch (Exception e) {
            log.error("JAudioTagWriter(): error Exception, filename=" + filename, e);
        }
    }
    
    public boolean save() {
        boolean success = false;
        try {
            if (id3v2 != null) {
                ArrayList arraylist = new ArrayList();
                Collection txxx_frames = txxx_frame_map.values();
                if (txxx_frames != null) {
                    Iterator iter = txxx_frames.iterator();
                    while (iter.hasNext()) {
                        arraylist.add(iter.next());
                    }
                	if (log.isTraceEnabled())
                		log.trace("save(): txxx frame arraylist=" + arraylist);            	                	                	    	
                    id3v2.setFrame(FRAME_TXXX, arraylist);
                }
            }
            if (mp3file != null) {
                mp3file.save();
                success = true;
                if (log.isDebugEnabled())
                	log.debug("save(): tag successfully written to filename=" + filename);            
            }
        } catch (Exception e) {
            log.error("save(): could not save tag to filename=" + filename, e);
        }
        return success;        
    }    
    
    public void setAlbum(String album) {
    	if (log.isTraceEnabled())
    		log.trace("setAlbum(): album=" + album);            	                	                	    	
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.ALBUM, album));
        	} catch (Exception e) { 
        		log.debug("setAlbum(): error", e);
        	}
        	/*
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_ALBUM);
            if (frame != null) {
                FrameBodyTALB frame_body = (FrameBodyTALB)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(album);
                }
            }
            if (!frame_exists) {
                AbstractTagFrameBody frameBody = new FrameBodyTALB((byte) 0, album);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_ALBUM));
            }
            */
        }
        if (id3v1 != null) {
            id3v1.setAlbum(album);
        }
    }
    
    public void setAlbumCover(String filename, String album) {
    	if (log.isTraceEnabled())
    		log.trace("setAlbumCover(): album=" + album + ", filename=" + filename);            	                	                	    	
    	if (id3v2 != null) {
            try {
                Vector previous_apic_frames = new Vector();
                Iterator frames = getFrameOfType(FRAME_ALBUM_COVER);
                while (frames.hasNext()) {
                    AbstractID3v2Frame frame = (AbstractID3v2Frame)frames.next();
                    if (log.isTraceEnabled()) log.trace("setAlbumCover(): found exising APIC frame to remove=" + frame);
                    previous_apic_frames.add(frame);
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
		            id3v2.setFrame(getNewFrame(frameBody, FRAME_ALBUM_COVER));
                } else {
                	log.warn("setAlbumCover(): album cover file doesn't exist=" + file);
                }
            } catch (Exception e) {
                log.error("setAlbumCover(): error setting album cover filename=" + filename, e);
            }
        }
    }
    
    public void setArtist(String artist) {
    	if (log.isTraceEnabled())
    		log.trace("setArtist(): artist=" + artist);            	                	                	    	
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.ARTIST, artist));
        	} catch (Exception e) { 
        		log.debug("setArtist(): error", e);
        	}
        	/*
            // lead performer
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_LEAD_PERFORMER);
            if (frame != null) {
                FrameBodyTPE1 frame_body = (FrameBodyTPE1)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(artist);
                }
            }
            if (!frame_exists) {
                AbstractTagFrameBody frameBody = new FrameBodyTPE1((byte) 0, artist);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_LEAD_PERFORMER));
            }
            */
            /*
            // composer
            frame_exists = false;
            frame = getFrame(FRAME_COMPOSER);
            if (frame != null) {
                FrameBodyTCOM frame_body = (FrameBodyTCOM)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(artist);
                }
            }
            if (!frame_exists) {
                AbstractTagFrameBody frameBody = new FrameBodyTCOM((byte) 0, artist);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_COMPOSER));
            }
            // original performer
            frame_exists = false;
            frame = getFrame(FRAME_ORIGINAL_PERFORMER);
            if (frame != null) {
                FrameBodyTOPE frame_body = (FrameBodyTOPE)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(artist);
                }
            }            
            if (!frame_exists) {
                AbstractTagFrameBody frameBody = new FrameBodyTOPE((byte) 0, artist);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_ORIGINAL_PERFORMER));
            }
            */            
        }
        if (id3v1 != null) {
            id3v1.setArtist(artist);
        }
    }
    
    public void setBeatIntensity(int beat_intensity) {
    	if (log.isTraceEnabled())
    		log.trace("setBeatIntensity(): beat_intensity=" + beat_intensity);            	                	                	    	
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_BEAT_INTENSITY, String.valueOf(beat_intensity));
            txxx_frame_map.put(TXXX_BEAT_INTENSITY, getNewFrame(frameBody, FRAME_TXXX));
        }
    }
    
    public void setBpm(int bpm) {
    	if (log.isTraceEnabled())
    		log.trace("setBpm(): bpm=" + bpm);            	                	                	    	
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.BPM, String.valueOf(bpm)));
        	} catch (Exception e) { 
        		log.debug("setBpm(): error", e);
        	}
        	/*
            try {
                boolean frame_exists = false;
                AbstractID3v2Frame frame = getFrame(FRAME_BPM);
                if (frame != null) {
                    FrameBodyTBPM bpm_frame = (FrameBodyTBPM)frame.getBody();
                    if (bpm_frame != null) {
                        frame_exists = true;
                        bpm_frame.setText(String.valueOf(bpm));
                    }
                }        
                if (!frame_exists) {
                    AbstractID3v2FrameBody frameBody = new FrameBodyTBPM((byte) 0, String.valueOf(bpm));
                    id3v2.setFrame(getNewFrame(frameBody, FRAME_BPM));
                }
            } catch (Exception e) {
                log.debug("setBpm(): error", e);
            }
            */                     
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_BPM_FINALSCRATCH, String.valueOf(bpm));
            txxx_frame_map.put(TXXX_BPM_FINALSCRATCH, getNewFrame(frameBody, FRAME_TXXX));   
        }        
    }
    
    public void setBpmFloat(float bpm) {        
    	if (log.isTraceEnabled())
    		log.trace("setBpmFloat(): bpm=" + bpm);            	                	                	    	
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.BPM, String.valueOf(bpm)));
        	} catch (Exception e) { 
        		log.debug("setBpm(): error", e);
        	}
        	/*
            try {
                boolean frame_exists = false;
                AbstractID3v2Frame frame = getFrame(FRAME_BPM);
                if (frame != null) {
                    FrameBodyTBPM bpm_frame = (FrameBodyTBPM)frame.getBody();
                    if (bpm_frame != null) {
                        frame_exists = true;
                        bpm_frame.setText(String.valueOf(bpm));
                    }
                }        
                if (!frame_exists) {
                    AbstractID3v2FrameBody frameBody = new FrameBodyTBPM((byte) 0, String.valueOf(bpm));
                    id3v2.setFrame(getNewFrame(frameBody, FRAME_BPM));
                }
            } catch (Exception e) {
                log.debug("setBpmFloat(): error", e);
            }
            */
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_BPM_FINALSCRATCH, String.valueOf(bpm));
            txxx_frame_map.put(TXXX_BPM_FINALSCRATCH, getNewFrame(frameBody, FRAME_TXXX));                        
        }        
    }
    
    public void setBpmAccuracy(int accuracy) {
    	if (log.isTraceEnabled())
    		log.trace("setBpmAccuracy(): accuracy=" + accuracy);            	                	                	    	
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_BPM_ACCURACY, String.valueOf(accuracy));
            txxx_frame_map.put(TXXX_BPM_ACCURACY, getNewFrame(frameBody, FRAME_TXXX));
        }        
    }
    
    public void setBpmStart(float start_bpm) {
    	if (log.isTraceEnabled())
    		log.trace("setBpmStart(): start_bpm=" + start_bpm);            	                	                	    	
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_BPM_START, String.valueOf(start_bpm));
            txxx_frame_map.put(TXXX_BPM_START, getNewFrame(frameBody, FRAME_TXXX));
        }
    }

    public void setBpmEnd(float end_bpm) {
    	if (log.isTraceEnabled())
    		log.trace("setBpmEnd(): end_bpm=" + end_bpm);            	                	                	    	
        if (id3v2 != null) {        
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_BPM_END, String.valueOf(end_bpm));
            txxx_frame_map.put(TXXX_BPM_END, getNewFrame(frameBody, FRAME_TXXX));
        }        
    }
    
    public void setCatalogId(String value) {
    	if (log.isTraceEnabled())
    		log.trace("setCatalogId(): value=" + value);            	                	                	    	
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_CATALOG_ID, value);
            txxx_frame_map.put(TXXX_CATALOG_ID, getNewFrame(frameBody, FRAME_TXXX));
        }        
    }
    
    public void setComments(String comments) {
    	if (log.isTraceEnabled())
    		log.trace("setComments(): comments=" + comments);            	                	                	    	
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.COMMENT, comments));
        	} catch (Exception e) { 
        		log.debug("setComment(): error", e);
        	}
        	/* 
            // lead performer
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_COMMENTS);
            if (frame != null) {
                FrameBodyCOMM frame_body = (FrameBodyCOMM)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(comments);
                }
            }
            if (!frame_exists) {
                AbstractTagFrameBody frameBody = new FrameBodyCOMM((byte) 0, "en", "Comments", comments);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_COMMENTS));
            }
            */
        }
        if (id3v1 != null) {
            id3v1.setComment(comments);
        }
    }
    
    public void setContentGroupDescription(String content_group_description) {
    	if (log.isTraceEnabled())
    		log.trace("setContentGroupDescription(): content_group_description=" + content_group_description);            	                	                	    	
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.GROUPING, content_group_description));
        	} catch (Exception e) { 
        		log.debug("setContentGroupDescription(): error", e);
        	}
        	/*
        	boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_CONTENT_GROUP_DESCRIPTION);
            if (frame != null) {
                FrameBodyTIT1 frame_body = (FrameBodyTIT1)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(content_group_description);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTIT1((byte) 0, content_group_description);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_CONTENT_GROUP_DESCRIPTION));                
            }
            */
        }
    }
    
    public void setContentType(String content_type) {
    	if (log.isTraceEnabled())
    		log.trace("setContentType(): content_type=" + content_type);            	                	                	    	
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_CONTENT_TYPE);
            if (frame != null) {
                FrameBodyTCON frame_body = (FrameBodyTCON)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(content_type);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTCON((byte) 0, content_type);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_CONTENT_TYPE));                
            }
        }        
    }
    
    public void setEncodedBy(String encoded_by) {
    	if (log.isTraceEnabled())
    		log.trace("setEncodedBy(): encoded_by=" + encoded_by);            	                	                	    	
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_ENCODED_BY);
            if (frame != null) {
                FrameBodyTENC frame_body = (FrameBodyTENC)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(encoded_by);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTENC((byte) 0, encoded_by);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_ENCODED_BY));
            }
        }   
    }
    
    public void setFileType(String file_type) {
    	if (log.isTraceEnabled())
    		log.trace("setFileType(): file_type=" + file_type);            	                	                	    	
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_FILE_TYPE);
            if (frame != null) {
                FrameBodyTFLT frame_body = (FrameBodyTFLT)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(file_type);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTFLT((byte) 0, file_type);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_FILE_TYPE));
            }
        }        
    }
    
    public void setGenre(String genre) {
    	if (log.isTraceEnabled())
    		log.trace("setGenre(): genre=" + genre);            	                	                	    	
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.GENRE, genre));
        	} catch (Exception e) { 
        		log.debug("setGenre(): error", e);
        	}
        	/*
        	boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_GENRE);
            if (frame != null) {
                FrameBodyTCON frame_body = (FrameBodyTCON)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(genre);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTCON((byte) 0, genre);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_GENRE));                
            }
            */
        }
        if (id3v1 != null) {
            id3v1.setGenre(genre);
        }        
    }
    
    public void setKeyAccuracy(int accuracy) {
    	if (log.isTraceEnabled())
    		log.trace("setKeyAccuracy(): accuracy=" + accuracy);            	                	                	    	
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_KEY_ACCURACY, String.valueOf(accuracy));
            txxx_frame_map.put(TXXX_KEY_ACCURACY, getNewFrame(frameBody, FRAME_TXXX));
        }        
    }

    public void setKey(String key) {
    	if (log.isTraceEnabled())
    		log.trace("setKey(): key=" + key);            	                	                	    	
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.KEY, key));
        	} catch (Exception e) { 
        		log.debug("setKey(): error", e);
        	}
        	/*
        	boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_KEY);
            if (frame != null) {
                FrameBodyTKEY frame_body = (FrameBodyTKEY)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(key);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTKEY((byte) 0, key);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_KEY));
            }
            */
        }        
    }
    
    public void setKeyStart(String start_key) {
    	if (log.isTraceEnabled())
    		log.trace("setKeyStart(): start_key=" + start_key);            	                	                	    	
        if (id3v2 != null) {            
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_KEY_START, start_key);
            txxx_frame_map.put(TXXX_KEY_START, getNewFrame(frameBody, FRAME_TXXX));
        }        
    }
    
    public void setKeyEnd(String end_key) {
    	if (log.isTraceEnabled())
    		log.trace("setKeyEnd(): end_key=" + end_key);            	                	                	    	
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_KEY_END, end_key);
            txxx_frame_map.put(TXXX_KEY_END, getNewFrame(frameBody, FRAME_TXXX));
        }
    }
    
    public void setLanguages(String languages) {
    	if (log.isTraceEnabled())
    		log.trace("setLanguages(): languages=" + languages);            	                	                	    	
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_LANGUAGES);
            if (frame != null) {
                FrameBodyTLAN frame_body = (FrameBodyTLAN)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(languages);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTLAN((byte) 0, languages);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_LANGUAGES));
            }
        }        
    }
    
    public void setPublisher(String publisher) {
    	if (log.isTraceEnabled())
    		log.trace("setPublisher(): publisher=" + publisher);            	                	                	    	
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.RECORD_LABEL, publisher));
        	} catch (Exception e) { 
        		log.debug("setPublisher(): error", e);
        	}
        	/*        	
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_PUBLISHER);
            if (frame != null) {
                FrameBodyTPUB frame_body = (FrameBodyTPUB)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(publisher);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTPUB((byte) 0, publisher);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_PUBLISHER));
            }
            */
        }
    }
    
    public void setRating(int rating) {
    	if (log.isTraceEnabled())
    		log.trace("setRating(): rating=" + rating);            	                	                	    	
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_RATING, String.valueOf(rating));
            txxx_frame_map.put(TXXX_RATING, getNewFrame(frameBody, FRAME_TXXX));
        }
    }
    
    public void setRemix(String remix) {
    	if (log.isTraceEnabled())
    		log.trace("setRemix(): remix=" + remix);            	                	                	    	
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.REMIXER, remix));
        	} catch (Exception e) { 
        		log.debug("setRemix(): error", e);
        	}
        	/*
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_REMIXER);
            if (frame != null) {
                FrameBodyTPE4 frame_body = (FrameBodyTPE4)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(remix);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTPE4((byte) 0, remix);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_REMIXER));
            }
            */
        }
    }
        
    public void setReplayGain(float value) {
        if (log.isTraceEnabled())
            log.trace("setReplayGain(): value=" + value);
        if (id3v2 != null) {
        	FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_REPLAYGAIN_TRACK_GAIN, String.valueOf(value) + " dB");
            txxx_frame_map.put(TXXX_REPLAYGAIN_TRACK_GAIN, getNewFrame(frameBody, FRAME_TXXX));
        }
/*
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_REPLAYGAIN);
            if (frame != null) {
                FrameBodyRVA2 frame_body = (FrameBodyRVA2)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
//                    frame_body.setObjectValue(remix);
                }
            }
            if (!frame_exists) {
//                AbstractID3v2FrameBody frameBody = new FrameBodyRVA2((byte) 0, remix);
//                id3v2.setFrame(getNewFrame(frameBody, FRAME_REMIXER));
            }
        }        
        */
    }
    
    public void setSizeInBytes(int size) {
    	if (log.isTraceEnabled())
    		log.trace("setSizeInBytes(): size=" + size);            	                	                	    	
        if (id3v2 != null) {
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_SIZE);
            if (frame != null) {
                FrameBodyTSIZ frame_body = (FrameBodyTSIZ)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(String.valueOf(size));
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTSIZ((byte) 0, String.valueOf(size));
                id3v2.setFrame(getNewFrame(frameBody, FRAME_SIZE));
            }
        }        
    }
    
    public void setStyles(Vector<DegreeValue> styles) {
    	if (log.isTraceEnabled())
    		log.trace("setStyles(): styles=" + styles);            	                	                	    	
        removeStyles();
        if (styles != null) {
            for (int s = 0; s < styles.size(); ++s) {
                setStyle(styles.get(s).getName(), styles.get(s).getPercentage(), s + 1);
            }
        }
    }

    public void setTags(Vector<DegreeValue> tags) {
    	if (log.isTraceEnabled())
    		log.trace("setTags(): tags=" + tags);            	                	                	    	
        removeTags();
        if (tags != null) {
            for (int s = 0; s < tags.size(); ++s) {
                setTag(tags.get(s).getName(), tags.get(s).getPercentage(), s + 1);
            }
        }
    }
    
    public void setTime(String time) {
    	if (log.isTraceEnabled())
    		log.trace("setTime(): time=" + time);            	                	                	    	
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_TIME, time);
            txxx_frame_map.put(TXXX_TIME, getNewFrame(frameBody, FRAME_TXXX));
        }        
    }
    
    public void setTimeSignature(String time_sig) {
    	if (log.isTraceEnabled())
    		log.trace("setTimeSignature(): time_sig=" + time_sig);            	                	                	    	
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TXXX_TIME_SIGNATURE, time_sig);
            txxx_frame_map.put(TXXX_TIME_SIGNATURE, getNewFrame(frameBody, FRAME_TXXX));
        } 
    }
    
    public void setTitle(String title) {
    	if (log.isTraceEnabled())
    		log.trace("setTitle(): title=" + title);
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.TITLE, title));
        	} catch (Exception e) { 
        		log.debug("setTitle(): error", e);
        	}
        	/*
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_TITLE);
            if (frame != null) {
                FrameBodyTIT2 frame_body = (FrameBodyTIT2)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(title);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTIT2((byte) 0, title);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_TITLE));
            }
            */
        }        
        if (id3v1 != null) {
            id3v1.setTitle(title);
        }   
    }
    
    public void setTrack(String track, Integer total_tracks) {
    	if (log.isTraceEnabled())
    		log.trace("setTrack(): track=" + track + ", total_tracks=" + total_tracks);            	                	                	    	
        if (id3v2 != null) {        	
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.TRACK, track));
            	if (total_tracks != null) {
    	        	try	{
    	        		id3v2.setField(id3v2.createField(FieldKey.TRACK_TOTAL, String.valueOf(total_tracks)));
    	        	} catch (Exception e) { 
    	        		log.debug("setTrackS(): error", e);
    	        	}
            	}
        	} catch (NumberFormatException nfe) {
            	String trackText = null;            
                if (total_tracks != null) {
                    trackText = track + "/" + total_tracks.intValue();
                } else {
                    trackText = track;
                }            
                boolean frame_exists = false;
                AbstractID3v2Frame frame = getFrame(FRAME_TRACK);
                if (frame != null) {
                    FrameBodyTRCK frame_body = (FrameBodyTRCK)frame.getBody();
                    if (frame_body != null) {
                        frame_exists = true;
                        frame_body.setText(trackText);
                    }
                }
                if (!frame_exists) {
                    AbstractID3v2FrameBody frameBody = new FrameBodyTRCK((byte) 0, trackText);
                    id3v2.setFrame(getNewFrame(frameBody, FRAME_TRACK));
                }         		
        	} catch (Exception e) { 
        		log.debug("setTrack(): error", e);
        	}
        }
        if (id3v11 != null) {
            id3v11.setTrack(track);
        }                        
    }
    
    public void setUserField(String fieldName, String value) {
    	if (log.isTraceEnabled())
    		log.trace("setUserField(): fieldName=" + fieldName + ", value=" + value);            	                	                	    	
        if (id3v2 != null) {
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, TagUtil.convertToValidTagId(fieldName), value);
            txxx_frame_map.put(TagUtil.convertToValidTagId(fieldName), getNewFrame(frameBody, FRAME_TXXX));
        }        
    }

    public void setYear(String year) {
    	if (log.isTraceEnabled())
    		log.trace("setYear(): year=" + year);            	                	                	    	
        if (id3v2 != null) {
        	try	{
        		id3v2.setField(id3v2.createField(FieldKey.YEAR, year));
        	} catch (Exception e) { 
        		log.debug("setYear(): error", e);
        	}       
        	/*
            boolean frame_exists = false;
            AbstractID3v2Frame frame = getFrame(FRAME_YEAR);
            if (frame != null) {
                FrameBodyTYER frame_body = (FrameBodyTYER)frame.getBody();
                if (frame_body != null) {
                    frame_exists = true;
                    frame_body.setText(year);
                }
            }
            if (!frame_exists) {
                AbstractID3v2FrameBody frameBody = new FrameBodyTYER((byte) 0, year);
                id3v2.setFrame(getNewFrame(frameBody, FRAME_YEAR));
            }
            */
        }
        if (id3v1 != null) {
            id3v1.setYear(year);
        }                
    }
    
    private AbstractID3v2Frame getNewFrame(AbstractTagFrameBody frameBody, String identifier) {
        AbstractID3v2Frame frame = null;
        if (id3v2_version == ID3_V_2_3) {
            if (identifier == null)
                frame = new ID3v23Frame();
            else
                frame = new ID3v23Frame(identifier);
        } else { // ID3_V_2_4
            if (identifier == null)
                frame = new ID3v24Frame();           
            else
                frame = new ID3v24Frame(identifier);
        }
        frame.setBody(frameBody);
        return frame;
    }
    
    private void populateTXXXFrameMap() {
        if (id3v2 != null) {
            Iterator iter = getFrameOfType(FRAME_TXXX);
            while (iter.hasNext()) {
                AbstractID3v2Frame iter_frame = (AbstractID3v2Frame)iter.next();
                AbstractTagFrameBody frameBody = iter_frame.getBody();
                if (frameBody instanceof FrameBodyTXXX) {
                    FrameBodyTXXX txxx_frame = (FrameBodyTXXX)frameBody;                        
                    txxx_frame_map.put(txxx_frame.getDescription(), iter_frame);
                }                    
            }
        }
    }
    
    private FrameBodyTXXX getTXXXFrame(String description) {
        if (id3v2 != null) {
            Iterator iter = getFrameOfType(FRAME_TXXX);
            while (iter.hasNext()) {
                AbstractID3v2Frame iter_frame = (AbstractID3v2Frame)iter.next();
                AbstractTagFrameBody frameBody = iter_frame.getBody();
                if (frameBody instanceof FrameBodyTXXX) {
                    FrameBodyTXXX txxx_frame = (FrameBodyTXXX)frameBody;
                    if (description.equals(txxx_frame.getDescription())) return txxx_frame;
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
    
    private void removeStyles() {
        if (id3v2 != null) {
            Vector<String> remove_ids = new Vector<String>();
            Iterator iter = getFrameOfType(FRAME_TXXX);
            while (iter.hasNext()) {
                AbstractID3v2Frame iter_frame = (AbstractID3v2Frame)iter.next();
                AbstractTagFrameBody frameBody = iter_frame.getBody();
                if (frameBody instanceof FrameBodyTXXX) {
                    FrameBodyTXXX txxx_frame = (FrameBodyTXXX)frameBody;
                    if (txxx_frame.getDescription().startsWith(TXXX_STYLES_PREFIX))
                        remove_ids.add(txxx_frame.getDescription());                    
                }                
            }
            for (int f = 0; f < remove_ids.size(); ++f) {
                txxx_frame_map.remove(remove_ids.get(f));
            }
        }        
    }
    
    private void setStyle(String style, float degreeValue, int style_number) {
        if (id3v2 != null) {
            String identifier = TagUtil.getStyleTagId(style_number);
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, identifier, style);
            txxx_frame_map.put(identifier, getNewFrame(frameBody, FRAME_TXXX));
            
            identifier = TagUtil.getStyleTagDegree(style_number);
            frameBody = new FrameBodyTXXX((byte) 0, identifier, String.valueOf(degreeValue));
            txxx_frame_map.put(identifier, getNewFrame(frameBody, FRAME_TXXX));
        }        
    }    

    private void removeTags() {
        if (id3v2 != null) {
            Vector<String> remove_ids = new Vector<String>();
            Iterator iter = getFrameOfType(FRAME_TXXX);
            while (iter.hasNext()) {
                AbstractID3v2Frame iter_frame = (AbstractID3v2Frame)iter.next();
                AbstractTagFrameBody frameBody = iter_frame.getBody();
                if (frameBody instanceof FrameBodyTXXX) {
                    FrameBodyTXXX txxx_frame = (FrameBodyTXXX)frameBody;
                    if (txxx_frame.getDescription().startsWith(TXXX_TAGS_PREFIX))
                        remove_ids.add(txxx_frame.getDescription());                    
                }                
            }
            for (int f = 0; f < remove_ids.size(); ++f) {
                txxx_frame_map.remove(remove_ids.get(f));
            }
        }        
    }
    
    private void setTag(String tag, float degreeValue, int tag_number) {
        if (id3v2 != null) {
            String identifier = TagUtil.getTagTagId(tag_number);
            FrameBodyTXXX frameBody = new FrameBodyTXXX((byte) 0, identifier, tag);
            txxx_frame_map.put(identifier, getNewFrame(frameBody, FRAME_TXXX));
            
            identifier = TagUtil.getTagTagDegree(tag_number);
            frameBody = new FrameBodyTXXX((byte) 0, identifier, String.valueOf(degreeValue));
            txxx_frame_map.put(identifier, getNewFrame(frameBody, FRAME_TXXX));
        }        
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
    		/*
    		//AudioFile f = AudioFileIO.read(new File("C:/Users/JBickmore/Desktop/new music/Caribou - Swim/01 Odessa.mp3"));
    		MP3File f = new MP3File("C:/Users/JBickmore/Desktop/new music/Caribou - Swim/01 Odessa.mp3");    		    		
    		Tag tag = f.getTag();
    		AbstractID3v2Tag id3v2 = f.getID3v2Tag();
    		tag.setField(FieldKey.TITLE, "Odessa RE3 B");
    		f.save();
    		*/
    		
    		JAudioTagWriter writer = new JAudioTagWriter("C:/Users/JBickmore/Desktop/new music/Caribou - Swim/01 Odessa.mp3", TAG_MODE_UPDATE, ID3_V_2_3);
    		writer.setTitle("Odessa");
    		writer.save();
    		
    	} catch (Exception e) {
    		log.error("main(): error", e);
    	}
    }
    
}
