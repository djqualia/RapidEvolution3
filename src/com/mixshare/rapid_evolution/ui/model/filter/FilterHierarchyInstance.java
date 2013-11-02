package com.mixshare.rapid_evolution.ui.model.filter;

import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.ui.model.tree.DefaultHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;

abstract public class FilterHierarchyInstance extends DefaultHierarchyInstance {
	
	////////////
	// FIELDS //
	////////////
		
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public FilterHierarchyInstance(HierarchicalRecord record, TreeHierarchyInstance parentInstance) {
		super(record, parentInstance);
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public FilterRecord getFilterRecord() { return (FilterRecord)getRecord(); }
			
	/////////////
	// SETTERS //
	/////////////
	
		
}
