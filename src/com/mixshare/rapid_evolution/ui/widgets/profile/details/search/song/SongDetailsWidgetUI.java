package com.mixshare.rapid_evolution.ui.widgets.profile.details.search.song;

import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;

public class SongDetailsWidgetUI extends DetailsWidgetUI {

	public SongDetailsWidgetUI(CommonDetailsModelManager modelManager) {
		super(modelManager);
	}
	
	protected DetailsTableView createTableView(CommonDetailsModelManager modelManager) {
		return new SongDetailsTableView(modelManager);
	}
	
	
}
