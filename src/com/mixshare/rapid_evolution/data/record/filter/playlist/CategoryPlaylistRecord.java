package com.mixshare.rapid_evolution.data.record.filter.playlist;

import java.util.Map;

import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class CategoryPlaylistRecord extends PlaylistRecord {

    static private final long serialVersionUID = 0L;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public CategoryPlaylistRecord() { };
    public CategoryPlaylistRecord(PlaylistIdentifier playlistId, int uniqueId) {
    	super(playlistId, uniqueId);
    }
    public CategoryPlaylistRecord(PlaylistIdentifier playlistId, int uniqueId, boolean isRoot) {
    	super(playlistId, uniqueId, isRoot);
    }
    public CategoryPlaylistRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }

    ////////////
    // FIELDS //
    ////////////

    /////////////
    // GETTERS //
    /////////////

    /*
	public int getNumArtistRecords() {
		int total = 0;
		for (HierarchicalRecord child : getChildRecords()) {
    		PlaylistRecord childPlaylist = (PlaylistRecord)child;
    		if (childPlaylist != null)
    			total += childPlaylist.getNumArtistRecords();
    	}
		return total;
	}
	public int getNumLabelRecords() {
		int total = 0;
		for (HierarchicalRecord child : getChildRecords()) {
    		PlaylistRecord childPlaylist = (PlaylistRecord)child;
    		if (childPlaylist != null)
    			total += childPlaylist.getNumLabelRecords();
    	}
		return total;
	}
	public int getNumReleaseRecords() {
		int total = 0;
		for (HierarchicalRecord child : getChildRecords()) {
    		PlaylistRecord childPlaylist = (PlaylistRecord)child;
    		if (childPlaylist != null)
    			total += childPlaylist.getNumReleaseRecords();
    	}
		return total;
	}
	public int getNumSongRecords() {
		int total = 0;
		for (HierarchicalRecord child : getChildRecords()) {
    		PlaylistRecord childPlaylist = (PlaylistRecord)child;
    		if (childPlaylist != null)
    			total += childPlaylist.getNumSongRecords();
    	}
		return total;
	}
	public int getNumExternalArtistRecords() {
		int total = 0;
		for (HierarchicalRecord child : getChildRecords()) {
    		PlaylistRecord childPlaylist = (PlaylistRecord)child;
    		if (childPlaylist != null)
    			total += childPlaylist.getNumExternalArtistRecords();
    	}
		return total;
	}
	public int getNumExternalLabelRecords() {
		int total = 0;
		for (HierarchicalRecord child : getChildRecords()) {
    		PlaylistRecord childPlaylist = (PlaylistRecord)child;
    		if (childPlaylist != null)
    			total += childPlaylist.getNumExternalLabelRecords();
    	}
		return total;
	}
	public int getNumExternalReleaseRecords() {
		int total = 0;
		for (HierarchicalRecord child : getChildRecords()) {
    		PlaylistRecord childPlaylist = (PlaylistRecord)child;
    		if (childPlaylist != null)
    			total += childPlaylist.getNumExternalReleaseRecords();
    	}
		return total;
	}
	public int getNumExternalSongRecords() {
		int total = 0;
		for (HierarchicalRecord child : getChildRecords()) {
    		PlaylistRecord childPlaylist = (PlaylistRecord)child;
    		if (childPlaylist != null)
    			total += childPlaylist.getNumExternalSongRecords();
    	}
		return total;
	}
	*/

	/////////////
	// SETTERS //
	/////////////

    @Override
	public void addSong(int songId) { }
    @Override
	public void addLabel(int labelId) { }
    @Override
	public void addRelease(int releaseId) { }
    @Override
	public void addArtist(int artistId) { }

    @Override
	public void removeSong(int songId) { }
    @Override
	public void removeLabel(int labelId) { }
    @Override
	public void removeRelease(int releaseId) { }
    @Override
	public void removeArtist(int artistId) { }

	/////////////
	// METHODS //
	/////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);

    	OrderedPlaylistRecord playlist = (OrderedPlaylistRecord)record;
    	for (int songId : playlist.getSongIds())
    		addSong(songId);

    }

    @Override
	public boolean matches(SearchRecord searchRecord) {
    	if (getChildRecords().length == 0)
    		return true;
    	for (HierarchicalRecord child : getChildRecords()) {
    		PlaylistRecord childPlaylist = (PlaylistRecord)child;
    		if (childPlaylist.matches(searchRecord))
    			return true;
    	}
    	return false;
    }

    @Override
	public void write(LineWriter writer) {
    	writer.writeLine("1", "CategoryPlaylistRecord.type"); // type
    	super.write(writer);
    	writer.writeLine("1", "CategoryPlaylistRecord.version"); // version
    }

}
