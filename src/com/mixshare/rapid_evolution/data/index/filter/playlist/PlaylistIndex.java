package com.mixshare.rapid_evolution.data.index.filter.playlist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.exceptions.UnknownErrorException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.index.filter.FilterIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.CategoryPlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.DynamicPlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.OrderedPlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.DynamicPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.filter.playlist.PlaylistSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedCategoryPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedDynamicPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedOrderedPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedPlaylist;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.filter.playlist.PlaylistModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class PlaylistIndex extends FilterIndex {

	static private Logger log = Logger.getLogger(PlaylistIndex.class);
	
    static private final long serialVersionUID = 0L;    

    /////////////////
    // CONSTRUCTOR //
    /////////////////

	public PlaylistIndex() {
		super();
	}
	public PlaylistIndex(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}

	////////////
	// FIELDS //
	////////////
		
	/////////////
	// GETTERS //
	/////////////
	
	public byte getDataType() { return DATA_TYPE_PLAYLISTS; }
	
	public PlaylistModelManager getPlaylistModelManager() { return (PlaylistModelManager)modelManager; }
	
	public PlaylistRecord getPlaylistRecord(int uniqueId) { return (PlaylistRecord)getRecord(uniqueId); } 
	public PlaylistRecord getPlaylistRecord(Identifier id) { return (PlaylistRecord)getRecord(id); } 

	public PlaylistProfile getPlaylistProfile(int uniqueId) { return (PlaylistProfile)getProfile(uniqueId); } 
	public PlaylistProfile getPlaylistProfile(Identifier id) { return (PlaylistProfile)getProfile(id); } 
	
	protected Profile getNewProfile(SubmittedProfile profile, int fileId) {
		if (profile instanceof SubmittedDynamicPlaylist)
			return new DynamicPlaylistProfile((PlaylistIdentifier)profile.getIdentifier(), fileId);
		else if (profile instanceof SubmittedOrderedPlaylist)
			return new OrderedPlaylistProfile((PlaylistIdentifier)profile.getIdentifier(), fileId);
		else if (profile instanceof SubmittedCategoryPlaylist)
			return new CategoryPlaylistProfile((PlaylistIdentifier)profile.getIdentifier(), fileId);
		return null;
	}
		
	protected HierarchicalRecord createRootRecord() {
		String rootPlaylistName = "***ROOT PLAYLIST***";
		PlaylistIdentifier playlistId = new PlaylistIdentifier(rootPlaylistName);
		int uniqueId = imdb.getUniqueIdFromIdentifier(playlistId);
		return new DynamicPlaylistRecord(playlistId, uniqueId, true);
	}
	protected HierarchicalRecord createRootRecord(LineReader lineReader) {
		return PlaylistRecord.readPlaylistRecord(lineReader);
	}
	
	public SearchParameters getNewSearchParameters() { return new PlaylistSearchParameters(); }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setPlaylistModelManager(PlaylistModelManager modelManager) { this.modelManager = modelManager; }
	
	/////////////
	// METHODS //
	/////////////
	
	public PlaylistProfile addPlaylist(SubmittedPlaylist submittedPlaylist) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		return (PlaylistProfile)add(submittedPlaylist);
	}
	
	public ModelManagerInterface createModelManager() {
		PlaylistModelManager result = new PlaylistModelManager();
		result.initColumns();
		return result;
	}		

	protected void initProfile(Profile profile, SubmittedProfile initialValues) {		
		PlaylistProfile playlistProfile = (PlaylistProfile)profile;
		SubmittedPlaylist submittedPlaylist = (SubmittedPlaylist)initialValues;
		for (int songId : submittedPlaylist.getSongIds())
			playlistProfile.addSong(songId);
		if (submittedPlaylist instanceof SubmittedDynamicPlaylist) {
			SubmittedDynamicPlaylist dynamicPlaylist = (SubmittedDynamicPlaylist)submittedPlaylist;
			DynamicPlaylistProfile dynamicProfile = (DynamicPlaylistProfile)playlistProfile;
			dynamicProfile.getDynamicPlaylistRecord().addArtistSearchParameters(dynamicPlaylist.getArtistSearchParameters());
			dynamicProfile.getDynamicPlaylistRecord().addLabelSearchParameters(dynamicPlaylist.getLabelSearchParameters());
			dynamicProfile.getDynamicPlaylistRecord().addReleaseSearchParameters(dynamicPlaylist.getReleaseSearchParameters());
			dynamicProfile.getDynamicPlaylistRecord().addSongSearchParameters(dynamicPlaylist.getSongSearchParameters());
		}
		super.initProfile(profile, initialValues);
	}	

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
		
}
