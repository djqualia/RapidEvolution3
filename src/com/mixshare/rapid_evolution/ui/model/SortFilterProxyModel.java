package com.mixshare.rapid_evolution.ui.model;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.gui.QSortFilterProxyModel;

/**
 * The base class for sort filter proxy models in RE3.  Establishes some default behavior
 * and common functionality.
 */
abstract public class SortFilterProxyModel extends QSortFilterProxyModel implements DataConstants {

    static private Logger log = Logger.getLogger(SortFilterProxyModel.class);    
	
    static public boolean EMPTY_INITIAL_RESULTS_MODE = RE3Properties.getBoolean("lazy_search_mode");
    
    ////////////
    // FIELDS //
    ////////////
    
	protected ModelManagerInterface modelManager = null;
	
	private boolean updateSort = true;
	private boolean manualSort = false;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SortFilterProxyModel() { }
	public SortFilterProxyModel(QObject parent, ModelManagerInterface modelManager) {
		super(parent);
		this.modelManager = modelManager;
		setSourceModel(modelManager.getSourceModel());
		setDynamicSortFilter(true);			
		modelManager.setProxyModel(this);
	}		
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract protected void modelRefreshCallout(); // gives a model a chance to re-search when sorted (for lazy mode)
	abstract public boolean isLazySearchSupported();
	
	/////////////
	// GETTERS //
	/////////////
	
	public ModelManagerInterface getModelManager() {
		return modelManager;
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setModelManager(ModelManagerInterface modelManager) {
		this.modelManager = modelManager;
	}	
	
	public void setUpdateSort(boolean updateSort) {
		this.updateSort = updateSort;
	}
	public void setManualSort(boolean manualSort) {
		this.manualSort = manualSort;
	}
	
	/////////////
	// METHODS //
	/////////////

	public void sort() {
		if (!(EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported()))
			super.sort(0, SortOrder.AscendingOrder);
	}
	
	public void sort(int column, SortOrder order) {
		if (!updateSort)
			return;
		if (log.isDebugEnabled())
			log.debug("sort(): column=" + column + ", order=" + order);
		Column sortedColumn = null;
		int c = 0;
		int index = 0;
		while ((c < modelManager.getNumColumns()) && (sortedColumn == null)) {
			//if (!modelManager.getSourceColumnType(c).isHidden()) {
				if (index == column)
					sortedColumn = modelManager.getSourceColumnType(c);
				++index;
			//}
			++c;
		}
		if ((sortedColumn != null) && !manualSort) {
			if (log.isDebugEnabled())
				log.debug("sort(): setting new primary sort column=" + sortedColumn + " for type=" + modelManager.getTypeDescription());
			modelManager.setPrimarySortColumn(sortedColumn.getColumnId());
		}
		if (log.isDebugEnabled())
			log.debug("sort(): sortOrdering=" + modelManager.getSortOrdering());
		long timeBefore = System.currentTimeMillis();		
		modelRefreshCallout();
		if (!(EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported()))
			super.sort(column, order);
		if (log.isDebugEnabled())
			log.debug("sort(): time=" + (System.currentTimeMillis() - timeBefore) + "ms");
		manualSort = false;
	}
	
	public void applyFilter() {
		if (log.isTraceEnabled())
			log.trace("applyFilter(): called");
		// NOTE: there's also an "invalidateFilter" method but it did not seem to cause any 
		// performance advantages at the time, could be tested more thoroughly...
		invalidate();	
	}
	
	protected boolean filterAcceptsColumn(int sourceColumn, QModelIndex sourceParent) {
		//Column column = modelManager.getSourceColumnType(sourceColumn);
		//if (log.isTraceEnabled())
			//log.trace("filterAcceptsColumn(): sourceColumn=" + sourceColumn + ", sourceParent=" + sourceParent + ", column=" + column + ", visible=" + (!column.isHidden()));
		//return !column.isHidden();
		return true;
	}

	protected boolean lessThan(Record leftRecord, Record rightRecord, boolean compareSubColumns) {
		boolean result = false;
		ColumnOrdering primaryColumnOrder = null;
		for (ColumnOrdering columnOrder : modelManager.getSortOrdering()) {
			if (primaryColumnOrder == null)
				primaryColumnOrder = columnOrder;			
			Object leftData = modelManager.getSourceData(columnOrder.getColumnId(), leftRecord);
			Object rightData = modelManager.getSourceData(columnOrder.getColumnId(), rightRecord);
			if ((leftData != null) && (rightData != null)) {
				if (leftData instanceof Comparable) {				
					Comparable leftComp = (Comparable)leftData;
					int cmp  = leftComp.compareTo(rightData);
					if (cmp != 0) {						
						if (columnOrder.isAscending() == primaryColumnOrder.isAscending()) {
							result = (cmp < 0);
							break;
						} else {
							result = (cmp >= 0);
							break;
						}
					}
					if (!compareSubColumns)
						break;					
				} else {
					int cmp = leftData.toString().compareToIgnoreCase(rightData.toString());
					if (cmp != 0) {
						if (columnOrder.isAscending() == primaryColumnOrder.isAscending()) {
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
				result = (columnOrder.isAscending() == primaryColumnOrder.isAscending());
				break;
			} else if (rightData != null) {
				result = (columnOrder.isAscending() != primaryColumnOrder.isAscending());
				break;
			} else if (!compareSubColumns) {				
				break;
			}						
		}		
		if (log.isTraceEnabled())
			log.trace("lessThan(): leftRecord=" + leftRecord + ", rightRecord=" + rightRecord + ", result=" + result);		
		return result;		
	}
	
}
