package com.mixshare.rapid_evolution.workflow.maintenance.search;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class SearchRecordComputer extends CommonTask {

	static private Logger log = Logger.getLogger(SearchRecordComputer.class);
    static private final long serialVersionUID = 0L;
	
    private Vector<SearchRecord> records;
    private boolean success = false;
    
    public SearchRecordComputer(Vector<SearchRecord> records) {
    	this.records = records;
    }
    
	public String toString() {
		return "Search Record Computer Task";
	}
    
    
	public void execute() {
		try {
			if (log.isDebugEnabled())
				log.debug("execute(): starting...");
			
			for (SearchRecord searchRecord : records) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				//searchRecord.computeActualStyles();
				//searchRecord.computeActualTags();
				//searchRecord.update();
				SearchProfile profile = (SearchProfile)Database.getProfile(searchRecord.getIdentifier());
				if (profile != null) {
					if (log.isDebugEnabled())
						log.debug("execute(): processing=" + profile);
					profile.computeChanges(DATA_SOURCE_UNKNOWN);
					profile.save();
				}				
			}
			success = true;
			if (log.isDebugEnabled())
				log.debug("execute(): finished...");

		} catch (Exception e) {
			log.error("execute(): error", e);			
		}
	}
	
	public Object getResult() {
		return success;
	}
	
	public int getTaskPriority() {
		return RE3Properties.getInt("default_task_priority");
	}

	public boolean isIndefiniteTask() { return true; }
	
}
