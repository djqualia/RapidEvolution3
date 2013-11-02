package com.mixshare.rapid_evolution.ui.model.tree;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt.ItemDataRole;
import com.trolltech.qt.gui.QStandardItem;

public class TreeSortFilterProxyModel extends SortFilterProxyModel {

    static private Logger log = Logger.getLogger(TreeSortFilterProxyModel.class);    

    static public final long COLUMN_SORT_CLICK_INTERVAL = 1000;
    
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	    
	public TreeSortFilterProxyModel(QObject parent, TreeModelManager modelManager) {
		super(parent, modelManager);
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public TreeModelManager getTreeModelManager() { return (TreeModelManager)modelManager; }		
	
	/////////////
	// METHODS //
	/////////////
	
	protected boolean lessThan(QModelIndex sourceLeft, QModelIndex sourceRight) {
		QStandardItem parent = null;
		if (sourceLeft.parent() != null)
			parent = getTreeModelManager().getTreeItemModel().itemFromIndex(sourceLeft.parent());					
		boolean result = false;
		ColumnOrdering primaryColumnOrder = null;
		for (ColumnOrdering columnOrder : modelManager.getSortOrdering()) {
			int column = 0;
			if (primaryColumnOrder == null) {
				primaryColumnOrder = columnOrder;
				column = sourceLeft.column();
			} else {
				int c = 0;
				boolean found = false;
				while ((c < getTreeModelManager().getNumColumns()) && !found) {
					if (getTreeModelManager().getSourceColumnType(c).getColumnId() == columnOrder.getColumnId()) {
						column = c;
						found = true;
					} else { 
						++c;
					}
				}								
			}
			Object leftData = null;
			Object rightData = null;
			if (parent != null) {
				leftData = parent.child(sourceLeft.row(), column).data(ItemDataRole.DisplayRole);
				rightData = parent.child(sourceRight.row(), column).data(ItemDataRole.DisplayRole);								
			} else {
				leftData = modelManager.getSourceModel().data(sourceLeft.row(), column);
				rightData = modelManager.getSourceModel().data(sourceRight.row(), column);				
			}
			if ((leftData != null) && (rightData != null)) {
				if (leftData instanceof Comparable) {				
					Comparable leftComp = (Comparable)leftData;
					int cmp  = leftComp.compareTo(rightData);
					if (cmp != 0) {						
						if ((columnOrder.isAscending() && primaryColumnOrder.isAscending()) || (!columnOrder.isAscending() && !primaryColumnOrder.isAscending())) {
							result = (cmp < 0);
							break;
						} else {
							result = (cmp >= 0);
							break;
						}
					}					
				} else {
					int cmp = leftData.toString().compareToIgnoreCase(rightData.toString());
					if (cmp != 0) {
						if ((columnOrder.isAscending() && primaryColumnOrder.isAscending()) || (!columnOrder.isAscending() && !primaryColumnOrder.isAscending())) {
							result = (cmp < 0);
							break;
						} else {
							result = (cmp >= 0);
							break;
						}
					}
				}
			} else if (leftData != null) {					
				result = ((columnOrder.isAscending() && primaryColumnOrder.isAscending()) || (!columnOrder.isAscending() && !primaryColumnOrder.isAscending()));
				break;
			} else if (rightData != null) {
				result = ((!columnOrder.isAscending() && primaryColumnOrder.isAscending()) || (columnOrder.isAscending() && !primaryColumnOrder.isAscending()));
				break;
			}						
		}		
		if (log.isTraceEnabled())
			log.trace("lessThan(): leftInstance=" + (TreeHierarchyInstance)getTreeModelManager().getTreeItemModel().itemFromIndex(sourceLeft).data() + ", rightInstance=" + (TreeHierarchyInstance)getTreeModelManager().getTreeItemModel().itemFromIndex(sourceRight).data() + ", result=" + result);
		return result;	
	}

	protected void modelRefreshCallout() { }
	public boolean isLazySearchSupported() { return false; }
	
	////////////
	// EVENTS //
	////////////
	

}
