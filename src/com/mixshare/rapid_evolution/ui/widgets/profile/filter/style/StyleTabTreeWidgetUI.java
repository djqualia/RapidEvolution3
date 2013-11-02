package com.mixshare.rapid_evolution.ui.widgets.profile.filter.style;

import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.ProfileStyleModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.style.StyleTabTreeProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.TabTreeProxyModel;
import com.mixshare.rapid_evolution.ui.model.tree.TreeModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeWidget;
import com.trolltech.qt.core.QObject;

public class StyleTabTreeWidgetUI extends TabTreeWidget {
	
	static public StyleTabTreeWidgetUI instance;
	
	////////////
	// FIELDS //
	////////////
	
	private StyleTabTextInputSearchDelay searchDelay;

	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public StyleTabTreeWidgetUI(ProfileStyleModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
		searchDelay = new StyleTabTextInputSearchDelay(this);
		instance = this;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public ProfileStyleModelManager getProfileStyleModelManager() { return (ProfileStyleModelManager)getModelManager(); } 
	
	protected TabTreeView getTreeView(FilterModelManager modelManager) {
		return new StyleTabTreeView(this, modelManager);		
	}
	
	protected TabTreeProxyModel getTabTreeProxyModel(QObject parent, TreeModelManager modelManager, TabTreeView treeView) {
		return new StyleTabTreeProxyModel(parent, modelManager, treeView);
	}

	/////////////
	// METHODS //
	/////////////
	
	protected void searchTextChanged() {
		searchDelay.searchTextChanged(searchBarWidget.getFilterText().text());
	}
		
}
