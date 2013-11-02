package com.mixshare.rapid_evolution.ui.widgets.profile.details.search.label;

import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;

public class LabelDetailsWidgetUI extends DetailsWidgetUI {

	public LabelDetailsWidgetUI(CommonDetailsModelManager modelManager) {
		super(modelManager);
	}
	
	protected DetailsTableView createTableView(CommonDetailsModelManager modelManager) {
		return new LabelDetailsTableView(modelManager);
	}
	
	
}
