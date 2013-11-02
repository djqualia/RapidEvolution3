package com.mixshare.rapid_evolution.ui.widgets.profile.details.filter.tag;

import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;

public class TagDetailsWidgetUI extends DetailsWidgetUI {

	public TagDetailsWidgetUI(CommonDetailsModelManager modelManager) {
		super(modelManager);
	}
	
	protected DetailsTableView createTableView(CommonDetailsModelManager modelManager) {
		return new TagDetailsTableView(modelManager);
	}
	
	
}
