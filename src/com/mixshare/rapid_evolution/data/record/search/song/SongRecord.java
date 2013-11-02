package com.mixshare.rapid_evolution.data.record.search.song;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.json.JSONObject;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.submitted.search.artist.SubmittedArtist;
import com.mixshare.rapid_evolution.data.submitted.search.label.SubmittedLabel;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityDescription;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.bpm.CombinedBpm;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.key.CombinedKey;
import com.mixshare.rapid_evolution.music.key.CombinedKeyCode;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.key.KeyCode;
import com.mixshare.rapid_evolution.music.timesig.TimeSig;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.SemaphoreFactory;
import com.mixshare.rapid_evolution.video.util.VideoUtil;

public class SongRecord extends SearchRecord {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(SongRecord.class);

    static private SemaphoreFactory releaseInstancesSem = new SemaphoreFactory();

    ////////////
    // FIELDS //
    ////////////

    private String track; // used to be able to store a track if no release exists
    private String title;
    private String remix;

    // release instances
    private int[] releaseIds;
    private String[] releaseTracks;

    private int[] labelIds;

    // key
    private float startKeyRootValue = Key.ROOT_UNKNOWN; // ~0-12 range, corresponds with each note on the keyboard
    private byte startKeyScaleType = -1;
    private float endKeyRootValue = Key.ROOT_UNKNOWN; // ~0-12 range, corresponds with each note on the keyboard
    private byte endKeyScaleType = -1;
    private byte keyAccuracy = 0;

    // bpm
    private float startBpm;
    private float endBpm;
    private byte bpmAccuracy = 0;

    // time signature
    private byte timeSigNumerator = 4;
    private byte timeSigDenominator = 4;

    private byte beatIntensity;

    // duration
    private int timeInMillis;

    private String songFilename;

    private short originalYearReleased;

    private byte numMixouts = Byte.MIN_VALUE;

    private float[] timbre;
    private float[] timbreVariance;

    private int[] featuringArtistIds;

    transient private boolean doesFileExist;
    transient private boolean hasCheckedFileExists;

