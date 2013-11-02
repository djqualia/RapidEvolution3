package com.mixshare.rapid_evolution.data.submitted.search.song;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.submitted.search.SubmittedSearchProfile;
import com.mixshare.rapid_evolution.data.submitted.search.release.SubmittedRelease;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.timesig.TimeSig;

public class SubmittedSong extends SubmittedSearchProfile {

	static private Map<String, String> trackPrefixes = new HashMap<String, String>(); // keys are what to be removed, values are what will be used as the track value
	static {
		// add all of track prefixes such as "01 " -> "01", "(01) " -> "01"
		for (int i = 1; i <= 99; ++i) {
			String track = String.valueOf(i);
			if (track.length() == 1)
				track = "0" + track;
			trackPrefixes.put(track + " ", track);
			trackPrefixes.put("(" + track + ") ", track);
		}
		// add vinyl based track prefixes such as "a1 " -> "a1", "(b2) " -> "b2"
		for (int i = 1; i <= 9; ++i) {
			String track = String.valueOf(i);
			trackPrefixes.put("a" + track + " ", "a" + track);
			trackPrefixes.put("b" + track + " ", "b" + track);
			trackPrefixes.put("c" + track + " ", "c" + track);
			trackPrefixes.put("d" + track + " ", "d" + track);
			trackPrefixes.put("e" + track + " ", "e" + track);
			trackPrefixes.put("f" + track + " ", "f" + track);
			trackPrefixes.put("g" + track + " ", "g" + track);
			trackPrefixes.put("h" + track + " ", "h" + track);
			trackPrefixes.put("(a" + track + ") ", "a" + track);
			trackPrefixes.put("(b" + track + ") ", "b" + track);
			trackPrefixes.put("(c" + track + ") ", "c" + track);
			trackPrefixes.put("(d" + track + ") ", "d" + track);
			trackPrefixes.put("(e" + track + ") ", "e" + track);
			trackPrefixes.put("(f" + track + ") ", "f" + track);
			trackPrefixes.put("(g" + track + ") ", "g" + track);
			trackPrefixes.put("(h" + track + ") ", "h" + track);
		}		
	}
	
