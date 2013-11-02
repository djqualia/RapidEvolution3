package com.mixshare.rapid_evolution.ui.widgets.search.util;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.search.InternalSearchTableView;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.maintenance.FieldClearTask;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QWidget;

public class FieldClearTrigger {

	static private Logger log = Logger.getLogger(FieldClearTrigger.class);
	
	private QWidget parent;
	private Column column;
	private InternalSearchTableView searchView;
	private SearchDetailsModelManager detailsModelManager;
	
	public FieldClearTrigger(QWidget parent, Column column, InternalSearchTableView searchView, SearchDetailsModelManager detailsModelManager) {
		this.parent = parent;
		this.column = column;
		this.searchView = searchView;
		this.detailsModelManager = detailsModelManager;
	}
	
	public void clearFields() {
		if (log.isDebugEnabled())
			log.debug("clearFields(): column=" + column.getColumnTitle());
		String dialogText = Translations.get("dialog_clear_fields_text");
		dialogText = StringUtil.replace(dialogText, Translations.getPreferredCase("%fieldName%"), column.getColumnTitle());
		if (QMessageBox.question(parent, Translations.get("dialog_clear_fields_title"), dialogText, QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}    	
		TaskManager.runForegroundTask(new FieldClearTask(detailsModelManager, column, searchView.getSelectedRecords()));
	}
	
}
