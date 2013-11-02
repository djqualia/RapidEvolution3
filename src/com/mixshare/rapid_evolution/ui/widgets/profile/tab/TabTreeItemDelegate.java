package com.mixshare.rapid_evolution.ui.widgets.profile.tab;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.accuracy.Accuracy;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRating;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QItemDelegate;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QStyle;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QWidget;

public class TabTreeItemDelegate extends QItemDelegate implements AllColumns, DataConstants {
	
	static private Logger log = Logger.getLogger(TabTreeItemDelegate.class);
	
	private FilterModelManager modelManager;
	private SearchProfile searchProfile;
	private TabTreeView tabTreeView;
	
    public TabTreeItemDelegate(QWidget parent, FilterModelManager modelManager, TabTreeView tabTreeView) {
        super(parent);
        this.modelManager = modelManager;
        this.tabTreeView = tabTreeView;
    }

    public void setSearchProfile(SearchProfile searchProfile) {
    	this.searchProfile = searchProfile;
    }
    
    @Override
    public void paint(QPainter painter, QStyleOptionViewItem option, QModelIndex index) {
        Object data = index.data();
        if (data != null) {
            if (data instanceof StarRating) {
                if (option.state().isSet(QStyle.StateFlag.State_Selected) && option.state().isSet(QStyle.StateFlag.State_Active)) {
                    painter.fillRect(option.rect(), option.palette().highlight());
                }
                ((StarRating) data).paint(painter, option.rect(), option.palette(),
                                          StarRating.ReadOnly);
                return;
            }        	
        } 
        if (index.column() != 0) {
			QStyle.State state = option.state();
			state.clear(QStyle.StateFlag.State_Selected);
			option.setState(state);        	
        }
        super.paint(painter, option, index);
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
        if (data instanceof Accuracy) {
        	QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(index);
        	if (sourceIndex != null) {
        		FilterHierarchyInstance filterInstance = (FilterHierarchyInstance)((QStandardItemModel)modelManager.getSourceModel()).itemFromIndex(modelManager.getSourceModel().index(sourceIndex.row(), 0, sourceIndex.parent())).data();        		
	        	if (log.isTraceEnabled())
	        		log.trace("createEditor(): creating slider editor for filterInstance=" + filterInstance);
	        	QSlider slider = new QSlider(parent);
	        	slider.setMinimum(0);
	        	slider.setMaximum(100);
	        	slider.setTickInterval(1);
	        	slider.setOrientation(Orientation.Horizontal);        	
	        	slider.setValue(((Accuracy)data).getAccuracy());
	        	slider.setAutoFillBackground(true);
	        	slider.setMaximumWidth(200);
	        	slider.setTracking(false);
	        	//slider.sliderReleased.connect(new SliderReleasedListener(slider, filterInstance), "sliderReleased()");
	        	//slider.sliderMoved.connect(new SliderMovedListener(filterInstance), "sliderMoved(Integer)");
	        	slider.valueChanged.connect(new SliderValueChangedListener(slider, filterInstance), "valueChanged(Integer)");
	        	return slider;
        	}
        }
        return null;
        //return super.createEditor(parent, item, index);
    }

    @Override
    public void setEditorData(QWidget editor, QModelIndex index) {
        Object data = index.data();
        if (data instanceof Accuracy)
        	((QSlider) editor).setValue(((Accuracy)data).getAccuracy());
        else
            super.setEditorData(editor, index);
    }

    @Override
    public void setModelData(QWidget editor, QAbstractItemModel model, QModelIndex index) {
        if (index.data() instanceof Accuracy) {
        	QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(index);
        	FilterHierarchyInstance filterInstance = (FilterHierarchyInstance)((QStandardItemModel)modelManager.getSourceModel()).itemFromIndex(modelManager.getSourceModel().index(sourceIndex.row(), 0, sourceIndex.parent())).data();
        	FilterRecord filter = ((FilterHierarchyInstance)filterInstance).getFilterRecord();
        	float newDegree = Accuracy.getAccuracy(((QSlider) editor).value()).getAccuracyNormalized();
        	float oldDegree = 0.0f;
        	if (filter instanceof StyleRecord) {
        		oldDegree = searchProfile.getSourceStyleDegreeFromUniqueId(filter.getUniqueId());
        	} else if (filter instanceof TagRecord) {
        		oldDegree = searchProfile.getSourceTagDegreeFromUniqueId(filter.getUniqueId());
        	}
        	float difference = Math.abs(newDegree - oldDegree);
        	if (difference >= 0.01f) {        	
            	filterInstance.setNeedsRefresh(true);
	        	if (log.isTraceEnabled())
	        		log.trace("setModelData(): filter=" + filter + ", degree=" + newDegree);
	        	if (filter instanceof StyleRecord) {
	        		searchProfile.setStyle(new DegreeValue(((StyleRecord)filter).getStyleName(), newDegree, DATA_SOURCE_USER));
	        	} else if (filter instanceof TagRecord) {
	        		searchProfile.setTag(new DegreeValue(((TagRecord)filter).getTagName(), newDegree, DATA_SOURCE_USER));
	        	}
	        	ProfileWidgetUI.instance.setUpdateImmediately(true);
	        	TaskManager.runForegroundTask(new TreeUpdateTask());
        	}
        } else {
            super.setModelData(editor, model, index);
        }
    }    
    
    private class SliderValueChangedListener {
    	private QSlider slider;
    	private FilterHierarchyInstance filterInstance;;
    	public SliderValueChangedListener(QSlider slider, FilterHierarchyInstance filterInstance) {
    		this.slider = slider; 
    		this.filterInstance = filterInstance;
    	}
    	public void valueChanged(Integer value) {
    		if (log.isTraceEnabled())
    			log.trace("valueChanged(): value=" + value);
    		filterInstance.setNeedsRefresh(true);
    		FilterRecord filter = filterInstance.getFilterRecord();
        	float newDegree = Accuracy.getAccuracy(value).getAccuracyNormalized();
        	float oldDegree = 0.0f;
        	if (filter instanceof StyleRecord) {
        		oldDegree = searchProfile.getSourceStyleDegreeFromUniqueId(filter.getUniqueId());
        	} else if (filter instanceof TagRecord) {
        		oldDegree = searchProfile.getSourceTagDegreeFromUniqueId(filter.getUniqueId());
        	}
        	float difference = Math.abs(newDegree - oldDegree);
        	if (difference >= 0.01f) {        	        	
	        	if (log.isTraceEnabled())
	        		log.trace("valueChanged(): filter=" + filter + ", degree=" + newDegree);
	        	if (filter instanceof StyleRecord) {
	        		searchProfile.setStyle(new DegreeValue(((StyleRecord)filter).getStyleName(), newDegree, DATA_SOURCE_USER));
	        	} else if (filter instanceof TagRecord) {
	        		searchProfile.setTag(new DegreeValue(((TagRecord)filter).getTagName(), newDegree, DATA_SOURCE_USER), true);
	        	}
	        	ProfileWidgetUI.instance.setUpdateImmediately(true);
	        	TaskManager.runForegroundTask(new TreeUpdateTask());
        	}
    	}
    }
    
	private class TreeUpdateTask extends CommonTask {
		public String toString() {
			return "Updating Tab Tree";
		}
		public void execute() {
        	searchProfile.getRecord().update();
			QApplication.invokeLater(new ActionMenuUpdateThread());        	
		}
	}
	
	private class ActionMenuUpdateThread extends Thread {
		public void run() {
        	tabTreeView.updateActionMenu();
		}
	}
	
    
}