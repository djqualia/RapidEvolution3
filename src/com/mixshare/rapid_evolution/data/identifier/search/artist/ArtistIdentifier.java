package com.mixshare.rapid_evolution.data.identifier.search.artist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.search.SearchIdentifier;

public class ArtistIdentifier extends SearchIdentifier {

    static private Logger log = Logger.getLogger(ArtistIdentifier.class);
    static private final long serialVersionUID = 0L;    
	    
    static public final String typeDescription = "artist";
    
    ////////////
    // FIELDS //
    ////////////
        
	private String name;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public ArtistIdentifier() { }
	public ArtistIdentifier(String name) {
		this.name = name;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public byte getType() { return DATA_TYPE_ARTISTS; }
    public String getTypeDescription() { return typeDescription; }
	
	public String getName() { return name; }

	public boolean isValid() { 
		return ((name != null) && (name.length() > 0) && !name.equalsIgnoreCase("Various") && !name.equalsIgnoreCase("Various Artists")); 
	}
		
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
	
	static public ArtistIdentifier parseIdentifier(String uniqueId) {
		return new ArtistIdentifier(uniqueId.substring(typeDescription.length() + 1));
	}
		
}
