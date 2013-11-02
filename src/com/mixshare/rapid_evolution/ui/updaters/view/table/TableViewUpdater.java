package com.mixshare.rapid_evolution.ui.updaters.view.table;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.widgets.common.table.CommonTableView;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QWidget;

public class TableViewUpdater extends Thread {

	static private Logger log = Logger.getLogger(TableViewUpdater.class);
	    
	private CommonTableView commonTableView;

	public TableViewUpdater(CommonTableView commonTableView) {
		this.commonTableView = commonTableView;
	}
	
	public void run() {
		try {
			commonTableView.update();
			for (QObject w : commonTableView.findChildren(QWidget.class)) {
				((QWidget)w).update();
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}	
	
}
