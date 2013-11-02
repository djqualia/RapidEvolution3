package com.mixshare.rapid_evolution.ui.model.column;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * Combines common functionality between static columns (defined in the code) and user defined
 * columns/fields.
 */
abstract public class CommonColumn implements Column, Serializable {

    static private Logger log = Logger.getLogger(CommonColumn.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	protected short columnId;
	protected boolean hidden;
	protected short size;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public CommonColumn() { }
	public CommonColumn(short columnId, boolean hidden, short size) {
		this.columnId = columnId;
		this.hidden = hidden;
		this.size = size;
	}
	public CommonColumn(LineReader lineReader) {
		int version = Integer.parseInt(lineReader.getNextLine());
		columnId = Short.parseShort(lineReader.getNextLine());
		hidden = Boolean.parseBoolean(lineReader.getNextLine());
		size = Short.parseShort(lineReader.getNextLine());
	}
		
	/////////////
	// GETTERS //
	/////////////
	
	public short getColumnId() { return columnId; }
	public boolean isHidden() { return hidden; }
	public short getSize() { return size; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setHidden(boolean hidden) { this.hidden = hidden; }
	public void setSize(short size) { this.size = size; }
	
	// for serialization
	public void setColumnId(short columnId) { this.columnId = columnId; }
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() { return getColumnTitle(); }
	
	public boolean equals(Object o) {
		if (o instanceof CommonColumn) {
			CommonColumn c = (CommonColumn)o;
			return columnId == c.columnId;
		}
		return false;
	}
	
	public int hashCode() { return columnId; }
	
	public int compareTo(CommonColumn c) {
		return getColumnTitle().compareToIgnoreCase(c.getColumnTitle());
	}
	
	public void write(LineWriter writer) {
		writer.writeLine(1); // version
		writer.writeLine(columnId);
		writer.writeLine(hidden);
		writer.writeLine(size);
	}
	
	public String getColumnTitleId() {
		String titleId = getColumnTitle();
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < titleId.length(); ++i) {
			char c = titleId.charAt(i);
			if (Character.isLetterOrDigit(c))
				result.append(c);
			else if ((c == ' ') || (c == '_'))
				result.append('_');
		}
		return result.toString();
	}
	
	////////////////////
	// STATIC METHODS //
	////////////////////

	static public Column readColumn(LineReader lineReader) {
		int type = Integer.parseInt(lineReader.getNextLine());
		if (type == 1)
			return new StaticTypeColumn(lineReader);
		else if (type == 2)
			return new UserDataColumn(lineReader);		
		log.warn("readColumn(): unknown column type=" + type);
		return null;
	}
	
	static public void writeColumn(Column column, LineWriter writer) {
		if (column instanceof StaticTypeColumn)
			writer.writeLine(1);
		else if (column instanceof UserDataColumn)
			writer.writeLine(2);
		column.write(writer);
	}
	
	
}
