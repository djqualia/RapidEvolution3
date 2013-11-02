package com.mixshare.rapid_evolution.ui.model.profile.filter.playlist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.filter.FilterProfileArtistsModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.search_tabs.FilterArtistTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class PlaylistArtistTabTableWidget extends RecordTabTableWidget {

    static private Logger log = Logger.getLogger(PlaylistArtistTabTableWidget.class);	
	
    private FilterProfileArtistsModel model;			
	private PlaylistArtistProxyModel proxyModel;
	
	public PlaylistArtistTabTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
		this.model = (FilterProfileArtistsModel)modelManager.getModelPopulator();														
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		proxyModel = new PlaylistArtistProxyModel(tabTableWidget, tableManager, tabTableView, this);
		if (UIProperties.hasProperty("playlist_artists_show_type"))
			proxyModel.setFilterType(UIProperties.getByte("playlist_artists_show_type"));
		return proxyModel;
	}
	
    public String[] getShowTypes() { return new String[] { }; }
	public byte getShowType() {
		if (UIProperties.hasProperty("playlist_artists_show_type"))
			return UIProperties.getByte("playlist_artists_show_type");
		return 0;
	}

    protected SortTableView createTableView(TableModelManager modelManager) {
    	return new FilterArtistTabTableView((RecordTableModelManager)modelManager);
    }
	
	public void setShowType() { 
		updateFilter();
    }

    public void updateFilter() {
    	if (log.isTraceEnabled())
    		log.trace("updateFilter(): invalidating...");
    	if (searchBarWidget != null) {
	    	((SearchSearchParameters)proxyModel.getSearchParameters()).setSearchText(searchBarWidget.getFilterText().text());
	    	if (model.update()) {
	    		modelManager.refresh();
	    		proxyModel.invalidate();
	    	}
    	}
    }
	
}
