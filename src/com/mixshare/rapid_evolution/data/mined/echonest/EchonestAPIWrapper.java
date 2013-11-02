package com.mixshare.rapid_evolution.data.mined.echonest;

import org.apache.log4j.Logger;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.echonest.artist.EchonestArtistProfile;
import com.mixshare.rapid_evolution.data.mined.echonest.song.EchonestSongProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;

public class EchonestAPIWrapper extends CommonMiningAPIWrapper {

    static private Logger log = Logger.getLogger(EchonestAPIWrapper.class);
	
	static public String API_KEY = RE3Properties.getEncryptedProperty("echonest_api_key");
	
	static private EchoNestAPI echonestAPI;
	
	public byte getDataSource() { return DATA_SOURCE_ECHONEST; }
	
	static public EchoNestAPI getEchoNestAPI() throws EchoNestException {
		if (echonestAPI == null) {
			echonestAPI = new EchoNestAPI(API_KEY);
			echonestAPI.setMinCommandTime(0);
		}
		return echonestAPI;
	}
	
	public MinedProfile fetchArtistProfile(ArtistProfile artistProfile) {
		try {
			getRateController().startQuery();
			return new EchonestArtistProfile(artistProfile.getArtistName());
		} catch (Exception e) {
			log.error("fetchArtistProfile(): error", e);
		}
		return null;
	}
	public MinedProfile fetchLabelProfile(LabelProfile labelProfile) { return null; }
	public MinedProfile fetchReleaseProfile(ReleaseProfile releaseProfile) { return null; }
	public MinedProfile fetchSongProfile(SongProfile songProfile) {
		try {
			if (songProfile.getSongFilename() != null) {
				EchonestSongProfile result = new EchonestSongProfile(songProfile.getSongFilename(), songProfile.getArtistsDescription(), songProfile.getSongDescription());
				getRateController().startQuery();
				if ((result != null) && (result.isValid()))
					return result;
			}
		} catch (Exception e) {
			log.error("fetchSongProfile(): error", e);
		}
		return null;		
	}
		
}
