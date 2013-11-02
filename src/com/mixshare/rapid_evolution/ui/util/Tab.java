package com.mixshare.rapid_evolution.ui.util;

import com.trolltech.qt.gui.QWidget;

public class Tab {

	////////////
	// FIELDS //
	////////////
	
	private String name;
	private QWidget content;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public Tab(String name, QWidget content) {
		this.name = name;
		this.content = content;
	}
	
	/////////////
	// GETTERS //
	/////////////

	public String getName() { return name; }
	public QWidget getContent() { return content; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setName(String name) { this.name = name; }
	public void setContent(QWidget content) { this.content = content; }
	
}
