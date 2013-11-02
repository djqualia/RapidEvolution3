package com.mixshare.rapid_evolution.ui.widgets.filter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.OrderedPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.filter.SubmittedFilterProfile;
import com.mixshare.rapid_evolution.player.PlayerManager;
import com.mixshare.rapid_evolution.ui.dialogs.columns.SelectColumnsDialog;
import com.mixshare.rapid_evolution.ui.dialogs.filter.AddFilterDialog;
import com.mixshare.rapid_evolution.ui.dialogs.merge.MergeRecordsDialog;
import com.mixshare.rapid_evolution.ui.event.FilterHierarchyChangeListener;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.FilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.util.Color;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.tree.CommonTreeView;
import com.mixshare.rapid_evolution.ui.widgets.filter.playlists.PlaylistsTreeView;
import com.mixshare.rapid_evolution.ui.widgets.filter.styles.StylesTreeView;
import com.mixshare.rapid_evolution.ui.widgets.filter.tags.TagsTreeView;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.DeleteRecordsTask;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.KeyboardModifier;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QSortFilterProxyModel;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QStyle;
import com.trolltech.qt.gui.QStyleOptionViewItem;

abstract public class FilterTreeView extends CommonTreeView implements AllColumns {
	
	static private Logger log = Logger.getLogger(FilterTreeView.class);
	
	static private Color FILTER_MATCH_COLOR = RE3Properties.getColor("filter_match_color");
	
	////////////
	// FIELDS //
	////////////
	
	protected FilterModelManager modelManager;
	
    private QAction playAction;
    private QAction separator1;
    private QAction editAction;
    private QAction mergeAction;
    private QAction separator2;
    protected QAction addAction;
    private QAction deleteAction;
    private QAction hideAction;
    private QAction separator3;
    private QAction expandAllAction;
    private QAction collapseAllAction;
    private QAction separator4;
    private QAction clearAllAction;
    private QAction separator5;
    private QAction hideEmptyAction;
    protected QAction loadAction;
    
    private QMenu sortMenu;
    private QAction sortMenuAction;
    private QAction sortByNameAction;
    private QAction sortByNumArtistsAction;
    private QAction sortByNumLabelsAction;
    private QAction sortByNumReleasesAction;
    private QAction sortByNumSongsAction;
        
