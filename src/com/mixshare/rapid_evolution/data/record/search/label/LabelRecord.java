package com.mixshare.rapid_evolution.data.record.search.label;

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
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.ReleaseGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.similarity.PearsonSimilarity;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.SemaphoreFactory;

public class LabelRecord extends ReleaseGroupRecord {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(LabelRecord.class);

    static public int maxArtistsToCheck = RE3Properties.getInt("artist_similarity_max_comparisons");
    static public float minArtistDegreeThreshold = RE3Properties.getFloat("artist_similarity_minimum_degree_threshold");

    static private SemaphoreFactory artistDegreesSem = new SemaphoreFactory();

    ////////////
    // FIELDS //
    ////////////

    private String discogsLabelName;

    private int[] artistIds;
    private float[] artistDegrees;

	static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(LabelRecord.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("encodedArtistTokens")) {
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

    public LabelRecord() { };
    public LabelRecord(LabelIdentifier labelId, int uniqueId) {
    	this.id = labelId;
    	this.uniqueId = uniqueId;
    }
    public LabelRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	int numArtists = Integer.parseInt(lineReader.getNextLine());
    	artistIds = new int[numArtists];
    	artistDegrees = new float[numArtists];
    	for (int i = 0; i < numArtists; ++i)
    		artistIds[i] = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numArtists; ++i)
    		artistDegrees[i] = Float.parseFloat(lineReader.getNextLine());
    	discogsLabelName = lineReader.getNextLine();
    }

    /////////////
    // GETTERS //
    /////////////

    public String getDiscogsLabelName() {
    	if ((discogsLabelName == null) || (discogsLabelName.length() == 0))
    		return getLabelName();
    	return discogsLabelName;
    }

    @Override
	public byte getDataType() { return DATA_TYPE_LABELS; }

    @Override
	public Index getIndex() { return Database.getLabelIndex(); }

    public LabelIdentifier getLabelIdentifier() { return (LabelIdentifier)id; }
    public String getLabelName() { return (getLabelIdentifier() != null) ? getLabelIdentifier().getName() : ""; }

    public ModelManagerInterface getModelManager() { return Database.getLabelModelManager(); }

    @Override
	public Vector<SongRecord> getSongs() {
    	Vector<SongRecord> result = new Vector<SongRecord>();
    	LabelProfile labelProfile = Database.getLabelIndex().getLabelProfile(getUniqueId());
    	if (labelProfile != null) {
    		for (int songId : labelProfile.getSongIds()) {
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
    		Vector<LabelRecord> labels = song.getLabels();
    		boolean found = false;
    		for (LabelRecord label : labels) {
    			if (label.equals(this))
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
    	LabelProfile labelProfile = Database.getLabelIndex().getLabelProfile(getUniqueId());
    	if (labelProfile != null) {
    		for (int releaseId : labelProfile.getReleaseIds()) {
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
    		Vector<LabelRecord> labels = release.getLabels();
    		boolean found = false;
    		for (LabelRecord label : labels) {
    			if (label.equals(this))
    				found = true;
    		}
    		if (found) {
    			result.add(release);
    		}
    	}
    	return result;
    	*/
    }

    public int[] getArtistIds() { return artistIds; }
    public float[] getArtistDegrees() { return artistDegrees; }
    public int getNumArtists() { return artistIds != null ? artistIds.length : 0; }
    public Vector<Integer> getArtistIdsVector() {
    	Vector<Integer> result = new Vector<Integer>(artistIds != null ? artistIds.length : 0);
    	if (artistIds != null)
    		for (int artistId : artistIds)
    			result.add(artistId);
    	return result;
    }
    public float getArtistDegree(String artistName) {
    	int artistId = Database.getArtistIndex().getUniqueIdFromIdentifier(new ArtistIdentifier(artistName));
    	for (int i = 0; i < artistIds.length; ++i)
    		if (artistIds[i] == artistId)
    			return artistDegrees[i];
    	return 0.0f;
    }
	public String getArtistsDescription() {
		StringBuffer result = new StringBuffer();
		if (artistIds != null) {
			for (int artistId : artistIds) {
				if (result.length() > 0)
					result.append("; ");
				ArtistRecord artistRecord = (ArtistRecord)Database.getArtistIndex().getRecord(artistId);
				if (artistRecord != null)
					result.append(artistRecord.toString());
				else {
					Identifier id = Database.getArtistIndex().getIdentifierFromUniqueId(artistId);
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

    public void setDiscogsLabelName(String discogsLabelName) {
    	this.discogsLabelName = discogsLabelName;
    }

    public void setArtistDegrees(Vector<DegreeValue> degrees) {
    	try {
    		artistDegreesSem.acquire(uniqueId);
    		getWriteLockSem().startRead("setArtistDegrees");
	    	artistIds = new int[degrees.size()];
	    	artistDegrees = new float[degrees.size()];
	    	int i = 0;
	    	for (DegreeValue degree : degrees) {
	    		artistIds[i] = Database.getArtistIndex().getUniqueIdFromIdentifier(new ArtistIdentifier(degree.getName()));
	    		artistDegrees[i] = degree.getPercentage();
	    		++i;
	    	}
    	} catch (Exception e) {
    		log.error("setArtistDegrees(): error", e);
    	} finally {
    		getWriteLockSem().endRead();
    		artistDegreesSem.release(uniqueId);
    	}
    }

    public void removeArtistIdsBeyond(int maxArtistId) {
    	if (artistIds != null) {
    		int removed = 0;
    		for (int artistId : artistIds)
    			if (artistId >= maxArtistId)
    				++removed;
    		if (removed > 0) {
    			int[] newArtistIds = new int[artistIds.length - removed];
    			float[] newArtistDegrees = new float[artistIds.length - removed];
    			int i = 0;
    			int c = 0;
    			for (int artistId : artistIds) {
    				if (artistId < maxArtistId) {
    					newArtistIds[i] = artistIds[c];
    					newArtistDegrees[i] = artistDegrees[c];
    					++i;
    				}
    				++c;
    			}
    			artistIds = newArtistIds;
    			artistDegrees = newArtistDegrees;
    		}
    	}
    }

	public void setArtistDegrees(float[] artistDegrees) { this.artistDegrees = artistDegrees; }
	public void setArtistIds(int[] artistIds) { this.artistIds = artistIds; }

    /////////////
    // METHODS //
    /////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);
    	LabelRecord labelRecord = (LabelRecord)record;
    	// update related songs (whose actual label names might have changed)
    	Vector<SongRecord> songs = labelRecord.getSongs();
    	for (SongRecord song : songs)
    		recordsToRefresh.put(song, null);
    	// updated related releases (whose actual label names might have changed)
    	Vector<ReleaseRecord> releases = labelRecord.getReleases();
    	for (ReleaseRecord release : releases)
    		recordsToRefresh.put(release, null);
    }

    /**
     * This computes the "Pearson Correlation Coefficient" between 2 profiles,
     * using artist degree information.
     */
    public float computeArtistSimilarity(LabelRecord record) {
        return PearsonSimilarity.computeSimilarity(artistIds, artistDegrees, record.artistIds, record.artistDegrees, maxArtistsToCheck, minArtistDegreeThreshold);
    }

    @Override
	public void write(LineWriter textWriter) {
    	super.write(textWriter);
    	textWriter.writeLine(1); //version
    	if (artistIds != null) {
    		textWriter.writeLine(artistIds.length);
    		for (int artistId : artistIds)
    			textWriter.writeLine(artistId);
    		for (float artistDegree : artistDegrees)
    			textWriter.writeLine(artistDegree);
    	} else {
    		textWriter.writeLine(0);
    	}
    	textWriter.writeLine(discogsLabelName);
    }

    @Override
	public void addFieldsToDocument(Document document) {
    	super.addFieldsToDocument(document);
    	Field titleField = new Field("name", getLabelName(), Field.Store.NO, Field.Index.ANALYZED);
    	titleField.setBoost(RE3Properties.getFloat("title_field_boost"));
    	document.add(titleField);
    	document.add(new Field("artists", getArtistsDescription(), Field.Store.NO, Field.Index.ANALYZED));
    }

}
