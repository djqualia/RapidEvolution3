package com.mixshare.rapid_evolution.audio.tags.writers;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.util.DegreeValue;


public abstract class BaseTagWriter extends AbstractTagWriter {
      
    public void setAlbum(String album) { }
    public void setAlbumCover(String filename, String album) { }
    public void setArtist(String artist) { }
    public void setBeatIntensity(int beat_intensity) { }    
    public void setBpm(int bpm) { }
    public void setBpmFloat(float bpm) { }
    public void setBpmAccuracy(int accuracy) { }
    public void setBpmStart(float start_bpm) { }
    public void setBpmEnd(float end_bpm) { }
    public void setCatalogId(String value) { }    
    public void setComments(String comments) { }
    public void setContentGroupDescription(String content_group_description) { }
    public void setContentType(String content_type) { }
    public void setEncodedBy(String encoded_by) { }
    public void setFileType(String file_type) { }
    public void setGenre(String genre) { }
    public void setKey(String key) { }
    public void setKeyAccuracy(int accuracy) { }
    public void setKeyStart(String start_key) { }
    public void setKeyEnd(String end_key) { }
    public void setLanguages(String languages) { }
    public void setLyrics(String lyrics) { }
    public void setPublisher(String publisher) { }
    public void setRating(int rating) { }
    public void setRemix(String remix) { }
    public void setReplayGain(float value) { }
    public void setSizeInBytes(int size) { }
    public void setStyles(Vector<DegreeValue> styles) { }
    public void setTags(Vector<DegreeValue> tags) { }
    public void setTime(String time) { }
    public void setTimeSignature(String time_sig) { }
    public void setTitle(String title) { }
    public void setTrack(String track, Integer total_tracks) { }
    public void setUserField(String fieldTitle, String value) { }
    public void setYear(String year) { }
    
}
