package com.mixshare.rapid_evolution.data.submitted.search.song;

import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;

public class SubmittedMixout extends SubmittedProfile {

	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SubmittedMixout(MixoutIdentifier mixoutId, float bpmDiff) {
		identifier = mixoutId;		
		this.bpmDiff = bpmDiff;		
	}
	public SubmittedMixout(MixoutIdentifier mixoutId, MixoutProfile profile) {
		identifier = mixoutId;
		comments = profile.getComments();
		bpmDiff = profile.getBpmDiff();
		type = profile.getType();
		isSyncedWithMixshare = profile.isSyncedWithMixshare();
		rating = profile.getRating();
		ratingSource = profile.getRatingSource();
		disabled = profile.isDisabled();
	}
	
	////////////
	// FIELDS //
	////////////
	
	protected String comments;
	protected float bpmDiff;
	protected byte type;
	protected boolean isSyncedWithMixshare;
	
	/////////////
	// GETTERS //
	/////////////
	
	public String getComments() { return comments; }
	public float getBpmDiff() { return bpmDiff; }
	public byte getType() { return type; }
	public boolean isSyncedWithMixshare() { return isSyncedWithMixshare; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setComments(String comments) { this.comments = comments; }
	public void setBpmDiff(float bpmDiff) { this.bpmDiff = bpmDiff; }
	public void setType(byte type) { this.type = type; }
	public void setSyncedWithMixshare(boolean isSyncedWithMixshare) { this.isSyncedWithMixshare = isSyncedWithMixshare; }
	
}
