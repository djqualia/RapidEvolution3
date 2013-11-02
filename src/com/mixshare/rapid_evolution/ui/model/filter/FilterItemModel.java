package com.mixshare.rapid_evolution.ui.model.filter;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.tree.TreeItemModel;
import com.mixshare.rapid_evolution.ui.model.tree.TreeModelManager;
import com.trolltech.qt.core.QObject;

public class FilterItemModel extends TreeItemModel {

    static private final long serialVersionUID = 0L;    	
	
    static private Logger log = Logger.getLogger(FilterItemModel.class);    
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
    
	public FilterItemModel(int numRows, int numColumns, QObject parent, TreeModelManager modelManager) {
		super(numRows, numColumns, parent, modelManager);
		this.modelManager = modelManager;
	}	
	
	/////////////
	// METHODS //
	/////////////

	
}
