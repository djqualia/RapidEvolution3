package com.mixshare.rapid_evolution.ui.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.model.column.CommonColumn;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.UserDataColumn;
import com.mixshare.rapid_evolution.ui.updaters.model.ModelResetter;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QSortFilterProxyModel;

abstract public class CommonModelManager extends AbstractModelManager implements Serializable, AllColumns, DataConstants {

    static private Logger log = Logger.getLogger(CommonModelManager.class);    
	
    static private boolean extendedSortEnabled = RE3Properties.getBoolean("enable_sub_sorting");
    
    ////////////
    // FIELDS //
    ////////////
    	
	protected byte nextUserColumnId = Byte.MIN_VALUE;			
	protected Vector<Column> sourceColumns = new Vector<Column>();
	protected Vector<Column> viewColumns = new Vector<Column>(); // keeps track of column ordering changes without altering the source model
	protected Vector<ColumnOrdering> sortOrdering = new Vector<ColumnOrdering>();
	
	transient protected QAbstractItemModel model = null;
	transient protected QSortFilterProxyModel proxyModel = null;
			
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(CommonModelManager.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("model") || pd.getName().equals("proxyModel")) {
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
	
	public CommonModelManager() { }	
	public CommonModelManager(LineReader lineReader) {
		int version = Integer.parseInt(lineReader.getNextLine());
		nextUserColumnId = Byte.parseByte(lineReader.getNextLine());
		int numSourceColumns = Integer.parseInt(lineReader.getNextLine());
		for (int i = 0; i < numSourceColumns; ++i)
			sourceColumns.add(CommonColumn.readColumn(lineReader));
		int numViewColumns = Integer.parseInt(lineReader.getNextLine());
		for (int i = 0; i < numViewColumns; ++i)
			viewColumns.add(CommonColumn.readColumn(lineReader));
		int numSortOrderings = Integer.parseInt(lineReader.getNextLine());
		for (int i = 0; i < numSortOrderings; ++i)
			sortOrdering.add(new ColumnOrdering(lineReader));
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public void initColumns();
	abstract protected void createSourceModel(QObject parent);
	abstract public void resetModel();
	
	abstract public StaticTypeColumn[] getAllStaticColumns();
	
	abstract public Object getSourceData(short columnId, Object record);
	
	abstract public void refresh();
						
	/////////////
	// GETTERS //
	/////////////
		
	public QAbstractItemModel getSourceModel() { return model; }				
	public QSortFilterProxyModel getProxyModel() { return proxyModel; }
		
	public int getNumColumns() { return sourceColumns.size(); }
	public int getNumVisibleColumns() {
		int numVisibleColumns = 0;
		for (Column column : sourceColumns) {
			if (!column.isHidden())
				++numVisibleColumns;
		}
		return numVisibleColumns;
	}
	public boolean isColumnVisible(Column column) {
		for (Column sourceColumn : sourceColumns) {
			if (sourceColumn.equals(column))
				return !sourceColumn.isHidden();
		}
		return false;
	}
	
	public Vector<ColumnOrdering> getSortOrdering() { return sortOrdering; }	
	public ColumnOrdering getPrimarySortColumnOrdering() { return sortOrdering.get(0); }
	
	// source model
	public Vector<Column> getSourceColumnOrder() { return sourceColumns; }
	public Vector<Column> getSourceColumnOrderCopy() {
		Vector<Column> result = new Vector<Column>(sourceColumns.size());
		for (Column column : sourceColumns)
			result.add(column);
		return result;
	}
	public String getSourceColumnTitle(int index) {
		Column type = getSourceColumnOrder().get(index);
		return type.getColumnTitle();
	}	
	public Column getSourceColumnType(int index) { return getSourceColumnOrder().get(index); }
	public int getSourceColumnIndex(Column column) {
		int i = 0;
		for (Column sourceColumn : getSourceColumnOrder()) {
			if (sourceColumn.equals(column))
				return i;
			++i;
		}
		return -1;
	}
	public Object getSourceColumnData(int index, Object obj) { return getSourceData(getSourceColumnOrder().get(index).getColumnId(), obj); }			
	public boolean isSourceColumnTypeImage(int index) { return (getSourceColumnType(index).getColumnId() == COLUMN_THUMBNAIL_IMAGE.getColumnId()); }
	
	// view source
	public Vector<Column> getViewColumnOrder() { return viewColumns; }
	public String getViewColumnTitle(int index) {
		Column type = getViewColumnOrder().get(index);
		return type.getColumnTitle();
	}	
	public int getViewColumnIndex(Column column) {
		int i = 0;
		for (Column viewColumn : getViewColumnOrder()) {
			if (viewColumn.equals(column))
				return i;
			++i;
		}
		return -1;
	}
	public Column getViewColumnType(int index) { return getViewColumnOrder().get(index); }		
		
	public boolean isExtendedSortEnabled() { return extendedSortEnabled; }
		
	
	// for serialization
	public byte getNextUserColumnId() { return nextUserColumnId; }
	public Vector<Column> getViewColumns() { return viewColumns; }
	public Vector<Column> getSourceColumns() { return sourceColumns; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setProxyModel(QSortFilterProxyModel proxyModel) { this.proxyModel = proxyModel; }
	
	public void setPrimarySortColumn(short columnId) {
		if (log.isDebugEnabled())
			log.debug("setPrimarySortColumn(): columnId=" + columnId + ", type=" + this.getTypeDescription());		
		if (sortOrdering.size() > 0) {
			ColumnOrdering primaryOrder = sortOrdering.get(0);
			if (primaryOrder.getColumnId() == columnId) {
				primaryOrder.setAscending(!primaryOrder.isAscending());
				return;
			}
			int i = 0;
			boolean found = false;
			while ((i < sortOrdering.size()) && !found) {
				if (sortOrdering.get(i).getColumnId() == columnId) {
					found = true;
					sortOrdering.remove(i);
				}
				++i;
			}
		}		
		sortOrdering.insertElementAt(new ColumnOrdering(columnId), 0);
	}	
	
	// for serialization
	public void setSourceColumns(Vector<Column> sourceColumns) { this.sourceColumns = sourceColumns; }
	public void setNextUserColumnId(byte nextUserColumnId) { this.nextUserColumnId = nextUserColumnId; }
	public void setViewColumns(Vector<Column> viewColumns) { this.viewColumns = viewColumns; }
	public void setSortOrdering(Vector<ColumnOrdering> sortOrdering) { this.sortOrdering = sortOrdering; }
	
	/////////////
	// METHODS //
	/////////////			
	
	public void initialize(QObject parent) {
		try {
			initialize();
			createSourceModel(parent);
		} catch (Exception e) {
			log.error("initialize(): error", e);
		}
	}
	
	public void initialize() {
		try {
			if (log.isTraceEnabled())
				log.trace("initialize(): type=" + getTypeDescription() + ", viewColumns=" + viewColumns);
			if (viewColumns.size() != 0) {
				int staticColumnIndex = 0;
				// check for new static columns
				for (Column staticColumn : getAllStaticColumns()) {
					if (!viewColumns.contains(staticColumn)) {
						// add the new column, by looking at the previous column in the source, and inserting after that in the view
						int previousStaticColumnIndex = staticColumnIndex - 1;
						boolean inserted = false;
						if (previousStaticColumnIndex >= 0) {
							Column previousStaticColumn = getAllStaticColumns()[previousStaticColumnIndex];
							int viewIndex = viewColumns.indexOf(previousStaticColumn);
							if (viewIndex >= 0) {
								inserted = true;
								viewColumns.insertElementAt(staticColumn, viewIndex + 1);
							}
						}
						if (!inserted)
							viewColumns.insertElementAt(staticColumn, 0);							
					} else {
						Column existingColumn = null;
						for (Column column : viewColumns) {
							if (column.equals(staticColumn)) {
								existingColumn = column;
								break;
							}
						}
						if (existingColumn != null) {
							existingColumn.setClearable(staticColumn.isClearable());
						}
					}
					++staticColumnIndex;
				}
				// check for removed static columns
				for (int i = 0; i < viewColumns.size(); ++i) {
					Column viewColumn = viewColumns.get(i);
					if (!(viewColumn instanceof UserDataColumn)) {
						boolean contains = false;
						for (Column staticColumn : getAllStaticColumns()) {
							if (staticColumn.equals(viewColumn)) {
								contains = true;
								break;
							}
						}
						if (!contains) {
							viewColumns.remove(i);
							--i;
						}
					}
				}
				// restore column ordering from previous session
				sourceColumns.clear();
				for (Column column : viewColumns)
					sourceColumns.add(column);
			} else {
				for (Column column : sourceColumns)
					viewColumns.add(column);
			}
			if (log.isTraceEnabled())
				log.trace("initialize(): viewColumns after=" + viewColumns);			
		} catch (Exception e) {
			log.error("initialize(): error", e);
		}
	}

	public void resetSourceColumns() {
		sourceColumns.clear();
		for (Column column : viewColumns)
			sourceColumns.add(column);
		reset();
	}		
	
	public void reset() {
		QApplication.invokeAndWait(new ModelResetter(this)); // will call resetModel() in GUI thread
	}		
	
	/**
	 * Called when the model is created, this will initialize the view columns to match the source the first time the model is created
	 */
	public void initViewColumns() {
		if (viewColumns.size() == 0) {
			for (Column column : sourceColumns)
				viewColumns.add(column);
		}
	}	
		
	////////////
	// EVENTS //
	////////////
	
	/**
	 * This is called whenever the user resizes a column.  The new size is recorded so it can be restored properly next session...
	 * 
	 * Note: seems to be called not just when the user changes the column size...
	 */
	public void columnResized(Integer sourceIndex, Integer oldSize, Integer newSize) {
		if (newSize == 0)
			return;
		if (!RapidEvolution3.isLoaded)
			return;
		if ((oldSize != 0) && (newSize != oldSize)) { // seems to filter out non-user resize events
			if (log.isTraceEnabled())
				log.trace("columnResized(): sourceIndex=" + sourceIndex + ", newSize=" + newSize + ", oldSize=" + oldSize);
			int index = 0;
			for (int c = 0; c < getNumColumns(); ++c) {
				//if (!getSourceColumnType(c).isHidden()) {
					if (index == sourceIndex.intValue()) {
						if (log.isTraceEnabled())
							log.trace("columnResized(): resizing column=" + getSourceColumnType(c));
						getSourceColumnType(c).setSize((short)newSize.intValue());
						//this.getProxyModel().layoutChanged.emit();
						getProxyModel().invalidate();
						return;
					}
					++index;
				//}
			}
		}
	}
	
	/**
	 * This is called whenever a column is dragged and re-ordered by the user.  The corresponding VIEW_COLUMNS array must be 
	 * updated with the re-ordering so that the new column ordering can be restored in the next session.
	 */
	public void columnMoved(Integer sourceIndex, Integer oldVisualIndex, Integer newVisualIndex) {
		if (log.isTraceEnabled())
			log.trace("columnMoved(): sourceIndex=" + sourceIndex + ", oldVisualIndex=" + oldVisualIndex + ", newVisualIndex=" + newVisualIndex);
		int movedFromIndex = 0;		
		for (int c = 0; c < getNumColumns(); ++c) {
			//if (!viewColumns.get(c).isHidden()) {
				if (movedFromIndex == oldVisualIndex) {
					movedFromIndex = c;
					int movedToIndex = 0;
					for (c = 0; c < getNumColumns(); ++c) {
						//if (!viewColumns.get(c).isHidden()) {
							if (movedToIndex == newVisualIndex) {
								movedToIndex = c;
								if (log.isTraceEnabled())
									log.trace("columnMoved(): moving from=" + viewColumns.get(movedFromIndex).getColumnTitle() + ", to=" + viewColumns.get(movedToIndex).getColumnTitle());
								if (newVisualIndex > oldVisualIndex)
									++movedToIndex; // insert after
								Column removed = viewColumns.remove(movedFromIndex);
								if (movedFromIndex < movedToIndex)
									--movedToIndex;
								viewColumns.insertElementAt(removed, movedToIndex);
								return;
							}
							++movedToIndex;
						//}
					}
				}					
				++movedFromIndex;
			}
		//}	
	}
	
	public void write(LineWriter writer) {
		writer.writeLine(1); // version
		writer.writeLine(nextUserColumnId);
		writer.writeLine(sourceColumns.size());
		for (Column sourceColumn : sourceColumns)
			CommonColumn.writeColumn(sourceColumn, writer);
		writer.writeLine(viewColumns.size());
		for (Column viewColumn : viewColumns)
			CommonColumn.writeColumn(viewColumn, writer);
		writer.writeLine(sortOrdering.size());
		for (ColumnOrdering ordering : sortOrdering)
			ordering.write(writer);
	}
	
	
}
