package com.mixshare.rapid_evolution.net;

import java.net.ServerSocket;

import org.apache.log4j.Logger;

public abstract class SocketListener extends Thread {

	static private Logger log = Logger.getLogger(SocketListener.class);
	
    protected ServerSocket listening_socket = null;

    public void stopListening() {
        try {
            listening_socket.close();
        } catch (java.io.IOException e) {
            log.error("stopListening(): error closing socket", e);
        }
    }
}
