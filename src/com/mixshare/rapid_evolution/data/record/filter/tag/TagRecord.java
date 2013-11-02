package com.mixshare.rapid_evolution.data.record.filter.tag;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
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

public class TagRecord extends FilterRecord {

    static private Logger log = Logger.getLogger(TagRecord.class);

    static private final long serialVersionUID = 0L;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public TagRecord() { };
    public TagRecord(TagIdentifier tagId, int uniqueId) {
    	this.id = tagId;
    	this.uniqueId = uniqueId;
    }
    public TagRecord(TagIdentifier tagId, int uniqueId, boolean isRoot) {
    	this.id = tagId;
    	this.uniqueId = uniqueId;
    	this.isRoot = isRoot;
    }
    public TagRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	categoryOnly = Boolean.parseBoolean(lineReader.getNextLine());
    }

    ////////////
    // FIELDS //
    ////////////

    private boolean categoryOnly; // categories are not computed in style similarity

    transient private boolean needsUpdate; // added for performance, update() method on tags was taking too long when the tree got big...

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(TagRecord.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("needsUpdate")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    /////////////
    // GETTERS //
    /////////////

	public boolean isCategoryOnly() { return categoryOnly; }

    @Override
	public byte getDataType() { return DATA_TYPE_TAGS; }

    @Override
	public Index getIndex() { return Database.getTagIndex(); }

    public TagIdentifier getTagIdentifier() { return (TagIdentifier)id; }
	public String getTagName() { return (getTagIdentifier() != null) ? getTagIdentifier().getName() : ""; }

    public ModelManagerInterface getModelManager() { return Database.getTagModelManager(); }

    @Override
	public HierarchicalIndex getHierarchicalIndex() { return Database.getTagIndex(); }

	public boolean needsUpdate() { return needsUpdate; }

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
			Vector<DegreeValue> tagDegrees = searchRecord.getSourceTagDegreeValues();
			tagDegrees.add(new DegreeValue(getTagName(), 1.0f, DATA_SOURCE_USER));
			searchRecord.setTags(tagDegrees);
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
				searchProfile.removeTag(getTagName());
		}
	}

	public void setNeedsUpdate(boolean needsUpdate) { this.needsUpdate = needsUpdate; }

	/////////////
	// METHODS //
	/////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);
    	TagRecord tagRecord = (TagRecord)record;
    	// update related artists (whose actual tags might have changed)
    	for (SearchResult searchRecord : getArtistRecords())
    		recordsToRefresh.put(searchRecord.getRecord(), null);
    	// update related labels (whose actual tags might have changed)
    	for (SearchResult searchRecord : getLabelRecords())
    		recordsToRefresh.put(searchRecord.getRecord(), null);
    	// update related releases (whose actual tags might have changed)
    	for (SearchResult searchRecord : getReleaseRecords())
    		recordsToRefresh.put(searchRecord.getRecord(), null);
    	// update related songs (whose actual tags might have changed)
    	for (SearchResult searchRecord : getSongRecords())
    		recordsToRefresh.put(searchRecord.getRecord(), null);
    }

    public void addSearchTokensForId(Identifier id, Vector<String> result) {
    	TagIdentifier tagId = (TagIdentifier)id;
    	result.add(SearchEncoder.encodeString(tagId.getName()));
    }

    @Override
	public boolean matches(SearchRecord searchRecord) {
    	if (searchRecord == null)
    		return false;
    	if (searchRecord.containsActualTag(uniqueId))
    		return true;
    	if (duplicateIds != null) {
    		for (int dupId : duplicateIds)
    			if (searchRecord.containsActualTag(dupId))
    				return true;
    	}
    	return false;
    }

    @Override
	public void update() {
    	super.update();
    	needsUpdate = false;
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
    	document.add(new Field("name", getTagName(), Field.Store.NO, Field.Index.ANALYZED));
    }

}
