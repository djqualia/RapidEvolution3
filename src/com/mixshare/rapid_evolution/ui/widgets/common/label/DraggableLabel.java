package com.mixshare.rapid_evolution.ui.widgets.common.label;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.DropAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDrag;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QMouseEvent;

public class DraggableLabel extends QLabel {

	static private Logger log = Logger.getLogger(DraggableLabel.class);
	
	private QPoint dragStartPosition;
	
	protected void mousePressEvent(QMouseEvent event) {
        if (event.button().equals(Qt.MouseButton.LeftButton))
            dragStartPosition = event.pos();
        else
        	dragStartPosition = null;
	}
	
	protected void mouseMoveEvent(QMouseEvent event) {
        if (!(event.buttons().isSet(Qt.MouseButton.LeftButton)))
            return;
        if (dragStartPosition == null)
        	return;
        if ((event.pos().subtract(dragStartPosition)).manhattanLength() < QApplication.startDragDistance())
            return;		
		if (ProfileWidgetUI.instance.getCurrentRecord() instanceof SearchRecord) {
			if (log.isDebugEnabled())
				log.debug("mouseMoveEvent(): starting drag");
			Vector<SearchRecord> searchRecords = new Vector<SearchRecord>();
			searchRecords.add((SearchRecord)ProfileWidgetUI.instance.getCurrentRecord());
			QMimeData mimeData = DragDropUtil.getMimeDataForSearchRecords(searchRecords);
			QDrag drag = new QDrag(this);
			drag.setMimeData(mimeData);
			//drag.setPixmap(pixmap());
			drag.setHotSpot(event.pos());
			drag.exec(DropAction.CopyAction);
		}
		
	}
	
}
