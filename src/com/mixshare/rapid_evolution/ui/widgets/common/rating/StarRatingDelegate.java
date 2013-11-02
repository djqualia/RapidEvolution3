package com.mixshare.rapid_evolution.ui.widgets.common.rating;

import org.apache.log4j.Logger;

import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QItemDelegate;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QStyle;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QWidget;

public class StarRatingDelegate extends QItemDelegate {
	
	static private Logger log = Logger.getLogger(StarRatingDelegate.class);
	
	private StarRatingChangedListener listener;
	
    public StarRatingDelegate(QWidget parent, StarRatingChangedListener listener) {
        super(parent);
        this.listener = listener;
    }

    @Override
    public void paint(QPainter painter, QStyleOptionViewItem option, QModelIndex index) {
    	//if (log.isTraceEnabled())
    		//log.trace("paint(): index=" + index + ", option.palette().highlight()=" + option.palette().highlight());
		QStyle.State state = option.state();
		state.set(QStyle.StateFlag.State_Active);
		option.setState(state);        	
        if (option.state().isSet(QStyle.StateFlag.State_Selected) && option.state().isSet(QStyle.StateFlag.State_Active)) {
            //painter.fillRect(option.rect(), option.palette().highlight());
        }
        if (index.data() != null)
        	((StarRating) index.data()).paint(painter, option.rect(), option.palette(), StarRating.ReadOnly);
    }

    @Override
    public QSize sizeHint(QStyleOptionViewItem option, QModelIndex index) {
        Object data = index.data();

        if (data instanceof StarRating)
            return ((StarRating) data).sizeHint();
        else
            return super.sizeHint(option, index);
    }

    @Override
    public QWidget createEditor(QWidget parent, QStyleOptionViewItem item, QModelIndex index) {
        Object data = index.data();
        return new StarEditor(parent, (StarRating) data);
    }

    @Override
    public void setEditorData(QWidget editor, QModelIndex index) {
        ((StarEditor) editor).setStarRating((StarRating) index.data());
    }

    @Override
    public void setModelData(QWidget editor, QAbstractItemModel model, QModelIndex index) {    	
    	if (listener != null)
    		listener.ratingChanged(((StarEditor) editor).starRating().getRating());
    }
        
}