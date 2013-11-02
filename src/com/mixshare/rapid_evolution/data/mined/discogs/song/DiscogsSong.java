package com.mixshare.rapid_evolution.data.mined.discogs.song;

import java.io.Serializable;
import java.util.Vector;

import com.mixshare.rapid_evolution.util.StringUtil;

public class DiscogsSong implements Serializable {
    
    static private final long serialVersionUID = 0L;
    
    ////////////
    // FIELDS //
    ////////////
    
    private String position;
    private String title;
    private String duration;
    private Vector<String> artists;
    private Vector<String> remixers;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public DiscogsSong() { }
    public DiscogsSong(String position, String title, String duration, Vector<String> artists, Vector<String> remixers) {
        this.position = position == null ? "" : position;
        this.title = title == null ? "" : title;
        this.duration = duration == null ? "" : duration;
        this.artists = artists;
        this.remixers = remixers;
    }

    public DiscogsSong(String position, String title, String duration, String[] artists, String[] remixers) {
        this.position = position == null ? "" : position;
        this.title = title == null ? "" : title;
        this.duration = duration == null ? "" : duration;
        this.artists = new Vector<String>(artists.length);
        for (int i = 0; i < artists.length; ++i)
        	this.artists.add(artists[i]);
        this.remixers = new Vector<String>(remixers.length);
        for (int i = 0; i < remixers.length; ++i)
        	this.remixers.add(remixers[i]);
    }

    /////////////
    // GETTERS //
    /////////////
        
    public Vector<String> getArtists() { return artists; }
    public String getArtistDescription() {
    	StringBuffer result = new StringBuffer();
    	for (int a = 0; a < artists.size(); ++a) {
    		result.append(artists.get(a).toString());
    		if (a + 1 < artists.size())
    			result.append(" & ");
    	}
    	return result.toString();
    }
    
    public String getRemixersDescription() {
    	StringBuffer result = new StringBuffer();
    	for (int a = 0; a < remixers.size(); ++a) {
    		result.append(remixers.get(a).toString());
    		if (a + 1 < remixers.size())
    			result.append(" & ");
    	}
    	return result.toString();
    }

    public String getDuration() {
    	return StringUtil.validateTime(duration);
    }
    public String getPosition() {
    	if (position == null)
    		return "";
    	if ((position.length() == 1) && Character.isDigit(position.charAt(0)))
    		return "0" + position;
        return position;
    }
    public String getTitle() {
        return title;
    }
    
    public String getTitleNoRemix() {
		String title = getTitle();
		int endIndex = title.lastIndexOf(")");
		if (endIndex >= 0) {
			int startIndex = title.lastIndexOf(" (", endIndex);
			if (startIndex >= 0) {
				title = title.substring(0, startIndex);
			}
		}	
		return title;
    }
    
    public String getRemix() {
		String title = getTitle();
		String remix = "";
		int endIndex = title.lastIndexOf(")");
		if (endIndex >= 0) {
			int startIndex = title.lastIndexOf(" (", endIndex);
			if (startIndex >= 0) {
				remix = title.substring(startIndex + 2, endIndex);
			}
		}
		return remix;
    }

    static public boolean isValidTitle(String input) {
    	if (input == null)
    		return false;
    	if (input.length() == 0)
    		return false;
    	if (input.equalsIgnoreCase("Untitled"))
    		return false;
    	return true;
    }
    
    static public boolean isValidPosition(String input) {
    	if (input == null)
    		return false;
    	if (input.length() == 0)
    		return false;
    	return true;
    }

	public Vector<String> getRemixers() { return remixers; }
    
    /////////////
    // SETTERS //
    /////////////

    public void setDuration(String duration) { this.duration = duration; }
    public void setPosition(String position) { this.position = position; }
    public void setTitle(String title) { this.title = title; }
    
	public void setRemixers(Vector<String> remixers) {
		this.remixers = remixers;
	}
	public void setArtists(Vector<String> artists) {
		this.artists = artists;
	}
    
    /////////////
    // METHODS //
    /////////////
    
    public boolean equals(Object o) {
    	if (o instanceof DiscogsSong) {
    		DiscogsSong oT = (DiscogsSong)o;
    		if (!getPosition().equals(oT.getPosition()))
    			return false;
    		if (!title.equals(oT.getTitle()))
    			return false;
    		if ((duration != null) && !duration.equals(oT.getDuration()))
    			return false;
    		if (!getArtistDescription().equals(oT.getArtistDescription()))
    			return false;
    		if (!getRemixersDescription().equals(oT.getRemixersDescription()))    			
    			return false;
    		return true;
    	}
    	return false;
    }
    
    public int hashCode() {
    	StringBuffer data = new StringBuffer();
    	data.append(position);
    	data.append(title);
    	data.append(duration);
    	data.append(getArtistDescription());
    	data.append(getRemixersDescription());
    	return data.toString().hashCode();
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        if (artists.size() > 0) {
            for (int a = 0; a < artists.size(); ++a) {
                result.append(artists.get(a));
                if (a + 1 < artists.size())
                    result.append(" & ");
            }
            result.append(" - ");
        }
        if (position.length() > 0) {
            result.append(position);
            result.append(" - ");
        }
        result.append(title);
        if (remixers.size() > 0) {
        	result.append(" (remixed by: ");
        	for (int r = 0; r < remixers.size(); ++r) {
        		result.append(remixers.get(r));
        		if (r + 1 < remixers.size())
        			result.append(", ");
        	}
        	result.append(")");
        }        
        if (duration.length() > 0) {
            result.append(" [");
            result.append(duration);
            result.append("]");
        }
        return result.toString();
    }
    
}
