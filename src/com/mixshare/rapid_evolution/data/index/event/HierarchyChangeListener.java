package com.mixshare.rapid_evolution.data.index.event;

import com.mixshare.rapid_evolution.data.index.HierarchicalIndex;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;

public interface HierarchyChangeListener {

	public void hierarchyChanged(HierarchicalIndex index, HierarchicalRecord movedRecord);
		
}