	public SubmittedSong(Vector<String> artistNames, String release, String track, String title, String remix) {
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower")) {
			for (int i = 0; i < artistNames.size(); ++i)
				artistNames.set(i, artistNames.get(i).toLowerCase());			
			release = release.toLowerCase();
			track = track.toLowerCase();
			title = title.toLowerCase();
			remix = remix.toLowerCase();
		} else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper")) {
			release = release.toUpperCase();								
			track = track.toUpperCase();
			title = title.toUpperCase();
			remix = remix.toUpperCase();
			for (int i = 0; i < artistNames.size(); ++i)
				artistNames.set(i, artistNames.get(i).toUpperCase());			
		}
        this.release = release.trim();
        this.track = track.trim();
        this.title = title.trim();
        this.remix = remix.trim();
        if ((release != null) && (release.length() > 0))
        	submittedRelease = new SubmittedRelease(artistNames, release);
        parseTitleField();
        String songDescription = null;
        if (this.title.trim().length() > 0)
        	songDescription = SongIdentifier.getSongDescriptionFromTitleAndRemix(this.title, this.remix);
        else
        	songDescription = SongIdentifier.getSongDescriptionFromReleaseAndTrack(this.release, this.track);
        identifier = new SongIdentifier(artistNames, songDescription);
	}
	public SubmittedSong(String artistName, String release, String track, String title, String remix) {
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower")) {
			artistName = artistName.toLowerCase();
			release = release.toLowerCase();
			track = track.toLowerCase();
			title = title.toLowerCase();
			remix = remix.toLowerCase();
		} else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper")) {
			artistName = artistName.toUpperCase();
			release = release.toUpperCase();								
			track = track.toUpperCase();
			title = title.toUpperCase();
			remix = remix.toUpperCase();
		}
        this.release = release.trim();
        this.track = track.trim();
        this.title = title.trim();
        this.remix = remix.trim();
        if ((release != null) && (release.length() > 0))
        	submittedRelease = new SubmittedRelease(artistName, release);
        parseTitleField();
        String songDescription = null;
        if (this.title.trim().length() > 0)
        	songDescription = SongIdentifier.getSongDescriptionFromTitleAndRemix(this.title, this.remix);
        else
        	songDescription = SongIdentifier.getSongDescriptionFromReleaseAndTrack(this.release, this.track);
        identifier = new SongIdentifier(artistName, songDescription);
	}
	public SubmittedSong(SongProfile song) {
		super(song);
		
		this.release = song.getReleaseTitle();
		this.track = song.getTrack();
		this.title = song.getTitle();
		this.remix = song.getRemix();
		this.labelNames = song.getLabelNames();
		this.startKey = song.getStartKey();
		this.endKey = song.getEndKey();
		this.keySource = song.getKeySource();
		this.keyAccuracy = song.getKeyAccuracy();
		this.startBpm = song.getBpmStart();
		this.endBpm = song.getBpmEnd();
		this.bpmSource = song.getBpmSource();
		this.bpmAccuracy = song.getBpmAccuracy();
		this.timeSig = song.getTimeSig();
		this.timeSigSource = song.getTimeSigSource();
		this.beatIntensity = song.getBeatIntensityValue();
		this.beatIntensitySource = song.getBeatIntensitySource();
		this.duration = song.getDuration();
		this.durationSource = song.getDurationSource();
		this.songFilename = song.getSongFilename();
		this.songFilenameLastUpdated = song.getSongFileLastUpdated();
		this.originalYearReleased = song.getOriginalYearReleased();
		this.originalYearReleasedSource = song.getOriginalYearReleasedSource();
		this.replayGain = song.getReplayGain();
		this.replayGainSource = song.getReplayGainSource();
		this.iTunesID = song.getITunesID();
		this.isSyncedWithMixshare = song.isSyncedWithMixshare();
		this.lyrics = song.getLyrics();
		this.lyricsSource = song.getLyricsSource();		
	}
	
	//////////////////////////
	// CONSTRUCTION HELPERS //
	//////////////////////////
	
	private void parseTitleField() {
		if (track.length() == 0) {
			String titleLC = title.toLowerCase();
			for (Entry<String, String> entry : trackPrefixes.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (titleLC.startsWith(key)) {
					track = value;
					title = title.substring(key.length());
					return;
				}
			}
		}
	}
	
	////////////
	// FIELDS //
	////////////
	
	protected SubmittedRelease submittedRelease;	
	protected String release;
	protected String track;
	protected String title;
	protected String remix;
	protected Vector<String> labelNames;
	protected Key startKey = Key.NO_KEY;
	protected Key endKey = Key.NO_KEY;
	protected byte keySource;
	protected byte keyAccuracy;
	protected Bpm startBpm = new Bpm(0.0f);
	protected Bpm endBpm = new Bpm(0.0f);
	protected byte bpmSource;
	protected byte bpmAccuracy;	
	protected TimeSig timeSig = TimeSig.getTimeSig(4,4);
	protected byte timeSigSource;	
	protected BeatIntensity beatIntensity = BeatIntensity.getBeatIntensity(0);
	protected byte beatIntensitySource;
	protected Duration duration = new Duration(0);
	protected byte durationSource;
	protected String songFilename;
	protected long songFilenameLastUpdated;
	protected short originalYearReleased;
	protected byte originalYearReleasedSource;
    protected float replayGain;	
    protected byte replayGainSource;
    protected String iTunesID;
    protected boolean isSyncedWithMixshare;
	protected String lyrics;
	protected byte lyricsSource;
	protected Vector<PlaylistRecord> playlists = new Vector<PlaylistRecord>();
    protected Vector<String> featuringArtists = new Vector<String>();
	
    /////////////
    // GETTERS //
    /////////////
    
	public SongIdentifier getSongIdentifier() { return (SongIdentifier)identifier; }
    
    public SubmittedRelease getSubmittedRelease() {
    	if (submittedRelease != null) {
    		if (submittedRelease.getOriginalYearReleased() == 0)
    			submittedRelease.setOriginalYearReleased(originalYearReleased, originalYearReleasedSource);
    		if ((submittedRelease.getLabelNames() == null) || (submittedRelease.getLabelNames().size() == 0))
    			submittedRelease.setLabelNames(labelNames);
    		if (submittedRelease.getDateAdded() == 0)
    			submittedRelease.setDateAdded(dateAdded);
    	}
    	return submittedRelease;
    }
    
    public String getTrack() { return track; }
    public String getRelease() { return release; }
    public String getTitle() { return title; }
    public String getRemix() { return remix; }
	public Vector<String> getLabelNames() { return labelNames; }
	public Key getStartKey() { return startKey; }
	public Key getEndKey() { return endKey; }
	public byte getKeySource() { return keySource; }
	public byte getKeyAccuracy() { return keyAccuracy; }
	public Bpm getStartBpm() { return startBpm; }
	public Bpm getEndBpm() { return endBpm; }
	public byte getBpmSource() { return bpmSource; }
	public byte getBpmAccuracy() { return bpmAccuracy; }
	public TimeSig getTimeSig() { return timeSig; }
	public String getITunesID() { return iTunesID; }
	public float getReplayGain() { return replayGain; }
	public byte getReplayGainSource() { return replayGainSource; }
	public byte getOriginalYearReleasedSource() { return originalYearReleasedSource; }
	public short getOriginalYearReleased() { return originalYearReleased; }
	public String getSongFilename() { return songFilename; }
	public long getSongFilenameLastUpdated() { return songFilenameLastUpdated; }
	public byte getTimeSigSource() { return timeSigSource; }
	public BeatIntensity getBeatIntensity() { return beatIntensity; }
	public byte getBeatIntensitySource() { return beatIntensitySource; }
	public Duration getDuration() { return duration; }
	public byte getDurationSource() { return durationSource; }
	public boolean isSyncedWithMixshare() { return isSyncedWithMixshare; }
	public String getLyrics() { return lyrics; }
	public byte getLyricsSource() { return lyricsSource; }	
    public Vector<String> getFeaturingArtists() { return featuringArtists; }
	public Vector<PlaylistRecord> getPlaylists() { return playlists; }
	
	/////////////
	// SETTERS //
	/////////////

	public void setIdentifier(String artist, String album, String track, String title, String remix) {
		super.setIdentifier(new SubmittedSong(artist, album, track, title, remix).getIdentifier());
		this.release = album;
		this.track = track;
		this.title = title;
		this.remix = remix;
		if (submittedRelease != null) {
			ReleaseIdentifier releaseId = submittedRelease.getReleaseIdentifier();
			releaseId = new ReleaseIdentifier(((SongIdentifier)identifier).getArtistNames(), releaseId.getReleaseTitle());
			submittedRelease.setIdentifier(releaseId);
		}		
	}	
	
	public void setRelease(String release) { this.release = release; }
	public void setTrack(String track) { this.track = track; }
	public void setTitle(String title) { 
		this.title = title;
		SongIdentifier oldIdentifier = (SongIdentifier)identifier;
		identifier = new SongIdentifier(oldIdentifier.getArtistIds(), SongIdentifier.getSongDescriptionFromTitleAndRemix(title, remix));		
	}
	public void setRemix(String remix) { this.remix = remix; }	
	
	public void setCompilationFlag(boolean compilation) {
		if (compilation)
			submittedRelease = new SubmittedRelease(release);
		else
			submittedRelease = new SubmittedRelease(((SongIdentifier)identifier).getArtistNames(), release);
	}
	public void setSubmittedRelease(SubmittedRelease submittedRelease) {
		this.submittedRelease = submittedRelease;
	}
	
	public void setLabelNames(Vector<String> labelNames) { this.labelNames = labelNames; }
	public void setLabelName(String labelName) {
		labelNames = new Vector<String>(1);
		labelNames.add(labelName);
	}
	
	public void setKey(Key startKey, Key endKey, byte keyAccuracy, byte keySource) {
		this.startKey = startKey;
		this.endKey = endKey;
		this.keyAccuracy = keyAccuracy;
		this.keySource = keySource;
	}
	public void setBpm(Bpm startBpm, Bpm endBpm, byte bpmAccuracy, byte bpmSource) {
		this.startBpm = startBpm;
		this.endBpm = endBpm;
		this.bpmAccuracy = bpmAccuracy;
		this.bpmSource = bpmSource;
	}
	public void setTimeSig(TimeSig timeSig, byte timeSigSource) {
		this.timeSig = timeSig;
		this.timeSigSource = timeSigSource;
	}
	public void setBeatIntensity(BeatIntensity beatIntensity, byte beatIntensitySource) {
		this.beatIntensity = beatIntensity;
		this.beatIntensitySource = beatIntensitySource;
	}
	public void setDuration(Duration duration, byte durationSource) {
		this.duration = duration;
		this.durationSource = durationSource;
	}
	public void setSongFilename(String songFilename) { this.songFilename = songFilename; }
	public void setSongFilenameLastUpdated(long songFilenameLastUpdated) { this.songFilenameLastUpdated = songFilenameLastUpdated; }
	public void setOriginalYearReleased(short originalYearReleased, byte originalYearReleasedSource) {
		this.originalYearReleased = originalYearReleased;
		this.originalYearReleasedSource = originalYearReleasedSource;
	}
	public void setReplayGain(float replayGain, byte replayGainSource) {
		this.replayGain = replayGain;
		this.replayGainSource = replayGainSource;
	}
	public void setITunesID(String tunesID) { iTunesID = tunesID; }
	public void setSyncedWithMixshare(boolean isSyncedWithMixshare) { this.isSyncedWithMixshare = isSyncedWithMixshare; }	
	public void setLyrics(String lyrics, byte lyricsSource) {
		this.lyrics = lyrics;
		this.lyricsSource = lyricsSource;
	}
	
	public void addImage(Image image, boolean thumbnail) {
		super.addImage(image, thumbnail);
		if (submittedRelease != null)
			submittedRelease.addImage(image, thumbnail);
	}
	
	public void addInitialPlaylist(PlaylistRecord playlist) {
		playlists.add(playlist);
	}

	public void addFeaturingArtist(String featuringArtist) {
		if ((featuringArtist != null) && (featuringArtist.length() > 0) && (!featuringArtists.contains(featuringArtist)))
			featuringArtists.add(featuringArtist);		
	}
	public void setFeaturingArtists(Vector<String> featuringArtists) { this.featuringArtists = featuringArtists; }
	
}
