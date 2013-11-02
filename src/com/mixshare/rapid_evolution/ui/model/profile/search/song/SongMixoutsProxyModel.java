package com.mixshare.rapid_evolution.ui.model.profile.search.song;

import java.util.ArrayList;
import java.util.List;

import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QObject;

public class SongMixoutsProxyModel extends RecordTabTableProxyModel {

	public SongMixoutsProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView) {
		super(parent, modelManager, tableView);
		searchParameters = new SongSearchParameters();
	}

	/**
	 * Lists the accepted mime types here...
	 */
	public List<String> mimeTypes() {
		List<String> result = new ArrayList<String>();
		result.add(DragDropUtil.MIME_TYPE_SONG_IDENTIFIER_LIST);
		return result;
	}		
	
}
