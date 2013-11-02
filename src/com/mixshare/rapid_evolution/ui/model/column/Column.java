package com.mixshare.rapid_evolution.ui.model.column;

import com.mixshare.rapid_evolution.util.io.LineWriter;

public interface Column {

	/////////////
	// GETTERS //
	/////////////
	
	public short getColumnId();	
	public String getColumnTitle();
	public String getColumnDescription();
	public boolean isHidden();
	public short getSize();	
	public boolean isIdColumn(); // does the column uniquely identify a record of that type?
	public boolean isGroupEditable();
	public boolean isClearable();
	public String getColumnTitleId();
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setHidden(boolean hidden);
	public void setSize(short size);
	public void setClearable(boolean clearable);
		
	/////////////
	// METHODS //
	/////////////
	
	public void write(LineWriter writer);
	
}
