package com.mixshare.rapid_evolution.ui.widgets.profile.search.release;

import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.release.ReleaseSongProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class ReleaseSongTabTableWidget extends RecordTabTableWidget {

	private ReleaseSongProxyModel proxyModel;
	
	public ReleaseSongTabTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		proxyModel = new ReleaseSongProxyModel(tabTableWidget, tableManager, tabTableView);
		if (UIProperties.hasProperty("release_songs_show_type"))
			proxyModel.setFilterType(UIProperties.getByte("release_songs_show_type"));
		return proxyModel;
	}
	
    public String[] getShowTypes() { return new String[] { Translations.get("songs_all"), Translations.get("songs_mine"), Translations.get("songs_missing") }; }
	public byte getShowType() {
		if (UIProperties.hasProperty("release_songs_show_type"))
			return UIProperties.getByte("release_songs_show_type");
		return 0;
	}

	public void setShowType() { 
    	if (proxyModel != null) {
    		if (searchBarWidget.getFilterCombo().currentIndex() == 0)
    			proxyModel.setFilterType(ReleaseSongProxyModel.FILTER_TYPE_ALL_SONGS);
    		else if (searchBarWidget.getFilterCombo().currentIndex() == 1)
    			proxyModel.setFilterType(ReleaseSongProxyModel.FILTER_TYPE_MY_SONGS);
    		else if (searchBarWidget.getFilterCombo().currentIndex() == 2)
    			proxyModel.setFilterType(ReleaseSongProxyModel.FILTER_TYPE_MISSING_SONGS);
    		UIProperties.setProperty("release_songs_show_type", String.valueOf(searchBarWidget.getFilterCombo().currentIndex()));
    		proxyModel.invalidate();
    	}    	
    }
	
}
