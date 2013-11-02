package com.mixshare.rapid_evolution.ui.widgets.profile.details.filter.playlist;

import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;

public class PlaylistDetailsWidgetUI extends DetailsWidgetUI {

	public PlaylistDetailsWidgetUI(CommonDetailsModelManager modelManager) {
		super(modelManager);
	}
	
	protected DetailsTableView createTableView(CommonDetailsModelManager modelManager) {
		return new PlaylistDetailsTableView(modelManager);
	}
	
	
}
