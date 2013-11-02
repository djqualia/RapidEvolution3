package com.mixshare.rapid_evolution.workflow.ui;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.Task;

public class TagUpdateTask extends CommonTask {
	
	static private Logger log = Logger.getLogger(TagUpdateTask.class);
    static private final long serialVersionUID = 0L;    	
    
    static private long TAG_UPDATE_TASK_SLEEP_INTERVAL = RE3Properties.getLong("tag_update_task_sleep_interval_millis");
    
	public TagUpdateTask() { }
					
	public void execute() {
		try {
			if (log.isTraceEnabled())
				log.trace("execute(): updating invalidated tags...");			
			for (int tagId : Database.getTagIndex().getIds()) {
				TagRecord tag = Database.getTagIndex().getTagRecord(tagId);
				if ((tag != null) && tag.needsUpdate())
					tag.update();									
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		} finally {
			if (!RapidEvolution3.isTerminated)
				SandmanThread.putForegroundTaskToSleep(this, TAG_UPDATE_TASK_SLEEP_INTERVAL);
		}
	}
	
	public int getTaskPriority() { return Task.DEFAULT_TASK_PRIORITY; }
	
	public Object getResult() { return null; }
	
	public String toString() { return "Tag tree refresher task"; }

	public boolean isIndefiniteTask() { return true; }
	
}
