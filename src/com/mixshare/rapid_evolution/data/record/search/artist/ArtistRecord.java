package com.mixshare.rapid_evolution.data.record.search.artist;

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
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.ReleaseGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.similarity.PearsonSimilarity;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.SemaphoreFactory;

public class ArtistRecord extends ReleaseGroupRecord {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(ArtistRecord.class);

    static public int maxLabelsToCheck = RE3Properties.getInt("label_similarity_max_comparisons");
    static public float minLabelDegreeThreshold = RE3Properties.getFloat("label_similarity_minimum_degree_threshold");

    static private SemaphoreFactory labelDegreesSem = new SemaphoreFactory();

    ////////////
    // FIELDS //
    ////////////

    private int[] labelIds;
    private float[] labelDegrees;

    private String discogsArtistName;
    private String lastfmArtistName;
    private String mbId;

	static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(ArtistRecord.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("encodedLabelTokens")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public ArtistRecord() { }
    public ArtistRecord(ArtistIdentifier artistId, int uniqueId) {
    	this.id = artistId;
    	this.uniqueId = uniqueId;
    }
    public ArtistRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	int numLabels = Integer.parseInt(lineReader.getNextLine());
    	labelIds = new int[numLabels];
    	labelDegrees = new float[numLabels];
    	for (int i = 0; i < numLabels; ++i)
    		labelIds[i] = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numLabels; ++i)
    		labelDegrees[i] = Float.parseFloat(lineReader.getNextLine());
    	discogsArtistName = lineReader.getNextLine();
    	lastfmArtistName = lineReader.getNextLine();
    	mbId = lineReader.getNextLine();
    }

    /////////////
    // GETTERS //
    /////////////

    @Override
	public byte getDataType() { return DATA_TYPE_ARTISTS; }

    @Override
	public Index getIndex() { return Database.getArtistIndex(); }

    public ArtistIdentifier getArtistIdentifier() { return (ArtistIdentifier)id; }
    public String getArtistName() { return (getArtistIdentifier() != null) ? getArtistIdentifier().getName() : ""; }

    public String getDiscogsArtistName() {
    	if ((discogsArtistName == null) || (discogsArtistName.length() == 0))
    		return getArtistName();
    	return discogsArtistName;
    }
    public String getLastfmArtistName() {
    	if ((lastfmArtistName == null) || (lastfmArtistName.length() == 0))
    		return getArtistName();
    	return lastfmArtistName;
    }
    public String getMbId() {
    	return mbId;
    }

    public ModelManagerInterface getModelManager() { return Database.getArtistModelManager(); }

    @Override
	public Vector<SongRecord> getSongs() {
    	Vector<SongRecord> result = new Vector<SongRecord>();
    	ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(getUniqueId());
    	if (artistProfile != null) {
    		for (int songId : artistProfile.getSongIds()) {
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
    		Vector<ArtistRecord> artists = song.getArtists();
    		boolean found = false;
    		for (ArtistRecord artist : artists) {
    			if (artist.equals(this))
    				found = true;
    		}
    		if (found) {
    			result.add(song);
    		}
    	}
    	return result;
    	*/
    }

    @Override
	public Vector<ReleaseRecord> getReleases() {
    	Vector<ReleaseRecord> result = new Vector<ReleaseRecord>();
    	ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(getUniqueId());
    	if (artistProfile != null) {
    		for (int releaseId : artistProfile.getReleaseIds()) {
    			ReleaseRecord release = Database.getReleaseIndex().getReleaseRecord(releaseId);
    			if (release != null)
    				result.add(release);
    		}
    	}
    	return result;
    	/*
    	Vector<ReleaseRecord> result = new Vector<ReleaseRecord>(getNumReleases());
    	Iterator<Integer> releaseIter = Database.getReleaseIndex().getIdsIterator();
    	while (releaseIter.hasNext()) {
    		ReleaseRecord release = (ReleaseRecord)Database.getReleaseIndex().getRecord(releaseIter.next());
    		Vector<ArtistRecord> artists = release.getArtists();
    		boolean found = false;
    		for (ArtistRecord artist : artists) {
    			if (artist.equals(this))
    				found = true;
    		}
    		if (found) {
    			result.add(release);
    		}
    	}
    	return result;
    	*/
    }

    public int[] getLabelIds() { return labelIds; }
	public float[] getLabelDegrees() { return labelDegrees; }
    public int getNumLabels() { return labelIds != null ? labelIds.length : 0; }
    public Vector<Integer> getLabelIdsVector() {
    	Vector<Integer> result = new Vector<Integer>(labelIds != null ? labelIds.length : 0);
    	if (labelIds != null)
    		for (int labelId : labelIds)
    			result.add(labelId);
    	return result;
    }
    public float getLabelDegree(String labelName) {
    	int labelId = Database.getLabelIndex().getUniqueIdFromIdentifier(new LabelIdentifier(labelName));
    	for (int i = 0; i < labelIds.length; ++i)
    		if (labelIds[i] == labelId)
    			return labelDegrees[i];
    	return 0.0f;
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
					Identifier id = Database.getLabelIndex().getIdentifierFromUniqueId(labelId);
					if (id != null)
						result.append(id.toString());
				}
			}
		}
		return result.toString();
	}

    /////////////
    // SETTERS //
    /////////////

    public void setLabelDegrees(Vector<DegreeValue> degrees) {
    	try {
    		labelDegreesSem.acquire(uniqueId);
    		getWriteLockSem().startRead("setLabelDegrees");
	    	labelIds = new int[degrees.size()];
	    	labelDegrees = new float[degrees.size()];
	    	int i = 0;
	    	for (DegreeValue degree : degrees) {
	    		labelIds[i] = Database.getLabelIndex().getUniqueIdFromIdentifier(new LabelIdentifier(degree.getName()));
	    		labelDegrees[i] = degree.getPercentage();
	    		++i;
	    	}
    	} catch (Exception e) {
    		log.error("setLabelDegrees(): error", e);
    	} finally {
    		getWriteLockSem().endRead();
    		labelDegreesSem.release(uniqueId);
    	}
    }

    public void removeLabelIdsBeyond(int maxLabelId) {
    	if (labelIds != null) {
    		int removed = 0;
    		for (int labelId : labelIds)
    			if (labelId >= maxLabelId)
    				++removed;
    		if (removed > 0) {
    			int[] newLabelIds = new int[labelIds.length - removed];
    			float[] newLabelDegrees = new float[labelIds.length - removed];
    			int i = 0;
    			int c = 0;
    			for (int labelId : labelIds) {
    				if (labelId < maxLabelId) {
    					newLabelIds[i] = labelIds[c];
    					newLabelDegrees[i] = labelDegrees[c];
    					++i;
    				}
    				++c;
    			}
    			labelIds = newLabelIds;
    			labelDegrees = newLabelDegrees;
    		}
    	}
    }

    public void setDiscogsArtistName(String discogsArtistName) { this.discogsArtistName = discogsArtistName; }
    public void setLastfmArtistName(String lastfmArtistName) { this.lastfmArtistName = lastfmArtistName; }
    public void setMbId(String mbId) { this.mbId = mbId; }

	public void setLabelDegrees(float[] labelDegrees) { this.labelDegrees = labelDegrees; }
	public void setLabelIds(int[] labelIds) { this.labelIds = labelIds; }

    /////////////
    // METHODS //
    /////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);
    	ArtistRecord artistRecord = (ArtistRecord)record;
    	// update related songs (whose actual artist names might have changed)
    	Vector<SongRecord> songs = artistRecord.getSongs();
    	for (SongRecord song : songs) {
    		// check to see if the song contains both artist ids, if so one needs to be removed
    		int[] songArtistIds = song.getSongIdentifier().getArtistIds();
    		boolean contains = false;
    		int i = 0;
    		while ((i < songArtistIds.length) && !contains) {
    			if (songArtistIds[i] == getUniqueId())
    				contains = true;
    			++i;
    		}
    		if (contains) {
    			// there would be duplicate artists on the song if we don't remove the merged id
    			SongProfile songProfile = Database.getSongIndex().getSongProfile(song.getUniqueId());
    			if (songProfile != null) {
    				Vector<String> newArtistNames = new Vector<String>();
    				for (i = 0; i < songArtistIds.length; ++i) {
    					if (songArtistIds[i] != artistRecord.getUniqueId()) { // remove the merged id
    						ArtistRecord newArtist = Database.getArtistIndex().getArtistRecord(songArtistIds[i]);
    						if (newArtist != null)
    							newArtistNames.add(newArtist.getArtistName());
    					}
    				}
    				try {
    					songProfile.setArtistNames(newArtistNames);
    				} catch (AlreadyExistsException ae) {
    					SongProfile existingProfile = Database.getSongIndex().getSongProfile(ae.getId());
    					if (existingProfile != null)
    						Database.mergeProfiles(existingProfile, songProfile);
    				}
    			}
    		}
    		recordsToRefresh.put(song, null);
    	}
    	// updated related releases (whose actual artist names might have changed)
    	Vector<ReleaseRecord> releases = artistRecord.getReleases();
    	for (ReleaseRecord release : releases) {
    		// check to see if the release contains both artist ids, if so one needs to be removed
    		int[] releaseArtistIds = release.getReleaseIdentifier().getArtistIds();
    		boolean contains = false;
    		int i = 0;
    		while ((i < releaseArtistIds.length) && !contains) {
    			if (releaseArtistIds[i] == getUniqueId())
    				contains = true;
    			++i;
    		}
    		if (contains) {
    			// there would be duplicate artists on the release if we don't remove the merged id
    			ReleaseProfile releaseProfile = Database.getReleaseIndex().getReleaseProfile(release.getUniqueId());
    			if (releaseProfile != null) {
    				Vector<String> newArtistNames = new Vector<String>();
    				for (i = 0; i < releaseArtistIds.length; ++i) {
    					if (releaseArtistIds[i] != artistRecord.getUniqueId()) { // remove the merged id
    						ArtistRecord newArtist = Database.getArtistIndex().getArtistRecord(releaseArtistIds[i]);
    						if (newArtist != null)
    							newArtistNames.add(newArtist.getArtistName());
    					}
    				}
    				try {
    					releaseProfile.setArtistNames(newArtistNames, false);
    				} catch (AlreadyExistsException ae) {
    					ReleaseProfile existingRelease = Database.getReleaseIndex().getReleaseProfile(ae.getId());
    					if (existingRelease != null)
    						Database.mergeProfiles(existingRelease, releaseProfile);
    				}
    			}
    		}
    		recordsToRefresh.put(release, null);
    	}
    }

    /**
     * This computes the "Pearson Correlation Coefficient" between 2 profiles,
     * using label degree information.
     */
    public float computeLabelSimilarity(ArtistRecord record) {
        return PearsonSimilarity.computeSimilarity(labelIds, labelDegrees, record.labelIds, record.labelDegrees, maxLabelsToCheck, minLabelDegreeThreshold);
    }

    @Override
	public void write(LineWriter textWriter) {
    	super.write(textWriter);
    	textWriter.writeLine(1); //version
    	if (labelIds != null) {
    		textWriter.writeLine(labelIds.length);
    		for (int labelId : labelIds)
    			textWriter.writeLine(labelId);
    		for (float labelDegree : labelDegrees)
    			textWriter.writeLine(labelDegree);
    	} else {
    		textWriter.writeLine(0);
    	}
    	textWriter.writeLine(discogsArtistName);
    	textWriter.writeLine(lastfmArtistName);
    	textWriter.writeLine(mbId);
    }

    @Override
	public void addFieldsToDocument(Document document) {
    	super.addFieldsToDocument(document);
    	Field titleField = new Field("name", getArtistName(), Field.Store.NO, Field.Index.ANALYZED);
    	titleField.setBoost(RE3Properties.getFloat("title_field_boost"));
    	document.add(titleField);
    	document.add(new Field("labels", getLabelsDescription(), Field.Store.NO, Field.Index.ANALYZED));
    }

}
