package com.mixshare.rapid_evolution.ui.model.profile.filter.tag;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.filter.FilterProfileArtistsModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.search_tabs.FilterArtistTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class TagArtistTabTableWidget extends RecordTabTableWidget {

    static private Logger log = Logger.getLogger(TagArtistTabTableWidget.class);	
	
    private FilterProfileArtistsModel model;		
	private TagArtistProxyModel proxyModel;
	
	public TagArtistTabTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
		this.model = (FilterProfileArtistsModel)modelManager.getModelPopulator();						
	}	
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		proxyModel = new TagArtistProxyModel(tabTableWidget, tableManager, tabTableView, this);
		if (UIProperties.hasProperty("tag_artists_show_type"))
			proxyModel.setFilterType(UIProperties.getByte("tag_artists_show_type"));
		return proxyModel;
	}
	
    public String[] getShowTypes() { return new String[] { Translations.get("artists_all"), Translations.get("artists_mine"), Translations.get("artists_new") }; }
	public byte getShowType() {
		if (UIProperties.hasProperty("tag_artists_show_type"))
			return UIProperties.getByte("tag_artists_show_type");
		return 0;
	}
	
    protected SortTableView createTableView(TableModelManager modelManager) {
    	return new FilterArtistTabTableView((RecordTableModelManager)modelManager);
    }

	public void setShowType() { 
    	if (proxyModel != null) {
    		if (searchBarWidget.getFilterCombo().currentIndex() == 0) {
    			proxyModel.setFilterType(TagArtistProxyModel.FILTER_TYPE_ALL_ARTISTS);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setInternalItemsOnly(false);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setExternalItemsOnly(false);    			
    		} else if (searchBarWidget.getFilterCombo().currentIndex() == 1) {
    			proxyModel.setFilterType(TagArtistProxyModel.FILTER_TYPE_MY_ARTISTS);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setInternalItemsOnly(true);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setExternalItemsOnly(false);
    		} else if (searchBarWidget.getFilterCombo().currentIndex() == 2) {
    			proxyModel.setFilterType(TagArtistProxyModel.FILTER_TYPE_NEW_ARTISTS);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setInternalItemsOnly(false);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setExternalItemsOnly(true);    			
    		}
    		UIProperties.setProperty("tag_artists_show_type", String.valueOf(searchBarWidget.getFilterCombo().currentIndex()));
    		updateFilter();
    	}    	
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
