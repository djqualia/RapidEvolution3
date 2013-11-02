package com.mixshare.rapid_evolution.ui.widgets.profile.search.label;

import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.SimilarLabelProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class LabelSimilarTableWidget extends RecordTabTableWidget {

	public LabelSimilarTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {
		return new SimilarLabelProxyModel(tabTableWidget, tableManager, tabTableView);
	}
	
    public String[] getShowTypes() { return new String[0]; }
    public byte getShowType() { return (byte)0; }

    public void setShowType() { }
	
}
