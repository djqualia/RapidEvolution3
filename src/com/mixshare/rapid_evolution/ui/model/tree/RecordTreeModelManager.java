package com.mixshare.rapid_evolution.ui.model.tree;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.index.HierarchicalIndex;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.index.event.ProfilesMergedListener;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedHierarchicalProfile;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.model.filter.FilterProxyModel;
import com.mixshare.rapid_evolution.ui.updaters.model.tree.TreeModelHierarchyUpdater;
import com.mixshare.rapid_evolution.ui.updaters.model.tree.TreeModelInstanceUpdater;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.Semaphore;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.ItemDataRole;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;

/**
 * The tree model supports drag and drop operations, as well as a record existing in multiple
 * places in the hierarchy (i.e. multiple parents).  Each place a record exists in the tree
 * is referred to as a TreeInstance.
 */
abstract public class RecordTreeModelManager extends TreeModelManager implements ProfilesMergedListener {

    static private Logger log = Logger.getLogger(RecordTreeModelManager.class);    
    
    static private long TREE_UPDATE_TIMEOUT_MILLIS = RE3Properties.getLong("tree_update_sem_timeout_millis");
    
    ////////////
    // FIELDS //
    ////////////
    
    transient protected Map<TreeHierarchyInstance, QStandardItem> itemMap = new HashMap<TreeHierarchyInstance, QStandardItem>();
    
    transient protected Semaphore itemMapSem;
    //protected transient Semaphore updateSem;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(RecordTreeModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("itemMap") || pd.getName().equals("itemMapSem") || pd.getName().equals("updateSem")) {
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
    
