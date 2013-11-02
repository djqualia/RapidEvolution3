package com.mixshare.rapid_evolution.ui.model.profile.search;

import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

abstract public class SimilarTabTableProxyModel extends RecordTabTableProxyModel {

	static public byte FILTER_TYPE_SHOW_ALL = 0;
	static public byte FILTER_TYPE_SHOW_MINE = 1;
	static public byte FILTER_TYPE_SHOW_NEW = 2;
	
	private byte filterType;
	
	public SimilarTabTableProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView) {
		super(parent, modelManager, tableView);	
	}	
	
	public byte getFilterType() { return filterType; }
	
	public void setFilterType(byte filterType) {
		this.filterType = filterType;
	}
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		SearchRecord searchRecord = (SearchRecord)getTableModelManager().getRecordForRow(sourceRow);
		if ((filterType == FILTER_TYPE_SHOW_MINE) && searchRecord.isExternalItem())
			return false;
		if ((filterType == FILTER_TYPE_SHOW_NEW) && !searchRecord.isExternalItem())
			return false;
		return super.filterAcceptsRow(sourceRow, sourceParent);
	}	
	
}
