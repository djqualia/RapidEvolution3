package com.mixshare.rapid_evolution.ui.widgets.profile.details.search.song;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.search.SearchDetailsTableView;

public class SongDetailsTableView extends SearchDetailsTableView {

	public SongDetailsTableView(CommonDetailsModelManager modelManager) {
		super(modelManager);
	}
	
	protected Index getIndex() {
		return Database.getSongIndex();
	}
	
}
