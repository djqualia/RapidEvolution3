package com.mixshare.rapid_evolution.ui.model.profile.filter.playlist;

import java.util.ArrayList;
import java.util.List;

import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagArtistTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class PlaylistArtistProxyModel extends RecordTabTableProxyModel {

	////////////
	// FIELDS //
	////////////
		
	private byte filterType;
	private PlaylistArtistTabTableWidget widget;

	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public PlaylistArtistProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView, PlaylistArtistTabTableWidget widget) {
		super(parent, modelManager, tableView);	
		searchParameters = new ArtistSearchParameters();
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
		result.add(DragDropUtil.MIME_TYPE_ARTIST_IDENTIFIER_LIST);
		return result;		
	}		

	protected void modelRefreshCallout() {
		widget.updateFilter();
	}		
}
