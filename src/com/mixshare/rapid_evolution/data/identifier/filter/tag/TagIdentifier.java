package com.mixshare.rapid_evolution.data.identifier.filter.tag;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;

public class TagIdentifier extends FilterIdentifier {

    static private Logger log = Logger.getLogger(TagIdentifier.class);
    static private final long serialVersionUID = 0L;    
	
    static public final String typeDescription = "tag";
    
    ////////////
    // FIELDS //
    ////////////    
        
	private String name;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public TagIdentifier() { }
	public TagIdentifier(String name) {
		this.name = name;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public byte getType() { return DATA_TYPE_TAGS; }
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
	
	static public TagIdentifier parseIdentifier(String uniqueId) {
		return new TagIdentifier(uniqueId.substring(typeDescription.length() + 1));
	}
	
}
