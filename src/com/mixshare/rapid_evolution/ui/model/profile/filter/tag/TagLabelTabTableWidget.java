package com.mixshare.rapid_evolution.ui.model.profile.filter.tag;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.filter.FilterProfileLabelsModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.search_tabs.FilterLabelTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class TagLabelTabTableWidget extends RecordTabTableWidget {

    static private Logger log = Logger.getLogger(TagLabelTabTableWidget.class);	
	
    private FilterProfileLabelsModel model;	
	private TagLabelProxyModel proxyModel;
	
	public TagLabelTabTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
		this.model = (FilterProfileLabelsModel)modelManager.getModelPopulator();						
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		proxyModel = new TagLabelProxyModel(tabTableWidget, tableManager, tabTableView, this);
		if (UIProperties.hasProperty("tag_labels_show_type"))
			proxyModel.setFilterType(UIProperties.getByte("tag_labels_show_type"));
		return proxyModel;
	}
	
    public String[] getShowTypes() { return new String[] { Translations.get("labels_all"), Translations.get("labels_mine"), Translations.get("labels_new") }; }
	public byte getShowType() {
		if (UIProperties.hasProperty("tag_labels_show_type"))
			return UIProperties.getByte("tag_labels_show_type");
		return 0;
	}

    protected SortTableView createTableView(TableModelManager modelManager) {
    	return new FilterLabelTabTableView((RecordTableModelManager)modelManager);
    }
	
	public void setShowType() { 
    	if (proxyModel != null) {
    		if (searchBarWidget.getFilterCombo().currentIndex() == 0) {
    			proxyModel.setFilterType(TagLabelProxyModel.FILTER_TYPE_ALL_LABELS);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setInternalItemsOnly(false);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setExternalItemsOnly(false);
    		} else if (searchBarWidget.getFilterCombo().currentIndex() == 1) {
    			proxyModel.setFilterType(TagLabelProxyModel.FILTER_TYPE_MY_LABELS);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setInternalItemsOnly(true);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setExternalItemsOnly(false);
    		} else if (searchBarWidget.getFilterCombo().currentIndex() == 2) {
    			proxyModel.setFilterType(TagLabelProxyModel.FILTER_TYPE_NEW_LABELS);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setInternalItemsOnly(false);
    			((SearchSearchParameters)proxyModel.getSearchParameters()).setExternalItemsOnly(true);    			
    		}
    		UIProperties.setProperty("tag_labels_show_type", String.valueOf(searchBarWidget.getFilterCombo().currentIndex()));
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
