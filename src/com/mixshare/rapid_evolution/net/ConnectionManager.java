package com.mixshare.rapid_evolution.net;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class ConnectionManager {

    private static Logger log = Logger.getLogger(ConnectionManager.class);
	    
    static private Map<String, Integer> connections = new HashMap<String, Integer>(); // maps user names to connection counts
           
    static synchronized private void updateConnections(String userName, boolean add) {
    	if (userName == null)
    		return;
    	String key = userName.toLowerCase();
    	if (add) {
    		Integer count = (Integer)connections.get(key);
    		if (count == null) {
    			count = new Integer(1);
        		log.info("user logged in=" + userName);
        		log.info("total # connected users=" + (connections.size() + 1));
    		} else {
    			count = new Integer(1 + count.intValue());
    		}
    		connections.put(key, count);
    	} else {
    		// remove 
    		Integer count = (Integer)connections.get(key);
    		if ((count != null) && (count.intValue() > 1)) {
    			count = new Integer(count.intValue() - 1);
    			connections.put(key, count);
    		} else if (count != null) { 
    			connections.remove(key);
    			log.info("user logged out=" + userName);
    			log.info("total # connected users=" + connections.size());
    		}
    	}
    }
    
    static public void addConnection(String userName) {
    	updateConnections(userName, true);    	
    }
    
    static public void removeConnection(String userName) {
    	updateConnections(userName, false);
    }
    
    static public int getNumConnections() {
    	return connections.size();
    }
    
}
