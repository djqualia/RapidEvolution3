package com.mixshare.rapid_evolution.data.mined.idiomag.artist;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.mined.echonest.video.EchonestVideo;

public class IdiomagArtistVideo implements Serializable {

    static private Logger log = Logger.getLogger(EchonestVideo.class);
	
    static private final long serialVersionUID = 0L;
	
    ////////////
    // FIELDS //    
    ////////////
    
    private String title;
    private String location;
    private String info;
    private String thumb;
    
    ////////////
    // PUBLIC //
    ////////////
    
    public IdiomagArtistVideo() { }
    
    //////////////
    // GETTERS //
    /////////////
    
	public String getTitle() { return title; }
	public String getLocation() { return location; }
	public String getInfo() { return info; }
	public String getThumb() { return thumb; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setTitle(String title) { this.title = title; }
	public void setLocation(String location) { this.location = location; }
	public void setInfo(String info) { this.info = info; }
	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

	/////////////
	// METHODS //
	/////////////
	
    public String toString() { return title + " (" + info + ")"; }
    
}
