package com.mixshare.rapid_evolution.ui.model.profile.tab;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.search.parameters.filter.FilterSearchParameters;
import com.mixshare.rapid_evolution.ui.model.tree.TreeModelManager;
import com.mixshare.rapid_evolution.ui.model.tree.TreeSortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeView;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

abstract public class TabTreeProxyModel extends TreeSortFilterProxyModel {

	static private Logger log = Logger.getLogger(TabTreeProxyModel.class);
		
	////////////
	// FIELDS //
	////////////
		
	protected TabTreeView treeView = null;
	protected SearchProfile searchProfile = null;
	protected String searchText = null;
			
	private transient FilterSearchParameters searchParams = null;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public TabTreeProxyModel(QObject parent, TreeModelManager modelManager, TabTreeView treeView) {
		super(parent, modelManager);	
		this.treeView = treeView;
		setDynamicSortFilter(false);
		rowsInserted.connect(this, "rowsInserted(QModelIndex,Integer,Integer)");
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public TreeModelManager getTreeModelManager() { return (TreeModelManager)modelManager; }
	
	public FilterSearchParameters getSearchParameters() {
		if (searchParams == null) {
			searchParams = getNewFilterSearchParameters();
			searchParams.setSearchText(searchText);
		}
		return searchParams;
	}
	
	/////////////
	// SETTERS //
	/////////////

	public void setSearchProfile(SearchProfile searchProfile) {
		this.searchProfile = searchProfile;
	}
	
	public void setSearchText(String text) {
		searchText = text;
		searchParams = null;
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
		
	abstract public byte getShowType();
	abstract public void setShowType(byte type);
	
	abstract public FilterSearchParameters getNewFilterSearchParameters();
	
	/////////////
	// METHODS //
	/////////////				
	
	/**
	 * This method returns true if the passed record or any of its children matches the current search filter...
	 */
	protected boolean checkChildrenRecursively(HierarchicalRecord record) {
		if ((searchText == null) || (searchText.length() == 0))
			return true;
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
		if ((searchText == null) || (searchText.length() == 0))
			return true;
		if (getSearchParameters().matches(record) > 0.0f)
			return true;
		for (HierarchicalRecord parentStyle : record.getParentRecords()) {
			if (checkParentsRecursively((HierarchicalRecord)parentStyle))
				return true;
		}
		return false;		
	}	
	
	public List<String> mimeTypes() {
		List<String> result = new ArrayList<String>();
		result.add(DragDropUtil.MIME_TYPE_FILENAME_LIST);
		return result;		
	}
	
	/**
	 * Lists the accepted mime types here...
	 */	
	public QMimeData mimeData(List<QModelIndex> indexes) {		
		return DragDropUtil.getMimeDataForFilterInstances(treeView.getCurrentSelectedInstances());
	}
	
	public void rowsInserted(QModelIndex p1, Integer p2, Integer p3) {
		treeView.setupPersistentEditors();
	}
	
}
