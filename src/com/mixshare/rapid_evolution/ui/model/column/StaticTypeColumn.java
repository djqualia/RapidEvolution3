package com.mixshare.rapid_evolution.ui.model.column;

import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * These are pre-defined columns defined in the code (as opposed to user defined).
 */
public class StaticTypeColumn extends CommonColumn {
	
    static private final long serialVersionUID = 0L;    	
	
    ////////////
    // FIELDS //
    ////////////
    
	private String columnTitleKey;
	private String columnDescriptionKey;
	private boolean isIdColumn; // does it uniquely idenfies a record?
	private boolean isGroupEditable;
	private boolean isClearable;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////

	public StaticTypeColumn() { super(); }
	
	public StaticTypeColumn(short columnId, String columnTitleKey, String columnDescriptionKey, boolean hidden, short size, boolean isIdColumn) {
		super(columnId, hidden, size);
		this.columnTitleKey = columnTitleKey;		
		this.columnDescriptionKey = columnDescriptionKey;
		this.isIdColumn = isIdColumn;
	}
	
	public StaticTypeColumn(short columnId, String columnTitleKey, String columnDescriptionKey, boolean hidden, short size) {
		super(columnId, hidden, size);
		this.columnTitleKey = columnTitleKey;		
		this.columnDescriptionKey = columnDescriptionKey;
	}

	public StaticTypeColumn(int columnId, String columnTitleKey, String columnDescriptionKey, boolean hidden, short size, boolean isIdColumn) {
		super((short)columnId, hidden, size);
		this.columnTitleKey = columnTitleKey;		
		this.columnDescriptionKey = columnDescriptionKey;
		this.isIdColumn = isIdColumn;
	}

	public StaticTypeColumn(int columnId, String columnTitleKey, String columnDescriptionKey, boolean hidden, short size) {
		super((short)columnId, hidden, size);
		this.columnTitleKey = columnTitleKey;		
		this.columnDescriptionKey = columnDescriptionKey;
	}
	
	public StaticTypeColumn(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
		columnTitleKey = lineReader.getNextLine();
		columnDescriptionKey = lineReader.getNextLine();
		isIdColumn = Boolean.parseBoolean(lineReader.getNextLine());
		isGroupEditable = Boolean.parseBoolean(lineReader.getNextLine());
		isClearable = Boolean.parseBoolean(lineReader.getNextLine());
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	/**
	 * Some columns can exist in multiple types of tables, and so copies (instances) of the 
	 * columns must be used in order to maintain the state of the same column in different tables.
	 * 
	 * The various model managers will call the getInstance method of each column it is adding.
	 */
	public StaticTypeColumn getInstance(boolean isVisible) {
		StaticTypeColumn result = new StaticTypeColumn(columnId, columnTitleKey, columnDescriptionKey, hidden, size);
		result.setHidden(!isVisible);
		result.isIdColumn = isIdColumn;
		return result;
	}
	public StaticTypeColumn getInstance(boolean isVisible, int size) {
		StaticTypeColumn result = new StaticTypeColumn(columnId, columnTitleKey, columnDescriptionKey, hidden, (short)size);
		result.setHidden(!isVisible);
		result.isIdColumn = isIdColumn;
		result.size = (short)size;
		return result;
	}
	public StaticTypeColumn getInstance(boolean isVisible, int size, boolean isGroupEditable, boolean isClearable) {
		StaticTypeColumn result = new StaticTypeColumn(columnId, columnTitleKey, columnDescriptionKey, hidden, (short)size);
		result.setHidden(!isVisible);
		result.isIdColumn = isIdColumn;
		result.setGroupEditable(isGroupEditable);
		result.setClearable(isClearable);
		result.size = (short)size;
		return result;
	}
	
	public String getColumnTitle() { return Translations.get(columnTitleKey); }
	public String getColumnDescription() { return Translations.get(columnDescriptionKey); }
	
	public String getColumnTitleKey() { return columnTitleKey; }
	public String getColumnDescriptionKey() { return columnDescriptionKey; }
	
	public boolean isIdColumn() { return isIdColumn; }	
	public boolean isGroupEditable() { return isGroupEditable; }
	public boolean isClearable() { return isClearable; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setColumnTitleKey(String columnTitleKey) { this.columnTitleKey = columnTitleKey; }
	public void setColumnDescriptionKey(String columnDescriptionKey) { this.columnDescriptionKey = columnDescriptionKey; } 
	
	public void setGroupEditable(boolean isGroupEditable) { this.isGroupEditable = isGroupEditable; }
	public void setClearable(boolean isClearable) { this.isClearable = isClearable; }

	// for serialization
	public void setIdColumn(boolean isIdColumn) { this.isIdColumn = isIdColumn; }
	
	/////////////
	// METHODS //
	/////////////
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
		writer.writeLine(columnTitleKey);
		writer.writeLine(columnDescriptionKey);
		writer.writeLine(isIdColumn);
		writer.writeLine(isGroupEditable);
		writer.writeLine(isClearable);
	}
	
}
