package com.mixshare.rapid_evolution.ui.model.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableSortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.common.table.CommonRecordTableView;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;

/**
 * This class performs the common filtering between the search items (artists, labels, releases, songs).
 * It also controls the drag and drop operations from the search models.
 * 
 * Sub-classes should make sure when over-riding filterAcceptsRow(...) to call super.filterAcceptsRow(...) appropriately...
 */
abstract public class SearchProxyModel extends TableSortFilterProxyModel implements AllColumns {

	static private Logger log = Logger.getLogger(SearchProxyModel.class);
		
	////////////
	// FIELDS //
	////////////
	
	protected SearchParameters searchParameters = null;
	
	protected CommonRecordTableView searchView = null;
		
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SearchProxyModel() { }
	public SearchProxyModel(QObject parent, TableModelManager modelManager, CommonRecordTableView searchView) {
		super(parent, modelManager);	
		this.searchView = searchView;
		setSupportedDragActions(Qt.DropAction.CopyAction);
		if (EMPTY_INITIAL_RESULTS_MODE && getSearchModelManager().isLazySearchSupported())
			setDynamicSortFilter(false);
	}
	
	/////////////
	// GETTERS //
	/////////////
		
	public SearchModelManager getSearchModelManager() { return (SearchModelManager)modelManager; }
	
	public String getSearchText() {
		CommonSearchParameters commonParameters = (CommonSearchParameters)searchParameters;
		if ((commonParameters != null) && (commonParameters.getSearchText() != null))
			return commonParameters.getSearchText();
		return "";
	}
	
	public SearchParameters getSearchParameters() { return searchParameters; }

	public CommonRecordTableView getSearchView() {
		return searchView;
	}
	
	public boolean isLazySearchSupported() { return getSearchModelManager().isLazySearchSupported(); }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setSearchText(String searchText) {
		SearchSearchParameters commonParameters = (SearchSearchParameters)searchParameters;
		commonParameters.setSearchText(searchText);
	}
	
	public void setStyles(FilterSelection selectedStyles) {
		SearchSearchParameters commonParameters = (SearchSearchParameters)searchParameters;
		commonParameters.setStylesSelection(selectedStyles);
	}	

	public void setTags(FilterSelection selectedTags) {
		SearchSearchParameters commonParameters = (SearchSearchParameters)searchParameters;
		commonParameters.setTagsSelection(selectedTags);
	}	

	public void setPlaylists(FilterSelection selectedPlaylists) {
		SearchSearchParameters commonParameters = (SearchSearchParameters)searchParameters;
		commonParameters.setPlaylistsSelection(selectedPlaylists);
	}	

	public void setSortOrdering(Vector<ColumnOrdering> sortOrdering) {
		SearchSearchParameters commonParameters = (SearchSearchParameters)searchParameters;
		byte[] sortTypes = new byte[sortOrdering.size()];
		boolean[] sortDescending = new boolean[sortOrdering.size()];
		int i = 0;
		for (ColumnOrdering ordering : sortOrdering) {
			sortDescending[i] = !ordering.isAscending();
			sortTypes[i] = CommonSearchParameters.getSortTypeFromColumnId(ordering.getColumnId());
			++i;
		}
		commonParameters.setSortType(sortTypes);
		commonParameters.setSortDescending(sortDescending);		
	}
	
	public void setSearchView(CommonRecordTableView searchView) {
		this.searchView = searchView;
	}
	public void setSearchParameters(SearchParameters searchParameters) {
		this.searchParameters = searchParameters;
	}
	
	/////////////
	// METHODS //
	/////////////
					
	public List<String> mimeTypes() {
		List<String> result = new ArrayList<String>();
		result.add(DragDropUtil.MIME_TYPE_FILENAME_LIST);
		return result;		
	}
	
	public QMimeData mimeData(List<QModelIndex> indexes) {		
		return DragDropUtil.getMimeDataForSearchRecords(searchView.getSelectedRecords());
	}

	protected void modelRefreshCallout() {
		if (SearchWidgetUI.instance.getCurrentSearchView().getProxyModel() == this)
			SearchWidgetUI.instance.updateFilter();
	}
	
}
