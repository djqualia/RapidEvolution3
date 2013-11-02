package com.mixshare.rapid_evolution.ui.widgets.profile.details.filter.style;

import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;

public class StyleDetailsWidgetUI extends DetailsWidgetUI {

	public StyleDetailsWidgetUI(CommonDetailsModelManager modelManager) {
		super(modelManager);
	}
	
	protected DetailsTableView createTableView(CommonDetailsModelManager modelManager) {
		return new StyleDetailsTableView(modelManager);
	}
	
	
}
