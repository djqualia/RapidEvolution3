package com.mixshare.rapid_evolution.ui.model.search.release;

import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.ui.model.search.SearchProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class ReleaseProxyModel extends SearchProxyModel {

	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public ReleaseProxyModel() { }
	public ReleaseProxyModel(QObject parent, TableModelManager modelManager, SearchTableView searchView) {
		super(parent, modelManager, searchView);
		searchParameters = new ReleaseSearchParameters();
		((ReleaseSearchParameters)searchParameters).setEnableRelativeSearch(true);
		((ReleaseSearchParameters)searchParameters).setInternalItemsOnly(true);				
	}
			
	/////////////
	// METHODS //
	/////////////
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		if (EMPTY_INITIAL_RESULTS_MODE && getSearchModelManager().isLazySearchSupported()) // in this mode, we will update the model when searching and not utilize a proxy
			return true;
		if (searchParameters != null) {
			ReleaseRecord releaseRecord = (ReleaseRecord)getSearchModelManager().getRecordForRow(sourceRow);
			ReleaseSearchParameters releaseParameters = (ReleaseSearchParameters)searchParameters;
			return (releaseParameters.matches(releaseRecord) > 0.0f);
		}
		return true;
	}
	
}
