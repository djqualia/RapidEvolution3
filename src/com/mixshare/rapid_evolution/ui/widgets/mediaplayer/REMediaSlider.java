package com.mixshare.rapid_evolution.ui.widgets.mediaplayer;

import org.apache.log4j.Logger;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.MouseButton;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QWidget;

public class REMediaSlider extends QSlider {

	static private Logger log = Logger.getLogger(REMediaSlider.class);	
	
	static public final int SLIDER_PRECISION = 2000;	
	
	public REMediaSlider(QWidget parent) {
		super(Qt.Orientation.Horizontal, parent);
        setRange(0, SLIDER_PRECISION);
        setTracking(false);				
	}
	
	protected void mousePressEvent(QMouseEvent e) {
		if (e.buttons().isSet(MouseButton.LeftButton)) {
			float percent = (float)e.x() / width();
			if (log.isTraceEnabled())
				log.trace("mousePressEvent(): percent=" + percent);
			setValue((int)(minimum() + (maximum() - minimum()) * percent));
			e.accept();
		}
		super.mousePressEvent(e);		
	}

	public double getPercent() {
		return ((double)value() / SLIDER_PRECISION);		
	}
	
	public int getMaxValue() { return SLIDER_PRECISION; }
}
