package com.mixshare.rapid_evolution.ui.widgets.filter.options;

import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.trolltech.qt.gui.QWidget;

abstract public class AbstractSearchOptionsUI extends QWidget {

	protected int currentCount;
	
	public int getCurrentCount() { return currentCount; }
	
	abstract public int update(SearchParameters songParameters);
	
}
