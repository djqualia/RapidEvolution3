package com.mixshare.rapid_evolution.workflow.mining.discogs.release;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsRelease;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseLabelInstance;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.search.release.SubmittedRelease;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;

public class DiscogsReleaseRecommendationsFetchStateManager extends CommonTask implements DataConstants, TaskResultListener {
	
    static private Logger log = Logger.getLogger(DiscogsReleaseRecommendationsFetchStateManager.class);
    static private final long serialVersionUID = 0L;
		
	////////////
	// FIELDS //
	////////////
	
	protected Vector<Integer> currentJobs; // current IDs with discogs profiles that need updating
	protected int currentIndex = 0;
	protected ReleaseProfile currentRelease;
	protected DiscogsReleaseProfile currentDiscogsRelease;
	protected int currentRecommendedIndex = 0;
	protected Vector<DiscogsRelease> currentRecommendations = new Vector<DiscogsRelease>();
    private int taskPriority;
    private boolean stopAfterCurrentProfile = false;
    
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DiscogsReleaseRecommendationsFetchStateManager() {
		super();
		taskPriority = RE3Properties.getInt("discogs_mining_task_priority");				
	}
	
	public DiscogsReleaseRecommendationsFetchStateManager(ReleaseProfile currentRelease, DiscogsReleaseProfile currentDiscogsRelease, int taskPriority) {
		this.currentRelease = currentRelease;
		this.currentDiscogsRelease = currentDiscogsRelease;
		this.taskPriority = taskPriority;
		this.stopAfterCurrentProfile = true;
	}	
	
	/////////////
	// GETTERS //
	/////////////	
		
