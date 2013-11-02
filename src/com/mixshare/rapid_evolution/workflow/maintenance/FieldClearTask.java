package com.mixshare.rapid_evolution.workflow.maintenance;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class FieldClearTask extends CommonTask {

	static private Logger log = Logger.getLogger(FieldClearTask.class);
    static private final long serialVersionUID = 0L;    	
    
    private SearchDetailsModelManager detailsModelManager;
    private Column column;
    private Vector<SearchRecord> searchRecords;  
    
    public FieldClearTask(SearchDetailsModelManager detailsModelManager, Column column, Vector<SearchRecord> searchRecords) { 
    	this.detailsModelManager = detailsModelManager;
    	this.column = column;
    	this.searchRecords = searchRecords;
    }
    
	public void execute() {
		try {			
    		if (log.isDebugEnabled())
    			log.debug("execute(): column=" + column.getColumnTitle());    		
        	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));        				
			int size = searchRecords.size();
			int count = 0;
    		for (SearchRecord record : searchRecords) {
    			if (RapidEvolution3.isTerminated || isCancelled())
    				return;
    			SearchProfile profile = (SearchProfile)record.getIndex().getProfile(record.getUniqueId());
    			if (profile != null)
    				detailsModelManager.setFieldValue(column, null, profile);
    			++count;
    			setProgress(((float)count) / size);
    		}			
		} catch (Exception e) {
			log.error("execute(): error", e);
		}	
	}
	
	public boolean isIndefiniteTask() { return false; }
	
	public String toString() {
		return "Clearing the " + column.toString() + " field";
	}
	
}
