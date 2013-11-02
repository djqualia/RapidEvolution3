package com.mixshare.rapid_evolution.music.duration;

import java.io.Serializable;

import com.mixshare.rapid_evolution.util.StringUtil;

public class Duration implements Serializable, Comparable<Duration> {

    static private final long serialVersionUID = 0L;    

    ////////////
    // FIELDS //
    ////////////
    
	private int timeInMillis;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public Duration() { }
	
	public Duration(double timeInMillis) { setDurationInMillis((int)timeInMillis); }
	public Duration(int timeInMillis) { setDurationInMillis(timeInMillis); }
	public Duration(String time) { setDurationFromString(time);  }
	
	/////////////
	// GETTERS //
	/////////////
	
	public String getDurationAsString(boolean hideEmpty) { return StringUtil.getDurationAsString((int)getDurationInSeconds(), hideEmpty); }
	public String getDurationAsString() { return StringUtil.getDurationAsString((int)getDurationInSeconds()); }
	public float getDurationInSeconds() { return ((float)timeInMillis) / 1000.0f; }
	public int getDurationInMillis() { return timeInMillis; }

	public boolean isValid() { return timeInMillis != 0; }
	
	// for serialization
	public int getTimeInMillis() { return timeInMillis; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setDurationInMillis(int timeInMillis) { this.timeInMillis = timeInMillis; }
	public void setDurationInSeconds(int timeInSeconds) { this.timeInMillis = timeInSeconds * 1000; }
	public void setDurationFromString(String time) { this.timeInMillis = StringUtil.getDurationInSeconds(time) * 1000; }	
	
	// for serialization
	public void setTimeInMillis(int timeInMillis) { this.timeInMillis = timeInMillis; }
	
	/////////////
	// METHODS //
	/////////////
	
	public float getSimilarityWith(Duration otherDuration) { return getSimilarity(timeInMillis, otherDuration.timeInMillis); }	
	static public float getSimilarity(int timeInMillis1, int timeInMillis2) {
		if ((timeInMillis1 == 0) || (timeInMillis2 == 0))
			return 0.0f;
		float absDiff = Math.abs(timeInMillis1 - timeInMillis2);
		float total = timeInMillis1 + timeInMillis2;
		float similarity = (total - absDiff * 2.0f) / total;
		return similarity;		
	}
	
	public int compareTo(Duration d) {
		if ((timeInMillis != 0) && (d.timeInMillis != 0)) {
			if (timeInMillis < d.getDurationInMillis())
				return -1;
			else if (timeInMillis > d.getDurationInMillis())
				return 1;				
			return 0;
		}
		if (timeInMillis != 0)
			return -1;
		if (d.timeInMillis != 0)
			return 1;
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Duration) {
			Duration d = (Duration)o;
			return (getDurationInMillis() == d.getDurationInMillis());
		}		
		return false;
	}
	
	public int hashCode() { return timeInMillis; }
	
	public String toString() { return getDurationAsString(); }
	
}
