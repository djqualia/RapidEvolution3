package com.mixshare.rapid_evolution.ui.model.profile.filter.tag;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.filter.FilterProfileReleasesModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.search_tabs.FilterReleaseTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class TagReleaseTabTableWidget extends RecordTabTableWidget {

    static private Logger log = Logger.getLogger(TagReleaseTabTableWidget.class);	
	
    private FilterProfileReleasesModel model;	
	private TagReleaseProxyModel proxyModel;
	
	public TagReleaseTabTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
		this.model = (FilterProfileReleasesModel)modelManager.getModelPopulator();				
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		proxyModel = new TagReleaseProxyModel(tabTableWidget, tableManager, tabTableView, this);
		if (UIProperties.hasProperty("tag_releases_show_type"))
			proxyModel.setFilterType(UIProperties.getByte("tag_releases_show_type"));
		return proxyModel;
	}
	
    public String[] getShowTypes() { return new String[] { Translations.get("releases_all"), Translations.get("releases_mine"), Translations.get("releases_new") }; }
	public byte getShowType() {
		if (UIProperties.hasProperty("tag_releases_show_type"))
			return UIProperties.getByte("tag_releases_show_type");
		return 0;
	}
	
    protected SortTableView createTableView(TableModelManager modelManager) {
    	return new FilterReleaseTabTableView((RecordTableModelManager)modelManager);
    }	

	public void setShowType() { 
    	if (proxyModel != null) {
    		if (searchBarWidget.getFilterCombo().currentIndex() == 0) {
    			proxyModel.setFilterType(TagReleaseProxyModel.FILTER_TYPE_ALL_RELEASES);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setInternalItemsOnly(false);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setExternalItemsOnly(false);    			
    		} else if (searchBarWidget.getFilterCombo().currentIndex() == 1) {
    			proxyModel.setFilterType(TagReleaseProxyModel.FILTER_TYPE_MY_RELEASES);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setInternalItemsOnly(true);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setExternalItemsOnly(false);    			
    		} else if (searchBarWidget.getFilterCombo().currentIndex() == 2) {
    			proxyModel.setFilterType(TagReleaseProxyModel.FILTER_TYPE_NEW_RELEASES);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setInternalItemsOnly(false);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setExternalItemsOnly(true);    			
    		}
    		UIProperties.setProperty("tag_releases_show_type", String.valueOf(searchBarWidget.getFilterCombo().currentIndex()));
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
