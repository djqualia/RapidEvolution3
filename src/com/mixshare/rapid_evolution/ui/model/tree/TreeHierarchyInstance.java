package com.mixshare.rapid_evolution.ui.model.tree;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.LabelReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;

/**
 * A single filter (i.e. style/tag/playlist) can be the child of multiple parents, therefore to distinguish between the multiple
 * instances of a filter within the model hierarchy, this class is used...
 */
abstract public class TreeHierarchyInstance implements Serializable {
	
    static private Logger log = Logger.getLogger(TreeHierarchyInstance.class);	
	
	static public final byte SELECTION_STATE_NONE = 0;
	static public final byte SELECTION_STATE_OR = 1; // default selection type
	static public final byte SELECTION_STATE_AND = 2;
	static public final byte SELECTION_STATE_NOT = 3;
	
	static public final byte SELECTION_MODE_NORMAL = 0;
	static public final byte SELECTION_MODE_TOGGLE = 1;
	
	////////////
	// FIELDS //
	////////////
	
	protected HierarchicalRecord record;
	protected TreeHierarchyInstance parentInstance;
	protected String name;
	
	transient protected long needsRefreshSince;
	transient protected byte selectionState;	
	
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(TreeHierarchyInstance.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("needsRefreshSince") || pd.getName().equals("selectionState")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
    
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public TreeHierarchyInstance() { }
	public TreeHierarchyInstance(HierarchicalRecord record, TreeHierarchyInstance parentInstance) {
		this.record = record;
		this.parentInstance = parentInstance;
		this.name = calculateName();
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract protected String calculateName();

	/////////////
	// GETTERS //
	/////////////
	
	public TreeHierarchyInstance getParentInstance() {
		return parentInstance;
	}
	
	public HierarchicalRecord getRecord() {
		return record;
	}

	public String getName() { return name; }		
	
	public boolean needsRefresh() { return (needsRefreshSince != 0); }
	
	public byte getSelectionState() { return selectionState; }	
	public boolean isSelected() { return (selectionState != SELECTION_STATE_NONE); }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setNeedsRefresh(boolean needsRefresh) {
		if (needsRefresh) {
			if (needsRefreshSince == 0)
				needsRefreshSince = System.currentTimeMillis();
		} else {
			needsRefreshSince = 0;
		}
	}	
	
	public void setSelectionState(byte state) { selectionState = state; }	
	
	public void recomputeName() {
		this.name = calculateName();
	}
	
	public void setRecord(HierarchicalRecord record) {
		this.record = record;
	}
	public void setParentInstance(TreeHierarchyInstance parentInstance) {
		this.parentInstance = parentInstance;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() { return getName(); }
	
	public boolean equals(Object o) {
		if (o instanceof TreeHierarchyInstance) {
			TreeHierarchyInstance t = (TreeHierarchyInstance)o;
			if (getRecord().equals(t.getRecord())) {
				if ((getParentInstance() == null) && (t.getParentInstance() == null))
					return true;
				if (getParentInstance() != null)
					return getParentInstance().equals(t.getParentInstance());
			}
		}
		return false;
	}
	
	public int hashCode() {
		StringBuffer result = new StringBuffer();
		result.append(String.valueOf(record.getUniqueId()));
		TreeHierarchyInstance parentInstanceIter = parentInstance;
		while (parentInstanceIter != null) {
			result.append(",");
			result.append(String.valueOf(parentInstanceIter.getRecord().getUniqueId()));
			parentInstanceIter = parentInstanceIter.parentInstance;
		}
		return result.toString().hashCode();
	}
		
}
