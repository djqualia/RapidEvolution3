package com.mixshare.rapid_evolution.data.profile;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * Hierarchy information is stored in the record (and therefore memory), however, this abstraction
 * wraps up access to the record hierarchy calls...
 */
abstract public class HierarchicalProfile extends CommonProfile {

    static private Logger log = Logger.getLogger(HierarchicalProfile.class);

	////////////
	// FIELDS //
	////////////

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(HierarchicalProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("parentRecords") || pd.getName().equals("childRecords")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    public HierarchicalProfile() { super(); }
    public HierarchicalProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }

	/////////////
	// GETTERS //
	/////////////

	public HierarchicalRecord getHierarchicalRecord() { return (HierarchicalRecord)record; }
	public HierarchicalRecord[] getParentRecords() { return (getHierarchicalRecord() != null) ? getHierarchicalRecord().getParentRecords() : null; }
	public HierarchicalRecord[] getChildRecords() { return (getHierarchicalRecord() != null) ? getHierarchicalRecord().getChildRecords() : null; }

	public int[] getParentIds() { return (getHierarchicalRecord() != null) ? getHierarchicalRecord().getParentIds() : new int[0]; }
	public int[] getChildIds() { return (getHierarchicalRecord() != null) ? getHierarchicalRecord().getChildIds() : new int[0]; }

	/////////////
	// METHODS //
	/////////////

	public boolean isChildOf(HierarchicalProfile profile) { return getHierarchicalRecord().isChildOf(profile.getHierarchicalRecord()); }
	public boolean isParentOf(HierarchicalProfile profile) { return getHierarchicalRecord().isParentOf(profile.getHierarchicalRecord()); }

    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1", "HierarchicalProfile.version"); // version
    }

}
