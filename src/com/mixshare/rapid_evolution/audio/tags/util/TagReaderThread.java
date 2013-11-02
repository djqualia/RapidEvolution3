package com.mixshare.rapid_evolution.audio.tags.util;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.tags.TagReader;
import com.mixshare.rapid_evolution.audio.tags.TagReaderFactory;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.record.user.UserData;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.music.timesig.TimeSig;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.video.util.VideoUtil;

public class TagReaderThread extends Thread implements DataConstants {

    private static Logger log = Logger.getLogger(TagReaderThread.class);    
    
    private boolean done = false;
    private String filename = null;
    private SubmittedSong result = null;
    
    public TagReaderThread(String filename) {
        this.filename = filename;
    }
    
    public void run() {
    	result = readTags(filename);
        done = true;
    }
    
    static public SubmittedSong readTags(String filename) {
        SubmittedSong song = null;
        try {  
        	if (log.isDebugEnabled())
        		log.debug("readTags(): reading tags for filename=" + filename);
            TagReader tag_reader = TagReaderFactory.getTagReader(filename);                        
            if (tag_reader != null) {
                
            	// must parse identifier info first...
            	String artist = null;
            	String album = null;
            	String track = null;
            	String title = null;
            	String remix = null;
            	
            	String parsedStartKeyFromTitle = null;
            	String parsedEndKeyFromTitle = null;
            	
                // artist:
                try {
                	artist = StringUtil.cleanString(tag_reader.getArtist());
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading artist", e);
                }
                if (artist == null)
                	artist = "";
            	
                // album:
                try {
                	album = StringUtil.cleanString(tag_reader.getAlbum());
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading album", e);
                }
                if (album == null)
                	album = "";
                         
                // track:
                try {
                    track = StringUtil.cleanString(tag_reader.getTrack());
                    if ((track.length() == 1) && ((track.charAt(0) >= '1') && (track.charAt(0) <= '9'))) {
                        track = "0" + track;
                    }
                    Integer total_tracks = tag_reader.getTotalTracks();
                    if ((total_tracks != null) && (total_tracks.intValue() > 0)) {
                        if (!track.endsWith("/" + String.valueOf(total_tracks))) {
                            if ((total_tracks.intValue() >= 1) && (total_tracks.intValue() <= 9))
                                track += "/0" + String.valueOf(total_tracks);
                            else
                                track += "/" + String.valueOf(total_tracks);
                        }
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading track", e);
                }
                if (track == null)
                	track = "";
                
                // title:
                try {
                    title = StringUtil.cleanString(tag_reader.getTitle());
                    int index = title.indexOf("-");
                    if (index > 0) {
                    	int dualIndex = title.indexOf("->");
                    	if (dualIndex == index) {
                    		index = title.indexOf("-", index + 1);
                    		if (index < 0)
                    			index = dualIndex;
                    	}
                        String before = title.substring(0, index).trim();
                        int before_int = -1;
                        try {
                            before_int = Integer.parseInt(before);
                        } catch (Exception e) { }
                        if (before_int > 0) {
                            title = title.substring(index + 1).trim();
                            if ((track == null) || track.equals(""))
                            	track = before;
                        } else if (dualIndex > 0) {
                        	// possibly 2 keys
                        	String keyBefore = before.substring(0, dualIndex).trim();
                        	String keyAfter = before.substring(dualIndex + 2).trim();
                        	if (Key.isWellFormed(keyBefore) && Key.isWellFormed(keyAfter)) {
                        		title = title.substring(index + 1).trim();
                        		parsedStartKeyFromTitle = keyBefore;
                        		parsedEndKeyFromTitle = keyAfter;
                        	}                        		
                        } else if (Key.isWellFormed(before)) {
                            title = title.substring(index + 1).trim();
                            parsedStartKeyFromTitle = before;
                        }
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading title", e);
                }    
                if (title == null)
                	title = "";
                
                // remix:
                try {
                    remix = StringUtil.cleanString(tag_reader.getRemix());
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading remix", e);
                }                    
                if (remix == null)
                	remix = "";

                if (remix.equals("") && !title.equals("")) {
                    remix = StringUtil.parseRemix(title);
                    title = StringUtil.parseTitle(title);
                }

                if (title.equals("") && RE3Properties.getBoolean("use_filename_as_title_if_necessary")) {
                	String fileNameOnly = FileUtil.getFilenameMinusDirectory(filename);
                	String fileExtension = FileUtil.getExtension(filename);
                	title = StringUtil.removeUnderscores(fileNameOnly.substring(0, fileNameOnly.length() - fileExtension.length() - 1));
                }
                
                if (RE3Properties.getBoolean("tag_reading_parse_title")) {
                	try {
	                    String titleToParse = title;
	                    String parsedArtist = "";
	                    String parsedAlbum = "";
	                    String parsedTrack = "";
	                    String parsedTitle = "";
	                    String parsedRemix = "";
	                    
	                    if (track.equals("")) {
		                    int i = 0;
		                    while ((i < titleToParse.length()) && Character.isDigit(titleToParse.charAt(i)))
		                    	++i;
		                    if ((i > 0) && (i <= 2) && (titleToParse.length() >= (i + 1))) {
		                    	parsedTrack = titleToParse.substring(0, i);
		                    	titleToParse = titleToParse.substring(i + 1).trim();
		                    }
	                    }
	                    
	                    int divider_index = titleToParse.indexOf(" - ");
	                    if (divider_index > 0) {
	                        parsedArtist = titleToParse.substring(0, divider_index);
	                        parsedTitle = titleToParse.substring(divider_index + 3);
	                    } else {
	                        parsedTitle = titleToParse;
	                    }
	                    titleToParse = parsedTitle;
	                    divider_index = titleToParse.indexOf(" - ");
	                    if (divider_index > 0) {
	                        parsedAlbum = titleToParse.substring(0, divider_index).trim();
	                        if (parsedAlbum.startsWith("[") && parsedAlbum.endsWith("]")) {
	                        	parsedTrack = parsedAlbum.substring(1, parsedAlbum.length() - 1);
	                        	parsedAlbum = "";
	                        }
	                        	
	                        parsedTitle = titleToParse.substring(divider_index + 3);
	                    }
	                    parsedRemix = StringUtil.parseRemix(parsedTitle);
	                    parsedTitle = StringUtil.parseTitle(parsedTitle);
	                    boolean replaced_parsedArtist = false;
	                    boolean replaced_parsedAlbum = false;
	                    boolean replaced_parsedRemixer = false;
	                    if (artist.equals("") && !parsedArtist.equals("")) {
	                        replaced_parsedArtist = true;
	                        artist = parsedArtist;
	                    }
	                    if (album.equals("") && !parsedAlbum.equals("")) {
	                        replaced_parsedAlbum = true;
	                        album = parsedAlbum;
	                    }
	                    if (((remix == null) || remix.equals("")) && !parsedRemix.equals("")) {
	                        replaced_parsedRemixer = true;
	                        remix = parsedRemix;                                    
	                    }
	                    if (replaced_parsedArtist && replaced_parsedAlbum && replaced_parsedRemixer) {
	                    	title = parsedTitle;                        
	                    } else if (replaced_parsedArtist && replaced_parsedAlbum && !replaced_parsedRemixer) {
	                        if (!parsedRemix.equals(""))
	                            title = parsedTitle + " (" + parsedRemix + ")";
	                        else if (remix.equalsIgnoreCase(parsedRemix))
	                            title = parsedTitle;
	                    } else if (replaced_parsedArtist && !replaced_parsedAlbum && replaced_parsedRemixer) {
	                        if (!parsedAlbum.equals(""))
	                            title = parsedAlbum + " - " + parsedTitle;
	                        else if (album.equalsIgnoreCase(parsedAlbum))
	                            title = parsedTitle;
	                    } else if (!replaced_parsedArtist && replaced_parsedAlbum && replaced_parsedRemixer) {
	                        if (!parsedArtist.equals(""))
	                            title = parsedArtist + " - " + parsedTitle;
	                        else if (artist.equalsIgnoreCase(parsedArtist))
	                            title = parsedTitle;
	                    } else if (replaced_parsedArtist && !replaced_parsedAlbum && !replaced_parsedRemixer) {
	                        if (!parsedAlbum.equals("") && !parsedRemix.equals(""))
	                            title = parsedAlbum + " - " + parsedTitle + " (" + parsedRemix + ")";
	                        else if (parsedAlbum.equals("") && !parsedRemix.equals(""))
	                            title = parsedTitle + " (" + parsedRemix + ")";
	                       else if (!parsedAlbum.equals("") && parsedRemix.equals(""))
	                           title = parsedAlbum + " - " + parsedTitle;
	                       else
	                           title = parsedTitle;                            
	                    } else if (!replaced_parsedArtist && !replaced_parsedAlbum && replaced_parsedRemixer) {
	                        if (!parsedAlbum.equals("") && !parsedArtist.equals(""))
	                            title = parsedArtist + " - " + parsedAlbum + " - " + parsedTitle;
	                        else if (parsedAlbum.equals("") && !parsedArtist.equals(""))
	                            title = parsedArtist + " - " + parsedTitle;
	                       else if (!parsedAlbum.equals("") && parsedArtist.equals(""))
	                           title = parsedAlbum + " - " + parsedTitle;
	                       else
	                           title = parsedTitle;
	                    } else if (!replaced_parsedArtist && replaced_parsedAlbum && !replaced_parsedRemixer) {
	                        if (!parsedArtist.equals("") && !parsedRemix.equals(""))
	                            title = parsedArtist + " - " + parsedTitle + " (" + parsedRemix + ")";
	                        else if (parsedArtist.equals("") && !parsedRemix.equals(""))
	                            title = parsedTitle + " (" + parsedRemix + ")";
	                       else if (!parsedArtist.equals("") && parsedRemix.equals(""))
	                           title = parsedArtist + " - " + parsedTitle;
	                       else
	                           title = parsedTitle;                            
	                    } else {
	                        // didn't replace anything
	                    }
	                    if ((parsedTrack.length() > 0) && (track.length() == 0)) {
	                    	track = parsedTrack;
	                    }
	                    if (parsedTitle.startsWith("[")) {
	                        int endIndex = parsedTitle.indexOf("] - ");
	                        if (endIndex > 0) {
	                            parsedTrack = parsedTitle.substring(1, endIndex);
	                            parsedTitle = parsedTitle.substring(endIndex + 4);
	                            track = parsedTrack;
	                            title = parsedTitle;
	                        }
	                    }
                	} catch (Exception e) {
                		log.error("readTags(): error parsing title=" + title, e);
                	}
                }
                
                // make sure remix isn't duplicated in title, and if so remove it
                if (StringUtil.substring(remix, title)) {
                	int index = title.toLowerCase().indexOf(remix.toLowerCase());
                	if (index > 0)
                		title = title.substring(0, index - 1).trim();
                }
                                
                song = new SubmittedSong(artist, album, track, title, remix);
                song.setSongFilename(filename);
                
                // beat intensity:
                try {
                    Integer intensity = tag_reader.getBeatIntensity();
                    if (intensity != null) song.setBeatIntensity(BeatIntensity.getBeatIntensity(intensity.intValue()), DATA_SOURCE_FILE_TAGS);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading beat intensity", e);
                }
                
                // bpm accurcy:
                byte bpmAccuracy = 0;
                try {
                    Integer accuracy = tag_reader.getBpmAccuracy();
                    if (accuracy != null)
                    	bpmAccuracy = accuracy.byteValue();
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading bpm accuracy", e);
                }
                
                // bpm start:
                Float bpmStart = null;
                try {
                    bpmStart = tag_reader.getBpmStart();
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading start bpm", e);
                }

                // bpm end:
                Float bpmEnd = null;
                try {
                    bpmEnd = tag_reader.getBpmEnd();
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading end bpm", e);
                }                
                song.setBpm(new Bpm(bpmStart), new Bpm(bpmEnd), bpmAccuracy, DATA_SOURCE_FILE_TAGS);
                
                Key keyStart = null;
                Key keyEnd = null;
                byte keyAccuracy = 0;
                
                // comments
                try {
                    String comments = tag_reader.getComments();
                    int index = comments.indexOf("-");
                    if (index > 0) {
                    	int dualIndex = comments.indexOf("->");
                    	if (dualIndex == index) {
                    		index = comments.indexOf("-", index + 1);
                    		if (index < 0)
                    			index = dualIndex;
                    	}
                        String before = comments.substring(0, index).trim();
                        if (dualIndex > 0) {
                        	// possibly 2 keys
                        	String keyBefore = before.substring(0, dualIndex).trim();
                        	String keyAfter = before.substring(dualIndex + 2).trim();
                        	if (Key.isWellFormed(keyBefore) && Key.isWellFormed(keyAfter)) {
                        		comments = comments.substring(index + 1).trim();
                        		parsedStartKeyFromTitle = keyBefore;
                        		parsedEndKeyFromTitle = keyAfter;
                        	}                        		
                        } else if (Key.isWellFormed(before)) {
                            comments = comments.substring(index + 1).trim();
                            parsedStartKeyFromTitle = before;
                        }
                    }                    
                    song.setComments(StringUtil.cleanString(comments), DATA_SOURCE_FILE_TAGS);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading comments", e);
                }
                
                // key accurcy:
                try {
                    Integer accuracy = tag_reader.getKeyAccuracy();
                    if (accuracy != null)
                    	keyAccuracy = accuracy.byteValue();
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading key accuracy", e);
                }
                
                // start key:
                try {
                    String key = StringUtil.cleanString(tag_reader.getKeyStart());
                    if (!key.equals(""))
                        keyStart = Key.getKey(key);
                    if (((keyStart == null) || !keyStart.isValid()) && (parsedStartKeyFromTitle != null))
                    	keyStart = Key.getKey(parsedStartKeyFromTitle);
                    
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading start key", e);
                }
                
                // end key:
                try {
                    keyEnd = Key.getKey(StringUtil.cleanString(tag_reader.getKeyEnd()));
                    if (!keyEnd.isValid() && (parsedEndKeyFromTitle != null))
                    	keyEnd = Key.getKey(parsedEndKeyFromTitle);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading end key", e);
                }
                song.setKey(keyStart, keyEnd, keyAccuracy, DATA_SOURCE_FILE_TAGS);
                
                // genre:
                Vector<DegreeValue> styleDegrees = new Vector<DegreeValue>();               
                try {
                    String genre = StringUtil.cleanString(tag_reader.getGenre());
                    if (genre != null) {   
                    	if (genre.startsWith("(") && genre.endsWith(")")) {
                    		try {
                    			int genreNumber = Integer.parseInt(genre.substring(1, genre.length() - 1));
                    			genre = GenreUtil.getGenre(genreNumber);
                    		} catch (Exception e) { }
                    	} else if (genre.startsWith("(") && (genre.indexOf(")") > 0)) {
                    		try {
                    			int genreNumber = Integer.parseInt(genre.substring(1, genre.indexOf(")")));
                    			genre = GenreUtil.getGenre(genreNumber);
                    		} catch (Exception e) { }                    		
                    	}
                    	styleDegrees.add(new DegreeValue(genre, 1.0f, DATA_SOURCE_FILE_TAGS));
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading genre", e);
                }                 
                
                // rating:
                try {
                    Integer rating = tag_reader.getRating();
                    if (rating != null)
                        song.setRating(Rating.getRating(rating.intValue() * 20), DATA_SOURCE_FILE_TAGS);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading rating", e);
                }
                                
                // replay gain:
                try {
                    Float replayGain = tag_reader.getReplayGain();
                    if (replayGain != null) {
                        song.setReplayGain(replayGain.floatValue(), DATA_SOURCE_FILE_TAGS);
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading replay gain", e);
                }                    

                // styles:
                try {
                    Vector<DegreeValue> styles = tag_reader.getStyles();
                    if ((styles != null) && (styles.size() > 0)) {
                    	styleDegrees = styles; // drop use of genre if styles exist
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading styles", e);
                }          
                song.setStyleDegrees(styleDegrees);

                // tags:
                Vector<DegreeValue> tagDegrees = new Vector<DegreeValue>();
                try {
                    Vector<DegreeValue> tags = tag_reader.getTags();
                    if (tags != null) {
                    	tagDegrees = tags; // drop use of genre if tags exist
                    }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading tags", e);
                }          
                song.setTagDegrees(tagDegrees);
                
                // time/duration
                try {
                    String time = StringUtil.cleanString(tag_reader.getTime());
                    if (!StringUtil.isValid(time)) {
                    	double track_time = 0.0f;
                    	if (AudioUtil.isSupportedAudioFileType(filename))
                    		track_time = AudioUtil.getDuration(filename).getDurationInSeconds();
                    	else if (VideoUtil.isSupportedVideoFileType(filename))
                    		track_time = VideoUtil.getDuration(filename).getDurationInSeconds();
                        time = new Duration(track_time * 1000.0f).getDurationAsString();
                    }
                    song.setDuration(new Duration(time), DATA_SOURCE_FILE_TAGS);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading time", e);
                }                                
                
                // time signature
                try {
                    String time_sig = StringUtil.cleanString(tag_reader.getTimeSignature());
                    song.setTimeSig(TimeSig.getTimeSig(time_sig), DATA_SOURCE_FILE_TAGS);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading time signature", e);
                }

                // custom user data
                try {
	                for (UserDataType userDataType : Database.getSongIndex().getUserDataTypes()) {
	                	String userValue = tag_reader.getUserField(userDataType.getTitle());
	                	if ((userValue != null) && !userValue.equals(""))
	                		song.addUserData(new UserData(userDataType, userValue));
	                }
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading custom user data", e);
                }
                
                // album cover:
                try {
                    String albumcover_filename = tag_reader.getAlbumCoverFilename();
                    if (log.isTraceEnabled())
                    	log.trace("readTags() read album cover=" + albumcover_filename);
                    if (albumcover_filename != null)
                    	song.addImage(new Image(albumcover_filename, albumcover_filename, DATA_SOURCE_FILE_TAGS), true);
                } catch (InvalidImageException iie) {
                	if (log.isDebugEnabled())
                		log.debug("readTags(): invalid image retrieved for album cover");
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading album cover", e);
                }                
                
                // label
                try {
                	String label = tag_reader.getPublisher();
                	if ((label != null) && !label.equals(""))
                		song.setLabelName(label);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading label", e);
                }   

                // year
                try {
                	String year = tag_reader.getYear();
                	if ((year != null) && !year.equals(""))
                		song.setOriginalYearReleased(Short.parseShort(year), DATA_SOURCE_FILE_TAGS);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading label", e);
                }   
                
                // lyrics
                try {
                	String lyrics = tag_reader.getLyrics();
                	song.setLyrics(lyrics, DATA_SOURCE_FILE_TAGS);
                } catch (Exception e) {
                	if (log.isTraceEnabled())
                		log.trace("readTags(): error reading lyrics", e);
                }   
            }
                                    
        } catch (Exception e) {
            log.error("readTags(): error Exception", e);
        }
        return song;
    }
    
    public SubmittedSong getResult() { return result; }

    public boolean isDone() { return done; }
    
}
