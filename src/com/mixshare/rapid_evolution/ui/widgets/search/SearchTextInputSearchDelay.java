package com.mixshare.rapid_evolution.ui.widgets.search;

import com.mixshare.rapid_evolution.ui.updaters.view.search.SearchFilterUpdate;
import com.mixshare.rapid_evolution.ui.util.TextInputSearchDelay;
import com.trolltech.qt.gui.QApplication;

/**
 * On large collections, updating the filter after every letter has changed can cause lags.  This class
 * delays the invalidation of the filter to an interval after the user stops typing...
 */
public class SearchTextInputSearchDelay extends TextInputSearchDelay {
	
	public void invokeFilter() {
		QApplication.invokeLater(new SearchFilterUpdate());
	}
	
}
