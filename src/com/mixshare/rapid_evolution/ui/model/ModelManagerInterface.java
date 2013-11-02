package com.mixshare.rapid_evolution.ui.model;

import java.util.Vector;

import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.gui.QSortFilterProxyModel;

/**
 * There are many different types of tables in RE3 (search tables for artists, releases,
 * songs, similar items tables, tables to show releases by artists/labels, etc).  This class
 * controls the column data, ordering, sizing, etc for each table.  It is an added level
 * of abstraction over the pure data model layer.  Some tables will contain relative information
 * that is not in the data model itself...
 */
public interface ModelManagerInterface {

	//////////////////
	// INIT METHODS //
	//////////////////
	
	public void initColumns();
	public void reset();

	/////////////
	// GETTERS //
	/////////////
	
	public String getTypeDescription();	

	public QAbstractItemModel getSourceModel();	
	public QSortFilterProxyModel getProxyModel();
	
	public int getNumColumns();
	public int getNumVisibleColumns();

	public Vector<ColumnOrdering> getSortOrdering();
	public ColumnOrdering getPrimarySortColumnOrdering();

	// source column queries
	public String getSourceColumnTitle(int index);	
	public Object getSourceColumnData(int index, Object record);
	public Object getSourceData(short columnId, Object record);	
	public Column getSourceColumnType(int index);
	public int getSourceColumnIndex(Column column);
	public Vector<Column> getSourceColumnOrder();
	public boolean isSourceColumnTypeImage(int index);
	public boolean isColumnVisible(Column column);
	
	// view column queries
	public String getViewColumnTitle(int index);
	public int getViewColumnIndex(Column column);
	public Column getViewColumnType(int index);
	public Vector<Column> getViewColumnOrder();
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setProxyModel(QSortFilterProxyModel proxyModel);			
	
	public void setPrimarySortColumn(short columnId);
	public boolean isExtendedSortEnabled();
	
	public void setSortOrdering(Vector<ColumnOrdering> sortOrdering);

	////////////
	// EVENTS //
	////////////		
	
	// UI events
	public void columnResized(Integer logicalIndex, Integer oldSize, Integer newSize);	
	public void columnMoved(Integer logicalIndex, Integer oldVisualIndex, Integer newVisualIndex);
	
	/////////////
	// METHODS //
	/////////////

	public void write(LineWriter write);
	
}
