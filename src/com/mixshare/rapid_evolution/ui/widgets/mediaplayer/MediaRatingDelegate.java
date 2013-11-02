package com.mixshare.rapid_evolution.ui.widgets.mediaplayer;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.player.PlayerManager;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarEditor;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRating;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;
import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QItemDelegate;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QStyle;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QWidget;

public class MediaRatingDelegate extends QItemDelegate implements AllColumns, DataConstants {
	
	static private Logger log = Logger.getLogger(MediaRatingDelegate.class);
	
    public MediaRatingDelegate(QWidget parent) {
        super(parent);
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
		SongRecord song = PlayerManager.getCurrentSong();
		SearchProfile searchProfile = (SearchProfile)Database.getSongIndex().getSongProfile(song.getUniqueId());
		if (searchProfile != null) {
			searchProfile.setRating(((StarEditor) editor).starRating().getRating(), DATA_SOURCE_USER);
			ProfileSaveTask.save(searchProfile);
		}
    }
        
}