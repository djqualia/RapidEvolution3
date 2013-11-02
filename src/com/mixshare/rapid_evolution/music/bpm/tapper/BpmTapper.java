package com.mixshare.rapid_evolution.music.bpm.tapper;

import org.apache.log4j.Logger;

import com.trolltech.qt.gui.QApplication;

public class BpmTapper {
	
	static private Logger log = Logger.getLogger(BpmTapper.class);
	
	////////////
	// FIELDS //
	////////////
	    
    private int m_numTicks = -1;
    private double m_timeTotal, m_timeMark;
    private double m_threshold = .05;
    private double m_tolerance = .25;
    private int m_stablize = 8;
    private boolean m_bRegulate = true;
    private int m_regulationcutoff = 4;
    private double m_beatmultiplierround = 0.2;
    private double m_beatdividerround = 0.1;
    private int m_reset = 5;
    private boolean m_bBpmSmartRound = true;
    private double m_responsedeviation = 0.01;

    private BpmTapperListener listener;
    private DelayThread instance;
	
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public BpmTapper(BpmTapperListener listener) {
    	this.listener = listener;
    }

    /////////////
    // METHODS //
    /////////////
    
    public void tap() {
    	double timerValue = ((double)System.currentTimeMillis()) / 1000.0;
        double beatTime, avgTime, multiplier, divider, factor = 0.0;
        beatTime = timerValue - m_timeMark;
        if (m_numTicks == -1) {
        	// initialize measurement variables
        	m_numTicks = 0;
        	m_timeTotal = 0;
        } else if (m_numTicks == 0) {
        	// got first beat ...
        	m_numTicks++;
        	m_timeTotal += beatTime;
        } else if (!(beatTime < m_threshold)) {
        	boolean detectbeat = true;
        	while (detectbeat) {
        		detectbeat = false;
        		avgTime = m_timeTotal / m_numTicks;
        		if ((Math.abs(beatTime - avgTime) / avgTime) <= m_tolerance) {
        			m_timeTotal += beatTime;
        			m_numTicks++;
        	    	if (m_numTicks >= m_stablize) {
        	    		double roundedBpm = roundBpm(avgTime);
        	    		listener.setBpm(roundedBpm);
        	    		if (instance == null) {
        	    			instance = new DelayThread(roundedBpm, (long)(avgTime * 1000 * 3));
        	    			instance.start();
        	    		} else {
        	    			instance.newTap(roundedBpm, (long)(avgTime * 1000 * 3));
        	    		}
        	        }
        		} else if ((Math.abs(beatTime - avgTime) / avgTime) >= m_reset) {
        			m_numTicks = -1;
        			listener.resetBpm();
        		} else if (m_bRegulate && (m_numTicks >= m_stablize)) {
        			multiplier = avgTime / beatTime;
        			divider = beatTime / avgTime;
        			if ((Math.round(multiplier) <= m_regulationcutoff) && (Math.round(divider) <= m_regulationcutoff)) {
        				if (Math.abs(Math.round(multiplier) - multiplier) < m_beatmultiplierround) {
        					// regulation beat to multiplier
        					beatTime *= Math.round( multiplier );
        					factor = multiplier;
        					detectbeat = true;
        				} else if (Math.abs( Math.round(divider) - divider ) < m_beatdividerround) {
        					// regulate beat to divider
        					beatTime /= Math.round( divider );
        					factor = divider;
        					detectbeat = true;
        				}
        			}
        		}
        	}
        }
        m_timeMark = timerValue;
    }

    /**
     * Helper method so it can be easily connected to Qt buttons...
     */
    public void tap(Boolean checked) { tap(); }
    
    private double roundBpm(double time) {
    	double rounder;
        double bpm = 60.0 / time;
        if (m_bBpmSmartRound) {
        	rounder = m_responsedeviation / Math.sqrt((double) m_numTicks) * bpm * bpm / 60.0;
        	if (rounder < 0.1) rounder = 0.1;
        	else if (rounder < 0.2)	rounder = 0.2;
        	else if (rounder < 0.5) rounder = 0.5;
        	else if (rounder < 1) rounder = 1;
        	else rounder = 2;
        } else {
        	rounder = 0.1;
        }
        return Math.round( bpm / rounder ) * rounder;
    }

    private class DelayThread extends Thread {
    	private double bpm;
    	private long startTime;
    	private long delay;
    	public DelayThread(double bpm, long delay) {    		
    		this.bpm = bpm;
    		this.delay = delay;
    		this.startTime = System.currentTimeMillis();
    		if (log.isDebugEnabled())
    			log.debug("DelayThread(): delaying=" + delay);
    	}
    	public void run() {
    		try {
    			while (true) {
    				Thread.sleep(delay / 2);
    				if (System.currentTimeMillis() - startTime >= delay) {
    					if (log.isDebugEnabled())
    						log.debug("run(): final tap detected");
    					QApplication.invokeAndWait(new Thread() { public void run() { 
    						listener.finalBpm(bpm);
    					}});
    					return;
    				}
    			}
    		} catch (Exception e) {
    			log.error("DelayThread(): error", e);
    		} finally {
    			instance = null;
    		}
    	}
    	public void newTap(double bpm, long delay) {
    		this.bpm = bpm;
    		this.delay = delay;
    		startTime = System.currentTimeMillis();
    	}
    }
    
}
