package com.mixshare.rapid_evolution.ui.widgets.filter.playlists;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.filter.playlist.DynamicPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedCategoryPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedDynamicPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedOrderedPlaylist;
import com.mixshare.rapid_evolution.ui.dialogs.filter.AddFilterDialog;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.CentralWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterTreeView;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMenu;

public class PlaylistsTreeView extends FilterTreeView implements DataConstants {

	static private Logger log = Logger.getLogger(PlaylistsTreeView.class);
	
    protected QMenu addMenu;
    protected QAction addMenuAction;
    protected QAction addDynamic;
    protected QAction addOrdered;
    protected QAction addCategory;
	
    public PlaylistsTreeView(FilterModelManager modelManager) {
    	super(modelManager);   
    	
    	addDynamic = new QAction(Translations.get("playlist_menu_add_dynamic"), this);
    	addDynamic.triggered.connect(this, "addDynamic()");		

    	addOrdered = new QAction(Translations.get("playlist_menu_add_ordered"), this);
    	addOrdered.triggered.connect(this, "addOrdered()");		

    	addCategory = new QAction(Translations.get("playlist_menu_add_category"), this);
    	addCategory.triggered.connect(this, "addCategory()");		
    	
    	addMenu = new QMenu(Translations.get("filter_menu_add"), this);
    	addMenu.addAction(addDynamic);
    	addMenu.addAction(addOrdered);
    	addMenu.addAction(addCategory);
    	addMenuAction = new QAction(Translations.get("filter_menu_add"), this);
    	addMenuAction.setMenu(addMenu);
    	addMenuAction.setIcon(new QIcon(RE3Properties.getProperty("menu_add_icon")));
    	
    }

    public QAction getAddAction() { return addMenuAction; }
    
    protected void executeSelectionChangeActions() {
    	if (CentralWidgetUI.instance != null)
    		CentralWidgetUI.instance.setPlaylistsItemText(this.getFilterSelection().size());
    	super.executeSelectionChangeActions();
    }
    
