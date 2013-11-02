package com.mixshare.rapid_evolution.player;

public interface PlayerCallBack {

    /**
     * Where value is between 0 and 1
     */
    public void setPosition(double value);
        
    public void donePlayingSong();
    
    public void setIsPlaying(boolean playing);
    
    public void playerError(String description);
    
}
