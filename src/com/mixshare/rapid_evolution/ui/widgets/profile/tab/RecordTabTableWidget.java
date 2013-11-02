package com.mixshare.rapid_evolution.ui.widgets.profile.tab;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;
import com.mixshare.rapid_evolution.ui.widgets.filter.playlists.PlaylistsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.styles.StylesWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.tags.TagsWidgetUI;

abstract public class RecordTabTableWidget extends CommonTabTableWidget {
	
	static private Logger log = Logger.getLogger(RecordTabTableWidget.class);    			
    	
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
	public RecordTabTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
    }	
        
    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    abstract public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView);
    
    /////////////
    // GETTERS //
    /////////////
    
    protected SortTableView createTableView(TableModelManager modelManager) {
    	return new RecordTabTableView((RecordTableModelManager)modelManager);
    }
    
    protected SortFilterProxyModel getSortFilterProxyModel(TableModelManager modelManager, SortTableView itemView) {
    	return getTabTableProxyModel(this, (RecordTableModelManager)modelManager, (RecordTabTableView)itemView);
    }
    
    public RecordTabTableProxyModel getRecordTabTableProxyModel() { return (RecordTabTableProxyModel)modelManager.getProxyModel(); }
    
    
    /////////////
    // METHODS //
    /////////////
    
    public void updateFilter() {
    	if (log.isTraceEnabled())
    		log.trace("updateFilter(): invalidating...");
    	if (searchBarWidget != null) {
    		getRecordTabTableProxyModel().setSearchText(searchBarWidget.getFilterText().text());
    		getRecordTabTableProxyModel().applyFilter();
    		((RecordTableModelManager)modelManager).setSourceColumnVisibilities(itemView);
    	}
    }
    
    
}