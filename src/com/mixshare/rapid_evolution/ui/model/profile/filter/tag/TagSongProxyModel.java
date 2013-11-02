package com.mixshare.rapid_evolution.ui.model.profile.filter.tag;

import java.util.ArrayList;
import java.util.List;

import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class TagSongProxyModel extends RecordTabTableProxyModel {

	static public final byte FILTER_TYPE_ALL_SONGS = 0;
	static public final byte FILTER_TYPE_MY_SONGS = 1;
	static public final byte FILTER_TYPE_NEW_SONGS = 2;
	
	////////////
	// FIELDS //
	////////////
		
	private byte filterType;
	private TagSongTabTableWidget widget;
		
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public TagSongProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView, TagSongTabTableWidget widget) {
		super(parent, modelManager, tableView);	
		searchParameters = new SongSearchParameters();
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
			if ((filterType == FILTER_TYPE_MY_SONGS) && searchRecord.isExternalItem())
				return false;
			if ((filterType == FILTER_TYPE_NEW_SONGS) && !searchRecord.isExternalItem())
				return false;
			return super.filterAcceptsRow(sourceRow, sourceParent);
		} else {
			return false;
		}
	}	
	
	public List<String> mimeTypes() {
		List<String> result = new ArrayList<String>();
		result.add(DragDropUtil.MIME_TYPE_SONG_IDENTIFIER_LIST);
		result.add(DragDropUtil.MIME_TYPE_FILENAME_LIST);
		return result;		
	}		

	protected void modelRefreshCallout() {
		widget.updateFilter();
	}	
	
}
