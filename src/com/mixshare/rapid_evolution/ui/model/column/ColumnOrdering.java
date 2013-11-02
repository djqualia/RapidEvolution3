package com.mixshare.rapid_evolution.ui.model.column;

import java.io.Serializable;

import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * Simple class to keep track of a column and whether it's sorted ascending/descending.
 * A list of ColumnOrdering classes can be used to define primary/secondary/tertiary sorting (and so on)...
 */
public class ColumnOrdering implements Serializable {

    static private final long serialVersionUID = 0L;    	
	
	////////////
	// FIELDS //
	////////////
	
	private short columnId;
	private boolean ascending;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public ColumnOrdering() { }
	
	public ColumnOrdering(short columnId) {
		this.columnId = columnId;
		this.ascending = true;
	}
	
	public ColumnOrdering(short columnId, boolean ascending) {
		this.columnId = columnId;
		this.ascending = ascending;
	}
	
	public ColumnOrdering(LineReader lineReader) {
		int version = Integer.parseInt(lineReader.getNextLine());
		columnId = Short.parseShort(lineReader.getNextLine());
		ascending = Boolean.parseBoolean(lineReader.getNextLine());
	}
	
	/////////////
	// GETTERS //
	/////////////

	public short getColumnId() { return columnId; }
	public boolean isAscending() { return ascending; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setColumnId(short columnId) { this.columnId = columnId; }
	public void setAscending(boolean ascending) { this.ascending = ascending; }
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() {
		return String.valueOf(columnId) + " (" + (ascending ? "Ascending" : "Descending") + ")";
	}
	
	public boolean equals(Object o) {
		if (o instanceof ColumnOrdering) {
			ColumnOrdering c = (ColumnOrdering)o;
			return columnId == c.columnId;
		}
		return false;			
	}
	
	public int hashCode() { return columnId; }
	
	public void write(LineWriter writer) {
		writer.writeLine(1); // version
		writer.writeLine(columnId);
		writer.writeLine(ascending);
	}
	
}
