package com.mixshare.rapid_evolution.data.identifier.search.label;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.search.SearchIdentifier;

public class LabelIdentifier extends SearchIdentifier {

    static private Logger log = Logger.getLogger(LabelIdentifier.class);	
    static private final long serialVersionUID = 0L;    
	    
    static public final String typeDescription = "label";
    
    ////////////
    // FIELDS //
    ////////////
        
	private String name;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public LabelIdentifier() { }
	public LabelIdentifier(String name) {
		this.name = name;
	}	

	/////////////
	// GETTERS //
	/////////////
	
	public byte getType() { return DATA_TYPE_LABELS; }	
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
		
	public String toString() {
		return name;
	}	

	// for serialization
	public void setName(String name) {
		this.name = name;
	}	

	/////////////
	// METHODS //
	/////////////	
	
	static public LabelIdentifier parseIdentifier(String uniqueId) {
		return new LabelIdentifier(uniqueId.substring(typeDescription.length() + 1));
	}
	
}
