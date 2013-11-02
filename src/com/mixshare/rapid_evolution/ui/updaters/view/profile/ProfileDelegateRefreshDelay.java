package com.mixshare.rapid_evolution.ui.updaters.view.profile;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class ProfileDelegateRefreshDelay extends CommonTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(ProfileDelegateRefreshDelay.class);
    
	static public ProfileDelegateRefreshDelay instance;
    
    private boolean doneDelaying = false;
    
	public ProfileDelegateRefreshDelay() {
		instance = this;
	}
	
	public Object getResult() { return null; }
	public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }		
	public boolean isDoneDelaying() { return doneDelaying; }
	
	public String toString() {
		return "Refreshing Profile Details";
	}		
	
	public void execute() {
		try {
			Thread.sleep(RE3Properties.getInt("profile_refresh_delay_millis"));
			doneDelaying = true;
			if ((!RapidEvolution3.isTerminated) && (ProfileWidgetUI.instance.getProfileDelegate() != null))
				QApplication.invokeAndWait(new ProfileDelegateRefresh(ProfileWidgetUI.instance.getProfileDelegate()));
		} catch (Exception e) {
			log.error("execute(): error", e);
		}		
		instance = null;
	}	
	
}
