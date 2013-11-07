package com.mixshare.rapid_evolution.ui.model.profile.search.release;

import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class ReleaseSongProxyModel extends RecordTabTableProxyModel {

	static public final byte FILTER_TYPE_ALL_SONGS = 0;
	static public final byte FILTER_TYPE_MY_SONGS = 1;
	static public final byte FILTER_TYPE_MISSING_SONGS = 2;
	
	////////////
	// FIELDS //
	////////////
		
	private byte filterType;
		
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public ReleaseSongProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView) {
		super(parent, modelManager, tableView);	
		searchParameters = new SongSearchParameters();
	}
	
	public void setFilterType(byte filterType) {
		this.filterType = filterType;
	}
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		SearchRecord searchRecord = (SearchRecord)getTableModelManager().getRecordForRow(sourceRow);
		if (searchRecord != null) {
			if ((filterType == FILTER_TYPE_MY_SONGS) && searchRecord.isExternalItem())
				return false;
			if ((filterType == FILTER_TYPE_MISSING_SONGS) && !searchRecord.isExternalItem())
				return false;
			return super.filterAcceptsRow(sourceRow, sourceParent);
		} else {
			return false;
		}
	}	
	
}
