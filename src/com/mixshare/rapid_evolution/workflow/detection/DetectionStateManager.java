package com.mixshare.rapid_evolution.workflow.detection;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.search.song.SongIndex;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.widgets.RE3StatusBar;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.event.DetectionTaskFinishedListener;

public class DetectionStateManager extends CommonTask implements DataConstants, TaskResultListener {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(DetectionStateManager.class);
		
    static private int DETECTION_REPRIORITIZE_INTERVAL = RE3Properties.getInt("detection_reprioritize_interval_tasks");
    
	////////////
	// FIELDS //
	////////////
	
	private DetectionTask detectionTask;
	private Vector<SongRecord> currentJobs;
	private int currentIndex = 0;
	private Vector<DetectionTaskFinishedListener> taskFinishedListeners = new Vector<DetectionTaskFinishedListener>();
	private String readyCheckId;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DetectionStateManager(DetectionTask detectionTask, String readyCheckId) {
		super();
		this.detectionTask = detectionTask;
		this.readyCheckId = readyCheckId;
		if (!RE3Properties.getBoolean("server_mode"))
			taskFinishedListeners.add(RE3StatusBar.instance);
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public int getTaskPriority() { return detectionTask.getTaskPriority(); }
	
	private Vector<SongRecord> getSongsToProcess() {
		SongIndex index = Database.getSongIndex();
		Vector<DetectSongRecord> sortList = new Vector<DetectSongRecord>();
		for (int id : index.getIds()) {
			if (RapidEvolution3.isTerminated)
				return null;			
			SongRecord songRecord = index.getSongRecord(id);
			if ((songRecord != null) && detectionTask.isDetectionNeeded(songRecord))				
				sortList.add(new DetectSongRecord(songRecord));							
		}
		java.util.Collections.sort(sortList);
		Vector<SongRecord> result = new Vector<SongRecord>(sortList.size());
		for (DetectSongRecord record : sortList)
			result.add(record.getSongRecord());
		return result;
	}
	
	private SongRecord getNextSongForProcessing() {
		if ((currentJobs == null) || (currentIndex >= currentJobs.size()) || (currentIndex >= DETECTION_REPRIORITIZE_INTERVAL)) {
			currentJobs = getSongsToProcess();
			currentIndex = 0;
		}
		if ((currentJobs != null) && currentIndex < currentJobs.size())
			return currentJobs.get(currentIndex++);
		return null;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void startNextProcess() {
		try {
			if (RapidEvolution3.isTerminated)
				return;
			SongRecord songForProcessing = getNextSongForProcessing();
			if (songForProcessing == null) {
				SandmanThread.putBackgroundTaskToSleep(this, RE3Properties.getLong("detection_task_no_work_delay_seconds") * 1000);
				return; // want to end this thread so we don't hold up other background tasks...
			}
			if (log.isTraceEnabled())
				log.trace("startProcess(): processing=" + songForProcessing);
			detectionTask.init(this, songForProcessing);			
			if (log.isTraceEnabled())
				log.trace("startNextProcess(): adding background task=" + detectionTask);
			TaskManager.runBackgroundTask(detectionTask);			
		} catch (Exception e) {
			log.error("startProcess(): error", e);
		}
	}
	
	public void processResult(Object result) {
		if (RapidEvolution3.isTerminated)
			return;		
		if ((result != null) && ((Boolean)result)) {
			String message = "Detected " + detectionTask.getDetectionType().toLowerCase() + " from \"" + detectionTask.getSong() + "\"";
			for (int i = 0; i < taskFinishedListeners.size(); ++i)
				taskFinishedListeners.get(i).finishedDetectionTask(message);
		}
		TaskManager.runBackgroundTask(this); // will start processing the next (by adding to the end of the queue)		
	}
	
	public void execute() { startNextProcess(); }
	public Object getResult() { return null; }
	
	public boolean isReady() {
		return RE3Properties.getBoolean(readyCheckId);
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(detectionTask.getDetectionType());
		result.append(" detection state manager");
		if (currentJobs != null) {
			result.append(" [");
			result.append(String.valueOf(currentIndex));
			result.append("/");
			result.append(String.valueOf(currentJobs.size()));
			result.append("]");
		}
		return result.toString();
	}
	
	/////////////
	// CLASSES //
	/////////////
	
	private class DetectSongRecord implements Comparable<DetectSongRecord> {
		private SongRecord songRecord;		
		public DetectSongRecord(SongRecord songRecord) {
			this.songRecord = songRecord;
		}
		public SongRecord getSongRecord() { return songRecord; }
		public int compareTo(DetectSongRecord l) {
			if (songRecord.getRatingValue().getRatingValue() > l.songRecord.getRatingValue().getRatingValue())
				return -1;
			if (songRecord.getRatingValue().getRatingValue() < l.songRecord.getRatingValue().getRatingValue())
				return 1;
			if (songRecord.getPlayCount() > l.songRecord.getPlayCount())
				return -1;
			if (songRecord.getPlayCount() < l.songRecord.getPlayCount())
				return 1;
			if (!songRecord.isExternalItem() && l.songRecord.isExternalItem())
				return -1;
			if (songRecord.isExternalItem() && !l.songRecord.isExternalItem())
				return 1;
			return 0;
		}
	}
	
}
