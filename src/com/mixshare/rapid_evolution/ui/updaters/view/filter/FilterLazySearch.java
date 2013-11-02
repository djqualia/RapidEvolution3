package com.mixshare.rapid_evolution.ui.updaters.view.filter;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.filter.FilterWidgetUI;
import com.mixshare.rapid_evolution.util.timing.Semaphore;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class FilterLazySearch extends CommonTask {

    static private final long serialVersionUID = 0L;    	
	static private Logger log = Logger.getLogger(FilterLazySearch.class);
	
	static private FilterLazySearch instance;
	
	static private Semaphore updateLimiter = new Semaphore(1);	
	
	private FilterWidgetUI widgetUI;
	private String searchText;
	
	public FilterLazySearch(FilterWidgetUI widgetUI, String searchText) {
		this.searchText = searchText;
		this.widgetUI = widgetUI;
	}
	
	public String toString() { return "Searching filters with=" + searchText; }
	
	public void execute() {
		try {
			updateLimiter.acquire();
			widgetUI.lazySearch(searchText);
		} catch (Exception e) {
			log.error("execute(): error", e);
		} finally {
			updateLimiter.release();
		}
		
	}
	
}
