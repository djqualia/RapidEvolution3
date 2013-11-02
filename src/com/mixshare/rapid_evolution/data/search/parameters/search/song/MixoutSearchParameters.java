package com.mixshare.rapid_evolution.data.search.parameters.search.song;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class MixoutSearchParameters extends SearchSearchParameters {

    static private Logger log = Logger.getLogger(MixoutSearchParameters.class);
    static private final long serialVersionUID = 0L;    		

    ////////////
    // FIELDS //
    ////////////
    

    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public MixoutSearchParameters() { }
    
    public MixoutSearchParameters(SongSearchParameters copy) {
    	super(copy);
    }
    
    public MixoutSearchParameters(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	
    }    
    
    /////////////
    // GETTERS //
    /////////////
    
    public byte getDataType() { return DATA_TYPE_MIXOUTS; }
    
    public String getUniqueHash() {
    	StringBuffer result = new StringBuffer();
    	result.append(super.getUniqueHash());
    	// insert class specific parameters here...

    	return result.toString();
    }
    
    public Index getIndex() { return Database.getMixoutIndex(); }
    
	/////////////
	// SETTERS //
	/////////////
	

	
	/////////////
	// METHODS //
	/////////////
	
	public float matches(Record record) {
		float superScore = super.matches(record);
		if (superScore > 0.0f) {			
			return superScore;
		} else {
			return 0.0f;
		}
	}

	public boolean isEmpty() {
		if (!super.isEmpty())
			return false;

		return true;
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); //version
	}	
	
}
