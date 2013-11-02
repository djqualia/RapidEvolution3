package com.mixshare.rapid_evolution.data.index.search.song;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.exceptions.UnknownErrorException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.ReleaseInstance;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.record.user.UserData;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.artist.SubmittedArtist;
import com.mixshare.rapid_evolution.data.submitted.search.label.SubmittedLabel;
import com.mixshare.rapid_evolution.data.submitted.search.release.SubmittedRelease;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.model.search.song.RecommendedSongModelManager;
import com.mixshare.rapid_evolution.ui.model.search.song.SongModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;

public class SongIndex extends SearchIndex {

    static private Logger log = Logger.getLogger(SongIndex.class);
    static private final long serialVersionUID = 0L;

    ////////////
    // FIELDS //
    ////////////

    private Map<String, Integer> filenameMap = new HashMap<String, Integer>();

    transient private RWSemaphore filenameMapSem = new RWSemaphore(-1);

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SongIndex.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("filenameMapSem")) {
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

	public SongIndex() {
		super();
	}

	public SongIndex(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
		if (version >= 2) {
			int numFilenames = Integer.parseInt(lineReader.getNextLine());
			filenameMap = new HashMap<String, Integer>(numFilenames);
			for (int i = 0; i < numFilenames; ++i) {
				String key = lineReader.getNextLine();
				Integer value = Integer.parseInt(lineReader.getNextLine());
				filenameMap.put(key, value);
			}
		}

	}

    /////////////
    // GETTERS //
    /////////////

	@Override
	public byte getDataType() { return DATA_TYPE_SONGS; }
	public SongModelManager getSongModelManager() { return (SongModelManager)modelManager; }

	@Override
	protected Profile getNewProfile(SubmittedProfile profile, int fileId) { return new SongProfile((SongIdentifier)profile.getIdentifier(), fileId); }

	public SongRecord getSongRecord(int uniqueId) { return (SongRecord)getRecord(uniqueId); }
	public SongRecord getSongRecord(Identifier id) { return (SongRecord)getRecord(id); }
	public SongRecord getSongRecord(String filename) {
		Integer songId = filenameMap.get(FileUtil.unify(filename));
		if (songId != null)
			return getSongRecord(songId);
		return null;
	}

	public SongProfile getSongProfile(int uniqueId) { return (SongProfile)getProfile(uniqueId); }
	public SongProfile getSongProfile(Identifier id) { return (SongProfile)getProfile(id); }
	public SongProfile getSongProfile(String filename) {
		Integer songId = filenameMap.get(FileUtil.unify(filename));
		if (songId != null)
			return getSongProfile(songId);
		return null;
	}

	public RWSemaphore getFilenameMapSem() {
		if (filenameMapSem == null)
			filenameMapSem = new RWSemaphore(60000);
		return filenameMapSem;
	}

	@Override
	public SearchParameters getNewSearchParameters() { return new SongSearchParameters(); }

	// for serialization
	public Map<String, Integer> getFilenameMap() { return filenameMap; }

	public RecommendedSongModelManager getRecommendedSongModelManager() { return (RecommendedSongModelManager)recommendedModelManager; }

	/////////////
	// SETTERS //
	/////////////

	// for serialization
	public void setSongModelManager(SongModelManager modelManager) { this.modelManager = modelManager; }
	public void setFilenameMap(Map<String, Integer> filenameMap) { this.filenameMap = filenameMap; }
	public void setFilenameMapSem(RWSemaphore filenameMapSem) { this.filenameMapSem = filenameMapSem; }
	public void setRecommendedSongModelManager(RecommendedSongModelManager recommendedModelManager) { this.recommendedModelManager = recommendedModelManager; }

	/////////////
	// METHODS //
	/////////////

	@Override
	public void propertiesChanged() {
		SongProfile.loadProperties();
	}

	public SongProfile addSong(SubmittedSong submittedSong) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		return (SongProfile)add(submittedSong);
	}

	@Override
	public ModelManagerInterface createModelManager() {
		SongModelManager result = new SongModelManager();
		result.initColumns();
		return result;
	}

	@Override
	protected SearchModelManager createRecommendedModelManager() {
		RecommendedSongModelManager result = new RecommendedSongModelManager();
		result.initColumns();
		return result;
	}

	@Override
	protected void initProfile(Profile profile, SubmittedProfile initialValues) {
		super.initProfile(profile, initialValues);
		SongProfile songProfile = (SongProfile)profile;
		SubmittedSong submittedSong = (SubmittedSong)initialValues;
		if (submittedSong.getLabelNames() != null)
			songProfile.setInitialLabelNames(submittedSong.getLabelNames());
		songProfile.getSongRecord().setTrack(submittedSong.getTrack());
		songProfile.getSongRecord().setTitle(submittedSong.getTitle());
		songProfile.getSongRecord().setRemix(submittedSong.getRemix());
		if ((submittedSong.getStartKey() != null) && submittedSong.getStartKey().isValid())
			songProfile.setKey(submittedSong.getStartKey(), submittedSong.getEndKey(), submittedSong.getKeyAccuracy(), submittedSong.getKeySource());
		if ((submittedSong.getStartBpm() != null) && submittedSong.getStartBpm().isValid())
			songProfile.setBpm(submittedSong.getStartBpm(), submittedSong.getEndBpm(), submittedSong.getBpmAccuracy(), submittedSong.getBpmSource());
		if ((submittedSong.getTimeSig() != null) && submittedSong.getTimeSig().isValid())
			songProfile.setTimeSig(submittedSong.getTimeSig(), submittedSong.getTimeSigSource());
		if ((submittedSong.getBeatIntensity() != null) && (submittedSong.getBeatIntensity().isValid()))
			songProfile.setBeatIntensity(submittedSong.getBeatIntensity(), submittedSong.getBeatIntensitySource());
		if ((submittedSong.getDuration() != null) && submittedSong.getDuration().isValid())
			songProfile.setDuration(submittedSong.getDuration(), submittedSong.getDurationSource());
		songProfile.setSongFilename(submittedSong.getSongFilename());
		if (submittedSong.getOriginalYearReleased() != 0)
			songProfile.setOriginalYearReleased(submittedSong.getOriginalYearReleased(), submittedSong.getOriginalYearReleasedSource());
		songProfile.setReplayGain(submittedSong.getReplayGain(), submittedSong.getReplayGainSource());
		songProfile.setITunesID(submittedSong.getITunesID());
		songProfile.setSyncedWithMixshare(submittedSong.isSyncedWithMixshare());
		songProfile.setLyrics(submittedSong.getLyrics(), submittedSong.getLyricsSource());
		songProfile.setSongFileLastUpdated(submittedSong.getSongFilenameLastUpdated());
		Vector<UserData> userDataValues = submittedSong.getUserData();
		for (UserData userData : userDataValues) {
			songProfile.setUserData(userData);
		}
		if (submittedSong.getSubmittedRelease() != null) {
			int releaseId = Database.getReleaseIndex().getUniqueIdFromIdentifier(submittedSong.getSubmittedRelease().getReleaseIdentifier());
			songProfile.getSongRecord().addReleaseInstance(new ReleaseInstance(releaseId, submittedSong.getTrack()), true);
		}
		if (submittedSong.getPlayCount() > 0)
			songProfile.setPlayCount(submittedSong.getPlayCount());
		for (PlaylistRecord playlist: submittedSong.getPlaylists()) {
			playlist.addSong(songProfile.getUniqueId());
			playlist.update();
		}
		songProfile.getSongRecord().setFeaturingArtists(submittedSong.getFeaturingArtists());
	}

	private void checkUniqueness(SubmittedSong submittedSong) {
		if (!RE3Properties.getBoolean("prevent_duplicates_from_being_added") && (submittedSong.getSongFilename() != null) && (submittedSong.getSongFilename().length() > 0)) {
			SongRecord song = getSongRecord(submittedSong.getSongFilename());
			if (song == null) {
				// there doesn't exist a song with this filename yet...
				SongIdentifier submittedId = (SongIdentifier)submittedSong.getIdentifier();
				String originalTitle = submittedSong.getTitle();
				int count = 2;
				SongRecord existingSong = getSongRecord(submittedId);
				while ((existingSong != null) && !existingSong.isExternalItem()) {
					// but a non-external song with the same id already exists
					// we will append and increment a number in the title until it is unique...
					String newTitle = null;
					if (originalTitle.length() > 0)
						newTitle = originalTitle + " (" + count + ")";
					else
						newTitle = Translations.get("song_untitled") + " (" + count + ")";
					submittedSong.setTitle(newTitle);
					submittedId = submittedSong.getSongIdentifier();
					existingSong = getSongRecord(submittedId);
					++count;
				}
			}
		}
	}

	@Override
	public Profile add(SubmittedProfile submittedProfile) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		if ((RE3Properties.getInt("max_external_songs") == 0) && ((SubmittedSong)submittedProfile).isExternalItem())
			return null;
		checkUniqueness((SubmittedSong)submittedProfile);
		SongProfile result = (SongProfile)super.add(submittedProfile);
		String filename = result.getSongFilename();
		if ((filename != null) && (filename.length() > 0)) {
			try {
				getFilenameMapSem().startRead("add");
				filenameMap.put(FileUtil.unify(filename), result.getUniqueId());
			} catch(Exception e) { } finally {
				getFilenameMapSem().endRead();
			}
		}
		return result;
	}

	public void addDuplicateReference(String filename, int uniqueId) {
		try {
			getFilenameMapSem().startRead("addDuplicateReference");
			filenameMap.put(FileUtil.unify(filename), uniqueId);
		} catch(Exception e) { } finally {
			getFilenameMapSem().endRead();
		}
	}

	public void updateSongFilename(String fromFilename, String toFilename, int uniqueId) {
		try {
			getFilenameMapSem().startRead("addDuplicateReference");
			filenameMap.remove(FileUtil.unify(fromFilename));
			filenameMap.put(FileUtil.unify(toFilename), uniqueId);
		} catch(Exception e) { } finally {
			getFilenameMapSem().endRead();
		}
	}

	@Override
	public boolean delete(Integer id) {
		SongRecord songRecord = getSongRecord(id);
		if (songRecord != null) {
			String filename = songRecord.getSongFilename();
			if ((filename != null) && (filename.length() > 0)) {
				int mappedId = -1;
				try {
					getFilenameMapSem().startWrite("delete");
					mappedId = filenameMap.remove(FileUtil.unify(filename));
					// remove any alternate filename mappings, from merges, etc
					Vector<String> removedKeys = new Vector<String>();
					for (Entry<String, Integer> entrySet : filenameMap.entrySet()) {
						if (entrySet.getValue() == id)
							removedKeys.add(entrySet.getKey());
						else {
							for (int d = 0; d < songRecord.getNumDuplicateIds(); ++d) {
								if (entrySet.getValue() == songRecord.getDuplicateId(d))
									removedKeys.add(entrySet.getKey());
							}
						}
					}
					for (String removedKey : removedKeys)
						filenameMap.remove(removedKey);
				} catch(Exception e) { } finally {
					getFilenameMapSem().endWrite();
				}
				if (RE3Properties.getBoolean("delete_actual_song_filenames") && (mappedId == id)) {
					File file = new File(filename);
					file.delete();
				}
			}
		}
		return super.delete(id);
	}

	@Override
	public void update(Record record) {
		super.update(record);
		try {
			getFilenameMapSem().startWrite("update");
			Iterator<Entry<String, Integer>> iter = filenameMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Integer> entry = iter.next();
				if (entry.getValue() == record.getUniqueId()) {
					iter.remove();
					break;
				}
			}
			String filename = ((SongRecord)record).getSongFilename();
			if ((filename != null) && (filename.length() > 0))
				filenameMap.put(FileUtil.unify(filename), record.getUniqueId());
		} catch (Exception e) {
			log.error("update(): error", e);
		} finally {
			getFilenameMapSem().endWrite();
		}
	}

	@Override
	public Profile addOrUpdate(SubmittedProfile submittedProfile) throws InsufficientInformationException, UnknownErrorException {
		try {
			SubmittedSong submittedSong = (SubmittedSong)submittedProfile;
			String filename = submittedSong.getSongFilename();
			Integer uniqueId = null;
			if ((filename != null) && (filename.length() > 0))
				uniqueId = filenameMap.get(FileUtil.unify(filename));
			checkUniqueness((SubmittedSong)submittedProfile);
			SongProfile profile = null;
			if (uniqueId != null) {
				profile = getSongProfile(uniqueId);
				if (profile != null)
					submittedSong.setIdentifier(profile.getSongIdentifier());
				else
					profile = getSongProfile(submittedProfile.getIdentifier());
			} else {
				profile = getSongProfile(submittedProfile.getIdentifier());
			}
			if (profile == null) {
				try {
					return add(submittedProfile);
				} catch (AlreadyExistsException ae) {
					// try again (maybe it was just created in another thread)
					profile = getSongProfile(submittedProfile.getIdentifier());
				} catch (Exception e) {
					log.error("addOrUpdate(): error", e);
				}
			}
			if (profile != null) {
				profile.update(submittedProfile, false);
				profile.save();
			}
			return profile;
		} catch (Exception e) {
			log.error("addOrUpdate(): error", e);
		}
		return null;
	}

	/**
	 * When a song is added, add/update the related artist/release/label profiles...
	 */
	@Override
	protected void addRelationalItems(Record addedRecord) {
		super.addRelationalItems(addedRecord);
		// note: insert code after call to super
		SongRecord songRecord = (SongRecord)addedRecord;
		SongIdentifier songId = (SongIdentifier)songRecord.getIdentifier();

		if (log.isTraceEnabled())
			log.trace("addRelationalItems(): updating song artists");
		Vector<ArtistProfile> updatedArtistProfiles = new Vector<ArtistProfile>();
		for (String artistName : songId.getArtistNames()) {
			try {
				if ((artistName != null) && (artistName.length() > 0)) {
					ArtistIdentifier artistId = new ArtistIdentifier(artistName);
					if (artistId.isValid()) {
						ArtistProfile artistProfile = (ArtistProfile)Database.getArtistIndex().getProfile(artistId);
	    				if (artistProfile == null) {
	    					SubmittedArtist newArtist = new SubmittedArtist(artistName);
	    					newArtist.setInitialSongId(songId);
	    					newArtist.setExternalItem(songRecord.isExternalItem());
	    					updatedArtistProfiles.add((ArtistProfile)Database.getArtistIndex().addOrUpdate(newArtist));
	    				} else {
    						artistProfile.addSong(songRecord.getUniqueId(), false);
    						if (!songRecord.isExternalItem() && artistProfile.isExternalItem()) {
    							artistProfile.setExternalItem(false);
    							artistProfile.setDisabled(false);
    						}
    						updatedArtistProfiles.add(artistProfile);
	    				}
					}
				}
			} catch (Exception e) {
				log.error("addRelationalItems(): error", e);
			}
		}

		if (log.isTraceEnabled())
			log.trace("addRelationalItems(): updating song labels");
		for (String labelName : songRecord.getSourceLabelNames()) {
			try {
				LabelIdentifier labelId = new LabelIdentifier(labelName);
				if (labelId.isValid()) {
					LabelProfile labelProfile = (LabelProfile)Database.getLabelIndex().getProfile(labelId);
					if (labelProfile == null) {
						SubmittedLabel newLabel = new SubmittedLabel(labelName);
						newLabel.setInitialSongId(songId);
						newLabel.setExternalItem(songRecord.isExternalItem());
						Database.getLabelIndex().addOrUpdate(newLabel);
					} else {
						labelProfile.addSong(songRecord.getUniqueId());
						if (!songRecord.isExternalItem() && labelProfile.isExternalItem()) {
							labelProfile.setExternalItem(false);
							labelProfile.setDisabled(false);
						}
						labelProfile.save();
					}
				}
			} catch (Exception e) {
				log.error("addRelationalItems(): error", e);
			}
		}

		if (log.isTraceEnabled())
			log.trace("addRelationalItems(): recomputing artist metadata");
		// must recompute metadata on artists now that associated labels exist (for label degrees to be accurate)
		for (ArtistProfile artistProfile : updatedArtistProfiles) {
			if (artistProfile != null) {
				artistProfile.computeMetadataFromSongs();
				artistProfile.save();
			}
		}

		if (log.isTraceEnabled())
			log.trace("addRelationalItems(): updating song releases");
		for (ReleaseInstance releaseInstance : songRecord.getReleaseInstances()) {
			try {
				ReleaseIdentifier releaseId = (ReleaseIdentifier)Database.getReleaseIndex().getIdentifierFromUniqueId(releaseInstance.getReleaseId());
				if (releaseId != null) {
					ReleaseProfile releaseProfile = (ReleaseProfile)Database.getReleaseIndex().getProfile(releaseId);
					if (releaseProfile == null) {
						SubmittedRelease newRelease = new SubmittedRelease(releaseId);
						newRelease.setInitialSongId(songId);
						newRelease.setLabelNames(songRecord.getSourceLabelNames());
						newRelease.setOriginalYearReleased(songRecord.getOriginalYearReleased(), DATA_SOURCE_COMPUTED);
						newRelease.setExternalItem(songRecord.isExternalItem());
						try {
							if (songRecord.hasThumbnail())
								newRelease.addImage(new Image(songRecord.getThumbnailImageFilename(), songRecord.getThumbnailImageFilename(), DATA_SOURCE_COMPUTED), true);
						} catch (InvalidImageException iie) { }
						Database.getReleaseIndex().addOrUpdate(newRelease);
					} else {
						releaseProfile.addSong(songRecord.getUniqueId());
		        		if (releaseProfile.getOriginalYearReleased() == 0)
		        			releaseProfile.setOriginalYearReleased(songRecord.getOriginalYearReleased(), DATA_SOURCE_COMPUTED);
		        		if (releaseProfile.getLabels().size() == 0)
		        			releaseProfile.setLabelNames(songRecord.getSourceLabelNames());
		        		try {
		        			if (!releaseProfile.hasThumbnail() && songRecord.hasThumbnail())
		        				releaseProfile.addImage(new Image(songRecord.getThumbnailImageFilename(), songRecord.getThumbnailImageFilename(), DATA_SOURCE_COMPUTED));
		        		} catch (InvalidImageException iie) { }
						if (!songRecord.isExternalItem() && releaseProfile.isExternalItem()) {
							releaseProfile.setExternalItem(false);
							releaseProfile.setDisabled(false);
						}
						releaseProfile.save();
					}
				}
			} catch (Exception e) {
				log.error("addRelationalItems(): error", e);
			}
		}

	}

	/**
	 * When a song is removed, remove/update the related artist/release/label profiles...
	 */
	@Override
	protected void removeRelationalItems(Record removedRecord, boolean deleteRecords) {
		SongRecord songRecord = (SongRecord)removedRecord;
		SongIdentifier songId = (SongIdentifier)songRecord.getIdentifier();
		if (log.isTraceEnabled())
			log.trace("removeRelationalItems(): updating associated releases for song=" + songRecord);
		for (ReleaseRecord releaseRecord : songRecord.getReleases()) {
			if (log.isTraceEnabled())
				log.trace("removeRelationalItems(): updating release=" + releaseRecord);
			ReleaseProfile profile = (ReleaseProfile)Database.getReleaseIndex().getProfile(releaseRecord.getUniqueId());
			if (profile != null) {
				profile.removeSong(songRecord.getUniqueId());
				if (profile.getNumSongs() == 0) {
					if (deleteRecords)
						Database.getReleaseIndex().delete(releaseRecord.getUniqueId());
				} else {
					profile.save();
				}
			} else {
				Database.getReleaseIndex().delete(releaseRecord.getUniqueId());
			}
		}
		for (String artistName : songId.getArtistNames()) {
			ArtistIdentifier artistId = new ArtistIdentifier(artistName);
			ArtistProfile profile = (ArtistProfile)Database.getArtistIndex().getProfile(artistId);
			if (profile != null) {
				profile.removeSong(songRecord.getUniqueId());
				if (profile.getNumSongs() == 0) {
					if (deleteRecords)
						Database.getArtistIndex().delete(artistId);
				} else {
					profile.save();
				}
			} else {
				Database.getArtistIndex().delete(artistId);
			}
		}
		for (String labelName : songRecord.getSourceLabelNames()) {
			LabelIdentifier labelId = new LabelIdentifier(labelName);
			LabelProfile profile = (LabelProfile)Database.getLabelIndex().getProfile(labelId);
			if (profile != null) {
				profile.removeSong(songRecord.getUniqueId());
				if (profile.getNumSongs() == 0) {
					if (deleteRecords)
						Database.getLabelIndex().delete(labelId);
				} else {
					profile.save();
				}
			} else {
				Database.getLabelIndex().delete(labelId);
			}
		}
		// note: insert code before call to super
		super.removeRelationalItems(removedRecord, deleteRecords);
	}

	@Override
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(2); // version
		writer.writeLine(filenameMap.size());
		for (Entry<String, Integer> entry : filenameMap.entrySet()) {
			writer.writeLine(entry.getKey());
			writer.writeLine(entry.getValue());
		}
	}

}
