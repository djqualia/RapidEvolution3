package com.mixshare.rapid_evolution.data.identifier;

import java.io.Serializable;

import com.mixshare.rapid_evolution.data.DataConstants;

/**
 * Since identifiers will often be used as keys in maps and compared/printed, some common functionality
 * is defined here...
 */
abstract public class CommonIdentifier implements Identifier, Serializable, Comparable<Identifier>, DataConstants {
			
	public boolean equals(Object o) {
		if (o instanceof Identifier) {
			Identifier id = (Identifier)o;
			return id.getUniqueId().equalsIgnoreCase(getUniqueId());
		}
		return false;
	}
	
	public int hashCode() {
		return getUniqueId().toLowerCase().hashCode();
	}
	
	public int compareTo(Identifier id) {
		return toString().compareToIgnoreCase(id.toString());
	}
	
}
