package com.mixshare.rapid_evolution.data.record.filter.playlist;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class DynamicPlaylistRecord extends PlaylistRecord {

    static private Logger log = Logger.getLogger(DynamicPlaylistRecord.class);

    static private final long serialVersionUID = 0L;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public DynamicPlaylistRecord() { };
    public DynamicPlaylistRecord(PlaylistIdentifier playlistId, int uniqueId) {
    	super(playlistId, uniqueId);
    }
    public DynamicPlaylistRecord(PlaylistIdentifier playlistId, int uniqueId, boolean isRoot) {
    	super(playlistId, uniqueId, isRoot);
    }
    public DynamicPlaylistRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());

    	int numIds = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numIds; ++i)
    		artistIds.put(Integer.parseInt(lineReader.getNextLine()), null);
    	numIds = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numIds; ++i)
    		labelIds.put(Integer.parseInt(lineReader.getNextLine()), null);
    	numIds = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numIds; ++i)
    		releaseIds.put(Integer.parseInt(lineReader.getNextLine()), null);
    	numIds = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numIds; ++i)
    		songIds.put(Integer.parseInt(lineReader.getNextLine()), null);
    	numIds = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numIds; ++i)
    		excludeArtistIds.put(Integer.parseInt(lineReader.getNextLine()), null);
    	numIds = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numIds; ++i)
    		excludeLabelIds.put(Integer.parseInt(lineReader.getNextLine()), null);
    	numIds = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numIds; ++i)
    		excludeReleaseIds.put(Integer.parseInt(lineReader.getNextLine()), null);
    	numIds = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numIds; ++i)
    		excludeSongIds.put(Integer.parseInt(lineReader.getNextLine()), null);

    	if (version >= 2) {
        	int numParams = Integer.parseInt(lineReader.getNextLine());
        	if (numParams > 0) {
        		artistSearchParameters = new ArtistSearchParameters[numParams];
        		for (int i = 0; i < numParams; ++i)
        			artistSearchParameters[i] = new ArtistSearchParameters(lineReader);
        	}
        	numParams = Integer.parseInt(lineReader.getNextLine());
        	if (numParams > 0) {
        		labelSearchParameters = new LabelSearchParameters[numParams];
        		for (int i = 0; i < numParams; ++i)
        			labelSearchParameters[i] = new LabelSearchParameters(lineReader);
        	}
        	numParams = Integer.parseInt(lineReader.getNextLine());
        	if (numParams > 0) {
        		releaseSearchParameters = new ReleaseSearchParameters[numParams];
        		for (int i = 0; i < numParams; ++i)
        			releaseSearchParameters[i] = new ReleaseSearchParameters(lineReader);
        	}
        	numParams = Integer.parseInt(lineReader.getNextLine());
        	if (numParams > 0) {
        		songSearchParameters = new SongSearchParameters[numParams];
        		for (int i = 0; i < numParams; ++i)
        			songSearchParameters[i] = new SongSearchParameters(lineReader);
        	}
    	}
    }

    ////////////
    // FIELDS //
    ////////////

    private Map<Integer, Object> artistIds = new HashMap<Integer, Object>();
    private Map<Integer, Object> labelIds = new HashMap<Integer, Object>();
    private Map<Integer, Object> releaseIds = new HashMap<Integer, Object>();
    private Map<Integer, Object> songIds = new HashMap<Integer, Object>();

    private Map<Integer, Object> excludeArtistIds = new HashMap<Integer, Object>();
    private Map<Integer, Object> excludeLabelIds = new HashMap<Integer, Object>();
    private Map<Integer, Object> excludeReleaseIds = new HashMap<Integer, Object>();
    private Map<Integer, Object> excludeSongIds = new HashMap<Integer, Object>();

    private ArtistSearchParameters[] artistSearchParameters;
    private LabelSearchParameters[] labelSearchParameters;
    private ReleaseSearchParameters[] releaseSearchParameters;
    private SongSearchParameters[] songSearchParameters;

    /////////////
    // GETTERS //
    /////////////

    public boolean containsEntries() {
    	return (artistIds.size() > 0) || (labelIds.size() > 0) || (releaseIds.size() > 0) || (songIds.size() > 0);
    }

	public ArtistSearchParameters[] getArtistSearchParameters() { return artistSearchParameters; }
    public ArtistSearchParameters getFirstArtistSearchParameters() {
    	if ((artistSearchParameters != null) && (artistSearchParameters.length > 0))
    		return artistSearchParameters[0];
    	return null;
    }

	public LabelSearchParameters[] getLabelSearchParameters() { return labelSearchParameters; }
    public LabelSearchParameters getFirstLabelSearchParameters() {
    	if ((labelSearchParameters != null) && (labelSearchParameters.length > 0))
    		return labelSearchParameters[0];
    	return null;
    }

	public ReleaseSearchParameters[] getReleaseSearchParameters() { return releaseSearchParameters; }
    public ReleaseSearchParameters getFirstReleaseSearchParameters() {
    	if ((releaseSearchParameters != null) && (releaseSearchParameters.length > 0))
    		return releaseSearchParameters[0];
    	return null;
    }

	public SongSearchParameters[] getSongSearchParameters() { return songSearchParameters; }
    public SongSearchParameters getFirstSongSearchParameters() {
    	if ((songSearchParameters != null) && (songSearchParameters.length > 0))
    		return songSearchParameters[0];
    	return null;
    }

	@Override
	public int getNumArtistRecords() {
		if ((artistSearchParameters == null) || (artistSearchParameters.length == 0))
			return artistIds.size();
		return getInternalArtistRecordsCount();
	}
	@Override
	public int getNumArtistRecordsCached() { return getNumArtistRecords(); }
	@Override
	public int getNumLabelRecords() {
		if ((labelSearchParameters == null) || (labelSearchParameters.length == 0))
			return labelIds.size();
		return getInternalLabelRecordsCount();
	}
	@Override
	public int getNumLabelRecordsCached() { return getNumLabelRecords(); }
	@Override
	public int getNumReleaseRecords() {
		if ((releaseSearchParameters == null) || (releaseSearchParameters.length == 0))
			return releaseIds.size();
		return getInternalReleaseRecordsCount();
	}
	@Override
	public int getNumReleaseRecordsCached() { return getNumReleaseRecords(); }
	@Override
	public int getNumSongRecords() {
		if ((songSearchParameters == null) || (songSearchParameters.length == 0))
			return songIds.size();
		return getInternalSongRecordsCount();
	}
	@Override
	public int getNumSongRecordsCached() { return getNumSongRecords(); }
	@Override
	public int getNumExternalArtistRecords() { return 0; }
	@Override
	public int getNumExternalLabelRecords() { return 0; }
	@Override
	public int getNumExternalReleaseRecords() { return 0; }
	@Override
	public int getNumExternalSongRecords() { return 0; }

    // for serialization
	public Map<Integer, Object> getArtistIds() { return artistIds; }
	public Map<Integer, Object> getLabelIds() { return labelIds; }
	public Map<Integer, Object> getReleaseIds() { return releaseIds; }
	public Map<Integer, Object> getSongIds() { return songIds; }
	public Map<Integer, Object> getExcludeArtistIds() { return excludeArtistIds; }
	public Map<Integer, Object> getExcludeLabelIds() { return excludeLabelIds; }
	public Map<Integer, Object> getExcludeReleaseIds() { return excludeReleaseIds; }
	public Map<Integer, Object> getExcludeSongIds() { return excludeSongIds; }

	/////////////
	// SETTERS //
	/////////////

    @Override
	public void addSong(int songId) { songIds.put(songId, null); }
    @Override
	public void addLabel(int labelId) { labelIds.put(labelId, null); }
    @Override
	public void addRelease(int releaseId) { releaseIds.put(releaseId, null); }
    @Override
	public void addArtist(int artistId) { artistIds.put(artistId, null); }

    @Override
	public void removeSong(int songId) {
    	songIds.remove(songId);
    	excludeSongIds.put(songId, null);
    }
    @Override
	public void removeLabel(int labelId) {
    	labelIds.remove(labelId);
    	excludeLabelIds.put(labelId, null);
    }
    @Override
	public void removeRelease(int releaseId) {
    	releaseIds.remove(releaseId);
    	excludeReleaseIds.put(releaseId, null);
    }
    @Override
	public void removeArtist(int artistId) {
    	artistIds.remove(artistId);
    	excludeArtistIds.put(artistId, null);
    }

	public void addArtistSearchParameters(ArtistSearchParameters artistSearchParameter) {
		if (artistSearchParameter == null)
			return;
		if (artistSearchParameters == null) {
			artistSearchParameters = new ArtistSearchParameters[1];
			artistSearchParameters[0] = artistSearchParameter;
		} else {
			ArtistSearchParameters[] newParams = new ArtistSearchParameters[artistSearchParameters.length + 1];
			for (int i = 0; i < artistSearchParameters.length; ++i)
				newParams[i] = artistSearchParameters[i];
			newParams[artistSearchParameters.length] = artistSearchParameter;
			artistSearchParameters = newParams;
		}
	}
	public void addLabelSearchParameters(LabelSearchParameters labelSearchParameter) {
		if (labelSearchParameter == null)
			return;
		if (labelSearchParameters == null) {
			labelSearchParameters = new LabelSearchParameters[1];
			labelSearchParameters[0] = labelSearchParameter;
		} else {
			LabelSearchParameters[] newParams = new LabelSearchParameters[labelSearchParameters.length + 1];
			for (int i = 0; i < labelSearchParameters.length; ++i)
				newParams[i] = labelSearchParameters[i];
			newParams[labelSearchParameters.length] = labelSearchParameter;
			labelSearchParameters = newParams;
		}
	}
	public void addReleaseSearchParameters(ReleaseSearchParameters releaseSearchParameter) {
		if (releaseSearchParameter == null)
			return;
		if (releaseSearchParameters == null) {
			releaseSearchParameters = new ReleaseSearchParameters[1];
			releaseSearchParameters[0] = releaseSearchParameter;
		} else {
			ReleaseSearchParameters[] newParams = new ReleaseSearchParameters[releaseSearchParameters.length + 1];
			for (int i = 0; i < releaseSearchParameters.length; ++i)
				newParams[i] = releaseSearchParameters[i];
			newParams[releaseSearchParameters.length] = releaseSearchParameter;
			releaseSearchParameters = newParams;
		}

	}
	public void addSongSearchParameters(SongSearchParameters songSearchParameter) {
		if (songSearchParameter == null)
			return;
		if (songSearchParameters == null) {
			songSearchParameters = new SongSearchParameters[1];
			songSearchParameters[0] = songSearchParameter;
		} else {
			SongSearchParameters[] newParams = new SongSearchParameters[songSearchParameters.length + 1];
			for (int i = 0; i < songSearchParameters.length; ++i)
				newParams[i] = songSearchParameters[i];
			newParams[songSearchParameters.length] = songSearchParameter;
			songSearchParameters = newParams;
		}
	}

	// for serialization
	public void setArtistIds(Map<Integer, Object> artistIds) { this.artistIds = artistIds; }
	public void setLabelIds(Map<Integer, Object> labelIds) { this.labelIds = labelIds; }
	public void setReleaseIds(Map<Integer, Object> releaseIds) { this.releaseIds = releaseIds; }
	public void setSongIds(Map<Integer, Object> songIds) { this.songIds = songIds; }
	public void setExcludeArtistIds(Map<Integer, Object> excludeArtistIds) { this.excludeArtistIds = excludeArtistIds; }
	public void setExcludeLabelIds(Map<Integer, Object> excludeLabelIds) { this.excludeLabelIds = excludeLabelIds; }
	public void setExcludeReleaseIds(Map<Integer, Object> excludeReleaseIds) { this.excludeReleaseIds = excludeReleaseIds; }
	public void setExcludeSongIds(Map<Integer, Object> excludeSongIds) { this.excludeSongIds = excludeSongIds; }
	public void setArtistSearchParameters(ArtistSearchParameters[] artistSearchParameters) { this.artistSearchParameters = artistSearchParameters; }
	public void setLabelSearchParameters(LabelSearchParameters[] labelSearchParameters) { this.labelSearchParameters = labelSearchParameters; }
	public void setReleaseSearchParameters(ReleaseSearchParameters[] releaseSearchParameters) { this.releaseSearchParameters = releaseSearchParameters; }
	public void setSongSearchParameters(SongSearchParameters[] songSearchParameters) { this.songSearchParameters = songSearchParameters; }

	/////////////
	// METHODS //
	/////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);

    	DynamicPlaylistRecord playlist = (DynamicPlaylistRecord)record;
    	for (int artistId : playlist.getArtistIds().keySet())
    		addArtist(artistId);
    	for (int labelId : playlist.getLabelIds().keySet())
    		addLabel(labelId);
    	for (int releaseId : playlist.getReleaseIds().keySet())
    		addRelease(releaseId);
    	for (int songId : playlist.getSongIds().keySet())
    		addSong(songId);

    	for (int artistId : playlist.getExcludeArtistIds().keySet())
    		removeArtist(artistId);
    	for (int labelId : playlist.getExcludeLabelIds().keySet())
    		removeLabel(labelId);
    	for (int releaseId : playlist.getExcludeReleaseIds().keySet())
    		removeRelease(releaseId);
    	for (int songId : playlist.getExcludeSongIds().keySet())
    		removeSong(songId);

    	if (playlist.artistSearchParameters != null)
    		for (ArtistSearchParameters params : playlist.artistSearchParameters)
    			addArtistSearchParameters(params);
    	if (playlist.labelSearchParameters != null)
    		for (LabelSearchParameters params : playlist.labelSearchParameters)
    			addLabelSearchParameters(params);
    	if (playlist.releaseSearchParameters != null)
    		for (ReleaseSearchParameters params : playlist.releaseSearchParameters)
    			addReleaseSearchParameters(params);
    	if (playlist.songSearchParameters != null)
    		for (SongSearchParameters params : playlist.songSearchParameters)
    			addSongSearchParameters(params);
    }

    @Override
	public boolean matches(SearchRecord searchRecord) {
    	if (searchRecord instanceof ArtistRecord) {
    		if (excludeArtistIds.containsKey(searchRecord.getUniqueId()))
    			return false;
    		if (artistIds.containsKey(searchRecord.getUniqueId()))
    			return true;
    		if (artistSearchParameters != null)
    			for (ArtistSearchParameters params : artistSearchParameters)
    				if (params.matches(searchRecord) > 0.0f)
    					return true;
    		return false;
    	}  else if (searchRecord instanceof LabelRecord) {
    		if (excludeLabelIds.containsKey(searchRecord.getUniqueId()))
    			return false;
    		if (labelIds.containsKey(searchRecord.getUniqueId()))
    			return true;
    		if (labelSearchParameters != null)
    			for (LabelSearchParameters params : labelSearchParameters)
    				if (params.matches(searchRecord) > 0.0f)
    					return true;
    		return false;
    	} else if (searchRecord instanceof ReleaseRecord) {
    		if (excludeReleaseIds.containsKey(searchRecord.getUniqueId()))
    			return false;
    		if (releaseIds.containsKey(searchRecord.getUniqueId()))
    			return true;
    		if (releaseSearchParameters != null)
    			for (ReleaseSearchParameters params : releaseSearchParameters)
    				if (params.matches(searchRecord) > 0.0f)
    					return true;
    		boolean include = false;
    		ReleaseRecord releaseRecord = (ReleaseRecord)searchRecord;
    		if (releaseRecord.getArtistIds() != null) {
	    		for (int artistId : releaseRecord.getArtistIds()) {
	    			if (excludeArtistIds.containsKey(artistId))
	    				return false;
	    			if (artistIds.containsKey(artistId))
	    				include = true;
	    		}
    		}
    		if (releaseRecord.getLabelIds() != null) {
	    		for (int labelId : releaseRecord.getLabelIds()) {
	    			if (excludeLabelIds.containsKey(labelId))
	    				return false;
	    			if (labelIds.containsKey(labelId))
	    				include = true;
	    		}
    		}
    		return include;
    	} else if (searchRecord instanceof SongRecord) {
    		if (excludeSongIds.containsKey(searchRecord.getUniqueId()))
    			return false;
    		if (songIds.containsKey(searchRecord.getUniqueId()))
    			return true;
    		if (songSearchParameters != null)
    			for (SongSearchParameters params : songSearchParameters)
    				if (params.matches(searchRecord) > 0.0f)
    					return true;
    		boolean include = false;
    		SongRecord songRecord = (SongRecord)searchRecord;
    		if (songRecord.getArtistIds() != null) {
    			for (int artistId : songRecord.getArtistIds()) {
    				if (excludeArtistIds.containsKey(artistId))
    					return false;
    				if (artistIds.containsKey(artistId))
    					include = true;
    			}
    		}
    		if (songRecord.getLabelIds() != null) {
    			for (int labelId : songRecord.getLabelIds()) {
    				if (excludeLabelIds.containsKey(labelId))
    					return false;
    				if (labelIds.containsKey(labelId))
    					include = true;
    			}
    		}
    		if (songRecord.getReleaseIds() != null) {
    			for (int releaseId : songRecord.getReleaseIds()) {
    				if (excludeReleaseIds.containsKey(releaseId))
    					return false;
    				if (releaseIds.containsKey(releaseId))
    					include = true;
    			}
    		}
    		return include;
    	}
    	return false;
    }

    @Override
	public void write(LineWriter textWriter) {
    	textWriter.writeLine(2); // type
    	super.write(textWriter);
    	textWriter.writeLine(2); //version

    	textWriter.writeLine(artistIds.size());
    	for (Integer id : artistIds.keySet())
    		textWriter.writeLine(id);
    	textWriter.writeLine(labelIds.size());
    	for (Integer id : labelIds.keySet())
    		textWriter.writeLine(id);
    	textWriter.writeLine(releaseIds.size());
    	for (Integer id : releaseIds.keySet())
    		textWriter.writeLine(id);
    	textWriter.writeLine(songIds.size());
    	for (Integer id : songIds.keySet())
    		textWriter.writeLine(id);
    	textWriter.writeLine(excludeArtistIds.size());
    	for (Integer id : excludeArtistIds.keySet())
    		textWriter.writeLine(id);
    	textWriter.writeLine(excludeLabelIds.size());
    	for (Integer id : excludeLabelIds.keySet())
    		textWriter.writeLine(id);
    	textWriter.writeLine(excludeReleaseIds.size());
    	for (Integer id : excludeReleaseIds.keySet())
    		textWriter.writeLine(id);
    	textWriter.writeLine(excludeSongIds.size());
    	for (Integer id : excludeSongIds.keySet())
    		textWriter.writeLine(id);
    	if (artistSearchParameters != null) {
    		textWriter.writeLine(artistSearchParameters.length);
    		for (ArtistSearchParameters params : artistSearchParameters)
    			params.write(textWriter);
    	} else {
    		textWriter.writeLine(0);
    	}
    	if (labelSearchParameters != null) {
    		textWriter.writeLine(labelSearchParameters.length);
    		for (LabelSearchParameters params : labelSearchParameters)
    			params.write(textWriter);
    	} else {
    		textWriter.writeLine(0);
    	}
    	if (releaseSearchParameters != null) {
    		textWriter.writeLine(releaseSearchParameters.length);
    		for (ReleaseSearchParameters params : releaseSearchParameters)
    			params.write(textWriter);
    	} else {
    		textWriter.writeLine(0);
    	}
    	if (songSearchParameters != null) {
    		textWriter.writeLine(songSearchParameters.length);
    		for (SongSearchParameters params : songSearchParameters)
    			params.write(textWriter);
    	} else {
    		textWriter.writeLine(0);
    	}
    }

}
