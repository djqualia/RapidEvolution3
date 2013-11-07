package com.mixshare.rapid_evolution.ui.widgets.search.release;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.SongGroupProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.SongGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.release.ReleaseDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.search.InternalSearchTableView;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.DeleteRecordsTask;
import com.mixshare.rapid_evolution.workflow.user.DeleteReleasesTask;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QMessageBox;

public class ReleaseTableView extends InternalSearchTableView {

	////////////
	// FIELDS //
	////////////
	
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public ReleaseTableView(SearchModelManager modelManager) {
		super(modelManager);
		
	}	
	
	/////////////
	// GETTERS //
	/////////////
	
	protected SearchDetailsModelManager getDetailsModelManager() {
		return (ReleaseDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ReleaseDetailsModelManager.class);
	}
	
	/////////////
	// METHODS //
	/////////////

	protected void deleteRecords() {
		if (QMessageBox.question(this, Translations.get("dialog_delete_record_title"), Translations.get("dialog_delete_record_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}    	
    	List<QModelIndex> deletedIndexes = selectionModel().selectedRows();
    	Vector<Record> deletedRecords = new Vector<Record>(deletedIndexes.size());
    	for (QModelIndex index : deletedIndexes) {    		    		
    		QModelIndex sourceIndex = getProxyModel().mapToSource(index);    		
    		SearchRecord record = (SearchRecord)getRecordTableModelManager().getRecordForRow(sourceIndex.row());
			if (!deletedRecords.contains(record))
				deletedRecords.add(record);    		
    	}
    	TaskManager.runForegroundTask(new DeleteReleasesTask(deletedRecords));    	
    }   
	
	
}
