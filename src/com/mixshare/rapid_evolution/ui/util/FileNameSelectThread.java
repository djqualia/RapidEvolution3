package com.mixshare.rapid_evolution.ui.util;

import java.util.List;

import org.apache.log4j.Logger;

import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileDialog;

public class FileNameSelectThread extends Thread {

    static private Logger log = Logger.getLogger(FileNameSelectThread.class);
	
    private String filterText;
    private String filter;
	private String filename = null;
	
	public FileNameSelectThread(String filterText, String filter) {
		this.filterText = filterText;
		this.filter = filter;
	}
	
	public void run() {
		try {
    		QFileDialog fileDialog = new QFileDialog();    		
    		fileDialog.setFileMode(QFileDialog.FileMode.ExistingFile);
    		fileDialog.setViewMode(QFileDialog.ViewMode.Detail);        		
    		fileDialog.setFilter(filterText + " " + filter);
    		fileDialog.setDirectory(System.getProperty("user.home", "."));
    	    if (fileDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    	        List<String> filenames = fileDialog.selectedFiles();
    	        filename = filenames.get(0);
    	        if (log.isTraceEnabled())
    	        	log.trace("browseFilename(): selected filename=" + filename);
    	    }        					
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}
	
	public String getFilename() { return filename; }
	
}
