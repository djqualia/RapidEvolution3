package com.mixshare.rapid_evolution.data.record.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityDescription;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityVariance;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityVarianceDescription;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * This class abstracts types of records which contain sets of songs (artists, labels, releases)
 */
abstract public class SongGroupRecord extends SearchRecord {

    static private Logger log = Logger.getLogger(SongGroupRecord.class);

    ////////////
    // FIELDS //
    ////////////

    protected int numSongs;

    protected float avgBeatIntensity;
    protected float beatIntensityVariance = Float.NaN;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public SongGroupRecord() { super(); }
    public SongGroupRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	numSongs = Integer.parseInt(lineReader.getNextLine());
    	avgBeatIntensity = Float.parseFloat(lineReader.getNextLine());
    	beatIntensityVariance = Float.parseFloat(lineReader.getNextLine());
    }

    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    abstract public Vector<SongRecord> getSongs();

    /////////////
    // GETTERS //
    /////////////

    public int getNumSongs() { return numSongs; }

    public BeatIntensity getAvgBeatIntensity() { return BeatIntensity.getBeatIntensity((byte)avgBeatIntensity); }
    public BeatIntensityDescription getAvgBeatIntensityDescription() { return BeatIntensityDescription.getBeatIntensityDescription((byte)avgBeatIntensity); }

    public BeatIntensityVariance getBeatIntensityVariance() { return BeatIntensityVariance.getBeatIntensityVariance(beatIntensityVariance); }
    public BeatIntensityVarianceDescription getBeatIntensityVarianceDescription() { return BeatIntensityVarianceDescription.getBeatIntensityVarianceDescription(beatIntensityVariance); }

    /////////////
    // SETTERS //
    /////////////

    public void setNumSongs(int numSongs) {
    	this.numSongs = numSongs;
    }

    public void setAvgBeatIntensity(float avgBeatIntensity, float beatIntensityVariance) {
    	this.avgBeatIntensity = avgBeatIntensity;
    	this.beatIntensityVariance = beatIntensityVariance;
    }

	public void setAvgBeatIntensity(float avgBeatIntensity) { this.avgBeatIntensity = avgBeatIntensity; }
	public void setBeatIntensityVariance(float beatIntensityVariance) { this.beatIntensityVariance = beatIntensityVariance; }

    /////////////
    // METHODS //
    /////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);
    	SongGroupRecord groupRecord = (SongGroupRecord)record;
    	// # songs
    	Vector<SongRecord> songs1 = getSongs();
    	Vector<SongRecord> songs2 = groupRecord.getSongs();
    	Map<Identifier, Object> songMap = new HashMap<Identifier, Object>(songs1.size() + songs2.size());
    	for (SongRecord song : songs1)
    		songMap.put(song.getIdentifier(), null);
    	for (SongRecord song : songs2)
    		songMap.put(song.getIdentifier(), null);
    	numSongs = songMap.size();
    }

    @Override
	public void write(LineWriter textWriter) {
    	super.write(textWriter);
    	textWriter.writeLine(1); //version
    	textWriter.writeLine(numSongs);
    	textWriter.writeLine(avgBeatIntensity);
    	textWriter.writeLine(beatIntensityVariance);
    }

}
