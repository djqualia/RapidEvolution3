package com.mixshare.rapid_evolution.ui.model.profile.filter.tag;

import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.search.parameters.filter.FilterSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.filter.tag.TagSearchParameters;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.profile.tab.TabTreeProxyModel;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.tree.TreeModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeWidget;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;

public class TagTabTreeProxyModel extends TabTreeProxyModel {

	static protected byte showType;
	
	static {
		if (UIProperties.hasProperty("tag_tab_show_type"))
			showType = UIProperties.getByte("tag_tab_show_type");
	}
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public TagTabTreeProxyModel(QObject parent, TreeModelManager modelManager, TabTreeView treeView) {
		super(parent, modelManager, treeView);
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public byte getShowType() { return showType; }
	
	public FilterSearchParameters getNewFilterSearchParameters() { return new TagSearchParameters(); }
	
	/////////////
	// SETTERS //
	/////////////
		
	public void setShowType(byte type) {
		showType = type;
		UIProperties.setProperty("tag_tab_show_type", String.valueOf(type));
		invalidate();
	}
	
	/////////////
	// METHODS //
	/////////////			
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		if (searchProfile == null)
			return false;
		QStandardItemModel thisModel = (QStandardItemModel)modelManager.getSourceModel();
		QStandardItem parentItem = thisModel.itemFromIndex(sourceParent);
		HierarchicalRecord record = null;
		if (parentItem != null) {
			QStandardItem thisItem = parentItem.child(sourceRow, 0);			
			TreeHierarchyInstance tagInstance = (TreeHierarchyInstance)thisItem.data();
			record = (HierarchicalRecord)tagInstance.getRecord();
		} else {
			// root tag
			QStandardItem item = thisModel.item(sourceRow);
			if (item != null) {
				TreeHierarchyInstance tagInstance = (TreeHierarchyInstance)item.data();
				record = (HierarchicalRecord)tagInstance.getRecord();
			} else {
				return false;
			}
		}
		if (record.isDisabled())
			return false;
		if (TabTreeWidget.SHOW_SELECTED == showType) {
			if (!checkChildrenDegreesRecursively(record))
				return false;			
		}
		return checkChildrenRecursively(record) || checkParentsRecursively(record);
	}
	
	/**
	 * This method returns true if the passed record or any of its children matches the current search filter...
	 */
	protected boolean checkChildrenDegreesRecursively(HierarchicalRecord record) {
		if (searchProfile != null) {
			if (record != null) {
				if (searchProfile.getSourceTagDegreeFromUniqueId(record.getUniqueId()) > 0.0f)
					return true;
				for (HierarchicalRecord childTag : record.getChildRecords()) {
					if (checkChildrenDegreesRecursively((HierarchicalRecord)childTag))
						return true;
				}
			}
		}
		return false;		
	}	
	
}
