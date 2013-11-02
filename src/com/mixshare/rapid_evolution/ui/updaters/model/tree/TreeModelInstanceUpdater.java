package com.mixshare.rapid_evolution.ui.updaters.model.tree;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.tree.RecordTreeModelManager;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.tree.TreeModelManager;

public class TreeModelInstanceUpdater extends Thread {

	static private Logger log = Logger.getLogger(TreeModelInstanceUpdater.class);
	
    static public final byte ACTION_ADD = 0;
    static public final byte ACTION_UPDATE = 1;
    static public final byte ACTION_FULL_UPDATE = 2;
    static public final byte ACTION_REMOVE = 3;
    static public final byte ACTION_REFRESH = 4;
    
	private Vector<TreeHierarchyInstance> treeInstances;
	private RecordTreeModelManager modelManager;
	private byte action;

	public TreeModelInstanceUpdater(Vector<TreeHierarchyInstance> treeInstances, RecordTreeModelManager modelManager, byte action) {
		this.treeInstances = treeInstances;
		this.modelManager = modelManager;
		this.action = action;		
	}
	
	public TreeModelInstanceUpdater(TreeHierarchyInstance treeInstance, RecordTreeModelManager modelManager, byte action) {
		treeInstances = new Vector<TreeHierarchyInstance>(1);
		treeInstances.add(treeInstance);
		this.modelManager = modelManager;
		this.action = action;
	}

	public TreeModelInstanceUpdater(RecordTreeModelManager modelManager, byte action) {
		this.modelManager = modelManager;
		this.action = action;
	}
	
	public void run() {
		try {
			if (treeInstances != null) {
				for (TreeHierarchyInstance treeInstance : treeInstances) {
					if (action == ACTION_ADD) {
						modelManager.addInstance(treeInstance);
					} else if (action == ACTION_UPDATE){
						modelManager.updateInstance(treeInstance);
					} else if (action == ACTION_FULL_UPDATE){
						modelManager.updateInstance(treeInstance, true, false);
					} else if (action == ACTION_REMOVE) {
						modelManager.removeInstance(treeInstance);
					}
				}
			}  else if (action == ACTION_REFRESH) {
				modelManager.refresh();
			}
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}	
	
}
