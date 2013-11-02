package com.mixshare.rapid_evolution.ui.model.profile.search.label;

import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class LabelReleaseProxyModel extends RecordTabTableProxyModel {

	static public final byte FILTER_TYPE_ALL_RELEASES = 0;
	static public final byte FILTER_TYPE_MY_RELEASES = 1;
	static public final byte FILTER_TYPE_MISSING_RELEASES = 2;
	
	////////////
	// FIELDS //
	////////////
		
	private byte filterType;
		
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public LabelReleaseProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView) {
		super(parent, modelManager, tableView);	
		searchParameters = new ReleaseSearchParameters();
	}
	
	public void setFilterType(byte filterType) {
		this.filterType = filterType;
	}
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		SearchRecord searchRecord = (SearchRecord)getTableModelManager().getRecordForRow(sourceRow);
		if (searchRecord != null) {
			if ((filterType == FILTER_TYPE_MY_RELEASES) && searchRecord.isExternalItem())
				return false;
			if ((filterType == FILTER_TYPE_MISSING_RELEASES) && !searchRecord.isExternalItem())
				return false;
			return super.filterAcceptsRow(sourceRow, sourceParent);
		} else {
			return false;
		}
	}	
	
}
