package com.mixshare.rapid_evolution.workflow.user.mining;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.profile.ProfileManager;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class DataMiningDataDeleter extends CommonTask implements DataConstants {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(DataMiningDataDeleter.class);

    public static Byte getDataSourceFromId(String id) {
		if (id.equals("enable_bbc_data_miners")) {
			return DATA_SOURCE_BBC;
		} else if (id.equals("enable_billboard_data_miners")) {
			return DATA_SOURCE_BILLBOARD;
		} else if (id.equals("enable_discogs_data_miners")) {
			return DATA_SOURCE_DISCOGS;
		} else if (id.equals("enable_echonest_data_miners")) {
			return DATA_SOURCE_ECHONEST;
		} else if (id.equals("enable_idiomag_data_miners")) {
			return DATA_SOURCE_IDIOMAG;
		} else if (id.equals("enable_lastfm_data_miners")) {
			return DATA_SOURCE_LASTFM;
		} else if (id.equals("enable_lyricsfly_data_miners")) {
			return DATA_SOURCE_LYRICSFLY;
		} else if (id.equals("enable_lyricwiki_data_miners")) {
			return DATA_SOURCE_LYRICWIKI;
		} else if (id.equals("enable_musicbrainz_data_miners")) {
			return DATA_SOURCE_MUSICBRAINZ;
		} else if (id.equals("enable_yahoo_data_miners")) {
			return DATA_SOURCE_YAHOO;
		}
		return null;
    }

    private final String dataSourceName;
    private final Byte dataSource;

    private int totalProfiles;
    private int processedProfiles = 0;

	public DataMiningDataDeleter(Byte dataSource) {
		this.dataSource = dataSource;
		this.dataSourceName = DataConstantsHelper.getDataSourceDescription(dataSource);
	}

	@Override
	public String toString() { return "Deleting mined data for source " + dataSourceName; }

	@Override
	public void execute() {
		if (dataSource == null)
			return;
    	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
    		QApplication.invokeLater(new TaskProgressLauncher(this));

    	totalProfiles = Database.getArtistIndex().getSize()
    			+ Database.getLabelIndex().getSize()
    			+ Database.getReleaseIndex().getSize()
    			+ Database.getSongIndex().getSize();
    	if (totalProfiles == 0)
    		return;

    	clearMinedProfilesFromIndex(Database.getArtistIndex());
    	clearMinedProfilesFromIndex(Database.getLabelIndex());
    	clearMinedProfilesFromIndex(Database.getReleaseIndex());
    	clearMinedProfilesFromIndex(Database.getSongIndex());
	}

	private void clearMinedProfilesFromIndex(SearchIndex index) {
		if (RapidEvolution3.isTerminated || isCancelled())
			return;
		for (int id : index.getIds()) {
			if (RapidEvolution3.isTerminated || isCancelled())
				return;
			SearchProfile profile = (SearchProfile) index.getProfile(id);
			if (profile.hasMinedProfile(dataSource)) {

				// TEMP CODE
//				RE3StatusBar.instance.showStatusMessage("Processing artist=" + ((ArtistProfile) profile).getArtistName());
//				if (profile instanceof ArtistProfile
//						&& dataSource == DATA_SOURCE_ECHONEST
//						&& profile.hasMinedProfile(DATA_SOURCE_ECHONEST)) {
//					EchonestArtistProfile echonestProfile = (EchonestArtistProfile) profile.getMinedProfile(DATA_SOURCE_ECHONEST);
//					echonestProfile.setVideos(new Vector());
//					ProfileManager.saveMinedProfile(echonestProfile, id);
//					((ArtistProfile) profile).clearVideoLinks();
//					profile.save();
//				}
//				if (profile instanceof ArtistProfile
//						&& dataSource == DATA_SOURCE_IDIOMAG
//						&& profile.hasMinedProfile(DATA_SOURCE_IDIOMAG)) {
//					IdiomagArtistProfile idiomagProfile = (IdiomagArtistProfile) profile.getMinedProfile(DATA_SOURCE_IDIOMAG);
//					idiomagProfile.setVideos(new Vector());
//					ProfileManager.saveMinedProfile(idiomagProfile, id);
//					((ArtistProfile) profile).clearVideoLinks();
//					profile.save();
//				}
				// END TEMP CODE

				profile.removeMinedProfile(dataSource);
				ProfileManager.deleteMinedProfile(index.getDataType(), dataSource, id);
				profile.save();
			}
			++processedProfiles;
			setProgress(((float)processedProfiles) / totalProfiles);
		}
	}
}
