package com.mixshare.rapid_evolution.ui.widgets.profile.details.filter.playlist;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.filter.FilterDetailsTableView;

public class PlaylistDetailsTableView extends FilterDetailsTableView {

	public PlaylistDetailsTableView(CommonDetailsModelManager modelManager) {
		super(modelManager);
	}
	
	protected Index getIndex() {
		return Database.getPlaylistIndex();
	}
	
}
