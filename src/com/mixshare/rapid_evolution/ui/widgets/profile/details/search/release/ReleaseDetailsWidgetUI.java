package com.mixshare.rapid_evolution.ui.widgets.profile.details.search.release;

import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;

public class ReleaseDetailsWidgetUI extends DetailsWidgetUI {

	public ReleaseDetailsWidgetUI(CommonDetailsModelManager modelManager) {
		super(modelManager);
	}
	
	protected DetailsTableView createTableView(CommonDetailsModelManager modelManager) {
		return new ReleaseDetailsTableView(modelManager);
	}
	
	
}
