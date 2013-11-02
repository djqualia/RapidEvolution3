package com.mixshare.rapid_evolution.ui.widgets.profile.filter.tag;

import com.mixshare.rapid_evolution.ui.updaters.view.profile.ProfileTabTreeFilterUpdate;
import com.mixshare.rapid_evolution.ui.util.TextInputSearchDelay;
import com.trolltech.qt.gui.QApplication;

/**
 * On large collections, updating the filter after every letter has changed can cause lags.  This class
 * delays the invalidation of the filter to an interval after the user stops typing...
 */
public class TagTabTextInputSearchDelay extends TextInputSearchDelay {

	private TagTabTreeWidgetUI instance;
	
	public TagTabTextInputSearchDelay(TagTabTreeWidgetUI instance) {
		this.instance = instance;
	}
	
	public void invokeFilter() {
		QApplication.invokeLater(new ProfileTabTreeFilterUpdate(instance));
	}
	
}

