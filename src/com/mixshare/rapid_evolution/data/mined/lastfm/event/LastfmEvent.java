package com.mixshare.rapid_evolution.data.mined.lastfm.event;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import net.roarsoftware.lastfm.Event;
import net.roarsoftware.lastfm.ImageSize;
import net.roarsoftware.lastfm.Event.TicketSupplier;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmCommonProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.venue.LastfmVenue;

public class LastfmEvent implements Serializable {

    static private Logger log = Logger.getLogger(LastfmEvent.class);
    static private final long serialVersionUID = 0L;
            
    ////////////
    // FIELDS //
    ////////////
    
    private int id;
    private String title;
    private String description;
    private String headliner;
    private String website;
	private String url;
    private Vector<String> artists = new Vector<String>();
    private LastfmVenue venue;
    private int attendance;
    private Date startDate;
	private int numReviews;
    private Map<Byte, String> imageURLs;
    private Vector<LastfmTicketSupplier> ticketSuppliers = new Vector<LastfmTicketSupplier>();
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public LastfmEvent() { }
    public LastfmEvent(Event event) {
    	for (String artist : event.getArtists())
    		artists.add(artist);
    	attendance = event.getAttendance();
    	description = event.getDescription();
    	headliner = event.getHeadliner();
    	id = event.getId();
    	website = event.getWebsite();
    	url = event.getUrl();
    	venue = new LastfmVenue(event.getVenue());
    	title = event.getTitle();
    	startDate = event.getStartDate();
    	numReviews = event.getReviews();
    	for (TicketSupplier ticketSupplier : event.getTicketSuppliers())
    		ticketSuppliers.add(new LastfmTicketSupplier(ticketSupplier));
    	imageURLs = new HashMap<Byte, String>();
    	for (ImageSize imageSize : event.availableSizes()) {
    		String imageUrl = event.getImageURL(imageSize);
    		if ((imageUrl != null) && (imageUrl.length() > 0)) {
    			byte lastfmImageSize = 0;
    			if (imageSize == ImageSize.MEGA)
    				lastfmImageSize = LastfmCommonProfile.LASTFM_IMAGE_SIZE_MEGA;
    			else if (imageSize == ImageSize.HUGE)
    				lastfmImageSize = LastfmCommonProfile.LASTFM_IMAGE_SIZE_HUGE;
    			else if (imageSize == ImageSize.EXTRALARGE)
    				lastfmImageSize = LastfmCommonProfile.LASTFM_IMAGE_SIZE_EXTRALARGE;
    			else if (imageSize == ImageSize.LARGESQUARE)
    				lastfmImageSize = LastfmCommonProfile.LASTFM_IMAGE_SIZE_LARGESQUARE;
    			else if (imageSize == ImageSize.LARGE)
    				lastfmImageSize = LastfmCommonProfile.LASTFM_IMAGE_SIZE_LARGE;
    			else if (imageSize == ImageSize.ORIGINAL)
    				lastfmImageSize = LastfmCommonProfile.LASTFM_IMAGE_SIZE_ORIGINAL;
    			else if (imageSize == ImageSize.MEDIUM)
    				lastfmImageSize = LastfmCommonProfile.LASTFM_IMAGE_SIZE_MEDIUM;
    			else if (imageSize == ImageSize.SMALL)
    				lastfmImageSize = LastfmCommonProfile.LASTFM_IMAGE_SIZE_SMALL;
    			imageURLs.put(lastfmImageSize, imageUrl);
    		}
    	}  
    }
    
    /////////////
    // GETTERS //
    /////////////

    public int getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	public String getHeadliner() {
		return headliner;
	}
	public String getWebsite() {
		return website;
	}
	public String getUrl() {
		return url;
	}
	public Vector<String> getArtists() {
		return artists;
	}
	public LastfmVenue getVenue() {
		return venue;
	}
	public int getAttendance() {
		return attendance;
	}
	public Date getStartDate() {
		return startDate;
	}
	public int getNumReviews() {
		return numReviews;
	}
	public Map<Byte, String> getImageURLs() {
		return imageURLs;
	}
	public Vector<LastfmTicketSupplier> getTicketSuppliers() {
		return ticketSuppliers;
	}
	
    public String getImageURL() {
    	if (imageURLs.containsKey(LastfmCommonProfile.LASTFM_IMAGE_SIZE_MEGA))
    		return imageURLs.get(LastfmCommonProfile.LASTFM_IMAGE_SIZE_MEGA);
    	if (imageURLs.containsKey(LastfmCommonProfile.LASTFM_IMAGE_SIZE_HUGE))
    		return imageURLs.get(LastfmCommonProfile.LASTFM_IMAGE_SIZE_HUGE);
    	if (imageURLs.containsKey(LastfmCommonProfile.LASTFM_IMAGE_SIZE_EXTRALARGE))
    		return imageURLs.get(LastfmCommonProfile.LASTFM_IMAGE_SIZE_EXTRALARGE);
    	if (imageURLs.containsKey(LastfmCommonProfile.LASTFM_IMAGE_SIZE_LARGESQUARE))
    		return imageURLs.get(LastfmCommonProfile.LASTFM_IMAGE_SIZE_LARGESQUARE);
    	if (imageURLs.containsKey(LastfmCommonProfile.LASTFM_IMAGE_SIZE_LARGE))
    		return imageURLs.get(LastfmCommonProfile.LASTFM_IMAGE_SIZE_LARGE);
    	if (imageURLs.containsKey(LastfmCommonProfile.LASTFM_IMAGE_SIZE_ORIGINAL))
    		return imageURLs.get(LastfmCommonProfile.LASTFM_IMAGE_SIZE_ORIGINAL);
    	if (imageURLs.containsKey(LastfmCommonProfile.LASTFM_IMAGE_SIZE_MEDIUM))
    		return imageURLs.get(LastfmCommonProfile.LASTFM_IMAGE_SIZE_MEDIUM);
    	if (imageURLs.containsKey(LastfmCommonProfile.LASTFM_IMAGE_SIZE_SMALL))
    		return imageURLs.get(LastfmCommonProfile.LASTFM_IMAGE_SIZE_SMALL);    	
    	return null;
    }	
	
    /////////////
    // SETTERS //
    /////////////
	
    public void setId(int id) {
		this.id = id;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setHeadliner(String headliner) {
		this.headliner = headliner;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setArtists(Vector<String> artists) {
		this.artists = artists;
	}
	public void setVenue(LastfmVenue venue) {
		this.venue = venue;
	}
	public void setAttendance(int attendance) {
		this.attendance = attendance;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public void setNumReviews(int numReviews) {
		this.numReviews = numReviews;
	}
	public void setImageURLs(Map<Byte, String> imageURLs) {
		this.imageURLs = imageURLs;
	}
	public void setTicketSuppliers(Vector<LastfmTicketSupplier> ticketSuppliers) {
		this.ticketSuppliers = ticketSuppliers;
	}
	
    /////////////
    // METHODS //
    /////////////
    
    public String toString() { return title; }
        
}