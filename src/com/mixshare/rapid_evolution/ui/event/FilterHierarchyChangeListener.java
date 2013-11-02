package com.mixshare.rapid_evolution.ui.event;

import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;

public interface FilterHierarchyChangeListener {

	public void updateHierarchy(TreeHierarchyInstance sourceInstance, TreeHierarchyInstance destinationInstance, boolean copy);
	
}
