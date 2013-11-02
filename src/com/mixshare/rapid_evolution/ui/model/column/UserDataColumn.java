package com.mixshare.rapid_evolution.ui.model.column;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class UserDataColumn extends CommonColumn {

    static private final long serialVersionUID = 0L;    	
	
    ////////////
    // FIELDS //
    ////////////
    
	private UserDataType userDataType;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public UserDataColumn() { }
	
	public UserDataColumn(short columnId, UserDataType userDataType, boolean hidden) {
		super(columnId, hidden, (short)100);
		this.userDataType = userDataType;
		if (userDataType.getFieldType() == UserDataType.TYPE_BOOLEAN_FLAG)
			setSize(RE3Properties.getShort("column_width_user_flag_default"));
		else if (userDataType.getFieldType() == UserDataType.TYPE_TEXT_FIELD)
			setSize(RE3Properties.getShort("column_width_user_field_default"));
	}
	
	public UserDataColumn(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
		userDataType = new UserDataType(lineReader);
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
	public UserDataColumn getInstance(boolean isVisible) {
		UserDataColumn result = new UserDataColumn(columnId, userDataType, hidden);
		result.setHidden(!isVisible);
		return result;
	}
	public UserDataColumn getInstance(boolean isVisible, int size) {
		UserDataColumn result = new UserDataColumn(columnId, userDataType, hidden);
		result.setHidden(!isVisible);
		result.setSize((short)size);
		return result;
	}
	
	public String getColumnTitle() { return userDataType.getTitle(); }
	public String getColumnDescription() { return userDataType.getDescription(); }
	public UserDataType getUserDataType() { return userDataType; }
	public boolean isIdColumn() { return false; }
	
	public boolean isGroupEditable() { return (userDataType.getFieldType() == UserDataType.TYPE_TEXT_FIELD); }
	public boolean isClearable() { return (userDataType.getFieldType() == UserDataType.TYPE_TEXT_FIELD); }
	public void setClearable(boolean clearable) { }

	
	/////////////
	// SETTERS //
	/////////////
	
	// for serialization
	public void setUserDataType(UserDataType userDataType) { this.userDataType = userDataType; }
	
	/////////////
	// METHODS //
	/////////////
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); //version
		userDataType.write(writer);
	}
	
}
