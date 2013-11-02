package com.mixshare.rapid_evolution.data.submitted.filter.playlist;

import java.util.Vector;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.SubmittedFilterProfile;

abstract public class SubmittedPlaylist extends SubmittedFilterProfile {

	public SubmittedPlaylist(String playlistName) {
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower"))
			playlistName = playlistName.toLowerCase();
		else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper"))
			playlistName = playlistName.toUpperCase();			
		identifier = new PlaylistIdentifier(playlistName);
	}
	public SubmittedPlaylist(PlaylistProfile playlist) {
		super(playlist);
	}
	
	
	////////////
	// FIELDS //
	////////////
	
	private Vector<Integer> songIds = new Vector<Integer>();
		
	/////////////
	// SETTERS //
	/////////////
	
	public void setSongIds(Vector<Integer> songIds) { this.songIds = songIds; }
	
	/////////////
	// GETTERS //
	/////////////
	
	public Vector<Integer> getSongIds() { return songIds; }

	/////////////
	// SETTERS //
	/////////////
	
	public void addSongId(int uniqueId) { songIds.add(uniqueId); }

}
