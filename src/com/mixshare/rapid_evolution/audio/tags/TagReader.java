package com.mixshare.rapid_evolution.audio.tags;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.util.DegreeValue;

public interface TagReader {

    public boolean isFileSupported();
    public String getAlbum(); 
    public String getAlbumCoverFilename();
    public String getArtist();
    public Integer getBeatIntensity();
    public Integer getBpmAccuracy();
    public Float getBpmStart();
    public Float getBpmEnd();
    public String getCatalogId();
    public String getComments();
    public String getContentGroupDescription();
    public String getContentType();
    public String getEncodedBy();
    public String getFilename();
    public String getFileType();
    public String getGenre();
    public Integer getKeyAccuracy();
    public String getKeyStart();
    public String getKeyEnd();
    public String getLanguages();
    public String getLyrics();
    public String getPublisher();
    public Integer getRating();
    public String getRemix();
    public Float getReplayGain();
    public Integer getSizeInBytes();
    public Vector<DegreeValue> getStyles();
    public Vector<DegreeValue> getTags();
    public String getTime();
    public String getTimeSignature();
    public String getTitle();
    public String getTrack();
    public Integer getTotalTracks();
    public String getUserField(String fieldName);
    public String getYear();
    
}
