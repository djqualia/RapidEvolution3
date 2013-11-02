package com.mixshare.rapid_evolution.player;

public interface PlayerInterface {

    public boolean isFileSupported();
    
    /**
     * @return true if it is able to open and play the filename
     */
    public boolean open(String filename);
    
    public void start();
    public void pause();
    public void stop();
    public void close();        
    
    public boolean hasVideo();
    
    /**
     * @return double in seconds
     */
    public double getTotalTime();
    
    /**
     * Where percentage is between 0 and 1.  The player should obey
     * the current running state.  I.e. if it is not playing when this is
     * called, then it won't be playing after, and vice versa.
     */
    public void setPosition(double percentage);
    
    public void setVolume(double percentage);
    
    public void setCallBack(PlayerCallBack callBack);
    
}
