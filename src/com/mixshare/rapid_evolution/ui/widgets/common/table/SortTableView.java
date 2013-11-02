package com.mixshare.rapid_evolution.ui.widgets.common.table;

import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.gui.QSortFilterProxyModel;
import com.trolltech.qt.gui.QTableView;

abstract public class SortTableView extends QTableView {

	////////////
	// FIELDS //
	////////////
	
	protected ModelManagerInterface modelManager;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public SortTableView(ModelManagerInterface modelManager) {
		this.modelManager = modelManager;		
	}
	
	/////////////
	// GETTERS //
	/////////////
	
    public QSortFilterProxyModel getProxyModel() { return (QSortFilterProxyModel)model(); }
	
    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////
    
    abstract public void setupEventListeners();
    
	/////////////
	// METHODS //
	/////////////
	    
	public void sortByColumn(short columnId) {
		if ((modelManager.getSortOrdering().size() == 0) || (modelManager.getSortOrdering().get(0).getColumnId() != columnId))
			modelManager.setPrimarySortColumn(columnId);
		int viewIndex = 0;
		for (int c = 0; c < modelManager.getNumColumns(); ++c) {
			Column viewColumn = modelManager.getViewColumnType(c);
			//if (!viewColumn.isHidden()) {
				if (viewColumn.getColumnId() == columnId) {	
					sortByColumn(viewIndex, SortOrder.AscendingOrder);
					return;
				}
				++viewIndex;
			//}
		}
	}	
	
}
