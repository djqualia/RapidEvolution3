package com.mixshare.rapid_evolution.ui.model.table;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.ui.model.ModelPopulatorInterface;
import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.search.SearchProxyModel;
import com.mixshare.rapid_evolution.ui.updaters.model.table.TableModelRowUpdater;
import com.mixshare.rapid_evolution.util.debug.StackTraceLogger;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QApplication;

abstract public class RecordTableModelManager extends TableModelManager {

    static private Logger log = Logger.getLogger(RecordTableModelManager.class);    
	
    static private long TABLE_UPDATE_TIMEOUT_MILLIS = RE3Properties.getLong("table_update_sem_timeout_millis");
    
    ////////////
    // FIELDS //
    ////////////
    	
	protected transient Map<Integer, Integer> idMap = new HashMap<Integer, Integer>();
	protected transient RWSemaphore sem = new RWSemaphore(-1);
	
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(RecordTableModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("idMap") || pd.getName().equals("sem")) {
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
    
    public RecordTableModelManager() { super(); }
	public RecordTableModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}	    
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public ModelPopulatorInterface getModelPopulator();		
	
	abstract public Index getIndex();
	
	abstract public boolean excludeExternalItems();
	abstract public boolean excludeInternalItems();
	
	abstract public boolean isLazySearchSupported();
	
	abstract public boolean shouldAddToView(Record record);
	
	/////////////
	// GETTERS //
	/////////////
	
	public int getNumRows() {
		return getRowObjects().size();
	}
	public Record getRecordForRow(int row) {
		if (row < getRowObjects().size())
			return (Record)getRowObjects().get(row);
		return null;
	}
	public Integer getRowForUniqueId(Integer uniqueId) { return getIdMap().get(uniqueId); }		
	
	private RWSemaphore getSemaphore() {
		if (sem == null)
			sem = new RWSemaphore(-1);
		return sem;
	}
	
	public Map<Integer, Integer> getIdMap() {
		if (idMap == null)
			idMap = new HashMap<Integer, Integer>((RE3Properties.getBoolean("lazy_search_mode") && isLazySearchSupported()) ? 0 : getModelPopulator().getSize());
		return idMap;
	}		
	
	protected Vector<Object> getRowObjects() {
		if (rowObjects == null)
			rowObjects = new Vector<Object>((RE3Properties.getBoolean("lazy_search_mode") && isLazySearchSupported()) ? 0 : getModelPopulator().getSize());
		return rowObjects;
	}
		
	/////////////
	// METHODS //
	/////////////
	
	public void initialize(QObject parent) {
		if (idMap != null)
			idMap.clear();
		super.initialize(parent);
	}	
	
	protected void createSourceModel(QObject parent) {
		model = new TableItemModel(getModelPopulator().getSize(), getNumColumns(), parent, this);
		loadTable();
	}
	
	public void loadData(Vector<SearchResult> results, SearchParameters searchParameters) {
		if (log.isDebugEnabled())
			log.debug("loadData(): # results=" + results.size());
		resetModel(results.size(), false);
		if (log.isDebugEnabled())
			log.debug("loadData(): reset model...");
		int row = 0;
		for (SearchResult result : results) {
			getIdMap().put(result.getRecord().getUniqueId(), row);
			getRowObjects().add(result.getRecord());
			populateRow(result.getRecord(), row, false);
			++row;			
		}
		if (log.isDebugEnabled())
			log.debug("loadData(): done");
	}
	
