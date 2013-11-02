package com.mixshare.rapid_evolution.ui.model.profile.search.song;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.table.TableItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;

public class SongMixoutTableItemModel extends TableItemModel implements AllColumns {

    static private Logger log = Logger.getLogger(SongMixoutTableItemModel.class);    
    static private final long serialVersionUID = 0L;    	
	
	public SongMixoutTableItemModel(int numRows, int numColumns, QObject parent, SongMixoutsModelManager modelManager) {
		super(numRows, numColumns, parent, modelManager);
	}
	
	public SongMixoutsModelManager getMixoutModelManager() { return (SongMixoutsModelManager)modelManager; }
	
    /**
     * Must add drag/drop support...
     */
    public Qt.ItemFlags flags(QModelIndex index) {    	
    	Qt.ItemFlags result = super.flags(index);
    	if (index != null) {
    		Column column = getMixoutModelManager().getSourceColumnType(index.column());
    		if (isEditableMixoutColumn(column))
    			result.set(Qt.ItemFlag.ItemIsEditable);
    	}
    	return result;
    }
    
    static public boolean isEditableMixoutColumn(Column column) {
    	if ((column.getColumnId() == COLUMN_MIXOUT_RATING_STARS.getColumnId()) ||
    			(column.getColumnId() == COLUMN_MIXOUT_BPM_DIFF.getColumnId()) ||
    			(column.getColumnId() == COLUMN_MIXOUT_TYPE.getColumnId()) ||
    			(column.getColumnId() == COLUMN_MIXOUT_COMMENTS.getColumnId()) ||
    			(column.getColumnId() == COLUMN_MIXOUT_RATING_VALUE.getColumnId()))
    		return true;
    	return false;
    }
	
}
