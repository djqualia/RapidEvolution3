package com.mixshare.rapid_evolution.data.mined.billboard;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.billboard.artist.BillboardArtistProfile;
import com.mixshare.rapid_evolution.data.mined.billboard.song.BillboardSongProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;

/**
 * http://developer.billboard.com
 * 
 * RATE LIMITS:
 * 		2	Queries per second
 * 	1,500	Queries per day
 */
public class BillboardAPIWrapper extends CommonMiningAPIWrapper {

	static public String API_KEY = RE3Properties.getEncryptedProperty("billboard_api_key");
	
	public byte getDataSource() { return DATA_SOURCE_BILLBOARD; }
	
	public MinedProfile fetchArtistProfile(ArtistProfile artistProfile) { return new BillboardArtistProfile(artistProfile.getArtistName()); }
	public MinedProfile fetchLabelProfile(LabelProfile labelProfile) { return null; }
	public MinedProfile fetchReleaseProfile(ReleaseProfile releaseProfile) { return null; }
	public MinedProfile fetchSongProfile(SongProfile songProfile) { return new BillboardSongProfile(songProfile.getArtistsDescription(), songProfile.getSongDescription()); }	
		
}
