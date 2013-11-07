package com.mixshare.rapid_evolution.data.mined.discogs.release;

import java.io.Serializable;

public class DiscogsUserRating implements Comparable<DiscogsUserRating>, Serializable {

	static private final long serialVersionUID = 0L;
    
    private String username;
    private float rating;
    
    public DiscogsUserRating() { }
    public DiscogsUserRating(String username, int rating) {
        this.username = username;
        this.rating = rating;
    }
    public DiscogsUserRating(String username, float rating) {
        this.username = username;
        this.rating = rating;
    }

	public boolean equals(Object o) {
		if (o instanceof DiscogsUserRating) {
			DiscogsUserRating oP = (DiscogsUserRating)o;
			if (oP.rating != rating)
				return false;
			if (!oP.username.equalsIgnoreCase(username))
				return false;
			return true;
		}
		return false;
	}        
    
    public String getUsername() { return username; }
    public float getRating() { return rating; }
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(getUsername());
        result.append(" (");
        result.append(String.valueOf(getRating()));
        result.append(")");
        return result.toString();
    }

    public int compareTo(DiscogsUserRating r) {
        if (getRating() > r.getRating())
            return -1;
        if (getRating() < r.getRating())
            return 1;
        return 0;
    }
	public void setUsername(String username) {
		this.username = username;
	}
	public void setRating(float rating) {
		this.rating = rating;
	}
	
}
