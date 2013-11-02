package com.mixshare.rapid_evolution.ui.widgets.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.index.CommonIndex;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.index.event.ProfilesMergedListener;
import com.mixshare.rapid_evolution.data.mined.echonest.artist.EchonestArtistProfile;
import com.mixshare.rapid_evolution.data.mined.idiomag.artist.IdiomagArtistProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.artist.LastfmArtistProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.DynamicPlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.OrderedPlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.OrderedPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.record.search.SongGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedOrderedPlaylist;
import com.mixshare.rapid_evolution.player.PlayerManager;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.dialogs.filter.AddFilterDialog;
import com.mixshare.rapid_evolution.ui.dialogs.trail.TrailDialog;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.updaters.view.profile.ProfileDelegateRefresh;
import com.mixshare.rapid_evolution.ui.updaters.view.profile.ProfileDelegateRefreshDelay;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.CentralWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.common.LazyLoadWidget;
import com.mixshare.rapid_evolution.ui.widgets.common.splitter.RESplitter;
import com.mixshare.rapid_evolution.ui.widgets.filter.playlists.PlaylistsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.event.ProfileWidgetChangeListener;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.playlist.DynamicPlaylistProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.playlist.OrderedPlaylistProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.style.StyleProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.tag.TagProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.artist.ArtistProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.label.LabelProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.release.ReleaseProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.song.SongProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.song.stage.StageChangeListener;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.song.stage.StageWidget;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.mining.TriggeredMinerStarter;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class ProfileWidgetUI extends QWidget implements DataConstants, IndexChangeListener, ProfilesMergedListener, StageChangeListener {

	static private Logger log = Logger.getLogger(ProfileWidgetUI.class);

	static public ProfileWidgetUI instance;

	static private int MINIMUM_SIZE = 50;

    ////////////
    // FIELDS //
    ////////////

	// ui stuff
	private final QWidget detailsWidget;
	private final QVBoxLayout detailsLayout;
	private final QTabWidget itemDetailTabsWidget;
    private final QPushButton backButton;
    private final QPushButton playButton;
    private final QPushButton nextButton;
	private final RESplitter detailsSplitter;
	private final QHBoxLayout profileTitleBarLayout;
	private final RESplitter stageSplitter;

	private boolean updateImmediately = false;

	private ProfileDelegate profileDelegate;
	private Vector<Record> profileTrailRecords = new Vector<Record>();
	private int profileTrailIndex = -1;

	private final QAction saveAsPlaylistAction;
	private final QAction clearTrailAction;

	private final QPushButton trailButton;
	private final QPushButton stageButton;
	private final StageWidget stageWidget;

	private final Vector<ProfileWidgetChangeListener> changeListeners = new Vector<ProfileWidgetChangeListener>();

	transient private Profile currentProfile = null;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public ProfileWidgetUI() {
    	instance = this;

    	QSizePolicy searchWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
    	searchWidgetSizePolicy.setVerticalStretch((byte)1);
    	setSizePolicy(searchWidgetSizePolicy);

    	QVBoxLayout searchLayout = new QVBoxLayout(this);
    	searchLayout.setMargin(0);

    	detailsSplitter = new RESplitter("profile_details_splitter", false); // separates item detail summary with item detail tabs (horizontal)
    	stageSplitter = new RESplitter("stage_details_splitter", false);
    	stageSplitter.splitterMoved.connect(this, "stageSplitterMoved(Integer, Integer)");

        // search details pane
        detailsSplitter.setMinimumSize(MINIMUM_SIZE, MINIMUM_SIZE);
    	QSizePolicy searchDetailsSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
    	searchDetailsSizePolicy.setVerticalStretch((byte)0);
    	detailsSplitter.setSizePolicy(searchDetailsSizePolicy);


    	detailsWidget = new QWidget();
    	detailsLayout  = new QVBoxLayout(detailsWidget);
    	detailsLayout.setMargin(0);
    	detailsLayout.setSpacing(0);
    	detailsWidget.setMinimumSize(MINIMUM_SIZE, MINIMUM_SIZE);
    	QSizePolicy itemDetailsSummarySizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
    	itemDetailsSummarySizePolicy.setHorizontalStretch((byte)0);
    	detailsWidget.setSizePolicy(itemDetailsSummarySizePolicy);

        itemDetailTabsWidget = new QTabWidget();
    	QSizePolicy itemDetailTabsSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
    	itemDetailTabsSizePolicy.setHorizontalStretch((byte)1);
    	itemDetailTabsWidget.setSizePolicy(itemDetailTabsSizePolicy);

    	stageWidget = new StageWidget();
    	stageWidget.addChangeListener(this);

    	stageSplitter.addWidget(itemDetailTabsWidget);
    	stageSplitter.addWidget(stageWidget);

        detailsSplitter.addWidget(detailsWidget);
        detailsSplitter.addWidget(stageSplitter);

        ArrayList<Integer> detailsSplitterSizes = new ArrayList<Integer>();
        detailsSplitterSizes.add(175);
        detailsSplitterSizes.add(475);
        detailsSplitter.setSizes(detailsSplitterSizes);

        profileTitleBarLayout = new QHBoxLayout();
        profileTitleBarLayout.setMargin(0);

        QPushButton closeButton = new QPushButton();
        closeButton.setIcon(new QIcon(RE3Properties.getProperty("cancel_icon")));
        closeButton.setMaximumWidth(25);
        closeButton.setMaximumHeight(25);
        closeButton.clicked.connect(this, "closeDetails()");

        stageButton = new QPushButton();
        stageButton.setIcon(new QIcon(RE3Properties.getProperty("stage_expand_icon")));
        stageButton.setMaximumHeight(25);
        stageButton.setText(Translations.get("stage_button_text"));
        stageButton.clicked.connect(this, "stageButtonClicked()");
    	QSizePolicy stageButtonSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
    	stageButton.setSizePolicy(stageButtonSizePolicy);

    	trailButton = new QPushButton();
    	trailButton.setIcon(new QIcon(RE3Properties.getProperty("trail_icon")));
    	trailButton.setMaximumHeight(25);
    	trailButton.setText(Translations.get("trail_button_text"));
    	trailButton.clicked.connect(this, "trailButtonClicked()");
    	trailButton.setSizePolicy(stageButtonSizePolicy);

    	profileTitleBarLayout.addWidget(trailButton);
        profileTitleBarLayout.addWidget(stageButton);
        profileTitleBarLayout.addWidget(closeButton);

        searchLayout.addLayout(profileTitleBarLayout);
        searchLayout.addWidget(detailsSplitter);
        searchLayout.setStretch(0, 0);
        searchLayout.setStretch(1, 1);

    	QWidget buttonGroupWidget = new QWidget();
    	QHBoxLayout buttonLayout  = new QHBoxLayout(buttonGroupWidget);
    	buttonLayout.setMargin(0);
    	buttonGroupWidget.setMinimumSize(MINIMUM_SIZE, MINIMUM_SIZE);
    	QSizePolicy buttonGroupSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
    	itemDetailsSummarySizePolicy.setHorizontalStretch((byte)0);
    	buttonGroupWidget.setSizePolicy(buttonGroupSizePolicy);

    	backButton = new QPushButton();
        QIcon backIcon = new QIcon(RE3Properties.getProperty("profile_back_button"));
        backButton.setIcon(backIcon);
    	backButton.setToolTip(Translations.get("profile_last_tooltip"));
    	backButton.setVisible(false);
    	backButton.setEnabled(false);
    	backButton.clicked.connect(this, "back(Boolean)");

    	playButton = new QPushButton();
        QIcon playIcon = new QIcon(RE3Properties.getProperty("profile_play_button"));
        playButton.setIcon(playIcon);
    	playButton.setToolTip(Translations.get("play_text"));
    	playButton.setVisible(false);
    	playButton.clicked.connect(this, "play(Boolean)");

    	nextButton = new QPushButton();
        QIcon nextIcon = new QIcon(RE3Properties.getProperty("profile_next_button"));
        nextButton.setIcon(nextIcon);
    	nextButton.setToolTip(Translations.get("profile_next_tooltip"));
    	nextButton.setVisible(false);
    	nextButton.setEnabled(false);
    	nextButton.clicked.connect(this, "next(Boolean)");

    	buttonLayout.addWidget(backButton);
    	buttonLayout.addWidget(playButton);
    	buttonLayout.addWidget(nextButton);

    	backButton.setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);
    	playButton.setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);
    	nextButton.setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);

    	saveAsPlaylistAction = new QAction(Translations.get("save_trail_as_playlist_text"), this);
    	saveAsPlaylistAction.triggered.connect(this, "saveAsPlaylist()");
    	saveAsPlaylistAction.setIcon(new QIcon(RE3Properties.getProperty("menu_save_as_playlist_icon")));

    	clearTrailAction = new QAction(Translations.get("clear_trail_text"), this);
    	clearTrailAction.triggered.connect(this, "clearTrail()");
    	clearTrailAction.setIcon(new QIcon(RE3Properties.getProperty("menu_clear_icon")));

    	backButton.addAction(saveAsPlaylistAction);
    	playButton.addAction(saveAsPlaylistAction);
    	nextButton.addAction(saveAsPlaylistAction);

    	backButton.addAction(clearTrailAction);
    	playButton.addAction(clearTrailAction);
    	nextButton.addAction(clearTrailAction);

    	detailsLayout.addWidget(buttonGroupWidget);

    	if (!UIProperties.hasProperty("stage_details_splitter_size_1")) {
    		UIProperties.setProperty("stage_details_splitter_size_1", String.valueOf(stageSplitter.width() - 75));
    		UIProperties.setProperty("stage_details_splitter_size_2", String.valueOf(75));
    	}
        ArrayList<Integer> splitterSizes = new ArrayList<Integer>();
        splitterSizes.add(stageSplitter.width() - 75);
        splitterSizes.add(75);
        stageSplitter.setSizes(splitterSizes);

    	for (CommonIndex index : Database.getAllIndexes())
    		index.addIndexChangeListener(this);
    	for (CommonIndex index : Database.getAllIndexes())
    		index.addProfilesMergedListener(this);
    }

    /////////////
    // GETTERS //
    /////////////

    public Record getCurrentRecord() {
    	if ((profileTrailIndex >= 0) && (profileTrailIndex < profileTrailRecords.size()))
    		return profileTrailRecords.get(profileTrailIndex);
    	return null;
    }

    public Profile getCurrentProfile() {
    	if (currentProfile == null) {
    		if ((profileTrailIndex >= 0) && (profileTrailIndex < profileTrailRecords.size()))
    			currentProfile = Database.getProfile(profileTrailRecords.get(profileTrailIndex).getIdentifier());
    	}
    	return currentProfile;
    }

    public ProfileDelegate getProfileDelegate() {
    	return profileDelegate;
    }

    public int getProfileIndex() { return profileTrailIndex; }
    public Vector<Record> getProfileTrail() { return profileTrailRecords; }
    public Vector<Record> getProfileTrailCopy() {
    	Vector<Record> copy = new Vector<Record>();
    	for (Record record : profileTrailRecords)
    		copy.add(record);
    	return copy;
    }
    public Vector<Record> getProfileTrailToCurrent() {
    	Vector<Record> result = new Vector<Record>((profileTrailIndex > 0) ? profileTrailIndex : 0);
    	if (profileTrailRecords != null)
    		for (int i = 0; i <= Math.min(profileTrailIndex, profileTrailRecords.size()); ++i)
    			result.add(profileTrailRecords.get(i));
    	return result;
    }
    public Vector<SongRecord> getSongTrail() {
    	Vector<SongRecord> result = new Vector<SongRecord>(profileTrailRecords.size());
    	for (Record record : profileTrailRecords)
    		if (record instanceof SongRecord)
    			result.add((SongRecord)record);
    	return result;
    }

    public StageWidget getStageWidget() { return stageWidget; }

    public void addCurrentProfileChangedListener(ProfileWidgetChangeListener listener) {
    	this.changeListeners.add(listener);
    }

    /////////////
    // SETTERS //
    /////////////

    public void setUpdateImmediately(boolean updateImmediately) {
    	this.updateImmediately = updateImmediately;
    }

    public void setProfileTrail(Vector<Record> profileTrail) {
    	setProfileTrail(profileTrail, getCurrentRecord());
    }
    public void setProfileTrail(Vector<Record> profileTrail, Record currentRecord) {
    	this.profileTrailRecords = profileTrail;
    	int i = 0;
    	for (Record record : profileTrail) {
    		if (record.equals(currentRecord)) {
    			profileTrailIndex = i;
    			currentProfile = null;
    			break;
    		}
    		++i;
    	}
    	setBackNextButtonStates();
    	fireTrailChanged();
    }

    /////////////
    // METHODS //
    /////////////

    public void setSplitterSizes() {
    	detailsSplitter.restorePosition();

    	if (UIProperties.getBoolean("show_stage")) {
	    	stageSplitter.restorePosition();
    	} else {
    		List<Integer> sizes = stageSplitter.sizes();
            ArrayList<Integer> mainSplitterSizes = new ArrayList<Integer>();
            mainSplitterSizes.add(stageSplitter.width());
            mainSplitterSizes.add(0);
            stageSplitter.setSizes(mainSplitterSizes);
    	}
        setStageButtonState(isStageVisible());
    }

    public void clearProfile() {
    	if (profileDelegate != null) {
			profileDelegate.getTitleWidget().setVisible(false);
			profileDelegate.getImageViewerWidget().setVisible(false);
			detailsLayout.removeWidget(profileDelegate.getImageViewerWidget());
			//detailsLayout.removeWidget(profileDelegate.getTitleWidget());
			profileTitleBarLayout.removeWidget(profileDelegate.getTitleWidget());
			itemDetailTabsWidget.currentChanged.disconnect(this, "currentChanged(Integer)");
			// the code below was needed before the "clear()" method because the clear was triggering the lazy loaded widgets to load...
			int tabIndex = 0;
			QWidget tab = itemDetailTabsWidget.widget(tabIndex);
			while (tab != null) {
				if (tab instanceof LazyLoadWidget)
					((LazyLoadWidget)tab).setIsUnloading(true);
				++tabIndex;
				tab = itemDetailTabsWidget.widget(tabIndex);
			}
			itemDetailTabsWidget.clear();
			profileDelegate.unload();
			profileDelegate = null;
    	}
    }

    ////////////////////
    // SLOTS (EVENTS) //
    ////////////////////

    public void currentChanged(Integer index) {
    	String tabTitle = itemDetailTabsWidget.tabText(index);
    	int lastIndex = tabTitle.lastIndexOf(" ("); // strip away counts for tabs like mixouts
    	if (lastIndex >= 0)
    		tabTitle = tabTitle.substring(0, lastIndex);
    	if (log.isDebugEnabled())
    		log.debug("currentChanged(): tabTitle=" + tabTitle);
    	profileDelegate.setTabIndexTitle(tabTitle);
    }

    public void editProfile(Profile profile) {
    	showProfile(profile);
    	if (profileDelegate != null) {
	    	for (int i = 0; i < profileDelegate.getTabsCached().size(); ++i) {
	    		if (itemDetailTabsWidget.tabText(i).equalsIgnoreCase("Details")) {
	    			itemDetailTabsWidget.setCurrentIndex(i);
	    			break;
	    		}
	    	}
    	}
    }

    public void setProfileIndex(int index) {
    	profileTrailIndex = index;
    	currentProfile = null;
    	showProfile(getCurrentProfile(), false);
    }

    public void showProfile(Profile profile) { showProfile(profile, true); }
    public void showProfile(Profile profile, boolean updateTrail) {
    	try {
    		if (log.isDebugEnabled())
    			log.debug("showProfile(): profile=" + profile + ", updateTrail=" + updateTrail);
    		if (profileDelegate != null) {
    			clearProfile();
    		}
    		if (profile != null) {
    			if (profile instanceof ArtistProfile) {
    				profileDelegate = new ArtistProfileDelegate((ArtistProfile)profile, itemDetailTabsWidget);
    				ArtistProfile artist = (ArtistProfile)profile;
    				if (RE3Properties.getBoolean("enable_triggered_data_miners"))
    					TaskManager.runForegroundTask(new TriggeredMinerStarter(artist));
	    			ArtistProfile.loadProperties();

	    			// debug
	    			if (log.isDebugEnabled()) {
		    			LastfmArtistProfile lastfmProfile = (LastfmArtistProfile)artist.getMinedProfile(DATA_SOURCE_LASTFM);
		    			if (lastfmProfile != null) {
		    				if (log.isDebugEnabled())
		    					log.debug("showProfile(): lastfm similar artists=" + lastfmProfile.getSimilarArtistMap());
		    			}
		    			EchonestArtistProfile echonestProfile = (EchonestArtistProfile)artist.getMinedProfile(DATA_SOURCE_ECHONEST);
		    			if (echonestProfile != null) {
		    				if (log.isDebugEnabled())
		    					log.debug("showProfiel(): echonest similar artists=" + echonestProfile.getSimilarArtists());
		    			}
		    			IdiomagArtistProfile idiomagProfile = (IdiomagArtistProfile)artist.getMinedProfile(DATA_SOURCE_IDIOMAG);
		    			if (idiomagProfile != null) {
		    				if (log.isDebugEnabled())
		    					log.debug("showProfiel(): idiomag similar artists=" + idiomagProfile.getRecommendedArtists());
		    			}
	    			}
    			} else if (profile instanceof LabelProfile) {
    				profileDelegate = new LabelProfileDelegate((LabelProfile)profile, itemDetailTabsWidget);
    				LabelProfile label = (LabelProfile)profile;
    				if (RE3Properties.getBoolean("enable_triggered_data_miners"))
    					TaskManager.runForegroundTask(new TriggeredMinerStarter(label));
	    			LabelProfile.loadProperties();
    			} else if (profile instanceof ReleaseProfile) {
    				profileDelegate = new ReleaseProfileDelegate((ReleaseProfile)profile, itemDetailTabsWidget);
    				ReleaseProfile release = (ReleaseProfile)profile;
    				if (RE3Properties.getBoolean("enable_triggered_data_miners"))
    					TaskManager.runForegroundTask(new TriggeredMinerStarter(release));
	    			ReleaseProfile.loadProperties();
    			} else if (profile instanceof SongProfile) {
    				profileDelegate = new SongProfileDelegate((SongProfile)profile, itemDetailTabsWidget);
    				SongProfile song = (SongProfile)profile;
    				if (RE3Properties.getBoolean("enable_triggered_data_miners"))
    					TaskManager.runForegroundTask(new TriggeredMinerStarter(song));
	    			SongProfile.loadProperties();
    			} else if (profile instanceof StyleProfile) {
    				StyleProfile style = (StyleProfile)profile;
    				profileDelegate = new StyleProfileDelegate(itemDetailTabsWidget, style);
    			} else if (profile instanceof TagProfile) {
    				TagProfile tag = (TagProfile)profile;
    				profileDelegate = new TagProfileDelegate(itemDetailTabsWidget, tag);
    			} else if (profile instanceof DynamicPlaylistProfile) {
    				DynamicPlaylistProfile playlist = (DynamicPlaylistProfile)profile;
    				profileDelegate = new DynamicPlaylistProfileDelegate(itemDetailTabsWidget, playlist);
    			} else if (profile instanceof OrderedPlaylistProfile) {
    				OrderedPlaylistProfile playlist = (OrderedPlaylistProfile)profile;
    				profileDelegate = new OrderedPlaylistProfileDelegate(itemDetailTabsWidget, playlist);
    			}
    		}
    		if (profileDelegate != null) {
    			backButton.setVisible(true);
    			playButton.setVisible(true);
    			nextButton.setVisible(true);
    			CentralWidgetUI.instance.checkDetailsSectionSize();
    			int numTabs = 0;
	    		for (Tab tab : profileDelegate.getTabsCached()) {
	    			itemDetailTabsWidget.addTab(tab.getContent(), tab.getName());
	    			++numTabs;
	    		}
        		itemDetailTabsWidget.currentChanged.connect(this, "currentChanged(Integer)");
	    		detailsLayout.insertWidget(0, profileDelegate.createImageViewerWidget(detailsWidget, profile));
	    		//detailsLayout.insertWidget(0, profileDelegate.getTitleWidget());
	    		QWidget titleWidget = profileDelegate.getTitleWidget();
	    		//titleWidget.setMaximumHeight(PROFILE_TITLEBAR_MAX_HEIGHT);
	        	QSizePolicy titleWidgetPolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
	        	titleWidget.setSizePolicy(titleWidgetPolicy);
	    		profileTitleBarLayout.insertWidget(0, titleWidget);
	    		if (!(profile instanceof SearchProfile))
	    			updateTrail = false;
	    		if (updateTrail) {
		    		// update profile trail
		    		if (profileTrailIndex == -1) {
		    			profileTrailRecords.add(profile.getRecord());
		    			profileTrailIndex = 0;
		    		} else if ((profileTrailIndex + 1) < profileTrailRecords.size()) {
		    			Profile nextProfile = Database.getProfile(profileTrailRecords.get(profileTrailIndex + 1).getIdentifier());
		    			if (!nextProfile.equals(profile)) {
		    				while (profileTrailRecords.size() > (profileTrailIndex + 1))
		    					profileTrailRecords.remove(profileTrailRecords.size() - 1);
		    				profileTrailRecords.add(profile.getRecord());
		    			}
		    			++profileTrailIndex;
		    		} else {
		    			profileTrailRecords.add(profile.getRecord());
		    			++profileTrailIndex;
		    		}
		    		currentProfile = null;
		    		fireTrailChanged();
	    		} else {
	    			currentProfile = profile;
	    		}

	    		fireCurrentProfileChanged();
	    		setBackNextButtonStates();

	    		if (profileDelegate.getTabIndexTitle() != null) {
	    			for (int i = 0; i < numTabs; ++i) {
	    		    	String tabTitle = itemDetailTabsWidget.tabText(i);
	    		    	int lastIndex = tabTitle.lastIndexOf(" ("); // strip away counts for tabs like mixouts
	    		    	if (lastIndex >= 0)
	    		    		tabTitle = tabTitle.substring(0, lastIndex);
	    				if (tabTitle.equals(profileDelegate.getTabIndexTitle())) {
	    					itemDetailTabsWidget.setCurrentIndex(i);
	    					break;
	    				}
	    			}
	    		}

    			setSplitterSizes();
	    		if (profile instanceof SongProfile) {
    				stageWidget.setCurrentSong((SongProfile)profile);
    				SearchWidgetUI.instance.updateFilter();
	    		}
    		}
    	} catch (Exception e) {
    		log.error("loadProfile(): error", e);
    	}
    }

    public void setBackNextButtonStates() {
		if ((profileTrailRecords != null) && ((profileTrailIndex + 1) < profileTrailRecords.size()))
			nextButton.setEnabled(true);
		else {
			boolean hasNextItemInView = false;
			if ((SearchWidgetUI.instance != null) && (getCurrentProfile() != null)) {
				if (SearchWidgetUI.instance.getCurrentSearchView().getRecordTableModelManager().getIndex().doesExist(getCurrentProfile().getIdentifier())) {
					Integer row = SearchWidgetUI.instance.getCurrentSearchView().getRecordTableModelManager().getRowForUniqueId(getCurrentProfile().getUniqueId());
					if (row != null) {
						QModelIndex viewIndex = SearchWidgetUI.instance.getCurrentSearchView().getProxyModel().mapFromSource(SearchWidgetUI.instance.getCurrentSearchView().getRecordTableModelManager().getSourceModel().index(row, 0));
						if (viewIndex != null) {
							int proxyRow = viewIndex.row();
							if (proxyRow + 1 < SearchWidgetUI.instance.getCurrentSearchView().getProxyModel().rowCount())
								hasNextItemInView = true;
						}
					}
				}
			}
			if (hasNextItemInView)
				nextButton.setEnabled(true);
			else
				nextButton.setEnabled(false);
		}
		if (profileTrailIndex > 0)
			backButton.setEnabled(true);
		else
			backButton.setEnabled(false);
    }

    private void back(Boolean checked) {
    	--profileTrailIndex;
    	currentProfile = null;
    	showProfile(Database.getProfile(profileTrailRecords.get(profileTrailIndex).getIdentifier()), false);
    }

    private void next(Boolean checked) {
    	Profile oldProfile = getCurrentProfile();
    	++profileTrailIndex;
    	currentProfile = null;
    	if (profileTrailIndex < profileTrailRecords.size())
    		showProfile(Database.getProfile(profileTrailRecords.get(profileTrailIndex).getIdentifier()), false);
    	else {
    		--profileTrailIndex;
    		// advance to next in search window
			if ((SearchWidgetUI.instance != null) && (oldProfile != null)) {
				if (SearchWidgetUI.instance.getCurrentSearchView().getRecordTableModelManager().getIndex().doesExist(oldProfile.getIdentifier())) {
					int row = SearchWidgetUI.instance.getCurrentSearchView().getRecordTableModelManager().getRowForUniqueId(oldProfile.getUniqueId());
					QModelIndex viewIndex = SearchWidgetUI.instance.getCurrentSearchView().getProxyModel().mapFromSource(SearchWidgetUI.instance.getCurrentSearchView().getRecordTableModelManager().getSourceModel().index(row, 0));
					if (viewIndex != null) {
						int proxyRow = viewIndex.row();
						QModelIndex nextSourceIndex = SearchWidgetUI.instance.getCurrentSearchView().getProxyModel().mapToSource(SearchWidgetUI.instance.getCurrentSearchView().getProxyModel().index(proxyRow + 1, 0));
						if (nextSourceIndex != null) {
							Record record = SearchWidgetUI.instance.getCurrentSearchView().getRecordTableModelManager().getRecordForRow(nextSourceIndex.row());
							if (record != null) {
								Profile profile = SearchWidgetUI.instance.getCurrentSearchView().getRecordTableModelManager().getIndex().getProfile(record.getUniqueId());
								if (profile != null) {
									showProfile(profile, true);
								}
							}
						}
					}
				}
			}
    	}
    }

    private void play(Boolean checked) {
    	Vector<Integer> playlist = new Vector<Integer>();
    	Record record = profileTrailRecords.get(profileTrailIndex);
		if (record instanceof SongGroupRecord) {
			SongGroupRecord groupRecord = (SongGroupRecord)record;
			Vector<SongRecord> groupSongs = groupRecord.getSongs();
			for (SongRecord groupSong : groupSongs) {
				Integer songId = groupSong.getUniqueId();
				if (!playlist.contains(songId))
					playlist.add(songId);
			}
		} else if (record instanceof SongRecord) {
			// song
			SongRecord song = (SongRecord)record;
			Integer songId = song.getUniqueId();
			if (!playlist.contains(songId))
				playlist.add(songId);
		} else if (record instanceof OrderedPlaylistRecord) {
			OrderedPlaylistRecord orderedList = (OrderedPlaylistRecord)record;
			for (int songId : orderedList.getSongIds())
				playlist.add(songId);
		} else if (record instanceof FilterRecord) {
			FilterRecord filterRecord = (FilterRecord)record;
			for (SearchResult song : filterRecord.getSongRecords())
				playlist.add(song.getRecord().getUniqueId());
		}
    	PlayerManager.playSongs(playlist);
    }

    public void saveAsPlaylist() {
    	saveAsPlaylist(null, true);
    }
    public String saveAsPlaylist(String initialValue, boolean editProfile) {
    	try {
	    	AddFilterDialog addFilterDialog = new AddFilterDialog(Database.getPlaylistModelManager().getTypeDescription());
	    	if (initialValue != null)
	    		addFilterDialog.setFilterName(initialValue);
	    	if (addFilterDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	    		String newFilterName = addFilterDialog.getFilterName();
	    		FilterIdentifier newId = Database.getPlaylistModelManager().getFilterIdentifier(newFilterName);
	    		boolean create = false;
	    		HierarchicalRecord[] parentRecords = null;
	    		PlaylistRecord existingRecord = (PlaylistRecord)Database.getRecord(newId);
	    		if (existingRecord != null) {
	    			if (log.isDebugEnabled())
	    				log.debug("add(): filter already exists with name=" + newFilterName);
	    			String text = Translations.get("save_as_playlist_overwrite_text");
	    			text = StringUtil.replace(text, Translations.getPreferredCase("%playlistName%"), newFilterName);
					if (QMessageBox.question(this, Translations.get("save_as_playlist_overwrite_title"), text, QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) == QMessageBox.StandardButton.Yes.value()) {
						parentRecords = existingRecord.getParentRecords();
						Database.delete(newId);
						create = true;
					}
	    		} else {
	    			create = true;
	    		}
	    		if (create) {
	    			SubmittedOrderedPlaylist newPlaylist = new SubmittedOrderedPlaylist(newFilterName);
	    			for (SongRecord song : ProfileWidgetUI.instance.getSongTrail())
	    				newPlaylist.addSongId(song.getUniqueId());
	    			if (parentRecords != null) {
	    				Vector<TreeHierarchyInstance> treeInstances = new Vector<TreeHierarchyInstance>();
	    				for (HierarchicalRecord parentRecord : parentRecords)
	    					for (TreeHierarchyInstance instance : PlaylistsWidgetUI.instance.getModelManager().getMatchingInstances(parentRecord))
	    						treeInstances.add(instance);
	    				newPlaylist.setParentInstances(treeInstances);
	    			}
	    			Profile profile = Database.add(newPlaylist);
	    			if ((profile != null) && editProfile)
	    				ProfileWidgetUI.instance.editProfile(profile);
	    		}
	    		return newFilterName;
	    	}
    	} catch (Exception e) {
    		log.error("saveAsPlaylist(): error", e);
    	}
    	return null;
    }

    public void clearTrail() {
    	Record currentRecord = getCurrentRecord();
    	profileTrailRecords = new Vector<Record>();
    	if (currentRecord != null) {
    		profileTrailRecords.add(currentRecord);
    		profileTrailIndex = 0;
    	} else {
    		profileTrailIndex = -1;
    	}
    	backButton.setDisabled(true);
    	nextButton.setDisabled(true);
    	fireTrailChanged();
    }

	@Override
	public void addedRecord(Record record, SubmittedProfile submittedProfile) { } // should be safe to ignore this
	@Override
	public void removedRecord(Record record) {
		// TODO: test/implement further
		if (getCurrentProfile() != null) {
			if (record.getIdentifier().equals(getCurrentProfile().getIdentifier())) {
				QApplication.invokeAndWait(new Thread() { @Override
				public void run() { CentralWidgetUI.instance.closeDetailsTab(); }});
				--profileTrailIndex;
				currentProfile = null;
			}
		}
		boolean profileTrailChanged = false;
		if (profileTrailRecords != null) {
			for (int i = 0; i < profileTrailRecords.size(); ++i) {
				if (profileTrailRecords.get(i).equals(record)) {
					profileTrailRecords.remove(i);
					if (i < profileTrailIndex)
						--profileTrailIndex;
					--i;
					fireTrailChanged();
					profileTrailChanged = true;
					currentProfile = null;
				}
			}
		}
		if (profileTrailChanged) {
			if (TrailDialog.instance != null) {
				TrailDialog.instance.refresh();
			}
		}
	}
	@Override
	public void updatedRecord(Record record) {
		if (getCurrentProfile() != null) {
			if ((record.getDataType() == getCurrentProfile().getRecord().getDataType()) && (record.getUniqueId() == getCurrentProfile().getUniqueId())) {
			//if (record.getIdentifier().equals(getCurrentProfile().getIdentifier())) {
				if (updateImmediately) {
					if (profileDelegate != null)
						QApplication.invokeAndWait(new ProfileDelegateRefresh(profileDelegate));
					updateImmediately = false;
				} else {
					refresh();
				}
			}
		}
		boolean profileTrailChanged = false;
		if (profileTrailRecords != null) {
			for (Record trailRecord : profileTrailRecords) {
				if (trailRecord.equals(record)) {
					profileTrailChanged = true;
				}
			}
		}
		if (profileTrailChanged) {
			if (TrailDialog.instance != null) {
				TrailDialog.instance.refresh();
			}
		}
	}

	public void refresh() {
		if ((ProfileDelegateRefreshDelay.instance != null) && !ProfileDelegateRefreshDelay.instance.isDoneDelaying())
			return;
		TaskManager.runForegroundTask(new ProfileDelegateRefreshDelay());
	}

	@Override
	public void profilesMerged(Profile primaryProfile, Profile mergedProfile) {
		if (getCurrentProfile() != null) {
			if (mergedProfile.getIdentifier().equals(getCurrentProfile().getIdentifier())) {
				// see if this was merged, and if the primary exists
				showProfile(primaryProfile, false);
			}
		}
		for (int i = 0; i < profileTrailRecords.size(); ++i) {
			if (profileTrailRecords.get(i).equals(mergedProfile.getRecord())) {
				profileTrailRecords.set(i, primaryProfile.getRecord());
				fireTrailChanged();
			}
		}
	}

	protected void closeDetails() {
		CentralWidgetUI.instance.closeDetailsTab();
	}

	protected boolean toggleStage() {
    	List<Integer> sizes = stageSplitter.sizes();
    	if (sizes.get(1) == 0) {
    		// expand it
    		stageSplitter.restorePosition();
    		return true;
    	} else {
    		// hide it
        	ArrayList<Integer> splitterSizes = new ArrayList<Integer>();
        	splitterSizes.add(sizes.get(0) + sizes.get(1));
        	splitterSizes.add(0);
            stageSplitter.setSizes(splitterSizes);
            return false;
    	}
	}

    public boolean isStageVisible() {
    	List<Integer> sizes = stageSplitter.sizes();
    	if (sizes.get(1) == 0)
    		return false;
    	return true;
    }

    public void setStageButtonState(boolean showing) {
    	Profile profile = getCurrentProfile();
    	if (profile == null)
    		return;
    	if (profile instanceof SongProfile) {
    		stageButton.setVisible(true);
    		stageWidget.setVisible(true);
	    	if (showing) {
	    		stageButton.setIcon(new QIcon(RE3Properties.getProperty("stage_collapse_icon")));
	    		stageButton.setToolTip(Translations.get("hide_stage_text"));
	    		UIProperties.setProperty("show_stage", "true");
	    	} else {
	    		stageButton.setIcon(new QIcon(RE3Properties.getProperty("stage_expand_icon")));
	    		stageButton.setToolTip(Translations.get("show_stage_text"));
	    		UIProperties.setProperty("show_stage", "false");
	    	}
    	} else {
    		stageButton.setVisible(false);
    		stageWidget.setVisible(false);
    	}
    }

    private void stageButtonClicked() {
    	setStageButtonState(toggleStage());
    }

    public void trailButtonClicked() {
    	TrailDialog dialog = TrailDialog.instance;
    	if (dialog == null)
    		dialog = new TrailDialog(RapidEvolution3UI.instance);
    	if (!dialog.isVisible())
    		dialog.show();
    }

    private void fireCurrentProfileChanged() {
    	for (ProfileWidgetChangeListener listener : changeListeners)
    		listener.currentProfileChanged();
    }

    private void fireTrailChanged() {
    	for (ProfileWidgetChangeListener listener : changeListeners)
    		listener.profileTrailChanged();
    }

	public void stageSplitterMoved(Integer pos, Integer index) {
		setStageButtonState(isStageVisible());
	}

	@Override
	public void stageChanged() {
		if (profileDelegate instanceof SongProfileDelegate) {
			((SongProfileDelegate)profileDelegate).updateRelativeSongWidgets();
			((SongProfileDelegate)profileDelegate).updateTitle();
			if (SearchWidgetUI.instance.isSongSearchType())
				SearchWidgetUI.instance.updateFilter();
		}
	}

	public void load(PlaylistRecord playlist, Vector<Record> profileTrail) {
    	setProfileTrail(profileTrail);
    	setProfileIndex(0);
    	trailButtonClicked();
		TrailDialog.instance.setCurrentPlaylistName(playlist.getPlaylistName());
	}

}
