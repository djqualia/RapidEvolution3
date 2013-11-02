package com.mixshare.rapid_evolution.ui.widgets.search;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.dialogs.columns.SelectColumnsDialog;
import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.model.search.SearchProxyModel;
import com.mixshare.rapid_evolution.ui.model.search.artist.ArtistProxyModel;
import com.mixshare.rapid_evolution.ui.model.search.artist.RecommendedArtistProxyModel;
import com.mixshare.rapid_evolution.ui.model.search.label.LabelProxyModel;
import com.mixshare.rapid_evolution.ui.model.search.label.RecommendedLabelProxyModel;
import com.mixshare.rapid_evolution.ui.model.search.release.RecommendedReleaseProxyModel;
import com.mixshare.rapid_evolution.ui.model.search.release.ReleaseProxyModel;
import com.mixshare.rapid_evolution.ui.model.search.song.RecommendedSongProxyModel;
import com.mixshare.rapid_evolution.ui.model.search.song.SongProxyModel;
import com.mixshare.rapid_evolution.ui.updaters.view.search.SearchLazySearch;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.CentralWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.common.search.SearchBarWidget;
import com.mixshare.rapid_evolution.ui.widgets.filter.options.AbstractSearchOptionsUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.options.artist.ArtistSearchOptionsUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.options.label.LabelSearchOptionsUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.options.release.ReleaseSearchOptionsUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.options.song.SongSearchOptionsUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.playlists.PlaylistsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.styles.StylesWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.tags.TagsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.search.artist.ArtistTableView;
import com.mixshare.rapid_evolution.ui.widgets.search.label.LabelTableView;
import com.mixshare.rapid_evolution.ui.widgets.search.release.ReleaseTableView;
import com.mixshare.rapid_evolution.ui.widgets.search.song.SongTableView;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QSpacerItem;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class SearchWidgetUI extends QWidget implements DataConstants, AllColumns {

	static private Logger log = Logger.getLogger(SearchWidgetUI.class);

	static public SearchWidgetUI instance;

    ////////////
    // FIELDS //
    ////////////

	private final SearchBarWidget searchBarWidget;

    private final SearchTableView artistSearchView;
    private final SearchTableView labelSearchView;
    private final SearchTableView releaseSearchView;
    private final SearchTableView songSearchView;

    private final SearchTableView recommendedArtistSearchView;
    private final SearchTableView recommendedLabelSearchView;
    private final SearchTableView recommendedReleaseSearchView;
    private final SearchTableView recommendedSongSearchView;

    private ArtistProxyModel artistProxyModel;
    private LabelProxyModel labelProxyModel;
    private ReleaseProxyModel releaseProxyModel;
    private SongProxyModel songProxyModel;

    private ArtistProxyModel recommendedArtistProxyModel;
    private LabelProxyModel recommendedLabelProxyModel;
    private ReleaseProxyModel recommendedReleaseProxyModel;
    private SongProxyModel recommendedSongProxyModel;

    private final String lastSearchText = "";
    private final SearchTextInputSearchDelay searchDelay;

    private final QLabel numSelectedLabel;
    private final QLabel numResultsLabel;

	private final QPushButton filterButton;
	private final QPushButton clearFiltersButton;

    transient private Vector<SearchResult> searchResults;
    transient private SearchSearchParameters searchParameters;
    transient private int lastNumSelected = 0;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public SearchWidgetUI() {
    	instance = this;

    	QSizePolicy searchWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
    	searchWidgetSizePolicy.setVerticalStretch((byte)1);
    	setSizePolicy(searchWidgetSizePolicy);

    	QVBoxLayout searchLayout = new QVBoxLayout(this);
    	searchLayout.setMargin(0);

        searchBarWidget = new SearchBarWidget(true, 0);

        searchBarWidget.getFilterCombo().addItem(Translations.get("text_my_artists"), 1);
        searchBarWidget.getFilterCombo().addItem(Translations.get("text_my_labels"), 2);
        searchBarWidget.getFilterCombo().addItem(Translations.get("text_my_releases"), 3);
        searchBarWidget.getFilterCombo().addItem(Translations.get("text_my_songs"), 4);
        if (RE3Properties.getInt("max_external_artists") != 0)
        	searchBarWidget.getFilterCombo().addItem(Translations.get("text_recommended_artists"), 5);
        if (RE3Properties.getInt("max_external_labels") != 0)
        	searchBarWidget.getFilterCombo().addItem(Translations.get("text_recommended_labels"), 6);
        if (RE3Properties.getInt("max_external_releases") != 0)
        	searchBarWidget.getFilterCombo().addItem(Translations.get("text_recommended_releases"), 7);
        if (RE3Properties.getInt("max_external_songs") != 0)
        	searchBarWidget.getFilterCombo().addItem(Translations.get("text_recommended_songs"), 8);
        searchBarWidget.getFilterCombo().setCurrentIndex(3);

        // search views
        artistSearchView = new ArtistTableView(Database.getArtistModelManager());
        labelSearchView = new LabelTableView(Database.getLabelModelManager());
        releaseSearchView = new ReleaseTableView(Database.getReleaseModelManager());
        songSearchView = new SongTableView(Database.getSongModelManager());
        recommendedArtistSearchView = new RecommendedSearchTableView(Database.getRecommendedArtistModelManager());
        recommendedLabelSearchView = new RecommendedSearchTableView(Database.getRecommendedLabelModelManager());
        recommendedReleaseSearchView = new RecommendedSearchTableView(Database.getRecommendedReleaseModelManager());
        recommendedSongSearchView = new RecommendedSearchTableView(Database.getRecommendedSongModelManager());

        searchLayout.addWidget(searchBarWidget);
        searchLayout.addWidget(artistSearchView);
        searchLayout.addWidget(labelSearchView);
        searchLayout.addWidget(releaseSearchView);
        searchLayout.addWidget(songSearchView);
        searchLayout.addWidget(recommendedArtistSearchView);
        searchLayout.addWidget(recommendedLabelSearchView);
        searchLayout.addWidget(recommendedReleaseSearchView);
        searchLayout.addWidget(recommendedSongSearchView);

        // results info section
    	filterButton = new QPushButton();
    	filterButton.clicked.connect(this, "filterButtonClicked()");
    	filterButton.setText(Translations.get("filters_expand_text"));

    	clearFiltersButton = new QPushButton();
    	clearFiltersButton.clicked.connect(this, "clearFiltersButtonClicked()");
    	clearFiltersButton.setText(Translations.get("filters_clear_text"));
    	clearFiltersButton.setVisible(false);
    	clearFiltersButton.setToolTip(Translations.get("filters_clear_tooltip"));

        QWidget resultsWidget = new QWidget();
    	QSizePolicy resultsSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
    	resultsWidget.setSizePolicy(resultsSizePolicy);
        QHBoxLayout resultsLayout = new QHBoxLayout(resultsWidget);
        resultsLayout.setMargin(0);
        resultsLayout.setObjectName("horizontalLayout");
        numSelectedLabel = new QLabel(resultsWidget);
        numSelectedLabel.setText("# selected");
        resultsLayout.addWidget(filterButton);
        resultsLayout.addWidget(clearFiltersButton);
        QSpacerItem horizontalSpacer = new QSpacerItem(0, 0, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
        QSpacerItem horizontalSpacer2 = new QSpacerItem(0, 0, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
        resultsLayout.addItem(horizontalSpacer);
        resultsLayout.addWidget(numSelectedLabel);
        resultsLayout.addItem(horizontalSpacer2);
        numResultsLabel = new QLabel(resultsWidget);
        numResultsLabel.setText("# results");
        resultsLayout.addWidget(numResultsLabel);

        if (RE3Properties.getBoolean("show_search_result_count")) {
        	searchLayout.addWidget(resultsWidget);
        	searchLayout.setStretch(0, 0);
        	searchLayout.setStretch(1, 1);
        	searchLayout.setStretch(2, 1);
        	searchLayout.setStretch(3, 1);
        	searchLayout.setStretch(4, 1);
        	searchLayout.setStretch(5, 1);
        	searchLayout.setStretch(6, 1);
        	searchLayout.setStretch(7, 1);
        	searchLayout.setStretch(8, 1);
    		searchLayout.setStretch(9, 0);
        }

        // set up search tables
        createModels();

        setSearchType();

        searchDelay = new SearchTextInputSearchDelay();

        // setup signals (events)
        searchBarWidget.getFilterText().textChanged.connect(this, "searchTextChanged()");
        searchBarWidget.getFilterCombo().currentIndexChanged.connect(this, "setSearchType()");
        searchBarWidget.getConfigureColumnsButton().clicked.connect(this, "configureColumns()");
    }

    private void createModels() {
    	// initialize the model managers (which will create the source models)
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_init_artists_table"));
    	Database.getArtistModelManager().initialize(this);
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_init_labels_table"));
    	Database.getLabelModelManager().initialize(this);
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_init_releases_table"));
    	Database.getReleaseModelManager().initialize(this);
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_init_songs_table"));
    	Database.getSongModelManager().initialize(this);
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_init_recommended_artists_table"));
    	Database.getRecommendedArtistModelManager().initialize(this);
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_init_recommended_labels_table"));
    	Database.getRecommendedLabelModelManager().initialize(this);
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_init_recommended_releases_table"));
    	Database.getRecommendedReleaseModelManager().initialize(this);
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_init_recommended_songs_table"));
    	Database.getRecommendedSongModelManager().initialize(this);
    	// change listeners
    	Database.getArtistIndex().addIndexChangeListener(Database.getArtistModelManager());
    	Database.getLabelIndex().addIndexChangeListener(Database.getLabelModelManager());
    	Database.getReleaseIndex().addIndexChangeListener(Database.getReleaseModelManager());
    	Database.getSongIndex().addIndexChangeListener(Database.getSongModelManager());
    	Database.getArtistIndex().addIndexChangeListener(Database.getRecommendedArtistModelManager());
    	Database.getLabelIndex().addIndexChangeListener(Database.getRecommendedLabelModelManager());
    	Database.getReleaseIndex().addIndexChangeListener(Database.getRecommendedReleaseModelManager());
    	Database.getSongIndex().addIndexChangeListener(Database.getRecommendedSongModelManager());
    	// setup the proxy models (for sorting/filtering)
        artistProxyModel = new ArtistProxyModel(this, Database.getArtistModelManager(), artistSearchView);
        labelProxyModel = new LabelProxyModel(this, Database.getLabelModelManager(), labelSearchView);
        releaseProxyModel = new ReleaseProxyModel(this, Database.getReleaseModelManager(), releaseSearchView);
        songProxyModel = new SongProxyModel(this, Database.getSongModelManager(), songSearchView);
        recommendedArtistProxyModel = new RecommendedArtistProxyModel(this, Database.getRecommendedArtistModelManager(), recommendedArtistSearchView);
        recommendedLabelProxyModel = new RecommendedLabelProxyModel(this, Database.getRecommendedLabelModelManager(), recommendedLabelSearchView);
        recommendedReleaseProxyModel = new RecommendedReleaseProxyModel(this, Database.getRecommendedReleaseModelManager(), recommendedReleaseSearchView);
        recommendedSongProxyModel = new RecommendedSongProxyModel(this, Database.getRecommendedSongModelManager(), recommendedSongSearchView);
        // set up tree views
        artistSearchView.setModel(artistProxyModel);
        labelSearchView.setModel(labelProxyModel);
        releaseSearchView.setModel(releaseProxyModel);
        songSearchView.setModel(songProxyModel);
        recommendedArtistSearchView.setModel(recommendedArtistProxyModel);
        recommendedLabelSearchView.setModel(recommendedLabelProxyModel);
        recommendedReleaseSearchView.setModel(recommendedReleaseProxyModel);
        recommendedSongSearchView.setModel(recommendedSongProxyModel);
        // set initial column sizes
        Database.getArtistModelManager().setSourceColumnSizes(artistSearchView);
        Database.getLabelModelManager().setSourceColumnSizes(labelSearchView);
        Database.getReleaseModelManager().setSourceColumnSizes(releaseSearchView);
        Database.getSongModelManager().setSourceColumnSizes(songSearchView);
        Database.getRecommendedArtistModelManager().setSourceColumnSizes(recommendedArtistSearchView);
        Database.getRecommendedLabelModelManager().setSourceColumnSizes(recommendedLabelSearchView);
        Database.getRecommendedReleaseModelManager().setSourceColumnSizes(recommendedReleaseSearchView);
        Database.getRecommendedSongModelManager().setSourceColumnSizes(recommendedSongSearchView);

        // link search parameters with model managers
        Database.getArtistModelManager().setSearchParams(artistProxyModel.getSearchParameters());
        Database.getLabelModelManager().setSearchParams(labelProxyModel.getSearchParameters());
        Database.getReleaseModelManager().setSearchParams(releaseProxyModel.getSearchParameters());
        Database.getSongModelManager().setSearchParams(songProxyModel.getSearchParameters());
        Database.getRecommendedArtistModelManager().setSearchParams(recommendedArtistProxyModel.getSearchParameters());
        Database.getRecommendedLabelModelManager().setSearchParams(recommendedLabelProxyModel.getSearchParameters());
        Database.getRecommendedReleaseModelManager().setSearchParams(recommendedReleaseProxyModel.getSearchParameters());
        Database.getRecommendedSongModelManager().setSearchParams(recommendedSongProxyModel.getSearchParameters());

        // init event listeners (selection changed, columns moved, etc)
        artistSearchView.setupEventListeners();
        labelSearchView.setupEventListeners();
        releaseSearchView.setupEventListeners();
        songSearchView.setupEventListeners();
        recommendedArtistSearchView.setupEventListeners();
        recommendedLabelSearchView.setupEventListeners();
        recommendedReleaseSearchView.setupEventListeners();
        recommendedSongSearchView.setupEventListeners();

        if (log.isDebugEnabled())
        	log.debug("createModels(): artist sort ordering=" + Database.getArtistModelManager().getSortOrdering());
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_sorting_artists_table"));
        artistSearchView.sortByColumn(Database.getArtistModelManager().getPrimarySortColumnOrdering().getColumnId());
        if (log.isDebugEnabled())
        	log.debug("createModels(): label sort ordering=" + Database.getLabelModelManager().getSortOrdering());
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_sorting_labels_table"));
        labelSearchView.sortByColumn(Database.getLabelModelManager().getPrimarySortColumnOrdering().getColumnId());
        if (log.isDebugEnabled())
        	log.debug("createModels(): release sort ordering=" + Database.getReleaseModelManager().getSortOrdering());
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_sorting_releases_table"));
        releaseSearchView.sortByColumn(Database.getReleaseModelManager().getPrimarySortColumnOrdering().getColumnId());
        if (log.isDebugEnabled())
        	log.debug("createModels(): song sort ordering=" + Database.getSongModelManager().getSortOrdering());
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_sorting_songs_table"));
        songSearchView.sortByColumn(Database.getSongModelManager().getPrimarySortColumnOrdering().getColumnId());

        if (log.isDebugEnabled())
        	log.debug("createModels(): recommended artist sort ordering=" + Database.getRecommendedArtistModelManager().getSortOrdering());
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_sorting_recommended_artists_table"));
        recommendedArtistSearchView.sortByColumn(Database.getRecommendedArtistModelManager().getPrimarySortColumnOrdering().getColumnId());
        if (log.isDebugEnabled())
        	log.debug("createModels(): recommended label sort ordering=" + Database.getRecommendedLabelModelManager().getSortOrdering());
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_sorting_recommended_labels_table"));
        recommendedLabelSearchView.sortByColumn(Database.getRecommendedLabelModelManager().getPrimarySortColumnOrdering().getColumnId());
        if (log.isDebugEnabled())
        	log.debug("createModels(): recommended release sort ordering=" + Database.getRecommendedReleaseModelManager().getSortOrdering());
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_sorting_recommended_releases_table"));
        recommendedReleaseSearchView.sortByColumn(Database.getRecommendedReleaseModelManager().getPrimarySortColumnOrdering().getColumnId());
        if (log.isDebugEnabled())
        	log.debug("createModels(): recommended song sort ordering=" + Database.getRecommendedSongModelManager().getSortOrdering());
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_sorting_recommended_songs_table"));
        recommendedSongSearchView.sortByColumn(Database.getRecommendedSongModelManager().getPrimarySortColumnOrdering().getColumnId());
    }

    /////////////
    // GETTERS //
    /////////////

    private SearchModelManager getCurrentModelManager() {
    	int selectedIndex = ((Integer)searchBarWidget.getFilterCombo().itemData(searchBarWidget.getFilterCombo().currentIndex())).intValue();
        if (selectedIndex == 1) {
        	return Database.getArtistModelManager();
        } else if (selectedIndex == 2) {
        	return Database.getLabelModelManager();
        } else if (selectedIndex == 3) {
        	return Database.getReleaseModelManager();
        } else if (selectedIndex == 4) {
        	return Database.getSongModelManager();
        } else if (selectedIndex == 5) {
        	return Database.getRecommendedArtistModelManager();
        } else if (selectedIndex == 6) {
        	return Database.getRecommendedLabelModelManager();
        } else if (selectedIndex == 7) {
        	return Database.getRecommendedReleaseModelManager();
        } else if (selectedIndex == 8) {
        	return Database.getRecommendedSongModelManager();
        }
        return null;
    }

    private SearchProxyModel getCurrentProxyModel() {
    	int selectedIndex = ((Integer)searchBarWidget.getFilterCombo().itemData(searchBarWidget.getFilterCombo().currentIndex())).intValue();
        if (selectedIndex == 1) {
        	return artistProxyModel;
        } else if (selectedIndex == 2) {
        	return labelProxyModel;
        } else if (selectedIndex == 3) {
        	return releaseProxyModel;
        } else if (selectedIndex == 4) {
        	return songProxyModel;
        } else if (selectedIndex == 5) {
        	return recommendedArtistProxyModel;
        } else if (selectedIndex == 6) {
        	return recommendedLabelProxyModel;
        } else if (selectedIndex == 7) {
        	return recommendedReleaseProxyModel;
        } else if (selectedIndex == 8) {
        	return recommendedSongProxyModel;
        }
        return null;
    }

    public SearchTableView getCurrentSearchView() {
    	int selectedIndex = ((Integer)searchBarWidget.getFilterCombo().itemData(searchBarWidget.getFilterCombo().currentIndex())).intValue();
        if (selectedIndex == 1) {
        	return artistSearchView;
        } else if (selectedIndex == 2) {
        	return labelSearchView;
        } else if (selectedIndex == 3) {
        	return releaseSearchView;
        } else if (selectedIndex == 4) {
        	return songSearchView;
        } else if (selectedIndex == 5) {
        	return recommendedArtistSearchView;
        } else if (selectedIndex == 6) {
        	return recommendedLabelSearchView;
        } else if (selectedIndex == 7) {
        	return recommendedReleaseSearchView;
        } else if (selectedIndex == 8) {
        	return recommendedSongSearchView;
        }
        return null;
    }

    public int getCurrentSearchType() {
    	return ((Integer)searchBarWidget.getFilterCombo().itemData(searchBarWidget.getFilterCombo().currentIndex())).intValue();
    }

    public boolean isSongSearchType() {
    	return getCurrentSearchType() == 4;
    }

    public byte getDataType() {
		byte dataType = -1;
		int selectedIndex = ((Integer)searchBarWidget.getFilterCombo().itemData(searchBarWidget.getFilterCombo().currentIndex())).intValue();
        if (selectedIndex == 1) {
        	artistSearchView.setVisible(true);
        	dataType = DATA_TYPE_ARTISTS;
        } else if (selectedIndex == 2) {
        	labelSearchView.setVisible(true);
        	dataType = DATA_TYPE_LABELS;
        } else if (selectedIndex == 3) {
        	releaseSearchView.setVisible(true);
        	dataType = DATA_TYPE_RELEASES;
        } else if (selectedIndex == 4) {
        	songSearchView.setVisible(true);
        	dataType = DATA_TYPE_SONGS;
        } else if (selectedIndex == 5) {
        	recommendedArtistSearchView.setVisible(true);
        	dataType = DATA_TYPE_ARTISTS;
        } else if (selectedIndex == 6) {
        	recommendedLabelSearchView.setVisible(true);
        	dataType = DATA_TYPE_LABELS;
        } else if (selectedIndex == 7) {
        	recommendedReleaseSearchView.setVisible(true);
        	dataType = DATA_TYPE_RELEASES;
        } else if (selectedIndex == 8) {
        	recommendedSongSearchView.setVisible(true);
        	dataType = DATA_TYPE_SONGS;
        }
        return dataType;
    }

    ////////////////////
    // SLOTS (EVENTS) //
    ////////////////////

    private void searchTextChanged() {
    	searchDelay.searchTextChanged(searchBarWidget.getFilterText().text());
    }

    /**
     * This method will invalidate the filter for the search table.
     */
    public void updateFilter() {
    	if (log.isDebugEnabled())
    		log.debug("updateFilter(): invalidating...");
    	getCurrentProxyModel().setSearchText(searchBarWidget.getFilterText().text());
    	boolean filtersEnabled = false;
    	if (StylesWidgetUI.instance != null) {
    		FilterSelection selectedStyles = StylesWidgetUI.instance.getFilterSelection();
    		getCurrentProxyModel().setStyles(selectedStyles);
    		getCurrentModelManager().setSelectedStyles(selectedStyles);
    		if (!selectedStyles.isEmpty())
    			filtersEnabled = true;
    	}
    	if (TagsWidgetUI.instance != null) {
    		FilterSelection selectedTags = TagsWidgetUI.instance.getFilterSelection();
    		getCurrentProxyModel().setTags(selectedTags);
    		getCurrentModelManager().setSelectedTags(selectedTags);
    		if (!selectedTags.isEmpty())
    			filtersEnabled = true;
    	}
    	if (PlaylistsWidgetUI.instance != null) {
    		FilterSelection selectedPlaylists = PlaylistsWidgetUI.instance.getFilterSelection();
    		getCurrentProxyModel().setPlaylists(selectedPlaylists);
    		if (!selectedPlaylists.isEmpty())
    			filtersEnabled = true;
    	}
    	AbstractSearchOptionsUI searchOptions = getCurrentSearchOptionsUI();
    	if ((searchOptions != null) && (searchOptions.getCurrentCount() > 0))
    		filtersEnabled = true;
    	getCurrentProxyModel().setSortOrdering(getCurrentModelManager().getSortOrdering());
    	if (log.isTraceEnabled())
    		log.trace("updateFilter(): sort ordering=" + getCurrentModelManager().getSortOrdering());
    	if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && getCurrentModelManager().isLazySearchSupported()) {
    		searchParameters = (SearchSearchParameters)getCurrentProxyModel().getSearchParameters();
    		searchParameters.clearRelativeProfiles();
    		Vector<ColumnOrdering> sortOrdering = getCurrentModelManager().getSortOrdering();
    		if (sortOrdering != null) {
    			byte[] sortTypes = new byte[sortOrdering.size()];
    			boolean[] sortDescending = new boolean[sortOrdering.size()];
    			int i = 0;
    			for (ColumnOrdering ordering : sortOrdering) {
    				sortDescending[i] = !ordering.isAscending();
    				sortTypes[i] = CommonSearchParameters.getSortTypeFromColumnId(ordering.getColumnId());
    				++i;
    			}
    			searchParameters.setSortType(sortTypes);
    			searchParameters.setSortDescending(sortDescending);
    		}
    		getCurrentSearchView().setEnabled(false);
			TaskManager.runForegroundTask(new SearchLazySearch(this, getCurrentModelManager()));
    	} else {
    		searchParameters = (SearchSearchParameters)getCurrentProxyModel().getSearchParameters();
    		getCurrentModelManager().getIndex().computeSearchScores(searchParameters);
    		if (getCurrentModelManager().isColumnVisible(COLUMN_FILTERS_MATCH))
    			getCurrentModelManager().refreshColumn(COLUMN_FILTERS_MATCH);
    		if (getCurrentModelManager().isColumnVisible(COLUMN_STYLES_MATCH))
    			getCurrentModelManager().refreshColumn(COLUMN_STYLES_MATCH);
    		if (getCurrentModelManager().isColumnVisible(COLUMN_TAGS_MATCH))
    			getCurrentModelManager().refreshColumn(COLUMN_TAGS_MATCH);
    		if (getCurrentModelManager().isColumnVisible(COLUMN_SIMILARITY))
    			getCurrentModelManager().refreshColumn(COLUMN_SIMILARITY);
    		getCurrentProxyModel().applyFilter();
    		getCurrentModelManager().setSourceColumnVisibilities(getCurrentSearchView());
    		updateResultLabel();
    	}
    	if (ProfileWidgetUI.instance != null) {
    		ProfileWidgetUI.instance.setBackNextButtonStates();
    	}
    	if (!filtersEnabled)
    		clearFiltersButton.setVisible(false);
    	else
    		clearFiltersButton.setVisible(true);
    }

    public AbstractSearchOptionsUI getCurrentSearchOptionsUI() {
    	if (getDataType() == DATA_TYPE_ARTISTS)
    		return ArtistSearchOptionsUI.instance;
    	if (getDataType() == DATA_TYPE_LABELS)
    		return LabelSearchOptionsUI.instance;
    	if (getDataType() == DATA_TYPE_RELEASES)
    		return ReleaseSearchOptionsUI.instance;
    	if (getDataType() == DATA_TYPE_SONGS)
    		return SongSearchOptionsUI.instance;
    	return null;
    }

    public void lazySearch(SearchModelManager currentModelManager) {
    	if (currentModelManager.getIndex() != searchParameters.getIndex())
    		return;
    	searchResults = new Vector<SearchResult>();
    	if (!searchParameters.isEmpty()) {
    		searchResults = currentModelManager.getIndex().searchRecords(searchParameters);
    		if (log.isDebugEnabled())
    			log.debug("lazySearch(): # results=" + searchResults.size());
    	}
    	QApplication.invokeAndWait(new LazySearchComplete(currentModelManager));
    }

    private class LazySearchComplete extends Thread {
    	private final SearchModelManager currentModelManager;
    	public LazySearchComplete(SearchModelManager currentModelManager) {
    		this.currentModelManager = currentModelManager;
    	}
    	@Override
		public void run() {
    		if (currentModelManager == getCurrentModelManager()) {
	    		getCurrentModelManager().loadData(searchResults, searchParameters);
	    		getCurrentProxyModel().invalidate();
	    		getCurrentSearchView().setEnabled(true);
	    		updateResultLabel();
    		}
    	}
    }

    public void setSortOrdering(Vector<ColumnOrdering> sortOrdering, byte[] sortTypes, boolean[] sortDescending) {
    	if (log.isTraceEnabled())
    		log.trace("setSortOrdering(): sortOrdering=" + sortOrdering);
    	getCurrentModelManager().setSortOrdering(sortOrdering);
    	getCurrentProxyModel().setSortOrdering(sortOrdering);
    	getCurrentSearchView().sortByColumn(getCurrentModelManager().getPrimarySortColumnOrdering().getColumnId());
    }

    public ArtistSearchParameters getArtistSearchParameters() {
    	return (ArtistSearchParameters)artistProxyModel.getSearchParameters();
    }
    public ArtistSearchParameters getRecommendedArtistSearchParameters() {
    	return (ArtistSearchParameters)recommendedArtistProxyModel.getSearchParameters();
    }
    public LabelSearchParameters getLabelSearchParameters() {
    	return (LabelSearchParameters)labelProxyModel.getSearchParameters();
    }
    public LabelSearchParameters getRecommendedLabelSearchParameters() {
    	return (LabelSearchParameters)recommendedLabelProxyModel.getSearchParameters();
    }
    public ReleaseSearchParameters getReleaseSearchParameters() {
    	return (ReleaseSearchParameters)releaseProxyModel.getSearchParameters();
    }
    public ReleaseSearchParameters getRecommendedReleaseSearchParameters() {
    	return (ReleaseSearchParameters)recommendedReleaseProxyModel.getSearchParameters();
    }
    public SongSearchParameters getSongSearchParameters() {
    	return (SongSearchParameters)songProxyModel.getSearchParameters();
    }
    public SongSearchParameters getRecommendedSongSearchParameters() {
    	return (SongSearchParameters)recommendedSongProxyModel.getSearchParameters();
    }

    public void updateResultLabel() {
    	updateResultLabel(getCurrentProxyModel().rowCount());
    }
    public void updateResultLabel(int numRows) {
    	if (numResultsLabel.nativePointer() != null) {
    		if (numRows == 1)
	    		numResultsLabel.setText(Translations.getPreferredCase("1 result"));
	    	else {
	    		if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && getCurrentModelManager().isLazySearchSupported() && (numRows >= RE3Properties.getInt("lazy_search_mode_max_results")))
	    			numResultsLabel.setText(numRows + Translations.getPreferredCase(" results [max reached]"));
	    		else if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && getCurrentModelManager().isLazySearchSupported() && (numRows == 0) && getCurrentModelManager().getSearchProxyModel().getSearchParameters().isEmpty())
	    			numResultsLabel.setText(Translations.getPreferredCase("[no search criteria provided]"));
	    		else
	    			numResultsLabel.setText(numRows + Translations.getPreferredCase(" results"));
	    	}
    	}
    }
    public void updateSelectedLabel(int numSelected) {
    	if (numSelectedLabel.nativePointer() != null)
    		numSelectedLabel.setText(numSelected + Translations.getPreferredCase(" selected"));
    	lastNumSelected = numSelected;
    }
    public void updateSelectedLabel() { updateSelectedLabel(getCurrentSearchView().getSelectedRecords().size()); }
    public int getLastNumSelected() { return lastNumSelected; }

    public void clearSearchText() {
    	searchBarWidget.getFilterText().setText("");
    }

    public void setSongSearchType() {
    	int selectedIndex = ((Integer)searchBarWidget.getFilterCombo().itemData(searchBarWidget.getFilterCombo().currentIndex())).intValue();
    	if (selectedIndex != 4) {
    		searchBarWidget.getFilterCombo().setCurrentIndex(3);
    		setSearchType();
    	}
    }
    private void setSearchType() {
    	try {
    		artistSearchView.setVisible(false);
    		labelSearchView.setVisible(false);
    		releaseSearchView.setVisible(false);
    		songSearchView.setVisible(false);
    		recommendedArtistSearchView.setVisible(false);
    		recommendedLabelSearchView.setVisible(false);
    		recommendedReleaseSearchView.setVisible(false);
    		recommendedSongSearchView.setVisible(false);
    		int selectedIndex = ((Integer)searchBarWidget.getFilterCombo().itemData(searchBarWidget.getFilterCombo().currentIndex())).intValue();
    		byte dataType = -1;
            if (selectedIndex == 1) {
            	artistSearchView.setVisible(true);
            	dataType = DATA_TYPE_ARTISTS;
            } else if (selectedIndex == 2) {
            	labelSearchView.setVisible(true);
            	dataType = DATA_TYPE_LABELS;
            } else if (selectedIndex == 3) {
            	releaseSearchView.setVisible(true);
            	dataType = DATA_TYPE_RELEASES;
            } else if (selectedIndex == 4) {
            	songSearchView.setVisible(true);
            	dataType = DATA_TYPE_SONGS;
            } else if (selectedIndex == 5) {
            	recommendedArtistSearchView.setVisible(true);
            	dataType = DATA_TYPE_ARTISTS;
            } else if (selectedIndex == 6) {
            	recommendedLabelSearchView.setVisible(true);
            	dataType = DATA_TYPE_LABELS;
            } else if (selectedIndex == 7) {
            	recommendedReleaseSearchView.setVisible(true);
            	dataType = DATA_TYPE_RELEASES;
            } else if (selectedIndex == 8) {
            	recommendedSongSearchView.setVisible(true);
            	dataType = DATA_TYPE_SONGS;
            }
            if (CentralWidgetUI.instance != null)
            	CentralWidgetUI.instance.setFilterType(dataType);
            // if the search text is cleared between views
            searchBarWidget.getFilterText().setText(getCurrentProxyModel().getSearchText());
            StylesWidgetUI.instance.updateFilter();
            TagsWidgetUI.instance.updateFilter();
            PlaylistsWidgetUI.instance.updateFilter();
            updateFilter();
    	} catch (Exception e) {
    		log.error("setSearchType(): error", e);
    	}
    }

    protected void configureColumns() {
    	SelectColumnsDialog selectColumnsDialog = new SelectColumnsDialog(this, getCurrentModelManager());
    	if (selectColumnsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		selectColumnsDialog.saveSelections();
    		getCurrentModelManager().setSourceColumnSizes(getCurrentSearchView());
    	}
    }

    public void setFilterButtonState(boolean showing) {
    	if (showing) {
    		filterButton.setIcon(new QIcon(RE3Properties.getProperty("filter_collapse_icon")));
    		filterButton.setToolTip(Translations.get("hide_filters_text"));
    		UIProperties.setProperty("show_filters", "true");
    	} else {
    		filterButton.setIcon(new QIcon(RE3Properties.getProperty("filter_expand_icon")));
    		filterButton.setToolTip(Translations.get("show_filters_text"));
    		UIProperties.setProperty("show_filters", "false");
    	}
    }

    private void filterButtonClicked() {
    	setFilterButtonState(CentralWidgetUI.instance.toggleFilter());
    }

    private void clearFiltersButtonClicked() {
    	CentralWidgetUI.instance.clearAllFilters();
    	clearFiltersButton.setVisible(false);
    }

}
