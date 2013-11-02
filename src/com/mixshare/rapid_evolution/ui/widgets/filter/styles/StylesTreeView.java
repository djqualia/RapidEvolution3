package com.mixshare.rapid_evolution.ui.widgets.filter.styles;

import java.util.Vector;

import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.widgets.CentralWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterTreeView;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.gui.QAction;

public class StylesTreeView extends FilterTreeView {

    public StylesTreeView(FilterModelManager modelManager) {
    	super(modelManager);   
    }
    
    public QAction getAddAction() { return addAction; }
	
    protected void executeSelectionChangeActions() {
    	if (CentralWidgetUI.instance != null)
    		CentralWidgetUI.instance.setStyleItemText(this.getFilterSelection().size());
    	super.executeSelectionChangeActions();
    }
    
    protected boolean areSelectedInstancesMergable(Vector<FilterHierarchyInstance> selectedInstances) {
    	return true;
    }

    protected void sortByName() { sortByColumn(COLUMN_STYLE_NAME.getColumnId()); }
    
    protected void sortByInvisibleColumn(Column viewColumn, int viewIndex) {
		viewColumn.setHidden(false);
		StylesWidgetUI.instance.updateVisibleColumns();
		sortByColumn(viewIndex, SortOrder.AscendingOrder);    	
    }

    protected void selectedFilter(FilterHierarchyInstance filterInstance) { }
    
}
