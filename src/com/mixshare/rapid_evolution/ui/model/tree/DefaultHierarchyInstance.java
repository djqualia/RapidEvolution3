package com.mixshare.rapid_evolution.ui.model.tree;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;

abstract public class DefaultHierarchyInstance extends TreeHierarchyInstance {
	
    static private Logger log = Logger.getLogger(DefaultHierarchyInstance.class);    
	
	public DefaultHierarchyInstance(HierarchicalRecord record, TreeHierarchyInstance parentInstance) {
		super(record, parentInstance);
	}
	
	protected String calculateName() {
		return record.getIdentifier().toString();
	}
	
}