    private void addDynamic() {
    	try {
        	Vector<FilterHierarchyInstance> selectedInstances = getCurrentSelectedInstances();
        	if (log.isDebugEnabled())
        		log.debug("add(): selectdInstances=" + selectedInstances);
        	AddFilterDialog addFilterDialog = new AddFilterDialog(modelManager.getTypeDescription());
	    	if (addFilterDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	    		String newFilterName = addFilterDialog.getFilterName();
	    		FilterIdentifier newId = getFilterModelManager().getFilterIdentifier(newFilterName);
	    		if (Database.getRecord(newId) != null) {
	    			if (log.isDebugEnabled())
	    				log.debug("add(): filter already exists with name=" + newFilterName);
	    			ProfileWidgetUI.instance.editProfile(Database.getProfile(newId));
	    		} else {
	    			SubmittedDynamicPlaylist newFilter = new SubmittedDynamicPlaylist(newFilterName);
	    			newFilter.setParentFilterInstances(selectedInstances);
	    			Profile profile = Database.add(newFilter);
	    			if (profile != null) {
	    				ProfileWidgetUI.instance.editProfile(profile);
	    			}	    			
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("add(): error", e);
    	}    	
    }
    
    private void addOrdered() {
    	try {
        	Vector<FilterHierarchyInstance> selectedInstances = getCurrentSelectedInstances();
        	if (log.isDebugEnabled())
        		log.debug("add(): selectdInstances=" + selectedInstances);
        	AddFilterDialog addFilterDialog = new AddFilterDialog(modelManager.getTypeDescription());
	    	if (addFilterDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	    		String newFilterName = addFilterDialog.getFilterName();
	    		FilterIdentifier newId = getFilterModelManager().getFilterIdentifier(newFilterName);
	    		if (Database.getRecord(newId) != null) {
	    			if (log.isDebugEnabled())
	    				log.debug("add(): filter already exists with name=" + newFilterName);
	    			ProfileWidgetUI.instance.editProfile(Database.getProfile(newId));
	    		} else {
	    			SubmittedOrderedPlaylist newFilter = new SubmittedOrderedPlaylist(newFilterName);
	    			newFilter.setParentFilterInstances(selectedInstances);
	    			Profile profile = Database.add(newFilter);
	    			if (profile != null) {
	    				ProfileWidgetUI.instance.editProfile(profile);
	    			}	    			
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("add(): error", e);
    	}    	
    }

    private void addCategory() {
    	try {
        	Vector<FilterHierarchyInstance> selectedInstances = getCurrentSelectedInstances();
        	if (log.isDebugEnabled())
        		log.debug("add(): selectdInstances=" + selectedInstances);
        	AddFilterDialog addFilterDialog = new AddFilterDialog(modelManager.getTypeDescription());
	    	if (addFilterDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	    		String newFilterName = addFilterDialog.getFilterName();
	    		FilterIdentifier newId = getFilterModelManager().getFilterIdentifier(newFilterName);
	    		if (Database.getRecord(newId) != null) {
	    			if (log.isDebugEnabled())
	    				log.debug("add(): filter already exists with name=" + newFilterName);
	    			ProfileWidgetUI.instance.editProfile(Database.getProfile(newId));
	    		} else {
	    			SubmittedCategoryPlaylist newFilter = new SubmittedCategoryPlaylist(newFilterName);
	    			newFilter.setParentFilterInstances(selectedInstances);
	    			Profile profile = Database.add(newFilter);
	    			if (profile != null) {
	    				ProfileWidgetUI.instance.editProfile(profile);
	    			}	    				    			
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("add(): error", e);
    	}    	
    }    
    
    protected void sortByName() { sortByColumn(COLUMN_PLAYLIST_NAME.getColumnId()); }
    
    protected boolean areSelectedInstancesMergable(Vector<FilterHierarchyInstance> selectedInstances) {
    	Map<Class, Object> types = new HashMap<Class, Object>();
    	for (FilterHierarchyInstance instance : selectedInstances)
    		types.put(instance.getFilterRecord().getClass(), null);    	
    	if (types.size() > 1)
    		return false;
    	return true;
    }
    
    protected void sortByInvisibleColumn(Column viewColumn, int viewIndex) {
		viewColumn.setHidden(false);
		PlaylistsWidgetUI.instance.updateVisibleColumns();
		sortByColumn(viewIndex, SortOrder.AscendingOrder);    	
    }
    
    protected void selectedFilter(FilterHierarchyInstance filterInstance) {
    	if (RE3Properties.getBoolean("restore_playlist_sort_parameters")) {
	    	PlaylistRecord playlist = (PlaylistRecord)filterInstance.getRecord();
	    	if (playlist instanceof DynamicPlaylistRecord) {
	    		SearchSearchParameters[] searchParams = null;
	    		DynamicPlaylistRecord dynamicPlaylist = (DynamicPlaylistRecord)playlist;
	    		Byte dataType = null;
	    		if (SearchWidgetUI.instance.getCurrentSearchType() == 1) {
	    			searchParams = dynamicPlaylist.getArtistSearchParameters();
	    			dataType = DATA_TYPE_ARTISTS;
	    		} else if (SearchWidgetUI.instance.getCurrentSearchType() == 2) {
	    			searchParams = dynamicPlaylist.getLabelSearchParameters();
	    			dataType = DATA_TYPE_LABELS;
	    		} else if (SearchWidgetUI.instance.getCurrentSearchType() == 3) {
	    			searchParams = dynamicPlaylist.getReleaseSearchParameters();
	    			dataType = DATA_TYPE_RELEASES;
	    		} else if (SearchWidgetUI.instance.getCurrentSearchType() == 4) {
	    			searchParams = dynamicPlaylist.getSongSearchParameters();
	    			dataType = DATA_TYPE_SONGS;
	    		}
	    		if ((searchParams != null) && (searchParams.length > 0)) {
	    			SearchSearchParameters searchParam = searchParams[0];
	    			byte[] sortTypes = searchParam.getSortType();
	    			boolean[] isSortDescending = searchParam.isSortDescending();
	    			if (sortTypes != null) {
		    			Vector<ColumnOrdering> sortOrdering = new Vector<ColumnOrdering>();
		    			for (int i = 0; i < sortTypes.length; ++i) {    				
		    				Short columnId = null;
		    				byte sortType = sortTypes[i];
		    				columnId = CommonSearchParameters.getColumnIdFromSortType(sortType, dataType);
		    				if (columnId != null) {    					
		    					ColumnOrdering ordering = new ColumnOrdering(columnId, !isSortDescending[i]);
		    					sortOrdering.add(ordering);    					
		    				}
		    			}
		    			if (sortOrdering.size() > 0) {
		        			if (log.isDebugEnabled())
		        				log.debug("selectedFilter(): setting sort ordering from playlist=" + sortOrdering);        			
		        			SearchWidgetUI.instance.setSortOrdering(sortOrdering, sortTypes, isSortDescending);
		    			}
	    			}
	    		}    		
	    	}
    	}
    }
    
}
