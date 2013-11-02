package com.mixshare.rapid_evolution.util.io.writers;

import com.mixshare.rapid_evolution.util.io.LineWriter;

public abstract class AbstractLineWriter implements LineWriter {

	abstract public void writeLine(boolean value);
	abstract public void writeLine(int value);
	abstract public void writeLine(long value);
	abstract public void writeLine(byte value);
	abstract public void writeLine(float value);
	abstract public void writeLine(double value);
	abstract public void writeLine(short value);    
    abstract public void writeLine(String text);    
    abstract public void close();

}
