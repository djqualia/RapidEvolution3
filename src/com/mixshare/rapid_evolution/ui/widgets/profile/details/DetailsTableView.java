package com.mixshare.rapid_evolution.ui.widgets.profile.details;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.ui.dialogs.columns.SelectColumnsDialog;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.UserDataColumn;
import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QTableView;
import com.trolltech.qt.gui.QHeaderView.ResizeMode;

abstract public class DetailsTableView extends QTableView implements AllColumns {
	
	static private Logger log = Logger.getLogger(DetailsTableView.class);
	
	////////////
	// FIELDS //
	////////////
	
	private CommonDetailsModelManager modelManager;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DetailsTableView(CommonDetailsModelManager modelManager) {
		super();
		this.modelManager = modelManager;
		
		// setup default table behavior
        setItemDelegate(new ProfileDetailsItemDelegate(this, modelManager, modelManager.getRelativeProfile()));
        //setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers);
        setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectItems);
        setSelectionMode(SelectionMode.NoSelection);        
        setAlternatingRowColors(true);
        setSortingEnabled(false);
        setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);
        horizontalHeader().hide();
        horizontalHeader().setStretchLastSection(false);
        horizontalHeader().setResizeMode(ResizeMode.Fixed);        
        verticalHeader().setMovable(true);
        verticalHeader().setStretchLastSection(false);
                
        verticalHeader().setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu);
        verticalHeader().customContextMenuRequested.connect(this, "customContextMenuRequested(QPoint)");        
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract protected Index getIndex();
	
	/////////////
	// GETTERS //
	/////////////
    
    
	/////////////
	// METHODS //
	/////////////
	
	public void setupEventListeners() {
        verticalHeader().sectionResized.connect(modelManager, "columnResized(Integer,Integer,Integer)");
        verticalHeader().sectionMoved.connect(modelManager, "columnMoved(Integer,Integer,Integer)");
	}    
	
	public void setColumnWidths(int totalWidth) {
		int padding = 20;
		totalWidth -= verticalHeader().width();
		totalWidth -= padding;
		int c2 = 50;
		int c1 = totalWidth - c2;
		setColumnWidth(0, c1);
		setColumnWidth(1, c2);
	}

	
	public boolean event(QEvent event) {
		if (event instanceof QKeyEvent) {
			QKeyEvent ke = (QKeyEvent)event;
			if (ke.key() == Qt.Key.Key_Tab.value()) {
				if (ke.type() == QEvent.Type.KeyRelease) {
					if (log.isTraceEnabled())
						log.trace("event(): tab pressed");
					focusNextPrevChild(true);
				}				
				return true;
			} else if (ke.key() == Qt.Key.Key_Backtab.value()) {
				if (ke.type() == QEvent.Type.KeyRelease) {
					if (log.isTraceEnabled())
						log.trace("event(): back tab pressed");
					focusNextPrevChild(false);
				}
				return true;
			} else {
				return super.event(event);
			}
		} else {
			return super.event(event);
		}
	}
		
	protected boolean focusNextPrevChild(boolean next) {
		if (log.isTraceEnabled())
			log.trace("focusNextPrevChild(): next=" + next);
		int row = currentIndex().row();
		QModelIndex currentIndex = currentIndex();
		QModelIndex nextIndex = null;
		int nextRow = next ? currentIndex.row() + 1 : currentIndex.row() - 1;
		if (nextRow >= model().rowCount())
			nextRow = 0;
		if (nextRow < 0)
			nextRow = model().rowCount() - 1;
		if (log.isTraceEnabled())
			log.trace("focusNextPrevChild(): nextRow=" + nextRow);
		nextIndex = model().index(nextRow, 0);
		setCurrentIndex(nextIndex);
		return true;
		//return super.focusNextPrevChild(next);
	}
	
	public void setupPersistentEditors() {
    	for (int i = 0; i < modelManager.getNumColumns(); ++i) {
    		boolean setPersistent = false;
    		int persistentColumn = 0;
    		Column column = modelManager.getSourceColumnType(i);
    		if (column instanceof UserDataColumn) {
    			UserDataColumn userColumn = (UserDataColumn)column;
    			if (userColumn.getUserDataType().getFieldType() == UserDataType.TYPE_BOOLEAN_FLAG)
    				setPersistent = true;
    		} else {
    			if (column.getColumnId() == COLUMN_DISABLED.getColumnId())
    				setPersistent = true;
    			else if (column.getColumnId() == COLUMN_STYLE_CATEGORY_ONLY.getColumnId())
    				setPersistent = true;
    			else if (column.getColumnId() == COLUMN_TAG_CATEGORY_ONLY.getColumnId())
    				setPersistent = true;
    			else if (column.getColumnId() == COLUMN_RELEASE_IS_COMPILATION.getColumnId())
    				setPersistent = true;
    			else if ((column.getColumnId() == COLUMN_BPM_START.getColumnId()) || (column.getColumnId() == COLUMN_BPM_END.getColumnId())) { // for tap buttons
    				setPersistent = true;
    				persistentColumn = 1;
    			} else if ((column.getColumnId() == COLUMN_KEY_START.getColumnId()) || (column.getColumnId() == COLUMN_KEY_END.getColumnId())) {
    				//setPersistent = true;
    				//persistentColumn = 1;
    			} else if (column.getColumnId() == COLUMN_FILEPATH.getColumnId()) { // for browse button
    				setPersistent = true;
    				persistentColumn = 1;
    			} else if (column.getColumnId() == COLUMN_RATING_STARS.getColumnId() || (column.getColumnId() == COLUMN_RATING_STARS.getColumnId())) { // for clear button
    				setPersistent = true;
    				persistentColumn = 1;
    			} else if (column.getColumnId() == COLUMN_BEAT_INTENSITY_VALUE.getColumnId()) {
    				//setPersistent = true;
    				//persistentColumn = 1;
    			}
    		}
    		if (setPersistent) {
    			QModelIndex proxyIndex = modelManager.getProxyModel().mapFromSource(modelManager.getSourceModel().index(i, persistentColumn));
    			if (proxyIndex != null)
    				openPersistentEditor(proxyIndex);
    		}
    	}    		
	}
	
	////////////
	// EVENTS //
	////////////
	
	public void customContextMenuRequested(QPoint point) {
		if (log.isDebugEnabled())
			log.debug("customContextMenuRequested(): point=" + point);
    	SelectColumnsDialog selectColumnsDialog = new SelectColumnsDialog(this, modelManager);
    	if (selectColumnsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		selectColumnsDialog.saveSelections();
    		modelManager.setSourceColumnSizes(this);
    	}    	

	}
		
}
