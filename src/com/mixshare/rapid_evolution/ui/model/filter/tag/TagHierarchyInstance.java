package com.mixshare.rapid_evolution.ui.model.filter.tag;

import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;

public class TagHierarchyInstance extends FilterHierarchyInstance {

    static private final long serialVersionUID = 0L;    	

    public TagHierarchyInstance(HierarchicalRecord record, TreeHierarchyInstance parentInstance) {
		super(record, parentInstance);
	}
    
}
