package com.mixshare.rapid_evolution.ui.widgets.profile.filter;

import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.ui.widgets.common.image.ImageViewer;
import com.mixshare.rapid_evolution.ui.widgets.profile.CommonProfileDelegate;
import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QWidget;

abstract public class FilterProfileDelegate extends CommonProfileDelegate {

	public FilterProfileDelegate(QTabWidget itemDetailTabsWidget) {
		super(itemDetailTabsWidget);
	}
	
	public QWidget createImageViewerWidget(QWidget parent, Profile profile) {
		FilterProfile filterProfile = (FilterProfile)profile;
		imageViewer = new ImageViewer(parent, filterProfile);
		return imageViewer;		
	}
	
	
}
