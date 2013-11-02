package com.mixshare.rapid_evolution.workflow.user;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.Task;
import com.trolltech.qt.gui.QApplication;

public class DeleteRecordsTask extends CommonTask {
	
	static private Logger log = Logger.getLogger(DeleteRecordsTask.class);
    static private final long serialVersionUID = 0L;    	
    
	private Vector<Record> deletedRecords;
	
	public DeleteRecordsTask(Vector<Record> deletedRecords) {
		this.deletedRecords = deletedRecords;
	}
				
	public void execute() {
		try {
			int numToDelete = deletedRecords.size();
			int numDeleted = 0;
        	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));        							
			for (Record record : deletedRecords) {
            	if (RapidEvolution3.isTerminated || isCancelled())
            		return;				
				if (log.isDebugEnabled())
					log.debug("run(): deleting=" + record.getIdentifier());
				Database.delete(record);
				++numDeleted;
				setProgress(((float)numDeleted) / numToDelete);
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}
	
	public int getTaskPriority() { return Task.DEFAULT_TASK_PRIORITY; }
	
	public Object getResult() { return null; }
	
	public String toString() { return "Deleting records " + StringUtil.getTruncatedDescription(deletedRecords, true); }

}
