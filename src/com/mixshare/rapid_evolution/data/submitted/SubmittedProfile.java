package com.mixshare.rapid_evolution.data.submitted;

import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.profile.CommonProfile;
import com.mixshare.rapid_evolution.music.rating.Rating;

/**
 * To create a new record/profile, create the appropriate submitted object and call Database.add(...) passing the identifier
 *  and submitted object...
 */
public class SubmittedProfile {
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////

	public SubmittedProfile() { }
	public SubmittedProfile(CommonProfile commonProfile) {
		identifier = commonProfile.getIdentifier();
		rating = commonProfile.getRating();
		ratingSource = commonProfile.getRatingSource();
		disabled = commonProfile.isDisabled();
	}
	
	////////////
	// FIELDS //
	////////////
	
	protected Identifier identifier;
	protected Rating rating;
	protected byte ratingSource;	
	protected boolean disabled;
	
	/////////////
	// GETTERS //
	/////////////
	
	public Identifier getIdentifier() { return identifier; }
	public Rating getRating() { return rating; }
	public byte getRatingSource() { return ratingSource; }
	public boolean isDisabled() { return disabled; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}
	
	public void setRating(Rating rating, byte ratingSource) {
		this.rating = rating;
		this.ratingSource = ratingSource;
	}
	public void setDisabled(boolean disabled) { this.disabled = disabled; }
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() { return identifier.toString(); }
	
}
