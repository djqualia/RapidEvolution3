package com.mixshare.rapid_evolution.ui.widgets.search.artist;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.SongGroupProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.artist.ArtistDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.search.InternalSearchTableView;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.DeleteRecordsTask;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QMessageBox;

public class ArtistTableView extends InternalSearchTableView {

	////////////
	// FIELDS //
	////////////


    /////////////////
    // CONSTRUCTOR //
    /////////////////

	public ArtistTableView(SearchModelManager modelManager) {
		super(modelManager);


	}

	/////////////
	// GETTERS //
	/////////////

	@Override
	protected SearchDetailsModelManager getDetailsModelManager() {
		return (ArtistDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ArtistDetailsModelManager.class);
	}

	/////////////
	// METHODS //
	/////////////

	@Override
	protected void deleteRecords() {
		if (QMessageBox.question(this, Translations.get("dialog_delete_record_title"), Translations.get("dialog_delete_record_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}
    	List<QModelIndex> deletedIndexes = selectionModel().selectedRows();
    	Vector<Record> deletedRecords = new Vector<Record>(deletedIndexes.size());
    	for (QModelIndex index : deletedIndexes) {
    		QModelIndex sourceIndex = getProxyModel().mapToSource(index);
    		SearchRecord record = (SearchRecord)getRecordTableModelManager().getRecordForRow(sourceIndex.row());
			SongGroupProfile profile = (SongGroupProfile)Database.getProfile(record.getIdentifier());
			Iterator<Integer> songIter = profile.getSongIds().iterator();
			while (songIter.hasNext()) {
				Record associatedSong = Database.getSongIndex().getRecord(songIter.next());
				if (!deletedRecords.contains(associatedSong))
					deletedRecords.add(associatedSong);
			}
    	}
    	TaskManager.runForegroundTask(new DeleteRecordsTask(deletedRecords));
    }

}
