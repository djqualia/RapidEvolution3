package com.mixshare.rapid_evolution.data.mined.lastfm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.roarsoftware.lastfm.ImageSize;
import net.roarsoftware.lastfm.MusicEntry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.profile.common.link.InvalidLinkException;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;

/**
 * Combines common lastfm profile fields/functionality...
 */
abstract public class LastfmCommonProfile extends MinedProfile {

    static private Logger log = Logger.getLogger(LastfmCommonProfile.class);    

    static public byte LASTFM_IMAGE_SIZE_MEGA = 0;
    static public byte LASTFM_IMAGE_SIZE_HUGE = 1;
    static public byte LASTFM_IMAGE_SIZE_EXTRALARGE = 2;
    static public byte LASTFM_IMAGE_SIZE_LARGESQUARE = 3;
    static public byte LASTFM_IMAGE_SIZE_LARGE = 4;
    static public byte LASTFM_IMAGE_SIZE_ORIGINAL = 5;
    static public byte LASTFM_IMAGE_SIZE_MEDIUM = 6;
    static public byte LASTFM_IMAGE_SIZE_SMALL = 7;
            
    ////////////
    // FIELDS //
    ////////////
    
    protected long lastUpdated = System.currentTimeMillis();
    protected float numListeners;
    protected float playCount;
    protected String mbid;
    protected String url;
    protected Date wikiChanged;
    protected String wikiSummary;
    protected String wikiText;
    protected boolean isStreamable;
    protected Map<Byte, String> imageURLs;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public LastfmCommonProfile() { super(); }
    public LastfmCommonProfile(byte dataType) {
    	super(new MinedProfileHeader(dataType, DATA_SOURCE_LASTFM));
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public boolean isValid() {
    	if ((url != null) && (url.length() > 0))
    		return true;
    	return false;
    }    

	public float getNumListeners() { return numListeners; }
	public float getPlayCount() { return playCount; }
	public String getMbid() { return mbid; }
	public String getUrl() { return url; }
	public Link getLink() {
		try {
			if (url != null)
				return new Link("", "", url, "Lastfm", DATA_SOURCE_LASTFM);
		} catch (InvalidLinkException il) { }
		return null;
	}
	public Date getWikiChanged() { return wikiChanged; }
	public String getWikiSummary() { return wikiSummary; }
	public String getWikiText() { return wikiText; }
	public boolean isStreamable() { return isStreamable; }
    
    /**
     * Returns the largest image URL available...
     */
    public String getImageURL() {
    	if (imageURLs.containsKey(LASTFM_IMAGE_SIZE_MEGA))
    		return imageURLs.get(LASTFM_IMAGE_SIZE_MEGA);
    	if (imageURLs.containsKey(LASTFM_IMAGE_SIZE_HUGE))
    		return imageURLs.get(LASTFM_IMAGE_SIZE_HUGE);
    	if (imageURLs.containsKey(LASTFM_IMAGE_SIZE_EXTRALARGE))
    		return imageURLs.get(LASTFM_IMAGE_SIZE_EXTRALARGE);
    	if (imageURLs.containsKey(LASTFM_IMAGE_SIZE_LARGESQUARE))
    		return imageURLs.get(LASTFM_IMAGE_SIZE_LARGESQUARE);
    	if (imageURLs.containsKey(LASTFM_IMAGE_SIZE_LARGE))
    		return imageURLs.get(LASTFM_IMAGE_SIZE_LARGE);
    	if (imageURLs.containsKey(LASTFM_IMAGE_SIZE_ORIGINAL))
    		return imageURLs.get(LASTFM_IMAGE_SIZE_ORIGINAL);
    	if (imageURLs.containsKey(LASTFM_IMAGE_SIZE_MEDIUM))
    		return imageURLs.get(LASTFM_IMAGE_SIZE_MEDIUM);
    	if (imageURLs.containsKey(LASTFM_IMAGE_SIZE_SMALL))
    		return imageURLs.get(LASTFM_IMAGE_SIZE_SMALL);    	
    	return null;
    }
    public Image getImage() {
    	try {
    		String imageUrl = getImageURL();
    		if (imageUrl != null)
    			return new Image(imageUrl, DATA_SOURCE_LASTFM);
    	} catch (InvalidImageException ie) { }
    	return null;
    }
    
	public long getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public Map<Byte, String> getImageURLs() {
		return imageURLs;
	}
    
    /////////////
    // SETTERS //
    /////////////
    
	public void setImageURLs(Map<Byte, String> imageURLs) {
		this.imageURLs = imageURLs;
	}
	public void setNumListeners(float numListeners) {
		this.numListeners = numListeners;
	}
	public void setPlayCount(float playCount) {
		this.playCount = playCount;
	}
	public void setMbid(String mbid) {
		this.mbid = mbid;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setWikiChanged(Date wikiChanged) {
		this.wikiChanged = wikiChanged;
	}
	public void setWikiSummary(String wikiSummary) {
		this.wikiSummary = wikiSummary;
	}
	public void setWikiText(String wikiText) {
		this.wikiText = wikiText;
	}
	public void setStreamable(boolean isStreamable) {
		this.isStreamable = isStreamable;
	}
	
    /////////////
    // METHODS //
    /////////////
    
    public void loadCommonInfo(MusicEntry entry) {
    	try {
    		this.numListeners = entry.getListeners();
    		this.playCount = entry.getPlaycount();
    		this.mbid = entry.getMbid();
    		this.url = entry.getUrl();
    		this.wikiChanged = entry.getWikiLastChanged();
    		this.wikiSummary = entry.getWikiSummary();
    		this.wikiText = entry.getWikiText();
    		this.isStreamable = entry.isStreamable();    		
    		this.imageURLs = new HashMap<Byte, String>();
        	for (ImageSize imageSize : entry.availableSizes()) {
        		String imageUrl = entry.getImageURL(imageSize);
        		if ((imageUrl != null) && (imageUrl.length() > 0)) {
        			byte lastfmImageSize = 0;
        			if (imageSize == ImageSize.MEGA)
        				lastfmImageSize = LASTFM_IMAGE_SIZE_MEGA;
        			else if (imageSize == ImageSize.HUGE)
        				lastfmImageSize = LASTFM_IMAGE_SIZE_HUGE;
        			else if (imageSize == ImageSize.EXTRALARGE)
        				lastfmImageSize = LASTFM_IMAGE_SIZE_EXTRALARGE;
        			else if (imageSize == ImageSize.LARGESQUARE)
        				lastfmImageSize = LASTFM_IMAGE_SIZE_LARGESQUARE;
        			else if (imageSize == ImageSize.LARGE)
        				lastfmImageSize = LASTFM_IMAGE_SIZE_LARGE;
        			else if (imageSize == ImageSize.ORIGINAL)
        				lastfmImageSize = LASTFM_IMAGE_SIZE_ORIGINAL;
        			else if (imageSize == ImageSize.MEDIUM)
        				lastfmImageSize = LASTFM_IMAGE_SIZE_MEDIUM;
        			else if (imageSize == ImageSize.SMALL)
        				lastfmImageSize = LASTFM_IMAGE_SIZE_SMALL;
        			imageURLs.put(lastfmImageSize, imageUrl);
        		}
        	}        	        	
    	} catch (Exception e) {
    		log.error("loadCommonInfo(): error", e);
    	}
    }
    
}
