package com.mixshare.rapid_evolution.audio.player.util;

import com.mixshare.rapid_evolution.RE3Properties;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QWidget;

public class ToggleLink extends QLabel {
	
	private String id;
	private boolean enabled = false;
	private String disabledIconFilename;
	private String enabledIconFilename;
	
	public ToggleLink(QWidget parent, String disabledIconFilename, String enabledIconFilename, String id) {
		super(parent);
		this.id = id;
		this.disabledIconFilename = disabledIconFilename;
		this.enabledIconFilename = enabledIconFilename;
		setMouseTracking(true);
		enabled = RE3Properties.getBoolean(id);
		setImage();
	}
	
	public boolean isLabelEnabled() { return enabled; }
	
	protected void setImage() {
		if (enabled)
			setPixmap(new QPixmap(enabledIconFilename));
		else
			setPixmap(new QPixmap(disabledIconFilename));
	}
	
	protected void mousePressEvent(QMouseEvent e) {
		enabled = !enabled;
		if (enabled)
			RE3Properties.setProperty(id, "true");
		else
			RE3Properties.setProperty(id, "false");
		setImage();
	}

}
