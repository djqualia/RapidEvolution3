package com.mixshare.rapid_evolution.data.mined.echonest.image;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.echonest.api.v4.Image;

public class EchonestImage implements Serializable {

    static private Logger log = Logger.getLogger(EchonestImage.class);
    static private final long serialVersionUID = 0L;
	
    ////////////
    // FIELDS //
    ////////////
    
    private String id;
    private String imageUrl;
    private String type;
        
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public EchonestImage() { }
    public EchonestImage(Image image) {
    	id = null;
    	imageUrl = image.getURL();
    	type = image.getLicenseType();
    }
    
    /////////////
    // GETTERS //
    /////////////

    public String getId() { return id; }
	public String getImageUrl() { return imageUrl; }
	public String getType() { return type; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setId(String id) {
		this.id = id;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public void setType(String type) {
		this.type = type;
	}		
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(id);
		result.append(" (imageUrl=");
		result.append(imageUrl);
		result.append(", type=");
		result.append(type);
		result.append(")");
		return result.toString();
	}
	
	public boolean equals(Object o) {
		if (o instanceof EchonestImage) {
			EchonestImage eV = (EchonestImage)o;
			return eV.imageUrl.equals(imageUrl);
		}
		return false;
	}
	
	public int hashCode() { return imageUrl.hashCode(); }
	
}
