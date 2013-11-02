package com.mixshare.rapid_evolution.ui.dialogs.options;

import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QSortFilterProxyModel;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;

public class OptionsProxyModel extends QSortFilterProxyModel implements AllColumns {

	private OptionsModelManager modelManager;
	private String searchText = "";

	public OptionsProxyModel(OptionsModelManager modelManager) {
		this.modelManager = modelManager;		
	}
	
	public void setSearchText(String text) {
		searchText = text;		
	}

	protected boolean filterAcceptsColumn(int sourceColumn, QModelIndex sourceParent) {
		Column column = modelManager.getSourceColumnType(sourceColumn);
		if (column.getColumnId() == COLUMN_SETTING_ID.getColumnId())
			return false;
		if (column.getColumnId() == COLUMN_SETTING_TYPE.getColumnId())
			return false;
		return true;
	}
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		QStandardItemModel thisModel = (QStandardItemModel)sourceModel();
		QStandardItem parentItem = thisModel.itemFromIndex(sourceParent);
		QStandardItem item = null;
		if (parentItem != null) {
			item = parentItem.child(sourceRow, 0);			
		} else {
			// root style
			item = thisModel.item(sourceRow);
		}
		if (item == null)
			return false;
		return checkChildrenRecursively(item) || checkParentsRecursively(item);			
	}
	
	protected boolean checkChildrenRecursively(QStandardItem item) {
		if (item == null)
			return false;
		if (StringUtil.substring(searchText, item.text()))		
			return true;
		for (int i = 0; i < item.rowCount(); ++i)
			if (checkChildrenRecursively(item.child(i)))
				return true;		
		return false;
	}
	
	protected boolean checkParentsRecursively(QStandardItem item) {
		if (item == null)
			return false;
		if (StringUtil.substring(searchText, item.text()))		
			return true;
		if (checkParentsRecursively(item.parent()))
			return true;		
		return false;		
	}
	
}
