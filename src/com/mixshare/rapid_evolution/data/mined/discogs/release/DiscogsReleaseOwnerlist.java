package com.mixshare.rapid_evolution.data.mined.discogs.release;

import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;

public class DiscogsReleaseOwnerlist implements Serializable {

    private static final long serialVersionUID = 0L;
    
    static private Logger log = Logger.getLogger(DiscogsReleaseOwnerlist.class);
    
    private String releaseId;
    private Vector<String> users = new Vector<String>();
    
    public DiscogsReleaseOwnerlist() { }
    public DiscogsReleaseOwnerlist(String htmlData) {
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
            
            
            prefix = "members have this:</h3><table";
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
                                users.add(userName);
                                lookAfter = endIndex;
                            }
                        } else {
                            done = true;
                        }
                    }
                    
                }
            }
            
            
        } catch (Exception e) {
            log.error("DiscogsReleaseOwnerlist(): error", e);
        }
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(users);
        return result.toString();
    }
        
    public String getReleaseId() {
        return releaseId;
    }
    
    public int getNumUsers() {
        return users.size();
    }
    
    public String getUser(int index) {
        return (String)users.get(index);
    }
    
    public Vector<String> getUsers() { return users; }
    
	public void setReleaseId(String releaseId) {
		this.releaseId = releaseId;
	}
	public void setUsers(Vector<String> users) {
		this.users = users;
	}
	
}
