package com.mixshare.rapid_evolution.data.record.filter.playlist;

import java.util.Map;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class OrderedPlaylistRecord extends PlaylistRecord {

    static private final long serialVersionUID = 0L;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public OrderedPlaylistRecord() { };
    public OrderedPlaylistRecord(PlaylistIdentifier playlistId, int uniqueId) {
    	super(playlistId, uniqueId);
    }
    public OrderedPlaylistRecord(PlaylistIdentifier playlistId, int uniqueId, boolean isRoot) {
    	super(playlistId, uniqueId, isRoot);
    }
    public OrderedPlaylistRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	int numSongs = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numSongs; ++i)
    		songIds.add(Integer.parseInt(lineReader.getNextLine()));
    }

    ////////////
    // FIELDS //
    ////////////

    private Vector<Integer> songIds = new Vector<Integer>();

    /////////////
    // GETTERS //
    /////////////

    public int getPositionOf(int songId) {
    	int i = 1;
    	for (int id : songIds) {
    		if (id == songId)
    			return i;
    		++i;
    	}
    	return 0;
    }

    @Override
    public Vector<SearchResult> getSongRecords() {
    	Vector<SearchResult> result = new Vector<SearchResult>(songIds.size());
    	for (int songId : songIds) {
    		SearchRecord song = Database.getSongIndex().getSongRecord(songId);
    		if (song != null)
    			result.add(new SearchResult(song, 1.0f));
    	}
    	return result;
	}

    // for serialization
	public Vector<Integer> getSongIds() { return songIds; }

	@Override
	public int getNumArtistRecords() { return 0; }
	@Override
	public int getNumArtistRecordsCached() { return 0; }
	@Override
	public int getNumLabelRecords() { return 0; }
	@Override
	public int getNumLabelRecordsCached() { return 0; }
	@Override
	public int getNumReleaseRecords() { return 0; }
	@Override
	public int getNumReleaseRecordsCached() { return 0; }
	@Override
	public int getNumSongRecords() { return songIds.size(); }
	@Override
	public int getNumSongRecordsCached() { return getNumSongRecords(); }
	@Override
	public int getNumExternalArtistRecords() { return 0; }
	@Override
	public int getNumExternalLabelRecords() { return 0; }
	@Override
	public int getNumExternalReleaseRecords() { return 0; }
	@Override
	public int getNumExternalSongRecords() { return 0; }

	/////////////
	// SETTERS //
	/////////////

	public void insertSongs(Vector<Integer> songIds, int insertIndex) {
		for (int songId : songIds) {
			insertSong(songId, insertIndex++);
		}
	}
	public void setSongIds(Vector<Integer> songIds) { this.songIds = songIds; }

	@Override
	public void addArtist(int songId) { }
	@Override
	public void addLabel(int songId) { }
	@Override
	public void addRelease(int songId) { }
	@Override
	public void addSong(int songId) {
		if (!songIds.contains(songId))
			songIds.add(songId);
	}
	public void insertSong(int songId, int index) {
		if (!songIds.contains(songId))
			songIds.insertElementAt(songId, index);
	}

	@Override
	public void removeArtist(int songId) { }
	@Override
	public void removeLabel(int songId) { }
	@Override
	public void removeRelease(int songId) { }
	@Override
	public void removeSong(int songId) {
		while (songIds.contains(songId))
			songIds.removeElement(songId);
	}

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
    	if (searchRecord instanceof ArtistRecord) {
    		return false;
    	}  else if (searchRecord instanceof LabelRecord) {
    		return false;
    	} else if (searchRecord instanceof ReleaseRecord) {
    		return false;
    	} else if (searchRecord instanceof SongRecord) {
    		return (songIds.contains(searchRecord.getUniqueId()));
    	}
    	return false;
    }

    @Override
	public void write(LineWriter textWriter) {
    	textWriter.writeLine(3); // type
    	super.write(textWriter);
    	textWriter.writeLine(1); //version
    	textWriter.writeLine(songIds.size());
    	for (Integer value : songIds)
    		textWriter.writeLine(value);
    }


}

