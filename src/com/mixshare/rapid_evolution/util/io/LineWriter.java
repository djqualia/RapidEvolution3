package com.mixshare.rapid_evolution.util.io;


public interface LineWriter {
    
    public void writeLine(boolean value);
    public void writeLine(int value);
    public void writeLine(long value);
    public void writeLine(byte value);
    public void writeLine(float value);
    public void writeLine(double value);
    public void writeLine(short value);    
    public void writeLine(String text);
    public void writeLine(Object text, String annotation);
    public void close();
    
}