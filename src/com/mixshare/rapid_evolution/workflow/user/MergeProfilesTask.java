package com.mixshare.rapid_evolution.workflow.user;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.Task;

public class MergeProfilesTask extends CommonTask {

	static private Logger log = Logger.getLogger(MergeProfilesTask.class);
    static private final long serialVersionUID = 0L;    	
	
	////////////
	// FIELDS //
	////////////
	
	private Profile primaryProfile;
	private Profile mergedProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public MergeProfilesTask(Profile primaryProfile, Profile mergedProfile) {
		this.primaryProfile = primaryProfile;
		this.mergedProfile = mergedProfile;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void execute() {
		try {
			if (log.isDebugEnabled())
				log.debug("run(): merging primaryProfile=" + primaryProfile + ", mergedProfile=" + mergedProfile);	    						
			Database.mergeProfiles(primaryProfile, mergedProfile);
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}
	
	public int getTaskPriority() { return Task.DEFAULT_TASK_PRIORITY; }
	
	public Object getResult() { return null; }

	public String toString() { return "Merging profiles " + primaryProfile + " with " + mergedProfile; }
	
	public boolean isIndefiniteTask() { return true; }
	
}
