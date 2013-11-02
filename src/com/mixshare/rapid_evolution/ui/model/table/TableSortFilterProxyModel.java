package com.mixshare.rapid_evolution.ui.model.table;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;

public class TableSortFilterProxyModel extends SortFilterProxyModel {

    static private Logger log = Logger.getLogger(TableSortFilterProxyModel.class);    

    static public final long COLUMN_SORT_CLICK_INTERVAL = 1000;
        
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
    public TableSortFilterProxyModel() { }
	public TableSortFilterProxyModel(QObject parent, TableModelManager modelManager) {
		super(parent, modelManager);
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public RecordTableModelManager getTableModelManager() { return (RecordTableModelManager)modelManager; }		
	
	/////////////
	// METHODS //
	/////////////
	
	protected Object getDataFromModel(Record record, Column column) {
		int row = getTableModelManager().getRowForUniqueId(record.getUniqueId());
		for (int c = 0; c < getTableModelManager().getNumColumns(); ++c)
			if (getTableModelManager().getSourceColumnType(c).equals(column))
				return getTableModelManager().getSourceModel().data(row, c);
		return null;
	}
	
	protected boolean lessThan(QModelIndex sourceLeft, QModelIndex sourceRight) {
		return lessThan(sourceLeft, sourceRight, modelManager.isExtendedSortEnabled());
	}
	protected boolean lessThan(QModelIndex sourceLeft, QModelIndex sourceRight, boolean compareSubColumns) {
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
				while ((c < getTableModelManager().getNumColumns()) && !found) {
					if (getTableModelManager().getSourceColumnType(c).getColumnId() == columnOrder.getColumnId()) {
						column = c;
						found = true;
					} else { 
						++c;
					}
				}				
			}				
			//Object leftData = modelManager.getSourceData(columnOrder.getColumn(), leftRecord);
			//Object rightData = modelManager.getSourceData(columnOrder.getColumn(), rightRecord);
			Object leftData = getTableModelManager().getTableItemModel().getDisplayData(sourceLeft.row(), column);
			Object rightData = getTableModelManager().getTableItemModel().getDisplayData(sourceRight.row(), column);
			if (log.isTraceEnabled())
				log.trace("lessThan(): leftData=" + leftData + ", rightData=" + rightData);					
			if ((leftData != null) && (rightData != null)) {
				if (leftData instanceof Comparable) {				
					Comparable leftComp = (Comparable)leftData;
					int cmp  = leftComp.compareTo(rightData);
					if (log.isTraceEnabled())
						log.trace("lessThan(): cmp=" + cmp + ", columnOrder.isAscending()=" + columnOrder.isAscending() + ", primaryColumnOrder.isAscending()=" + primaryColumnOrder.isAscending());					
					if (cmp != 0) {						
						if ((columnOrder.isAscending() && primaryColumnOrder.isAscending()) || (!columnOrder.isAscending() && !primaryColumnOrder.isAscending())) {
							result = (cmp < 0);
							if (log.isTraceEnabled())
								log.trace("lessThan(): result1=" + result);					
							break;
						} else {
							result = (cmp >= 0);
							if (log.isTraceEnabled())
								log.trace("lessThan(): result2=" + result);					
							break;
						}
					}	
					if (!compareSubColumns)
						break;
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
					if (!compareSubColumns)
						break;
				}
			} else if (leftData != null) {					
				result = ((columnOrder.isAscending() && primaryColumnOrder.isAscending()) || (!columnOrder.isAscending() && !primaryColumnOrder.isAscending()));
				break;
			} else if (rightData != null) {
				result = ((!columnOrder.isAscending() && primaryColumnOrder.isAscending()) || (columnOrder.isAscending() && !primaryColumnOrder.isAscending()));
				break;
			} else if (!compareSubColumns) {				
				break;
			}
		}		
		if (log.isTraceEnabled())
			log.trace("lessThan(): leftRecord=" + getTableModelManager().getRecordForRow(sourceLeft.row()) + ", rightRecord=" + getTableModelManager().getRecordForRow(sourceRight.row()) + ", result=" + result);		
		return result;			
	}
	
	protected void modelRefreshCallout() { }
	
	public boolean isLazySearchSupported() { return false; }
	
}
