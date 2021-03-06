package com.mixshare.rapid_evolution.ui.model.search.release;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class RecommendedReleaseProxyModel extends ReleaseProxyModel {

	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public RecommendedReleaseProxyModel(QObject parent, TableModelManager modelManager, SearchTableView searchView) {
		super(parent, modelManager, searchView);		
		((ReleaseSearchParameters)searchParameters).setInternalItemsOnly(false);
		((ReleaseSearchParameters)searchParameters).setExternalItemsOnly(true);				
	}

	/////////////
	// METHODS //
	/////////////
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		boolean pass = super.filterAcceptsRow(sourceRow, sourceParent);
		if (pass) {
			SearchRecord searchRecord = (SearchRecord)getSearchModelManager().getRecordForRow(sourceRow);
			if (searchRecord == null)
				return false;
			if (!searchRecord.isExternalItem())
				pass = false;
			if (pass) {			
				float preference = Database.getUserProfile().computePreference(searchRecord);
				if (preference < RE3Properties.getFloat("minimum_recommended_preference"))
					pass = false;
			}
		}
		return pass;
	}
	
}
