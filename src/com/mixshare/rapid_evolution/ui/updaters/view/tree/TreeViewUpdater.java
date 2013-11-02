package com.mixshare.rapid_evolution.ui.updaters.view.tree;

import org.apache.log4j.Logger;

import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QTreeView;
import com.trolltech.qt.gui.QWidget;

public class TreeViewUpdater extends Thread {

	static private Logger log = Logger.getLogger(TreeViewUpdater.class);
	    
	private QTreeView treeView;

	public TreeViewUpdater(QTreeView treeView) {
		this.treeView = treeView;
	}
	
	public void run() {
		try {
			treeView.update();
			for (QObject w : treeView.findChildren(QWidget.class)) {
				((QWidget)w).update();
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}	
	
}
