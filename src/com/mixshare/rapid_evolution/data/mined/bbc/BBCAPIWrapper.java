package com.mixshare.rapid_evolution.data.mined.bbc;

import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.bbc.artist.BBCArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;

/**
 * http://www.bbc.co.uk/music/developers
 * 
 * As of now, the BBC API is used to get at the extracted wikipedia profile text for the artist.
 * No other information seemed to be unique or useful at the time (11/17/2009).
 */
public class BBCAPIWrapper extends CommonMiningAPIWrapper {

	public byte getDataSource() { return DATA_SOURCE_BBC; }
	
	public MinedProfile fetchArtistProfile(ArtistProfile artistProfile) { return new BBCArtistProfile(artistProfile.getArtistName()); }
	public MinedProfile fetchLabelProfile(LabelProfile labelProfile) { return null; }
	public MinedProfile fetchReleaseProfile(ReleaseProfile releaseProfile) { return null; }
	public MinedProfile fetchSongProfile(SongProfile songProfile) { return null; }
	
}
