package com.mixshare.rapid_evolution.ui.model.filter.tag;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.search.parameters.filter.FilterSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.filter.tag.TagSearchParameters;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.FilterProxyModel;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterTreeView;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterWidgetUI;
import com.trolltech.qt.core.QObject;

public class TagProxyModel extends FilterProxyModel {

	public TagProxyModel(QObject parent, FilterModelManager modelManager, FilterTreeView treeView, FilterWidgetUI widget) {
		super(parent, modelManager, treeView, widget);
	}
	
	public boolean isHideEmptyFilters() { return RE3Properties.getBoolean("tag_filters_hide_empty"); }
	public void setHideEmptyFilters(boolean hideEmptyFilters) {
		if (hideEmptyFilters)
			RE3Properties.setProperty("tag_filters_hide_empty", "true");
		else
			RE3Properties.setProperty("tag_filters_hide_empty", "false");
	}	
	
	public FilterSearchParameters getNewFilterSearchParameters() { return new TagSearchParameters(); }
	
	public boolean isLazySearchSupported() { return RE3Properties.getBoolean("tag_model_supports_lazy"); }
	
}
