package com.mixshare.rapid_evolution.ui.model.search.song;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class RecommendedSongProxyModel extends SongProxyModel {

	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public RecommendedSongProxyModel(QObject parent, TableModelManager modelManager, SearchTableView searchView) {
		super(parent, modelManager, searchView);		
		((SongSearchParameters)searchParameters).setInternalItemsOnly(false);
		((SongSearchParameters)searchParameters).setExternalItemsOnly(true);						
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
