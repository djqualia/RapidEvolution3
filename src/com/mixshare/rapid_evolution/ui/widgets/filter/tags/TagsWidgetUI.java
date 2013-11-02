package com.mixshare.rapid_evolution.ui.widgets.filter.tags;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.FilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.filter.tag.TagModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.tag.TagProxyModel;
import com.mixshare.rapid_evolution.ui.util.TextInputSearchDelay;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterTreeView;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterWidgetUI;

public class TagsWidgetUI extends FilterWidgetUI {
	
	static private Logger log = Logger.getLogger(TagsWidgetUI.class);
    
	static public TagsWidgetUI instance = null;
    
    /////////////////
    // CONSTRUCOTR //
    /////////////////
    
    public TagsWidgetUI(TagModelManager modelManager) {
    	super(modelManager);
    	instance = this;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public FilterProxyModel getFilterProxyModel() { return new TagProxyModel(this, modelManager, filterView, this); }
    
    /////////////
    // METHODS //
    /////////////
    
    protected TextInputSearchDelay getTextInputSearchDelay() {
    	return new TagFilterTextInputSearchDelay();
    }
 
    public FilterTreeView createFilterTreeView(FilterModelManager modelManager) {
    	return new TagsTreeView(modelManager);
    }
    
}
