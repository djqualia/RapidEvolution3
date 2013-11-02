package com.mixshare.rapid_evolution.ui.widgets.profile.search.artist;

import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.SimilarTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.search.artist.SimilarArtistProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class ArtistSimilarTableWidget extends RecordTabTableWidget {

	private SimilarTabTableProxyModel similarProxy;
	
	public ArtistSimilarTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		similarProxy = new SimilarArtistProxyModel(tabTableWidget, tableManager, tabTableView);
		if (UIProperties.hasProperty("artist_similarity_show_type"))
			similarProxy.setFilterType(UIProperties.getByte("artist_similarity_show_type"));
		return similarProxy;
	}
	
	public String[] getShowTypes() { return new String[] { Translations.get("artists_all"), Translations.get("artists_mine"), Translations.get("artists_new") }; }
	public byte getShowType() {
		if (UIProperties.hasProperty("artist_similarity_show_type"))
			return UIProperties.getByte("artist_similarity_show_type");
		return 0;
	}
	
    public void setShowType() {
    	if (similarProxy != null) {
    		if (searchBarWidget.getFilterCombo().currentIndex() == 0)
    			similarProxy.setFilterType(SimilarTabTableProxyModel.FILTER_TYPE_SHOW_ALL);
    		else if (searchBarWidget.getFilterCombo().currentIndex() == 1)
    			similarProxy.setFilterType(SimilarTabTableProxyModel.FILTER_TYPE_SHOW_MINE);
    		else if (searchBarWidget.getFilterCombo().currentIndex() == 2)
    			similarProxy.setFilterType(SimilarTabTableProxyModel.FILTER_TYPE_SHOW_NEW);
    		UIProperties.setProperty("artist_similarity_show_type", String.valueOf(searchBarWidget.getFilterCombo().currentIndex()));
    		similarProxy.invalidate();
    	}
    }
	
}