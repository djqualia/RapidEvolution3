package com.mixshare.rapid_evolution.data.record.search;

import org.json.JSONObject;

import com.mixshare.rapid_evolution.data.record.CommonRecord;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.util.io.LineReader;

abstract public class AbstractSearchRecord extends CommonRecord {

	public AbstractSearchRecord() { super(); }
	public AbstractSearchRecord(LineReader lineReader) { super(lineReader); }

	abstract public void setPlayCount(long numPlays);
	abstract public void incrementPlayCount(long increment);

	abstract public JSONObject getJSON(ModelManagerInterface modelManager);

}
