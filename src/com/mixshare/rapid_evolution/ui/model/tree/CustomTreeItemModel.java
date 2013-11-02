package com.mixshare.rapid_evolution.ui.model.tree;

import org.apache.log4j.Logger;

import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt.ItemDataRole;
import com.trolltech.qt.core.Qt.ItemFlags;
import com.trolltech.qt.core.Qt.Orientation;

public class CustomTreeItemModel extends QAbstractItemModel {

    static private Logger log = Logger.getLogger(CustomTreeItemModel.class);    
    static private final long serialVersionUID = 0L;    		
	
    ////////////
    // FIELDS //
    ////////////
    
    protected TreeModelManager modelManager;

    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public CustomTreeItemModel(TreeModelManager modelManager) {
		this.modelManager = modelManager;		
	}
	
	/////////////
	// METHODS //
	/////////////
	
	/**
	 * Returns the index of the item in the model specified by the given row, column and parent index.
	 * 
	 * When reimplementing this function in a subclass, call createIndex to generate model indexes that other components can use to refer to items in your model.
	 */
	public QModelIndex index(int row, int column, QModelIndex parent) {
		this.createIndex(row, column, 5);
		return null;	
	}

	/**
	 * Returns the parent of the model item with the given child, or QModelIndex() if it has no parent.
	 * 
	 * A common convention used in models that expose tree data structures is that only items in the first column have children.
	 * When reimplementing this function in a subclass that provides a tree model, you should return a model index corresponding
	 * to an item in the first column by calling createIndex with a value of 0 for the column number.
	 */
	public QModelIndex parent(QModelIndex child) {
		return null;
	}
	
	public int rowCount(QModelIndex parent) {
		return 0;
	}
	
	public int columnCount(QModelIndex parent) {
		return 0;
	}
	
	public Object data(QModelIndex index, int role) {
		return null;
	}
	
	/**
	 * Sets the role data for the item at index to value. Returns true if successful; otherwise returns false.
	 * 
	 * The dataChanged signal should be emitted if the data was successfully set.
	 * 
	 * The base class implementation returns false. This function and data must be reimplemented for editable models.
	 * Note that the dataChanged signal must be emitted explicitly when reimplementing this function.
	 */
	public boolean setData(QModelIndex index, Object value, int role) {
		return false;
	}
	
	public ItemFlags flags(QModelIndex index) {
		return super.flags(index);
	}
	
	public Object headerData(int section, Orientation orientation, int role) {
		// NOTE: It was necessary to override this method to get new column titles to show up 
		// after calling appendColumn on the model (perhaps there's a better way?)
		if (orientation == Orientation.Horizontal) {
            if (role == ItemDataRole.DisplayRole) {
            	if (section != -1)
            		return modelManager.getSourceColumnTitle(section);
            }
        }
        return super.headerData(section, orientation, role);
	}	    
	
    public void resetData() {
    	//clear();
    }
	
    /**
     * On models that support this, inserts count rows into the model before the given row. The items in the new row will be children of the item represented by the parent model index.
     * 
     * If row is 0, the rows are prepended to any existing rows in the parent. If row is rowCount, the rows are appended to any existing rows in the parent. If parent has no children, a single column with count rows is inserted.
     * 
     * Returns true if the rows were successfully inserted; otherwise returns false.
     * 
     * The base class implementation does nothing and returns false.
     * 
     * If you implement your own model, you can reimplement this function if you want to support insertions. Alternatively, you can provide you own API for altering the data.
     */
    public boolean insertRows(int row, int count, QModelIndex parent) {
    	beginInsertRows(parent, row, row + count - 1);
    	endInsertRows();
    	return false;
    }

    /**
     * On models that support this, removes count rows starting with the given row under parent parent from the model. Returns true if the rows were successfully removed; otherwise returns false.
     * 
     * The base class implementation does nothing and returns false.
     * 
     * If you implement your own model, you can reimplement this function if you want to support removing. Alternatively, you can provide you own API for altering the data.
     */
    public boolean removeRows(int row, int count, QModelIndex parent) {
    	beginRemoveRows(parent, row, row + count - 1);
    	endRemoveRows();
    	return false;
    }
    
}
