package com.mixshare.rapid_evolution.ui.widgets.profile.filter.playlist;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.OrderedPlaylistProfile;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.OrderedPlaylistSongModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.OrderedPlaylistSongTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.filter.playlist.PlaylistDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.filter.playlist.PlaylistDetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.FilterProfileDelegate;
import com.trolltech.qt.gui.QTabWidget;

public class OrderedPlaylistProfileDelegate extends FilterProfileDelegate {

	////////////
	// FIELDS //
	////////////
	
	private OrderedPlaylistProfile playlistProfile;
	
	private OrderedPlaylistSongModelManager playlistSongsModel;
	private OrderedPlaylistSongTabTableWidget songsWidget;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public OrderedPlaylistProfileDelegate(QTabWidget itemDetailTabsWidget, OrderedPlaylistProfile playlistProfile) {
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
		
		// songs
		playlistSongsModel = (OrderedPlaylistSongModelManager)Database.getRelativeModelFactory().getRelativeModelManager(OrderedPlaylistSongModelManager.class);
		playlistSongsModel.setRelativePlaylist(playlistProfile);
		songsWidget = new OrderedPlaylistSongTabTableWidget(playlistSongsModel, COLUMN_PLAYLIST_POSITION);
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

	}
	
}
