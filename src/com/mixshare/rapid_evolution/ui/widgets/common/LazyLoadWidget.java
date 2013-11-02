package com.mixshare.rapid_evolution.ui.widgets.common;

import com.trolltech.qt.gui.QShowEvent;
import com.trolltech.qt.gui.QWidget;

abstract public class LazyLoadWidget extends QWidget {

	////////////
	// FIELDS //
	////////////
	
	protected LazyLoaderInterface lazyLoader;
	protected boolean loaded;
	protected boolean isUnloading;
	
	/////////////////
	// CONSTRUCTOR //	
	/////////////////
	
	public LazyLoadWidget() {		 }			
	
	/////////////
	// GETTERS //
	/////////////
	
	public boolean isLoaded() { return loaded; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setIsUnloading(boolean isUnloading) { this.isUnloading = isUnloading; }	
	
	/////////////
	// METHODS //
	/////////////
	
	public void showEvent(QShowEvent e) {
		if (!loaded && !isUnloading) {
			lazyLoad();
		}
		super.showEvent(e);
	}
	
	public void lazyLoad() {
		loaded = true;		
		lazyLoader.populateWidget(this);
	}
			
}
