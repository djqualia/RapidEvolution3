package com.mixshare.rapid_evolution.workflow.ui;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.Task;

public class SongDescriptionUpdateTask extends CommonTask {
	
	static private Logger log = Logger.getLogger(SongDescriptionUpdateTask.class);
    static private final long serialVersionUID = 0L;    	
    	
	public SongDescriptionUpdateTask() {
	}
				
	public void execute() {
		try {
			if (RE3Properties.getBoolean("lazy_search_mode")) {
				int numRows = Database.getSongModelManager().getNumRows();
				int numProcessed = 0;
				for (int i = 0; i < numRows; ++i) {
					if (RapidEvolution3.isTerminated || isCancelled())
						return;
					Record record = Database.getSongModelManager().getRecordForRow(i);
					if (record != null)
						record.update();
					++numProcessed;
					setProgress(((float)numProcessed) / numRows);
				}
			} else {
				int numProcessed = 0;
				for (int songId : Database.getSongIndex().getIds()) {
					if (RapidEvolution3.isTerminated || isCancelled())
						return;
					SongRecord song = Database.getSongIndex().getSongRecord(songId);
					if (song != null) {
						song.update();
					}
					++numProcessed;
					setProgress(((float)numProcessed) / Database.getSongIndex().getSize());
				}				
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}
	
	public int getTaskPriority() { return Task.DEFAULT_TASK_PRIORITY; }
	
	public Object getResult() { return null; }

	public String toString() { return "Updating song descriptions..."; }
	
}
