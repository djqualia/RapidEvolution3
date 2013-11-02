package com.mixshare.rapid_evolution.ui.updaters.view.profile;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.profile.tab.CommonTabTableWidget;

public class ProfileTabTableFilterUpdate extends Thread {

    private static Logger log = Logger.getLogger(ProfileTabTableFilterUpdate.class);
	
    private CommonTabTableWidget widget;
    
	public ProfileTabTableFilterUpdate(CommonTabTableWidget widget) {
		this.widget = widget;
	}
	
	public void run() {
		try {
			widget.updateFilter();
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}	
	
}
