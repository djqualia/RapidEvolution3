package com.mixshare.rapid_evolution.data.record.user;

import java.io.Serializable;

import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * Allows the user to specify any number of custom user data types (such as fields, and flags).
 * The index for each type of search item (artist, label, release, songs) stores a list of unique
 * user data types...
 */
public class UserDataType implements Serializable {

    static private final long serialVersionUID = 0L;    	
	
    static public final byte TYPE_BOOLEAN_FLAG = 1;
    static public final byte TYPE_TEXT_FIELD = 2;
    
    ////////////
    // FIELDS //
    ////////////
    
	private short id;
	private String title;
	private String description;
	private byte fieldType;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public UserDataType() { }
	public UserDataType(short id, String title, String description, byte fieldType) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.fieldType = fieldType;
	}
	public UserDataType(LineReader lineReader) {
		String version = lineReader.getNextLine();
		id = Short.parseShort(lineReader.getNextLine());
		title = lineReader.getNextLine();
		description = lineReader.getNextLine();
		fieldType = Byte.parseByte(lineReader.getNextLine());
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public short getId() { return id; }
	public String getTitle() { return title; }
	public String getDescription() { return description; }
	public byte getFieldType() { return fieldType; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setTitle(String title) { this.title = title; }
	public void setDescription(String description) { this.description = description; }
	public void setFieldType(byte fieldType) { this.fieldType = fieldType; }
	
	// for serialization
	public void setId(short id) { this.id = id; }
	
	/////////////
	// METHODS //
	/////////////
	
	public String toString() { return title + " [" + id + "]"; }
	
	public boolean equals(Object o) {
		if (o instanceof UserDataType) {
			UserDataType oT = (UserDataType)o;
			return id == oT.id;
		}
		return false;
	}
	
	public int hashCode() { return id; }
	
	public void write(LineWriter writer) {
		writer.writeLine(1); // version
		writer.writeLine(id);
		writer.writeLine(title);
		writer.writeLine(description);
		writer.writeLine(fieldType);
	}
	
}
