package com.mixshare.rapid_evolution.util.io.readers;

import com.mixshare.rapid_evolution.util.io.LineReader;

public abstract class AbstractLineReader implements LineReader {

	abstract public String getNextLine();    
	abstract public void close();
    
}
