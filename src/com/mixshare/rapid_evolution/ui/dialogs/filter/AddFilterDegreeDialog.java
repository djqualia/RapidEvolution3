package com.mixshare.rapid_evolution.ui.dialogs.filter;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.DynamicPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedDynamicPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.style.SubmittedStyle;
import com.mixshare.rapid_evolution.data.submitted.filter.tag.SubmittedTag;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.style.AddTabStyleTask;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.style.StyleTabTreeView;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.tag.AddTabTagTask;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.tag.TagTabTreeView;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QCompleter;
import com.trolltech.qt.gui.QDialog;

public class AddFilterDegreeDialog extends QDialog implements DataConstants {

	static private Logger log = Logger.getLogger(AddFilterDegreeDialog.class);	
	
	static private AddFilterDegreeDialog instance = null;
	
	static public boolean isOpen() { return instance != null; }	
	static public void focusInstance() { if (instance != null) instance.setFocus(); }	
	
	////////////
	// FIELDS //
	////////////
	
    private Ui_AddFilterDegreeDialog ui = new Ui_AddFilterDegreeDialog();
    private String typeDescription;
    private QCompleter completer;
    private FilterModelManager filterModelManager;
    private Vector<Integer> profileIds = new Vector<Integer>();
    private Vector<Index> profileIndexes = new Vector<Index>();
    private StyleTabTreeView stylesTreeView;
    private TagTabTreeView tagsTreeView;

    //////////////////
    // CONSTRUCTORS //    
    //////////////////
    
    public AddFilterDegreeDialog(QCompleter completer, FilterModelManager filterModelManager, SearchProfile profile) {
    	super(RapidEvolution3UI.instance);
    	this.typeDescription = filterModelManager.getTypeDescription();
    	this.completer = completer;
    	this.filterModelManager = filterModelManager;
    	profileIds.add(profile.getUniqueId());
    	profileIndexes.add(profile.getSearchRecord().getIndex());
        init();
    }
    public AddFilterDegreeDialog(QCompleter completer, FilterModelManager filterModelManager, Vector<SearchRecord> searchRecords, boolean hideSlider) {
    	super(RapidEvolution3UI.instance);
    	this.typeDescription = filterModelManager.getTypeDescription();
    	this.completer = completer;
    	this.filterModelManager = filterModelManager;
    	for (SearchRecord searchRecord : searchRecords) {
    		profileIds.add(searchRecord.getUniqueId());
    		profileIndexes.add(searchRecord.getIndex());
    	}
        init(hideSlider);
    }
    public AddFilterDegreeDialog(QCompleter completer, FilterModelManager filterModelManager, SearchProfile profile, StyleTabTreeView stylesTreeView) {
    	super(RapidEvolution3UI.instance);
    	this.typeDescription = filterModelManager.getTypeDescription();
    	this.completer = completer;
    	this.filterModelManager = filterModelManager;
    	profileIds.add(profile.getUniqueId());
    	profileIndexes.add(profile.getSearchRecord().getIndex());
    	this.stylesTreeView = stylesTreeView;
        init();
    }
    public AddFilterDegreeDialog(QCompleter completer, FilterModelManager filterModelManager, SearchProfile profile, TagTabTreeView tagsTreeView) {
    	super(RapidEvolution3UI.instance);
    	this.typeDescription = filterModelManager.getTypeDescription();
    	this.completer = completer;
    	this.filterModelManager = filterModelManager;
    	profileIds.add(profile.getUniqueId());
    	profileIndexes.add(profile.getSearchRecord().getIndex());
    	this.tagsTreeView = tagsTreeView;
        init();
    }

    private void init() {
    	init(false);
    }
    
