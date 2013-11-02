package com.mixshare.rapid_evolution.audio.tags.readers;

import com.mixshare.rapid_evolution.audio.qt.QTConstants;

abstract public class QTTagReader extends BaseTagReader implements QTConstants {
    
    abstract public String getProperty(int tag);
    
    public String getArtist() {
        return getProperty(kUserDataTextArtist);
    }
    
    public String getAlbum() {
        return getProperty(kUserDataTextAlbum);
    }
    
    public String getComments() {
        return getProperty(kUserDataTextComment);
    }
    
    public String getEncodedBy() {
        return getProperty(kUserDataTextEncodedBy);      
    }
    
    public String getGenre() {
        return getProperty(kUserDataTextGenre);
    }
    
    public String getKeyStart() {
        return getProperty(kUserDataTextKey1);
    }
    
    public String getTitle() {
        return getProperty(kUserDataTextFullName);
    }
    
    public String getTrack() {
        return getProperty(kUserDataTextTrack);
    }

    public String getYear() {
        return getProperty(kUserDataTextCreationDate);
    }    

}
