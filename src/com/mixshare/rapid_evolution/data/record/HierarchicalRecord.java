package com.mixshare.rapid_evolution.data.record;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.index.HierarchicalIndex;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.SemaphoreFactory;

abstract public class HierarchicalRecord extends CommonRecord {

    static private Logger log = Logger.getLogger(HierarchicalRecord.class);

    static private SemaphoreFactory parentSem = new SemaphoreFactory();
    static private SemaphoreFactory childSem = new SemaphoreFactory();

    ////////////
    // FIELDS //
    ////////////

    protected boolean isRoot;
    private int[] parentIds;
    private int[] childIds;

    transient private HierarchicalRecord[] parentRecords;
    transient private HierarchicalRecord[] childRecords;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(HierarchicalRecord.class);
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

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public HierarchicalRecord() { }
    public HierarchicalRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	isRoot = Boolean.parseBoolean(lineReader.getNextLine());
    	int numParentIds = Integer.parseInt(lineReader.getNextLine());
    	parentIds = new int[numParentIds];
    	for (int i = 0; i < numParentIds; ++i)
    		parentIds[i] = Integer.parseInt(lineReader.getNextLine());
    	int numChildIds = Integer.parseInt(lineReader.getNextLine());
    	childIds = new int[numChildIds];
    	for (int i = 0; i < numChildIds; ++i)
    		childIds[i] = Integer.parseInt(lineReader.getNextLine());
    }

    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    abstract public HierarchicalIndex getHierarchicalIndex();

	/////////////
	// GETTERS //
	/////////////

	public boolean isRoot() { return isRoot; }

	public boolean isRootChild() {
		for (HierarchicalRecord record : getParentRecords()) {
			if ((record != null) && record.isRoot())
				return true;
		}
		return false;
	}

	public HierarchicalRecord[] getParentRecords() {
		if (parentRecords == null) {
			if (parentIds == null)
				parentRecords = new HierarchicalRecord[0];
			else {
				parentRecords = new HierarchicalRecord[isRoot() ? 0 : parentIds.length];
				int i = 0;
				for (int parentId : parentIds) {
					HierarchicalRecord record = (HierarchicalRecord)getIndex().getRecord(parentId);
					if ((record == null) && (parentId == getHierarchicalIndex().getRootRecord().getUniqueId()))
						record = getHierarchicalIndex().getRootRecord();
					parentRecords[i++] = record;
				}
			}
		}
		return parentRecords;
	}

	public HierarchicalRecord[] getChildRecords() {
		if (childRecords == null) {
			if (childIds == null)
				childRecords = new HierarchicalRecord[0];
			else {
				childRecords = new HierarchicalRecord[childIds.length];
				int i = 0;
				for (int childId : childIds)
					childRecords[i++] = (HierarchicalRecord)getIndex().getRecord(childId);
			}
		}
		return childRecords;
	}

	public boolean isChildOf(HierarchicalRecord record) {
		if (record != null) {
			for (HierarchicalRecord child : record.getChildRecords()) {
				if (this == child)
					return true;
				if (isChildOf(child))
					return true;
			}
		}
		return false;
	}

	public boolean isParentOf(HierarchicalRecord record) {
		if (record != null) {
			for (HierarchicalRecord parent : record.getParentRecords()) {
				if (this == parent)
					return true;
				if (isParentOf(parent))
					return true;
			}
		}
		return false;
	}

	public boolean containsChildDirectly(HierarchicalRecord record) {
		if (childIds != null) {
			for (int childId : childIds)
				if (childId == record.getUniqueId())
					return true;
		}
		return false;
	}

	public boolean containsParentDirectly(HierarchicalRecord record) {
		if (parentIds != null) {
			for (int parentId : parentIds)
				if (parentId == record.getUniqueId())
					return true;
		}
		return false;
	}

	public int[] getParentIds() { return parentIds; }
	public int[] getChildIds() { return childIds; }

	/////////////
	// SETTERS //
	/////////////

	/**
	 * Do not call directly, use HierarchicalIndex addRelationship method...
	 */
	public boolean addParent(HierarchicalRecord record) {
		return updateParents(record, true);
	}

	/**
	 * Do not call directly, use HierarchicalIndex addRelationship method...
	 */
	public boolean removeParent(HierarchicalRecord record) {
		return updateParents(record, false);
	}

	private boolean updateParents(HierarchicalRecord record, boolean add) {
		boolean result = false;
		if (record == null)
			return result;
		try {
			parentSem.acquire(uniqueId << 4 + getDataType());
			getWriteLockSem().startRead("updateParents");
			if (add) {
				boolean found = false;
				int i = 0;
				if (parentIds != null) {
					while ((i < parentIds.length) && !found) {
						if (parentIds[i] == record.getUniqueId())
							found = true;
						++i;
					}
				}
				if (!found) {
					int[] newParents = new int[(parentIds != null) ? parentIds.length + 1 : 1];
					i = 0;
					if (parentIds != null) {
						while (i < parentIds.length) {
							newParents[i] = parentIds[i];
							++i;
						}
					}
					newParents[i] = record.getUniqueId();
					parentIds = newParents;
					result = true;
				}
			} else {
				// remove
				boolean found = false;
				int removedIndex = 0;
				if (parentIds != null) {
					while ((removedIndex < parentIds.length) && !found) {
						if (parentIds[removedIndex] == record.getUniqueId())
							found = true;
						else
							++removedIndex;
					}
				}
				if (found) {
					int[] newParents = new int[parentIds.length - 1];
					int i = 0;
					while (i < parentIds.length) {
						if (i < removedIndex)
							newParents[i] = parentIds[i];
						else if (i > removedIndex)
							newParents[i - 1] = parentIds[i];
						++i;
					}
					parentIds = newParents;
					result = true;
				} else {
					if (log.isDebugEnabled())
						log.debug("updateParents(): parent not found=" + record);
				}
			}
		} catch (Exception e) {
			log.error("updateParents(): error", e);
		} finally {
			getWriteLockSem().endRead();
			parentSem.release(uniqueId << 4 + getDataType());
		}
		parentRecords = null;
		return result;
	}

	/**
	 * Do not call directly, use HierarchicalIndex addRelationship method...
	 */
	public boolean addChild(HierarchicalRecord record) {
		return updateChildren(record, true);
	}

	/**
	 * Do not call directly, use HierarchicalIndex removeRelationship method...
	 */
	public boolean removeChild(HierarchicalRecord record) {
		return updateChildren(record, false);
	}

	private boolean updateChildren(HierarchicalRecord record, boolean add) {
		boolean result = false;
		if (record == null)
			return result;
		try {
			childSem.acquire(uniqueId << 4 + getDataType());
			getWriteLockSem().startRead("updateChildren");
			if (add) {
				boolean found = false;
				int i = 0;
				if (childIds != null) {
					while ((i < childIds.length) && !found) {
						if (childIds[i] == record.getUniqueId())
							found = true;
						++i;
					}
				}
				if (!found) {
					int[] newChilds = new int[(childIds != null) ? childIds.length + 1 : 1];
					i = 0;
					if (childIds != null) {
						while (i < childIds.length) {
							newChilds[i] = childIds[i];
							++i;
						}
					}
					newChilds[i] = record.getUniqueId();
					childIds = newChilds;
					result = true;
				}
			} else {
				// remove
				removeChildId(record.getUniqueId());
				if (record.getDuplicateIds() != null) {
					for (int duplicateId : record.getDuplicateIds())
						removeChildId(duplicateId);
				}
			}
		} catch (Exception e) {
			log.error("updateChildren(): error", e);
		} finally {
			getWriteLockSem().endRead();
			childSem.release(uniqueId << 4 + getDataType());
		}
		childRecords = null;
		return result;
	}

	private void removeChildId(int childId) {
		if (childIds == null)
			return;
		boolean found = false;
		int removedIndex = 0;
		while ((removedIndex < childIds.length) && !found) {
			if (childIds[removedIndex] == childId)
				found = true;
			else
				++removedIndex;
		}
		if (found) {
			int[] newChilds = new int[childIds.length - 1];
			int i = 0;
			while (i < childIds.length) {
				if (i < removedIndex)
					newChilds[i] = childIds[i];
				else if (i > removedIndex)
					newChilds[i - 1] = childIds[i];
				++i;
			}
			childIds = newChilds;
		} else {
			if (log.isTraceEnabled())
				log.trace("updateChildren(): child id found=" + childId);
		}
	}


	// for serialization
	public void setRoot(boolean isRoot) { this.isRoot = isRoot; }
	public void setParentIds(int[] parentIds) { this.parentIds = parentIds; }
	public void setChildIds(int[] childIds) { this.childIds = childIds; }

	/////////////
	// METHODS //
	/////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);
    	HierarchicalRecord hierarchicalRecord = (HierarchicalRecord)record;
    	// parent ids
    	for (HierarchicalRecord parentRecord : hierarchicalRecord.getParentRecords()) {
    		if (!isParentOf(parentRecord) && !parentRecord.isRoot())
    			getHierarchicalIndex().addRelationship(parentRecord, this);
    	}
    	// child ids
    	for (HierarchicalRecord childRecord : hierarchicalRecord.getChildRecords()) {
    		if (!isChildOf(childRecord))
    			getHierarchicalIndex().addRelationship(this, childRecord);
    	}
    }

    public void checkRelations() {
    	if (parentIds != null) {
    		boolean foundNull = false;
    		for (int parentId : parentIds) {
    			if (!getIndex().doesExist(parentId) && (getHierarchicalIndex().getRootRecord().getUniqueId() != parentId)) {
    				log.warn("doesn't exist=" + parentId);
    				foundNull = true;
    				break;
    			}
    		}
    		if (foundNull) {
	    		Vector<Integer> newParentIds = new Vector<Integer>(parentIds.length);
	    		for (int parentId : parentIds)
	    			if (getIndex().doesExist(parentId) || (getHierarchicalIndex().getRootRecord().getUniqueId() == parentId))
	    				newParentIds.add(parentId);
    			parentIds = new int[newParentIds.size()];
    			int i = 0;
    			for (int newParentId : newParentIds)
    				parentIds[i++] = newParentId;
	    		parentRecords = null; // invalidate cached transient
    		}
    	}
    	if (childIds != null) {
    		boolean foundNull = false;
    		for (int childId : childIds) {
    			if (!getIndex().doesExist(childId)) {
    				foundNull = true;
    				break;
    			}
    		}
    		if (foundNull) {
	    		Vector<Integer> newChildIds = new Vector<Integer>(childIds.length);
	    		for (int childId : childIds)
	    			if (getIndex().doesExist(childId))
	    				newChildIds.add(childId);
    			childIds = new int[newChildIds.size()];
    			int i = 0;
    			for (int newChildId : newChildIds)
    				childIds[i++] = newChildId;
	    		childRecords = null; // invalidate cached transient
    		}
    	}
    }

    @Override
	public void write(LineWriter textWriter) {
    	super.write(textWriter);
    	textWriter.writeLine(1); //version
    	textWriter.writeLine(isRoot);
    	if (parentIds != null) {
    		textWriter.writeLine(parentIds.length);
    		for (int parentId : parentIds)
    			textWriter.writeLine(parentId);
    	} else {
    		textWriter.writeLine(0);
    	}
    	if (childIds != null) {
    		textWriter.writeLine(childIds.length);
    		for (int childId : childIds)
    			textWriter.writeLine(childId);
    	} else {
    		textWriter.writeLine(0);
    	}
    }

}
