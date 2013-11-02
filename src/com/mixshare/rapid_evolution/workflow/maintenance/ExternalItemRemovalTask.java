package com.mixshare.rapid_evolution.workflow.maintenance;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class ExternalItemRemovalTask extends CommonTask {

	static private Logger log = Logger.getLogger(ExternalItemRemovalTask.class);
    static private final long serialVersionUID = 0L;    	
    
    private SearchIndex searchIndex;
    
    public ExternalItemRemovalTask(SearchIndex searchIndex) { 
    	this.searchIndex = searchIndex;
    }
    
	public void execute() {
		try {			
			if (log.isTraceEnabled())
				log.trace("execute(): removing external items for index=" + DataConstantsHelper.getDataTypeDescription(searchIndex.getDataType()));
			
        	int numExternalItems = searchIndex.getSizeExternalItems();
        	if (numExternalItems > 0) {
            	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
            		QApplication.invokeLater(new TaskProgressLauncher(this));			
        		
            	int numDeleted = 0;
            	for (int id : searchIndex.getIds()) {
            		if (RapidEvolution3.isTerminated || isCancelled())
            			return;
            		SearchRecord record = (SearchRecord)searchIndex.getRecord(id);
            		if ((record != null) && record.isExternalItem()) {
            			searchIndex.delete(id);
            			++numDeleted;
            			setProgress(((float)numDeleted) / numExternalItems);
            		}
            	}
        	
        	}
        	
        	
		} catch (Exception e) {
			log.error("execute(): error", e);
		}	
	}
	
	public boolean isIndefiniteTask() { return false; }
	
	public String toString() {
		return "Deleting recommended " + DataConstantsHelper.getDataTypeDescription(searchIndex.getDataType()).toLowerCase() + "s";
	}
	
}
