package com.mixshare.rapid_evolution.ui.widgets.profile.details.filter.tag;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.filter.FilterDetailsTableView;

public class TagDetailsTableView extends FilterDetailsTableView {

	public TagDetailsTableView(CommonDetailsModelManager modelManager) {
		super(modelManager);
	}
	
	protected Index getIndex() {
		return Database.getTagIndex();
	}
	
}
