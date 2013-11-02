package com.mixshare.rapid_evolution.data.mined;

import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.mined.util.MiningRateController;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;

public interface MiningAPIWrapper {

	public MiningRateController getRateController();
	
	public MinedProfile getArtistProfile(ArtistProfile artistProfile) throws MiningLimitReachedException;
	public MinedProfile getLabelProfile(LabelProfile labelProfile) throws MiningLimitReachedException;
	public MinedProfile getReleaseProfile(ReleaseProfile releaseProfile) throws MiningLimitReachedException;
	public MinedProfile getSongProfile(SongProfile songProfile) throws MiningLimitReachedException;
	
}
