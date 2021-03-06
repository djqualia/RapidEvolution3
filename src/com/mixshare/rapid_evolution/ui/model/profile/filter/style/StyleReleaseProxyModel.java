package com.mixshare.rapid_evolution.ui.model.profile.filter.style;

import java.util.ArrayList;
import java.util.List;

import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class StyleReleaseProxyModel extends RecordTabTableProxyModel {

	static public final byte FILTER_TYPE_ALL_RELEASES = 0;
	static public final byte FILTER_TYPE_MY_RELEASES = 1;
	static public final byte FILTER_TYPE_NEW_RELEASES = 2;
	
	////////////
	// FIELDS //
	////////////
		
	private byte filterType;
	private StyleReleaseTabTableWidget widget;
		
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public StyleReleaseProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView, StyleReleaseTabTableWidget widget) {
		super(parent, modelManager, tableView);	
		searchParameters = new ReleaseSearchParameters();
		setDynamicSortFilter(false);
		this.widget = widget;
	}
	
	public void setFilterType(byte filterType) {
		this.filterType = filterType;
	}
	
	public boolean isLazySearchSupported() { return true; }
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		if (EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported())
			return true;
		SearchRecord searchRecord = (SearchRecord)getTableModelManager().getRecordForRow(sourceRow);
		if (searchRecord != null) {
			if ((filterType == FILTER_TYPE_MY_RELEASES) && searchRecord.isExternalItem())
				return false;
			if ((filterType == FILTER_TYPE_NEW_RELEASES) && !searchRecord.isExternalItem())
				return false;
			return super.filterAcceptsRow(sourceRow, sourceParent);
		} else {
			return false;
		}
	}	
	
	public List<String> mimeTypes() {
		List<String> result = new ArrayList<String>();
		result.add(DragDropUtil.MIME_TYPE_RELEASE_IDENTIFIER_LIST);
		return result;		
	}		

	protected void modelRefreshCallout() {
		widget.updateFilter();
	}		
}
