package com.mixshare.rapid_evolution.ui.widgets.profile.filter;

import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.table.TableItemModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;

public class FilterTabTableItemModel extends TableItemModel implements AllColumns {

	public FilterTabTableItemModel(int numRows, int numColumns, QObject parent, TableModelManager modelManager) {
		super(numRows, numColumns, parent, modelManager);
	}
	
    /**
     * Must add drag/drop support...
     */
    public Qt.ItemFlags flags(QModelIndex index) {    	
    	Qt.ItemFlags result = super.flags(index);
    	if (index != null) {
    		Column column = modelManager.getSourceColumnType(index.column());
    		if (isEditableFilterTabColumn(column))
    			result.set(Qt.ItemFlag.ItemIsEditable);
    	}
    	return result;
    }
    
    static public boolean isEditableFilterTabColumn(Column column) {
    	if ((column.getColumnId() == COLUMN_DEGREE.getColumnId()))
    		return true;
    	return false;
    }	
}
