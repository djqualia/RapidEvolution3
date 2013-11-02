package com.mixshare.rapid_evolution.ui.updaters.view.filter;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.filter.FilterWidgetUI;

public class FilterFilterUpdate extends Thread {

    private static Logger log = Logger.getLogger(FilterFilterUpdate.class);
	
    private FilterWidgetUI filterWidget;
    
	public FilterFilterUpdate(FilterWidgetUI filterWidget) {
		this.filterWidget = filterWidget;
	}
	
	public void run() {
		try {
			filterWidget.updateFilter();
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}	
	
}
