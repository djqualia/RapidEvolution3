package com.mixshare.rapid_evolution.ui.widgets.profile.details.search.artist;

import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;

public class ArtistDetailsWidgetUI extends DetailsWidgetUI {

	public ArtistDetailsWidgetUI(CommonDetailsModelManager modelManager) {
		super(modelManager);
	}
	
	protected DetailsTableView createTableView(CommonDetailsModelManager modelManager) {
		return new ArtistDetailsTableView(modelManager);
	}
	
	
}
