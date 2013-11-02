package com.mixshare.rapid_evolution.ui.widgets.profile.search.artist;

import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.artist.ArtistSongProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class ArtistSongTabTableWidget extends RecordTabTableWidget {

	private ArtistSongProxyModel proxyModel;
	
	public ArtistSongTabTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		proxyModel = new ArtistSongProxyModel(tabTableWidget, tableManager, tabTableView);
		if (UIProperties.hasProperty("artist_songs_show_type"))
			proxyModel.setFilterType(UIProperties.getByte("artist_songs_show_type"));
		return proxyModel;
	}
	
    public String[] getShowTypes() { return new String[] { Translations.get("songs_all"), Translations.get("songs_mine"), Translations.get("songs_missing") }; }
	public byte getShowType() {
		if (UIProperties.hasProperty("artist_songs_show_type"))
			return UIProperties.getByte("artist_songs_show_type");
		return 0;
	}

	public void setShowType() { 
    	if (proxyModel != null) {
    		if (searchBarWidget.getFilterCombo().currentIndex() == 0)
    			proxyModel.setFilterType(ArtistSongProxyModel.FILTER_TYPE_ALL_SONGS);
    		else if (searchBarWidget.getFilterCombo().currentIndex() == 1)
    			proxyModel.setFilterType(ArtistSongProxyModel.FILTER_TYPE_MY_SONGS);
    		else if (searchBarWidget.getFilterCombo().currentIndex() == 2)
    			proxyModel.setFilterType(ArtistSongProxyModel.FILTER_TYPE_MISSING_SONGS);
    		UIProperties.setProperty("artist_songs_show_type", String.valueOf(searchBarWidget.getFilterCombo().currentIndex()));
    		proxyModel.invalidate();
    	}    	
    }
	
}
