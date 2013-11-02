package com.mixshare.rapid_evolution.data.mined.echonest.audio;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

import com.echonest.api.v4.Audio;

public class EchonestAudio implements Serializable {

    static private Logger log = Logger.getLogger(EchonestAudio.class);
    static private final long serialVersionUID = 0L;
	
    ////////////
    // FIELDS //
    ////////////
    
    private String artistId;
    private String artistName;
    private Date date;
    private float timeInSeconds;
    private String link;
    private String url;
    private String release;
    private String title;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public EchonestAudio() { }
    public EchonestAudio(Audio audio) {
    	url = audio.getURL();
    	release = audio.getRelease();
    	title = audio.getTitle();
    	link = audio.getLink();
    	try {
    		timeInSeconds = (float)audio.getLength();
    	} catch (Exception e) { }
    	date = audio.getDate();
    	artistName = audio.getArtistName();
    	artistId = null;
    }

    /////////////
    // GETTERS //
    /////////////

	public String getUrl() { return url; }
	public String getArtistId() { return artistId; }
	public String getArtistName() { return artistName; }
	public Date getDate() { return date; }
	public float getTimeInSeconds() { return timeInSeconds; }
	public String getLink() { return link; }
	public String getRelease() { return release; }
	public String getTitle() { return title; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setArtistId(String artistId) {
		this.artistId = artistId;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public void setTimeInSeconds(float timeInSeconds) {
		this.timeInSeconds = timeInSeconds;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setRelease(String release) {
		this.release = release;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(artistName);
		result.append(" - ");
		result.append(title);
		result.append(" (");
		result.append(url);
		result.append(")");
		return result.toString();
	}
	
	public boolean equals(Object o) {
		if (o instanceof EchonestAudio) {
			EchonestAudio eV = (EchonestAudio)o;
			return eV.getUrl().equals(getUrl());
		}
		return false;
	}
	
	public int hashCode() { return url.hashCode(); }
	
}
