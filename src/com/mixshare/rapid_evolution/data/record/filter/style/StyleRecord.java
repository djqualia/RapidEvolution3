package com.mixshare.rapid_evolution.data.record.filter.style;

import java.util.Map;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.index.HierarchicalIndex;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchEncoder;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class StyleRecord extends FilterRecord {

    static private final long serialVersionUID = 0L;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public StyleRecord() { };
    public StyleRecord(StyleIdentifier styleId, int uniqueId) {
    	this.id = styleId;
    	this.uniqueId = uniqueId;
    }
    public StyleRecord(StyleIdentifier styleId, int uniqueId, boolean isRoot) {
    	this.id = styleId;
    	this.uniqueId = uniqueId;
    	this.isRoot = isRoot;
    }
    public StyleRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	categoryOnly = Boolean.parseBoolean(lineReader.getNextLine());
    }

    ////////////
    // FIELDS //
    ////////////

    private boolean categoryOnly; // categories are not computed in style similarity

    /////////////
    // GETTERS //
    /////////////

	public boolean isCategoryOnly() { return categoryOnly; }

    @Override
	public byte getDataType() { return DATA_TYPE_STYLES; }

    @Override
	public Index getIndex() { return Database.getStyleIndex(); }

    public StyleIdentifier getStyleIdentifier() { return (StyleIdentifier)id; }
	public String getStyleName() { return (getStyleIdentifier() != null) ? getStyleIdentifier().getName() : ""; }

    public ModelManagerInterface getModelManager() { return Database.getStyleModelManager(); }

    @Override
	public HierarchicalIndex getHierarchicalIndex() { return Database.getStyleIndex(); }

	/////////////
	// SETTERS //
	/////////////

	public void setCategoryOnly(boolean categoryOnly) { this.categoryOnly = categoryOnly; }

	@Override
	public void addArtistRecords(Vector<ArtistRecord> artistRecords) {
		for (ArtistRecord artistRecord : artistRecords)
			addSearchRecord(artistRecord);
	}
	@Override
	public void addLabelRecords(Vector<LabelRecord> labelRecords) {
		for (LabelRecord labelRecord : labelRecords)
			addSearchRecord(labelRecord);
	}
	@Override
	public void addReleaseRecords(Vector<ReleaseRecord> releaseRecords) {
		for (ReleaseRecord releaseRecord : releaseRecords)
			addSearchRecord(releaseRecord);
	}
	@Override
	public void addSongRecords(Vector<SongRecord> songRecords) {
		for (SongRecord songRecord : songRecords)
			addSearchRecord(songRecord);
	}

	public void addSearchRecord(SearchRecord searchRecord) {
		if (!matches(searchRecord)) {
			Vector<DegreeValue> styleDegrees = searchRecord.getSourceStyleDegreeValues();
			styleDegrees.add(new DegreeValue(getStyleName(), 1.0f, DATA_SOURCE_USER));
			searchRecord.setStyles(styleDegrees);
		}
	}

	@Override
	public void removeArtistRecords(Vector<ArtistRecord> artistRecords) {
		for (ArtistRecord artistRecord : artistRecords)
			removeSearchRecord(artistRecord);
	}
	@Override
	public void removeLabelRecords(Vector<LabelRecord> labelRecords) {
		for (LabelRecord labelRecord : labelRecords)
			removeSearchRecord(labelRecord);
	}
	@Override
	public void removeReleaseRecords(Vector<ReleaseRecord> releaseRecords) {
		for (ReleaseRecord releaseRecord : releaseRecords)
			removeSearchRecord(releaseRecord);
	}
	@Override
	public void removeSongRecords(Vector<SongRecord> songRecords) {
		for (SongRecord songRecord : songRecords)
			removeSearchRecord(songRecord);
	}

	public void removeSearchRecord(SearchRecord searchRecord) {
		if (matches(searchRecord)) {
			SearchProfile searchProfile = (SearchProfile)Database.getProfile(searchRecord.getIdentifier());
			if (searchProfile != null)
				searchProfile.removeStyle(getStyleName());
		}
	}

	/////////////
	// METHODS //
	/////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);
    	StyleRecord styleRecord = (StyleRecord)record;
    	// update related artists (whose actual styles might have changed)
    	for (SearchResult searchRecord : getArtistRecords())
    		recordsToRefresh.put(searchRecord.getRecord(), null);
    	// update related labels (whose actual styles might have changed)
    	for (SearchResult searchRecord : getLabelRecords())
    		recordsToRefresh.put(searchRecord.getRecord(), null);
    	// update related releases (whose actual styles might have changed)
    	for (SearchResult searchRecord : getReleaseRecords())
    		recordsToRefresh.put(searchRecord.getRecord(), null);
    	// update related songs (whose actual styles might have changed)
    	for (SearchResult searchRecord : getSongRecords())
    		recordsToRefresh.put(searchRecord.getRecord(), null);
    }

    public void addSearchTokensForId(Identifier id, Vector<String> result) {
    	StyleIdentifier styleId = (StyleIdentifier)id;
    	result.add(SearchEncoder.encodeString(styleId.getName()));
    }

    @Override
	public boolean matches(SearchRecord searchRecord) {
    	if (searchRecord == null)
    		return false;
    	if (searchRecord.containsActualStyle(uniqueId))
    		return true;
    	if (duplicateIds != null) {
    		for (int dupId : duplicateIds)
    			if (searchRecord.containsActualStyle(dupId))
    				return true;
    	}
    	return false;
    }

    @Override
	public void write(LineWriter textWriter) {
    	super.write(textWriter);
    	textWriter.writeLine(1); //version
    	textWriter.writeLine(categoryOnly);
    }


    @Override
	public void addFieldsToDocument(Document document) {
    	super.addFieldsToDocument(document);
    	document.add(new Field("name", getStyleName(), Field.Store.NO, Field.Index.ANALYZED));
    }

}