	static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SongRecord.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("duration") || pd.getName().equals("startKey") || pd.getName().equals("endKey")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public SongRecord() { };
    public SongRecord(SongIdentifier songId, int uniqueId) {
    	this.id = songId;
    	this.uniqueId = uniqueId;
    }
    public SongRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	track = lineReader.getNextLine();
    	title = lineReader.getNextLine();
    	remix = lineReader.getNextLine();
    	int numReleases = Integer.parseInt(lineReader.getNextLine());
    	releaseIds = new int[numReleases];
    	releaseTracks = new String[numReleases];
    	for (int i = 0; i < numReleases; ++i)
    		releaseIds[i] = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numReleases; ++i)
    		releaseTracks[i] = lineReader.getNextLine();
    	int numLabels = Integer.parseInt(lineReader.getNextLine());
    	labelIds = new int[numLabels];
    	for (int i = 0; i < numLabels; ++i)
    		labelIds[i] = Integer.parseInt(lineReader.getNextLine());
    	startKeyRootValue = Float.parseFloat(lineReader.getNextLine());
    	startKeyScaleType = Byte.parseByte(lineReader.getNextLine());
    	endKeyRootValue = Float.parseFloat(lineReader.getNextLine());
    	endKeyScaleType = Byte.parseByte(lineReader.getNextLine());
    	keyAccuracy = Byte.parseByte(lineReader.getNextLine());
    	startBpm = Float.parseFloat(lineReader.getNextLine());
    	endBpm = Float.parseFloat(lineReader.getNextLine());
    	bpmAccuracy = Byte.parseByte(lineReader.getNextLine());
    	timeSigNumerator = Byte.parseByte(lineReader.getNextLine());
    	timeSigDenominator = Byte.parseByte(lineReader.getNextLine());
    	beatIntensity = Byte.parseByte(lineReader.getNextLine());
    	timeInMillis = Integer.parseInt(lineReader.getNextLine());
    	songFilename = lineReader.getNextLine();
    	originalYearReleased = Short.parseShort(lineReader.getNextLine());
    	if (version == 1)
    		dateAdded = Long.parseLong(lineReader.getNextLine());
    	numMixouts = Byte.parseByte(lineReader.getNextLine());
    	int timbreSize = Integer.parseInt(lineReader.getNextLine());
    	if (timbreSize > 0) {
    		timbre = new float[timbreSize];
    		timbreVariance = new float[timbreSize];
    		for (int i = 0; i < timbreSize; ++i)
    			timbre[i] = Float.parseFloat(lineReader.getNextLine());
    		for (int i = 0; i < timbreSize; ++i)
    			timbreVariance[i] = Float.parseFloat(lineReader.getNextLine());
    	}
    	int numFeaturingArtists = Integer.parseInt(lineReader.getNextLine());
    	featuringArtistIds = new int[numFeaturingArtists];
    	for (int i = 0; i < numFeaturingArtists; ++i)
    		featuringArtistIds[i] = Integer.parseInt(lineReader.getNextLine());
    }

    /////////////
    // GETTERS //
    /////////////

    @Override
	public byte getDataType() { return DATA_TYPE_SONGS; }

    @Override
	public Index getIndex() { return Database.getSongIndex(); }

    public SongIdentifier getSongIdentifier() { return (SongIdentifier)id; }

    // artist info
    public int[] getArtistIds() { return (getSongIdentifier() != null) ? getSongIdentifier().getArtistIds() : null; }
	public Vector<ArtistRecord> getArtists() {
		Vector<ArtistRecord> result = new Vector<ArtistRecord>(getSongIdentifier() != null ? getSongIdentifier().getNumArtists() : 0);
		if (getSongIdentifier() != null) {
			for (int artistId : getSongIdentifier().getArtistIds()) {
				ArtistRecord artistRecord = (ArtistRecord)Database.getArtistIndex().getRecord(artistId);
				if (artistRecord != null)
					result.add(artistRecord);
			}
			java.util.Collections.sort(result);
		}
		return result;
	}
	public Vector<String> getArtistNames() {
		Vector<String> result = new Vector<String>((getSongIdentifier() != null) ? getSongIdentifier().getNumArtists() : 0);
		if (getSongIdentifier() != null) {
			for (int artistId : getSongIdentifier().getArtistIds()) {
				ArtistRecord artistRecord = Database.getArtistIndex().getArtistRecord(artistId);
				if (artistRecord != null)
					result.add(artistRecord.getArtistName());
			}
			java.util.Collections.sort(result);
		}
		return result;
	}
	public String getArtistsDescription() { return getArtistsDescription(true); }
	public String getArtistsDescription(boolean includeFeaturingArtists) {
		StringBuffer result = new StringBuffer();
		Vector<ArtistRecord> artists = getArtists();
		for (ArtistRecord artist : artists) {
			if (result.length() > 0)
				result.append("; ");
			result.append(artist.toString());
		}
		if (includeFeaturingArtists && (featuringArtistIds != null) && (featuringArtistIds.length > 0)) {
			boolean addedFeaturingLabel = false;
			for (int featuringArtistId : featuringArtistIds) {
				ArtistRecord artist = Database.getArtistIndex().getArtistRecord(featuringArtistId);
				if (artist != null) {
					if (!addedFeaturingLabel) {
						result.append(" feat. ");
						addedFeaturingLabel = true;
					} else {
						result.append("; ");
					}
					result.append(artist.getArtistName());
				}
			}
		}
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
		return result.toString();
	}
	public boolean containsArtist(int uniqueId) {
		for (int artistId : getSongIdentifier().getArtistIds())
			if (artistId == uniqueId)
				return true;
		return false;
	}

	// release info
	public int[] getReleaseIds() { return releaseIds; }
	public Vector<ReleaseRecord> getReleases() {
		Vector<ReleaseRecord> result = new Vector<ReleaseRecord>();
		if (releaseIds != null) {
			for (int releaseId : releaseIds) {
				ReleaseRecord releaseRecord = (ReleaseRecord)Database.getReleaseIndex().getRecord(releaseId);
				if (releaseRecord != null) {
					if (!result.contains(releaseRecord))
						result.add(releaseRecord);
				}
			}
		}
		java.util.Collections.sort(result);
		return result;
	}
	public Vector<ReleaseInstance> getReleaseInstances() {
		return getReleaseInstances(true);
	}
	public Vector<ReleaseInstance> getReleaseInstances(boolean sort) {
		Vector<ReleaseInstance> result = new Vector<ReleaseInstance>();
		int i = 0;
		if (releaseIds != null) {
			for (int releaseId : releaseIds) {
				result.add(new ReleaseInstance(releaseId, releaseTracks[i]));
				++i;
			}
		}
		if (sort)
			java.util.Collections.sort(result);
		return result;
	}
	public String getReleaseTitle() {
		Vector<ReleaseInstance> releaseInstances = getReleaseInstances();
		if (releaseInstances.size() > 0) {
			ReleaseRecord release = releaseInstances.get(0).getRelease();
			if (release != null)
				return release.getReleaseTitle();
		}
		return "";
	}

	public String getTrackForReleaseId(int releaseId) {
		for (ReleaseInstance releaseInstance : getReleaseInstances()) {
			if (releaseInstance.getReleaseId() == releaseId)
				return releaseInstance.getTrack();
		}
		return "";
	}

	public String getTrack() { return (track == null) ? "" : track; }
	public String getTitle() { return title; }
	public String getRemix() { return remix; }

	/**
	 * The song description might include title & remix, or release + track, whatever fields
	 * uniquely identify it amongst other songs (excluding artist info)
	 */
	public String getSongDescription() { return (getSongIdentifier() != null) ? getSongIdentifier().getSongDescription() : ""; }

	public int getNumLabels() { return labelIds != null ? labelIds.length : 0; }
	public Vector<String> getSourceLabelNames() {
		Vector<String> result = new Vector<String>(labelIds != null ? labelIds.length : 0);
		if (labelIds != null) {
			for (int labelId : labelIds) {
				Identifier labelIdentifier = Database.getLabelIndex().getIdentifierFromUniqueId(labelId);
				if (labelIdentifier != null)
					result.add(labelIdentifier.toString());
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
					Identifier id = Database.getLabelIndex().getIdentifierFromUniqueId(labelId);
					if (id != null)
						result.append(id.toString());
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

	public CombinedKey getKey() { return CombinedKey.getCombinedKey(getStartKey(), getEndKey()); }
	public CombinedKeyCode getKeyCode() { return CombinedKeyCode.getCombinedKeyCode(getStartKey().getKeyCode(), getEndKey().getKeyCode()); }
    public Key getStartKey() { return Key.getKey(startKeyRootValue, startKeyScaleType); }
    public Key getEndKey() { return Key.getKey(endKeyRootValue, endKeyScaleType); }
    public KeyCode getStartKeyCode() { return Key.getKey(startKeyRootValue, startKeyScaleType).getKeyCode(); }
    public KeyCode getEndKeyCode() { return Key.getKey(endKeyRootValue, endKeyScaleType).getKeyCode(); }
    public byte getKeyAccuracy() { return keyAccuracy; }

    public CombinedBpm getBpm() { return new CombinedBpm(startBpm, endBpm); }
    public Bpm getBpmStart() { return new Bpm(startBpm); }
    public float getStartBpm() { return startBpm; }
    public Bpm getBpmEnd() { return new Bpm(endBpm); }
    public float getEndBpm() { return endBpm; }
    public byte getBpmAccuracy() { return bpmAccuracy; }

    public String getGenre() {
    	Vector<DegreeValue> styleDegrees = getActualStyleDegreeValues();
    	if (styleDegrees.size() > 0)
    		return styleDegrees.get(0).getName();
    	return "";
    }

    public TimeSig getTimeSig() { return TimeSig.getTimeSig(timeSigNumerator, timeSigDenominator); }

    public byte getBeatIntensity() { return beatIntensity; }
    public BeatIntensity getBeatIntensityValue() { return BeatIntensity.getBeatIntensity(beatIntensity); }
    public BeatIntensityDescription getBeatIntensityDescription() { return BeatIntensityDescription.getBeatIntensityDescription(beatIntensity); }

    public Duration getDuration() { return new Duration(timeInMillis); }
    public int getDurationInMillis() { return timeInMillis; }

    public String getSongFilename() {  // can be null
    	return OSHelper.translateMusicDirectory(songFilename);
	}
    public boolean hasValidSongFilename() {
    	if ((songFilename != null) && (songFilename.length() > 0)) {
    		if (!hasCheckedFileExists) {
    			doesFileExist = new File(getSongFilename()).exists();
    			hasCheckedFileExists = true;
    		}
    		return doesFileExist;
    	}
    	return false;
    }
    public String getSongDirectory() {
    	String directory = FileUtil.getDirectoryFromFilename(getSongFilename());
    	if (directory.endsWith("/") || directory.endsWith("\\"))
    		return directory.substring(0, directory.length() - 1);
    	return directory;
    }

    public String getOriginalYearReleasedAsString() { return (originalYearReleased > 0) ? String.valueOf(originalYearReleased) : ""; }
    public short getOriginalYearReleased() { return originalYearReleased; }

	public short getNumMixouts() {
		short fullRange = numMixouts;
		fullRange -= Byte.MIN_VALUE; // will add 128
		return fullRange;
	}

    public ModelManagerInterface getModelManager() { return Database.getSongModelManager(); }

	public float[] getTimbre() { return timbre; }
	public float[] getTimbreVariance() { return timbreVariance; }

	public Vector<ArtistRecord> getFeaturingArtists() {
		Vector<ArtistRecord> result = new Vector<ArtistRecord>((featuringArtistIds != null) ? featuringArtistIds.length : 0);
		if (featuringArtistIds != null) {
			for (int artistId : featuringArtistIds) {
				ArtistRecord artist = Database.getArtistIndex().getArtistRecord(artistId);
				if (artist != null)
					result.add(artist);
			}
		}
		return result;
	}
    public int[] getFeaturingArtistIds() {
    	if (featuringArtistIds == null)
    		return new int[0];
    	return featuringArtistIds;
    }
	public String getFeaturingArtistsDescription() {
		StringBuffer result = new StringBuffer();
		if (featuringArtistIds != null) {
			for (int artistId : featuringArtistIds) {
				ArtistRecord artist = Database.getArtistIndex().getArtistRecord(artistId);
				if (artist != null) {
					if (result.length() > 0)
						result.append("; ");
					result.append(artist.getArtistName());
				}
			}
		}
		return result.toString();
	}

	public String getBestRelatedThumbnailImageFilename() {
		boolean foundImage = false;
		try {
			Vector<ReleaseRecord> releases = getReleases();
			for (ReleaseRecord release : releases) {
				if (release.hasThumbnail())
					return release.getThumbnailImageFilename();
			}
			if (!foundImage) {
				Vector<ArtistRecord> artists = getArtists();
				for (ArtistRecord artist : artists)
					if (artist.hasThumbnail())
						return artist.getThumbnailImageFilename();
			}
			if (!foundImage) {
				Vector<LabelRecord> labels = getLabels();
				for (LabelRecord label : labels)
					if (label.hasThumbnail())
						return label.getThumbnailImageFilename();
			}
		} catch (Error e) {
			log.error("getBestRelatedThumbnailImageFilename(): error", e);
		} catch (Exception e) {
			log.error("getBestRelatedThumbnailImageFilename(): exception", e);
		}
		return RE3Properties.getProperty("default_thumbnail_image_filename");
	}

    // for serialization
	public String[] getReleaseTracks() { return releaseTracks; }
	public float getStartKeyRootValue() { return startKeyRootValue; }
	public byte getStartKeyScaleType() { return startKeyScaleType; }
	public float getEndKeyRootValue() { return endKeyRootValue; }
	public byte getEndKeyScaleType() { return endKeyScaleType; }
	public byte getTimeSigNumerator() { return timeSigNumerator; }
	public byte getTimeSigDenominator() { return timeSigDenominator; }
	public int getTimeInMillis() { return timeInMillis; }

    /////////////
    // SETTERS //
    /////////////

    /**
     * Don't call directly (except in initProfile), call ReleaseProfile.addSong()
     */
    public void addReleaseInstance(ReleaseInstance releaseInstance) { addReleaseInstance(releaseInstance, false); }
    public void addReleaseInstance(ReleaseInstance releaseInstance, boolean insertAsPrimary) {
    	try {
    		releaseInstancesSem.acquire(uniqueId);
    		if (log.isTraceEnabled())
    			log.trace("addReleaseInstance(): this=" + this + ", releaseInstance=" + releaseInstance + ", insertAsPrimary=" + insertAsPrimary);
    		// make sure release doesn't already exist
    		if (releaseIds != null) {
    			for (int i = 0; i < releaseIds.length; ++i) {
    				if (releaseIds[i] == releaseInstance.getActualReleaseId()) {
    					// found
    					releaseTracks[i] = releaseInstance.getTrack();
    					if (insertAsPrimary && (i != 0)) {
    						int tempReleaseId = releaseIds[0];
    						String tempReleaseTrack = releaseTracks[0];
    						releaseIds[0] = releaseInstance.getReleaseId();
    						releaseTracks[0] = releaseInstance.getTrack();
    						releaseIds[i] = tempReleaseId;
    						releaseTracks[i] = tempReleaseTrack;
    					}
    					return;
    				}
    			}
    		}
    		getWriteLockSem().startRead("addReleaseInstance");
	    	if (releaseIds == null) {
	    		releaseIds = new int[1];
	    		releaseTracks = new String[1];
	    		releaseIds[0] = releaseInstance.getReleaseId();
	    		releaseTracks[0] = releaseInstance.getTrack();
	    	} else {
	    		int i = 0;
	    		boolean found = false;
	    		while ((i < releaseIds.length) && !found) {
	    			if ((releaseIds[i] == releaseInstance.getReleaseId()) && (releaseTracks[i].equalsIgnoreCase(releaseInstance.getTrack()))) {
	    				found = true;
	    			}
	    			++i;
	    		}
	    		if (!found) {
	    			int[] newReleaseIds = new int[releaseIds.length + 1];
	    			String[] newReleaseTracks = new String[releaseIds.length + 1];
	    			i = 0;
	    			int n = 0;
	    			if (insertAsPrimary) {
	        			newReleaseIds[n] = releaseInstance.getReleaseId();
	        			newReleaseTracks[n] = releaseInstance.getTrack();
	        			++n;
	    			}
	    			for (int releaseId : releaseIds) {
	    				newReleaseIds[n] = releaseId;
	    				newReleaseTracks[n] = releaseTracks[i];
	    				++n;
	    				++i;
	    			}
	    			if (!insertAsPrimary) {
	    				newReleaseIds[releaseIds.length] = releaseInstance.getReleaseId();
	    				newReleaseTracks[releaseIds.length] = releaseInstance.getTrack();
	    			}
	    			releaseIds = newReleaseIds;
	    			releaseTracks = newReleaseTracks;
	    		}
	    	}
	    	track = releaseTracks[0];
    	} catch (Exception e) {
    		log.error("addReleaseInstance(): error", e);
    	} finally {
    		getWriteLockSem().endRead();
    		releaseInstancesSem.release(uniqueId);
    	}
    	setRelationalItemsChanged(true);
    	clearCachedSongDescription();
    }

    public void removeReleaseInstance(int uniqueId) {
    	try {
    		releaseInstancesSem.acquire(uniqueId);
    		getWriteLockSem().startRead("removeReleaseInstance");
	    	boolean found = false;
	    	int i = 0;
	    	while ((i < releaseIds.length) && !found) {
	    		if (releaseIds[i] == uniqueId)
	    			found = true;
	    		++i;
	    	}
	    	if (found) {
	    		int[] newReleaseIds = new int[releaseIds.length - 1];
	    		String[] newReleaseTracks = new String[releaseIds.length - 1];
	    		i = 0;
	    		for (int n = 0; n < newReleaseIds.length; ++n) {
	    			if (releaseIds[i] == uniqueId)
	    				++i;
	    			newReleaseIds[n] = releaseIds[i];
	    			newReleaseTracks[n] = releaseTracks[i];
	    			++i;
	    		}
	    		releaseIds = newReleaseIds;
	    		releaseTracks = newReleaseTracks;
	    	}
    	} catch (Exception e) {
    		log.error("removeReleaseInstance(): error", e);
    	} finally {
    		getWriteLockSem().endRead();
    		releaseInstancesSem.release(uniqueId);
    	}
    	setRelationalItemsChanged(true);
    	clearCachedSongDescription();
    }

    /**
     * Don't call directly (except in initProfile).  Call Profile.setTitle(...)
     */
    public void setTrack(String track) {
    	this.track = track;
    	clearCachedSongDescription();
    }

    /**
     * Don't call directly (except in initProfile).  Call Profile.setTitle(...)
     */
    public void setTitle(String title) {
    	this.title = title;
    	clearCachedSongDescription();
    }

    /**
     * Don't call directly (except in initProfile).  Call Profile.setRemix(...)
     */
    public void setRemix(String remix) {
    	this.remix = remix;
    	clearCachedSongDescription();
    }

    public void setLabelNames(Vector<String> labelNames) { setLabelNames(labelNames, true, true); }
	public void setLabelNames(Vector<String> labelNames, boolean updateReleaseLabels, boolean addSongsToLabels) {
		try {
			labelNames = StringUtil.removeDuplicatesIgnoreCase(labelNames);
			getWriteLockSem().startRead("setLabelNames");
			// remove from old labels
			if (labelIds != null) {
				for (int labelId : labelIds) {
					LabelProfile label = Database.getLabelIndex().getLabelProfile(labelId);
					if (label != null) {
						label.removeSong(getUniqueId());
						if (label.getNumSongs() == 0)
							Database.getLabelIndex().delete(label.getUniqueId());
					}
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
						} catch (AlreadyExistsException e) {
							log.debug("setLabelNames(): already exists=" + labelName);
						}
					}
				}
			} else {
				labelIds = new int[0];
			}
			if (addSongsToLabels) {
				for (int labelId : labelIds) {
					LabelProfile label = Database.getLabelIndex().getLabelProfile(labelId);
					if (label != null)
						label.addSong(getUniqueId());
				}
			}
			if (updateReleaseLabels) {
				if (getReleases().size() > 0)
					getReleases().get(0).setLabelNames(labelNames, false, true); // only set label on primary release
			}
		} catch (Exception e) {
			log.error("setLabelNames(): error", e);
		} finally {
			getWriteLockSem().endRead();
		}
		setRelationalItemsChanged(true);
	}

	public void setStartKey(Key key) {
		if (key != null) {
			this.startKeyRootValue = key.getRootValue();
			this.startKeyScaleType = key.getScaleType();
		}
	}
	public void setEndKey(Key key) {
		if (key != null) {
			this.endKeyRootValue = key.getRootValue();
			this.endKeyScaleType = key.getScaleType();
		}
	}
	public void setKeyAccuracy(byte keyAccuracy) {
		this.keyAccuracy = keyAccuracy;
	}

	public void setStartBpm(Bpm startBpm) {
		this.startBpm = startBpm.getBpmValue();
	}
	public void setEndBpm(Bpm endBpm) {
		this.endBpm = endBpm.getBpmValue();
	}
	public void setBpmAccuracy(byte bpmAccuracy) {
		this.bpmAccuracy = bpmAccuracy;
	}

	public void setTimeSig(TimeSig timeSig) {
		if (timeSig != null) {
			timeSigNumerator = timeSig.getNumerator();
			timeSigDenominator = timeSig.getDenominator();
		}
	}

	public void setBeatIntensity(BeatIntensity beatIntensity) {
		if (beatIntensity != null) {
			this.beatIntensity = beatIntensity.getBeatIntensityValue();
		}
	}

	public void setDuration(Duration duration) {
		if (duration != null) {
			this.timeInMillis = duration.getDurationInMillis();
		}
	}

	public void setSongFilename(String songFilename) {
		Database.getSongIndex().updateSongFilename(this.songFilename, songFilename, uniqueId);
		this.songFilename = songFilename;
		hasCheckedFileExists = false;
	}

	public void setOriginalYearReleased(short originalYearReleased) {
		this.originalYearReleased = originalYearReleased;
	}

	public void setNumMixouts(short numMixouts) {
		numMixouts += Byte.MIN_VALUE; // will subtract 128
		this.numMixouts = (byte)numMixouts;
	}

	@Override
	public void setId(Identifier id) {
		super.setId(id);
		clearCachedSongDescription();
	}

	@Override
	public void setPlayCount(long numPlays) {
		long diff = numPlays - getPlayCount();
		super.setPlayCount(numPlays);
		if (Database.getArtistIndex() != null)
			for (ArtistRecord artist : getArtists())
				artist.incrementPlayCount(diff);
		if (Database.getReleaseIndex() != null)
			for (ReleaseRecord release : getReleases())
				release.incrementPlayCount(diff, false);
		if (Database.getLabelIndex() != null)
			for (LabelRecord label : getLabels())
				label.incrementPlayCount(diff);
	}

	public void incrementPlayCount(long increment, boolean updateRelations) {
		super.incrementPlayCount(increment);
		if (updateRelations) {
			for (ArtistRecord artist : getArtists())
				artist.incrementPlayCount(increment);
			for (ReleaseRecord release : getReleases())
				release.incrementPlayCount(increment, false);
			for (LabelRecord label : getLabels())
				label.incrementPlayCount(increment);
		}
	}
	@Override
	public void incrementPlayCount(long increment) { incrementPlayCount(increment, true); }

	public void setTimbre(float[] timbre) { this.timbre = timbre; }
	public void setTimbreVariance(float[] timbreVariance) { this.timbreVariance = timbreVariance; }

	public void setFeaturingArtists(Vector<String> featuringArtistNames) {
		Vector<Integer> featuringArtistIdsVector = new Vector<Integer>();
		for (String featuringArtist : featuringArtistNames) {
			ArtistRecord artistRecord = Database.getArtistIndex().getArtistRecord(new ArtistIdentifier(featuringArtist));
			if (artistRecord == null) {
				try {
					SubmittedArtist submittedArtist = new SubmittedArtist(featuringArtist);
					ArtistProfile artistProfile = (ArtistProfile)Database.getArtistIndex().add(submittedArtist);
					if (artistProfile != null)
						artistRecord = artistProfile.getArtistRecord();
				} catch (Exception e) {
					log.error("initProfile(): error", e);
				}
			}
			if (artistRecord != null) {
				featuringArtistIdsVector.add(artistRecord.getUniqueId());
			}
		}
		featuringArtistIds = new int[featuringArtistIdsVector.size()];
		for (int c = 0; c < featuringArtistIdsVector.size(); ++c)
			featuringArtistIds[c] = featuringArtistIdsVector.get(c);
		clearCachedSongDescription();
	}
	public void setFeaturingArtistIds(int[] featuringArtistIds) {
		this.featuringArtistIds = featuringArtistIds;
		clearCachedSongDescription();
	}

	// for serialization
	public void setReleaseTracks(String[] releaseTracks) { this.releaseTracks = releaseTracks; }
	public void setStartKeyRootValue(float startKeyRootValue) { this.startKeyRootValue = startKeyRootValue; }
	public void setStartKeyScaleType(byte startKeyScaleType) { this.startKeyScaleType = startKeyScaleType; }
	public void setEndKeyRootValue(float endKeyRootValue) { this.endKeyRootValue = endKeyRootValue; }
	public void setEndKeyScaleType(byte endKeyScaleType) { this.endKeyScaleType = endKeyScaleType; }
	public void setTimeSigNumerator(byte timeSigNumerator) { this.timeSigNumerator = timeSigNumerator; }
	public void setTimeSigDenominator(byte timeSigDenominator) { this.timeSigDenominator = timeSigDenominator; }
	public void setTimeInMillis(int timeInMillis) { this.timeInMillis = timeInMillis; }
	public void setReleaseIds(int[] releaseIds) { this.releaseIds = releaseIds; }
	public void setLabelIds(int[] labelIds) { this.labelIds = labelIds; }
	public void setStartBpm(float startBpm) { this.startBpm = startBpm; }
	public void setEndBpm(float endBpm) { this.endBpm = endBpm; }
	public void setBeatIntensity(byte beatIntensity) { this.beatIntensity = beatIntensity; }
	//public void setNumMixouts(byte numMixouts) { this.numMixouts = numMixouts; }

    /////////////
    // METHODS //
    /////////////

	@Override
	public String toString() {
		return computeSongDescription();
	}

	public String computeSongDescription() {
		Vector<ReleaseInstance> releaseInstances = getReleaseInstances(false);
		if (releaseInstances.size() > 0) {
			ReleaseInstance releaseInstance = releaseInstances.get(0);
			ReleaseRecord release = releaseInstance.getRelease();
			if (release != null) {
				String track = getTrack();
				if (track.length() == 0)
					track = releaseInstance.getTrack();
				return SongIdentifier.toString(getArtistsDescription(), release.getReleaseTitle(), track, title, remix);
			}
		}
		return SongIdentifier.toString(getArtistsDescription(), "", getTrack(), title, remix);
	}
	public void clearCachedSongDescription() {
		//songDescription = null;
	}

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);
    	SongRecord songRecord = (SongRecord)record;
    	if (songRecord.hasValidSongFilename())
    		Database.getSongIndex().addDuplicateReference(songRecord.getSongFilename(), getUniqueId());
    	if (hasValidSongFilename() && songRecord.hasValidSongFilename()) {
    		File f1 = new File(getSongFilename());
    		File f2 = new File(songRecord.getSongFilename());
    		if (f2.exists() && !f1.exists())
    			setSongFilename(songRecord.getSongFilename());
    	} else if (!hasValidSongFilename()) {
    		setSongFilename(songRecord.getSongFilename());
    	}
    	// releases
    	int[] otherReleaseIds = songRecord.releaseIds;
    	if (otherReleaseIds != null) {
    		int t = 0;
	    	for (int otherReleaseId : otherReleaseIds) {
    			ReleaseProfile releaseProfile = (ReleaseProfile)Database.getReleaseIndex().getProfile(otherReleaseId);
    			if (releaseProfile != null) {
    				releaseProfile.addSong(this, songRecord.releaseTracks[t]);
    				releaseProfile.save();
    			}
	    		++t;
	    	}
    	}
    	// labels
    	int[] otherLabelIds = songRecord.labelIds;
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
    	// compilation flag
    	//if (!isPartOfCompilationRelease || !songRecord.isPartOfCompilationRelease)
    		//isPartOfCompilationRelease = false; // will assume for now if merged, the user would keep the non compilation version
    	// key
    	if (startKeyRootValue == Key.ROOT_UNKNOWN) {
    		startKeyRootValue = songRecord.startKeyRootValue;
    		startKeyScaleType = songRecord.startKeyScaleType;
    	}
    	if (endKeyRootValue == Key.ROOT_UNKNOWN) {
    		endKeyRootValue = songRecord.endKeyRootValue;
    		endKeyScaleType = songRecord.endKeyScaleType;
    	}
    	if (keyAccuracy == 0)
    		keyAccuracy = songRecord.keyAccuracy;
        // bpm
    	if (startBpm == 0.0f)
    		startBpm = songRecord.startBpm;
    	if (endBpm == 0.0f)
    		endBpm = songRecord.endBpm;
    	if (bpmAccuracy == 0)
    		bpmAccuracy = songRecord.bpmAccuracy;
        // time signature
    	if ((songRecord.timeSigNumerator != 4) || (songRecord.timeSigDenominator != 4)) {
    		timeSigNumerator = songRecord.timeSigNumerator;
    		timeSigDenominator = songRecord.timeSigDenominator;
    	}
    	// beat intensity
    	if (beatIntensity == 0)
    		beatIntensity = songRecord.beatIntensity;
    	if (timeInMillis == 0)
    		timeInMillis = songRecord.timeInMillis;
    	if ((songFilename == null) || (songFilename.length() == 0))
    		songFilename = songRecord.songFilename;
    	// original year released
    	if ((originalYearReleased != 0) && (songRecord.originalYearReleased != 0))
    		originalYearReleased = (short)Math.min(originalYearReleased, songRecord.originalYearReleased);
    	else if (songRecord.originalYearReleased != 0)
    		originalYearReleased = songRecord.originalYearReleased;
    }

    @Override
	public void write(LineWriter textWriter) {
    	super.write(textWriter);
    	textWriter.writeLine(2); //version
    	textWriter.writeLine(track);
    	textWriter.writeLine(title);
    	textWriter.writeLine(remix);
    	if (releaseIds != null) {
    		textWriter.writeLine(releaseIds.length);
    		for (int releaseId : releaseIds)
    			textWriter.writeLine(releaseId);
    		for (String releaseTrack : releaseTracks)
    			textWriter.writeLine(releaseTrack);
    	} else {
    		textWriter.writeLine(0);
    	}
    	if (labelIds != null) {
    		textWriter.writeLine(labelIds.length);
    		for (int labelId : labelIds)
    			textWriter.writeLine(labelId);
    	} else {
    		textWriter.writeLine(0);
    	}
    	textWriter.writeLine(startKeyRootValue);
    	textWriter.writeLine(startKeyScaleType);
    	textWriter.writeLine(endKeyRootValue);
    	textWriter.writeLine(endKeyScaleType);
    	textWriter.writeLine(keyAccuracy);
    	textWriter.writeLine(startBpm);
    	textWriter.writeLine(endBpm);
    	textWriter.writeLine(bpmAccuracy);
    	textWriter.writeLine(timeSigNumerator);
    	textWriter.writeLine(timeSigDenominator);
    	textWriter.writeLine(beatIntensity);
    	textWriter.writeLine(timeInMillis);
    	textWriter.writeLine(songFilename);
    	textWriter.writeLine(originalYearReleased);
    	textWriter.writeLine(numMixouts);
    	if (timbre != null) {
    		textWriter.writeLine(timbre.length);
    		for (float value : timbre)
    			textWriter.writeLine(value);
    		for (float value : timbreVariance)
    			textWriter.writeLine(value);
    	} else {
    		textWriter.writeLine(0);
    	}
        if (featuringArtistIds != null) {
    		textWriter.writeLine(featuringArtistIds.length);
    		for (int value : featuringArtistIds)
    			textWriter.writeLine(value);
        } else {
        	textWriter.writeLine(0);
        }
    }

    @Override
	public void addFieldsToDocument(Document document) {
    	super.addFieldsToDocument(document);
    	Field artistField = new Field("artist", getArtistsDescription(), Field.Store.NO, Field.Index.ANALYZED);
    	artistField.setBoost(RE3Properties.getFloat("artist_field_boost"));
    	document.add(artistField);
    	Field featField = new Field("featuring", this.getFeaturingArtistsDescription(), Field.Store.NO, Field.Index.ANALYZED);
    	featField.setBoost(RE3Properties.getFloat("artist_field_boost"));
    	document.add(featField);
    	Field releaseField = new Field("release", getReleaseTitle(), Field.Store.NO, Field.Index.ANALYZED);
    	releaseField.setBoost(RE3Properties.getFloat("release_field_boost"));
    	document.add(releaseField);
    	document.add(new Field("track", getTrack(), Field.Store.NO, Field.Index.ANALYZED));
    	StringBuffer releaseInstances = new StringBuffer();
    	for (ReleaseInstance releaseInstance : getReleaseInstances()) {
    		ReleaseRecord release = releaseInstance.getRelease();
    		if (release != null) {
    			if (releaseInstances.length() > 0)
    				releaseInstances.append("; ");
    			releaseInstances.append(release.getReleaseTitle());
    			releaseInstances.append(" [");
    			releaseInstances.append(releaseInstance.getTrack());
    			releaseInstances.append("]");
    		}
    	}
    	if (releaseInstances.length() > 0)
    		document.add(new Field("release_instances", releaseInstances.toString(), Field.Store.NO, Field.Index.ANALYZED));
    	Field titleField = new Field("title", getTitle(), Field.Store.NO, Field.Index.ANALYZED);
    	titleField.setBoost(RE3Properties.getFloat("title_field_boost"));
    	document.add(titleField);
    	Field remixField = new Field("remix", getRemix(), Field.Store.NO, Field.Index.ANALYZED);
    	remixField.setBoost(RE3Properties.getFloat("title_field_boost"));
    	document.add(remixField);
    	document.add(new Field("label", getLabelsDescription(), Field.Store.NO, Field.Index.ANALYZED));
    	document.add(new Field("key", getKey().toString(), Field.Store.NO, Field.Index.ANALYZED));
    	document.add(new Field("bpm", getBpm().toString(), Field.Store.NO, Field.Index.ANALYZED));
    	document.add(new Field("timesig", getTimeSig().toString(), Field.Store.NO, Field.Index.ANALYZED));
    	document.add(new Field("filename", getSongFilename() != null ? getSongFilename() : "", Field.Store.NO, Field.Index.ANALYZED));
    	document.add(new Field("year", this.getOriginalYearReleasedAsString(), Field.Store.NO, Field.Index.ANALYZED));
    }

    @Override
	public JSONObject getJSON(ModelManagerInterface modelManager) {
    	JSONObject result = super.getJSON(modelManager);
    	try {
	    	boolean isVideo = VideoUtil.isSupportedVideoFileType(getSongFilename());
			result.put("is_video", isVideo);
			String src = "media?id=" + getUniqueId();
			result.put("src", src);
    	} catch (Exception e) {
    		log.error("getJSON(): error", e);
    	}
		return result;
    }

}
