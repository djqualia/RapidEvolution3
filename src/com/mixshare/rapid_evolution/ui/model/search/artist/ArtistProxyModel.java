package com.mixshare.rapid_evolution.ui.model.search.artist;

import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.ui.model.search.SearchProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class ArtistProxyModel extends SearchProxyModel {

	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public ArtistProxyModel() { }
	public ArtistProxyModel(QObject parent, TableModelManager modelManager, SearchTableView searchView) {
		super(parent, modelManager, searchView);		
		searchParameters = new ArtistSearchParameters();
		((ArtistSearchParameters)searchParameters).setEnableRelativeSearch(true);
		((ArtistSearchParameters)searchParameters).setInternalItemsOnly(true);
	}

	/////////////
	// METHODS //
	/////////////
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		if (EMPTY_INITIAL_RESULTS_MODE && getSearchModelManager().isLazySearchSupported()) // in this mode, we will update the model when searching and not utilize a proxy
			return true;
		if (searchParameters != null) {
			ArtistRecord artistRecord = (ArtistRecord)getSearchModelManager().getRecordForRow(sourceRow);
			ArtistSearchParameters artistParameters = (ArtistSearchParameters)searchParameters;
			return (artistParameters.matches(artistRecord) > 0.0f);
		}
		return true;
	}
	
}
