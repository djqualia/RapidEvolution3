package com.mixshare.rapid_evolution.ui.updaters;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.RE3StatusBar;

public class StatusMessageSetter extends Thread {

	static private Logger log = Logger.getLogger(StatusMessageSetter.class);
	
	static private final int DEFAULT_TIMEOUT = RE3Properties.getInt("status_message_timeout_millis");
    
	private RE3StatusBar statusbar;
	private String message;
	private int timeout;
	
	public StatusMessageSetter(RE3StatusBar statusbar, String message) {
		this.statusbar = statusbar;
		this.message = message;
		timeout = DEFAULT_TIMEOUT;
	}
	public StatusMessageSetter(RE3StatusBar statusbar, String message, int timeout) {
		this.statusbar = statusbar;
		this.message = message;
		this.timeout = timeout;
	}
	
	public void run() {
		try {
			statusbar.showMessage(Translations.getPreferredCase(message), timeout);
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}
	
}
