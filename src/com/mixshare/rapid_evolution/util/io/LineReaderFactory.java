package com.mixshare.rapid_evolution.util.io;

import java.io.FileNotFoundException;

import com.mixshare.rapid_evolution.util.io.readers.PlainTextLineReader;
import com.mixshare.rapid_evolution.util.io.readers.XMLLineReader;

public class LineReaderFactory {

	static public LineReader getLineReader(String filename) throws FileNotFoundException {
		return getLineReader(filename, false);
	}
	static public LineReader getLineReader(String filename, boolean useSeparateThread) throws FileNotFoundException {
		PlainTextLineReader lineReader = new PlainTextLineReader(filename);
		String firstLine = lineReader.getNextLine();
		lineReader.close();
		if (firstLine != null) {
			if (firstLine.startsWith("<?xml")) {
				return new XMLLineReader(filename, useSeparateThread);
			} else {
				return new PlainTextLineReader(filename);
			}
		} else {
			return null;
		}
	}
	
}
