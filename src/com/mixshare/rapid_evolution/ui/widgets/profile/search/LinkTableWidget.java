package com.mixshare.rapid_evolution.ui.widgets.profile.search;

import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.LinkModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTableWidget;

public class LinkTableWidget extends TabTableWidget {

	public LinkTableWidget(TableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
	}
	
    /////////////
    // GETTERS //
    /////////////
    
    protected SortTableView createTableView(TableModelManager modelManager) {
    	return new LinkTableView((LinkModelManager)modelManager);    	
    }
    
    public String[] getShowTypes() { return new String[0]; }
    public byte getShowType() { return (byte)0; }
    
    ////////////
    // EVENTS //
    ////////////
    
    public void setShowType() { }
	
}
