package com.mixshare.rapid_evolution.ui.util;

import com.mixshare.rapid_evolution.ui.UIProperties;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QWidget;

public class QWidgetUtil {

	static public void setWidgetSize(QWidget widget, String id, int defaultWidth, int defaultHeight) {
        try {
        	int width = UIProperties.getInt(id + "_width");
        	int height = UIProperties.getInt(id + "_height");
        	if (QApplication.desktopSettingsAware()) {
        		if (width > QApplication.desktop().screenGeometry().width())
        			width = QApplication.desktop().screenGeometry().width();
        		if (height > QApplication.desktop().screenGeometry().height())
        			height = QApplication.desktop().screenGeometry().height();        		
        	}
        	widget.resize(width, height);
        } catch (Exception e) {
        	widget.resize(defaultWidth, defaultHeight);
        }				
	}
	
	static public void setWidgetPosition(QWidget widget, String id) {
        try {
        	int x = UIProperties.getInt(id + "_x");
        	int y = UIProperties.getInt(id + "_y");
        	if (QApplication.desktopSettingsAware()) {
        		if (y > QApplication.desktop().screenGeometry().height())
        			y = QApplication.desktop().screenGeometry().height() - widget.height();
        	}
        	if (y < 0)
        		y = 0;
        	if (QApplication.desktopSettingsAware()) {
        		if (x > QApplication.desktop().screenGeometry().width())
        			x = QApplication.desktop().screenGeometry().width() - widget.width();
        	}
        	if (x < 0)
        		x = 0;
        	widget.move(x, y);
        } catch (Exception e) { }				
	}
	
}
