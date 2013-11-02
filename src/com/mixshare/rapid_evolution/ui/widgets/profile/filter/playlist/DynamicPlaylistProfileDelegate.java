package com.mixshare.rapid_evolution.ui.widgets.profile.filter.playlist;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.DynamicPlaylistProfile;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.PlaylistArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.PlaylistArtistTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.PlaylistDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.PlaylistLabelModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.PlaylistLabelTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.PlaylistReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.PlaylistReleaseTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.PlaylistSongModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.PlaylistSongTabTableWidget;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.filter.playlist.PlaylistDetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.FilterProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;
import com.trolltech.qt.gui.QTabWidget;

public class DynamicPlaylistProfileDelegate extends FilterProfileDelegate {

	////////////
	// FIELDS //
	////////////
	
	private DynamicPlaylistProfile playlistProfile;
	
	private PlaylistSongModelManager playlistSongsModel;
	private RecordTabTableWidget songsWidget;
	
	private PlaylistArtistModelManager playlistArtistsModel;
	private RecordTabTableWidget artistsWidget;

	private PlaylistLabelModelManager playlistLabelsModel;
	private RecordTabTableWidget labelsWidget;
	
	private PlaylistReleaseModelManager playlistReleasesModel;
	private RecordTabTableWidget releasesWidget;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DynamicPlaylistProfileDelegate(QTabWidget itemDetailTabsWidget, DynamicPlaylistProfile playlistProfile) {
		super(itemDetailTabsWidget);
		this.playlistProfile = playlistProfile;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public String getTitleText() {
		return playlistProfile.getPlaylistName();
	}
		
	public DetailsWidgetUI getDetailsWidget() {
		PlaylistDetailsModelManager playlistDetailsModel = (PlaylistDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(PlaylistDetailsModelManager.class);
		playlistDetailsModel.setRelativeProfile(playlistProfile);
		return new PlaylistDetailsWidgetUI(playlistDetailsModel);		
	}

	
	public String getTabIndexTitle() {
		return UIProperties.getProperty("playlist_tab_index");
	}	
	
	public Vector<Tab> getTabs() {
		Vector<Tab> result = super.getTabs();		

		// artists
		playlistArtistsModel = (PlaylistArtistModelManager)Database.getRelativeModelFactory().getRelativeModelManager(PlaylistArtistModelManager.class);
		playlistArtistsModel.setRelativePlaylist(playlistProfile);
		artistsWidget = new PlaylistArtistTabTableWidget(playlistArtistsModel, COLUMN_ARTIST_DESCRIPTION);
		result.add(new Tab(Translations.get("tab_title_artists"), artistsWidget));		

		// labels
		playlistLabelsModel = (PlaylistLabelModelManager)Database.getRelativeModelFactory().getRelativeModelManager(PlaylistLabelModelManager.class);
		playlistLabelsModel.setRelativePlaylist(playlistProfile);
		labelsWidget = new PlaylistLabelTabTableWidget(playlistLabelsModel, COLUMN_LABEL_NAME);
		result.add(new Tab(Translations.get("tab_title_labels"), labelsWidget));		

		// releases
		playlistReleasesModel = (PlaylistReleaseModelManager)Database.getRelativeModelFactory().getRelativeModelManager(PlaylistReleaseModelManager.class);
		playlistReleasesModel.setRelativePlaylist(playlistProfile);
		releasesWidget = new PlaylistReleaseTabTableWidget(playlistReleasesModel, COLUMN_RELEASE_DESCRIPTION);
		result.add(new Tab(Translations.get("tab_title_releases"), releasesWidget));		
		
		// songs
		playlistSongsModel = (PlaylistSongModelManager)Database.getRelativeModelFactory().getRelativeModelManager(PlaylistSongModelManager.class);
		playlistSongsModel.setRelativePlaylist(playlistProfile);
		songsWidget = new PlaylistSongTabTableWidget(playlistSongsModel, COLUMN_SONG_DESCRIPTION);
		result.add(new Tab(Translations.get("tab_title_songs"), songsWidget));		
		
		return result;
	}	
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setTabIndexTitle(String tabTitle) {
		UIProperties.setProperty("playlist_tab_index", tabTitle);
	}

	/////////////
	// METHODS //
	/////////////
	
	public void refresh() {
		super.refresh();	
		
		// songs
		if (songsWidget.isLoaded()) {
			playlistSongsModel.reset();
			songsWidget.updateFilter();
		}

		// artists
		if (artistsWidget.isLoaded()) {
			playlistArtistsModel.reset();
			songsWidget.updateFilter();
		}

		// labels
		if (labelsWidget.isLoaded()) {
			playlistLabelsModel.reset();
			labelsWidget.updateFilter();
		}

		// releases
		if (releasesWidget.isLoaded()) {
			playlistReleasesModel.reset();
			releasesWidget.updateFilter();
		}
		
	}
	
}
