package com.mixshare.rapid_evolution.ui.widgets.common.tree;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.widgets.filter.tags.TagsWidgetUI;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.gui.QSortFilterProxyModel;
import com.trolltech.qt.gui.QTreeView;

abstract public class SortTreeView extends QTreeView {

    static private Logger log = Logger.getLogger(SortTreeView.class);    
	
	////////////
	// FIELDS //
	////////////
	
	protected ModelManagerInterface modelManager;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public SortTreeView(ModelManagerInterface modelManager) {
		this.modelManager = modelManager;		
	}
	
	//////////////
	// ABSTRACT //
	//////////////
	
	abstract protected void sortByInvisibleColumn(Column viewColumn, int viewIndex);
	
	/////////////
	// GETTERS //
	/////////////
	
	public ModelManagerInterface getModelManager() { return modelManager; }
	
    public QSortFilterProxyModel getProxyModel() { return (QSortFilterProxyModel)model(); }
	
	/////////////
	// METHODS //
	/////////////
	
	public void sortByColumn(short columnId) {
		if (modelManager.getSortOrdering().get(0).getColumnId() != columnId)
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
			//} else if (viewColumn.getColumnId() == columnId) {
				//sortByInvisibleColumn(viewColumn, viewIndex);
				//return;				
			//}
		}
	}	
	
}
