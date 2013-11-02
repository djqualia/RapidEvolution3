package com.mixshare.rapid_evolution.data.mined;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.mined.util.MiningRateController;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.util.OSHelper;

abstract public class CommonMiningAPIWrapper implements MiningAPIWrapper, DataConstants {

    static private Logger log = Logger.getLogger(CommonMiningAPIWrapper.class);
	
    static private final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    
	////////////
	// FIELDS //
	////////////
	
	private MiningRateController rateController;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public CommonMiningAPIWrapper() {
		rateController = new MiningRateController(getDataSource());
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public byte getDataSource();
	
	abstract public MinedProfile fetchArtistProfile(ArtistProfile artistProfile);
	abstract public MinedProfile fetchLabelProfile(LabelProfile labelProfile);
	abstract public MinedProfile fetchReleaseProfile(ReleaseProfile releaseProfile);
	abstract public MinedProfile fetchSongProfile(SongProfile songProfile);
	
	/////////////
	// GETTERS //
	/////////////
	
	public MiningRateController getRateController() { return rateController; }
	
	/////////////
	// METHODS //
	/////////////
		
	public MinedProfile getArtistProfile(ArtistProfile artistProfile) throws MiningLimitReachedException {
		if (rateController.canMakeQuery()) {
			MinedProfile result = fetchArtistProfile(artistProfile);
			if ((result != null) && (result.isValid()))
				return result;
		} else throw new MiningLimitReachedException();
		return null;
	}
	
	public MinedProfile getLabelProfile(LabelProfile labelProfile) throws MiningLimitReachedException {
		if (rateController.canMakeQuery()) {
			MinedProfile result = fetchLabelProfile(labelProfile);
			if ((result != null) && (result.isValid()))
				return result;
		} else throw new MiningLimitReachedException();
		return null;
	}
	
	public MinedProfile getReleaseProfile(ReleaseProfile releaseProfile) throws MiningLimitReachedException {
		if (rateController.canMakeQuery()) {
			MinedProfile result = fetchReleaseProfile(releaseProfile);
			if ((result != null) && (result.isValid()))
				return result;
		} else throw new MiningLimitReachedException();
		return null;
	}
	
	public MinedProfile getSongProfile(SongProfile songProfile) throws MiningLimitReachedException {
		if (rateController.canMakeQuery()) {
			MinedProfile result = fetchSongProfile(songProfile);
			if ((result != null) && (result.isValid()))
				return result;
		} else throw new MiningLimitReachedException();
		return null;
	}

	public String getMinedDataDirectory() { return getMinedDataDirectory(getDataSource()); }
	static public String getMinedDataDirectory(byte dataSource) {
		return "/data/" + DataConstantsHelper.getDataSourceDescription(dataSource).toLowerCase() + "/";		
	}
	
}
