package com.mixshare.rapid_evolution.ui.widgets.profile.tab;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.BasicSortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;

abstract public class TabTableWidget extends CommonTabTableWidget {
	
	static private Logger log = Logger.getLogger(TabTableWidget.class);
    			    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public TabTableWidget(TableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
    }    
    
    /////////////
    // GETTERS //
    /////////////
    
    protected SortTableView createTableView(TableModelManager modelManager) {
    	return new TabTableView(modelManager);
    }
    
    protected SortFilterProxyModel getSortFilterProxyModel(TableModelManager modelManager, SortTableView itemView) {
    	return new BasicSortFilterProxyModel(this, modelManager);
    }
    
}
