package com.mixshare.rapid_evolution.workflow.mining.musicbrainz;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.artist.MusicbrainzArtistProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.release.MusicbrainzReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.song.MusicbrainzSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.submitted.search.release.SubmittedRelease;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class MusicbrainzArtistFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(MusicbrainzArtistFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private ArtistProfile artistProfile;
	private MusicbrainzArtistProfile musicbrainzProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public MusicbrainzArtistFetchTask() { }
	
	public MusicbrainzArtistFetchTask(ArtistProfile artistProfile, int taskPriority) {
		this.artistProfile = artistProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_MUSICBRAINZ);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("musicbrainz_artist_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
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
		artistProfile = (ArtistProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching musicbrainz artist profile=" + artistProfile);
		try {
			musicbrainzProfile = (MusicbrainzArtistProfile)MiningAPIFactory.getMusicbrainzAPI().getArtistProfile(artistProfile);		
			if ((musicbrainzProfile != null) && musicbrainzProfile.isValid()) {
				
				if (!artistProfile.isExternalItem() && RE3Properties.getBoolean("automatically_add_external_items")) {			
					// releases & songs...
					for (MusicbrainzReleaseProfile release : musicbrainzProfile.getReleaseProfiles()) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						if (release.getArtistDescription().equalsIgnoreCase(artistProfile.getArtistName())) {
							String artistMbid = release.getArtistMbId();
							String artistName = MiningAPIFactory.getMusicbrainzAPI().getLocalArtistName(artistMbid);
							if (artistName == null)
								artistName = release.getArtistDescription();						
							SubmittedRelease submittedRelease = new SubmittedRelease(artistName, release.getReleaseTitle());
							submittedRelease.setExternalItem(true);
							submittedRelease.addMinedProfile(release);
							try {
								ReleaseProfile releaseProfile = (ReleaseProfile)Database.getReleaseIndex().addOrUpdate(submittedRelease);
								if (releaseProfile != null) {
									int track = 1;
									for (MusicbrainzSongProfile musicbrainzSong : release.getSongs()) {
										if (RapidEvolution3.isTerminated || isCancelled())
											return;
										artistMbid = musicbrainzSong.getArtistMbId();
										artistName = MiningAPIFactory.getMusicbrainzAPI().getLocalArtistName(artistMbid);
										if (artistName == null)
											artistName = musicbrainzSong.getArtistDescription();
										SubmittedSong submittedSong = new SubmittedSong(artistName, releaseProfile.getReleaseTitle(), StringUtil.getTrackString(track), musicbrainzSong.getSongName(), "");
										submittedSong.setExternalItem(true);
										submittedSong.setSubmittedRelease(submittedRelease);
										submittedSong.setOriginalYearReleased(submittedRelease.getOriginalYearReleased(), DATA_SOURCE_MUSICBRAINZ);
										submittedSong.addMinedProfile(musicbrainzSong);
										try {
											Database.getSongIndex().addOrUpdate(submittedSong);
										} catch (Exception e) {
											log.error("execute(): error adding/updating external song=" + submittedSong, e);
										}
										++track;
									}
								}
							} catch (Exception e) {
								log.error("execute(): error adding/updating external release=" + submittedRelease, e);
							}							
						}
					}		
				}
				
				artistProfile.setMbId(musicbrainzProfile.getMbId(), DATA_SOURCE_MUSICBRAINZ, false);
				
				artistProfile.addMinedProfile(musicbrainzProfile);
				artistProfile.save();
			} else {
				artistProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_MUSICBRAINZ), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for musicbrainz");
		}							
	}	
	
	public String toString() { return "MusicbrainzArtistFetchTask()=" + artistProfile; }
	
}
