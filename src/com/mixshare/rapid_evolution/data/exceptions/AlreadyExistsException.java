package com.mixshare.rapid_evolution.data.exceptions;

import com.mixshare.rapid_evolution.data.identifier.Identifier;

public class AlreadyExistsException extends Exception {

    static private final long serialVersionUID = 0L;    
	
    private Identifier id;
    
    private AlreadyExistsException() { }
    public AlreadyExistsException(Identifier id) {
    	super();
    	this.id = id;
    }

	public Identifier getId() { return id; }
	public void setId(Identifier id) { this.id = id; }
    
}
