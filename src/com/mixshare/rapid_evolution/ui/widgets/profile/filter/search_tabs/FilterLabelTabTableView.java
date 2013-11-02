package com.mixshare.rapid_evolution.ui.widgets.profile.filter.search_tabs;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.FilterTabTableView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMessageBox;

public class FilterLabelTabTableView extends FilterTabTableView {
	
	static private Logger log = Logger.getLogger(FilterLabelTabTableView.class);
	
	private QAction removeAction;
	
	public FilterLabelTabTableView(RecordTableModelManager modelManager) {
		super(modelManager);
		
		removeAction = new QAction(Translations.get("remove_text"), this);
		removeAction.triggered.connect(this, "removeLabels()"); 
		removeAction.setIcon(new QIcon(RE3Properties.getProperty("menu_remove_icon")));
		
	}
	
    protected void selectionChanged() {    	
    	Vector<SearchRecord> selectedRecords = getSelectedRecords();
    	boolean containsPlayable = false;
    	for (SearchRecord searchRecord : selectedRecords) {
    		if (!searchRecord.isExternalItem()) {
    			containsPlayable = true;
    			break;
    		}
    	}
        removeAction(playAction);
        removeAction(separator1);
        removeAction(mergeAction);
        removeAction(separator2);
        removeAction(removeAction);
        boolean added = false;
        if (containsPlayable) {
        	addAction(playAction);
        	added = true;
        }
        if (selectedRecords.size() > 1) {
        	if (added)
        		addAction(separator1);
        	addAction(mergeAction);
        }
        if (selectedRecords.size() > 0) {
        	if (added)
        		addAction(separator2);
        	addAction(removeAction);
        }
    }	
	
	public void dropEvent(QDropEvent event) {				
		if (log.isTraceEnabled())
			log.trace("dropEvent(): event=" + event);
		Vector<LabelRecord> labels = DragDropUtil.getLabels(event.mimeData());
		FilterProfile filter = (FilterProfile)ProfileWidgetUI.instance.getCurrentProfile();
		filter.getFilterRecord().addLabelRecords(labels);
		filter.getFilterRecord().computeNumLabelRecords();
		filter.getFilterRecord().update();
	}	

	
	private void removeLabels() {
		if (QMessageBox.question(this, Translations.get("dialog_remove_filter_record_title"), Translations.get("dialog_remove_filter_record_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}    			
		Vector<SearchRecord> selectedRecords = getSelectedRecords();
		Vector<LabelRecord> selectedLabels = new Vector<LabelRecord>(selectedRecords.size());
		FilterRecord filterRecord = ((FilterProfile)ProfileWidgetUI.instance.getCurrentProfile()).getFilterRecord();
		if (log.isDebugEnabled())
			log.debug("removeLabels(): removing labels=" + selectedLabels + ", from filter=" + filterRecord);			
		for (SearchRecord record : selectedRecords)
			selectedLabels.add((LabelRecord)record);
		filterRecord.removeLabelRecords(selectedLabels);
		boolean containsInternal = false;
		boolean containsExternal = false;
		for (LabelRecord label : selectedLabels) {
			if (label.isExternalItem())
				containsExternal = true;
			else
				containsInternal = true;
		}			
		if (containsInternal)
			filterRecord.computeNumLabelRecords();
		if (containsExternal)
			filterRecord.computeNumExternalLabelRecords();			
		filterRecord.update();
	}	
}
