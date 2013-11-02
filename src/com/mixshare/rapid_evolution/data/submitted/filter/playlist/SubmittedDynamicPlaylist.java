package com.mixshare.rapid_evolution.data.submitted.filter.playlist;

import com.mixshare.rapid_evolution.data.profile.filter.playlist.DynamicPlaylistProfile;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;


public class SubmittedDynamicPlaylist extends SubmittedPlaylist {

	public SubmittedDynamicPlaylist(String playlistName) {
		super(playlistName);
	}
	public SubmittedDynamicPlaylist(DynamicPlaylistProfile dynamicPlaylist) {
		super(dynamicPlaylist);
	}
	
	////////////
	// FIELDS //
	////////////
	
    private ArtistSearchParameters artistSearchParameters;
    private LabelSearchParameters labelSearchParameters;
    private ReleaseSearchParameters releaseSearchParameters;
    private SongSearchParameters songSearchParameters;
	
	/////////////
	// GETTERS //
	/////////////
	
	public ArtistSearchParameters getArtistSearchParameters() { return artistSearchParameters; }
	public LabelSearchParameters getLabelSearchParameters() { return labelSearchParameters; }
	public ReleaseSearchParameters getReleaseSearchParameters() { return releaseSearchParameters; }
	public SongSearchParameters getSongSearchParameters() { return songSearchParameters; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setArtistSearchParameters(ArtistSearchParameters artistSearchParameters) { this.artistSearchParameters = artistSearchParameters; }
	public void setLabelSearchParameters(LabelSearchParameters labelSearchParameters) { this.labelSearchParameters = labelSearchParameters; }
	public void setReleaseSearchParameters( ReleaseSearchParameters releaseSearchParameters) { this.releaseSearchParameters = releaseSearchParameters; }
	public void setSongSearchParameters(SongSearchParameters songSearchParameters) { this.songSearchParameters = songSearchParameters; }
		
}
