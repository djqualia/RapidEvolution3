package com.mixshare.rapid_evolution.ui.model.search.label;

import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.ui.model.search.SearchProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class LabelProxyModel extends SearchProxyModel {

	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public LabelProxyModel() { }
	public LabelProxyModel(QObject parent, TableModelManager modelManager, SearchTableView searchView) {
		super(parent, modelManager, searchView);
		searchParameters = new LabelSearchParameters();
		((LabelSearchParameters)searchParameters).setEnableRelativeSearch(true);		
		((LabelSearchParameters)searchParameters).setInternalItemsOnly(true);		
	}
			
	/////////////
	// METHODS //
	/////////////
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		if (EMPTY_INITIAL_RESULTS_MODE && getSearchModelManager().isLazySearchSupported()) // in this mode, we will update the model when searching and not utilize a proxy
			return true;
		if (searchParameters != null) {
			LabelRecord labelRecord = (LabelRecord)getSearchModelManager().getRecordForRow(sourceRow);
			LabelSearchParameters labelParameters = (LabelSearchParameters)searchParameters;
			return (labelParameters.matches(labelRecord) > 0.0f);
		}
		return true;
	}
	
}
