package com.mixshare.rapid_evolution.ui.model.filter.playlist;

import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;

public class PlaylistHierarchyInstance extends FilterHierarchyInstance {

    static private final long serialVersionUID = 0L;    	

    public PlaylistHierarchyInstance(HierarchicalRecord record, TreeHierarchyInstance parentInstance) {
		super(record, parentInstance);
	}
    
}