	public int getTaskPriority() { return taskPriority; }
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("discogs_release_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}		
	
	private Integer getNextIdForProcessing() {
		if ((currentJobs == null) || (currentIndex >= currentJobs.size())) {
			currentJobs = getDiscogsReleasesForUpdating();
			currentIndex = 0;
		}
		if (currentIndex < currentJobs.size()) {
			return currentJobs.get(currentIndex++);
		}
		return null;
	}
	
	protected Vector<Integer> getDiscogsReleasesForUpdating() {		
		ReleaseSearchParameters searchParams = (ReleaseSearchParameters)Database.getReleaseIndex().getNewSearchParameters();
		searchParams.setInternalItemsOnly(true);
		searchParams.setMinedDiscogsRecommendationCutoff(System.currentTimeMillis() - getMinimumTimeBetweenQueries());
		searchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_RATING, CommonSearchParameters.SORT_BY_NUM_PLAYS, CommonSearchParameters.SORT_BY_INTERNAL_ITEMS });		
		Vector<SearchResult> searchResult = Database.getReleaseIndex().searchRecords(searchParams);		
		Vector<Integer> result = new Vector<Integer>(searchResult.size());
		for (SearchResult record : searchResult)
			result.add(((SearchRecord)record.getRecord()).getUniqueId());
		return result;
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
			if (currentRelease == null) {
				if (stopAfterCurrentProfile)
					return;
				Integer profileId = getNextIdForProcessing();
				if (profileId == null) { // if there's no work at the moment...
					if (log.isTraceEnabled())
						log.trace("startNextProcess(): no work now, sleeping...");
					SandmanThread.putBackgroundTaskToSleep(this);
					return; // want to end this thread so we don't hold up other background tasks...
				}
				ReleaseProfile profile = Database.getReleaseIndex().getReleaseProfile(profileId);
				if (profile != null) {
					if (log.isTraceEnabled())
						log.trace("startNextProcess(): setting current profile=" + profile);
					currentRelease = profile;
					currentDiscogsRelease = (DiscogsReleaseProfile)profile.getMinedProfile(DATA_SOURCE_DISCOGS);
					currentRecommendedIndex = 0;
					currentRecommendations.clear();
				}
				if ((currentDiscogsRelease == null) || (currentDiscogsRelease.getRecommendedReleaseIds().size() == 0)) {
					if (log.isTraceEnabled())
						log.trace("startNextProcess(): discogs profile removed?  skipping to next task");
					currentRelease = null;
					currentDiscogsRelease = null;
					SandmanThread.putBackgroundTaskToSleep(this);
					//startNextProcess();
					return;
				}
			}	
			String currentReleaseId = null;
			DiscogsRelease discogsRelease = null;
			do {	
				discogsRelease = null;
				if ((currentDiscogsRelease.getRecommendedReleaseIds().size() == 0) && stopAfterCurrentProfile)
					return;
				currentReleaseId = String.valueOf(currentDiscogsRelease.getRecommendedReleaseIds().get(currentRecommendedIndex));

				// 	check disk cache
				String filename = DiscogsReleaseFetchTask.releasesDirectory + currentReleaseId + ".xml";				
				if (FileSystemAccess.getFileSystem().doesExist(filename) && ((System.currentTimeMillis() - FileSystemAccess.getFileSystem().getLastModified(filename)) < getMinimumTimeBetweenQueries())) {
					// 	use cached item					
					discogsRelease = (DiscogsRelease)XMLSerializer.readBytes(FileSystemAccess.getFileSystem().readData(filename));				
					if (discogsRelease != null) {
						if (log.isTraceEnabled())
							log.trace("startNextProcess(): used cached release, id=" + currentReleaseId + ", skipping to next");
						currentRecommendations.add(discogsRelease);
						++currentRecommendedIndex;
						if (currentRecommendedIndex >= currentDiscogsRelease.getRecommendedReleaseIds().size()) {
							finish();	
							startNextProcess();
							return;
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
		
		if (currentRecommendations.size() == 0)
			return;
		
		if (log.isTraceEnabled())
			log.trace("finish(): finished processing=" + currentRelease);
			
		// similar/recommended releases
		for (DiscogsRelease discogsRelease : currentRecommendations) {
			if (RapidEvolution3.isTerminated || isCancelled())
				return;
			DiscogsReleaseProfile releaseProfile = new DiscogsReleaseProfile(discogsRelease.getTitle());
			releaseProfile.addRelease(discogsRelease);
			Vector<String> artistNames = releaseProfile.getArtistNames();
			// translate from discogs to local name (in case of ambiguity, i.e. justice (3) -> justice)
			for (int i = 0; i < artistNames.size(); ++i)
				artistNames.set(i, MiningAPIFactory.getDiscogsAPI().getLocalArtistName(artistNames.get(i)));														
			SubmittedRelease submittedRelease = new SubmittedRelease(artistNames, releaseProfile.getTitle());
			Vector<String> labelNames = new Vector<String>();
			for (DiscogsReleaseLabelInstance labelInstance : releaseProfile.getLabelInstances())
				labelNames.add(MiningAPIFactory.getDiscogsAPI().getLocalLabelName(labelInstance.getName()));						
			submittedRelease.setLabelNames(labelNames);
			submittedRelease.setExternalItem(true);	
			submittedRelease.addMinedProfile(releaseProfile);
			try {
				Database.getReleaseIndex().addOrUpdate(submittedRelease);
			} catch (Exception e) {
				log.error("finish(): error adding/updating externa release", e);
			}			
		}
		
		finishProfile();		
		
		// trigger next release
		currentRelease = null;
		currentDiscogsRelease = null;		
	}
	
	public void processResult(Object result) {
		if (log.isTraceEnabled())
			log.trace("processResult(): received result");
		if (result != null) {
			DiscogsRelease release = (DiscogsRelease)result;
			currentRecommendations.add(release);
		}
		++currentRecommendedIndex;			
		if (currentRecommendedIndex >= currentDiscogsRelease.getRecommendedReleaseIds().size())
			finish();		
		TaskManager.runBackgroundTask(this); // will start processing the next
	}
	
	public void execute() { startNextProcess(); }
	public Object getResult() { return null; }
	
	public void finishProfile() {
		if (log.isDebugEnabled())
			log.debug("finishProfile(): fetched recommended releases for release=" + currentRelease);
		
		// done processing this artist...
		currentDiscogsRelease.calculateRecommendReleases(currentRecommendations);
		
		currentRelease.addMinedProfile(currentDiscogsRelease);				
		currentDiscogsRelease.lastFetchedRecommendedReleases();			
		currentRelease.save();
	}	
	
	public String toString() {
		return "Discogs release recommendation fetcher state manager"; 
	}
	
	/////////////
	// CLASSES //
	/////////////
	
	protected class ProfileForUpdate implements Comparable<ProfileForUpdate> {
		private int profileId;
		private long lastUpdated;
		public ProfileForUpdate(int profileId, long lastUpdated) {
			this.profileId = profileId;
			this.lastUpdated = lastUpdated;
		}
		public int getProfileId() { return profileId; }
		public int compareTo(ProfileForUpdate l) {
			if (lastUpdated < l.lastUpdated)
				return -1;
			if (lastUpdated > l.lastUpdated)
				return 1;
			return 0;
		}
	}	
	
}
