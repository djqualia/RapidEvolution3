package com.mixshare.rapid_evolution.ui.model.search.song;

import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.ui.model.search.SearchProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class SongProxyModel extends SearchProxyModel {

	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public SongProxyModel() { }
	public SongProxyModel(QObject parent, TableModelManager modelManager, SearchTableView searchView) {
		super(parent, modelManager, searchView);
		searchParameters = new SongSearchParameters();
		((SongSearchParameters)searchParameters).setEnableRelativeSearch(true);
		((SongSearchParameters)searchParameters).setInternalItemsOnly(true);						
	}
	
	/////////////
	// METHODS //
	/////////////
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		if (EMPTY_INITIAL_RESULTS_MODE && getSearchModelManager().isLazySearchSupported()) // in this mode, we will update the model when searching and not utilize a proxy
			return true;
		if (searchParameters != null) {			
			SongRecord songRecord = (SongRecord)getSearchModelManager().getRecordForRow(sourceRow);
			SongSearchParameters songParameters = (SongSearchParameters)searchParameters;
			return (songParameters.matches(songRecord) > 0.0f);
		}
		return true;
	}
	
}
