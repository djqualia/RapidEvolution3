package com.mixshare.rapid_evolution.workflow.user;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.Task;

public class UpdateRecordsTask extends CommonTask {
	
	static private Logger log = Logger.getLogger(UpdateRecordsTask.class);
    static private final long serialVersionUID = 0L;    	
    
	private Collection<Record> recordsToRefresh;
	
	public UpdateRecordsTask(Collection<Record> recordsToRefresh) {
		this.recordsToRefresh = recordsToRefresh;
	}
				
	public void execute() {
		try {
			int numRecordsToRefresh = recordsToRefresh.size();
			int numProcessed = 0;
			for (Record record : recordsToRefresh) {
            	if (RapidEvolution3.isTerminated || isCancelled())
            		return;				
            	record.update();
            	++numProcessed;
            	setProgress(((float)numProcessed) / numRecordsToRefresh);
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}
	
	public int getTaskPriority() { return Task.DEFAULT_TASK_PRIORITY + 5; }
	
	public Object getResult() { return null; }

	public String toString() { return "Updating records " + recordsToRefresh; }
	
}
