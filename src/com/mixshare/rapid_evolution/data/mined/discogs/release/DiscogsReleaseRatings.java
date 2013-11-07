package com.mixshare.rapid_evolution.data.mined.discogs.release;

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;

public class DiscogsReleaseRatings implements Serializable {

    private static final long serialVersionUID = 0L;
    
    static private Logger log = Logger.getLogger(DiscogsReleaseRatings.class);
    
    private String releaseId;
    private Vector<String> users = new Vector<String>();
    private Vector<Integer> ratings = new Vector<Integer>();
    
    /**
     * This constructor is used for unit testing only
     */
    public DiscogsReleaseRatings() { }
    
    /**
     * This constructor is used for unit testing only
     */
    /*
    public DiscogsReleaseRatings(Vector<DiscogsRatingPair> ratings) {
    	for (DiscogsRatingPair pair : ratings) {
    		addRating(pair.getUser(), pair.getRating());
    	}
    }
    */

    public DiscogsReleaseRatings(String htmlData) {
        try {
            String prefix = "/release/";
            String suffix = "\"";
            int startIndex = htmlData.indexOf(prefix);
            if (startIndex > 0) {
                int endIndex = htmlData.indexOf(suffix, startIndex + 1);
                if (endIndex > 0) {
                    releaseId = htmlData.substring(startIndex + prefix.length(), endIndex);
                }
            }
            
            prefix = "Ratings:</h3><table";
            suffix = "</table>";
            startIndex = htmlData.indexOf(prefix);
            if (startIndex > 0) {
                int endIndex = htmlData.indexOf(suffix, startIndex + 1);
                if (endIndex > 0) {
                    String content = htmlData.substring(startIndex + prefix.length(), endIndex);
             
                    int lookAfter = 0;
                    prefix = "/user/";
                    suffix = "\"";
                    boolean done = false;
                    
                    while (!done) {
                        startIndex = content.indexOf(prefix, lookAfter);
                        if (startIndex > 0) {
                            endIndex = content.indexOf(suffix, startIndex + 1);
                            if (endIndex > 0) {
                                String userName = content.substring(startIndex + prefix.length(), endIndex);
                                String prefix2 = "</a> (";
                                startIndex = content.indexOf(prefix2, endIndex);
                                endIndex = content.indexOf("/", startIndex + prefix2.length());
                                int rating = Integer.parseInt(content.substring(startIndex + prefix2.length(), endIndex));
                                users.add(userName);
                                ratings.add(new Integer(rating));
                                lookAfter = endIndex;
                            }
                        } else {
                            done = true;
                        }
                    }                    
                }
            }
            
        } catch (Exception e) {
            log.error("DiscogsReleaseRatings(): error", e);
        }
    }
    
    public void addRating(String userName, int rating) {
    	users.add(userName);
    	ratings.add(rating);
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("RELEASE ID=");
        result.append(releaseId);
        result.append("\nUSERS=");
        result.append(users);
        result.append("\nRATINGS=");
        result.append(ratings);
        result.append("\n# RATINGS=");
        result.append(getNumRatings());
        result.append("\nAVG. RATING=");
        result.append(getAverageRating());
        return result.toString();
    }
    
    public int getRatingForUser(String username) {
    	for (int u = 0; u < users.size(); ++u) {
    		if (username.equalsIgnoreCase(users.get(u).toString()))
    			return ((Integer)ratings.get(u)).intValue();
    	}
    	return -1;
    }
    
    public float getAverageRating() {
        float total = 0.0f;
        for (int i = 0; i < ratings.size(); ++i) {
            int rating = ((Integer)ratings.get(i)).intValue();
            total += rating;
        }
        return total / ratings.size();
    }
    
    public String getReleaseId() {
        return releaseId;
    }
    
    public int getNumRatings() {
        return users.size();
    }
    
    public String getUser(int index) {
        return (String)users.get(index);
    }
    
    public int getRating(int index) {
        return ((Integer)ratings.get(index)).intValue();
    }
    
    public Vector<String> getUsers() { return users; }
    public Vector<Integer> getRatings() { return ratings; }

	public void setReleaseId(String releaseId) {
		this.releaseId = releaseId;
	}

	public void setUsers(Vector<String> users) {
		this.users = users;
	}

	public void setRatings(Vector<Integer> ratings) {
		this.ratings = ratings;
	}
    
}
