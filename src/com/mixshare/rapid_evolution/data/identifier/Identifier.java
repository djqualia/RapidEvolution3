package com.mixshare.rapid_evolution.data.identifier;

/**
 * This interface and set of classes provides a useful abstraction for identifying different types of objects (some of which are 
 * uniquely identified by more than 1 field, such as songs and releases).
 * 
 * These classes should be immutable, do not call the set methods (they are for XML serialization).
 */
public interface Identifier {

	/**
	 * Generate a unique ID which can be parsed back into an Identifier if necessary (providing serialization to strings).
	 * The ID should be unique when compared to items of other types as well.
	 */
	public String getUniqueId();

	/**
	 * Returns the type, useful for logging and generating unique ids (for example, "artist", "song", "style", etc).
	 */
	public byte getType();
	public String getTypeDescription();
	
	/**
	 * Returns true if the identifier contains sufficient information to identify an item of its type...
	 */
	public boolean isValid();
			
}
