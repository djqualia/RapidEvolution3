package com.mixshare.rapid_evolution.ui.widgets.search;

import java.util.Vector;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMessageBox;

public class RecommendedSearchTableView extends SearchTableView {

	////////////
	// FIELDS //
	////////////
	
	protected QAction hideAction;
    protected QAction separator1;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public RecommendedSearchTableView(SearchModelManager modelManager) {
		super(modelManager);
		
		hideAction = new QAction(Translations.get("hide_text"), this);
		hideAction.triggered.connect(this, "hideRecords()");
		hideAction.setIcon(new QIcon(RE3Properties.getProperty("menu_hide_icon")));
		
        separator1 = new QAction("", this);
        separator1.setSeparator(true);		
	}
	
	
	/////////////
	// METHODS //
	/////////////
	
	protected void selectionChanged() {
    	Vector<SearchRecord> selectedStyles = getSelectedRecords();
        removeAction(mergeAction);
        removeAction(hideAction);
    	if (selectedStyles.size() == 1) {
            addAction(hideAction);     
    	} else if (selectedStyles.size() > 1) {
            addAction(hideAction);     
            addAction(separator1);
            addAction(mergeAction);
    	}
    	SearchWidgetUI.instance.updateSelectedLabel(selectedStyles.size());
    }	    
	
	protected void hideRecords() {
		if (QMessageBox.question(this, Translations.get("dialog_hide_recommended_title"), Translations.get("dialog_hide_recommended_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}    			
    	Vector<SearchRecord> selectedSearchRecords = getSelectedRecords();
    	for (SearchRecord searchRecord : selectedSearchRecords) {
    		searchRecord.setDisabled(true);
    		searchRecord.update();
    	}
	}
	
}
