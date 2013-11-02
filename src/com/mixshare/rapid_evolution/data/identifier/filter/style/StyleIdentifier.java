package com.mixshare.rapid_evolution.data.identifier.filter.style;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;

public class StyleIdentifier extends FilterIdentifier {

    static private Logger log = Logger.getLogger(StyleIdentifier.class);
    static private final long serialVersionUID = 0L;    
	
    static public final String typeDescription = "style";
    
    ////////////
    // FIELDS //
    ////////////    

    private String name;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
    
	public StyleIdentifier() { }
	public StyleIdentifier(String name) {
		this.name = name;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public byte getType() { return DATA_TYPE_STYLES; }	
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
	
	static public StyleIdentifier parseIdentifier(String uniqueId) {
		return new StyleIdentifier(uniqueId.substring(typeDescription.length() + 1));
	}
		
}
