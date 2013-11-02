package com.mixshare.rapid_evolution.audio.tags.writers;

import java.util.Vector;

import com.mixshare.rapid_evolution.audio.tags.TagConstants;
import com.mixshare.rapid_evolution.audio.tags.TagWriter;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

public abstract class AbstractTagWriter implements TagWriter, TagConstants {

    public abstract void setAlbum(String album);
    public abstract void setAlbumCover(String filename, String album);
    public abstract void setArtist(String artist);
    public abstract void setBeatIntensity(int beat_intensity);    
    public abstract void setBpm(int bpm);
    public abstract void setBpmFloat(float bpm);
    public abstract void setBpmAccuracy(int accuracy);
    public abstract void setBpmStart(float start_bpm);
    public abstract void setBpmEnd(float end_bpm);
    public abstract void setCatalogId(String value);    
    public abstract void setComments(String comments);
    public abstract void setContentGroupDescription(String content_group_description);
    public abstract void setContentType(String content_type);
    public abstract void setEncodedBy(String encoded_by);
    public abstract void setFileType(String file_type);
    public abstract void setGenre(String genre);
    public abstract void setKey(String key);
    public abstract void setKeyAccuracy(int accuracy);
    public abstract void setKeyStart(String start_key);
    public abstract void setKeyEnd(String end_key);
    public abstract void setLanguages(String languages);
    public abstract void setLyrics(String lyrics);
    public abstract void setPublisher(String publisher);
    public abstract void setRating(int rating);
    public abstract void setRemix(String remix);
    public abstract void setReplayGain(float value);    
    public abstract void setSizeInBytes(int size);
    public abstract void setStyles(Vector<DegreeValue> styles);
    public abstract void setTags(Vector<DegreeValue> tags);
    public abstract void setTime(String time);
    public abstract void setTimeSignature(String time_sig);    
    public abstract void setTitle(String title);
    public abstract void setTrack(String track, Integer total_tracks);
    public abstract void setUserField(String fieldName, String value);
    public abstract void setYear(String year);
    
}
