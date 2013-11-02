package com.mixshare.rapid_evolution.data.submitted;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.profile.HierarchicalProfile;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;

public class SubmittedHierarchicalProfile extends SubmittedProfile {
		
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SubmittedHierarchicalProfile() { super(); }
	public SubmittedHierarchicalProfile(HierarchicalProfile hierarchicalProfile) {
		super(hierarchicalProfile);
	}
	
	////////////
	// FIELDS //
	////////////
	
	protected Vector<TreeHierarchyInstance> parentInstances = new Vector<TreeHierarchyInstance>();
	protected boolean doNotAddToHierarchy; // used during import process to hold off on establishing hierarchy until it is read at a later point

	/////////////
	// GETTERS //
	/////////////
	
	public Vector<TreeHierarchyInstance> getParentInstances() { return parentInstances; }
	public boolean doNotAddToHierarchy() { return doNotAddToHierarchy; }
	public boolean isChildOfRoot() { return (parentInstances.size() == 0); }
	
	/////////////
	// SETTERS //
	/////////////

	public void setParentInstances(Vector<TreeHierarchyInstance> parentInstances) { this.parentInstances = parentInstances; }		
	public void setDoNotAddToHierarchy(boolean doNotAddToHierarchy) { this.doNotAddToHierarchy = doNotAddToHierarchy; }	

}
