package com.mixshare.rapid_evolution.ui.model.profile.filter.playlist;

import java.util.ArrayList;
import java.util.List;

import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class PlaylistLabelProxyModel extends RecordTabTableProxyModel {

	////////////
	// FIELDS //
	////////////
		
	private byte filterType;
	private PlaylistLabelTabTableWidget widget;
		
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public PlaylistLabelProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView, PlaylistLabelTabTableWidget widget) {
		super(parent, modelManager, tableView);	
		searchParameters = new LabelSearchParameters();
		this.widget = widget;
	}
	
	public void setFilterType(byte filterType) {
		this.filterType = filterType;
	}
	
	public boolean isLazySearchSupported() { return true; }
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		if (EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported())
			return true;
		return super.filterAcceptsRow(sourceRow, sourceParent);
	}	

	public List<String> mimeTypes() {
		List<String> result = new ArrayList<String>();
		result.add(DragDropUtil.MIME_TYPE_LABEL_IDENTIFIER_LIST);
		return result;		
	}		

	protected void modelRefreshCallout() {
		widget.updateFilter();
	}		
}
