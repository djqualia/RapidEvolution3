package com.mixshare.rapid_evolution.data.profile.search.song;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class Exclude implements Serializable {
	
    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(Exclude.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private int fromSongId;
	private int toSongId;
    
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public Exclude() { }
	public Exclude(int fromSongId, int toSongId) {
		this.fromSongId = fromSongId;
		this.toSongId = toSongId;
	}
	public Exclude(LineReader lineReader) {
		int version = Integer.parseInt(lineReader.getNextLine());
		fromSongId = Integer.parseInt(lineReader.getNextLine());
		toSongId = Integer.parseInt(lineReader.getNextLine());
	}

	/////////////
	// GETTERS //
	/////////////
	
	public int getFromSongId() { return fromSongId; }
	public int getToSongId() { return toSongId; }
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(String.valueOf(fromSongId));
		result.append("->");
		result.append(String.valueOf(toSongId));
		return result.toString();
	}

	/////////////
	// SETTERS //
	/////////////
	
	public void setFromSongId(int fromSongId) { this.fromSongId = fromSongId; }
	public void setToSongId(int toSongId) { this.toSongId = toSongId; }	
	
	/////////////
	// METHODS //
	/////////////
	
	public boolean equals(Object o) {
		if (o instanceof Exclude) {
			Exclude oExclude = (Exclude)o;
			return fromSongId == oExclude.fromSongId && toSongId == oExclude.toSongId;
		}
		return false;
	}
	
	public int hashCode() {
		return fromSongId + toSongId << 8;
	}

    public void write(LineWriter writer) {
    	writer.writeLine("1"); // version
    	writer.writeLine(fromSongId);
    	writer.writeLine(toSongId);
    }    
	
}
