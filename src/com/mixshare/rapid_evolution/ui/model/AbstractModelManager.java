package com.mixshare.rapid_evolution.ui.model;

import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QObject;

abstract public class AbstractModelManager implements ModelManagerInterface {

	abstract public void initialize(QObject parent);
	
	abstract public void write(LineWriter writer);

	abstract public void setPrimarySortColumn(short columnId);
		
}
