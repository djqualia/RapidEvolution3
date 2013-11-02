package com.mixshare.rapid_evolution.ui.widgets.profile.filter;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.comparables.Percentage;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.ItemDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QStyle;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QWidget;

public class FilterItemDelegate extends ItemDelegate {

    public FilterItemDelegate(QWidget parent, ModelManagerInterface modelManager) {
    	super(parent, modelManager);    	
    }
    
    @Override
    public void paint(QPainter painter, QStyleOptionViewItem option, QModelIndex index) {
		QStyle.State state = option.state();
		state.set(QStyle.StateFlag.State_Active);
		option.setState(state);
		super.paint(painter, option, index);
    }    
    
    @Override
    public QWidget createEditor(QWidget parent, QStyleOptionViewItem item, QModelIndex index) {
        Object data = index.data();
    	QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(index);
    	Column sourceColumn = modelManager.getSourceColumnType(sourceIndex.column()); 
    	if (sourceColumn.getColumnId() == COLUMN_DEGREE.getColumnId()) {
        	QSlider slider = new QSlider(parent);
        	slider.setMinimum(0);
        	slider.setMaximum(100);
        	slider.setTickInterval(1);
        	slider.setOrientation(Orientation.Horizontal);        	
        	slider.setValue((int)(((Percentage)data).getPercentage() * 100.0f));
        	slider.setAutoFillBackground(true);
        	slider.setMaximumWidth(200);
        	return slider;
    	} else
            return super.createEditor(parent, item, index);
    }
    
    @Override
    public void setEditorData(QWidget editor, QModelIndex index) {
        Object data = index.data();
        if (data instanceof Percentage) {
        	((QSlider) editor).setValue((int)(((Percentage)data).getPercentage() * 100.0f));
        } else {
        	super.setEditorData(editor, index);
        }
    }

    @Override
    public void setModelData(QWidget editor, QAbstractItemModel model, QModelIndex index) {
    	if (index.data() instanceof Percentage) {
    		float degree = ((float)((QSlider) editor).value())/ 100.0f;
	    	model.setData(index, new Percentage(degree));
    		QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(index);
    		SearchRecord searchRecord = (SearchRecord)((RecordTableModelManager)modelManager).getRecordForRow(sourceIndex.row());
    		SearchProfile searchProfile = (SearchProfile)Database.getProfile(searchRecord.getIdentifier());
    		FilterProfile currentFilter = (FilterProfile)ProfileWidgetUI.instance.getCurrentProfile();
    		if (currentFilter instanceof StyleProfile)
    			searchProfile.setStyle(new DegreeValue(currentFilter.toString(), degree, DATA_SOURCE_USER));
    		else if (currentFilter instanceof TagProfile)
    			searchProfile.setTag(new DegreeValue(currentFilter.toString(), degree, DATA_SOURCE_USER));
	    	searchProfile.save();
	    } else {
    		super.setModelData(editor, model, index);
    	}
    }
    
    
}
