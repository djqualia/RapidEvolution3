package com.mixshare.rapid_evolution.data.mined;

import java.io.Serializable;

import com.mixshare.rapid_evolution.data.DataConstants;

/**
 * This is the base class for data mined profiles.  Each sub-class must supply the header, indicating
 * the type of profile (artist/label/release/song) and the source of the data (i.e. discogs, lastfm, etc).
 * 
 * In order to be compatible with the XML serialization, all sub-classes should implement a default
 * constructor as well as standard setter/getter functions for all fields.
 */
abstract public class MinedProfile implements Serializable, DataConstants  {

	////////////
	// FIELDS //
	////////////
	
	protected MinedProfileHeader header;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public MinedProfile() { }
	public MinedProfile(MinedProfileHeader header) {
		this.header = header;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getHeader() { return header; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setHeader(MinedProfileHeader header) {
		this.header = header;
	}	
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public boolean isValid();
	
	/////////////
	// METHODS //
	/////////////
	
	public boolean equals(Object o) {
		if (o instanceof MinedProfile) {
			MinedProfile m = (MinedProfile)o;
			return header.equals(m.header);
		}
		return false;
	}
	
	public int hashCode() { return header.hashCode(); }
	
}
