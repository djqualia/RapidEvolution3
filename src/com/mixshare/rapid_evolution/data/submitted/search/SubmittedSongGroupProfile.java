package com.mixshare.rapid_evolution.data.submitted.search;

import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.SongGroupProfile;

public class SubmittedSongGroupProfile extends SubmittedSearchProfile {
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////

	public SubmittedSongGroupProfile() {
		super();
	}
	
	public SubmittedSongGroupProfile(SongGroupProfile songGroupProfile) {
		super(songGroupProfile);
	}
	
	////////////
	// FIELDS //
	////////////
	
	protected SongIdentifier initialSongId;

	/////////////
	// GETTERS //
	/////////////
	
	public SongIdentifier getInitialSongId() { return initialSongId; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setInitialSongId(SongIdentifier initialSongId) { this.initialSongId = initialSongId; }
	
}
