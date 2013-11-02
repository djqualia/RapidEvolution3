package com.mixshare.rapid_evolution.ui.model.profile.search.artist;

import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QObject;

public class ArtistTabTableProxyModel extends RecordTabTableProxyModel {

	public ArtistTabTableProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView) {
		super(parent, modelManager, tableView);
		searchParameters = new ArtistSearchParameters();
	}
	
}
