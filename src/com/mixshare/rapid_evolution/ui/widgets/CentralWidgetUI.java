package com.mixshare.rapid_evolution.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedDynamicPlaylist;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.dialogs.filter.AddFilterDialog;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.search.artist.ArtistProxyModel;
import com.mixshare.rapid_evolution.ui.model.search.label.LabelProxyModel;
import com.mixshare.rapid_evolution.ui.model.search.release.ReleaseProxyModel;
import com.mixshare.rapid_evolution.ui.model.search.song.SongProxyModel;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.splitter.RESplitter;
import com.mixshare.rapid_evolution.ui.widgets.filter.options.artist.ArtistSearchOptionsUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.options.label.LabelSearchOptionsUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.options.release.ReleaseSearchOptionsUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.options.song.SongSearchOptionsUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.playlists.PlaylistsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.styles.StylesWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.tags.TagsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QToolBox;
import com.trolltech.qt.gui.QTreeView;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class CentralWidgetUI extends QWidget implements DataConstants {

	static private Logger log = Logger.getLogger(CentralWidgetUI.class);

	static private int MINIMUM_SIZE = 50;

	static public CentralWidgetUI instance = null;

	////////////
	// FIELDS //
	////////////

    private QTreeView playlistsTree;

    private final RESplitter mainSplitter; // separates styles and search/details sides (horizontal)
	private final RESplitter searchSplitter; // separates the search table and the item details (vertical)

	private final ProfileWidgetUI profileWidget;

	private final SearchWidgetUI searchWidget;
	private final StylesWidgetUI stylesWidget;
	private final TagsWidgetUI tagsWidget;
	private final PlaylistsWidgetUI playlistWidget;

	private final ArtistSearchOptionsUI artistSearchOptions;
	private final LabelSearchOptionsUI labelSearchOptions;
	private final ReleaseSearchOptionsUI releaseSearchOptions;
	private final SongSearchOptionsUI songSearchOptions;
	private int numArtistFilters;
	private int numLabelFilters;
	private int numReleaseFilters;
	private int numSongFilters;

	private final QToolBox filterToolbox;

	private int initialDetailsSize = 250;

	private final QAction clearAllAction;
	private final QAction saveAsPlaylistAction;

	/////////////////
    // CONSTRUCTOR //
	/////////////////

    public CentralWidgetUI() {

    	mainSplitter = new RESplitter("main_splitter", false); // separates styles and search/details sides (horizontal)
    	mainSplitter.splitterMoved.connect(this, "mainSplitterMoved(Integer, Integer)");
    	searchSplitter = new RESplitter("search_splitter", true); // separates the search table and the item details (vertical)

    	// search options
    	filterToolbox = new QToolBox();
    	filterToolbox.setMinimumSize(new QSize(MINIMUM_SIZE, MINIMUM_SIZE));
    	QSizePolicy filterToolboxSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
    	filterToolboxSizePolicy.setHorizontalStretch((byte)0);
    	filterToolbox.setSizePolicy(filterToolboxSizePolicy);

    	// styles pane
    	stylesWidget = new StylesWidgetUI(Database.getStyleModelManager());
    	filterToolbox.addItem(stylesWidget, Translations.get("text_styles"));
    	// tags pane
    	tagsWidget = new TagsWidgetUI(Database.getTagModelManager());
    	filterToolbox.addItem(tagsWidget, Translations.get("text_tags"));
    	// playlists pane
    	playlistWidget = new PlaylistsWidgetUI(Database.getPlaylistModelManager());
    	filterToolbox.addItem(playlistWidget, Translations.get("text_playlists"));
    	// search options panes
    	artistSearchOptions = new ArtistSearchOptionsUI();
    	labelSearchOptions = new LabelSearchOptionsUI();
    	releaseSearchOptions = new ReleaseSearchOptionsUI();
    	songSearchOptions = new SongSearchOptionsUI();
    	filterToolbox.addItem(songSearchOptions, Translations.get("song_search_filters"));
    	if (UIProperties.hasProperty("filter_toolbox_index"))
    		filterToolbox.setCurrentIndex(UIProperties.getInt("filter_toolbox_index"));
    	filterToolbox.currentChanged.connect(this, "filterToolBoxChanged(Integer)");

    	clearAllAction = new QAction(Translations.get("clear_all_filters_text"), this);
    	clearAllAction.triggered.connect(this, "clearAllFilters()");
    	clearAllAction.setIcon(new QIcon(RE3Properties.getProperty("menu_clear_filters_icon")));

    	saveAsPlaylistAction = new QAction(Translations.get("save_filters_as_playlist_text"), this);
    	saveAsPlaylistAction.triggered.connect(this, "saveAsPlaylist()");
    	saveAsPlaylistAction.setIcon(new QIcon(RE3Properties.getProperty("menu_save_as_playlist_icon")));

    	filterToolbox.setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);
    	filterToolbox.addAction(clearAllAction);
    	filterToolbox.addAction(saveAsPlaylistAction);

    	// search pane
    	searchSplitter.setOrientation(Orientation.Vertical);
    	QSizePolicy searchSplitterSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
    	searchSplitterSizePolicy.setHorizontalStretch((byte)1);
    	searchSplitter.setSizePolicy(searchSplitterSizePolicy);
    	profileWidget = new ProfileWidgetUI();

    	searchWidget = new SearchWidgetUI();

        searchSplitter.addWidget(searchWidget);
    	searchSplitter.addWidget(profileWidget);

    	mainSplitter.addWidget(filterToolbox);
    	mainSplitter.addWidget(searchSplitter);

    	QVBoxLayout mainLayout = new QVBoxLayout();
        mainLayout.addWidget(mainSplitter);
        setLayout(mainLayout);

        // set defaults
        if (!UIProperties.hasProperty("main_splitter_size_1")) {
	        UIProperties.setProperty("main_splitter_size_1", "180");
	        UIProperties.setProperty("main_splitter_size_2", "620");
        }
        ArrayList<Integer> mainSplitterSizes = new ArrayList<Integer>();
        mainSplitterSizes.add(180);
        mainSplitterSizes.add(620);
        mainSplitter.setSizes(mainSplitterSizes);

        ArrayList<Integer> searchSplitterSizes = new ArrayList<Integer>();
        searchSplitterSizes.add(600);
        searchSplitterSizes.add(0);
        searchSplitter.setSizes(searchSplitterSizes);

        instance = this;

        setWindowIcon(new QIcon("icons/logo.ico"));
    }

    public void setFilterType(byte dataType) {
    	boolean set = false;
    	if (filterToolbox.currentIndex() == 3) {
    		set = true;
    	}
    	filterToolbox.removeItem(3);
    	if (dataType == DATA_TYPE_ARTISTS)
    		filterToolbox.addItem(artistSearchOptions, getArtistFilterTitle());
    	else if (dataType == DATA_TYPE_LABELS)
    		filterToolbox.addItem(labelSearchOptions, getLabelFilterTitle());
    	else if (dataType == DATA_TYPE_RELEASES)
    		filterToolbox.addItem(releaseSearchOptions, getReleaseFilterTitle());
    	else if (dataType == DATA_TYPE_SONGS)
    		filterToolbox.addItem(songSearchOptions, getSongFilterTitle());
    	if (set)
    		filterToolbox.setCurrentIndex(3);
    }

    public String getArtistFilterTitle() {
    	if (numArtistFilters == 0)
    		return Translations.get("artist_search_filters");
    	return Translations.get("artist_search_filters") + "  (" + numArtistFilters + " " + Translations.get("text_enabled_suffix") + ")";
    }
    public String getLabelFilterTitle() {
    	if (numLabelFilters == 0)
    		return Translations.get("label_search_filters");
    	return Translations.get("label_search_filters") + "  (" + numLabelFilters + " " + Translations.get("text_enabled_suffix") + ")";
    }
    public String getReleaseFilterTitle() {
    	if (numReleaseFilters == 0)
    		return Translations.get("release_search_filters");
    	return Translations.get("release_search_filters") + "  (" + numReleaseFilters + " " + Translations.get("text_enabled_suffix") + ")";
    }
    public String getSongFilterTitle() {
    	if (numSongFilters == 0)
    		return Translations.get("song_search_filters");
    	return Translations.get("song_search_filters") + "  (" + numSongFilters + " " + Translations.get("text_enabled_suffix") + ")";
    }

    public void setSplitterSizes() {
    	if (UIProperties.getBoolean("show_filters")) {
	    	mainSplitter.restorePosition();
    	} else {
    		List<Integer> sizes = mainSplitter.sizes();
            ArrayList<Integer> mainSplitterSizes = new ArrayList<Integer>();
            mainSplitterSizes.add(0);
            mainSplitterSizes.add(sizes.get(0) + sizes.get(1));
            mainSplitter.setSizes(mainSplitterSizes);
    	}
        searchWidget.setFilterButtonState(areFiltersVisible());

        int size = searchSplitter.restoreFirstPosition();
    	if (size != 0)
    		initialDetailsSize = size;
    	profileWidget.setSplitterSizes();
    }
	public void mainSplitterMoved(Integer pos, Integer index) {
		searchWidget.setFilterButtonState(areFiltersVisible());
	}

    public void checkDetailsSectionSize() {
    	int size = searchSplitter.sizes().get(1);
    	if (size <= MINIMUM_SIZE) {
    		if (log.isTraceEnabled())
    			log.trace("checkDetailsSectionSize(): restoring splitter position, initialDetailsSize=" + initialDetailsSize);
    		searchSplitter.restorePosition(searchSplitter.height() - initialDetailsSize, initialDetailsSize, MINIMUM_SIZE);
    	} else {
    		if (log.isTraceEnabled())
    			log.trace("checkDetailsSectionSize(): existing size=" + size);
    	}
    }

    public void setStyleItemText(int numSelected) {
    	if (numSelected == 0)
    		filterToolbox.setItemText(0, Translations.get("text_styles"));
    	else
    		filterToolbox.setItemText(0, Translations.get("text_styles")  + "  (" + numSelected + " " + Translations.get("text_selected_suffix") + ")");
    }

    public void setTagItemText(int numSelected) {
    	if (numSelected == 0)
    		filterToolbox.setItemText(1, Translations.get("text_tags"));
    	else
    		filterToolbox.setItemText(1, Translations.get("text_tags") + "  (" + numSelected + " " + Translations.get("text_selected_suffix") + ")");
    }

    public void setPlaylistsItemText(int numSelected) {
    	if (numSelected == 0)
    		filterToolbox.setItemText(2, Translations.get("text_playlists"));
    	else
    		filterToolbox.setItemText(2, Translations.get("text_playlists") + "  (" + numSelected + " " + Translations.get("text_selected_suffix") + ")");
    }

    public void setNumArtistFilters(int numFilters) {
    	numArtistFilters = numFilters;
    	filterToolbox.setItemText(3, getArtistFilterTitle());
    }
    public void setNumLabelFilters(int numFilters) {
    	numLabelFilters = numFilters;
    	filterToolbox.setItemText(3, getLabelFilterTitle());
    }
    public void setNumReleaseFilters(int numFilters) {
    	numReleaseFilters = numFilters;
    	filterToolbox.setItemText(3, getReleaseFilterTitle());
    }
    public void setNumSongFilters(int numFilters) {
    	numSongFilters = numFilters;
    	filterToolbox.setItemText(3, getSongFilterTitle());
    }

    public void closeDetailsTab() {
        ArrayList<Integer> searchSplitterSizes = new ArrayList<Integer>();
        searchSplitterSizes.add(searchSplitter.height());
        searchSplitterSizes.add(0);
        searchSplitter.setSizes(searchSplitterSizes);
        profileWidget.clearProfile();
    }

    public boolean isDetailsTabOpen() {
    	return (searchSplitter.sizes().get(1) != 0);
    }

    public void setPlaylistsSelected() {
    	filterToolbox.setCurrentIndex(2);
    }

    public void clearAllFilters() {
    	stylesWidget.clearSelections();
    	tagsWidget.clearSelections();
    	playlistWidget.clearSelections();
    	artistSearchOptions.clearFilters();
    	labelSearchOptions.clearFilters();
    	releaseSearchOptions.clearFilters();
    	songSearchOptions.clearFilters();
    	numArtistFilters = 0;
    	numLabelFilters = 0;
    	numReleaseFilters = 0;
    	numSongFilters = 0;
    	setFilterType(SearchWidgetUI.instance.getDataType());
    	SearchWidgetUI.instance.updateFilter();
    }

    protected void saveAsPlaylist() {
    	AddFilterDialog addFilterDialog = new AddFilterDialog(Database.getPlaylistModelManager().getTypeDescription());
    	if (addFilterDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		String newFilterName = addFilterDialog.getFilterName();
    		FilterIdentifier newId = Database.getPlaylistModelManager().getFilterIdentifier(newFilterName);
    		if (Database.getRecord(newId) != null) {
    			if (log.isDebugEnabled())
    				log.debug("add(): filter already exists with name=" + newFilterName);
    		} else {
    			try {
    				SubmittedDynamicPlaylist newPlaylist = new SubmittedDynamicPlaylist(newFilterName);
    				ArtistSearchParameters artistParameters = new ArtistSearchParameters((ArtistSearchParameters)((ArtistProxyModel)Database.getArtistModelManager().getProxyModel()).getSearchParameters());
    				LabelSearchParameters labelParameters = new LabelSearchParameters((LabelSearchParameters)((LabelProxyModel)Database.getLabelModelManager().getProxyModel()).getSearchParameters());
    				ReleaseSearchParameters releaseParameters = new ReleaseSearchParameters((ReleaseSearchParameters)((ReleaseProxyModel)Database.getReleaseModelManager().getProxyModel()).getSearchParameters());
    				SongSearchParameters songParameters = new SongSearchParameters((SongSearchParameters)((SongProxyModel)Database.getSongModelManager().getProxyModel()).getSearchParameters());
    				artistParameters.setSearchText("");
    				labelParameters.setSearchText("");
    				releaseParameters.setSearchText("");
    				songParameters.setSearchText("");
    				FilterSelection stylesSelection = StylesWidgetUI.instance.getFilterSelection();
    				artistParameters.setStylesSelection(stylesSelection);
    				labelParameters.setStylesSelection(stylesSelection);
    				releaseParameters.setStylesSelection(stylesSelection);
    				songParameters.setStylesSelection(stylesSelection);
    				FilterSelection tagsSelection = TagsWidgetUI.instance.getFilterSelection();
    				artistParameters.setTagsSelection(tagsSelection);
    				labelParameters.setTagsSelection(tagsSelection);
    				releaseParameters.setTagsSelection(tagsSelection);
    				songParameters.setTagsSelection(tagsSelection);
    				artistParameters.setPlaylistsSelection(null);
    				labelParameters.setPlaylistsSelection(null);
    				releaseParameters.setPlaylistsSelection(null);
    				songParameters.setPlaylistsSelection(null);
    				newPlaylist.setArtistSearchParameters(artistParameters);
    				newPlaylist.setLabelSearchParameters(labelParameters);
    				newPlaylist.setReleaseSearchParameters(releaseParameters);
    				newPlaylist.setSongSearchParameters(songParameters);
    				Database.getPlaylistIndex().add(newPlaylist).getRecord().update();
    			} catch (Exception e) {
    				log.error("saveAsPlaylist(): error", e);
    			}
    		}
    	}
    }

    public boolean toggleFilter() {
    	List<Integer> sizes = mainSplitter.sizes();
    	if (sizes.get(0) == 0) {
    		// expand it
    		mainSplitter.restorePosition();
    		return true;
    	} else {
    		// hide it
        	ArrayList<Integer> mainSplitterSizes = new ArrayList<Integer>();
            mainSplitterSizes.add(0);
            mainSplitterSizes.add(sizes.get(0) + sizes.get(1));
            mainSplitter.setSizes(mainSplitterSizes);
            return false;
    	}
    }

    public boolean areFiltersVisible() {
    	List<Integer> sizes = mainSplitter.sizes();
    	if (sizes.get(0) == 0)
    		return false;
    	return true;
    }

    protected void filterToolBoxChanged(Integer index) {
    	UIProperties.setProperty("filter_toolbox_index", String.valueOf(index));
    }

}
