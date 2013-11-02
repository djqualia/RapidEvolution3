package com.mixshare.rapid_evolution.ui.widgets.filter.styles;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.FilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.filter.style.StyleModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.style.StyleProxyModel;
import com.mixshare.rapid_evolution.ui.util.TextInputSearchDelay;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterTreeView;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterWidgetUI;

public class StylesWidgetUI extends FilterWidgetUI {
	
	static private Logger log = Logger.getLogger(StylesWidgetUI.class);

	static public StylesWidgetUI instance = null;
	
	/////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public StylesWidgetUI(StyleModelManager modelManager) {
    	super(modelManager);    	
    	instance = this;
    }

    /////////////
    // GETTERS //
    /////////////
    
    public FilterProxyModel getFilterProxyModel() { return new StyleProxyModel(this, modelManager, filterView, this); }    
    
    /////////////
    // METHODS //
    /////////////
    
    protected TextInputSearchDelay getTextInputSearchDelay() {
    	return new StyleFilterTextInputSearchDelay();
    }
    
    public FilterTreeView createFilterTreeView(FilterModelManager modelManager) {
    	return new StylesTreeView(modelManager);
    }
    
}
