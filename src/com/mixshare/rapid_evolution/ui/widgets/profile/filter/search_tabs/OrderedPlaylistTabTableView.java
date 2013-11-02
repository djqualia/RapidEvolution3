package com.mixshare.rapid_evolution.ui.widgets.profile.filter.search_tabs;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.record.filter.playlist.OrderedPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QDropEvent;

public class OrderedPlaylistTabTableView extends FilterSongTabTableView {

	static private Logger log = Logger.getLogger(OrderedPlaylistTabTableView.class);
	
	public OrderedPlaylistTabTableView(RecordTableModelManager modelManager) {
		super(modelManager);

		this.setDragEnabled(true);
		this.setDropIndicatorShown(true);
	}
	
	public void dropEvent(QDropEvent event) {				
		if (log.isTraceEnabled())
			log.trace("dropEvent(): event=" + event);
		Vector<SongRecord> songs = DragDropUtil.getSongs(event.mimeData());
		Vector<Integer> songIds = new Vector<Integer>(songs.size());
		for (SongRecord song : songs)
			songIds.add(song.getUniqueId());
		QModelIndex proxyIndex = indexAt(event.pos());
		QModelIndex sourceIndex = null;
		SongRecord song = null;
		int insertPosition = -1;
		OrderedPlaylistRecord playlist = (OrderedPlaylistRecord)((PlaylistProfile)ProfileWidgetUI.instance.getCurrentProfile()).getPlaylistRecord();
		if (proxyIndex != null) {
			sourceIndex = getProxyModel().mapToSource(proxyIndex);
			song = (SongRecord)getRecordTableModelManager().getRecordForRow(sourceIndex.row());
			insertPosition = playlist.getPositionOf(song.getUniqueId()) - 1;
		}
		if (event.source() == this) {
			// rearranging
			int shift = 0;
			boolean before = false;
			for (SongRecord removedSong : songs) {
				int removedIndex = playlist.getPositionOf(removedSong.getUniqueId());
				if (removedIndex < insertPosition) {
					++shift;
					before = true;
				}
			}
			for (SongRecord removedSong : songs)
				playlist.removeSong(removedSong.getUniqueId());
			insertPosition -= shift;
			if (before)
				++insertPosition;
			if (log.isDebugEnabled())
				log.debug("dropEvent(): re-arranging=" + songs + ", insertPosition=" + insertPosition);			
			if (insertPosition != -1) {
				playlist.insertSongs(songIds, insertPosition);
			} else {
				playlist.addSongRecords(songs);				
			}
			playlist.update();
		} else {
			if (log.isDebugEnabled())
				log.debug("dropEvent(): adding songs=" + songs + ", insertPosition=" + insertPosition);
			if (insertPosition != -1)
				playlist.insertSongs(songIds, insertPosition);
			else
				playlist.addSongRecords(songs);
			playlist.update();
		}
	}	
	
	
}
