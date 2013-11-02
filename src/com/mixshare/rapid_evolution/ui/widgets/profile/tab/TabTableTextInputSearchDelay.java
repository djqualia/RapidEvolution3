package com.mixshare.rapid_evolution.ui.widgets.profile.tab;

import com.mixshare.rapid_evolution.ui.updaters.view.profile.ProfileTabTableFilterUpdate;
import com.mixshare.rapid_evolution.ui.util.TextInputSearchDelay;
import com.trolltech.qt.gui.QApplication;

public class TabTableTextInputSearchDelay extends TextInputSearchDelay {
	
	private CommonTabTableWidget widget;
	
	public TabTableTextInputSearchDelay(CommonTabTableWidget widget) {
		this.widget = widget;		
	}
	
	public void invokeFilter() {
		QApplication.invokeLater(new ProfileTabTableFilterUpdate(widget));
	}
	
}
