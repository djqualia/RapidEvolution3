package com.mixshare.rapid_evolution.ui.widgets.common.table;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.dialogs.columns.SelectColumnsDialog;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.ItemDelegate;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHeaderView.ResizeMode;

abstract public class CommonTableView extends SortTableView {
	
	static private Logger log = Logger.getLogger(CommonTableView.class);
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public CommonTableView(ModelManagerInterface modelManager) {
		super(modelManager);				
		
		// setup default table behavior
        setItemDelegate(new ItemDelegate(this, modelManager));
        setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers);
        setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectRows);
        setSelectionMode(SelectionMode.ExtendedSelection);        
        setAlternatingRowColors(true);        
        setSortingEnabled(true);        
        setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);
        verticalHeader().setResizeMode(ResizeMode.Fixed);
        verticalHeader().hide(); 
        horizontalHeader().setMovable(true);
        
        horizontalHeader().setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu);
        horizontalHeader().customContextMenuRequested.connect(this, "customContextMenuRequested(QPoint)");
	}
		
	/////////////
	// GETTERS //
	/////////////
    
	public TableModelManager getTableModelManager() { return (TableModelManager)modelManager; }
	    
	/////////////
	// METHODS //
	/////////////
	
	public void setupEventListeners() {
        horizontalHeader().sectionResized.connect(modelManager, "columnResized(Integer,Integer,Integer)");
        horizontalHeader().sectionMoved.connect(modelManager, "columnMoved(Integer,Integer,Integer)");    
	}    		
	        
	////////////
	// EVENTS //
	////////////
	
	public void customContextMenuRequested(QPoint point) {
		if (log.isDebugEnabled())
			log.debug("customContextMenuRequested(): point=" + point);
    	SelectColumnsDialog selectColumnsDialog = new SelectColumnsDialog(this, getTableModelManager());
    	if (selectColumnsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		selectColumnsDialog.saveSelections();
    		getTableModelManager().setSourceColumnSizes(this);
    	}    	

	}
	
}
