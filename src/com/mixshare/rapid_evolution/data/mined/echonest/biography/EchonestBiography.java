package com.mixshare.rapid_evolution.data.mined.echonest.biography;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.echonest.api.v4.Biography;

public class EchonestBiography implements Serializable {

    static private Logger log = Logger.getLogger(EchonestBiography.class);
    static private final long serialVersionUID = 0L;
	
    ////////////
    // FIELDS //
    ////////////
    
    private String id;
    private String site;
    private String text;
    private String type;
    private String url;
        
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public EchonestBiography() { }
    public EchonestBiography(Biography biography) {
    	id = null;
    	site = biography.getSite();
    	text = biography.getText();
    	type = biography.getLicenseType();
    	url = biography.getURL();
    }    
    
    /////////////
    // GETTERS //
    /////////////

    public String getId() { return id; }
	public String getUrl() { return url; }
	public String getType() { return type; }
	public String getText() { return text; }
	public String getSite() { return site; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setId(String id) {
		this.id = id;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public void setText(String text) {
		this.text = text;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setUrl(String url) {
		this.url = url;
	}		
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(id);
		result.append(" (url=");
		result.append(url);
		result.append(", type=");
		result.append(type);
		result.append(", site=");
		result.append(site);
		result.append(", text=");
		result.append(text);
		result.append(")");
		return result.toString();
	}
	
	public boolean equals(Object o) {
		if (o instanceof EchonestBiography) {
			EchonestBiography eV = (EchonestBiography)o;
			return eV.url.equals(url);
		}
		return false;
	}
	
	public int hashCode() { return url.hashCode(); }
	
}
