package com.mixshare.rapid_evolution.ui.model.profile.search.song;

import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.ui.model.profile.search.SimilarTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QObject;

public class SimilarSongProxyModel extends SimilarTabTableProxyModel {
	
	public SimilarSongProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView) {
		super(parent, modelManager, tableView);
		searchParameters = new SongSearchParameters();
	}	
	
}
