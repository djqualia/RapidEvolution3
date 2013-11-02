package com.mixshare.rapid_evolution.audio.tags;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jaudiotagger.tag.TagOptionSingleton;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.tags.util.TagReaderThread;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.util.io.FileLockManager;

public class TagManager {
    
	static private Logger log = Logger.getLogger(TagManager.class);
                    
    static private long TAG_READ_THREAD_MAX_WAIT_TIME = RE3Properties.getLong("tag_read_thread_max_wait_seconds") * 1000;
    
    static private Vector<TagEventListener> listeners = new Vector<TagEventListener>();
    
    static {
    	initTagManagerOptions();
    }
    
    static public void initTagManagerOptions() {
    	TagOptionSingleton.getInstance().setPadNumbers(RE3Properties.getBoolean("tag_options_pad_numbers"));
    	TagOptionSingleton.getInstance().setUnsyncTags(RE3Properties.getBoolean("tag_write_unsync"));
    	TagOptionSingleton.getInstance().setTruncateTextWithoutErrors(true);
    }
    
    static public void addTagEventListener(TagEventListener listener) {
    	listeners.add(listener);
    }    
    
    static public SubmittedSong readTags(String filename) {
        SubmittedSong song = null;
        try {        	
        	FileLockManager.startFileRead(filename);
        	if (RE3Properties.getBoolean("perform_tag_reading_in_separate_thread")) {        	
	            TagReaderThread readerThread = new TagReaderThread(filename);
	            readerThread.start();
	            boolean doneWaiting = false;
	            song = readerThread.getResult();
	            long startTime = System.currentTimeMillis();            
	            while (!readerThread.isDone() && !doneWaiting) {
	                Thread.sleep(RE3Properties.getLong("tag_read_thread_poll_interval_millis"));                
	                long elapsed = System.currentTimeMillis() - startTime;
	                if (elapsed >= TAG_READ_THREAD_MAX_WAIT_TIME)
	                	doneWaiting = true;
	            }
	            song = readerThread.getResult();
        	} else {
        		song = TagReaderThread.readTags(filename);
        	}
        	File file = new File(filename);
        	song.setSongFilenameLastUpdated(file.lastModified());
        } catch (java.lang.OutOfMemoryError e) {
            log.error("readTags(): error out of memory", e);
			if (RapidEvolution3UI.instance != null)
				RapidEvolution3UI.instance.notifyOutOfMemory("reading the tags from file=" + filename);                        
        } catch (Exception e) {
            log.error("readTags(): error Exception", e);
        } finally {
        	FileLockManager.endFileRead(filename);	
        }        
        if (song != null) 
        	for (int i = 0; i < listeners.size(); ++i)
        		listeners.get(i).tagsRead(song);        
        return song;    
    }

