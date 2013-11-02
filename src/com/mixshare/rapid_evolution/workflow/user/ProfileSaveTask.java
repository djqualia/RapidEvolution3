package com.mixshare.rapid_evolution.workflow.user;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;

public class ProfileSaveTask extends CommonTask {
	
	static private Logger log = Logger.getLogger(ProfileSaveTask.class);
    static private final long serialVersionUID = 0L;    	
    
    static private long PROFILE_SAVE_TASK_SLEEP_INTERVAL = RE3Properties.getLong("profile_save_task_sleep_interval_millis");
    
    static private ProfileSaveTask instance = null;
    
    static public void save(Profile profile) {
    	if (!instance.saveBuffer.contains(profile))
    		instance.saveBuffer.add(profile);
    }
    
    private Vector<Profile> saveBuffer = new Vector<Profile>();
    
	public ProfileSaveTask() { 
		instance = this;
	}
				
	public void execute() {
		try {
			if (log.isTraceEnabled())
				log.trace("execute(): saving profiles...");
			while (saveBuffer.size() > 0) {
				Profile profile = saveBuffer.remove(0);
				profile.save();
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		} finally {
			if (!RapidEvolution3.isTerminated)
				SandmanThread.putForegroundTaskToSleep(this, PROFILE_SAVE_TASK_SLEEP_INTERVAL);
		}
	}
	
	public int getTaskPriority() { return RE3Properties.getInt("profile_save_task_priority"); }
	
	public Object getResult() { return null; }
	
	public String toString() { return "Saving profiles " + saveBuffer; }
	
	public boolean isIndefiniteTask() { return true; }

}