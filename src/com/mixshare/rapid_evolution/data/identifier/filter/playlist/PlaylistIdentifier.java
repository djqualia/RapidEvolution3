package com.mixshare.rapid_evolution.data.identifier.filter.playlist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;

public class PlaylistIdentifier extends FilterIdentifier {

    static private Logger log = Logger.getLogger(PlaylistIdentifier.class);
    static private final long serialVersionUID = 0L;    
	
    static public final String typeDescription = "playlist";
    
    ////////////
    // FIELDS //
    ////////////
        
	private String name;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public PlaylistIdentifier() { }
	public PlaylistIdentifier(String name) {
		this.name = name;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public byte getType() { return DATA_TYPE_PLAYLISTS; }	
    public String getTypeDescription() { return typeDescription; }
	
	public String getName() { return name; }

	public boolean isValid() { return ((name != null) && (name.length() > 0)); }
		
	public String getUniqueId() {
		StringBuffer result = new StringBuffer();
		result.append(getTypeDescription());
		result.append("/");
		result.append(name);
		return result.toString();
	}
	
	public String toString() { return name; }
	
	// for serialization
	public void setName(String name) {
		this.name = name;
	}	
	
	/////////////
	// METHODS //
	/////////////	
	
	static public PlaylistIdentifier parseIdentifier(String uniqueId) {
		return new PlaylistIdentifier(uniqueId.substring(typeDescription.length() + 1));
	}
			
}
