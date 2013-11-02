package com.mixshare.rapid_evolution.ui.widgets.profile.search.song;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.SongMixoutsProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class MixoutTableWidget extends RecordTabTableWidget {

	static private Logger log = Logger.getLogger(MixoutTableWidget.class);    			
	
	public MixoutTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		return new SongMixoutsProxyModel(tabTableWidget, tableManager, tabTableView);
	}
	
    protected SortTableView createTableView(TableModelManager modelManager) {
    	MixoutTableView result = new MixoutTableView((RecordTableModelManager)modelManager);
    	result.setItemDelegate(new HarmonicColoringItemDelegate(this, modelManager));
    	return result;
    }		
	
    public String[] getShowTypes() { return new String[0]; }
    public byte getShowType() { return (byte)0; }

    public void setShowType() { }
	
    public void updateFilter() {
    	if (log.isTraceEnabled())
    		log.trace("updateFilter(): invalidating...");
    	if ((proxyModel != null) && (modelManager != null)) {
        	if (searchBarWidget != null) {
        		((SongSearchParameters)((RecordTabTableProxyModel)proxyModel).getSearchParameters()).setSearchText(searchBarWidget.getFilterText().text());    		
        		modelManager.refresh();
        		proxyModel.invalidate();
        	}
    	}
    }
    
}
