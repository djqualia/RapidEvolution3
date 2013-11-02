package com.mixshare.rapid_evolution.ui.model.table;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.ui.model.CommonModelManager;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.util.ThumbnailImageFactory;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QTableView;

abstract public class TableModelManager extends CommonModelManager {

    static private Logger log = Logger.getLogger(TableModelManager.class);    
	
    ////////////
    // FIELDS //
    ////////////
    	
	protected transient Vector<Object> rowObjects = new Vector<Object>();	
	
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(TableModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("rowObjects")) {
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
	
    public TableModelManager() { super(); }
    public TableModelManager(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }
    
	////////////////////
	// INITIALIZATION //
	////////////////////
	
	public void initialize(QObject parent) {
		if (rowObjects != null)
			rowObjects.clear();
		super.initialize(parent);
	}
	
	protected void populateRow(Object obj, int row) {
		populateRow(obj, row, true);
	}
	
	protected void populateRow(Object obj, int row, boolean fireChanged) {
		if (log.isTraceEnabled())
			log.trace("populateRow(): row=" + row + ", obj=" + obj);
		for (int c = 0; c < getNumColumns(); ++c) {
			if (c < getTableItemModel().columnCount())
				getTableItemModel().setDisplayData(row, c, getSourceColumnData(c, obj), fireChanged);
		}
	}		
	
	/////////////
	// GETTERS //
	/////////////
		
	public Object getObjectForRow(int row) { return getRowObjects().get(row); }
		
	protected Vector<Object> getRowObjects() {
		if (rowObjects == null)
			rowObjects = new Vector<Object>();
		return rowObjects;
	}

	public TableItemModel getTableItemModel() { return (TableItemModel)model; }	
	
	/////////////	
	// SETTERS //
	/////////////
	
	/**
	 * Called when the view is constructed, to set the initial column sizes
	 */
	public void setSourceColumnSizes(QTableView tableView) {
		int index = 0;
		boolean containsImage = false;
		for (int c = 0; c < getNumColumns(); ++c) {
			Column column = getSourceColumnType(c);
			//if (!column.isHidden()) {
				tableView.horizontalHeader().sectionResized.disconnect(this, "columnResized(Integer,Integer,Integer)");
				tableView.setColumnWidth(index, column.getSize());
				tableView.horizontalHeader().sectionResized.connect(this, "columnResized(Integer,Integer,Integer)");
				++index;
			//}
			if (!column.isHidden() && isSourceColumnTypeImage(c))
				containsImage = true;				
		}
		if (containsImage) {
			tableView.verticalHeader().setDefaultSectionSize(ThumbnailImageFactory.THUMBNAIL_SIZE.height());
		} else {
			tableView.verticalHeader().setDefaultSectionSize(RE3Properties.getInt("search_table_default_row_size"));
		}
		setSourceColumnVisibilities(tableView);
	}		
	
	public void setSourceColumnVisibilities(QTableView tableView) {
		int i = 0;
		for (Column column : getSourceColumnOrder())
			tableView.setColumnHidden(i++, column.isHidden());				
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void refresh() {
		// TODO: implement if ever needed
	}	
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); //version
	}
	
}
