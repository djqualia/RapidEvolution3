package com.mixshare.rapid_evolution.ui.widgets.profile.filter;

import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QMouseEvent;

public class FilterTabTableView extends RecordTabTableView implements AllColumns {

	public FilterTabTableView(RecordTableModelManager modelManager) {
		super(modelManager);
		setAcceptDrops(true);
		setEditTriggers(QAbstractItemView.EditTrigger.DoubleClicked);
    	setItemDelegate(new FilterItemDelegate(this, modelManager));    	
	}
	
    protected void mouseDoubleClickEvent(QMouseEvent event) {
    	QModelIndex proxyIndex = indexAt(event.pos());
    	QModelIndex sourceIndex = getProxyModel().mapToSource(proxyIndex);
    	if (modelManager.getSourceColumnType(sourceIndex.column()).getColumnId() == COLUMN_DEGREE.getColumnId()) {
    		super.mouseDoubleClickEvent(event, true);
    	} else
    		super.mouseDoubleClickEvent(event);
    }
	
}
