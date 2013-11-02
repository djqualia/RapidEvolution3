package com.mixshare.rapid_evolution.workflow.event;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMessageBox;

public class NoDecoderError extends Thread {

	static private Logger log = Logger.getLogger(NoDecoderError.class);
	
	static public NoDecoderError instance;
	
	private QMainWindow window;
	private String filename;
	
	public NoDecoderError(QMainWindow window, String filename) {
		this.window = window;
		this.filename = filename;
		instance = this;
	}

	public void run() {
		try {
			QMessageBox.warning(window, Translations.get("no_decoder_title"), Translations.get("no_decoder_description") + "  " + filename);			
		} catch (Exception e) {
			log.error("run(): error", e);
		} finally {
			instance = null;
		}
	}
	
}