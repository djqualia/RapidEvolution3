package com.mixshare.rapid_evolution.ui.updaters.view.search;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;

public class SearchFilterUpdate extends Thread {

    private static Logger log = Logger.getLogger(SearchFilterUpdate.class);
	
	public SearchFilterUpdate() { }
	
	public void run() {
		try {
			SearchWidgetUI.instance.updateFilter();
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}	
	
}
