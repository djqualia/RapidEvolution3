package com.mixshare.rapid_evolution.ui.updaters.view.filter;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.widgets.CentralWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.playlists.PlaylistsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;

public class SelectAndFocusPlaylist extends Thread {

	static private Logger log = Logger.getLogger(SelectAndFocusPlaylist.class);
	
    private PlaylistRecord playlistRecord;
    
	public SelectAndFocusPlaylist(PlaylistRecord playlistRecord) {
		this.playlistRecord = playlistRecord;
	}
	
	public void run() {
		try {			
			SearchWidgetUI.instance.setSongSearchType();
			if (!CentralWidgetUI.instance.areFiltersVisible()) {
				CentralWidgetUI.instance.toggleFilter();
				SearchWidgetUI.instance.setFilterButtonState(true);
			}
			CentralWidgetUI.instance.setPlaylistsSelected();
			PlaylistsWidgetUI.instance.getFilterTreeView().setTargetSelectionState(FilterHierarchyInstance.SELECTION_STATE_OR);
			PlaylistsWidgetUI.instance.selectPlaylist(playlistRecord);
			PlaylistsWidgetUI.instance.getFilterTreeView().ensureFilterIsVisible(playlistRecord);
			SearchWidgetUI.instance.clearSearchText();
			//PlaylistsWidgetUI.instance.getFilterTreeView().selectionChanged(); including this line caused the selection to fail sporadically
		} catch (Exception e) {
			log.error("run(): error", e);
		}
	}	
	
}
