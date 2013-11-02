package com.mixshare.rapid_evolution.util.io;

import com.mixshare.rapid_evolution.util.io.writers.XMLLineWriter;

public class LineWriterFactory {

	static public LineWriter getLineWriter(String filename) {
		//return new PlainTextLineWriter(filename);
		return new XMLLineWriter(filename);
	}
	
}
