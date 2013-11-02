package com.mixshare.rapid_evolution.ui.widgets.filter.tags;

import com.mixshare.rapid_evolution.ui.updaters.view.filter.FilterFilterUpdate;
import com.mixshare.rapid_evolution.ui.util.TextInputSearchDelay;
import com.trolltech.qt.gui.QApplication;

/**
 * On large collections, updating the filter after every letter has changed can cause lags.  This class
 * delays the invalidation of the filter to an interval after the user stops typing...
 */
public class TagFilterTextInputSearchDelay extends TextInputSearchDelay {
	
	public void invokeFilter() {
		QApplication.invokeLater(new FilterFilterUpdate(TagsWidgetUI.instance));
	}
	
}
