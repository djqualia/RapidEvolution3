package com.mixshare.rapid_evolution.ui.dialogs.trail;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.profile.trail.TrailModelManager;
import com.mixshare.rapid_evolution.ui.util.Color;
import com.mixshare.rapid_evolution.ui.widgets.common.ItemDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QStyle;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QWidget;

public class TrailItemDelegate extends ItemDelegate {

	static private Color TRAIL_CURRENT_PROFILE_COLOR = RE3Properties.getColor("trail_current_profile_color");
	
	public TrailItemDelegate(QWidget parent, ModelManagerInterface modelManager) {
		super(parent, modelManager);
	}

    @Override
    public void paint(QPainter painter, QStyleOptionViewItem option, QModelIndex index) {
		QStyle.State state = option.state();
		state.set(QStyle.StateFlag.State_Active);
		option.setState(state);        	
    	Object data = index.data();
    	QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(index);
		SearchRecord searchRecord = (SearchRecord)((TrailModelManager)modelManager).getRecordForRow(sourceIndex.row());
		if (!state.isSet(QStyle.StateFlag.State_Selected)) {
			if (searchRecord.equals(ProfileWidgetUI.instance.getCurrentRecord()) && (sourceIndex.row() == ProfileWidgetUI.instance.getProfileIndex())) {
				state.set(QStyle.StateFlag.State_Selected);
				option.setState(state);					
				QPalette p = option.palette();
				p.setColor(QPalette.ColorRole.Highlight, TRAIL_CURRENT_PROFILE_COLOR.getQColor());
				option.setPalette(p);			
			}
		}
        super.paint(painter, option, index);
    }
    
}
