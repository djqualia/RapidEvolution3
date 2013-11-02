package com.mixshare.rapid_evolution.net;

import java.net.ServerSocket;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;

public class ClientListener extends SocketListener {

	static private Logger log = Logger.getLogger(ClientListener.class);

    static private ClientListener instance = null;
    
    private int client_listen_port;
        
    public ClientListener(int listen_port) {
    	instance = this;
    	setDaemon(true);
    	setPriority(Thread.MIN_PRIORITY + 1);
        client_listen_port = listen_port;
    }
    
    public void run() {
    	try {
    		if (log.isInfoEnabled()) 
    			log.info("run(): listening for client connections on port: " + String.valueOf(client_listen_port));
    		listening_socket = new ServerSocket(client_listen_port);
    		while (!RapidEvolution3.isTerminated) {
    			try {
    				(new ClientServer(listening_socket.accept())).start();
    			} catch (java.net.SocketException se) {
    				log.debug("run(): socket exception=" + se);
    			} catch (java.io.IOException e) {
    				log.error("run(): error accepting client connection", e);
    			}
    		}
    		listening_socket.close();
    		if (log.isInfoEnabled()) 
    			log.info("run(): stopped listening for client connections");
    	} catch (java.net.SocketException se) {
    		log.debug("run(): socket exception=" + se);
    	} catch (java.io.IOException e) {
      		log.error("run(): ioexception listening for client connections=" + e);
      	}
    }
    
    static public void shutdown() {
    	try {
	    	if (instance.listening_socket != null)
	    		instance.listening_socket.close();
    	} catch (Exception e) {
    		log.error("shutdown(): error", e);
    	}
    }
    
}
