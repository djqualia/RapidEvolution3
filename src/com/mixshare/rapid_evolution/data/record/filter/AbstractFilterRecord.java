package com.mixshare.rapid_evolution.data.record.filter;

import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.util.io.LineReader;

abstract public class AbstractFilterRecord extends HierarchicalRecord {

	public AbstractFilterRecord() { super(); }
	public AbstractFilterRecord(LineReader lineReader) { super(lineReader); }

	abstract public int getNumArtistRecords();
	abstract public int getNumLabelRecords();
	abstract public int getNumReleaseRecords();
	abstract public int getNumSongRecords();
	abstract public int getNumArtistRecordsCached();
	abstract public int getNumLabelRecordsCached();
	abstract public int getNumReleaseRecordsCached();
	abstract public int getNumSongRecordsCached();
	abstract public int getNumExternalArtistRecords();
	abstract public int getNumExternalLabelRecords();
	abstract public int getNumExternalReleaseRecords();
	abstract public int getNumExternalSongRecords();

}