    private void init(boolean hideSlider) {
    	instance = this;
    	ui.setupUi(this);
    	setModal(false);
    	ui.label.setText(typeDescription + " " + Translations.get("add_filter_text_suffix"));
    	ui.label_2.setText(Translations.get("degree_input_text"));
    	if (hideSlider) {
    		ui.horizontalSlider.setVisible(false);
    		ui.label_2.setVisible(false);
    	} else {
    		ui.horizontalSlider.setVisible(true);
    		ui.label_2.setVisible(true);
	    	ui.horizontalSlider.setRange(0, 100);
	    	ui.horizontalSlider.setSingleStep(0);
	    	ui.horizontalSlider.setValue(100);
    	}
    	setWindowTitle(Translations.get("add_filter_window_title_prefix") + " " + typeDescription);
    	setFixedSize(size()); // disallows resizing (couldn't figure out how to do that from the GUI editor)
    	ui.filterName.setFocus();    	
    	if (completer != null)
    		ui.filterName.setCompleter(completer);   
    	ui.moreButton.clicked.connect(this, "addMore()");    	
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public String getFilterName() {
    	return ui.filterName.text();
    }
    
    public float getDegree() {
    	return ((float)ui.horizontalSlider.value()) / 100.0f;
    }
    
    /////////////
    // SETTERS //
    /////////////
    
    public void setTitle(String title) { setWindowTitle(title); }
    
    public void setLabel(String label) { ui.label.setText(label); }
    
    ////////////
    // EVENTS //
    ////////////
    
    
    public void accept() {
    	addMore();
    	this.close();
    }
    
    public void addMore() {    	
    	try {
    		if (getFilterName().length() > 0) {
				FilterIdentifier filterId = (FilterIdentifier)filterModelManager.getFilterIdentifier(getFilterName());
				if ((stylesTreeView != null) && (filterId instanceof StyleIdentifier)) {
					TaskManager.runForegroundTask(new AddTabStyleTask((StyleIdentifier)filterId, getDegree(), stylesTreeView));
				} else if ((tagsTreeView != null) && (filterId instanceof TagIdentifier)) {
					TaskManager.runForegroundTask(new AddTabTagTask((TagIdentifier)filterId, getDegree(), tagsTreeView));
				} else {
					FilterRecord filter = (FilterRecord)Database.getRecord(filterId);
					if (filter == null) {	
						FilterProfile filterProfile = null;
						if (filterId instanceof StyleIdentifier)
							filterProfile = (FilterProfile)Database.add(new SubmittedStyle(getFilterName()));
						else if (filterId instanceof TagIdentifier)
							filterProfile = (FilterProfile)Database.add(new SubmittedTag(getFilterName()));
						else if (filterId instanceof PlaylistIdentifier)
							filterProfile = (FilterProfile)Database.add(new SubmittedDynamicPlaylist(getFilterName()));
						if (filterProfile != null)
							filter = filterProfile.getFilterRecord();
					}
					if (filter != null) {
						boolean addedArtists = false;
						boolean addedLabels = false;
						boolean addedReleases = false;
						boolean addedSongs = false;
						boolean addedExternalArtists = false;
						boolean addedExternalLabels = false;
						boolean addedExternalReleases = false;
						boolean addedExternalSongs = false;
						for (int i = 0; i < profileIds.size(); ++i)  {
							int profileId = profileIds.get(i);
							SearchProfile profile = (SearchProfile)profileIndexes.get(i).getProfile(profileId);
							if (profile != null) {
								if (filter instanceof StyleRecord)
									profile.addStyle(new DegreeValue(getFilterName(), getDegree(), DATA_SOURCE_USER));
								else if (filter instanceof TagRecord)
									profile.addTag(new DegreeValue(getFilterName(), getDegree(), DATA_SOURCE_USER));
								else if (filter instanceof DynamicPlaylistRecord) {
									if (profile instanceof ArtistProfile) {
										((DynamicPlaylistRecord)filter).addArtist(profile.getUniqueId());
										if (profile.isExternalItem())											
											addedExternalArtists = true;
										else
											addedArtists = true;
									}
									else if (profile instanceof LabelProfile) {
										((DynamicPlaylistRecord)filter).addLabel(profile.getUniqueId());
										if (profile.isExternalItem())
											addedExternalLabels = true;
										else
											addedLabels = true;
									}
									else if (profile instanceof ReleaseProfile) {
										((DynamicPlaylistRecord)filter).addRelease(profile.getUniqueId());
										if (profile.isExternalItem())
											addedExternalReleases = true;
										else
											addedReleases = true;
									}
									else if (profile instanceof SongProfile) {
										((DynamicPlaylistRecord)filter).addSong(profile.getUniqueId());
										if (profile.isExternalItem())
											addedExternalSongs = true;
										else
											addedSongs = true;
									}
								}
								ProfileSaveTask.save(profile);								
							}							
						}
						if (addedArtists)
							filter.computeNumArtistRecords();
						if (addedLabels)
							filter.computeNumLabelRecords();
						if (addedReleases)
							filter.computeNumReleaseRecords();
						if (addedSongs)
							filter.computeNumSongRecords();
						if (addedExternalArtists)
							filter.computeNumExternalArtistRecords();
						if (addedExternalLabels)
							filter.computeNumExternalLabelRecords();
						if (addedExternalReleases)
							filter.computeNumExternalReleaseRecords();
						if (addedExternalSongs)
							filter.computeNumExternalSongRecords();
						filter.update();
		    		}
				}
				ui.filterName.setText("");
    		}				
    	} catch (Exception e) {
    		log.error("addMore(): error", e);
    	}
    }
        
    protected void closeEvent(QCloseEvent closeEvent) {
    	super.closeEvent(closeEvent);
    	instance = null;
    }
        
}
