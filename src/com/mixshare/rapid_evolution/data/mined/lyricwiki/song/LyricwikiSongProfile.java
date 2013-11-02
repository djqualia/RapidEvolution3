package com.mixshare.rapid_evolution.data.mined.lyricwiki.song;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.lyricwiki.LyricwikiAPIWrapper;

public class LyricwikiSongProfile extends MinedProfile {

    static private Logger log = Logger.getLogger(LyricwikiSongProfile.class);
    static private final long serialVersionUID = 0L;
	
    private String artistDescription;
    private String songDescription;
    private String lyricsText;
    
    public LyricwikiSongProfile() {
    	super(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_LYRICWIKI));
    }
	public LyricwikiSongProfile(String artistDescription, String songDescription) {
		super(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_LYRICWIKI));
		this.artistDescription = artistDescription;
		this.songDescription = songDescription;
		lyricsText = LyricwikiAPIWrapper.getLyrics(artistDescription, songDescription);
	}
	
	public boolean isValid() {
		return ((lyricsText != null) && (lyricsText.length() > 0));
	}
	
	public String getLyricsText() { return lyricsText; }
	
	public String getArtistDescription() {
		return artistDescription;
	}
	public void setArtistDescription(String artistDescription) {
		this.artistDescription = artistDescription;
	}
	public String getSongDescription() {
		return songDescription;
	}
	public void setSongDescription(String songDescription) {
		this.songDescription = songDescription;
	}
	public void setLyricsText(String lyricsText) {
		this.lyricsText = lyricsText;
	}
	
	public String toString() { return artistDescription + " - " + songDescription; }

}
