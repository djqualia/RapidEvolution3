package com.mixshare.rapid_evolution.ui.updaters.model.table;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.table.DetailsTableItemModel;

public class DetailsTableModelColumnUpdater extends Thread {

    static private Logger log = Logger.getLogger(DetailsTableModelColumnUpdater.class);    
	
    static public final byte ACTION_ADD = 0;
    
	private DetailsTableItemModel model;
	private byte action;
	
	public DetailsTableModelColumnUpdater(DetailsTableItemModel model, byte action) {
		this.model = model;
		this.action = action;
	}
	
	public void run() {
		try {
			if (model != null) {
				if (action == ACTION_ADD)
					model.insertRow(model.rowCount());
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}

}
