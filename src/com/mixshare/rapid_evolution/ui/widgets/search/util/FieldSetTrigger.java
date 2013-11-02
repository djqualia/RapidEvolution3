package com.mixshare.rapid_evolution.ui.widgets.search.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.dialogs.fields.TextFieldInputDialog;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.search.InternalSearchTableView;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.maintenance.FieldSetTask;
import com.trolltech.qt.gui.QCompleter;
import com.trolltech.qt.gui.QDialog;

public class FieldSetTrigger implements AllColumns {

	static private Logger log = Logger.getLogger(FieldSetTrigger.class);
	
	private Column column;
	private InternalSearchTableView searchView;
	private SearchDetailsModelManager detailsModelManager;
	
	public FieldSetTrigger(Column column, InternalSearchTableView searchView, SearchDetailsModelManager detailsModelManager) {
		this.column = column;
		this.searchView = searchView;
		this.detailsModelManager = detailsModelManager;
	}
	
	public void setFields() {
		// first get default value for field (for convenience)
		Map<String, Integer> existingValueCounts = new HashMap<String, Integer>();
		Vector<SearchRecord> selectedRecords = searchView.getSelectedRecords();
		for (SearchRecord searchRecord : selectedRecords) {
			Object data = searchView.getRecordTableModelManager().getSourceData(column.getColumnId(), searchRecord);
			if (data != null) {
				String value = data.toString();
				if (value.length() > 0) {
					Integer existingCount = existingValueCounts.get(value);
					if (existingCount == null)
						existingCount = 1;
					else
						existingCount += 1;
					existingValueCounts.put(value, existingCount);
				}
			}
		}
		int maxCount = 0;
		String maxValue = "";
		for (Entry<String, Integer> entry : existingValueCounts.entrySet()) {
			if (entry.getValue() > maxCount) {
				maxCount = entry.getValue();
				maxValue = entry.getKey();
			}
		}		
		QCompleter completer = null;
		if (column.getColumnId() == COLUMN_ARTIST_DESCRIPTION.getColumnId())			
			completer = Database.getArtistModelManager().getArtistCompleter();
		else if (column.getColumnId() == COLUMN_LABELS.getColumnId())			
			completer = Database.getLabelModelManager().getLabelCompleter();
		else if (column.getColumnId() == COLUMN_RELEASE_TITLE.getColumnId())			
			completer = Database.getReleaseModelManager().getReleaseTitleCompleter();
		
    	TextFieldInputDialog textFieldInputDialog = new TextFieldInputDialog(Translations.get("field_set_prefix") + " " + column.getColumnTitle(), column.getColumnTitle(), maxValue, completer);
    	if (textFieldInputDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		String newValue = textFieldInputDialog.getFilterName();
    		TaskManager.runForegroundTask(new FieldSetTask(detailsModelManager, column, newValue, selectedRecords));
    	}
		
	}
	
}
