package com.mixshare.rapid_evolution.ui.widgets.profile;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.trolltech.qt.gui.QWidget;

public interface ProfileDelegate {

	public QWidget createImageViewerWidget(QWidget parent, Profile profile);
	public QWidget getTitleWidget();	
	public QWidget getImageViewerWidget();	
	public Vector<Tab> getTabsCached();	
		
	public String getTabIndexTitle();
	public void setTabIndexTitle(String tabTitle);
	
	public void refresh(); // called when the profile is changed	
	public void unload();
	
}
