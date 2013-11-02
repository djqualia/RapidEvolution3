package com.mixshare.rapid_evolution.workflow.mining;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.ui.widgets.RE3StatusBar;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.event.MiningTaskFinishedListener;

public class MiningStateManager extends CommonTask implements DataConstants, TaskResultListener {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(MiningStateManager.class);
		
    static private int MINING_REPRIORITIZE_INTERVAL = RE3Properties.getInt("mining_reprioritize_interval_tasks");
    
	////////////
	// FIELDS //
	////////////
	
	private MiningTask miningTask;
	private Vector<SearchRecord> currentJobs;
	private int currentIndex = 0;
	private Vector<MiningTaskFinishedListener> taskFinishedListeners = new Vector<MiningTaskFinishedListener>();
	private String isReadyId;
	private boolean excludeExternalItems = false;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public MiningStateManager(MiningTask miningTask, String isReadyId) {
		super();
		this.miningTask = miningTask;
		this.isReadyId = isReadyId;
		if (!RE3Properties.getBoolean("server_mode"))
			taskFinishedListeners.add(RE3StatusBar.instance);
	}
	
	public MiningStateManager(MiningTask miningTask, String isReadyId, boolean internalItemsOnly) {
		super();
		this.miningTask = miningTask;
		this.isReadyId = isReadyId;
		this.excludeExternalItems = internalItemsOnly;
		if (!RE3Properties.getBoolean("server_mode"))
			taskFinishedListeners.add(RE3StatusBar.instance);
	}	
	
	/////////////
	// GETTERS //
	/////////////
	
	public int getTaskPriority() { return miningTask.getTaskPriority(); }
	
	public SearchIndex getIndex() {
		SearchIndex index = null;
		MinedProfileHeader header = miningTask.getMinedProfileHeader();
		if (header.getDataType() == DataConstants.DATA_TYPE_ARTISTS)
			index = Database.getArtistIndex();
		else if (header.getDataType() == DataConstants.DATA_TYPE_LABELS)
			index = Database.getLabelIndex();
		else if (header.getDataType() == DataConstants.DATA_TYPE_RELEASES)
			index = Database.getReleaseIndex();
		else if (header.getDataType() == DataConstants.DATA_TYPE_SONGS)
			index = Database.getSongIndex();		
		return index;
	}
	
	/**
	 * This fetches the search record and sorts them by the last time the particular mined profile was fetched, so that the oldest can be processed first
	 */
	private Vector<SearchRecord> getSearchRecordsByLastUpdated() {
		SearchIndex index = getIndex();
		SearchSearchParameters searchParams = (SearchSearchParameters)index.getNewSearchParameters();
		searchParams.setSortMinedHeader(miningTask.getMinedProfileHeader().getDataSource());
		searchParams.setMinedHeaderCutoffTime(System.currentTimeMillis() - miningTask.getMinimumTimeBetweenQueries());
		searchParams.setSortType(new byte[] { CommonSearchParameters.SORT_BY_MINED_HEADER, CommonSearchParameters.SORT_BY_RATING, CommonSearchParameters.SORT_BY_NUM_PLAYS, CommonSearchParameters.SORT_BY_INTERNAL_ITEMS });
		if (excludeExternalItems)
			searchParams.setInternalItemsOnly(true);
		Vector<SearchResult> searchResult = index.searchRecords(searchParams);		
		Vector<SearchRecord> result = new Vector<SearchRecord>(searchResult.size());
		for (SearchResult record : searchResult)
			result.add((SearchRecord)record.getRecord());
		return result;
	}
	
	private SearchRecord getNextSearchRecordForProcessing() {
		if ((currentJobs == null) || (currentIndex >= currentJobs.size()) || (currentIndex >= MINING_REPRIORITIZE_INTERVAL)) {
			currentJobs = getSearchRecordsByLastUpdated();
			currentIndex = 0;
		}
		if (currentIndex < currentJobs.size())
			return currentJobs.get(currentIndex++);
		return null;
	}
	
	public boolean isReady() {
		return RE3Properties.getBoolean(isReadyId);
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void startNextProcess() {
		try {
			if (RapidEvolution3.isTerminated)
				return;
			if (!miningTask.getAPIWrapper().getRateController().canMakeQuery()) {
				SandmanThread.putBackgroundTaskToSleep(this, miningTask.getAPIWrapper().getRateController().getTimeUntilNextQuery());
				return; // want to end this thread so we don't hold up other background tasks...				
			}
			SearchRecord recordForProcessing = getNextSearchRecordForProcessing();
			if (recordForProcessing == null) {
				SandmanThread.putBackgroundTaskToSleep(this);
				return; // want to end this thread so we don't hold up other background tasks...
			}
			SearchProfile searchProfile = (SearchProfile)getIndex().getProfile(recordForProcessing.getUniqueId());
			if (searchProfile != null) {
				if (log.isTraceEnabled())
					log.trace("startProcess(): processing=" + searchProfile);
				miningTask.init(this, searchProfile);
				searchProfile.getSearchRecord().addMinedProfileHeader(miningTask.getMinedProfileHeader());
				if (log.isTraceEnabled())
					log.trace("startNextProcess(): adding background task=" + miningTask);
				TaskManager.runBackgroundTask(miningTask);							
			} else {
				SandmanThread.putBackgroundTaskToSleep(this);
				return;
				// a stack overflow occurred once when using the line below
				//startNextProcess();
			}
				
		} catch (Exception e) {
			log.error("startProcess(): error", e);
		}
	}
	
	public void processResult(Object result) {
		if (log.isTraceEnabled())
			log.trace("processResult(): result=" + result);
		if (result != null) {
			MinedProfileHeader header = miningTask.getMinedProfileHeader();
			StringBuffer message = new StringBuffer();
			message.append("Fetched ");
			message.append(DataConstantsHelper.getDataSourceDescription(header.getDataSource()).toLowerCase());
			message.append(" ");
			message.append(DataConstantsHelper.getDataTypeDescription(header.getDataType()).toLowerCase());
			message.append(" \"");
			message.append(result.toString());
			message.append("\"");
			for (MiningTaskFinishedListener taskFinishedListener : taskFinishedListeners)
				taskFinishedListener.finishedMiningTask(message.toString());
		}
		TaskManager.runBackgroundTask(this); // will start processing the next
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(DataConstantsHelper.getDataSourceDescription(miningTask.getMinedProfileHeader().getDataSource()));
		result.append(" ");
		result.append(DataConstantsHelper.getDataTypeDescription(miningTask.getMinedProfileHeader().getDataType()));
		result.append(" mining state manager");
		if (currentJobs != null) {
			result.append(" [");
			result.append(String.valueOf(currentIndex));
			result.append("/");
			result.append(String.valueOf(currentJobs.size()));
			result.append("]");
		}
		return result.toString();
	}

	
	public void execute() { startNextProcess(); }
	public Object getResult() { return null; }
	
	public boolean isIndefiniteTask() { return true; }
		
}
