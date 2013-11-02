package com.mixshare.rapid_evolution.ui.updaters.view.profile;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeWidget;

public class ProfileTabTreeFilterUpdate extends Thread {

    private static Logger log = Logger.getLogger(ProfileTabTreeFilterUpdate.class);
	
    private TabTreeWidget treeWidget;
    
	public ProfileTabTreeFilterUpdate(TabTreeWidget treeWidget) {
		this.treeWidget = treeWidget;
	}
	
	public void run() {
		try {
			treeWidget.updateFilter();
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}	
	
}
