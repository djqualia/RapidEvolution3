package com.mixshare.rapid_evolution.ui.model.table;

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.util.ThumbnailImageFactory;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.ItemDataRole;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QAbstractTableModel;

/**
 * This custom source model provides a getDisplayData method quick seems to allow
 * quicker access to the data (during sort procedures for example), than the QStandardItem's 
 * data() methods...
 */
public class TableItemModel extends QAbstractTableModel implements Serializable {

    static private Logger log = Logger.getLogger(TableItemModel.class);    	
    static private final long serialVersionUID = 0L;    	
		
    ////////////
    // FIELDS //
    ////////////
    
	protected TableModelManager modelManager;
	
	private int numRows = 0;
	private int numColumns = 0;
	private Vector<Vector<Object>> data;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public TableItemModel() { }
	
	public TableItemModel(int numRows, int numColumns, QObject parent, TableModelManager modelManager) {
		super(parent);
		this.modelManager = modelManager;
		this.numRows = numRows;
		this.numColumns = numColumns;
		init();
	}
	
	private void init() {
		data = new Vector<Vector<Object>>(numRows);
		for (int r = 0; r < numRows; ++r) {
			Vector<Object> row = new Vector<Object>(numColumns);
			for (int c = 0; c < numColumns; ++c)
				row.add(null);
			data.add(row);
		}
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	/**
	 * Returns the number of columns for the children of the given parent.
	 * 
	 * Tip: When implementing a table based model, columnCount() should return 0 when the parent is valid. 
	 */
	public int columnCount(QModelIndex parent) { return numColumns; }	

	/**
	 * Returns the number of rows under the given parent. When the parent is valid it means that rowCount is returning the number of children of parent.
	 * 
	 * Tip: When implementing a table based model, rowCount() should return 0 when the parent is valid. 
	 */
	public int rowCount(QModelIndex parent) {
		return numRows;
	}

	public Object headerData(int section, Orientation orientation, int role) {
		if (orientation == Orientation.Horizontal) {
            if (role == ItemDataRole.DisplayRole) {
            	if (section >= 0)
            		return modelManager.getSourceColumnTitle(section);
            } else if (role == ItemDataRole.ToolTipRole) {
            	if (section >= 0)
            		return modelManager.getSourceColumnType(section).getColumnDescription();            	
            }
        }		
        return null;
	}
		
    public Object data(QModelIndex index, int role) {
    	// NOTE: This method is called repeatedly upon viewing, not just once to populate the model...
    	if (index != null) {
	        int row = index.row();
	        int col = index.column();
	        if (role == Qt.ItemDataRole.DisplayRole) {
	        	if (modelManager.isSourceColumnTypeImage(col))
	        		return null;
	        	return data.get(row).get(col);
	        } else if (role == Qt.ItemDataRole.DecorationRole) {
	        	if (modelManager.isSourceColumnTypeImage(col))
	        		return modelManager.getSourceColumnData(col, modelManager.getObjectForRow(row));
	        } else if (role == Qt.ItemDataRole.SizeHintRole) {
	        	if (modelManager.isSourceColumnTypeImage(col))
	        		return ThumbnailImageFactory.THUMBNAIL_SIZE;
	        }
    	}
    	return null;
    }
    
    /**
     * This method seems to allow much faster access than data(QModelIndex), perhaps the creation of the QModelIndex in the underlying model is
     * what slows everything down...
     */
    public Object getDisplayData(int row, int col) {
    	return data.get(row).get(col);
    }
	
    /**
     * Must add drag/drop support...
     */
    public Qt.ItemFlags flags(QModelIndex index) {
    	Qt.ItemFlags result = super.flags(index);    	
    	result.set(Qt.ItemFlag.ItemIsDragEnabled);
    	result.set(Qt.ItemFlag.ItemIsDropEnabled);    	
    	return result;
    }

	public TableModelManager getModelManager() {
		return modelManager;
	}
	public int getNumRows() {
		return numRows;
	}
	public int getNumColumns() {
		return numColumns;
	}
	public Vector<Vector<Object>> getData() {
		return data;
	}
    
	/////////////
	// SETTERS //
	/////////////
	
	public boolean setData(QModelIndex index, Object value, int role) {
		if (log.isTraceEnabled())
			log.trace("setData(): row=" + index.row() + ", column=" + index.column() + ", value=" + value);
		data.get(index.row()).set(index.column(), value);
		dataChanged.emit(index, index);
		return true;
	}

	/**
	 * This is a more direct method that avoids using the QModelIndex and index() methods...
	 */
	public boolean setDisplayData(int row, int column, Object value, boolean fireDataChanged) {
		if (log.isTraceEnabled())
			log.trace("setData(): row=" + row + ", column=" + column + ", value=" + value);
		if (row < data.size()) {
			data.get(row).set(column, value);
			QModelIndex index = index(row, column);
			if (fireDataChanged)
				dataChanged.emit(index, index);
			return true;
		}
		return false;		
	}
	
	public boolean setDisplayData(int row, int column, Object value) {
		return setDisplayData(row, column, value, true);
	}
	
	public void emitColumnDataChanged(int column) {
		QModelIndex index1 = index(0, column);
		QModelIndex index2 = index(rowCount() - 1, column);
		dataChanged.emit(index1, index2);
	}

	public void setModelManager(TableModelManager modelManager) {
		this.modelManager = modelManager;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}


	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

	public void setData(Vector<Vector<Object>> data) {
		this.data = data;
	}    
	
	/////////////
	// METHODS //
	/////////////
	
    public void resetData() {
    	init();
    }
    
    public boolean insertRows(int row, int count, QModelIndex parent) {
    	boolean success = false;
    	beginInsertRows(parent, row, row + count - 1);
    	for (int i = 0; i < count; ++i) {
			Vector<Object> rowVector = new Vector<Object>(numColumns);
			for (int c = 0; c < numColumns; ++c)
				rowVector.add(null);
    		data.insertElementAt(rowVector, row);
    	}
    	numRows += count;
    	endInsertRows();    	
    	return success;
    }
    
    public boolean removeRows(int row, int count, QModelIndex parent) {
    	boolean success = false;
    	beginRemoveRows(parent, row, row + count - 1);
    	for (int i = 0; i < count; ++i)
    		data.removeElementAt(row);
    	numRows -= count;
    	endRemoveRows();
    	return success;
    }

    public boolean insertColumns(int column, int count, QModelIndex parent) {
    	boolean success = false;
    	beginInsertColumns(parent, column, column + count - 1);
    	for (int r = 0; r < numRows; ++r) {
        	for (int i = 0; i < count; ++i)
        		data.get(r).insertElementAt(null, column);
    	}
    	numColumns += count;
    	endInsertColumns();
    	return success;
    }

    public boolean removeColumns(int column, int count, QModelIndex parent) {
    	boolean success = false;
    	beginRemoveColumns(parent, column, column + count - 1);
    	for (int r = 0; r < numRows; ++r) {
        	for (int i = 0; i < count; ++i)
        		data.get(r).removeElementAt(column);
    	}    	
    	numColumns -= count;
    	endRemoveColumns();
    	return success;
    }
        
}
