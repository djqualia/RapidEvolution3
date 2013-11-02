package com.mixshare.rapid_evolution.ui.updaters.model.tree;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.tree.RecordTreeModelManager;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.trolltech.qt.gui.QStandardItem;

public class TreeModelHierarchyUpdater extends Thread {

    private static Logger log = Logger.getLogger(TreeModelHierarchyUpdater.class);
	    
    private QStandardItem sourceItem;
	private TreeHierarchyInstance sourceInstance;
	private QStandardItem destinationItem;
	private TreeHierarchyInstance destinationInstance;
	private boolean copy;
	private RecordTreeModelManager modelManager;
	
	public TreeModelHierarchyUpdater(QStandardItem sourceItem, TreeHierarchyInstance sourceInstance, QStandardItem destinationItem, TreeHierarchyInstance destinationInstance, boolean copy, RecordTreeModelManager modelManager) {
		this.sourceItem = sourceItem;
		this.sourceInstance = sourceInstance;
		this.destinationItem = destinationItem;
		this.destinationInstance = destinationInstance;
		this.copy = copy;
		this.modelManager = modelManager;
	}
	
	
	public void run() {
		try {
			modelManager.updateHierarchy(sourceItem, sourceInstance, destinationItem, destinationInstance, copy);
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}	
	
}
