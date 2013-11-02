package com.mixshare.rapid_evolution.ui.widgets.filter.tags;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.widgets.CentralWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterTreeView;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.gui.QAction;

public class TagsTreeView extends FilterTreeView {

	static private Logger log = Logger.getLogger(TagsTreeView.class);
	
    public TagsTreeView(FilterModelManager modelManager) {
    	super(modelManager);   
    }
    
    public QAction getAddAction() { return addAction; }
	
    protected void executeSelectionChangeActions() {
    	if (CentralWidgetUI.instance != null)
    		CentralWidgetUI.instance.setTagItemText(this.getFilterSelection().size());
    	super.executeSelectionChangeActions();
    }
    
    protected void sortByName() { sortByColumn(COLUMN_TAG_NAME.getColumnId()); }
    
    protected boolean areSelectedInstancesMergable(Vector<FilterHierarchyInstance> selectedInstances) {
    	return true;
    }
    
    protected void sortByInvisibleColumn(Column viewColumn, int viewIndex) {
		viewColumn.setHidden(false);
		if (TagsWidgetUI.instance != null)
			TagsWidgetUI.instance.updateVisibleColumns();		
		sortByColumn(viewIndex, SortOrder.AscendingOrder);
    }

    protected void selectedFilter(FilterHierarchyInstance filterInstance) { }
    
}
