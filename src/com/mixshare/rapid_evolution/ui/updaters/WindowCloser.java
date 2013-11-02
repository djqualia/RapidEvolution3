package com.mixshare.rapid_evolution.ui.updaters;

import org.apache.log4j.Logger;

import com.trolltech.qt.gui.QMainWindow;

public class WindowCloser extends Thread {

	static private Logger log = Logger.getLogger(WindowCloser.class);
	
	private QMainWindow window;
	
	public WindowCloser(QMainWindow window) {
		this.window = window;
	}

	public void run() {
		try {
			window.close();
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}
	
}