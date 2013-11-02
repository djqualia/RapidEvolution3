package com.mixshare.rapid_evolution.ui.widgets.profile.search.song;

import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.SimilarTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.SimilarSongProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class SongSimilarTableWidget extends RecordTabTableWidget {

	private SimilarTabTableProxyModel similarProxy;
	
	public SongSimilarTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		similarProxy = new SimilarSongProxyModel(tabTableWidget, tableManager, tabTableView);
		if (UIProperties.hasProperty("song_similarity_show_type"))
			similarProxy.setFilterType(UIProperties.getByte("song_similarity_show_type"));
		return similarProxy;
	}
	
	public String[] getShowTypes() { return new String[] { Translations.get("songs_all"), Translations.get("songs_mine"), Translations.get("songs_new") }; }
	public byte getShowType() {
		if (UIProperties.hasProperty("song_similarity_show_type"))
			return UIProperties.getByte("song_similarity_show_type");
		return 0;
	}
	
    protected SortTableView createTableView(TableModelManager modelManager) {
    	SubSongTabTableView result = new SubSongTabTableView((RecordTableModelManager)modelManager);
    	result.setItemDelegate(new HarmonicColoringItemDelegate(this, modelManager));
    	return result;    	
    }	

	public void setShowType() {
    	if (similarProxy != null) {
    		if (searchBarWidget.getFilterCombo().currentIndex() == 0)
    			similarProxy.setFilterType(SimilarTabTableProxyModel.FILTER_TYPE_SHOW_ALL);
    		else if (searchBarWidget.getFilterCombo().currentIndex() == 1)
    			similarProxy.setFilterType(SimilarTabTableProxyModel.FILTER_TYPE_SHOW_MINE);
    		else if (searchBarWidget.getFilterCombo().currentIndex() == 2)
    			similarProxy.setFilterType(SimilarTabTableProxyModel.FILTER_TYPE_SHOW_NEW);
    		UIProperties.setProperty("song_similarity_show_type", String.valueOf(searchBarWidget.getFilterCombo().currentIndex()));
    		similarProxy.invalidate();
    	}
    }
	
}