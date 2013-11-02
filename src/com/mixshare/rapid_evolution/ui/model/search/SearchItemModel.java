package com.mixshare.rapid_evolution.ui.model.search;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.table.TableItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;

public class SearchItemModel extends TableItemModel implements AllColumns {

    static private Logger log = Logger.getLogger(SearchItemModel.class);    
    static private final long serialVersionUID = 0L;    	
	
	public SearchItemModel(int numRows, int numColumns, QObject parent, SearchModelManager modelManager) {
		super(numRows, numColumns, parent, modelManager);
	}
	
	public SearchModelManager getSearchModelManager() { return (SearchModelManager)modelManager; }
	
    /**
     * Must add drag/drop support...
     */
    public Qt.ItemFlags flags(QModelIndex index) {    	
    	Qt.ItemFlags result = super.flags(index);
    	if (index != null) {
    		Column column = getSearchModelManager().getSourceColumnType(index.column());
    		if (isEditableSearchColumn(column))
    			result.set(Qt.ItemFlag.ItemIsEditable);
    	}
    	return result;
    }
    
    static public boolean isEditableSearchColumn(Column column) {
    	if ((column.getColumnId() == COLUMN_RATING_STARS.getColumnId()))
    		return true;
    	return false;
    }
	
}
