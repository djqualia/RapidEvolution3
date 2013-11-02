package com.mixshare.rapid_evolution.util.timing;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class Semaphore extends java.util.concurrent.Semaphore implements Serializable {
        
    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(Semaphore.class);
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public Semaphore(int i) { super(i, true); } // fair semaphore

    /////////////
    // METHODS //
    /////////////
    
    public void release() {
    	//if (log.isTraceEnabled())
    		//log.trace("release(): before");
    	super.release();        
    	//if (log.isTraceEnabled())
    		//log.trace("release(): after");
    }

    public void acquire(String description) throws InterruptedException {
    	//if (log.isTraceEnabled()) 
    		//log.trace("acquire(): before, description=" + description);
    	super.acquire();        
    	//if (log.isTraceEnabled())
    		//log.trace("acquire(): after, description=" + description);
    }

    public void tryAcquire(String description, long timeOutMillis) throws InterruptedException {
    	//if (log.isTraceEnabled()) 
    		//log.trace("tryAcquire(): before, description=" + description);
    	try { 
    		if (!super.tryAcquire(timeOutMillis, TimeUnit.MILLISECONDS))
    			log.warn("tryAcquire(): semaphore not acquired=" + description);
    	} catch (InterruptedException ie) { }        
    	//if (log.isTraceEnabled())
    		//log.trace("tryAcquire(): after, description=" + description);
    }
    
}