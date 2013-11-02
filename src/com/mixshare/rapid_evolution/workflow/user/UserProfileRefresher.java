package com.mixshare.rapid_evolution.workflow.user;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.maintenance.DatabaseCleanerTask;

public class UserProfileRefresher extends CommonTask {
	
	static private Logger log = Logger.getLogger(UserProfileRefresher.class);
    static private final long serialVersionUID = 0L;    	
    	
	public UserProfileRefresher() {	}
				
	public void execute() {
		try {
    		if (RE3Properties.getBoolean("skip_user_profile_computation"))
    			return;			    		
    		if ((RE3Properties.getInt("max_external_artists") == 0) && (RE3Properties.getInt("max_external_labels") == 0) && (RE3Properties.getInt("max_external_releases") == 0) && (RE3Properties.getInt("max_external_songs") == 0))
    			return;
    		
			if (log.isTraceEnabled())
				log.trace("execute(): computing user profile...");
			Database.getUserProfile().computeProfile(this);
			if (RapidEvolution3.isTerminated || isCancelled())
				return;
			if (log.isTraceEnabled())
				log.trace("execute(): updating preferences on items...");
			for (SearchIndex searchIndex : Database.getSearchIndexes()) {
				for (int id : searchIndex.getIds()) {
					if (RapidEvolution3.isTerminated || isCancelled())
						return;
					SearchRecord record = searchIndex.getSearchRecord(id);
					//if ((record != null) && record.isExternalItem())
					if (record != null)
						record.update();					
				}
			}
			if (DatabaseCleanerTask.instance == null)
				TaskManager.runBackgroundTask(new DatabaseCleanerTask(true));
			if (log.isTraceEnabled())
				log.trace("execute(): done refreshing user profile");
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
		SandmanThread.putBackgroundTaskToSleep(this, RE3Properties.getInt("user_profile_refresh_interval_minutes") * 1000 * 60);
	}
	
	public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }
	
	public Object getResult() { return null; }
	
	public String toString() { return "Computing user profile"; }

	public boolean isIndefiniteTask() { return true; }

}
