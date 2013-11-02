package com.mixshare.rapid_evolution.ui.widgets.profile.search.song;

import com.mixshare.rapid_evolution.ui.model.profile.search.song.CompatibleSongsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.SongTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class CompatibleSongProxyModel extends SongTabTableProxyModel {

	private CompatibleSongTableWidget widget;
	
	public CompatibleSongProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView, CompatibleSongTableWidget widget) {
		super(parent, modelManager, tableView);
		this.widget = widget;
		((CompatibleSongsModelManager)widget.getModelManager()).setProxyModel(this);
	}
	
	public boolean isLazySearchSupported() { return true; }
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) { return true; }
	
	protected void modelRefreshCallout() {
		widget.updateFilter();
	}
	
}
