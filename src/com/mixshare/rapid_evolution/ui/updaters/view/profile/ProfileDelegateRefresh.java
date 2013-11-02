package com.mixshare.rapid_evolution.ui.updaters.view.profile;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileDelegate;

public class ProfileDelegateRefresh extends Thread {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(ProfileDelegateRefresh.class);
        
    private ProfileDelegate profileDelegate;
    
	public ProfileDelegateRefresh(ProfileDelegate profileDelegate) {
		this.profileDelegate = profileDelegate;
	}
		
	public void run() {
		try {
			profileDelegate.refresh();
		} catch (Exception e) {
			log.error("run(): error", e);
		}		
	}	
	
}
