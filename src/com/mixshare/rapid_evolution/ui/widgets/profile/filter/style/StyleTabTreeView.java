package com.mixshare.rapid_evolution.ui.widgets.profile.filter.style;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.ui.dialogs.filter.AddFilterDegreeDialog;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeWidget;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QIcon;

public class StyleTabTreeView extends TabTreeView {

	static private Logger log = Logger.getLogger(StyleTabTreeView.class);
	
	////////////
	// FIELDS //
	////////////
	
	private QAction addStyleAction;
	
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
	public StyleTabTreeView(TabTreeWidget tabTreeWidget, FilterModelManager modelManager) {
		super(tabTreeWidget, modelManager);		
		
		addStyleAction = new QAction(Translations.get("add_style_text"), this);
		addStyleAction.triggered.connect(this, "addStyle()");        
		addStyleAction.setIcon(new QIcon(RE3Properties.getProperty("menu_add_icon")));
		
    	addAction(addStyleAction);
    	addAction(addSeparator);    	
	}

	/////////////
	// GETTERS //
	/////////////
	
	protected int getFilterNameSourceColumnIndex() {
		return modelManager.getSourceColumnIndex(COLUMN_STYLE_NAME);
	}
	
	/////////////
	// METHODS //
	/////////////
	
	protected int getNumSelections() {
		if (searchProfile != null)
			return searchProfile.getNumSourceStyles();
		return 0;
	}
	
	protected float getFilterDegree(int profileUniqueId) {
		if (searchProfile != null)
			return searchProfile.getSourceStyleDegreeFromUniqueId(profileUniqueId);
		return 0.0f;
	}	
	
	public void clearAllSelection() {
		if (log.isTraceEnabled())
			log.trace("clearAllSelection(): clearing song selections=" + searchProfile);		
		invalidateProfileSelections();
		searchProfile.setStyles(new Vector<DegreeValue>());
		searchProfile.save();
		getProxyModel().invalidate();
		setupPersistentEditors();	    			
		updateActionMenu();		
	}

	public void invalidateProfileSelections() {
		for (DegreeValue sourceDegree : searchProfile.getSourceStyleDegrees()) {
			StyleRecord styleRecord = Database.getStyleIndex().getStyleRecord(new StyleIdentifier(sourceDegree.getName()));
			if (styleRecord != null) {
				for (TreeHierarchyInstance treeInstance : getFilterModelManager().getMatchingInstances(styleRecord)) {
					treeInstance.setNeedsRefresh(true);
				}
			}
		}		
	}
	
	public boolean enablePersistentEditors() { return true; }
	
	protected void addStyle() {
		try {
			if (!AddFilterDegreeDialog.isOpen()) {
		    	AddFilterDegreeDialog addFilterDialog = new AddFilterDegreeDialog(Database.getStyleIndex().getStyleModelManager().getStyleCompleter(), (FilterModelManager)modelManager, (SearchProfile)ProfileWidgetUI.instance.getCurrentProfile(), this);
		    	addFilterDialog.setTitle(Translations.get("add_style_text"));
		    	addFilterDialog.setLabel(Translations.get("style_name_text"));
		    	addFilterDialog.show();
		    	addFilterDialog.raise();
		    	addFilterDialog.activateWindow();
			} else {
				AddFilterDegreeDialog.focusInstance();
			}
		} catch (Exception e) {
			log.error("addStyle(): error", e);
		}
	}
	
	protected void sortByInvisibleColumn(Column viewColumn, int viewIndex) { }

    protected void selectedFilter(FilterHierarchyInstance filterInstance) { }
	
}
