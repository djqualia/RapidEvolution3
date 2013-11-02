package com.mixshare.rapid_evolution.data.index.search.artist;

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
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.artist.SubmittedArtist;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.model.search.artist.ArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.search.artist.RecommendedArtistModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class ArtistIndex extends SearchIndex {

    static private Logger log = Logger.getLogger(ArtistIndex.class);
    static private final long serialVersionUID = 0L;    
	
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public ArtistIndex() {
		super();
	}
	public ArtistIndex(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}
		    
	/////////////
	// GETTERS //
	/////////////
	
	public byte getDataType() { return DATA_TYPE_ARTISTS; }
	
	public ArtistModelManager getArtistModelManager() { return (ArtistModelManager)modelManager; }
	
	protected Profile getNewProfile(SubmittedProfile profile, int fileId) { return new ArtistProfile((ArtistIdentifier)profile.getIdentifier(), fileId); }
	
	public ArtistRecord getArtistRecord(int uniqueId) { return (ArtistRecord)getRecord(uniqueId); } 
	public ArtistRecord getArtistRecord(Identifier id) { return (ArtistRecord)getRecord(id); } 

	public ArtistProfile getArtistProfile(int uniqueId) { return (ArtistProfile)getProfile(uniqueId); } 
	public ArtistProfile getArtistProfile(Identifier id) { return (ArtistProfile)getProfile(id); } 
	
	public RecommendedArtistModelManager getRecommendedArtistModelManager() { return (RecommendedArtistModelManager)recommendedModelManager; }
	
	public SearchParameters getNewSearchParameters() { return new ArtistSearchParameters(); }
	
	/////////////
	// SETTERS //
	/////////////
	
	// for serialization
	public void setArtistModelManager(ArtistModelManager modelManager) { this.modelManager = modelManager; }	
	public void setRecommendedArtistModelManager(RecommendedArtistModelManager recommendedModelManager) { this.recommendedModelManager = recommendedModelManager; }
	
	/////////////
	// METHODS //
	/////////////
	
	public void propertiesChanged() {
		ArtistProfile.loadProperties();
	}
	
	public ArtistProfile addArtist(SubmittedArtist submittedArtist) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		return (ArtistProfile)add(submittedArtist);
	}
	
	public Profile add(SubmittedProfile submittedProfile) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		if ((RE3Properties.getInt("max_external_artists") == 0) && ((SubmittedArtist)submittedProfile).isExternalItem())
			return null;	
		return super.add(submittedProfile);
	}
	
	public ModelManagerInterface createModelManager() {
		ArtistModelManager result = new ArtistModelManager();
		result.initColumns();
		return result;
	}		
	
	protected SearchModelManager createRecommendedModelManager() {
		RecommendedArtistModelManager result = new RecommendedArtistModelManager();
		result.initColumns();
		return result;
	}
	
	protected void initProfile(Profile profile, SubmittedProfile initialValues) {
		super.initProfile(profile, initialValues);
		ArtistProfile artistProfile = (ArtistProfile)profile;
		SubmittedArtist submittedArtist = (SubmittedArtist)initialValues;
		if (submittedArtist.getInitialSongId() != null)
			artistProfile.addSong(Database.getSongIndex().getUniqueIdFromIdentifier(submittedArtist.getInitialSongId()));		
	}
	
	public void mergeProfiles(Profile primaryProfile, Profile mergedProfile) {
		ArtistProfile primaryArtist = (ArtistProfile)primaryProfile;
		ArtistProfile mergedArtist = (ArtistProfile)mergedProfile;
		Vector<Integer> primaryReleaseIds = primaryArtist.getReleaseIds();
		Vector<Integer> mergedReleaseIds = mergedArtist.getReleaseIds();
		Vector<Integer> primarySongIds = primaryArtist.getSongIds();
		Vector<Integer> mergedSongIds = mergedArtist.getSongIds();
		super.mergeProfiles(primaryProfile, mergedProfile);
		// clear cached song descriptions for old artist songs
		for (int songId : mergedSongIds) {
			SongRecord song = Database.getSongIndex().getSongRecord(songId);
			if (song != null)
				song.clearCachedSongDescription();
		}
		// check for releases that are now obviously duplicates
		try {
			for (int releaseId : primaryReleaseIds) {
				if (RapidEvolution3.isTerminated)
					return;
				ReleaseRecord primaryRelease = Database.getReleaseIndex().getReleaseRecord(releaseId);
				if (primaryRelease != null) {
					boolean found = false;
					int i = 0;
					while ((i < mergedReleaseIds.size()) && !found) {
						ReleaseRecord mergedRelease = Database.getReleaseIndex().getReleaseRecord(mergedReleaseIds.get(i));
						if (mergedRelease != null) {
							if (mergedRelease.getUniqueId() != primaryRelease.getUniqueId()) {
								if (primaryRelease.getReleaseTitle().equalsIgnoreCase(mergedRelease.getReleaseTitle()) &&
										primaryRelease.getArtistsDescription().equalsIgnoreCase(mergedRelease.getArtistsDescription())) {
									found = true;
									ReleaseProfile primaryReleaseProfile = Database.getReleaseIndex().getReleaseProfile(releaseId);
									ReleaseProfile mergedReleaseProfile = Database.getReleaseIndex().getReleaseProfile(mergedReleaseIds.get(i));
									Database.mergeProfiles(primaryReleaseProfile, mergedReleaseProfile);
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
	}	
	protected void removeRelationalItems(Record removedRecord, boolean deleteRecords) {
		// note: insert code before call to super
		super.removeRelationalItems(removedRecord, deleteRecords);
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
