package com.mixshare.rapid_evolution.data.record.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class ReleaseGroupRecord extends SongGroupRecord {

    ////////////
    // FIELDS //
    ////////////

    protected short numReleases;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public ReleaseGroupRecord() { super(); }
    public ReleaseGroupRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	numReleases = Short.parseShort(lineReader.getNextLine());
    }

    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    abstract public Vector<ReleaseRecord> getReleases();

    /////////////
    // GETTERS //
    /////////////

    public short getNumReleases() { return numReleases; }

    /////////////
    // SETTERS //
    /////////////

    public void setNumReleases(short numReleases) {
    	this.numReleases = numReleases;
    }

    /////////////
    // METHODS //
    /////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);
    	ReleaseGroupRecord groupRecord = (ReleaseGroupRecord)record;
    	// # songs
    	Vector<ReleaseRecord> releases1 = getReleases();
    	Vector<ReleaseRecord> releases2 = groupRecord.getReleases();
    	Map<Identifier, Object> releasesMap = new HashMap<Identifier, Object>(releases1.size() + releases2.size());
    	for (ReleaseRecord release : releases1)
    		releasesMap.put(release.getIdentifier(), null);
    	for (ReleaseRecord release : releases2)
    		releasesMap.put(release.getIdentifier(), null);
    	numReleases = (short)releasesMap.size();
    }

    @Override
	public void write(LineWriter textWriter) {
    	super.write(textWriter);
    	textWriter.writeLine(1); //version
    	textWriter.writeLine(numReleases);
    }


}
