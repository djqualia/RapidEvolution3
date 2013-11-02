package com.mixshare.rapid_evolution.workflow.maintenance.search;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class SearchTransientsClearer extends CommonTask {

	static private Logger log = Logger.getLogger(SearchTransientsClearer.class);
    static private final long serialVersionUID = 0L;
	
    private SearchProfile searchProfile = null;
    private boolean success = false;
        
    public SearchTransientsClearer(SearchProfile searchProfile) {
    	this.searchProfile = searchProfile;
    }
    
	public void execute() {
		if (RapidEvolution3.isTerminated || isCancelled())
			return;
		if (searchProfile != null)
			searchProfile.clearSimilaritySearchTransients();			
	}
	
	public Object getResult() {
		return success;
	}
	
	public int getTaskPriority() {
		return RE3Properties.getInt("default_task_priority") + 5;
	}
	
	public String toString() { return "Clearing Search Transients"; }

	public boolean isIndefiniteTask() { return true; }
	
}