    transient private Vector<FilterHierarchyChangeListener> hierarchyChangeListeners = new Vector<FilterHierarchyChangeListener>();
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(FilterTreeView.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("hierarchyChangeListeners")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public FilterTreeView(FilterModelManager modelManager) {
    	super(modelManager);    	
    	this.modelManager = modelManager;
    				        
        playAction = new QAction(Translations.get("play_text"), this);
        playAction.triggered.connect(this, "play()");
        playAction.setIcon(new QIcon(RE3Properties.getProperty("menu_play_icon")));

        separator1 = new QAction("", this);
        separator1.setSeparator(true);

        editAction = new QAction(Translations.get("filter_menu_edit"), this);
        editAction.triggered.connect(this, "edit()");
        editAction.setIcon(new QIcon(RE3Properties.getProperty("menu_edit_icon")));
        
        mergeAction = new QAction(Translations.get("filter_menu_merge"), this);
        mergeAction.triggered.connect(this, "merge()");
        mergeAction.setIcon(new QIcon(RE3Properties.getProperty("menu_merge_icon")));
        
        separator2 = new QAction("", this);
        separator2.setSeparator(true);
        
        addAction = new QAction(Translations.get("filter_menu_add"), this);
        addAction.triggered.connect(this, "add()");
        addAction.setIcon(new QIcon(RE3Properties.getProperty("menu_add_icon")));
        
        deleteAction = new QAction(Translations.get("filter_menu_delete"), this);
        deleteAction.triggered.connect(this, "delete()");
        deleteAction.setIcon(new QIcon(RE3Properties.getProperty("menu_remove_icon")));
        
        hideAction = new QAction(Translations.get("hide_text"), this);
        hideAction.triggered.connect(this, "hideFilters()");
        hideAction.setIcon(new QIcon(RE3Properties.getProperty("menu_hide_icon")));

        separator3 = new QAction("", this);
        separator3.setSeparator(true);
        
        expandAllAction = new QAction(Translations.get("expand_all_text"), this);
        expandAllAction.triggered.connect(this, "expandAll()");
        expandAllAction.setIcon(new QIcon(RE3Properties.getProperty("menu_expand_all_icon")));
        
        collapseAllAction = new QAction(Translations.get("collapse_all_text"), this);
        collapseAllAction.triggered.connect(this, "collapseAll()");
        collapseAllAction.setIcon(new QIcon(RE3Properties.getProperty("menu_collapse_all_icon")));
        
        separator4 = new QAction("", this);
        separator4.setSeparator(true);
        
        clearAllAction = new QAction(Translations.get("filter_menu_clear_selections"), this);
        clearAllAction.triggered.connect(this, "clearAllSelection()");
        clearAllAction.setIcon(new QIcon(RE3Properties.getProperty("menu_clear_selections_icon")));

        separator5 = new QAction("", this);
        separator5.setSeparator(true);
        
        hideEmptyAction = new QAction(Translations.get("filter_menu_hide_empty"), this);
        hideEmptyAction.triggered.connect(this, "hideEmptyToggle()");
        hideEmptyAction.setCheckable(true);
        
        sortByNameAction = new QAction(Translations.get("filter_menu_sort_by_name"), this);
        sortByNameAction.triggered.connect(this, "sortByName()");
        sortByNumArtistsAction = new QAction(Translations.get("filter_menu_sort_by_num_artists"), this);
        sortByNumArtistsAction.triggered.connect(this, "sortByNumArtists()");
        sortByNumLabelsAction = new QAction(Translations.get("filter_menu_sort_by_num_labels"), this);
        sortByNumLabelsAction.triggered.connect(this, "sortByNumLabels()");
        sortByNumReleasesAction = new QAction(Translations.get("filter_menu_sort_by_num_releases"), this);
        sortByNumReleasesAction.triggered.connect(this, "sortByNumReleases()");
        sortByNumSongsAction = new QAction(Translations.get("filter_menu_sort_by_num_songs"), this);
        sortByNumSongsAction.triggered.connect(this, "sortByNumSongs()");
        
        sortMenu = new QMenu(Translations.get("filter_menu_sort"), this);
        sortMenu.addAction(sortByNameAction);
        sortMenu.addAction(sortByNumArtistsAction);
        sortMenu.addAction(sortByNumLabelsAction);
        sortMenu.addAction(sortByNumReleasesAction);
        sortMenu.addAction(sortByNumSongsAction);
        sortMenuAction = new QAction(Translations.get("filter_menu_sort"), this);
        sortMenuAction.setMenu(sortMenu);
        sortMenuAction.setIcon(new QIcon(RE3Properties.getProperty("menu_sort_icon")));
        
    	loadAction = new QAction(Translations.get("playlist_load_text"), this);
    	loadAction.triggered.connect(this, "loadPlaylist()");
    	loadAction.setIcon(new QIcon(RE3Properties.getProperty("playlist_load_icon")));
        
        header().setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu);
        header().customContextMenuRequested.connect(this, "customContextMenuRequested(QPoint)");

    }		
        
    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////
    
    abstract public QAction getAddAction();
    abstract protected void sortByName();
	
    abstract protected boolean areSelectedInstancesMergable(Vector<FilterHierarchyInstance> selectedInstances);
    
	/////////////
	// GETTERS //
	/////////////
    
    /////////////
    // SETTERS //
    /////////////
    
    public void addFilterHierarchyChangeListener(FilterHierarchyChangeListener listener) {
    	if (hierarchyChangeListeners == null)
    		hierarchyChangeListeners = new Vector<FilterHierarchyChangeListener>();
    	if (!hierarchyChangeListeners.contains(listener))
    		hierarchyChangeListeners.add(listener);
    }
    
    public void removeFilterHierarchyChangeListener(FilterHierarchyChangeListener listener) {
    	if (hierarchyChangeListeners != null)
    		hierarchyChangeListeners.remove(listener);
    }
        
    /////////////
    // METHODS //
    /////////////
    
