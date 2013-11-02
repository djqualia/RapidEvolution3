package com.mixshare.rapid_evolution.data.mined.echonest.video;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

import com.echonest.api.v4.Video;

public class EchonestVideo implements Serializable {

    static private Logger log = Logger.getLogger(EchonestVideo.class);
    static private final long serialVersionUID = 0L;
	
    ////////////
    // FIELDS //
    ////////////
    
    private String url;
    private String imageUrl;
    private String site;
    private String title;
    private Date dateFound;
        
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public EchonestVideo() { }
    public EchonestVideo(Video video) {
    	url = video.getURL();
    	imageUrl = video.getImageURL();
    	site = video.getSite();
    	title = video.getTitle();
    	dateFound = video.getDateFound();
    }
    
    /////////////
    // GETTERS //
    /////////////

	public String getUrl() { return url; }
	public String getImageUrl() {
		if (imageUrl != null) {
			String thumbPrefix = "thumb=\"";
			int index = imageUrl.indexOf(thumbPrefix);
			if (index >= 0)
				return imageUrl.substring(index + thumbPrefix.length());
		}
		return imageUrl;
	}
	public String getSite() { return site; }
	public String getTitle() { return title; }
	public Date getDateFound() { return dateFound; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setUrl(String url) {
		this.url = url;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setDateFound(Date dateFound) {
		this.dateFound = dateFound;
	}		
		
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(title);
		result.append(" (url=");
		result.append(url);
		result.append(", img=");
		result.append(getImageUrl());
		result.append(")");
		return result.toString();
	}
	
	public boolean equals(Object o) {
		if (o instanceof EchonestVideo) {
			EchonestVideo eV = (EchonestVideo)o;
			return eV.getUrl().equals(getUrl());
		}
		return false;
	}
	
	public int hashCode() { return url.hashCode(); }
	
}
