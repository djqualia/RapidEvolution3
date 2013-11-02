package com.mixshare.rapid_evolution.ui.widgets.profile.search.label;

import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.LabelReleaseProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class LabelReleaseTabTableWidget extends RecordTabTableWidget {

	private LabelReleaseProxyModel proxyModel;
	
	public LabelReleaseTabTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		proxyModel = new LabelReleaseProxyModel(tabTableWidget, tableManager, tabTableView);
		if (UIProperties.hasProperty("label_releases_show_type"))
			proxyModel.setFilterType(UIProperties.getByte("label_releases_show_type"));
		return proxyModel;
	}
	
    public String[] getShowTypes() { return new String[] { Translations.get("releases_all"), Translations.get("releases_mine"), Translations.get("releases_missing") }; }
	public byte getShowType() {
		if (UIProperties.hasProperty("label_releases_show_type"))
			return UIProperties.getByte("label_releases_show_type");
		return 0;
	}

	public void setShowType() { 
    	if (proxyModel != null) {
    		if (searchBarWidget.getFilterCombo().currentIndex() == 0)
    			proxyModel.setFilterType(LabelReleaseProxyModel.FILTER_TYPE_ALL_RELEASES);
    		else if (searchBarWidget.getFilterCombo().currentIndex() == 1)
    			proxyModel.setFilterType(LabelReleaseProxyModel.FILTER_TYPE_MY_RELEASES);
    		else if (searchBarWidget.getFilterCombo().currentIndex() == 2)
    			proxyModel.setFilterType(LabelReleaseProxyModel.FILTER_TYPE_MISSING_RELEASES);
    		UIProperties.setProperty("label_releases_show_type", String.valueOf(searchBarWidget.getFilterCombo().currentIndex()));
    		proxyModel.invalidate();
    	}    	
    }
	
}
