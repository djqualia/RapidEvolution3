package com.mixshare.rapid_evolution.ui.widgets.common.splitter;

import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QLinearGradient;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QSplitter;
import com.trolltech.qt.gui.QSplitterHandle;

public class RESplitterHandle extends QSplitterHandle {

	public RESplitterHandle(Orientation orientation, QSplitter parent) {
		super(orientation, parent);				
	}
	
	 protected void paintEvent(QPaintEvent event) {	
		 QPainter painter = new QPainter(this);
		 QLinearGradient gradient = new QLinearGradient(0, 0, rect().width(), rect().height());
         gradient.setColorAt(0, new QColor(250, 250, 250));
         gradient.setColorAt(1, new QColor(190, 190, 190));
		 if (orientation() == Orientation.Horizontal) {
             gradient.setStart(rect().left(), rect().height()/2);
             gradient.setFinalStop(rect().right(), rect().height()/2);

             painter.fillRect(rect(), new QBrush(gradient));
             
             painter.setPen(new QColor(170, 170, 170));
             painter.drawLine(0, 0, 0, rect().bottom());
             painter.drawLine(6, 0, 6, rect().bottom());
         } else {
             gradient.setStart(rect().width()/2, rect().top());
             gradient.setFinalStop(rect().width()/2, rect().bottom());
             painter.fillRect(rect(), new QBrush(gradient));

             painter.setPen(new QColor(170, 170, 170));
             painter.drawLine(0, rect().top(), rect().width(), 0);
             painter.drawLine(0, rect().bottom(), rect().width(), rect().bottom());
         }
	 }	
}
