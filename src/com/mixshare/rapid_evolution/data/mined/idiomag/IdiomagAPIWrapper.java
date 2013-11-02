package com.mixshare.rapid_evolution.data.mined.idiomag;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.idiomag.artist.IdiomagArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;

public class IdiomagAPIWrapper extends CommonMiningAPIWrapper {

	static public String API_KEY = RE3Properties.getEncryptedProperty("idiomag_api_key");
	
	public byte getDataSource() { return DATA_SOURCE_IDIOMAG; }
	
	public MinedProfile fetchArtistProfile(ArtistProfile artistProfile) { return new IdiomagArtistProfile(artistProfile.getArtistName()); }
	public MinedProfile fetchLabelProfile(LabelProfile labelProfile) { return null; }
	public MinedProfile fetchReleaseProfile(ReleaseProfile releaseProfile) { return null; }
	public MinedProfile fetchSongProfile(SongProfile songProfile) { return null; }	
	
}
