package com.mixshare.rapid_evolution.data.index;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.index.event.HierarchyChangeListener;
import com.mixshare.rapid_evolution.data.profile.HierarchicalProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.ui.model.tree.TreeModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * Dictates how hierarchical/tree based UI models are updated when a record is added/removed...
 * For example, styles, tags, playlists...
 */
abstract public class HierarchicalIndex extends CommonIndex {
	
	static private Logger log = Logger.getLogger(HierarchicalIndex.class);
	
	////////////
	// FIELDS //
	////////////
	
	protected HierarchicalRecord rootRecord = createRootRecord(); // never displayed, but needed for uniform parent/child relationships to be defined for all hierarchical records...
	
	transient private Vector<HierarchyChangeListener> changeListeners = new Vector<HierarchyChangeListener>();	
	
    static {    	
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(HierarchicalIndex.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("changeListeners")) {
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
	
	public HierarchicalIndex() {
		super();
		imdb.update(rootRecord); // hack: needed to force storage of root in hbase solution
	}
	public HierarchicalIndex(LineReader lineReader) {
		super(lineReader);
		String version = lineReader.getNextLine();
		rootRecord = createRootRecord(lineReader);
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract protected HierarchicalRecord createRootRecord();
	abstract protected HierarchicalRecord createRootRecord(LineReader lineReader);
	
	/////////////
	// GETTERS //
	/////////////
	
	public HierarchicalRecord[] getRootRecords() { return rootRecord.getChildRecords(); }
			
	public TreeModelManager getTreeModelManager() { return (TreeModelManager)getModelManager(); }
	
	public HierarchicalRecord getRootRecord() { return rootRecord; }

	/////////////
	// SETTERS //
	/////////////
	
	// for serialization
	public void setRootRecord(HierarchicalRecord rootRecord) { this.rootRecord = rootRecord; }
	
	/////////////
	// METHODS //
	/////////////
	
	public void addHierarchyChangeListener(HierarchyChangeListener listener) {
		if (changeListeners == null)
			changeListeners = new Vector<HierarchyChangeListener>();
		if (!changeListeners.contains(listener))
			changeListeners.add(listener);
	}
			
	public void mergeProfiles(Profile primaryProfile, Profile mergedProfile) {
		// ensure a parent/child are not merged
		HierarchicalProfile profile1 = (HierarchicalProfile)primaryProfile;
		HierarchicalProfile profile2 = (HierarchicalProfile)mergedProfile;
		if ((!profile1.isParentOf(profile2)) && (!profile1.isChildOf(profile2)))
			super.mergeProfiles(primaryProfile, mergedProfile);
	}
	
	/**
	 * Adds a parent/child relationship.
	 * Returns true if successful (checks to prevent infinite loops in parent/child relationships before adding)
	 */
	public boolean addRelationship(HierarchicalRecord parent, HierarchicalRecord child) {
		if (!parent.isChildOf(child)) {
			boolean changed = parent.addChild(child);
			changed |= child.addParent(parent);
			if (changed) {
				getImdb().update(parent);
				getImdb().update(child);
				if (!parent.equals(getRootRecord())) {
					if (changeListeners != null) {
						for (HierarchyChangeListener changeListener : changeListeners)
							changeListener.hierarchyChanged(this, child);
					}
					HierarchicalProfile profile1 = (HierarchicalProfile)getProfile(parent.getUniqueId());
					if (profile1 != null)
						profile1.save();
					HierarchicalProfile profile2 = (HierarchicalProfile)getProfile(child.getUniqueId());
					if (profile2 != null)
						profile2.save();					
				}
			}
			return true;
		}
		return false;
	}
		
	/**
	 * Removes a parent/child relationship.
	 */
	public void removeRelationship(HierarchicalRecord parent, HierarchicalRecord child) {
		boolean changed = parent.removeChild(child);
		changed |= child.removeParent(parent);
		if (changed) {
			getImdb().update(parent);
			getImdb().update(child);
			if (!parent.equals(getRootRecord())) {
				if (changeListeners != null) {
					for (HierarchyChangeListener changeListener : changeListeners)
						changeListener.hierarchyChanged(this, child);
				}
			}
		}
	}
	
	protected void addRelationalItems(Record addedRecord) { }
	
	protected void removeRelationalItems(Record removedRecord, boolean deleteRecords) { }

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
		rootRecord.write(writer);
	}
	
}
