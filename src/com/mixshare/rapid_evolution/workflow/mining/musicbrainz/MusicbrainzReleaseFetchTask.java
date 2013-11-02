package com.mixshare.rapid_evolution.workflow.mining.musicbrainz;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.release.MusicbrainzReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.song.MusicbrainzSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class MusicbrainzReleaseFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(MusicbrainzReleaseFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private ReleaseProfile releaseProfile;
	private MusicbrainzReleaseProfile musicbrainzProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public MusicbrainzReleaseFetchTask() { }
	
	public MusicbrainzReleaseFetchTask(ReleaseProfile releaseProfile, int taskPriority) {
		this.releaseProfile = releaseProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_RELEASES, DATA_SOURCE_MUSICBRAINZ);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("musicbrainz_release_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() {
		if ((musicbrainzProfile != null) && (musicbrainzProfile.isValid()))
			return musicbrainzProfile;
		return null;
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getMusicbrainzAPI(); }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		releaseProfile = (ReleaseProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching musicbrainz release profile=" + releaseProfile);
		try {
			musicbrainzProfile = (MusicbrainzReleaseProfile)MiningAPIFactory.getMusicbrainzAPI().getReleaseProfile(releaseProfile);		
			if ((musicbrainzProfile != null) && musicbrainzProfile.isValid()) {			
	
				if (!releaseProfile.isExternalItem() && RE3Properties.getBoolean("automatically_add_external_items")) {
					// release songs
					int track = 1;
					for (MusicbrainzSongProfile musicbrainzSong : musicbrainzProfile.getSongs()) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						SubmittedSong submittedSong = new SubmittedSong(musicbrainzSong.getArtistDescription(), releaseProfile.getReleaseTitle(), StringUtil.getTrackString(track), musicbrainzSong.getSongName(), "");
						submittedSong.setExternalItem(true);
						submittedSong.setOriginalYearReleased(musicbrainzProfile.getOriginalYearReleasedShort(), DATA_SOURCE_MUSICBRAINZ);
						submittedSong.addMinedProfile(musicbrainzSong);
						try {
							Database.getSongIndex().addOrUpdate(submittedSong);
						} catch (Exception e) {
							log.error("execute(): error adding/updating external song=" + submittedSong, e);
						}
						++track;
					}
				}
				
				releaseProfile.addMinedProfile(musicbrainzProfile);
				releaseProfile.save();
			} else {
				releaseProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_RELEASES, DATA_SOURCE_MUSICBRAINZ), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for musicbrainz");
		}							
	}	
	
	public String toString() { return "MusicbrainzReleaseFetchTask()=" + releaseProfile; }
	
}