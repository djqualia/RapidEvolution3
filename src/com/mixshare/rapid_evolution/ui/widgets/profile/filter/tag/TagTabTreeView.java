package com.mixshare.rapid_evolution.ui.widgets.profile.filter.tag;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
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

public class TagTabTreeView extends TabTreeView {

	static private Logger log = Logger.getLogger(TagTabTreeView.class);
	
	////////////
	// FIELDS //
	////////////
		
	private QAction addTagAction;
	
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
	public TagTabTreeView(TabTreeWidget tabTreeWidget, FilterModelManager modelManager) {
		super(tabTreeWidget, modelManager);
		
		addTagAction = new QAction(Translations.get("add_tag_text"), this);
		addTagAction.triggered.connect(this, "addTag()");        
		addTagAction.setIcon(new QIcon(RE3Properties.getProperty("menu_add_icon")));
		
    	addAction(addTagAction);
    	addAction(addSeparator);
	}

	/////////////
	// GETTERS //
	/////////////
	
	protected int getFilterNameSourceColumnIndex() {
		return modelManager.getSourceColumnIndex(COLUMN_TAG_NAME);
	}
	
	/////////////
	// METHODS //
	/////////////
	
	protected int getNumSelections() {
		if (searchProfile != null)
			return searchProfile.getNumSourceTags();
		return 0;
	}
	
	protected float getFilterDegree(int profileUniqueId) {
		if (searchProfile != null)
			return searchProfile.getSourceTagDegreeFromUniqueId(profileUniqueId);
		return 0.0f;
	}	
	
	public void clearAllSelection() {
		if (log.isTraceEnabled())
			log.trace("clearAllSelection(): clearing song selections=" + searchProfile);
		invalidateProfileSelections();
		searchProfile.setTags(new Vector<DegreeValue>());
		searchProfile.save();
		getProxyModel().invalidate();
		setupPersistentEditors();	    					
		updateActionMenu();
	}	
	
	public void invalidateProfileSelections() {
		for (DegreeValue sourceDegree : searchProfile.getSourceTagDegrees()) {
			TagRecord tagRecord = Database.getTagIndex().getTagRecord(new TagIdentifier(sourceDegree.getName()));
			if (tagRecord != null) {
				for (TreeHierarchyInstance treeInstance : getFilterModelManager().getMatchingInstances(tagRecord)) {
					treeInstance.setNeedsRefresh(true);
				}
			}
		}		
	}
		
	public boolean enablePersistentEditors() {
		if (tabTreeWidget.isShowAll())
			return false;
		return true;
	}	
	
	protected void addTag() {
		try {
			if (!AddFilterDegreeDialog.isOpen()) {
				AddFilterDegreeDialog addFilterDialog = new AddFilterDegreeDialog(Database.getTagModelManager().getTagCompleter(), (FilterModelManager)modelManager, (SearchProfile)ProfileWidgetUI.instance.getCurrentProfile(), this);
		    	addFilterDialog.setTitle(Translations.get("add_tag_text"));
		    	addFilterDialog.setLabel(Translations.get("tag_name_text"));	    	
		    	addFilterDialog.show();
		    	addFilterDialog.raise();
		    	addFilterDialog.activateWindow();
			} else {
				AddFilterDegreeDialog.focusInstance();
			}
		} catch (Exception e) {
			log.error("addTag(): error", e);
		}
	}

	protected void sortByInvisibleColumn(Column viewColumn, int viewIndex) { }

    protected void selectedFilter(FilterHierarchyInstance filterInstance) { }
	
}
