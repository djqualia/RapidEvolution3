package com.mixshare.rapid_evolution.data.mined.discogs.release;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

public class DiscogsReleaseRecommendations implements Serializable {

    static private Logger log = Logger.getLogger(DiscogsReleaseRecommendations.class);
	static private final long serialVersionUID = 0L;    

    private String releaseId;
    private Vector<Integer> recommendedIds = new Vector<Integer>();
    
    public DiscogsReleaseRecommendations() { }
    public DiscogsReleaseRecommendations(String htmlData) {
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
            
            int lookAfter = startIndex + 1;
            suffix = "?";
            boolean done = false;
            
            while (!done) {
                startIndex = htmlData.indexOf(prefix, lookAfter);
                if (startIndex > 0) {
                    int endIndex = htmlData.indexOf(suffix, startIndex + 1);
                    if (endIndex > 0) {
                        String releaseId = htmlData.substring(startIndex + prefix.length(), endIndex);
                        try {
                        	Integer id = Integer.parseInt(releaseId);
                        	if (!recommendedIds.contains(id))
                        		recommendedIds.add(id);
                        } catch (Exception e) { }
                        lookAfter = endIndex;
                    }
                } else {
                    done = true;
                }
            }
            
            //if (log.isDebugEnabled())
            	//log.debug("DiscogsReleaseRecommendations(): recommended ids=" + recommendedIds);
        } catch (Exception e) {
            log.error("DiscogsReleaseRecommendations(): error", e);
        }
    }
    
    public String toString() { return String.valueOf(recommendedIds); }
    
    public boolean contains(Integer id) { return recommendedIds.contains(id); }
    public int getNumRecommendations() { return recommendedIds.size(); }
    public String getReleaseId() { return releaseId; }
    public Vector<Integer> getRecommendedIds() { return recommendedIds; }
    public int size() {
    	if (recommendedIds != null)
    		return recommendedIds.size();
    	return 0;    		
    }
    
    public void mergeWith(DiscogsReleaseRecommendations subResults) {
    	Iterator<Integer> iter = subResults.getRecommendedIds().iterator();
    	while (iter.hasNext()) {
    		recommendedIds.add((Integer)iter.next());
    	}
    }
	public void setReleaseId(String releaseId) {
		this.releaseId = releaseId;
	}
	public void setRecommendedIds(Vector<Integer> recommendedIds) {
		this.recommendedIds = recommendedIds;
	}
	
}
