package com.mixshare.rapid_evolution.data.submitted.filter;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.submitted.SubmittedHierarchicalProfile;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;

/**
 * Abstracted ahead of time just in case...
 */
abstract public class SubmittedFilterProfile extends SubmittedHierarchicalProfile {
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SubmittedFilterProfile() { super(); }
	public SubmittedFilterProfile(FilterProfile filterProfile) {
		super(filterProfile);
	}
	
	/////////////
	// SETTERS //
	/////////////

	public void setParentFilterInstances(Vector<FilterHierarchyInstance> parentInstances) {
		Vector<TreeHierarchyInstance> treeInstances = new Vector<TreeHierarchyInstance>(parentInstances.size());
		for (FilterHierarchyInstance filterInstance : parentInstances)
			treeInstances.add(filterInstance);
		setParentInstances(treeInstances);
	}		
	
}
