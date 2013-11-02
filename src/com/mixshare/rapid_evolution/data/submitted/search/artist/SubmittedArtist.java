package com.mixshare.rapid_evolution.data.submitted.search.artist;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.submitted.search.SubmittedSongGroupProfile;

public class SubmittedArtist extends SubmittedSongGroupProfile {
 
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SubmittedArtist(String artistName) {
		super();
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower"))
			artistName = artistName.toLowerCase();
		else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper"))
			artistName = artistName.toUpperCase();				
		identifier = new ArtistIdentifier(artistName);
	}
	
	public SubmittedArtist(ArtistProfile artist) {
		super(artist);
	}	
	
	////////////
	// FIELDS //
	////////////
	

	/////////////
	// GETTERS //
	/////////////
	

	/////////////
	// SETTERS //
	/////////////
	
	
}
