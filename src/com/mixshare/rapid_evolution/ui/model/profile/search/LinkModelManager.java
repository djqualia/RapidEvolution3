package com.mixshare.rapid_evolution.ui.model.profile.search;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.table.TableItemModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;

public class LinkModelManager extends TableModelManager {

    static private Logger log = Logger.getLogger(LinkModelManager.class);	
    static private final long serialVersionUID = 0L;    
    
    static public StaticTypeColumn[] ALL_COLUMNS = {     	
    	COLUMN_LINK_TITLE.getInstance(true),
    	COLUMN_LINK_DESCRIPTION.getInstance(false),
    	COLUMN_LINK_TYPE.getInstance(true),
    	COLUMN_LINK_URL.getInstance(true),
    	COLUMN_LINK_SOURCE.getInstance(false)    	
    };
    
    ////////////
    // FIELDS //
    ////////////
    
    transient private Vector<Link> links;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(LinkModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("links")) {
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
    
	public LinkModelManager() { }
	
	public LinkModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}		
	
	public void initColumns() {
		sourceColumns.clear();
		for (Column column : ALL_COLUMNS)
			sourceColumns.add(column);		
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public StaticTypeColumn[] getAllStaticColumns() { return ALL_COLUMNS; }
	
	public String getTypeDescription() { return "Link"; }
	
	public Object getSourceData(short columnId, Object obj) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", obj=" + obj);		
		Link link = (Link)obj;
		if (columnId == COLUMN_LINK_TITLE.getColumnId())
			return link.getTitle();
		else if (columnId == COLUMN_LINK_DESCRIPTION.getColumnId())
			return link.getDescription();
		else if (columnId == COLUMN_LINK_TYPE.getColumnId())
			return link.getType();
		else if (columnId == COLUMN_LINK_URL.getColumnId())
			return link.getUrl();
		else if (columnId == COLUMN_LINK_SOURCE.getColumnId())
			return DataConstantsHelper.getDataSourceDescription(link.getDataSource());
		return null;
	}
		
	public int getSize() { return links.size(); }
	
	public Link getLinkForRow(int row) {
		return links.get(row);
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setLinks(Vector<Link> links) {
		this.links = links;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	protected void createSourceModel(QObject parent) {
		model = new TableItemModel(links.size(), getNumColumns(), parent, this);
		loadTable();
	}
	
	protected void loadTable() {
		if (log.isDebugEnabled())
			log.debug("init(): loading " + getTypeDescription() + " model");
		initViewColumns();
		for (int c = 0; c < getNumColumns(); ++c)
			model.setHeaderData(c, Qt.Orientation.Horizontal, getSourceColumnTitle(c));		
		// load initial data
		for (int row = 0; row < links.size(); ++row) {
			Link link = links.get(row);
			getRowObjects().add(link);
			populateRow(link, row);
		}
		if (log.isDebugEnabled())
			log.debug("init(): done");		
	}	
	
	/**
	 * Don't call directly (in a Java thread), call remove(...)
	 */		
	public void resetModel() {
		getRowObjects().clear();
		getTableItemModel().resetData();
		int modelSize = links.size();
		if (modelSize > model.rowCount())
			model.insertRows(model.rowCount(), modelSize - model.rowCount());
		if (modelSize < model.rowCount())
			model.removeRows(modelSize, model.rowCount() - modelSize);
		loadTable();
	}	

	/////////////
	// METHODS //
	/////////////
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}	

}
