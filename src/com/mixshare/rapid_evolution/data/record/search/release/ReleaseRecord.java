package com.mixshare.rapid_evolution.data.record.search.release;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SongGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.search.label.SubmittedLabel;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class ReleaseRecord extends SongGroupRecord {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(ReleaseRecord.class);

    ////////////
    // FIELDS //
    ////////////

    private int[] labelIds;

    private short originalYearReleased;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed.
    		BeanInfo info = Introspector.getBeanInfo(ReleaseRecord.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			//if (pd.getName().equals("encodedReleaseTitleTokens") || pd.getName().equals("encodedArtistTokens") || pd.getName().equals("encodedLabelTokens")) {
    				//pd.setValue("transient", Boolean.TRUE);
    			//}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public ReleaseRecord() { };
    public ReleaseRecord(ReleaseIdentifier releaseId, int uniqueId) {
    	this.id = releaseId;
    	this.uniqueId = uniqueId;
    }
    public ReleaseRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	int numLabels = Integer.parseInt(lineReader.getNextLine());
    	labelIds = new int[numLabels];
    	for (int i = 0; i < numLabels; ++i)
    		labelIds[i] = Integer.parseInt(lineReader.getNextLine());
    	originalYearReleased = Short.parseShort(lineReader.getNextLine());
    }

    /////////////
    // GETTERS //
    /////////////

    @Override
	public byte getDataType() { return DATA_TYPE_RELEASES; }

    @Override
	public Index getIndex() { return Database.getReleaseIndex(); }

    public ReleaseIdentifier getReleaseIdentifier() { return (ReleaseIdentifier)id; }

    public boolean isCompilationRelease() { return (getReleaseIdentifier() != null) ? getReleaseIdentifier().getNumArtists() == 0 : false; }

    // artist info
    public int[] getArtistIds() { return (getReleaseIdentifier() != null) ? getReleaseIdentifier().getArtistIds() : null; }
	public Vector<ArtistRecord> getArtists() {
		Vector<ArtistRecord> result = new Vector<ArtistRecord>(getReleaseIdentifier() != null ? getReleaseIdentifier().getNumArtists() : 0);
		if (getReleaseIdentifier() != null) {
			for (int artistId : getReleaseIdentifier().getArtistIds()) {
				ArtistRecord artistRecord = (ArtistRecord)Database.getArtistIndex().getRecord(artistId);
				if (artistRecord != null)
					result.add(artistRecord);
			}
			java.util.Collections.sort(result);
		}
		return result;
	}
	public Vector<String> getArtistNames() {
		Vector<String> result = new Vector<String>(getReleaseIdentifier() != null ? getReleaseIdentifier().getNumArtists() : 0);
		if (getReleaseIdentifier() != null) {
			for (int artistId : getReleaseIdentifier().getArtistIds()) {
				ArtistRecord artistRecord = (ArtistRecord)Database.getArtistIndex().getRecord(artistId);
				if (artistRecord != null)
					result.add(artistRecord.getArtistName());
			}
			java.util.Collections.sort(result);
		}
		return result;
	}
	public String getArtistsDescription() {
		StringBuffer result = new StringBuffer();
		Vector<ArtistRecord> artists = getArtists();
		for (ArtistRecord artist : artists) {
			if (result.length() > 0)
				result.append("; ");
			result.append(artist.toString());
		}
		if (result.length() == 0)
			result.append(ReleaseIdentifier.releaseCompilationArtistDescription);
		return result.toString();
	}
	public String getDiscogsArtistsDescription() {
		StringBuffer result = new StringBuffer();
		Vector<ArtistRecord> artists = getArtists();
		for (ArtistRecord artist : artists) {
			if (result.length() > 0)
				result.append("; ");
			result.append(artist.getDiscogsArtistName());
		}
		if (result.length() == 0)
			result.append(ReleaseIdentifier.releaseCompilationArtistDescription);
		return result.toString();
	}
	public String getLastfmArtistsDescription() {
		StringBuffer result = new StringBuffer();
		Vector<ArtistRecord> artists = getArtists();
		for (ArtistRecord artist : artists) {
			if (result.length() > 0)
				result.append("; ");
			result.append(artist.getLastfmArtistName());
		}
		if (result.length() == 0)
			result.append(ReleaseIdentifier.releaseCompilationArtistDescription);
		return result.toString();
	}

	public String getReleaseTitle() { return (getReleaseIdentifier() != null) ? getReleaseIdentifier().getReleaseTitle() : ""; }

	public int getNumLabels() { return labelIds != null ? labelIds.length : 0; }
	public Vector<String> getSourceLabelNames() {
		Vector<String> result = new Vector<String>(labelIds != null ? labelIds.length : 0);
		if (labelIds != null) {
			for (int labelId : labelIds) {
				Identifier id = Database.getLabelIndex().getIdentifierFromUniqueId(labelId);
				if (id != null)
					result.add(id.toString());
			}
		}
		return result;
	}
	public String getLabelsDescription() {
		StringBuffer result = new StringBuffer();
		if (labelIds != null) {
			for (int labelId : labelIds) {
				if (result.length() > 0)
					result.append("; ");
				LabelRecord labelRecord = (LabelRecord)Database.getLabelIndex().getRecord(labelId);
				if (labelRecord != null)
					result.append(labelRecord.toString());
				else {
					Identifier identifier = Database.getLabelIndex().getIdentifierFromUniqueId(labelId);
					if (identifier != null)
						result.append(identifier.toString());
				}
			}
		}
		return result.toString();
	}
	public int[] getLabelIds() { return labelIds; }
	public Vector<LabelRecord> getLabels() {
		Vector<LabelRecord> result = new Vector<LabelRecord>(labelIds != null ? labelIds.length : 0);
		if (labelIds != null) {
			for (int labelId : labelIds) {
				LabelRecord labelRecord = (LabelRecord)Database.getLabelIndex().getRecord(labelId);
				if (labelRecord != null)
					result.add(labelRecord);
			}
		}
		java.util.Collections.sort(result);
		return result;
	}
	public Vector<String> getLabelNames() {
		Vector<String> result = new Vector<String>(labelIds != null ? labelIds.length : 0);
		if (labelIds != null) {
			for (int labelId : labelIds) {
				LabelRecord labelRecord = (LabelRecord)Database.getLabelIndex().getRecord(labelId);
				if (labelRecord != null)
					result.add(labelRecord.getLabelName());
			}
		}
		java.util.Collections.sort(result);
		return result;

	}

    public String getOriginalYearReleasedAsString() { return (originalYearReleased > 0) ? String.valueOf(originalYearReleased) : ""; }
    public short getOriginalYearReleased() { return originalYearReleased; }

    public ModelManagerInterface getModelManager() { return Database.getReleaseModelManager(); }

    @Override
	public Vector<SongRecord> getSongs() {
    	Vector<SongRecord> result = new Vector<SongRecord>();
    	ReleaseProfile releaseProfile = Database.getReleaseIndex().getReleaseProfile(getUniqueId());
    	if (releaseProfile != null) {
    		for (int songId : releaseProfile.getSongIds()) {
    			SongRecord song = Database.getSongIndex().getSongRecord(songId);
    			if (song != null)
    				result.add(song);
    		}
    	}
    	return result;
    	/*
    	Vector<SongRecord> result = new Vector<SongRecord>(getNumSongs());
    	Iterator<Integer> songIter = Database.getSongIndex().getIdsIterator();
    	while (songIter.hasNext()) {
    		SongRecord song = (SongRecord)Database.getSongIndex().getRecord(songIter.next());
    		if (song != null) {
	    		Vector<ReleaseRecord> releases = song.getReleases();
	    		boolean found = false;
	    		for (ReleaseRecord release : releases) {
	    			if (release.equals(this))
	    				found = true;
	    		}
	    		if (found) {
	    			result.add(song);
	    		}
    		}
    	}
    	return result;
    	*/
    }

    /////////////
    // SETTERS //
    /////////////

    public void setLabelNames(Vector<String> labelNames) { setLabelNames(labelNames, true, true); }
	public void setLabelNames(Vector<String> labelNames, boolean setSongLabels, boolean addReleasesToLabels) {
		try {
			labelNames = StringUtil.removeDuplicatesIgnoreCase(labelNames);
			getWriteLockSem().startRead("setLabelNames");
			// remove from old labels
			if (labelIds != null) {
				for (int labelId : labelIds) {
					LabelProfile label = Database.getLabelIndex().getLabelProfile(labelId);
					if (label != null)
						label.removeRelease(getUniqueId());
				}
			}
			if (labelNames != null) {
				labelIds = new int[labelNames.size()];
				int i = 0;
				for (String labelName : labelNames) {
					labelIds[i++] = Database.getLabelIndex().getUniqueIdFromIdentifier(new LabelIdentifier(labelName));
					LabelRecord labelRecord = Database.getLabelIndex().getLabelRecord(new LabelIdentifier(labelName));
					if (labelRecord == null) {
						try {
							Database.getLabelIndex().addLabel(new SubmittedLabel(labelName));
						} catch (AlreadyExistsException ae) {
						} catch (Exception e) {
							log.error("setLabelNames(): error", e);
						}
					}
				}
			} else {
				labelIds = new int[0];
			}
			if (addReleasesToLabels) {
				for (int labelId : labelIds) {
					LabelProfile label = Database.getLabelIndex().getLabelProfile(labelId);
					if (label != null)
						label.addRelease(getUniqueId());
				}
			}
			if (setSongLabels) {
				for (SongRecord song : getSongs()) {
					Vector<ReleaseRecord> songReleases = song.getReleases();
					if ((songReleases.size() > 0) && (songReleases.get(0).equals(this))) { // if this release is the song's primary release
						song.setLabelNames(labelNames, false, true);
					}
				}
			}
		} catch (Exception e) {
			log.error("error", e);
		} finally {
			getWriteLockSem().endRead();
		}
		setRelationalItemsChanged(true);
	}

	public void setOriginalYearReleased(short originalYearReleased) {
		this.originalYearReleased = originalYearReleased;
	}

	@Override
	public void setPlayCount(long numPlays) {
		long diff = numPlays - getPlayCount();
		super.setPlayCount(numPlays);
		for (ArtistRecord artist : getArtists())
			artist.incrementPlayCount(diff);
		for (LabelRecord label : getLabels())
			label.incrementPlayCount(diff);
	}

	public void incrementPlayCount(long increment, boolean updateRelations) {
		super.incrementPlayCount(increment);
		if (updateRelations) {
			for (ArtistRecord artist : getArtists())
				artist.incrementPlayCount(increment);
			for (LabelRecord label : getLabels())
				label.incrementPlayCount(increment);
		}
	}
	@Override
	public void incrementPlayCount(long increment) { incrementPlayCount(increment, true); }

	public void setLabelIds(int[] labelIds) { this.labelIds = labelIds; }

    /////////////
    // METHODS //
    /////////////

	@Override
	public String toString() {
		return computeReleaseDescription();
	}

	public String computeReleaseDescription() {
		ReleaseIdentifier releaseId = getReleaseIdentifier();
		return ReleaseIdentifier.toString(getArtistsDescription(), releaseId.getReleaseTitle());
	}

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);
    	ReleaseRecord releaseRecord = (ReleaseRecord)record;
    	// labels
    	int[] otherLabelIds = releaseRecord.labelIds;
    	if (otherLabelIds != null) {
	    	for (int otherLabelId : otherLabelIds) {
	    		boolean found = false;
	    		if (labelIds != null) {
	    			for (int labelId : labelIds) {
	    				if (labelId == otherLabelId)
	    					found = true;
	    			}
	    		}
	    		if (!found) {
	    			if (labelIds == null) {
	    				labelIds = new int[1];
	    				labelIds[0] = otherLabelId;
	    			} else {
	    				int[] newLabelIds = new int[labelIds.length + 1];
	    				for (int i = 0; i < labelIds.length; ++i)
	    					newLabelIds[i] = labelIds[i];
	    				newLabelIds[labelIds.length] = otherLabelId;
	    				labelIds = newLabelIds;
	    			}
	    		}
	    	}
    	}
    	// original year released
    	if ((originalYearReleased != 0) && (releaseRecord.originalYearReleased != 0))
    		originalYearReleased = (short)Math.min(originalYearReleased, releaseRecord.originalYearReleased);
    	else if (releaseRecord.originalYearReleased != 0)
    		originalYearReleased = releaseRecord.originalYearReleased;
    	// related songs (whose actual release name might have changed)
    	for (SongRecord song : releaseRecord.getSongs())
    		recordsToRefresh.put(song, null);
    }

    @Override
	public void write(LineWriter textWriter) {
    	super.write(textWriter);
    	textWriter.writeLine(1); //version
    	if (labelIds != null) {
    		textWriter.writeLine(labelIds.length);
    		for (int labelId : labelIds)
    			textWriter.writeLine(labelId);
    	} else {
    		textWriter.writeLine(0);
    	}
    	textWriter.writeLine(originalYearReleased);
    }

    @Override
	public void addFieldsToDocument(Document document) {
    	super.addFieldsToDocument(document);
    	Field artistField = new Field("artist", getArtistsDescription(), Field.Store.NO, Field.Index.ANALYZED);
    	artistField.setBoost(RE3Properties.getFloat("artist_field_boost"));
    	document.add(artistField);
    	Field titleField = new Field("title", getReleaseTitle(), Field.Store.NO, Field.Index.ANALYZED);
    	titleField.setBoost(RE3Properties.getFloat("title_field_boost"));
    	document.add(titleField);
    	document.add(new Field("label", getLabelsDescription(), Field.Store.NO, Field.Index.ANALYZED));
    	document.add(new Field("year", this.getOriginalYearReleasedAsString(), Field.Store.NO, Field.Index.ANALYZED));
    }

}
