package com.mixshare.rapid_evolution.data.mined.lastfm.event;

import java.io.Serializable;

import net.roarsoftware.lastfm.Event.TicketSupplier;

import org.apache.log4j.Logger;

public class LastfmTicketSupplier implements Serializable {

    static private Logger log = Logger.getLogger(LastfmTicketSupplier.class);
    static private final long serialVersionUID = 0L;
            
    ////////////
    // FIELDS //
    ////////////
    
    private String name;   
	private String website;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public LastfmTicketSupplier(TicketSupplier supplier) {
    	name = supplier.getName();
    	website = supplier.getWebsite();
    }
    
    /////////////
    // GETTERS //
    /////////////

    public String getName() {
		return name;
	}

	public String getWebsite() {
		return website;
	}    
    
    /////////////
    // SETTERS //
    /////////////
	
	public void setName(String name) {
		this.name = name;
	}

	public void setWebsite(String website) {
		this.website = website;
	}
	
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(name);
        result.append(" (");
        result.append(website);
        result.append(")");
        return result.toString();
    }
        
}