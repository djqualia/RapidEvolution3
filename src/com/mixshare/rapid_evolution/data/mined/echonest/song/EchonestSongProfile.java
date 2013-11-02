package com.mixshare.rapid_evolution.data.mined.echonest.song;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Segment;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.Track;
import com.echonest.api.v4.TrackAnalysis;
import com.echonest.api.v4.Track.AnalysisStatus;
import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.echonest.EchonestAPIWrapper;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.util.confidence.FloatConfidence;
import com.mixshare.rapid_evolution.data.util.confidence.IntConfidence;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.music.key.Key;

public class EchonestSongProfile extends MinedProfile implements Serializable {

    static private Logger log = Logger.getLogger(EchonestSongProfile.class);
    static private final long serialVersionUID = 0L;
    
    static private int processSongTimeout = RE3Properties.getInt("echonest_song_process_timeout_millis");
        
    ////////////
    // FIELDS //
    ////////////
    
    private boolean isValid = false;
    private String artist;
    private String release;
    private String title;
    private String genre;
    private int bitRate;
    private int sampleRate;
    private int analysisVersion;
    private float duration; // seconds i believe...
    private float endOfFadeIn; // seconds
    private float startOfFadeOut;
    private IntConfidence key;
    private IntConfidence timeSignature;
    private IntConfidence mode;
    private FloatConfidence bpm;
    private float overallLoudness;
    private Vector<EchonestTimedEvent> bars;    
    private Vector<EchonestTimedEvent> beats;
    private Vector<EchonestTimedEvent> tatums;
    private Vector<EchonestSongSegment> segments;
    private Vector<EchonestTimedEvent> sections;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public EchonestSongProfile() {
    	super(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_ECHONEST));
    }
    
    public EchonestSongProfile(SongProfile songProfile) {
        super(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_ECHONEST));
        this.artist = songProfile.getArtistsDescription();
        this.title = songProfile.getSongDescription();
        String filename = songProfile.getSongFilename();
        if (filename != null)
        	load(filename);
    }    
    
    public EchonestSongProfile(String filename, String artist, String title) {
    	super(new MinedProfileHeader(DATA_TYPE_SONGS, DATA_SOURCE_ECHONEST));
    	if (filename != null)
    		load(filename);
    	this.artist = artist;
    	this.title = title;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
	public boolean isValid() { return isValid; }
	public String getArtist() { return artist; }
	public String getRelease() { return release; }
	public String getTitle() { return title; }
	public String getGenre() { return genre; }
	public int getBitRate() { return bitRate; }
	public int getSampleRate() { return sampleRate; }
	public int getAnalysisVersion() { return analysisVersion; }
	public float getDuration() { return duration; }
	public float getEndOfFadeIn() { return endOfFadeIn; }
	public float getStartOfFadeOut() { return startOfFadeOut; }
	public float getKeyConfidence() {
		if (key != null)
			return key.getConfidence();
		return 0.0f;
	}
	public IntConfidence getKey() { return key; }
	public Key getKeyValue() {		
		if (key != null) {
			String keyName = null;
			if (key.getValue() == 0)
				keyName = "C";
			else if (key.getValue() == 1)
				keyName = "C#";
			else if (key.getValue() == 2)
				keyName = "D";
			else if (key.getValue() == 3)
				keyName = "D#";
			else if (key.getValue() == 4)
				keyName = "E";
			else if (key.getValue() == 5)
				keyName = "F";
			else if (key.getValue() == 6)
				keyName = "F#";
			else if (key.getValue() == 7)
				keyName = "G";
			else if (key.getValue() == 8)
				keyName = "G#";
			else if (key.getValue() == 9)
				keyName = "A";
			else if (key.getValue() == 10)
				keyName = "A#";
			else if (key.getValue() == 11)
				keyName = "B";
			if (mode.getValue() == 0)
				keyName += "m"; // minor
			return Key.getKey(keyName);			
		}
		return Key.NO_KEY;
	}
	public float getModeConfidence() {
		if (mode != null)
			mode.getConfidence(); 
		return 0.0f;
	}
	public IntConfidence getTimeSignature() { return timeSignature; }
	public FloatConfidence getBpm() { return bpm; }
	public float getBpmValue() {
		if (bpm != null)
			return bpm.getValue();
		return 0.0f;
	}
	public float getBpmConfidence() {
		if (bpm != null)
			return bpm.getConfidence();
		return 0.0f;
	}
	public float getOverallLoudness() { return overallLoudness; }
	public Vector<EchonestTimedEvent> getBars() { return bars; }
	public Vector<EchonestTimedEvent> getBeats() { return beats; }
	public Vector<EchonestTimedEvent> getTatums() { return tatums; }
	public Vector<EchonestSongSegment> getSegments() { return segments; }
	public Vector<EchonestTimedEvent> getSections() { return sections; }

	public IntConfidence getMode() {
		return mode;
	}
	
	public String toString() { return artist + " - " + title; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setMode(IntConfidence mode) {
		this.mode = mode;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public void setAnalysisVersion(int analysisVersion) {
		this.analysisVersion = analysisVersion;
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public void setEndOfFadeIn(float endOfFadeIn) {
		this.endOfFadeIn = endOfFadeIn;
	}

	public void setStartOfFadeOut(float startOfFadeOut) {
		this.startOfFadeOut = startOfFadeOut;
	}

	public void setKey(IntConfidence key) {
		this.key = key;
	}

	public void setTimeSignature(IntConfidence timeSignature) {
		this.timeSignature = timeSignature;
	}

	public void setBpm(FloatConfidence bpm) {
		this.bpm = bpm;
	}

	public void setOverallLoudness(float overallLoudness) {
		this.overallLoudness = overallLoudness;
	}
	
    /////////////
    // METHODS //
    /////////////
	
    protected void load(String filename) {
        try {
       	 	if (log.isDebugEnabled())
       	 		log.debug("load(): processing song filename=" + filename);
       	 	Track track = EchonestAPIWrapper.getEchoNestAPI().uploadTrack(new File(filename), true);
       	 	if ((track != null) && (track.getStatus() == AnalysisStatus.COMPLETE)) {       	 		
       	 		TrackAnalysis analysis = track.getAnalysis();
       	 		analysisVersion = 4; // v4 api, doesn't seem to have an analysis version
       	 		duration = analysis.getDuration().floatValue();       	 	
       	 		endOfFadeIn = analysis.getEndOfFadeIn().floatValue();
       	 		startOfFadeOut = analysis.getStartOfFadeOut().floatValue();     
       	 		key = new IntConfidence(analysis.getKey(), analysis.getKeyConfidence().floatValue());
       	 		mode = new IntConfidence(analysis.getMode(), analysis.getModeConfidence().floatValue());
       	 		overallLoudness = analysis.getLoudness().floatValue();
       	 		timeSignature = new IntConfidence(analysis.getTimeSignature(), analysis.getTimeSignatureConfidence().floatValue());
       	 		bpm = new FloatConfidence(analysis.getTempo().floatValue(), analysis.getTempoConfidence().floatValue());
       	 		sampleRate = analysis.getSampleRate().intValue();
       	 		
       	 		List<TimedEvent> analysisBars = analysis.getBars();
       	 		bars = new Vector<EchonestTimedEvent>(analysisBars.size());
       	 		for (TimedEvent bar : analysisBars)
       	 			bars.add(new EchonestTimedEvent(bar));       	 		
       	 		
       	 		List<TimedEvent> analysisBeats = analysis.getBeats();
       	 		beats = new Vector<EchonestTimedEvent>(analysisBeats.size());
       	 		for (TimedEvent beat : analysisBeats)
       	 			beats.add(new EchonestTimedEvent(beat));
       	 		
       	 		List<TimedEvent> analysisTatums = analysis.getTatums();
       	 		tatums = new Vector<EchonestTimedEvent>(analysisTatums.size());
       	 		for (TimedEvent tatum : analysisTatums)
       	 			tatums.add(new EchonestTimedEvent(tatum));
       	 			
       	 		List<TimedEvent> analysisSections = analysis.getSections();
       	 		sections = new Vector<EchonestTimedEvent>(analysisSections.size());
       	 		for (TimedEvent section : analysisSections)
       	 			sections.add(new EchonestTimedEvent(section));
       	 		
       	 		List<Segment> analysisSegments = analysis.getSegments();
       	 		segments = new Vector<EchonestSongSegment>(analysisSegments.size());
       	 		for (Segment segment : analysisSegments)
       	 			sections.add(new EchonestSongSegment(segment));
       	 		
       	 		isValid = true;       	 		
       	 	}
       	 	if (artist == null)
       	 		artist = track.getArtistName();
       	 	if (title == null)
       	 		title = track.getTitle();
       	 	release = track.getReleaseName();
       	 	genre = ""; // not in new api
       	 	bitRate = 0; // not in new api       	 	

        } catch (EchoNestException ee) {
        	log.warn("load(): error, echo nest exception=" + ee + ", on filename=" + filename);
        } catch (Exception e) {
        	log.error("load(): error", e);
        }    	
    }
	
    static public void main(String[] args) {
        try {
        	RapidEvolution3.loadLog4J();
            EchonestSongProfile profile = new EchonestSongProfile("C:/Users/jesse/Desktop/deceptikon/lost subject [2003]/deceptikon - lost subject - [04-15] - germanic.mp3", "Deceptikon", "Germanic");
            XMLSerializer.saveData(profile, "testserialize.out.xml");
            log.info("key=" + profile.getKeyValue() + ", bpm=" + profile.getBpmValue() + ", loudness=" + profile.getOverallLoudness() + ", time sig=" + profile.getTimeSignature().getValue());
            log.debug("main(): done");
        } catch (Exception e) {
            log.error("main(): error", e);
        }        
    }
    
}
