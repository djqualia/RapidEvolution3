package com.mixshare.rapid_evolution.ui.model.table;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.ItemDataRole;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QStandardItemModel;

public class DetailsTableItemModel extends QStandardItemModel {

    static private Logger log = Logger.getLogger(DetailsTableItemModel.class);    	
		
    ////////////
    // FIELDS //
    ////////////
    
	private Map<Integer, String> verticalHeaderLabels = new HashMap<Integer, String>();
	private Map<Integer, String> verticalHeaderDescriptions = new HashMap<Integer, String>();
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DetailsTableItemModel(int numRows, int numColumns) {
		super(numRows, numColumns);
	}
	
	/////////////
	// GETTERS //
	/////////////

	public Object headerData(int section, Orientation orientation, int role) {
		if (orientation == Orientation.Vertical) {
			if (role == ItemDataRole.DisplayRole) {
            	if (section >= 0)
            		return verticalHeaderLabels.get(section);
				
			} else if (role == ItemDataRole.ToolTipRole) {
            	if (section >= 0)
            		return verticalHeaderDescriptions.get(section);				
			}
		}
        return null;
	}		
	
	public Qt.ItemFlags flags(QModelIndex index) {
    	Qt.ItemFlags result = super.flags(index); 
    	if (index.column() != 0)
    		result.clear(Qt.ItemFlag.ItemIsEditable);
    	return result;
    }	
	    
	/////////////
	// SETTERS //
	/////////////	
	
	public void setVerticalHeaderLabel(int row, String label, String description) {
		verticalHeaderLabels.put(row, label);
		verticalHeaderDescriptions.put(row, description);		
	}
	        
}
