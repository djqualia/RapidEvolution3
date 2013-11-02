package com.mixshare.rapid_evolution.ui.dialogs.outofmemory;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.util.Translations;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMessageBox;

public class OutOfMemoryThread extends Thread {

	static private Logger log = Logger.getLogger(OutOfMemoryThread.class);
	
	private QMainWindow window;
	private String taskDescription;
	
	public OutOfMemoryThread(QMainWindow window, String taskDescription) {
		this.window = window;
		this.taskDescription = taskDescription;
	}

	public void run() {
		try {
			QMessageBox.critical(window, Translations.get("out_of_memory_title"), Translations.get("out_of_memory_description") + " " + taskDescription);
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}
	
}