package com.mixshare.rapid_evolution.ui.model.profile.search.label;

import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QObject;

public class LabelTabTableProxyModel extends RecordTabTableProxyModel {

	public LabelTabTableProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView) {
		super(parent, modelManager, tableView);
		searchParameters = new LabelSearchParameters();
	}
	
}
