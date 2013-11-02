package com.mixshare.rapid_evolution.ui.widgets.profile.tab;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.table.CommonTableView;

public class TabTableView extends CommonTableView {

	static private Logger log = Logger.getLogger(TabTableView.class);

	////////////
	// FIELDS //
	////////////
		
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
	public TabTableView(TableModelManager modelManager) {
		super(modelManager);				        
        setDragEnabled(false);		
	}	    
    
}