	protected void loadTable() {
		if (log.isDebugEnabled())
			log.debug("init(): loading " + getTypeDescription() + " model");
		initViewColumns();
		for (int c = 0; c < getNumColumns(); ++c)
			model.setHeaderData(c, Qt.Orientation.Horizontal, getSourceColumnTitle(c));
		if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported())
			return;
		// load initial data
		Iterator<Integer> initialIds = getModelPopulator().getIdsIterator();
		int row = 0;
		while (initialIds.hasNext()) {
			Integer uniqueId = initialIds.next();
			Record record = getIndex().getRecord(uniqueId);
			if (record != null) {
				getIdMap().put(record.getUniqueId(), row);
				getRowObjects().add(record);
				populateRow(record, row);
				++row;
			} else {
				log.error("loadTable(): no record for id=" + uniqueId + StackTraceLogger.getStackTrace());
				model.removeRow(model.rowCount() - 1);
			}			
		}
		if (log.isDebugEnabled())
			log.debug("init(): done, # rows=" + row);		
	}	
	
	public void refreshColumn(Column column) {
		int sourceColumn = getSourceColumnIndex(column);
		if (sourceColumn >= 0) {
			TableItemModel tableModel = getTableItemModel();		
			if (tableModel != null) {
				for (int r = 0; r < tableModel.rowCount(); ++r) {
					Record record = getRecordForRow(r);
					tableModel.setDisplayData(r, sourceColumn, getSourceData(column.getColumnId(), record), false);
				}
				// NOTE: omitting the dataChanged did not produce any negative side effects, since this method
				// is used to refresh the style/tag/filter match (when filter selections changed), which causes an invalidation anyhow...
				// if any problems with the values or sorting of these columns should arise, this method might need to be uncommented...
				//tableModel.emitColumnDataChanged(sourceColumn);  
			}
		}
	}
	
	////////////
	// EVENTS //
	////////////
	
	public void addedRecord(Record record) { addedRecord(record, null); }
	public void addedRecord(Record record, SubmittedProfile submittedProfile) {
		if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported()) {
			boolean pass = false;
			if (getProxyModel() instanceof SearchProxyModel) {
				SearchProxyModel searchProxy = (SearchProxyModel)getProxyModel();
				SearchParameters searchParams = searchProxy.getSearchParameters();
				if ((searchParams != null) && !searchParams.isEmpty()) {
					if (searchParams.matches(record) > 0.0f)
						pass = true;
				}
			}
			if (!pass)
				return;
		}
		try {
			getSemaphore().startRead("addedRecord");
			if (record instanceof SearchRecord) {
				SearchRecord searchRecord = (SearchRecord)record;
				if (searchRecord.isExternalItem() && excludeExternalItems())
					return;
				if (!searchRecord.isExternalItem() && excludeInternalItems())
					return;			
			}
			if (getRowForUniqueId(record.getUniqueId()) != null) {
				// if the record already exists...
				QApplication.invokeAndWait(new TableModelRowUpdater(record, this, TableModelRowUpdater.ACTION_UPDATE));
			} else {
				// if the record doesn't exist yet...
				QApplication.invokeAndWait(new TableModelRowUpdater(record, this, TableModelRowUpdater.ACTION_ADD));
			}
		} catch (Exception e) {
			log.error("addedRecord(): error", e);
		} finally {
			getSemaphore().endRead();
		}
	}
	public void updatedRecord(Record record) {
		try {
			getSemaphore().startRead("updatedRecord");			
			if (record instanceof SearchRecord) {
				SearchRecord searchRecord = (SearchRecord)record;
				if (searchRecord.isExternalItem() && excludeExternalItems())
					return;
				if (!searchRecord.isExternalItem() && excludeInternalItems()) {
					// check if it was an external item that was now added, if so remove it
					if (getRowForUniqueId(record.getUniqueId()) != null)
						QApplication.invokeAndWait(new TableModelRowUpdater(record, this, TableModelRowUpdater.ACTION_REMOVE));				
					return;			
				}
			}
			if (getRowForUniqueId(record.getUniqueId()) != null) {
				// if the record already exists...
				QApplication.invokeAndWait(new TableModelRowUpdater(record, this, TableModelRowUpdater.ACTION_UPDATE));
			} else {
				if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && isLazySearchSupported()) {
					if ((idMap.size() >= RE3Properties.getInt("lazy_search_mode_max_results")) || !shouldAddToView(record))
						return;
				}
				// if the record doesn't exist yet...
				QApplication.invokeAndWait(new TableModelRowUpdater(record, this, TableModelRowUpdater.ACTION_ADD));
			}			
		} catch (Exception e) {
			log.error("updatedRecord(): error", e);
		} finally {
			getSemaphore().endRead();			
		}
	}
	public void removedRecord(Record record) {
		try {
			getSemaphore().startWrite("removedRecord");
			if (record instanceof SearchRecord) {
				SearchRecord searchRecord = (SearchRecord)record;
				if (searchRecord.isExternalItem() && excludeExternalItems())
					return;
				if (!searchRecord.isExternalItem() && excludeInternalItems())
					return;			
			}		
			QApplication.invokeAndWait(new TableModelRowUpdater(record, this, TableModelRowUpdater.ACTION_REMOVE));
		} catch (Exception e) {
			log.error("removedRecord(): error", e);
		} finally {
			getSemaphore().endWrite();			
		}			
	}	
	
	/**
	 * Don't call directly (in a Java thread), call add(...)
	 */
	public void addRow(Record record) {
		try {
			int row = model.rowCount();
			getIdMap().put(record.getUniqueId(), new Integer(row));
			getRowObjects().add(record);
			model.insertRow(row);
			populateRow(record, row);
		} catch (Exception e) {
			log.error("addRow(): error", e);			
		}
	}
	
	/**
	 * Don't call directly (in a Java thread), call update(...)
	 */	
	public void updateRow(Record record) {
		try {
			Integer row = getRowForUniqueId(record.getUniqueId());		
			if (row != null)
				populateRow(record, row);			
		} catch (Exception e) {
			log.error("updateRow(): error", e);
		}
	}
	
	/**
	 * Don't call directly (in a Java thread), call remove(...)
	 */	
	public void removeRow(Record record) {
		try {
			Integer row = getRowForUniqueId(record.getUniqueId());		
			if (row != null) {
				model.removeRow(row.intValue());
				getRowObjects().remove(row.intValue());
				getIdMap().remove(record.getUniqueId());
				Iterator<Entry<Integer, Integer>> iter = getIdMap().entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Integer, Integer> entry = iter.next();
					int entryRow = entry.getValue();
					if (entryRow > row)
						entry.setValue(entryRow - 1);
				}
			}
		} catch (Exception e) {
			log.error("removeRow(): error", e);
		}
	}
	
	/**
	 * Don't call directly (in a Java thread), call remove(...)
	 */		
	public void resetModel(int modelSize) {
		resetModel(modelSize, true);
	}
	public void resetModel(int modelSize, boolean loadTable) {
		getIdMap().clear();
		getRowObjects().clear();
		if (model != null) {
			getTableItemModel().resetData();
			if (modelSize > model.rowCount())
				model.insertRows(model.rowCount(), modelSize - model.rowCount());
			if (modelSize < model.rowCount())
				model.removeRows(modelSize, model.rowCount() - modelSize);
			if (loadTable)
				loadTable();
		}
	}
	public void resetModel() { resetModel(getModelPopulator().getSize()); }

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
