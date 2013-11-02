package com.mixshare.rapid_evolution.workflow.maintenance.filter;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.filter.FilterIndex;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;

public class ComputeFilterStats extends CommonTask {

	static private Logger log = Logger.getLogger(ComputeFilterStats.class);
    static private final long serialVersionUID = 0L;
	
    private boolean success = false;
        
	public void execute() {
		try {
			if (log.isDebugEnabled())
				log.debug("execute(): starting...");
			
			for (FilterIndex filterIndex : Database.getFilterIndexes()) {
				for (int id : filterIndex.getIds()) {
					if (RapidEvolution3.isTerminated || isCancelled())
						return;
					FilterRecord filter = filterIndex.getFilterRecord(id);
					if (filter != null) {
						filter.getNumArtistRecords();
						filter.getNumLabelRecords();
						filter.getNumReleaseRecords();
						filter.getNumSongRecords();
						filter.getNumExternalArtistRecords();
						filter.getNumExternalLabelRecords();
						filter.getNumExternalReleaseRecords();
						filter.getNumExternalSongRecords();
					}
				}
			}

			success = true;
			if (log.isDebugEnabled())
				log.debug("execute(): finished!");
			
		} catch (Exception e) {
			log.error("execute(): error", e);			
		} finally {
			SandmanThread.putForegroundTaskToSleep(this, 1000 * 60);			
		}
	}
	
	public Object getResult() {
		return success;
	}
	
	public int getTaskPriority() {
		return RE3Properties.getInt("default_task_priority") + 5;
	}
	
	public String toString() { return "Computing Filter Stats"; }		
	
	public boolean isIndefiniteTask() { return true; }
	
}
