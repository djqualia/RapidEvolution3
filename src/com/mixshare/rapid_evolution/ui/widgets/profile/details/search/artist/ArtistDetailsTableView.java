package com.mixshare.rapid_evolution.ui.widgets.profile.details.search.artist;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.search.SearchDetailsTableView;

public class ArtistDetailsTableView extends SearchDetailsTableView {

	public ArtistDetailsTableView(CommonDetailsModelManager modelManager) {
		super(modelManager);
	}
	
	protected Index getIndex() {
		return Database.getArtistIndex();
	}
	
}