    static public boolean writeTags(SongProfile song) {
        boolean success = false;
        try {
        	String filename = song.getSongFilename();
            if (log.isDebugEnabled())
                log.debug("writeTags(): song=" + song + ", filename=" + filename);            
        	FileLockManager.startFileWrite(filename);
            TagWriter tag_writer = TagWriterFactory.getTagWriter(filename);
            if (tag_writer != null) {

                if (log.isDebugEnabled())
                    log.debug("writeTags(): tag_writer_class=" + tag_writer.getClass());
                
                // album:
                try {
                    if (RE3Properties.getBoolean("tag_write_album_field") && (!song.getReleaseTitle().equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setAlbum(song.getReleaseTitle());
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing album", e);
                }
                
                // album cover:
                try {
                	String thumbnail = song.getThumbnailImageFilename();
                	if ((thumbnail == null) || (thumbnail.length() == 0))
                		thumbnail = song.getSongRecord().getBestRelatedThumbnailImageFilename();
                	boolean hasThumbnail = false;
                	if ((thumbnail != null) && (thumbnail.length() > 0))
                		hasThumbnail = true;
                	if (SearchRecord.DEFAULT_THUMBNAIL_IMAGE.equals(thumbnail)) {
                		thumbnail = null;
                		hasThumbnail = false;
                	}
                    if (RE3Properties.getBoolean("tag_write_album_cover") && (hasThumbnail || RE3Properties.getBoolean("write_empty_tag_fields")))
                    	tag_writer.setAlbumCover(thumbnail, song.getReleaseTitle());                    	
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing album cover", e);
                }                    
                
                // artist:
                try {
                    if (RE3Properties.getBoolean("tag_write_artist_field") && (!song.getArtistsDescription().equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setArtist(song.getArtistsDescription());
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing artist", e);
                }
                
                // beat intensity:
                try {
                    if (RE3Properties.getBoolean("tag_write_beat_intensity_field") && (song.getBeatIntensityValue().isValid() || RE3Properties.getBoolean("write_empty_tag_fields"))) {
                        tag_writer.setBeatIntensity(song.getBeatIntensity());
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing beat intensity", e);
                }
                
                // bpm:
                try {
                    int bpm = (int)song.getStartBpm();
                    if (RE3Properties.getBoolean("tag_write_bpm_field") && ((bpm != 0) || RE3Properties.getBoolean("write_empty_tag_fields"))) {
                        tag_writer.setBpm(bpm);
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing bpm", e);
                }
                
                // bpm float
                try {
	                if (RE3Properties.getBoolean("tag_write_bpm_decimal_values")) {
	                    float bpm = song.getStartBpm();
	                    if (RE3Properties.getBoolean("tag_write_bpm_field") && ((bpm != 0.0f) || RE3Properties.getBoolean("write_empty_tag_fields"))) {
	                        tag_writer.setBpmFloat(bpm);
	                    }                    
	                }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing bpm float", e);
                }
                
                // bpm accuracy:
                try {
                    if (RE3Properties.getBoolean("tag_write_bpm_field") && ((song.getBpmAccuracy() != 0) || RE3Properties.getBoolean("write_empty_tag_fields"))) {
                        tag_writer.setBpmAccuracy(song.getBpmAccuracy());
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing bpm accuracy", e);
                }
                
                // bpm start:
                try {
                    if (RE3Properties.getBoolean("tag_write_bpm_field") && (song.getBpmStart().isValid() || RE3Properties.getBoolean("write_empty_tag_fields"))) {
                        tag_writer.setBpmStart(song.getStartBpm());
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing start bpm", e);
                }

                // bpm end:
                try {
                    if (RE3Properties.getBoolean("tag_write_bpm_field") && (song.getBpmEnd().isValid() || RE3Properties.getBoolean("write_empty_tag_fields"))) {
                        tag_writer.setBpmEnd(song.getEndBpm());
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing end bpm", e);
                }
                
                // comments:
                try {
                    String comments = song.getComments();
                    if (RE3Properties.getBoolean("tag_write_key_to_comments")) {                    	
                        String key_comments = RE3Properties.getBoolean("tag_write_key_codes") ? song.getKeyCode().toString() : song.getKey().toString(); // using combined key
                        if (key_comments.length() > 0) {
                            if (!song.getComments().equals("") && !RE3Properties.getBoolean("tag_write_key_to_comments_only"))
                                comments = key_comments + " - " + song.getComments();
                            else
                                comments = key_comments;    
                        }
                    }
                    if (log.isDebugEnabled())
                    	log.debug("writeTags(): writing to comments=" + comments);
                    if (RE3Properties.getBoolean("tag_write_comments_field") && (!comments.equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setComments(comments);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing comments", e);
                }
                
                // genre:
                try {
                	String genre = song.getGenre();
                    if (genre != null) {
                        if (RE3Properties.getBoolean("tag_write_genre_field") && (!genre.equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                            tag_writer.setGenre(genre);
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing genre", e);
                }
                
                // group tag:
                try {
                    String group_key_tag = song.getStartKey().getGroupSSLString();
                    if (RE3Properties.getBoolean("tag_write_key_to_grouping_field") && (!group_key_tag.equals("") || RE3Properties.getBoolean("write_empty_tag_fields"))) {
                        String group_key_end_tag = song.getEndKey().getGroupSSLString();
                        if (!group_key_end_tag.equals("") && !group_key_end_tag.equals(group_key_tag))
                        	tag_writer.setContentGroupDescription(group_key_tag + " -> " + group_key_end_tag);
                        else 
                        	tag_writer.setContentGroupDescription(group_key_tag);
                    }
                    
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing group tag", e);
                }
                
                // key:
                try {
                    Key key = song.getStartKey();
                    String keyStr = key.getID3SafeKeyNotation();
                    if (RE3Properties.getBoolean("tag_write_key_codes"))
                        keyStr = key.getKeyCode().toString();
                    //if (keyStr.equals("")) keyStr = new Key(song.getEndkey()).getID3SafeKeyNotation();
                    if (RE3Properties.getBoolean("tag_write_key_field") && (!keyStr.equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setKey(keyStr);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing key", e);
                }
                
                // key accuracy:
                try {
                    if (RE3Properties.getBoolean("tag_write_key_field") && ((song.getKeyAccuracy() != 0) || RE3Properties.getBoolean("write_empty_tag_fields"))) {
                        tag_writer.setKeyAccuracy(song.getKeyAccuracy());
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing key accuracy", e);
                }
                
                // key start:
                try {
                    String keyStr = null;
                    if (RE3Properties.getBoolean("tag_write_key_codes")) {
                        keyStr = song.getStartKey().getKeyCode().toString();
                    } else {
                        keyStr = song.getStartKey().getPreferredKeyNotation();
                    }                        
                    if (RE3Properties.getBoolean("tag_write_key_field") && (!keyStr.equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setKeyStart(keyStr);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing start key", e);
                }

                // key end:
                try {
                    String keyStr = null;
                    if (RE3Properties.getBoolean("tag_write_key_codes")) {
                        keyStr = song.getEndKey().getKeyCode().toString();
                    } else {
                        keyStr = song.getEndKey().getPreferredKeyNotation();
                    }                        
                    if (RE3Properties.getBoolean("tag_write_key_field") && (!keyStr.equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setKeyEnd(keyStr);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing end key", e);
                }
                
                // lyrics:
                try {
                	Vector<LabelRecord> labels = song.getLabels();
                	if (labels.size() > 0) {
                		String label = labels.get(0).getLabelName();
                        if (RE3Properties.getBoolean("tag_write_label_field") && (!label.equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                            tag_writer.setPublisher(label);
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing genre", e);
                }                     
                                
                // lyrics:
                try {
                	String lyrics = song.getLyrics();
                    if (lyrics != null) {
                        if (RE3Properties.getBoolean("tag_write_lyrics_field") && (!lyrics.equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                            tag_writer.setLyrics(lyrics);
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing genre", e);
                }                
                
                // rating:
                try {
                    Integer rating = null;
                    if (song.getRating().getRatingStars() == (byte)1) rating = new Integer(1);
                    else if (song.getRating().getRatingStars() == (byte)2) rating = new Integer(2);
                    else if (song.getRating().getRatingStars() == (byte)3) rating = new Integer(3);
                    else if (song.getRating().getRatingStars() == (byte)4) rating = new Integer(4);
                    else if (song.getRating().getRatingStars() == (byte)5) rating = new Integer(5);
                    if (rating != null) {
                        if (RE3Properties.getBoolean("tag_write_rating_field") && ((rating.intValue() > 0) || RE3Properties.getBoolean("write_empty_tag_fields")))
                            tag_writer.setRating(rating.intValue());
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing rating", e);
                }
                
                // remix:
                try {
                    if (!RE3Properties.getBoolean("tag_write_remix_with_title")) {
                        if (RE3Properties.getBoolean("tag_write_remix_field") && (!song.getRemix().equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                            tag_writer.setRemix(song.getRemix());
                    } else {
                        if (RE3Properties.getBoolean("tag_write_remix_field") && RE3Properties.getBoolean("write_empty_tag_fields"))
                            tag_writer.setRemix("");
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing remix", e);
                }
                
                // replay gain
                try {
                    if (RE3Properties.getBoolean("tag_write_replaygain_field")) {
                        if (song.getReplayGain() != null)
                            tag_writer.setReplayGain(song.getReplayGain());
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing replay gain", e);
                }                
                
                // styles:
                try {
                	Vector<DegreeValue> actualStyleDegrees = song.getActualStyleDegrees();
                    if (RE3Properties.getBoolean("tag_write_styles_fields") && ((actualStyleDegrees.size() > 0) || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setStyles(actualStyleDegrees);                    
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing styles", e);
                }

                // tags:
                try {
                	Vector<DegreeValue> actualTagDegrees = song.getActualTagDegrees();
                    if (RE3Properties.getBoolean("tag_write_tags_fields") && ((actualTagDegrees.size() > 0) || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setTags(actualTagDegrees);                    
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing tags", e);
                }
                
                // duration/time:
                try {
                    if (RE3Properties.getBoolean("tag_write_duration_field") && (song.getDuration().isValid() || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setTime(song.getDuration().getDurationAsString());
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing time", e);
                }                
                
                // time sig:
                try {
                    if (RE3Properties.getBoolean("tag_write_time_sig_field") && (song.getTimeSig().isValid() || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setTimeSignature(song.getTimeSig().toString());
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing time signature", e);
                }                
                
                // title:
                try {
                    String title = song.getTitle();
                    if (RE3Properties.getBoolean("tag_write_remix_with_title") && !song.getRemix().trim().equals("")) {
                        if (!title.equals(""))
                            title = title + " (" + song.getRemix() + ")";
                        else title = "(" + song.getRemix() + ")";                    
                    }
                    if (RE3Properties.getBoolean("tag_write_key_to_title")) {
                        String key_comments = RE3Properties.getBoolean("tag_write_key_codes") ? song.getKeyCode().toString() : song.getKey().toString();
                        if (key_comments.length() > 0)
                            title = key_comments + " - " + title;
                    }
                    if (RE3Properties.getBoolean("tag_write_title_field") && (!title.equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setTitle(title);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing title", e);
                }                    

                // track:
                try {
                    String track = song.getTrack();
                    Integer total_tracks = null;
                    int seperator = 0;
                    while ((seperator < track.length()) && ((track.charAt(seperator) != '/') && (track.charAt(seperator) != '.') && (track.charAt(seperator) != ',') && (track.charAt(seperator) != '-')))
                        seperator++;
                    if ((seperator > 0) && (seperator != track.length())) {
                        try {
                            total_tracks = new Integer(Integer.parseInt(track.substring(seperator + 1, track.length())));
                            track = track.substring(0, seperator);
                        } catch (java.lang.NumberFormatException e) {
                            if (log.isDebugEnabled()) log.debug("writeTags(): number format exception=" + track, e);
                        }
                    }
                    if (RE3Properties.getBoolean("tag_write_track_field") && (!song.getTrack().equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                        tag_writer.setTrack(track, total_tracks);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing track", e);
                }
                
                // genre:
                try {
                	String year = song.getOriginalYearReleasedAsString();
                    if (year != null) {
                        if (RE3Properties.getBoolean("tag_write_year_field") && (!year.equals("") || RE3Properties.getBoolean("write_empty_tag_fields")))
                            tag_writer.setYear(year);
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing year", e);
                }                
                
                try {
	                if (RE3Properties.getBoolean("tag_write_custom_fields")) {
	                	for (UserDataType userDataType : Database.getSongIndex().getUserDataTypes()) {
	                		Object userData = song.getUserData(userDataType);
	                		if (userData != null) {
	                			tag_writer.setUserField(userDataType.getTitle(), userData.toString());
	                		}
	                	}
	                }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("writeTags(): error writing custom field", e);
                }
                
                // save the tags
                success = tag_writer.save();
                if (log.isDebugEnabled()) {
                    if (success) log.debug("writeTags(): success");
                    else log.debug("writeTags(): failed");
                }

            }
        } catch (java.lang.OutOfMemoryError e) {
            log.error("writeTags(): error out of memory writing tags for song=" + song);
			if (RapidEvolution3UI.instance != null)
				RapidEvolution3UI.instance.notifyOutOfMemory("writing the tags to file=" + song.getSongFilename());                                    
        } catch (Exception e) {
            log.error("writeTags(): error Exception", e);
        } finally {
        	FileLockManager.endFileWrite(song.getSongFilename());
        }        
        if ((song != null) && success) 
        	for (int i = 0; i < listeners.size(); ++i)
        		listeners.get(i).tagsWritten(song);        
        return success;
    }
    
    /**
     * This method is not tested at the moment, and might not even be used/needed in RE3...
     */
    static public boolean removeTags(SongProfile song) {
        boolean success = false;
        try {
        	FileLockManager.startFileWrite(song.getSongFilename());
            TagRemover tag_remover = TagRemoverFactory.getTagRemover(song.getSongFilename());
            if (tag_remover != null) {
                tag_remover.removeTags();
            }
        } catch (Exception e) {
            log.error("removeTags(): error Exception", e);
        } finally {
        	FileLockManager.endFileWrite(song.getSongFilename());        	
        }        
        return success;
    }    

}
