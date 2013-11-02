package com.mixshare.rapid_evolution.workflow.mining.discogs;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.DiscogsCommonProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsRelease;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseLabelInstance;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.song.DiscogsSong;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.submitted.search.release.SubmittedRelease;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.discogs.release.DiscogsReleaseFetchTask;

abstract public class DiscogsCommonReleaseFetchStateManager extends CommonTask implements DataConstants, TaskResultListener {

    static private Logger log = Logger.getLogger(DiscogsCommonReleaseFetchStateManager.class);
		
	////////////
	// FIELDS //
	////////////
	
	protected Vector<Integer> currentJobs; // current IDs with discogs profiles that need updating
	protected int currentIndex = 0;
	protected Profile currentProfile;
	protected DiscogsCommonProfile currentDiscogsProfile;
	protected int currentReleaseIndex = 0;
	protected Vector<DiscogsRelease> currentReleases = new Vector<DiscogsRelease>();
    private int taskPriority;
    private boolean stopAfterCurrentProfile = false;
    
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DiscogsCommonReleaseFetchStateManager() {
		super();
		taskPriority = RE3Properties.getInt("discogs_mining_task_priority");				
	}
	
	public DiscogsCommonReleaseFetchStateManager(Profile currentProfile, DiscogsCommonProfile currentDiscogsProfile, int taskPriority) {
		this.currentProfile = currentProfile;
		this.currentDiscogsProfile = currentDiscogsProfile;
		this.taskPriority = taskPriority;
		this.stopAfterCurrentProfile = true;
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract protected Vector<Integer> getDiscogsProfilesForUpdating();
	abstract protected SearchProfile getSearchProfile(Integer profileId);
	abstract public boolean finishProfile();
	
	/////////////
	// GETTERS //
	/////////////	
		
	public int getTaskPriority() { return taskPriority; }
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("discogs_release_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}		
	
	private Integer getNextIdForProcessing() {
		if ((currentJobs == null) || (currentIndex >= currentJobs.size())) {
			currentJobs = getDiscogsProfilesForUpdating();
			currentIndex = 0;
		}
		if (currentIndex < currentJobs.size()) {
			return currentJobs.get(currentIndex++);
		}
		return null;
	}
	
	public boolean isReady() {
		return RE3Properties.getBoolean("enable_discogs_data_miners");
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void startNextProcess() {
		try {
			if (RapidEvolution3.isTerminated || isCancelled())
				return;			
			if (currentProfile == null) {
				if (stopAfterCurrentProfile)
					return;
				Integer profileId = getNextIdForProcessing();
				if (profileId == null) { // if there's no work at the moment...
					if (log.isTraceEnabled())
						log.trace("startNextProcess(): no work now, sleeping...");
					SandmanThread.putBackgroundTaskToSleep(this);
					return; // want to end this thread so we don't hold up other background tasks...
				}
				SearchProfile profile = getSearchProfile(profileId);
				if (profile != null) {
					if (log.isTraceEnabled())
						log.trace("startNextProcess(): setting current profile=" + profile);
					currentProfile = profile;
					currentDiscogsProfile = (DiscogsCommonProfile)profile.getMinedProfile(DATA_SOURCE_DISCOGS);
					currentReleaseIndex = 0;
					currentReleases.clear();
				}
				if ((currentDiscogsProfile == null) || (currentDiscogsProfile.getReleaseIds().size() == 0)) {
					if (log.isTraceEnabled())
						log.trace("startNextProcess(): discogs profile removed?  skipping to next task");
					currentProfile = null;
					currentDiscogsProfile = null;
					SandmanThread.putBackgroundTaskToSleep(this); 
					//startNextProcess(); there was a stack overflow on this call
					return;
				}
			}	
			String currentReleaseId = null;
			DiscogsRelease discogsRelease = null;
			do {	
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				discogsRelease = null;
				if (currentReleaseIndex < currentDiscogsProfile.getReleaseIds().size()) {
					currentReleaseId = currentDiscogsProfile.getReleaseIds().get(currentReleaseIndex);
					// 	check disk cache
					String filename = DiscogsReleaseFetchTask.releasesDirectory + currentReleaseId + ".xml";				
					if (FileSystemAccess.getFileSystem().doesExist(filename) && ((System.currentTimeMillis() - FileSystemAccess.getFileSystem().getLastModified(filename)) < getMinimumTimeBetweenQueries())) {
						// 	use cached item					
						discogsRelease = (DiscogsRelease)XMLSerializer.readBytes(FileSystemAccess.getFileSystem().readData(filename));
						if (discogsRelease != null) {
							if (log.isTraceEnabled())
								log.trace("startNextProcess(): used cached release, id=" + currentReleaseId + ", skipping to next");
							currentReleases.add(discogsRelease);
							++currentReleaseIndex;
							if (currentReleaseIndex >= currentDiscogsProfile.getReleaseIds().size()) {
								finish();	
								startNextProcess();
								return;
							}										
						}
					}
				}
			} while (discogsRelease != null);
			if (log.isTraceEnabled())
				log.trace("startNextProcess(): initiating release fetcher for id=" + currentReleaseId);			
			TaskManager.runBackgroundTask(new DiscogsReleaseFetchTask(currentReleaseId, this));
		} catch (Exception e) {
			log.error("startProcess(): error", e);
		}
	}
	
	public void finish() {
		
		if (RapidEvolution3.isTerminated || isCancelled())
			return;
		
		if (currentReleases.size() == 0)
			return;
		
		if (log.isTraceEnabled())
			log.trace("finishArtist(): finished processing=" + currentProfile);
		
		if (finishProfile()) {		
			SearchProfile currentSearchProfile = (SearchProfile)currentProfile;			
			if (!currentSearchProfile.isExternalItem() && RE3Properties.getBoolean("automatically_add_external_items")) {
				for (DiscogsReleaseProfile discogsReleaseProfile : currentDiscogsProfile.getReleaseProfiles()) {
					if (RapidEvolution3.isTerminated || isCancelled())
						return;
					Vector<String> artistNames = discogsReleaseProfile.getArtistNames();
					// translate from discogs to local name (in case of ambiguity, i.e. justice (3) -> justice)
					for (int i = 0; i < artistNames.size(); ++i)
						artistNames.set(i, MiningAPIFactory.getDiscogsAPI().getLocalArtistName(artistNames.get(i)));											
					SubmittedRelease submittedRelease = discogsReleaseProfile.isCompilation() ? new SubmittedRelease(discogsReleaseProfile.getTitle()) : new SubmittedRelease(artistNames, discogsReleaseProfile.getTitle());
					Vector<String> labelNames = new Vector<String>();
					for (DiscogsReleaseLabelInstance labelInstance : discogsReleaseProfile.getLabelInstances())
						labelNames.add(MiningAPIFactory.getDiscogsAPI().getLocalLabelName(labelInstance.getName()));						
					submittedRelease.setLabelNames(labelNames);
					submittedRelease.setExternalItem(true);				
					submittedRelease.addMinedProfile(discogsReleaseProfile);
					try {
						ReleaseProfile releaseProfile = (ReleaseProfile)Database.getReleaseIndex().addOrUpdate(submittedRelease);
						if (releaseProfile != null) {
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
								submittedSong.setLabelNames(labelNames);
								submittedSong.setCompilationFlag(discogsReleaseProfile.isCompilation());
								try {
									if (discogsReleaseProfile.getPrimaryImageURL().length() > 0)
										submittedSong.addImage(new Image(discogsReleaseProfile.getPrimaryImageURL(), DATA_SOURCE_DISCOGS), true);
								} catch (Exception e) {
									log.error("finish(): error fetching primary image=" + discogsReleaseProfile.getPrimaryImageURL());
								}							
								submittedSong.setSubmittedRelease(submittedRelease);
								try {
									Database.getSongIndex().addOrUpdate(submittedSong);
								} catch (Exception e) {
									log.error("finish(): error adding/updating external song=" + submittedSong, e);
								}
							}
						}
					} catch (Exception e) {
						log.error("finish(): error adding/updating external release=" + submittedRelease, e);
					}
				}
			}
		}
		
		// trigger next artist
		currentProfile = null;
		currentDiscogsProfile = null;		
	}
	
	public void processResult(Object result) {
		if (RapidEvolution3.isTerminated || isCancelled())
			return;		
		if (log.isTraceEnabled())
			log.trace("processResult(): received result");
		if (result != null) {
			DiscogsRelease release = (DiscogsRelease)result;
			currentReleases.add(release);
		}
		++currentReleaseIndex;			
		if (currentReleaseIndex >= currentDiscogsProfile.getReleaseIds().size())
			finish();		
		TaskManager.runBackgroundTask(this); // will start processing the next
	}
	
	public void execute() { startNextProcess(); }
	public Object getResult() { return null; }
	
}
