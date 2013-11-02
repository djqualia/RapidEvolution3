package com.mixshare.rapid_evolution.data.record.filter;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class FilterRecord extends AbstractFilterRecord {

    static private Logger log = Logger.getLogger(FilterRecord.class);

	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////

	abstract public boolean matches(SearchRecord searchRecord);

	abstract public void addArtistRecords(Vector<ArtistRecord> artistRecords);
	abstract public void addLabelRecords(Vector<LabelRecord> labelRecords);
	abstract public void addReleaseRecords(Vector<ReleaseRecord> releaseRecords);
	abstract public void addSongRecords(Vector<SongRecord> songRecords);

	abstract public void removeArtistRecords(Vector<ArtistRecord> artistRecords);
	abstract public void removeLabelRecords(Vector<LabelRecord> labelRecords);
	abstract public void removeReleaseRecords(Vector<ReleaseRecord> releaseRecords);
	abstract public void removeSongRecords(Vector<SongRecord> songRecords);

	/////////////////
	// CONSTRUCTOR //
	/////////////////

	public FilterRecord() { super(); }
	public FilterRecord(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
		numArtistRecordsCached = Integer.parseInt(lineReader.getNextLine());
		numLabelRecordsCached = Integer.parseInt(lineReader.getNextLine());
		numReleaseRecordsCached = Integer.parseInt(lineReader.getNextLine());
		numSongRecordsCached = Integer.parseInt(lineReader.getNextLine());
		numExternalArtistRecordsCached = Integer.parseInt(lineReader.getNextLine());
		numExternalLabelRecordsCached = Integer.parseInt(lineReader.getNextLine());
		numExternalReleaseRecordsCached = Integer.parseInt(lineReader.getNextLine());
		numExternalSongRecordsCached = Integer.parseInt(lineReader.getNextLine());
	}

	////////////
	// FIELDS //
	////////////

	protected int numArtistRecordsCached = -1;
	protected int numLabelRecordsCached = -1;
	protected int numReleaseRecordsCached = -1;
	protected int numSongRecordsCached = -1;

	protected int numExternalArtistRecordsCached = -1;
	protected int numExternalLabelRecordsCached = -1;
	protected int numExternalReleaseRecordsCached = -1;
	protected int numExternalSongRecordsCached = -1;

	private boolean isNumArtistsCachedValid = false;
	private boolean isNumLabelsCachedValid = false;
	private boolean isNumReleasesCachedValid = false;
	private boolean isNumSongsCachedValid = false;

	private boolean isNumExternalArtistsCachedValid = false;
	private boolean isNumExternalLabelsCachedValid = false;
	private boolean isNumExternalReleasesCachedValid = false;
	private boolean isNumExternalSongsCachedValid = false;

	/////////////
	// GETTERS //
	/////////////

	@Override
	public int getNumArtistRecordsCached() {
		return numArtistRecordsCached;
	}
	@Override
	public int getNumLabelRecordsCached() {
		return numLabelRecordsCached;
	}
	@Override
	public int getNumReleaseRecordsCached() {
		return numReleaseRecordsCached;
	}
	@Override
	public int getNumSongRecordsCached() {
		return numSongRecordsCached;
	}
	public int getNumExternalArtistRecordsCached() {
		return numExternalArtistRecordsCached;
	}
	public int getNumExternalLabelRecordsCached() {
		return numExternalLabelRecordsCached;
	}
	public int getNumExternalReleaseRecordsCached() {
		return numExternalReleaseRecordsCached;
	}
	public int getNumExternalSongRecordsCached() {
		return numExternalSongRecordsCached;
	}

	/////////////
	// SETTERS //
	/////////////

	public void setNumArtistRecordsCached(int numArtistRecordsCached) { this.numArtistRecordsCached = numArtistRecordsCached; }
	public void setNumLabelRecordsCached(int numLabelRecordsCached) { this.numLabelRecordsCached = numLabelRecordsCached; }
	public void setNumReleaseRecordsCached(int numReleaseRecordsCached) { this.numReleaseRecordsCached = numReleaseRecordsCached; }
	public void setNumSongRecordsCached(int numSongRecordsCached) { this.numSongRecordsCached = numSongRecordsCached; }
	public void setNumExternalArtistRecordsCached(int numExternalArtistRecordsCached) { this.numExternalArtistRecordsCached = numExternalArtistRecordsCached; }
	public void setNumExternalLabelRecordsCached(int numExternalLabelRecordsCached) { this.numExternalLabelRecordsCached = numExternalLabelRecordsCached; }
	public void setNumExternalReleaseRecordsCached(int numExternalReleaseRecordsCached) { this.numExternalReleaseRecordsCached = numExternalReleaseRecordsCached; }
	public void setNumExternalSongRecordsCached(int numExternalSongRecordsCached) { this.numExternalSongRecordsCached = numExternalSongRecordsCached; }

	public void resetCachedRecordCount() { resetCachedRecordCount(false); }
	public void resetCachedRecordCount(boolean hard) {
		isNumArtistsCachedValid = false;
		isNumLabelsCachedValid = false;
		isNumReleasesCachedValid = false;
		isNumSongsCachedValid = false;
		isNumExternalArtistsCachedValid = false;
		isNumExternalLabelsCachedValid = false;
		isNumExternalReleasesCachedValid = false;
		isNumExternalSongsCachedValid = false;
		if (hard) {
			numArtistRecordsCached = -1;
			numLabelRecordsCached = -1;
			numReleaseRecordsCached = -1;
			numSongRecordsCached = -1;
			numExternalArtistRecordsCached = -1;
			numExternalLabelRecordsCached = -1;
			numExternalReleaseRecordsCached = -1;
			numExternalSongRecordsCached = -1;
		}
	}

	/////////////
	// METHODS //
	/////////////

	/**
	 * Returns true if the current filter (for example, a style), does not match any
	 * search object in the database (i.e. artist, label, release, song).  This can be used
	 * during the update of relational elements to determine if the filter can be removed safely...
	 */
	public boolean isOrphaned() {
		for (SearchIndex searchIndex : Database.getSearchIndexes()) {
			if (searchIndex.getSearchRecords(this).size() != 0)
				return false;
		}
		return true;
	}

	// artists
	public int computeNumArtistRecords() {
		numArtistRecordsCached = Database.getArtistIndex().getInternalSearchRecordsCount(this);
		isNumArtistsCachedValid = true;
		return numArtistRecordsCached;
	}
	public int computeNumExternalArtistRecords() {
		numExternalArtistRecordsCached = Database.getArtistIndex().getExternalSearchRecordsCount(this);
		isNumExternalArtistsCachedValid = true;
		return numExternalArtistRecordsCached;
	}
	@Override
	public int getNumArtistRecords() {
		if (!isNumArtistsCachedValid || (numArtistRecordsCached == -1))
			computeNumArtistRecords();
		return numArtistRecordsCached;
	}
	@Override
	public int getNumExternalArtistRecords() {
		if (!isNumExternalArtistsCachedValid || (numExternalArtistRecordsCached == -1))
			computeNumExternalArtistRecords();
		return numExternalArtistRecordsCached;
	}
	public Vector<SearchResult> getArtistRecords() { return Database.getArtistIndex().getSearchRecords(this); }
	public Vector<SearchResult> getInternalArtistRecords() { return Database.getArtistIndex().getInternalSearchRecords(this); }
	public int getInternalArtistRecordsCount() { return Database.getArtistIndex().getInternalSearchRecordsCount(this); }
	public Vector<SearchResult> getExternalArtistRecords() { return Database.getArtistIndex().getExternalSearchRecords(this); }

	// labels
	public int computeNumLabelRecords() {
		numLabelRecordsCached = Database.getLabelIndex().getInternalSearchRecordsCount(this);
		isNumLabelsCachedValid = true;
		return numLabelRecordsCached;
	}
	public int computeNumExternalLabelRecords() {
		numExternalLabelRecordsCached = Database.getLabelIndex().getExternalSearchRecordsCount(this);
		isNumExternalLabelsCachedValid = true;
		return numExternalLabelRecordsCached;
	}
	@Override
	public int getNumLabelRecords() {
		if (!isNumLabelsCachedValid || (numLabelRecordsCached == -1))
			computeNumLabelRecords();
		return numLabelRecordsCached;
	}
	@Override
	public int getNumExternalLabelRecords() {
		if (!isNumExternalLabelsCachedValid || (numExternalLabelRecordsCached == -1))
			computeNumExternalLabelRecords();
		return numExternalLabelRecordsCached;
	}
	public Vector<SearchResult> getLabelRecords() { return Database.getLabelIndex().getSearchRecords(this); }
	public Vector<SearchResult> getInternalLabelRecords() { return Database.getLabelIndex().getInternalSearchRecords(this); }
	public int getInternalLabelRecordsCount() { return Database.getLabelIndex().getInternalSearchRecordsCount(this); }
	public Vector<SearchResult> getExternalLabelRecords() { return Database.getLabelIndex().getExternalSearchRecords(this); }

	// releases
	public int computeNumReleaseRecords() {
		numReleaseRecordsCached = Database.getReleaseIndex().getInternalSearchRecordsCount(this);
		isNumReleasesCachedValid = true;
		return numReleaseRecordsCached;
	}
	public int computeNumExternalReleaseRecords() {
		numExternalReleaseRecordsCached = Database.getReleaseIndex().getExternalSearchRecordsCount(this);
		isNumExternalReleasesCachedValid = true;
		return numExternalReleaseRecordsCached;
	}
	@Override
	public int getNumReleaseRecords() {
		if (!isNumReleasesCachedValid || (numReleaseRecordsCached == -1))
			computeNumReleaseRecords();
		return numReleaseRecordsCached;
	}
	@Override
	public int getNumExternalReleaseRecords() {
		if (!isNumExternalReleasesCachedValid || (numExternalReleaseRecordsCached == -1))
			computeNumExternalReleaseRecords();
		return numExternalReleaseRecordsCached;
	}
	public Vector<SearchResult> getReleaseRecords() { return Database.getReleaseIndex().getSearchRecords(this); }
	public Vector<SearchResult> getInternalReleaseRecords() { return Database.getReleaseIndex().getInternalSearchRecords(this); }
	public int getInternalReleaseRecordsCount() { return Database.getReleaseIndex().getInternalSearchRecordsCount(this); }
	public Vector<SearchResult> getExternalReleaseRecords() { return Database.getReleaseIndex().getExternalSearchRecords(this); }

	// songs
	public int computeNumSongRecords() {
		numSongRecordsCached = Database.getSongIndex().getInternalSearchRecordsCount(this);
		isNumSongsCachedValid = true;
		return numSongRecordsCached;
	}
	public int computeNumExternalSongRecords() {
		numExternalSongRecordsCached = Database.getSongIndex().getExternalSearchRecordsCount(this);
		isNumExternalSongsCachedValid = true;
		return numExternalSongRecordsCached;
	}
	@Override
	public int getNumSongRecords() {
		if (!isNumSongsCachedValid || (numSongRecordsCached == -1))
			computeNumSongRecords();
		return numSongRecordsCached;
	}
	@Override
	public int getNumExternalSongRecords() {
		if (!isNumExternalSongsCachedValid || (numExternalSongRecordsCached == -1))
			computeNumExternalSongRecords();
		return numExternalSongRecordsCached;
	}
	public Vector<SearchResult> getSongRecords() { return Database.getSongIndex().getSearchRecords(this); }
	public Vector<SearchResult> getInternalSongRecords() { return Database.getSongIndex().getInternalSearchRecords(this); }
	public int getInternalSongRecordsCount() { return Database.getSongIndex().getInternalSearchRecordsCount(this); }
	public Vector<SearchResult> getExternalSongRecords() { return Database.getSongIndex().getExternalSearchRecords(this); }

    @Override
	public void write(LineWriter textWriter) {
    	super.write(textWriter);
    	textWriter.writeLine(1); //version
    	textWriter.writeLine(numArtistRecordsCached);
    	textWriter.writeLine(numLabelRecordsCached);
    	textWriter.writeLine(numReleaseRecordsCached);
    	textWriter.writeLine(numSongRecordsCached);
    	textWriter.writeLine(numExternalArtistRecordsCached);
    	textWriter.writeLine(numExternalLabelRecordsCached);
    	textWriter.writeLine(numExternalReleaseRecordsCached);
    	textWriter.writeLine(numExternalSongRecordsCached);
    }

    ////////////////////
    // STATIC METHODS //
    ////////////////////

    static public FilterRecord readFilterRecord(LineReader lineReader) {
    	int type = Integer.parseInt(lineReader.getNextLine());
    	if (type == 1)
    		return new StyleRecord(lineReader);
    	else if (type == 2)
    		return new TagRecord(lineReader);
    	else if (type == 3)
    		return PlaylistRecord.readPlaylistRecord(lineReader);
    	return null;
    }

    static public void writeFilterRecord(FilterRecord record, LineWriter writer) {
    	if (record instanceof StyleRecord)
    		writer.writeLine(1);
    	else if (record instanceof TagRecord)
    		writer.writeLine(2);
    	else if (record instanceof PlaylistRecord)
    		writer.writeLine(3);
    	else
    		log.warn("writeFilterRecord(): unknown filter record type=" + record.getClass());
    	record.write(writer);
    }

}
