package com.mixshare.rapid_evolution.data.mined.discogs;

public class DiscogsException extends Throwable {

    private static final long serialVersionUID = 0L;
	
    private String message;
    
    public DiscogsException(String message) {
    	this.message = message;
    }
    
    public String toString() { return message; }
    
}
