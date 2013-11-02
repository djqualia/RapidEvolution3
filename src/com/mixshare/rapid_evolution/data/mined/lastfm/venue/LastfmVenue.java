package com.mixshare.rapid_evolution.data.mined.lastfm.venue;

import java.io.Serializable;

import net.roarsoftware.lastfm.Venue;

import org.apache.log4j.Logger;

public class LastfmVenue implements Serializable {

    static private Logger log = Logger.getLogger(LastfmVenue.class);
    static private final long serialVersionUID = 0L;
            
    ////////////
    // FIELDS //
    ////////////
    
    private String id;
    private String name;
    private String city;
    private String street;
	private String country;
    private String postalCode;
    private double longitude;
    private double latitude;
	private String timezone;
    private String url;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public LastfmVenue(Venue venue) {
    	id = venue.getId();
    	name = venue.getName();
    	city = venue.getCity();
    	street = venue.getStreet();
    	country = venue.getCountry();
    	postalCode = venue.getPostal();
    	longitude = venue.getLongitude();
    	latitude = venue.getLatitude();
    	timezone = venue.getTimezone();
    	url = venue.getUrl();
    }
    
    /////////////
    // GETTERS //
    /////////////

    public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCity() {
		return city;
	}

	public String getStreet() {
		return street;
	}

	public String getCountry() {
		return country;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public String getTimezone() {
		return timezone;
	}

	public String getUrl() {
		return url;
	}
	
    /////////////
    // SETTERS //
    /////////////
	
    public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
    /////////////
    // METHODS //
    /////////////
    
    public String toString() { return name + "@" + city; }
       
}