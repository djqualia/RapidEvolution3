package com.mixshare.rapid_evolution.data.mined;

import java.io.Serializable;

public class MinedProfileHeader implements Serializable {

    static private final long serialVersionUID = 0L;    
	
	////////////
	// FIELDS //
	////////////
	
	protected byte dataType;
	protected byte dataSource;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public MinedProfileHeader() { }
	public MinedProfileHeader(byte dataType, byte dataSource) {
		this.dataType = dataType;
		this.dataSource = dataSource;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public byte getDataType() { return dataType; }
	public byte getDataSource() { return dataSource; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setDataType(byte dataType) { this.dataType = dataType; }
	public void setDataSource(byte dataSource) { this.dataSource = dataSource; }
	
	/////////////
	// METHODS //
	/////////////
	
	public boolean equals(Object o) {
		if (o instanceof MinedProfileHeader) {
			MinedProfileHeader m = (MinedProfileHeader)o;
			if ((dataType == m.dataType) && (dataSource == m.dataSource)) {
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() { 
		return (((int)dataType) << 8) + dataSource;
	}

}
