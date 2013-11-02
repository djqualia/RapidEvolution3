package com.mixshare.rapid_evolution.data.record.filter.playlist;

import java.util.Map;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.index.HierarchicalIndex;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchEncoder;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class PlaylistRecord extends FilterRecord {

    static private final long serialVersionUID = 0L;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public PlaylistRecord() { };
    public PlaylistRecord(PlaylistIdentifier playlistId, int uniqueId) {
    	this.id = playlistId;
    	this.uniqueId = uniqueId;
    }
    public PlaylistRecord(PlaylistIdentifier playlistId, int uniqueId, boolean isRoot) {
    	this.id = playlistId;
    	this.uniqueId = uniqueId;
    	this.isRoot = isRoot;
    }
    public PlaylistRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }

    ////////////
    // FIELDS //
    ////////////

    /////////////
    // GETTERS //
    /////////////

    @Override
	public byte getDataType() { return DATA_TYPE_PLAYLISTS; }

    @Override
	public Index getIndex() { return Database.getPlaylistIndex(); }

    public PlaylistIdentifier getPlaylistIdentifier() { return (PlaylistIdentifier)id; }
	public String getPlaylistName() { return (getPlaylistIdentifier() != null) ? getPlaylistIdentifier().getName() : ""; }

    public ModelManagerInterface getModelManager() { return Database.getPlaylistModelManager(); }

    @Override
	public HierarchicalIndex getHierarchicalIndex() { return Database.getPlaylistIndex(); }

	/////////////
	// SETTERS //
	/////////////

	@Override
	public void addArtistRecords(Vector<ArtistRecord> artistRecords) {
		for (ArtistRecord artistRecord : artistRecords)
			addArtist(artistRecord.getUniqueId());
	}
	@Override
	public void addLabelRecords(Vector<LabelRecord> labelRecords) {
		for (LabelRecord labelRecord : labelRecords)
			addLabel(labelRecord.getUniqueId());
	}
	@Override
	public void addReleaseRecords(Vector<ReleaseRecord> releaseRecords) {
		for (ReleaseRecord releaseRecord : releaseRecords)
			addRelease(releaseRecord.getUniqueId());
	}
	@Override
	public void addSongRecords(Vector<SongRecord> songRecords) {
		for (SongRecord songRecord : songRecords)
			addSong(songRecord.getUniqueId());
	}

	@Override
	public void removeArtistRecords(Vector<ArtistRecord> artistRecords) {
		for (ArtistRecord artistRecord : artistRecords)
			removeArtist(artistRecord.getUniqueId());
	}
	@Override
	public void removeLabelRecords(Vector<LabelRecord> labelRecords) {
		for (LabelRecord labelRecord : labelRecords)
			removeLabel(labelRecord.getUniqueId());
	}
	@Override
	public void removeReleaseRecords(Vector<ReleaseRecord> releaseRecords) {
		for (ReleaseRecord releaseRecord : releaseRecords)
			removeRelease(releaseRecord.getUniqueId());
	}
	@Override
	public void removeSongRecords(Vector<SongRecord> songRecords) {
		for (SongRecord songRecord : songRecords)
			removeSong(songRecord.getUniqueId());
	}

    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    abstract public void addSong(int songId);
    abstract public void addLabel(int labelId);
    abstract public void addRelease(int releaseId);
    abstract public void addArtist(int artistId);

    abstract public void removeSong(int songId);
    abstract public void removeLabel(int labelId);
    abstract public void removeRelease(int releaseId);
    abstract public void removeArtist(int artistId);

	/////////////
	// METHODS //
	/////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);

    }

    public void addSearchTokensForId(Identifier id, Vector<String> result) {
    	PlaylistIdentifier playlistId = (PlaylistIdentifier)id;
    	result.add(SearchEncoder.encodeString(playlistId.getName()));
    }


    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1", "PlaylistRecord.version"); // version
    }

    @Override
	public void addFieldsToDocument(Document document) {
    	super.addFieldsToDocument(document);
    	document.add(new Field("name", getPlaylistName(), Field.Store.NO, Field.Index.ANALYZED));
    }

    ////////////////////
    // STATIC METHODS //
    ////////////////////

    static public PlaylistRecord readPlaylistRecord(LineReader lineReader) {
    	int type = Integer.parseInt(lineReader.getNextLine());
    	if (type == 1)
    		return new CategoryPlaylistRecord(lineReader);
    	else if (type == 2)
    		return new DynamicPlaylistRecord(lineReader);
    	else if (type == 3)
    		return new OrderedPlaylistRecord(lineReader);
    	return null;
    }

}
