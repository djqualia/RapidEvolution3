package com.mixshare.rapid_evolution.workflow.detection;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.timing.Semaphore;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;

abstract public class DetectionTask extends CommonTask {

	static private Logger log = Logger.getLogger(DetectionTask.class);
	
	static private int detectionTaskCount = 0;
	static private Semaphore detectionCountSem = new Semaphore(1);
	
	static private void updateDetectionTaskCount(int delta) {
		try {
			detectionCountSem.acquire();
			detectionTaskCount += delta;
		} catch (Exception e) { } finally {
			detectionCountSem.release();
		}
	}
	
	////////////
	// FIELDS //
	////////////
	
	protected int taskPriority;
	
	protected SongRecord processSong;
	protected TaskResultListener resultListener;
	
	protected boolean isSuccess = false;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DetectionTask() {
		super();		
		this.taskPriority = RE3Properties.getInt(StringUtil.removeSpaces(getDetectionType()).toLowerCase() + "_detection_task_priority");
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public String getDetectionType();
	abstract public void detect(SongRecord song);
	abstract public boolean isDetectionNeeded(SongRecord song);	
	
	/////////////
	// GETTERS //	
	/////////////
	
	public int getTaskPriority() { return taskPriority; }
	
	public Object getResult() { return isSuccess; }
	
	public SongRecord getSong() { return processSong; }
	
	public boolean isReady() {		
		File file = new File(getSong().getSongFilename());
		if (!file.exists())
			return false;
		if (detectionTaskCount < RE3Properties.getInt("max_simultaneous_detection_tasks"))
			return true;		
		return false;
	}	
	
	/////////////
	// METHODS //
	/////////////
	
	public void init(TaskResultListener resultListener, SongRecord processSong) {
		this.resultListener = resultListener;
		this.processSong = processSong;
	}
	
	public void execute() {
		if (processSong != null) {
			try {
				updateDetectionTaskCount(1);
				isSuccess = false;
			 	detect(processSong);
			} catch (Exception e) {
				log.error("execute(): error", e);
			} finally {
				updateDetectionTaskCount(-1);
			}
		}
		if (resultListener != null)
			resultListener.processResult(null);
	}
	
	public void setSuccess(boolean success) {
		isSuccess = success;
	}
		
}
