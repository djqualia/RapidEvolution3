package com.mixshare.rapid_evolution.ui.model.profile.tab;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.ui.model.search.SearchProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

abstract public class RecordTabTableProxyModel extends SearchProxyModel {

	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public RecordTabTableProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView) {
		super(parent, modelManager, tableView);
	}
	
	public Vector<Record> getSelectedRecords() {
		this.searchView.selectionModel().selectedIndexes();
		Vector<Record> result = new Vector<Record>();
		for (QModelIndex index : searchView.selectionModel().selectedIndexes()) {
			QModelIndex sourceIndex = mapToSource(index);
			Record record = getTableModelManager().getRecordForRow(sourceIndex.row());
			if ((record != null) && !result.contains(record))
				result.add(record);
		}
		return result;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		if (searchParameters != null) {			
			SearchRecord record = (SearchRecord)getSearchModelManager().getRecordForRow(sourceRow);
			CommonSearchParameters parameters = (CommonSearchParameters)searchParameters;
			return (parameters.matches(record) > 0.0f);
		}
		return true;
	}	
}
