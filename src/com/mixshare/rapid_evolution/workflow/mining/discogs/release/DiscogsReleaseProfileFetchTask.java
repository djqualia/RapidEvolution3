package com.mixshare.rapid_evolution.workflow.mining.discogs.release;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsRelease;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.song.DiscogsSong;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;

public class DiscogsReleaseProfileFetchTask extends CommonTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(DiscogsReleaseProfileFetchTask.class);    
	    
    ////////////
    // FIELDS //
    ////////////
    
	private ReleaseProfile releaseProfile;
	private DiscogsReleaseProfile result;
	private int taskPriority;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DiscogsReleaseProfileFetchTask(ReleaseProfile releaseProfile, TaskResultListener listener, int taskPriority) {
		this.releaseProfile = releaseProfile;
		this.taskPriority = taskPriority;
		this.setTaskResultListener(listener);
	}
	
	/////////////
	// GETTERS //
	/////////////
			
	public int getTaskPriority() { return taskPriority; }
	
	public Object getResult() { return result; }
	
	/////////////
	// METHODS //
	/////////////

	public void execute() {
		try {			
			Vector<Integer> releaseIds = MiningAPIFactory.getDiscogsAPI().searchForReleaseIds(releaseProfile.getDiscogsArtistsDescription(), releaseProfile.getReleaseTitle());
			if (log.isDebugEnabled())
				log.debug("execute(): fetching discogs release for=" + releaseProfile + ", releaseIds=" + releaseIds);
			DiscogsReleaseProfile discogsReleaseProfile = new DiscogsReleaseProfile(releaseProfile.getReleaseTitle());
			boolean foundMatch = false;
			for (Integer releaseId : releaseIds) {
				DiscogsRelease discogsRelease = MiningAPIFactory.getDiscogsAPI().getRelease(releaseId);
				if ((discogsRelease != null) && (discogsRelease.getTitle().equalsIgnoreCase(releaseProfile.getReleaseTitle()))) {
					foundMatch = true;
					discogsReleaseProfile.addRelease(discogsRelease);
				}					
			}
			if (foundMatch) {	
				if (RE3Properties.getBoolean("automatically_add_external_items")) {
					for (DiscogsSong discogsSong : discogsReleaseProfile.getPrimaryTrackSet()) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						SubmittedSong submittedSong;
						Vector<String> artists = discogsSong.getArtists();
						if ((artists == null) || (artists.size() == 0))
							submittedSong = new SubmittedSong(releaseProfile.getArtistNames(), releaseProfile.getReleaseTitle(), discogsSong.getPosition(), discogsSong.getTitleNoRemix(), discogsSong.getRemix());
						else {
							for (int i = 0; i < artists.size(); ++i) 
								artists.set(i, MiningAPIFactory.getDiscogsAPI().getLocalArtistName(artists.get(i)));																	
							submittedSong = new SubmittedSong(artists, releaseProfile.getReleaseTitle(), discogsSong.getPosition(), discogsSong.getTitleNoRemix(), discogsSong.getRemix());
						}
						submittedSong.setDuration(new Duration(discogsSong.getDuration()), DATA_SOURCE_DISCOGS);
						submittedSong.setExternalItem(true);
						submittedSong.setOriginalYearReleased(discogsReleaseProfile.getOriginalYearShort(), DATA_SOURCE_DISCOGS);
						submittedSong.setLabelNames(discogsReleaseProfile.getLabelNames());
						submittedSong.setCompilationFlag(discogsReleaseProfile.isCompilation());
						try {
							if (discogsReleaseProfile.getPrimaryImageURL().length() > 0)
								submittedSong.addImage(new Image(discogsReleaseProfile.getPrimaryImageURL(), DATA_SOURCE_DISCOGS), true);
						} catch (Exception e) {
							log.error("execute(): error fetching primary image=" + discogsReleaseProfile.getPrimaryImageURL());
						}
						try {
							Database.getSongIndex().addOrUpdate(submittedSong);
						} catch (Exception e) {
							log.error("finish(): error adding/updating external song=" + submittedSong, e);
						}
					}
				}
				
				releaseProfile.addMinedProfile(discogsReleaseProfile);
				releaseProfile.save();
				
				result = discogsReleaseProfile;
			} else {
				releaseProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_RELEASES, DATA_SOURCE_DISCOGS));
			}
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}	
	
	public String toString() { return "Fetching discogs release profile " + releaseProfile; }
	
}
