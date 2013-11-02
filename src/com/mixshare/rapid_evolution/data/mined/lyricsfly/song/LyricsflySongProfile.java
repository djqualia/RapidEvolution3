package com.mixshare.rapid_evolution.data.mined.lyricsfly.song;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.util.StringUtil;

public class LyricsflySongProfile extends MinedProfile {

    static private Logger log = Logger.getLogger(LyricsflySongProfile.class);
    static private final long serialVersionUID = 0L;
	
    private String artistDescription;
    private String songDescription;
    private String lyricsText;
    private String cs; // checksum
    private String id;
    
    public LyricsflySongProfile() {
    	super(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_LYRICSFLY));
    }
	public LyricsflySongProfile(String artistDescription, String songDescription, String lyrics, String cs, String id) {
		super(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_LYRICSFLY));
		this.artistDescription = artistDescription;
		this.songDescription = songDescription;
		this.lyricsText = lyrics;
		this.cs = cs;
		this.id = id;
	}
	
	public boolean isValid() {
		return ((lyricsText != null) && (lyricsText.length() > 0));
	}
	
	public String getLyricsText() {
		String result = lyricsText;
		result = StringUtil.replace(result, "lyricsfly.com", "<a href=\"www.lyricsfly.com\">lyricsfly.com</a>");
		if ((cs != null) && (id != null))
			result += "<br/><br/><a href=\"http://lyricsfly.com/search/correction.php?" + cs + "&id=" + id + "&\">Correction Form</a>";
		return result;
	}
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
