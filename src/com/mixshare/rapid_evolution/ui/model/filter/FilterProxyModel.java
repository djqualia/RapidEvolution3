package com.mixshare.rapid_evolution.ui.model.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.filter.FilterSearchParameters;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.tree.TreeSortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterTreeView;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;

abstract public class FilterProxyModel extends TreeSortFilterProxyModel {

    static private Logger log = Logger.getLogger(FilterProxyModel.class);    
		
    ////////////
    // FIELDS //
    ////////////
    
	protected String searchText = null;	
	
	private FilterWidgetUI widget = null;
	private FilterTreeView treeView = null;
	
	private transient FilterSearchParameters searchParams = null;
		
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public FilterProxyModel(QObject parent, FilterModelManager modelManager, FilterTreeView treeView, FilterWidgetUI widget) {
		super(parent, modelManager);
		this.treeView = treeView;
		this.widget = widget;
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public boolean isHideEmptyFilters();
	abstract public void setHideEmptyFilters(boolean hideEmptyFilters);
	abstract public FilterSearchParameters getNewFilterSearchParameters();
	
	/////////////
	// GETTERS //
	/////////////
	
	public FilterModelManager getFilterModelManager() { return (FilterModelManager)modelManager; }

	public String getSearchText() { return searchText; }
	public FilterTreeView getTreeView() { return treeView; }
	
	public FilterSearchParameters getSearchParameters() {
		if (searchParams == null) {
			searchParams = getNewFilterSearchParameters();			
			((CommonSearchParameters)searchParams).setSearchText(searchText);
		}
		return searchParams;
	}
		
	/////////////
	// SETTERS //
	/////////////
	
	public void setSearchText(String searchText) {
		this.searchText = searchText;
		searchParams = null;
	}
			
	public void setTreeView(FilterTreeView treeView) { this.treeView = treeView; }
	
	/////////////
	// METHODS //
	/////////////
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		if (EMPTY_INITIAL_RESULTS_MODE && getFilterModelManager().isLazySearchSupported()) // in this mode, we will update the model when searching and not utilize a proxy
			return true;
		QStandardItemModel thisModel = (QStandardItemModel)modelManager.getSourceModel();
		QStandardItem parentItem = thisModel.itemFromIndex(sourceParent);
		FilterRecord record = null;
		if (parentItem != null) {
			QStandardItem thisItem = parentItem.child(sourceRow, 0);			
			TreeHierarchyInstance filterInstance = (TreeHierarchyInstance)thisItem.data();
			record = (FilterRecord)filterInstance.getRecord();
		} else {
			// root style
			QStandardItem item = thisModel.item(sourceRow);
			if (item != null) {
				TreeHierarchyInstance filterInstance = (TreeHierarchyInstance)item.data();
				record = (FilterRecord)filterInstance.getRecord();
			} else {
				return false;
			}
		}
		if (record.isDisabled())
			return false;
		if (isHideEmptyFilters() && (SearchWidgetUI.instance != null)) {
			int currentSearchType = SearchWidgetUI.instance.getCurrentSearchType();
			int numRecords = 0;
			if (currentSearchType == 1)
				numRecords = record.getNumArtistRecords();
			else if (currentSearchType == 2)
				numRecords = record.getNumLabelRecords();
			else if (currentSearchType == 3)
				numRecords = record.getNumReleaseRecords();
			else if (currentSearchType == 4)
				numRecords = record.getNumSongRecords();			
			else if (currentSearchType == 5)
				numRecords = record.getNumExternalArtistRecords();			
			else if (currentSearchType == 6)
				numRecords = record.getNumExternalLabelRecords();			
			else if (currentSearchType == 7)
				numRecords = record.getNumExternalReleaseRecords();			
			else if (currentSearchType == 8)
				numRecords = record.getNumExternalSongRecords();			
			if ((numRecords == 0) && areAllChildrenZeroes(record))
				return false;
		}		
		return checkChildrenRecursively(record) || checkParentsRecursively(record);			
	}
	
	protected boolean areAllChildrenZeroes(FilterRecord record) {
		int currentSearchType = SearchWidgetUI.instance.getCurrentSearchType();
		for (HierarchicalRecord child : record.getChildRecords()) {
			int numRecords = 0;
			FilterRecord filter = (FilterRecord)child;
			if (currentSearchType == 1)
				numRecords = filter.getNumArtistRecords();
			else if (currentSearchType == 2)
				numRecords = filter.getNumLabelRecords();
			else if (currentSearchType == 3)
				numRecords = filter.getNumReleaseRecords();
			else if (currentSearchType == 4)
				numRecords = filter.getNumSongRecords();			
			else if (currentSearchType == 5)
				numRecords = filter.getNumExternalArtistRecords();			
			else if (currentSearchType == 6)
				numRecords = filter.getNumExternalLabelRecords();			
			else if (currentSearchType == 7)
				numRecords = filter.getNumExternalReleaseRecords();			
			else if (currentSearchType == 8)
				numRecords = filter.getNumExternalSongRecords();			
			if (numRecords > 0)
				return false;
			if (!areAllChildrenZeroes(filter))
				return false;
		}
		return true;
	}
	
	/**
	 * This method returns true if the passed record or any of its children matches the current search filter...
	 */
	protected boolean checkChildrenRecursively(HierarchicalRecord record) {
		if (record == null)
			return false;		
		if (getSearchParameters().matches(record) > 0.0f)
			return true;
		for (HierarchicalRecord childStyle : record.getChildRecords()) {
			if (checkChildrenRecursively((HierarchicalRecord)childStyle))
				return true;
		}
		return false;		
	}
	
	/**
	 * This method returns true if the passed record or any of its children matches the current search filter...
	 */
	protected boolean checkParentsRecursively(HierarchicalRecord record) {
		if (record == null)
			return false;
		if (getSearchParameters().matches(record) > 0.0f)
			return true;
		for (HierarchicalRecord parentStyle : record.getParentRecords()) {
			if (checkParentsRecursively((HierarchicalRecord)parentStyle))
				return true;
		}
		return false;		
	}
	
	/**
	 * Lists the accepted mime types here...
	 */
	public List<String> mimeTypes() {
		List<String> result = new ArrayList<String>();
		result.add(DragDropUtil.MIME_TYPE_FILENAME_LIST);
		result.add(DragDropUtil.MIME_TYPE_ARTIST_IDENTIFIER_LIST);
		result.add(DragDropUtil.MIME_TYPE_LABEL_IDENTIFIER_LIST);
		result.add(DragDropUtil.MIME_TYPE_RELEASE_IDENTIFIER_LIST);
		result.add(DragDropUtil.MIME_TYPE_SONG_IDENTIFIER_LIST);
		for (String filterMimeType : getFilterModelManager().getFilterMimeType())
			result.add(filterMimeType);
		return result;
	}	
	
	public QMimeData mimeData(List<QModelIndex> indexes) {		
		return DragDropUtil.getMimeDataForFilterInstances(treeView.getCurrentSelectedInstances());
	}

	protected void modelRefreshCallout() {
		widget.updateFilter();
	}
	
}

