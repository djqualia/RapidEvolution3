package com.mixshare.rapid_evolution.data.index.search.release;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.exceptions.UnknownErrorException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.artist.SubmittedArtist;
import com.mixshare.rapid_evolution.data.submitted.search.label.SubmittedLabel;
import com.mixshare.rapid_evolution.data.submitted.search.release.SubmittedRelease;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.model.search.release.RecommendedReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.search.release.ReleaseModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class ReleaseIndex extends SearchIndex {

    static private Logger log = Logger.getLogger(ReleaseIndex.class);
    static private final long serialVersionUID = 0L;    
	
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public ReleaseIndex() {
		super();
	}
	public ReleaseIndex(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}
	
	
    /////////////
    // GETTERS //
    /////////////
	
	public byte getDataType() { return DATA_TYPE_RELEASES; }
	
	public ReleaseModelManager getReleaseModelManager() { return (ReleaseModelManager)modelManager; }
	
	protected Profile getNewProfile(SubmittedProfile profile, int fileId) { return new ReleaseProfile((ReleaseIdentifier)profile.getIdentifier(), fileId); }
	
	public ReleaseRecord getReleaseRecord(int uniqueId) { return (ReleaseRecord)getRecord(uniqueId); } 
	public ReleaseRecord getReleaseRecord(Identifier id) { return (ReleaseRecord)getRecord(id); } 

	public ReleaseProfile getReleaseProfile(int uniqueId) { return (ReleaseProfile)getProfile(uniqueId); } 
	public ReleaseProfile getReleaseProfile(Identifier id) { return (ReleaseProfile)getProfile(id); } 

	public RecommendedReleaseModelManager getRecommendedReleaseModelManager() { return (RecommendedReleaseModelManager)recommendedModelManager; }
	
	public SearchParameters getNewSearchParameters() { return new ReleaseSearchParameters(); }
	
	/////////////
	// SETTERS //
	/////////////
	
	// for serialization
	public void setReleaseModelManager(ReleaseModelManager modelManager) { this.modelManager = modelManager; }
	public void setRecommendedReleaseModelManager(RecommendedReleaseModelManager recommendedModelManager) { this.recommendedModelManager = recommendedModelManager; }
	
	/////////////
	// METHODS //
	/////////////

	public void propertiesChanged() {
		ReleaseProfile.loadProperties();
	}	
	
	public ReleaseProfile addRelease(SubmittedRelease submittedRelease) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		return (ReleaseProfile)add(submittedRelease);
	}
	
	public Profile add(SubmittedProfile submittedProfile) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		if ((RE3Properties.getInt("max_external_releases") == 0) && ((SubmittedRelease)submittedProfile).isExternalItem())
			return null;	
		return super.add(submittedProfile);
	}
	
	public ModelManagerInterface createModelManager() {
		ReleaseModelManager result = new ReleaseModelManager();
		result.initColumns();
		return result;
	}
	
	protected SearchModelManager createRecommendedModelManager() {
		RecommendedReleaseModelManager result = new RecommendedReleaseModelManager();
		result.initColumns();
		return result;
	}
	
	protected void initProfile(Profile profile, SubmittedProfile initialValues) {
		super.initProfile(profile, initialValues);
		ReleaseProfile releaseProfile = (ReleaseProfile)profile;
		SubmittedRelease submittedRelease = (SubmittedRelease)initialValues;
		if (submittedRelease.getInitialSongId() != null)
			releaseProfile.addSong(Database.getSongIndex().getUniqueIdFromIdentifier(submittedRelease.getInitialSongId()));
		if (submittedRelease.getLabelNames() != null)
			releaseProfile.setInitialLabelNames(submittedRelease.getLabelNames());
		if (submittedRelease.getOriginalYearReleased() != 0)
			releaseProfile.setOriginalYearReleased(submittedRelease.getOriginalYearReleased(), submittedRelease.getOriginalYearReleasedSource());
	}
	
	public void mergeProfiles(Profile primaryProfile, Profile mergedProfile) {
		ReleaseProfile primaryRelease = (ReleaseProfile)primaryProfile;
		ReleaseProfile mergedRelease = (ReleaseProfile)mergedProfile;	
		int[] primaryArtistIds = primaryRelease.getReleaseIdentifier().getArtistIds();
		int[] mergedArtistIds = mergedRelease.getReleaseIdentifier().getArtistIds();
		Map<Integer, Object> artistIds = new HashMap<Integer, Object>(primaryArtistIds.length + mergedArtistIds.length);
		for (int primaryArtistId : primaryArtistIds)
			if (!artistIds.containsKey(primaryArtistId))
				artistIds.put(primaryArtistId, null);
		for (int mergedArtistId : mergedArtistIds)
			if (!artistIds.containsKey(mergedArtistId))
				artistIds.put(mergedArtistId, null);
		Vector<Integer> primarySongIds = primaryRelease.getSongIds();
		Vector<Integer> mergedSongIds = mergedRelease.getSongIds();
		super.mergeProfiles(primaryProfile, mergedProfile);
		if (!primaryRelease.isCompilationRelease()) {
			try {
				Vector<String> artistNames = new Vector<String>(artistIds.size());
				for (int artistId : artistIds.keySet()) {
					ArtistRecord artistRecord = Database.getArtistIndex().getArtistRecord(artistId);
					if (artistRecord != null) {
						String artistName = artistRecord.getArtistName();
						if (!artistNames.contains(artistName))
							artistNames.add(artistName);
					}
				}
				primaryRelease.setArtistNames(artistNames, false);
			} catch (Exception e) {
				log.error("mergeProfiles(): error", e);
			}
		}
		// update song descriptions of merged songs for songs that rely on album/track for identification...
		try {
			for (int songId : mergedSongIds) {
				SongRecord mergedSong = Database.getSongIndex().getSongRecord(songId);
				if (mergedSong != null) {
					if ((mergedSong.getTitle() == null) || (mergedSong.getTitle().equals(""))) {
						SongProfile mergedSongProfile = Database.getSongIndex().getSongProfile(songId);
						mergedSongProfile.setReleaseTitle(primaryRelease.getReleaseTitle(), true);
					}
				}
				
			}			
		} catch (Exception e) {
			log.error("mergeProfiles(): error", e);
		}
		// check for songs that are now obviously duplicates
		try {
			for (int songId : primarySongIds) {
				if (RapidEvolution3.isTerminated)
					return;
				SongRecord primarySong = Database.getSongIndex().getSongRecord(songId);
				if (primarySong != null) {
					boolean found = false;
					int i = 0;
					while ((i < mergedSongIds.size()) && !found) {
						SongRecord mergedSong = Database.getSongIndex().getSongRecord(mergedSongIds.get(i));
						if (mergedSong != null) {
							if (mergedSong.getUniqueId() != primarySong.getUniqueId()) {
								if (primarySong.getSongDescription().equalsIgnoreCase(mergedSong.getSongDescription()) &&
										primarySong.getArtistsDescription().equalsIgnoreCase(mergedSong.getArtistsDescription())) {
									found = true;
									SongProfile primarySongProfile = Database.getSongIndex().getSongProfile(songId);
									SongProfile mergedSongProfile = Database.getSongIndex().getSongProfile(mergedSongIds.get(i));
									Database.mergeProfiles(primarySongProfile, mergedSongProfile);
								}
							}
						}
						++i;
					}
				}
			}
		} catch (Exception e) {
			log.error("mergeProfiles(): error", e);
		}		
		
	}
	
	protected void addRelationalItems(Record addedRecord) {		
		super.addRelationalItems(addedRecord);
		// note: insert code after call to super
		ReleaseRecord releaseRecord = (ReleaseRecord)addedRecord;
		ReleaseIdentifier releaseId = (ReleaseIdentifier)releaseRecord.getIdentifier();
		for (String artistName : releaseId.getArtistNames()) {
			try {
				if ((artistName != null) && (artistName.length() > 0)) {
					ArtistIdentifier artistId = new ArtistIdentifier(artistName);
					ArtistProfile artistProfile = (ArtistProfile)Database.getArtistIndex().getProfile(artistId);
					if (artistProfile == null) {
						SubmittedArtist submittedArtist = new SubmittedArtist(artistName);
						submittedArtist.setExternalItem(releaseRecord.isExternalItem());
						artistProfile = (ArtistProfile)Database.getArtistIndex().add(submittedArtist);
					}
					if (artistProfile != null) {
						artistProfile.addRelease(releaseRecord.getUniqueId());
						artistProfile.save();
					}
				}
			} catch (Exception e) {
				log.error("addRelationalItems(): error", e);
			}
		}
		for (String labelName : releaseRecord.getSourceLabelNames()) {
			try {
				LabelIdentifier labelId = new LabelIdentifier(labelName);
				if (labelId.isValid()) {
					LabelProfile labelProfile = (LabelProfile)Database.getLabelIndex().getProfile(labelId);
					if (labelProfile == null) {
						SubmittedLabel newLabel = new SubmittedLabel(labelName);
						newLabel.setExternalItem(releaseRecord.isExternalItem());
						labelProfile = (LabelProfile)Database.getLabelIndex().add(newLabel);												
					}
					labelProfile.addRelease(releaseRecord.getUniqueId());
					labelProfile.save();					
				}
			} catch (Exception e) {
				log.error("addRelationalItems(): error", e);
			}
		}
		
	}	
	protected void removeRelationalItems(Record removedRecord, boolean deleteRecords) {
		ReleaseRecord releaseRecord = (ReleaseRecord)removedRecord;
		ReleaseIdentifier releaseId = (ReleaseIdentifier)releaseRecord.getIdentifier();
		for (String artistName : releaseId.getArtistNames()) {
			ArtistIdentifier artistId = new ArtistIdentifier(artistName);
			ArtistProfile profile = (ArtistProfile)Database.getArtistIndex().getProfile(artistId);
			if (profile != null) {
				profile.removeRelease(releaseRecord.getUniqueId());
				profile.save();
			}
		}
		for (String labelName : releaseRecord.getSourceLabelNames()) {
			LabelIdentifier labelId = new LabelIdentifier(labelName);
			LabelProfile profile = (LabelProfile)Database.getLabelIndex().getProfile(labelId);
			if (profile != null) {
				profile.removeRelease(releaseRecord.getUniqueId());
				profile.save();
			}
		}	
		// note: insert code before call to super
		super.removeRelationalItems(removedRecord, deleteRecords);
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}

