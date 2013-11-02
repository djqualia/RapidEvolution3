package com.mixshare.rapid_evolution.ui.updaters.model.table;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;

public class TableModelRowUpdater extends Thread {

	static private Logger log = Logger.getLogger(TableModelRowUpdater.class);
	
    static public final byte ACTION_ADD = 0;
    static public final byte ACTION_UPDATE = 1;
    static public final byte ACTION_REMOVE = 2;
    
	private Record record;
	private RecordTableModelManager modelManager;
	private byte action;

	public TableModelRowUpdater(Record record, RecordTableModelManager modelManager, byte action) {
		this.record = record;
		this.modelManager = modelManager;
		this.action = action;
	}
	
	public void run() {
		try {
			if (action == ACTION_ADD)
				modelManager.addRow(record);
			else if (action == ACTION_UPDATE)
				modelManager.updateRow(record);
			else if (action == ACTION_REMOVE)
				modelManager.removeRow(record);			
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}	
	
}
