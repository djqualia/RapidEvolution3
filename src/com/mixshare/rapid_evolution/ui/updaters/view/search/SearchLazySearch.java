package com.mixshare.rapid_evolution.ui.updaters.view.search;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;
import com.mixshare.rapid_evolution.util.timing.Semaphore;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class SearchLazySearch extends CommonTask {

    static private final long serialVersionUID = 0L;    	
	static private Logger log = Logger.getLogger(SearchLazySearch.class);
	
	static public SearchLazySearch instance;
	
	static private Semaphore updateLimiter = new Semaphore(1);	
	
	private SearchWidgetUI widgetUI;
	private SearchModelManager currentModelManager;
	
	public SearchLazySearch(SearchWidgetUI widgetUI, SearchModelManager currentModelManager) {
		this.widgetUI = widgetUI;
		this.currentModelManager = currentModelManager;
	}
	
	public String toString() { return "Searching..."; }
	
	public void execute() {
		try {
			updateLimiter.acquire();
			instance = this;
			widgetUI.lazySearch(currentModelManager);
		} catch (Exception e) {
			log.error("execute(): error", e);
		} finally {
			updateLimiter.release();
			instance = null;
		}
		
	}
	
}
