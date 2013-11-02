package com.mixshare.rapid_evolution.ui.updaters.model.table;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.table.TableItemModel;

public class TableModelColumnUpdater extends Thread {

    static private Logger log = Logger.getLogger(TableModelColumnUpdater.class);    
	
    static public final byte ACTION_ADD = 0;
    static public final byte ACTION_REMOVE = 1;
    
	private TableItemModel model;
	private int column;
	private byte action;
	
	public TableModelColumnUpdater(TableItemModel model, int column, byte action) {
		this.model = model;
		this.column = column;
		this.action = action;
	}
	
	public void run() {
		try {
			if (model != null) {
				if (action == ACTION_ADD)
					model.insertColumn(model.columnCount()); //model.appendColumn(null);
				else if (action == ACTION_REMOVE)
					model.removeColumn(column);
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}

}