    public RecordTreeModelManager() { super(); }
	public RecordTreeModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}    
    
    ////////////////////
    // INITIALIZATION //
    ////////////////////
    
	public void initialize(QObject parent) {
		if (itemMap != null)
			itemMap.clear();
		super.initialize(parent);
	}	

	protected void createSourceModel(QObject parent) {
		model = new TreeItemModel(getHierarchicalIndex().getRootRecords().length, getNumColumns(), parent, this);		
		loadTree();
	}
				
	public void loadData(Vector<SearchResult> results, SearchParameters searchParameters) { loadData(results, searchParameters, true); }
	public void loadData(Vector<SearchResult> results, SearchParameters searchParameters, boolean resetModel) {
		if (resetModel)
			resetModel();
		Map<FilterRecord, Object> rootRecords = new LinkedHashMap<FilterRecord, Object>();
		for (SearchResult result : results)
			collectRootRecords((FilterRecord)result.getRecord(), rootRecords);
		QStandardItem rootItem = ((QStandardItemModel)model).invisibleRootItem();
		int row = 0;
		for (FilterRecord record : rootRecords.keySet()) {
			TreeHierarchyInstance treeInstance = getTreeHierarchyInstance(record, null);
			if (treeInstance != null) {
				int column = 0;
				for (QStandardItem item : createRow(treeInstance))
					rootItem.setChild(row, column++, item);		
				for (HierarchicalRecord child : treeInstance.getRecord().getChildRecords()) {
					if ((searchParameters == null) || recursiveMatch(searchParameters, child)) {
						TreeHierarchyInstance childInstance = getTreeHierarchyInstance(child, treeInstance);
						addInstance(childInstance, searchParameters);
					}
				}
				++row;
			}			
		}
	}
	
	protected boolean recursiveMatch(SearchParameters searchParameters, HierarchicalRecord record) {
		if (record != null) {
			if (searchParameters.matches(record) > 0.0f)
				return true;
			for (HierarchicalRecord child : record.getChildRecords())
				if ((child != null) && recursiveMatch(searchParameters, child))
					return true;
		}
		return false;
	}
	
	protected void collectRootRecords(FilterRecord filter, Map<FilterRecord, Object> result) {
		if  (filter != null) {
			if (filter.isRootChild())
				result.put(filter, null);
			else {
				for (HierarchicalRecord parent : filter.getParentRecords()) {
					collectRootRecords((FilterRecord)parent, result);
				}
			}
		}
	}
	
	protected void loadTree() {
		if (log.isDebugEnabled())
			log.debug("loadTree(): starting...");
		initViewColumns();
		for (int c = 0; c < getNumColumns(); ++c)
			model.setHeaderData(c, Qt.Orientation.Horizontal, getSourceColumnTitle(c));
		if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported()) {
			//if (getProxyModel() instanceof FilterProxyModel) {
				//FilterProxyModel filterProxy = (FilterProxyModel)getProxyModel();
				//SearchParameters searchParams = filterProxy.getNewFilterSearchParameters();
				//loadData(getIndex().searchRecords(searchParams), searchParams, false);
			//}
			return;
		}
		// load initial data
		/*
		HierarchicalRecord[] rootRecords = getHierarchicalIndex().getRootRecords(); 	
		for (HierarchicalRecord record : rootRecords) {
			addInstance(getTreeHierarchyInstance(record, null));
		}
		*/		
		QStandardItem rootItem = ((QStandardItemModel)model).invisibleRootItem();
		HierarchicalRecord[] rootRecords = getHierarchicalIndex().getRootRecords();
		Map<HierarchicalRecord, Object> usedRecords = new HashMap<HierarchicalRecord, Object>(rootRecords.length);
		int row = 0;
		for (HierarchicalRecord record : rootRecords) {
			if (!usedRecords.containsKey(record)) {
				usedRecords.put(record, null);
				TreeHierarchyInstance treeInstance = getTreeHierarchyInstance(record, null);
				if (treeInstance != null) {
					int column = 0;
					for (QStandardItem item : createRow(treeInstance))
						rootItem.setChild(row, column++, item);		
					for (HierarchicalRecord child : treeInstance.getRecord().getChildRecords()) {
						TreeHierarchyInstance childInstance = getTreeHierarchyInstance(child, treeInstance);
						addInstance(childInstance);
					}
					++row;
				}
			}
		}
		if (log.isDebugEnabled())
			log.debug("loadTree(): done");
	}
	
	protected ArrayList<QStandardItem> createRow(TreeHierarchyInstance thisInstance) {
		if (log.isTraceEnabled())
			log.trace("createRow(): started, thisInstance=" + thisInstance);
		ArrayList<QStandardItem> newRow = new ArrayList<QStandardItem>(getNumColumns());
		if (thisInstance == null)
			return newRow;
		for (int c = 0; c < getNumColumns(); ++c) {
			QStandardItem column = new QStandardItem(); //TreeItem();						
			column.setData(thisInstance); // user role
			if (log.isTraceEnabled())
				log.trace("createRow(): getting data for column=" + getSourceColumnType(c));
			if (getSourceColumnType(c).isIdColumn()) {
				try {
					getItemMapSem().acquire("createRow()");						
					getItemMap().put(thisInstance, column);
				} catch (Exception e) {
					log.error("createRow(): error", e);
				} finally {
					getItemMapSem().release();
				}
				column.setData(new SmartString(thisInstance.getName()), ItemDataRole.DisplayRole);
			} else {
				column.setData(getSourceColumnData(c, thisInstance.getRecord()), ItemDataRole.DisplayRole);
			}
			newRow.add(column);
		}
		if (log.isTraceEnabled())
			log.trace("createRow(): done");
		return newRow;
	}
	
    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////
    
	abstract public TreeHierarchyInstance getTreeHierarchyInstance(HierarchicalRecord obj, TreeHierarchyInstance parentInstance);    
    
	abstract public Index getIndex();
	
	abstract public void updateInstance(TreeHierarchyInstance treeInstance);
			
	abstract public boolean isLazySearchSupported();
	
    /////////////
    // GETTERS //
    /////////////
    
	public HierarchicalIndex getHierarchicalIndex() { return (HierarchicalIndex)getIndex(); }
	
	private Semaphore getItemMapSem() {
		if (itemMapSem == null)
			itemMapSem = new Semaphore(1);
		return itemMapSem;
	}
	
	//private Semaphore getUpdateSem() {
		//if (updateSem == null)
			//updateSem = new Semaphore(1);
		//return updateSem;
	//}
	
	private Map<TreeHierarchyInstance, QStandardItem> getItemMap() {
		if (itemMap == null)
			itemMap = new HashMap<TreeHierarchyInstance, QStandardItem>();
		return itemMap;
	}
		
	public Vector<TreeHierarchyInstance> getInstances() {
		Vector<TreeHierarchyInstance> instances = new Vector<TreeHierarchyInstance>();
		try {
			getItemMapSem().acquire("getInstances");
			Iterator<TreeHierarchyInstance> iter = getItemMap().keySet().iterator();
			while (iter.hasNext()) {
				TreeHierarchyInstance treeInstance = iter.next();
				instances.add(treeInstance);
			}
		} catch (Exception e) {
			log.error("updated(): error", e);
		} finally {
			getItemMapSem().release();
		}
		return instances;		
	}
	
	public Vector<TreeHierarchyInstance> getAllInstancesToRefresh() {
		Vector<TreeHierarchyInstance> instances = new Vector<TreeHierarchyInstance>();
		try {
			getItemMapSem().acquire("getInstancesToRefresh");
			Iterator<TreeHierarchyInstance> iter = getItemMap().keySet().iterator();
			while (iter.hasNext()) {
				TreeHierarchyInstance treeInstance = iter.next();
				if (treeInstance.needsRefresh())
					instances.add(treeInstance);
			}
		} catch (Exception e) {
			log.error("updated(): error", e);
		} finally {
			getItemMapSem().release();
		}
		return instances;		
	}	
	
	public Vector<TreeHierarchyInstance> getTopInstancesToRefresh(int size) {
		Vector<TreeHierarchyInstance> instances = new Vector<TreeHierarchyInstance>();
		if (log.isTraceEnabled())
			log.trace("getTopInstancesToRefresh(): started");
		try {
			getItemMapSem().acquire("getInstancesToRefresh");
			Iterator<TreeHierarchyInstance> iter = getItemMap().keySet().iterator();
			while (iter.hasNext()) {
				TreeHierarchyInstance treeInstance = iter.next();
				if (treeInstance.needsRefresh()) {
					// sorted insert
					boolean inserted = false;
					int i = 0;
					while ((i < instances.size()) && !inserted) {
						TreeHierarchyInstance existingInstance = instances.get(i);
						if (treeInstance.needsRefreshSince < existingInstance.needsRefreshSince) {
							instances.insertElementAt(treeInstance, i);
							inserted = true;
						}						
						++i;
					}
					if (!inserted && (instances.size() < size))
						instances.add(treeInstance);
					while (instances.size() > size)
						instances.removeElementAt(instances.size() - 1);
				}
			}
		} catch (Exception e) {
			log.error("updated(): error", e);
		} finally {
			getItemMapSem().release();
		}
		if (log.isTraceEnabled())
			log.trace("getTopInstancesToRefresh(): result=" + instances);
		return instances;		
	}	
	
	/**
	 * Returns the "source" index of a tree hierarhcy index
	 */
	public QModelIndex getIndexOfInstance(TreeHierarchyInstance instance) {
		return getItemMap().get(instance).index();
	}
	
	/**
	 * Given a hierarchical record, this will return all instances of where it exists in the tree/hierarchy
	 */
	public Vector<TreeHierarchyInstance> getMatchingInstances(HierarchicalRecord record) {
		Vector<TreeHierarchyInstance> matchingInstances = new Vector<TreeHierarchyInstance>();
		try {
			getItemMapSem().acquire("getMatchingInstances");
			Iterator<TreeHierarchyInstance> iter = getItemMap().keySet().iterator();
			while (iter.hasNext()) {
				TreeHierarchyInstance treeInstance = iter.next();
				if (treeInstance.getRecord().equals(record))
					matchingInstances.add(treeInstance);
			}
		} catch (Exception e) {
			log.error("updated(): error", e);
		} finally {
			getItemMapSem().release();
		}
		return matchingInstances;
	}	
	
	/**
	 * During drag and drop operations, certain items are serialized and deserialized, and there seemed to be a problem when
	 * a serialized copy of the QStandardItem was passed for the hierarchy change update.  This method finds the actual
	 * instance being used by the tree which matches the serialized version dropped, and allows for the proper behavior. 
	 */
	public TreeHierarchyInstance getActualTreeHierarchyInstance(TreeHierarchyInstance serializedVersion) {
		TreeHierarchyInstance result = null;
		try {
			getItemMapSem().acquire("getActualTreeInstanceFromSerialized");
			Iterator<TreeHierarchyInstance> iter = getItemMap().keySet().iterator();
			while (iter.hasNext() && (result == null)) {
				TreeHierarchyInstance treeInstance = iter.next();
				if (treeInstance.equals(serializedVersion))
					result = treeInstance;
			}			
		} catch (Exception e) {
			log.error("getActualTreeInstanceFromSerialized(): error", e);
		} finally {
			getItemMapSem().release();
		}
		return result;
	}	
	
	public QStandardItem getStandardItem(TreeHierarchyInstance treeInstance) {
		if (treeInstance == null)
			return null;
		Vector<TreeHierarchyInstance> matches = new Vector<TreeHierarchyInstance>();
		TreeHierarchyInstance treeInstancWalk = treeInstance;
		matches.add(treeInstancWalk);
		while (treeInstancWalk.getParentInstance() != null) {
			treeInstancWalk = treeInstancWalk.getParentInstance();
			matches.insertElementAt(treeInstancWalk, 0);
		}		
		for (int r = 0; r < getTreeItemModel().rowCount(); ++r) {			
			QStandardItem standardItem = getTreeItemModel().item(r);
			TreeHierarchyInstance instance = (TreeHierarchyInstance)standardItem.data();
			if (instance.equals(matches.get(0))) {
				if (matches.size() == 1)					
					return standardItem;
				matches.remove(0);
				return getStandardItemCheckChildren(matches, standardItem);			
			}
		}
		return null;
	}
	
	private QStandardItem getStandardItemCheckChildren(Vector<TreeHierarchyInstance> matches, QStandardItem parent) {
		for (int r = 0; r < parent.rowCount(); ++r) {
			QStandardItem standardItem = parent.child(r);
			TreeHierarchyInstance instance = (TreeHierarchyInstance)standardItem.data();
			if (instance.equals(matches.get(0))) {
				if (matches.size() == 1)					
					return standardItem;
				matches.remove(0);
				return getStandardItemCheckChildren(matches, standardItem);			
			}
		}
		return null;
	}	
	
	/////////////
	// SETTERS //
	/////////////
	
	
	////////////
	// EVENTS //
	////////////
		
	public void addedRecord(Record record, SubmittedProfile submittedProfile) {
		if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported()) {			
			boolean pass = false;
			if (getProxyModel() instanceof FilterProxyModel) {
				FilterProxyModel filterProxy = (FilterProxyModel)getProxyModel();
				SearchParameters searchParams = filterProxy.getSearchParameters();
				if (searchParams != null) {
					if (!searchParams.isEmpty() && (searchParams.matches(record) > 0.0f))
						pass = true;
				}
			}
			if (!pass)
				return;
		}
		try {
			//getUpdateSem().tryAcquire("addedRecord", TREE_UPDATE_TIMEOUT_MILLIS);
			// make sure another thread did not add it while waiting
			Vector<TreeHierarchyInstance> matchingInstances = getMatchingInstances((HierarchicalRecord)record);
			if (matchingInstances.size() > 0) {
				QApplication.invokeAndWait(new TreeModelInstanceUpdater(matchingInstances, this, TreeModelInstanceUpdater.ACTION_UPDATE));
			} else {
				SubmittedHierarchicalProfile submittedHierarchicalProfile = (SubmittedHierarchicalProfile)submittedProfile;
				HierarchicalRecord hierarchicalRecord = (HierarchicalRecord)record;
				if (submittedHierarchicalProfile.isChildOfRoot()) {
					// add to the root
					QApplication.invokeAndWait(new TreeModelInstanceUpdater(getTreeHierarchyInstance((HierarchicalRecord)record, null), this, TreeModelInstanceUpdater.ACTION_ADD));
				} else {
					// parent instances were specified, add a new instance to each
					for (TreeHierarchyInstance hierarchyInstance : submittedHierarchicalProfile.getParentInstances()) {
						TreeHierarchyInstance newInstance = getTreeHierarchyInstance(hierarchicalRecord, hierarchyInstance);
						QApplication.invokeAndWait(new TreeModelInstanceUpdater(newInstance, this, TreeModelInstanceUpdater.ACTION_ADD));
					}
				}
			}
		} catch (Exception e) {
			log.error("addedRecord(): error", e);
		} finally {
			//getUpdateSem().release();
		}
	}	
	public void updatedRecord(Record record) {
		try {
			if (RapidEvolution3.isTerminated)
				return;
			//getUpdateSem().tryAcquire("updatedRecord", TREE_UPDATE_TIMEOUT_MILLIS);
			Vector<TreeHierarchyInstance> matchingInstances = getMatchingInstances((HierarchicalRecord)record);
			if (matchingInstances.size() > 0) {
				for (TreeHierarchyInstance instance : matchingInstances)
					instance.recomputeName();
				QApplication.invokeAndWait(new TreeModelInstanceUpdater(matchingInstances, this, TreeModelInstanceUpdater.ACTION_UPDATE));
			}
		} catch (Exception e) {
			log.error("updateRecord(): error", e);
		} finally {
			//getUpdateSem().release();
		}
	}
	public void removedRecord(Record record) {
		try {
			//getUpdateSem().tryAcquire("removeRecord", TREE_UPDATE_TIMEOUT_MILLIS);
			Vector<TreeHierarchyInstance> matchingInstances = getMatchingInstances((HierarchicalRecord)record);
			if (matchingInstances.size() > 0)		
				QApplication.invokeAndWait(new TreeModelInstanceUpdater(matchingInstances, this, TreeModelInstanceUpdater.ACTION_REMOVE));
		} catch (Exception e) {
			log.error("removedRecord(): error", e);
		} finally {
			//getUpdateSem().release();
		}
	}	
	public void profilesMerged(Profile primaryProfile, Profile mergedProfile) {
		try {			
			Vector<TreeHierarchyInstance> matchingInstances = getMatchingInstances((HierarchicalRecord)primaryProfile.getRecord());
			if (matchingInstances.size() > 0)
				QApplication.invokeAndWait(new TreeModelInstanceUpdater(matchingInstances, this, TreeModelInstanceUpdater.ACTION_FULL_UPDATE));
			// we'll do a full refresh on all parents of the merged profile, to ensure it is added to all places where the duplicate was removed
			// TODO: this is being lazy, a more efficient method could be developed to just check this profile...
			FilterProfile primaryFilter = (FilterProfile)primaryProfile;
			for (HierarchicalRecord parent : primaryFilter.getParentRecords()) {
				if (!parent.isRoot()) {
					Vector<TreeHierarchyInstance> matchingParentInstaces = getMatchingInstances(parent);
					if (matchingParentInstaces.size() > 0)
						QApplication.invokeAndWait(new TreeModelInstanceUpdater(matchingParentInstaces, this, TreeModelInstanceUpdater.ACTION_FULL_UPDATE));
				}
			}
		} catch (Exception e) {
			log.error("profilesMerged(): error", e);
		}
	}
	
	/////////////
	// METHODS //
	/////////////
		
	public void updateHierarchy(TreeHierarchyInstance sourceInstance, TreeHierarchyInstance destinationInstance, boolean copy) {
		QStandardItem sourceItem = getItemMap().get(sourceInstance);
		QStandardItem destinationItem = getItemMap().get(destinationInstance);
		QApplication.invokeAndWait(new TreeModelHierarchyUpdater(sourceItem, sourceInstance, destinationItem, destinationInstance, copy, this));
	}
		
	/**
	 * Don't call directly in a Java thread, only the (main) GUI thread...
	 */
	public void addInstance(TreeHierarchyInstance treeInstance) { addInstance(treeInstance, null); }
	public void addInstance(TreeHierarchyInstance treeInstance, SearchParameters searchParameters) { //HierarchicalRecord record, QStandardItem parentItem, TreeHierarchyInstance parentInstance) {
		try {
			if (log.isTraceEnabled())
				log.trace("addInstance(): treeInstance=" + treeInstance);
			if (treeInstance != null) {
				QStandardItem parentItem = getItemMap().get(treeInstance.getParentInstance());
				if (parentItem == null)
					parentItem = ((QStandardItemModel)model).invisibleRootItem();
				parentItem.appendRow(createRow(treeInstance));			
				for (HierarchicalRecord child : treeInstance.getRecord().getChildRecords()) {
					if ((searchParameters == null) || recursiveMatch(searchParameters, child)) {
						TreeHierarchyInstance childInstance = getTreeHierarchyInstance(child, treeInstance);
						addInstance(childInstance);
					}
				}
			}
		} catch (Exception e) {
			log.error("addRow(): error", e);			
		}
	}
	
	/**
	 * Don't call directly in a Java thread, only the (main) GUI thread...
	 * 
	 * Making a slight assumption that the update method won't have to deal with hierarchy changes, this will only update the row's data basically...
	 */		
	public void updateInstance(TreeHierarchyInstance treeInstance, boolean updateChildren, boolean fakeUpdate) {
		try {
			if (log.isTraceEnabled())
				log.trace("updateInstance(): starting, treeInstance=" + treeInstance + ", updateChildren=" + updateChildren);			
			QStandardItem treeItem = getItemMap().get(treeInstance);
			if ((treeItem != null) && treeItem.nativePointer() != null) { 
				QStandardItem parentItem = getItemMap().get(treeInstance.getParentInstance());
				if (parentItem == null)
					parentItem = ((QStandardItemModel)model).invisibleRootItem();
				int r = treeItem.row();
				if (log.isTraceEnabled())
					log.trace("updateInstance(): setting data");			
				for (int c = 0; c < getNumColumns(); ++c) {
					QStandardItem child = parentItem.child(r, c);
					if (child != null) {
						if (getSourceColumnType(c).isIdColumn()) {
							if (fakeUpdate)
								child.setData(new SmartString(treeInstance.getName() + " "), ItemDataRole.DisplayRole); // this was needed to force a repaint, a hack but it works...
							child.setData(new SmartString(treeInstance.getName()), ItemDataRole.DisplayRole);
						} else {
							child.setData(getSourceColumnData(c, treeInstance.getRecord()), ItemDataRole.DisplayRole);
						}
					}
				}
				if (log.isTraceEnabled())
					log.trace("updateInstance(): done setting data");			
				treeInstance.setNeedsRefresh(false);
				if (updateChildren)
					updateInstanceChildren(treeInstance, treeItem);
			}
			if (log.isTraceEnabled())
				log.trace("updateInstance(): done");			
		} catch (Exception e) {
			log.error("updateInstance(): error", e);
		}		
	}
	
	private void updateInstanceChildren(TreeHierarchyInstance treeInstance, QStandardItem item) {
		// add any missing children
		if (item == null) return;
		if (log.isTraceEnabled())
			log.trace("updateInstanceChildren(): treeInstance=" + treeInstance + ", item.rowCount()=" + item.rowCount());
		
		for (HierarchicalRecord childRecord : treeInstance.getRecord().getChildRecords()) {
			if (childRecord != null) {
				boolean process = true;
				if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported()) {					
					boolean pass = false;
					if (getProxyModel() instanceof FilterProxyModel) {
						FilterProxyModel filterProxy = (FilterProxyModel)getProxyModel();
						SearchParameters searchParams = filterProxy.getSearchParameters();
						if ((searchParams != null) && !searchParams.isEmpty()) {
							if (searchParams.matches(childRecord) > 0.0f)
								pass = true;
						}
					}
					if (!pass)
						process = false;;			
				}			
				if (process) {
					boolean found = false;
					int r = 0;
					while ((r < item.rowCount() && !found)) {
						QStandardItem child = item.child(r);
						TreeHierarchyInstance childInstance = (TreeHierarchyInstance)child.data();
						if (childInstance.getRecord().equals(childRecord)) {
							found = true;
							updateInstanceChildren(childInstance, item.child(r));
						}
						++r;
					}
					if (!found) {
						TreeHierarchyInstance childInstance = getTreeHierarchyInstance(childRecord, treeInstance);
						item.appendRow(createRow(childInstance));				
						updateInstanceChildren(childInstance, item.child(item.rowCount() - 1));
					}
				}
			}
		}		
	}		
			
	
	/**
	 * Removes instances of the tree, if all instances for a record are removed the record will be returned as part
	 * of a collection to be deleted from the database.
	 * 
	 * Don't call directly in a Java thread, only the (main) GUI thread...
	 */		
	public Vector<HierarchicalRecord> removeInstance(TreeHierarchyInstance treeInstance) {
		return removeInstance(treeInstance, new Vector<Identifier>(0));
	}
	public Vector<HierarchicalRecord> removeInstance(TreeHierarchyInstance treeInstance, Vector<Identifier> idsToAdd) {
		Vector<HierarchicalRecord> removedRecords = new Vector<HierarchicalRecord>();
		try {
			QStandardItem treeItem = getItemMap().get(treeInstance);
			if ((treeItem != null) && treeItem.nativePointer() != null) {
				for (int r = 0; r < treeItem.rowCount(); ++r) {
					TreeHierarchyInstance childInstance = (TreeHierarchyInstance)treeItem.child(r).data();
					Vector<HierarchicalRecord> removedChildren = removeInstance(childInstance, idsToAdd);
					for (HierarchicalRecord removedChild : removedChildren)
						removedRecords.add(removedChild);
				}
				try {
					getItemMapSem().acquire("removeInstance");
					getItemMap().remove(treeInstance);
				} catch (Exception e) {
					log.error("removeInstance(): error", e);
				} finally {
					getItemMapSem().release();
				}
				QStandardItem parent = treeItem.parent();
				if (parent == null)
					parent = ((QStandardItemModel)model).invisibleRootItem();
				int r = 0;
				boolean removed = false;
				while ((r < parent.rowCount()) && !removed) {
					QStandardItem child = parent.child(r);
					if (child != null) {
						if (child.data().equals(treeItem.data())) {
							parent.removeRow(r);
							removed = true;
						}
					}
					++r;
				}
				if (parent.data() instanceof TreeHierarchyInstance) {
					TreeHierarchyInstance parentInstance = (TreeHierarchyInstance)parent.data();
					if (!(idsToAdd.contains(parentInstance.getRecord().getIdentifier()) && idsToAdd.contains(treeInstance.getRecord().getIdentifier())))
						getHierarchicalIndex().removeRelationship(parentInstance.getRecord(), treeInstance.getRecord());
				} else {
					// remove from root
					if (log.isDebugEnabled())
						log.debug("removeInstance(): removing from root=" + treeInstance);
					getHierarchicalIndex().removeRelationship(getHierarchicalIndex().getRootRecord(), treeInstance.getRecord());
				}
				boolean foundOtherInstance = false;
				try {
					getItemMapSem().acquire("removeInstance");
					Iterator<TreeHierarchyInstance> iter = getItemMap().keySet().iterator();
					while (iter.hasNext() && !foundOtherInstance) {
						foundOtherInstance = (iter.next().getRecord().getUniqueId() == treeInstance.getRecord().getUniqueId());
					}
				} catch (Exception e) {
					log.error("removeInstance(): error", e);
				} finally {				
					getItemMapSem().release();
				}
				if (!foundOtherInstance) {
					if (!idsToAdd.contains(treeInstance.getRecord().getIdentifier()))
						removedRecords.add(treeInstance.getRecord());					
				}
			} else {
				if (log.isDebugEnabled())
					log.debug("removeInstance(): native tree pointer is null?");
			}
		} catch (Exception e) {
			log.error("remove(): error removing instance=" + treeInstance, e);
		}		
		return removedRecords;
	}	
	
	/**
	 * Don't call directly in a Java thread, only the (main) GUI thread...
	 */			
	public void updateHierarchy(QStandardItem sourceItem, TreeHierarchyInstance sourceInstance, QStandardItem destinationItem, TreeHierarchyInstance destinationInstance, boolean copy) {
		updateHierarchy(sourceItem, sourceInstance, destinationItem, destinationInstance, copy, false);
	}
	
	public void updateHierarchy(QStandardItem sourceItem, TreeHierarchyInstance sourceInstance, QStandardItem destinationItem, TreeHierarchyInstance destinationInstance, boolean copy, boolean secondaryTree) {
		try {
			if (log.isDebugEnabled())
				log.debug("updateHierarchy(): sourceItem=" + sourceItem + ", sourceInstance=" + sourceInstance + ", destinationItem=" + destinationItem + ", destinationInstance=" + destinationInstance + ", copy=" + copy);
			// check to make sure this is a valid update
			HierarchicalRecord destinationRecord = (destinationInstance != null) ? destinationInstance.getRecord() : getHierarchicalIndex().getRootRecord();
			HierarchicalRecord sourceRecord = sourceInstance.getRecord();
			if (sourceRecord.equals(destinationRecord))
				return;
			if (destinationRecord.isChildOf(sourceRecord))
				return;			
			if (!copy) {
				// move event, remove the original instance...
				Vector<Identifier> idsToAdd = new Vector<Identifier>();
				addRecordAndAllChildren(sourceInstance.getRecord(), idsToAdd);
				removeInstance(sourceInstance, idsToAdd);
			}
			// proceed with update
			if (destinationInstance != null) {				
				boolean alreadyExists = destinationInstance.getRecord().containsChildDirectly(sourceInstance.getRecord());
				if (!alreadyExists) {
					// update data model
					getHierarchicalIndex().addRelationship(destinationInstance.getRecord(), sourceInstance.getRecord());
				}
			} else {
				// add to root
				destinationItem = ((QStandardItemModel)model).invisibleRootItem();
				boolean alreadyExists = getHierarchicalIndex().getRootRecord().containsChildDirectly(sourceInstance.getRecord());
				if (!alreadyExists) {
					// update data model
					getHierarchicalIndex().addRelationship(getHierarchicalIndex().getRootRecord(), sourceInstance.getRecord());
				}
			}	
			boolean alreadyExistsInUI = false;
			for (int c = 0; c < destinationItem.rowCount(); ++c) {
				QStandardItem child = destinationItem.child(c);
				if (child != null) {
					TreeHierarchyInstance instance = (TreeHierarchyInstance)child.data();
					if (instance.getRecord().equals(sourceInstance.getRecord())) {
						alreadyExistsInUI = true;
						instance.setSelectionState(sourceInstance.getSelectionState());
					}
				}
			}
			if (!alreadyExistsInUI) {
				if (destinationInstance != null) {
					Vector<TreeHierarchyInstance> allTreeInstances = getMatchingInstances(destinationInstance.getRecord());
					for (TreeHierarchyInstance inst : allTreeInstances) {
						TreeHierarchyInstance newInstance = getTreeHierarchyInstance(sourceInstance.getRecord(), inst);
						newInstance.setSelectionState(sourceInstance.getSelectionState());
						if (log.isTraceEnabled())
							log.trace("updateHierarchy(): appending row...");
						QStandardItem instItem = getItemMap().get(inst);
						instItem.appendRow(createRow(newInstance));
						if (log.isTraceEnabled())
							log.trace("updateHierarchy(): appended");				
						for (HierarchicalRecord child : sourceInstance.getRecord().getChildRecords()) {
							TreeHierarchyInstance childInstance = getTreeHierarchyInstance(child, newInstance);
							addInstance(childInstance);
						}				
					}
				} else {
					TreeHierarchyInstance newInstance = getTreeHierarchyInstance(sourceInstance.getRecord(), null);
					newInstance.setSelectionState(sourceInstance.getSelectionState());
					if (log.isTraceEnabled())
						log.trace("updateHierarchy(): appending row...");
					QStandardItem instItem = ((QStandardItemModel)model).invisibleRootItem();
					instItem.appendRow(createRow(newInstance));
					if (log.isTraceEnabled())
						log.trace("updateHierarchy(): appended");				
					for (HierarchicalRecord child : sourceInstance.getRecord().getChildRecords()) {
						TreeHierarchyInstance childInstance = getTreeHierarchyInstance(child, newInstance);
						addInstance(childInstance);
					}									
				}
			}
		} catch (Exception e) {
			log.error("updateHierarchy(): error", e);
		}
	}	
	
	/**
	 * Don't call directly in a Java thread, only the (main) GUI thread...
	 */		
	public void resetModel() {
		try {
			getItemMapSem().acquire("resetModel");		
			getItemMap().clear();
		} catch (Exception e) {
			log.error("resetModel(): error", e);
		} finally {
			getItemMapSem().release();
		}
		getTreeItemModel().resetData();
		loadTree();
	}
	
	public void refresh() {
		Vector<TreeHierarchyInstance> treeInstances = getAllInstancesToRefresh();
		for (TreeHierarchyInstance treeInstance : treeInstances)
			updateInstance(treeInstance);
	}
		
	protected void addRecordAndAllChildren(HierarchicalRecord record, Vector<Identifier> results) {
		results.add(record.getIdentifier());
		for (HierarchicalRecord child : record.getChildRecords())
			addRecordAndAllChildren(child, results);
	}	

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
