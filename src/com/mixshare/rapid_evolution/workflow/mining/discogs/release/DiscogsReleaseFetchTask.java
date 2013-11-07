package com.mixshare.rapid_evolution.workflow.mining.discogs.release;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsRelease;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;

public class DiscogsReleaseFetchTask extends CommonTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(DiscogsReleaseFetchTask.class);    
	
    static public String releasesDirectory = "/data/discogs/releases/";
    
    ////////////
    // FIELDS //
    ////////////
    
	private String releaseId;
	private DiscogsRelease result;
	private int taskPriority;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DiscogsReleaseFetchTask(String releaseId, TaskResultListener listener) {
		this.releaseId = releaseId;
		setTaskResultListener(listener);
		taskPriority = RE3Properties.getInt("discogs_mining_task_priority");
	}
	
	/////////////
	// GETTERS //
	/////////////
			
	public int getTaskPriority() { return taskPriority; }
	
	public Object getResult() { return result; }
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("discogs_release_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}		
	
	/////////////
	// METHODS //
	/////////////

	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching discogs release id=" + releaseId);
		try {
			result = MiningAPIFactory.getDiscogsAPI().getRelease(releaseId);
			if (result != null) {
				// cache to the hard disk				
				FileSystemAccess.getFileSystem().saveData(releasesDirectory + releaseId + ".xml", XMLSerializer.getBytes(result), true);				
			}
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}	
	
	public String toString() { return "Fetching discogs release " + releaseId; }
	
}
