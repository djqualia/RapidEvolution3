package com.mixshare.rapid_evolution.ui.model.profile.search.artist;

import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.LabelTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class ArtistLabelTabTableWidget extends RecordTabTableWidget {

	public ArtistLabelTabTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		return new LabelTabTableProxyModel(tabTableWidget, tableManager, tabTableView);
	}
	
    public String[] getShowTypes() { return new String[0]; }
    public byte getShowType() { return (byte)0; }

    public void setShowType() { }
	
}
