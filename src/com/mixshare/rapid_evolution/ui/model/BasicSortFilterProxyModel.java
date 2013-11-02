package com.mixshare.rapid_evolution.ui.model;

import com.trolltech.qt.core.QObject;

public class BasicSortFilterProxyModel extends SortFilterProxyModel {

	public BasicSortFilterProxyModel() { super(); }
	public BasicSortFilterProxyModel(QObject parent, ModelManagerInterface modelManager) { super(parent, modelManager); }		

	protected void modelRefreshCallout() { }	
	public boolean isLazySearchSupported() { return false; }

	
}