    public void initHideEmpty() {
    	hideEmptyAction.setChecked(((FilterProxyModel)modelManager.getProxyModel()).isHideEmptyFilters());
    }
    
    ////////////////////
    // SLOTS (EVENTS) //
    ////////////////////
    
    private void add() {
    	try {
        	Vector<FilterHierarchyInstance> selectedInstances = getCurrentSelectedInstances();
        	if (!RE3Properties.getBoolean("add_under_selected_filters"))
        		selectedInstances.clear();
        	if (log.isDebugEnabled())
        		log.debug("add(): selectdInstances=" + selectedInstances);
        	AddFilterDialog addFilterDialog = new AddFilterDialog(modelManager.getTypeDescription());
	    	if (addFilterDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	    		String newFilterName = addFilterDialog.getFilterName();
	    		FilterIdentifier newId = getFilterModelManager().getFilterIdentifier(newFilterName);
	    		FilterRecord record = (FilterRecord)Database.getRecord(newId);
	    		if (record != null) {
	    			if (log.isDebugEnabled())
	    				log.debug("add(): filter already exists with name=" + newFilterName);
	    			
	    			// let's quickly check and make sure this style is not corrupt (and that the user
	    			// is not adding because it's invisible...)
	    			HierarchicalRecord[] parents = record.getParentRecords();
	    			log.info("add(): # parents=" + parents.length);
	    			for (HierarchicalRecord parent : parents) {
	    				boolean found = false;
	    				for (HierarchicalRecord parentChild : parent.getChildRecords()) {
	    					if (parentChild.equals(record)) {
	    						found = true;
	    						break;
	    					}
	    				}
	    				if (!found)
	    					log.info("add(): not found in parent child list=" + parent);
	    			}
	    			HierarchicalRecord[] children = record.getChildRecords();
	    			log.info("add(): # children=" + children.length);

	    			/*
	    			if (Database.getProfile(newId))
    				FilterRecord filter = (FilterRecord)filterIndex.getRecord(id);
    				if ((filter != null) && !filter.isRoot()) {
    					if ((filter.getParentRecords().length == 0) && !filter.isDisabled()) {
    						FilterProfile testProfile = (FilterProfile)filterIndex.getProfile(id);
    						if (testProfile != null) {
    							log.warn("consistencyCheck(): valid filter with no parent found=" + filter);
    							filterIndex.addRelationship(filterIndex.getRootRecord(), filter);
    						}
    					} else {
    						boolean validParentFound = false;
    						for (HierarchicalRecord parentRecord : filter.getParentRecords()) {
    							if (parentRecord != null) {
    								if (!parentRecord.isDisabled()) {
    									// re-establish relationship in case parent lost the child reference (TBD: root cause)
    									filterIndex.addRelationship(parentRecord, filter);
    									validParentFound = true;
    								} else
    									filterIndex.removeRelationship(parentRecord, filter);
    							}
    						}
    						if (!validParentFound && !filter.isDisabled()) {
    							log.warn("consistencyCheck(): valid filter with no visible parent found=" + filter);
        						filterIndex.addRelationship(filterIndex.getRootRecord(), filter);        						
    						}    						
    					}
    					//if (filter.computeNumArtistRecords() == 0)
    						//filter.computeNumArtistRecords();
    					//if (filter.computeNumLabelRecords() == 0)	
    						//filter.computeNumLabelRecords();
    					//if (filter.computeNumReleaseRecords() == 0)
    						//filter.computeNumReleaseRecords();
    					//if (filter.computeNumSongRecords() == 0)
    						//filter.computeNumSongRecords();    					
    				}	
    				*/    			
	    			
	    			ProfileWidgetUI.instance.editProfile(Database.getProfile(newId));
	    		} else {
	    			SubmittedFilterProfile newFilter = getFilterModelManager().getNewSubmittedFilter(newFilterName);
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
    
    private void delete() {
		if (QMessageBox.question(this, Translations.get("dialog_delete_filter_title"), Translations.get("dialog_delete_filter_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}    	
    	Vector<FilterHierarchyInstance> deletedInstances = getCurrentSelectedInstances();
    	Vector<Record> removedRecords = new Vector<Record>();
    	for (TreeHierarchyInstance instance : deletedInstances) {
    		Vector<HierarchicalRecord> removed = getFilterModelManager().removeInstance(instance);
    		for (HierarchicalRecord record : removed)
    			removedRecords.add(record);    		
    	}   
    	TaskManager.runForegroundTask(new DeleteRecordsTask(removedRecords));    	
    }

    private void play() {
    	FilterSelection selectedFilters = getFilterSelection();
    	SongSearchParameters songParameters = new SongSearchParameters();
    	// TODO: should probably be made an abstract method
    	if (this instanceof StylesTreeView)
    		songParameters.setStylesSelection(selectedFilters);
    	else if (this instanceof TagsTreeView)
    		songParameters.setTagsSelection(selectedFilters);
    	else if (this instanceof PlaylistsTreeView)
    		songParameters.setPlaylistsSelection(selectedFilters);
    	Vector<SearchResult> matchedSongs = Database.getSongIndex().searchRecords(songParameters);
    	Vector<Integer> playedSongs = new Vector<Integer>();
    	for (SearchResult matchedSong : matchedSongs)
    		playedSongs.add(matchedSong.getRecord().getUniqueId());
    	PlayerManager.playSongs(playedSongs);
    }
    
    private void edit() {
    	FilterRecord editedFilter = getCurrentSelectedInstances().get(0).getFilterRecord();
    	FilterProfile editedProfile = (FilterProfile)getTreeModelManager().getIndex().getProfile(editedFilter.getUniqueId());
    	if (editedProfile != null) {
    		ProfileWidgetUI.instance.showProfile(editedProfile, false);
    	}
    }
    
    private void loadPlaylist() {
    	OrderedPlaylistRecord orderedPlaylist = (OrderedPlaylistRecord)getCurrentSelectedInstances().get(0).getFilterRecord();
    	Vector<Integer> songIds = orderedPlaylist.getSongIds();
    	Vector<Record> profileTrail = new Vector<Record>(songIds.size());
    	SongRecord firstSong = null;
    	for (int songId : songIds) {
    		SongRecord song = Database.getSongIndex().getSongRecord(songId);
    		if (song != null) {
    			profileTrail.add(song);
    			if (firstSong == null)
    				firstSong = song;
    		}
    	}
    	ProfileWidgetUI.instance.load(orderedPlaylist, profileTrail);
    }
    
    private void merge() {
    	Vector<FilterHierarchyInstance> selectedFilters = getCurrentSelectedInstances();    	
    	Vector<Record> selectedRecords = new Vector<Record>(selectedFilters.size());
    	for (FilterHierarchyInstance filterInstance : selectedFilters) {
    		if (!selectedRecords.contains(filterInstance.getRecord()))
    			selectedRecords.add(filterInstance.getRecord());
    	}
    	if (selectedRecords.size() > 0) {
	    	MergeRecordsDialog mergeRecordsDialog = new MergeRecordsDialog(this, selectedRecords, modelManager.getTypeDescription());
	    	if (mergeRecordsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	    		mergeRecordsDialog.mergeRecords();    	
	    	}    	
    	}    	
    }            
        
    public void updateActionMenu() {
    	Vector<FilterHierarchyInstance> selectedInstances = getCurrentSelectedInstances();
    	FilterSelection filterSelection = getFilterSelection();
        removeAction(playAction);
        removeAction(getAddAction());
        removeAction(deleteAction);
        removeAction(hideAction);
        removeAction(editAction);
        removeAction(mergeAction);
        removeAction(expandAllAction);
        removeAction(collapseAllAction);
        removeAction(clearAllAction);
        removeAction(hideEmptyAction);
        removeAction(separator1);
        removeAction(separator2);
        removeAction(separator3);
        removeAction(separator4);
        removeAction(separator5);
        removeAction(sortMenuAction);
        removeAction(loadAction);
    	if (filterSelection.size() == 0) {
            addAction(getAddAction());
            if (modelManager.getProxyModel().rowCount() > 0) {
            	addAction(separator3);
            	addAction(expandAllAction);
            	addAction(collapseAllAction);
            	addAction(separator5);
            	addAction(sortMenuAction);
            	addAction(hideEmptyAction);
            } else {
            	addAction(separator3);
            	addAction(hideEmptyAction);
            }
    	} else if (filterSelection.size() == 1) {
            addAction(playAction);     
            addAction(separator1);
            if (selectedInstances.size() > 0) {
            	addAction(editAction);    		
            	if ((selectedInstances.size() == 1) && (selectedInstances.get(0).getFilterRecord() instanceof OrderedPlaylistRecord))
            		addAction(loadAction);
            	addAction(separator2);
            }
            addAction(getAddAction());
            if (selectedInstances.size() > 0) {
            	addAction(hideAction);
            	addAction(deleteAction);
            }
            addAction(separator3);
            if (modelManager.getProxyModel().rowCount() > 0) {
            	addAction(expandAllAction);
            	addAction(collapseAllAction);
            	addAction(separator4);
            	//addAction(sortMenuAction);
            	addAction(hideEmptyAction);
            	addAction(separator5);
            }
            addAction(clearAllAction);            
    	} else if (filterSelection.size() > 1) {
            addAction(playAction);     
            addAction(separator1);
            if (selectedInstances.size() == 1) {
            	addAction(editAction);    		
            	addAction(separator2);
            }
            if (selectedInstances.size() > 1) {
            	if (areSelectedInstancesMergable(selectedInstances))
            		addAction(mergeAction);
            	addAction(separator2);
            }
            addAction(getAddAction()); 
            if (selectedInstances.size() > 0) {
            	addAction(hideAction);
            	addAction(deleteAction);
            }
            addAction(separator3);
            if (modelManager.getProxyModel().rowCount() > 0) {
            	addAction(expandAllAction);
            	addAction(collapseAllAction);            	
            	addAction(separator4);
            	//addAction(sortMenuAction);
            	addAction(hideEmptyAction);
            	addAction(separator5);
            }
            addAction(clearAllAction);                        
    	}    	
    }
    
    protected void executeSelectionChangeActions() {
    	if (SearchWidgetUI.instance != null)
    		SearchWidgetUI.instance.updateFilter();    	
    }
        
    protected void hideEmptyToggle() {
    	((FilterProxyModel)modelManager.getProxyModel()).setHideEmptyFilters(hideEmptyAction.isChecked());
    	((FilterProxyModel)modelManager.getProxyModel()).applyFilter();
    }
    
    protected void hideFilters() {
		if (QMessageBox.question(this, Translations.get("dialog_hide_filter_title"), Translations.get("dialog_hide_filter_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}    	
    	Vector<FilterHierarchyInstance> hiddenInstances = getCurrentSelectedInstances();
    	for (TreeHierarchyInstance instance : hiddenInstances) {
    		Vector<HierarchicalRecord> hidden = getFilterModelManager().removeInstance(instance);
    		for (HierarchicalRecord record : hidden) {
    			record.setDisabled(true);
    			record.update();
    		}
    	}
    }
    
	public void dropEvent(QDropEvent event) {				
		if (log.isTraceEnabled())
			log.trace("dropEvent(): event=" + event);
		QModelIndex dropIndex = indexAt(event.pos());
		QSortFilterProxyModel proxyModel = (QSortFilterProxyModel)model();
		QStandardItemModel sourceModel = (QStandardItemModel)proxyModel.sourceModel();
		QModelIndex sourceIndex = proxyModel.mapToSource(dropIndex);		
		QStandardItem dropAtItem = sourceModel.itemFromIndex(sourceIndex);		
		TreeHierarchyInstance dropAtInstance = null;
		if (dropAtItem != null)
			dropAtInstance = (TreeHierarchyInstance)dropAtItem.data();
		boolean copyEvent = event.keyboardModifiers().isSet(KeyboardModifier.ControlModifier);
		if (DragDropUtil.containsSearchRecords(event.mimeData())) {
			if (dropAtInstance != null) {
				FilterRecord filter = (FilterRecord)dropAtInstance.getRecord();
				String title = Translations.get("dialog_add_records_to_filter_title");
				title = StringUtil.replace(title, Translations.getPreferredCase("%filterType%"), modelManager.getTypeDescription());
				String text = Translations.get("dialog_add_records_to_filter_text");
				text = StringUtil.replace(text, Translations.getPreferredCase("%filterName%"), filter.toString());
				if (QMessageBox.question(this, title, text, QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
					return;
				}							
				Vector<ArtistRecord> artists = DragDropUtil.getArtists(event.mimeData());				
				if (artists.size() > 0) {
					if (log.isDebugEnabled())
						log.debug("dropEvent(): dropAtInstance=" + dropAtInstance + ", artists=" + artists);
					boolean containsInternal = false;
					boolean containsExternal = false;
					for (ArtistRecord artist : artists) {
						if (artist.isExternalItem())
							containsExternal = true;
						else
							containsInternal = true;
					}							
					filter.addArtistRecords(artists);
					if (containsInternal)
						filter.computeNumArtistRecords();
					if (containsExternal)
						filter.computeNumExternalArtistRecords();						
				}
				Vector<LabelRecord> labels = DragDropUtil.getLabels(event.mimeData());				
				if (labels.size() > 0) {
					if (log.isDebugEnabled())
						log.debug("dropEvent(): dropAtInstance=" + dropAtInstance + ", labels=" + labels);
					boolean containsInternal = false;
					boolean containsExternal = false;
					for (LabelRecord label : labels) {
						if (label.isExternalItem())
							containsExternal = true;
						else
							containsInternal = true;
					}							
					filter.addLabelRecords(labels);
					if (containsInternal)
						filter.computeNumLabelRecords();
					if (containsExternal)
						filter.computeNumExternalLabelRecords();		
				}
				Vector<ReleaseRecord> releases = DragDropUtil.getReleases(event.mimeData());				
				if (releases.size() > 0) {
					if (log.isDebugEnabled())
						log.debug("dropEvent(): dropAtInstance=" + dropAtInstance + ", releases=" + releases);
					boolean containsInternal = false;
					boolean containsExternal = false;
					for (ReleaseRecord release : releases) {
						if (release.isExternalItem())
							containsExternal = true;
						else
							containsInternal = true;
					}							
					filter.addReleaseRecords(releases);
					if (containsInternal)
						filter.computeNumReleaseRecords();
					if (containsExternal)
						filter.computeNumExternalReleaseRecords();	
				}
				Vector<SongRecord> songs = DragDropUtil.getSongs(event.mimeData());				
				if (songs.size() > 0) {
					if (log.isDebugEnabled())
						log.debug("dropEvent(): dropAtInstance=" + dropAtInstance + ", songs=" + songs);
					boolean containsInternal = false;
					boolean containsExternal = false;
					for (SongRecord song : songs) {
						if (song.isExternalItem())
							containsExternal = true;
						else
							containsInternal = true;
					}					
					filter.addSongRecords(songs);
					if (containsInternal)
						filter.computeNumSongRecords();
					if (containsExternal)
						filter.computeNumExternalSongRecords();					
				}				
				filter.update();
			}
		} else if (DragDropUtil.containsFilterRecords(event.mimeData())) {
			Vector<TreeHierarchyInstance> dragInstances = DragDropUtil.getTreeInstances(event.mimeData());
			if ((dropAtInstance != null) && (dropAtInstance.equals(dragInstances.get(0)))) {
				if (log.isDebugEnabled())
					log.debug("dropEvent(): aborting drop (on self)");
				return;
			}
			String dropDescription = "ROOT";
			if (dropAtInstance != null)
				dropDescription = dropAtInstance.getName();
			String messagePrefix = copyEvent ? Translations.get("dialog_update_filter_hierarchy_copy_prefix") : Translations.get("dialog_update_filter_hierarchy_move_prefix");
			messagePrefix = StringUtil.replace(messagePrefix, Translations.getPreferredCase("%modelType%"), modelManager.getTypeDescription());
			String messageSuffix = Translations.get("dialog_update_filter_hierarchy_suffix");
			if (QMessageBox.question(this, Translations.get("dialog_update_filter_hierarchy_title"), messagePrefix + dropDescription + messageSuffix, QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
				return;
			}			
			for (TreeHierarchyInstance dragInstance : dragInstances) {
				if (log.isTraceEnabled())
					log.trace("dropEvent(): dragInstance=" + dragInstance + ", dropAtInstance=" + dropAtInstance + ", copy=" + copyEvent);
				disableSelectionChangedListener();
				getFilterModelManager().updateHierarchy(dragInstance, dropAtInstance, copyEvent);
				addMissingSelections();				
				if (log.isTraceEnabled())
					log.trace("dropEvent(): notifying listeners...");
				if (hierarchyChangeListeners != null) {
					for (FilterHierarchyChangeListener changeListener : hierarchyChangeListeners) {
						changeListener.updateHierarchy(dragInstance, dropAtInstance, copyEvent);
					}
				}				
				if (log.isTraceEnabled())
					log.trace("dropEvent(): done notifying listeners");				
			}	
			if (dropAtItem != null) {
				// the next line is to prevent against expanding the wrong row if position has changed due to sort properties...
				dropIndex = getTreeModelManager().getProxyModel().mapFromSource(getTreeModelManager().getIndexOfInstance(dropAtInstance));
				expand(dropIndex);
			}
		}
		
	}
	
	protected void mouseDoubleClickEvent(QMouseEvent mouseEvent) {
		super.mouseDoubleClickEvent(mouseEvent);
		
    	QModelIndex proxyIndex = indexAt(mouseEvent.pos());
    	QModelIndex sourceIndex = getProxyModel().mapToSource(proxyIndex);    
    	QStandardItem thisItem = ((QStandardItemModel)getTreeModelManager().getSourceModel()).itemFromIndex(sourceIndex);
    	if (thisItem != null) {
    		TreeHierarchyInstance filterInstance = (TreeHierarchyInstance)thisItem.data();
	    	HierarchicalRecord record = filterInstance.getRecord();
	    	FilterProfile profile = (FilterProfile)Database.getProfile(record.getIdentifier());
	    	if (profile != null) {
	    		ProfileWidgetUI.instance.editProfile(profile);
	    	}
    	}
	}
		
	protected void sortByNumArtists() { sortByColumn(COLUMN_NUM_ARTISTS.getColumnId()); }
	protected void sortByNumLabels() { sortByColumn(COLUMN_NUM_LABELS.getColumnId()); }
	protected void sortByNumReleases() { sortByColumn(COLUMN_NUM_RELEASES.getColumnId()); }
	protected void sortByNumSongs() { sortByColumn(COLUMN_NUM_SONGS.getColumnId()); }
		
	protected void highlightFiltersMatchingSelection(FilterRecord filter, QStyleOptionViewItem options) {		
		boolean matchesSelection = false;
		if ((filter instanceof StyleRecord) || (filter instanceof TagRecord)) {
			Vector<SearchRecord> selectedRecords = SearchWidgetUI.instance.getCurrentSearchView().getSelectedRecords();
			for (SearchRecord searchRecord : selectedRecords) {
				if ((filter instanceof StyleRecord) && (searchRecord.containsActualStyle(filter.getUniqueId()))) {
					matchesSelection = true;
					break;
				} else if ((filter instanceof TagRecord) && (searchRecord.containsActualTag(filter.getUniqueId()))) {
					matchesSelection = true;
					break;							
				}
			}					
		}
		if (matchesSelection) {
			QStyle.State state = options.state();
			state.set(QStyle.StateFlag.State_Selected);
			options.setState(state);					
			QPalette p = options.palette();
			p.setColor(QPalette.ColorRole.Highlight, FILTER_MATCH_COLOR.getQColor());
			options.setPalette(p);
		}				
	}

	////////////
	// EVENTS //
	////////////
	
	public void customContextMenuRequested(QPoint point) {
		if (log.isDebugEnabled())
			log.debug("customContextMenuRequested(): point=" + point);
    	SelectColumnsDialog selectColumnsDialog = new SelectColumnsDialog(this, modelManager);
    	if (selectColumnsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		selectColumnsDialog.saveSelections();
    		modelManager.setSourceColumnSizes(this);
    	}    	
	}
	
}
