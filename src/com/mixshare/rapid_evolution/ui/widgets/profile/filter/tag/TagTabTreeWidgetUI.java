package com.mixshare.rapid_evolution.ui.widgets.profile.filter.tag;

import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.ProfileTagModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagTabTreeProxyModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.TabTreeProxyModel;
import com.mixshare.rapid_evolution.ui.model.tree.TreeModelManager;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeWidget;
import com.trolltech.qt.core.QObject;

public class TagTabTreeWidgetUI extends TabTreeWidget {
	
	static public TagTabTreeWidgetUI instance;
	
	////////////
	// FIELDS //
	////////////
	
	private TagTabTextInputSearchDelay searchDelay;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public TagTabTreeWidgetUI(ProfileTagModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
		searchDelay = new TagTabTextInputSearchDelay(this);
		instance = this;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public ProfileTagModelManager getProfileTagModelManager() { return (ProfileTagModelManager)getModelManager(); } 
	
	protected TabTreeView getTreeView(FilterModelManager modelManager) {
		return new TagTabTreeView(this, modelManager);		
	}
	
	protected TabTreeProxyModel getTabTreeProxyModel(QObject parent, TreeModelManager modelManager, TabTreeView treeView) {
		return new TagTabTreeProxyModel(parent, modelManager, treeView);
	}		
	
	/////////////
	// METHODS //
	/////////////
	
	protected void searchTextChanged() {
		searchDelay.searchTextChanged(searchBarWidget.getFilterText().text());
	}
	
}
