package com.mixshare.rapid_evolution.data.profile.search;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.ReleaseGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.DegreeValueSetAverager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;

abstract public class ReleaseGroupProfile extends SongGroupProfile {

    static private Logger log = Logger.getLogger(ReleaseGroupProfile.class);

    static private float STYLE_DISCARD_THRESHOLD = RE3Properties.getFloat("style_discard_threshold");
    static private float TAG_DISCARD_THRESHOLD = RE3Properties.getFloat("tag_discard_threshold");

    ////////////
    // FIELDS //
    ////////////

    protected Map<Integer, Object> associatedReleases = new HashMap<Integer, Object>();

    protected transient RWSemaphore releaseIdSem;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(ReleaseGroupProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("releaseIdSem")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public ReleaseGroupProfile() { super(); }
    public ReleaseGroupProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	int numAssociatedReleases = Integer.parseInt(lineReader.getNextLine());
    	associatedReleases = new HashMap<Integer, Object>(numAssociatedReleases);
    	for (int i = 0; i < numAssociatedReleases; ++i)
    		associatedReleases.put(Integer.parseInt(lineReader.getNextLine()), null);
    }

    /////////////
    // GETTERS //
    /////////////

    public ReleaseGroupRecord getReleaseGroupRecord() { return (ReleaseGroupRecord)record; }

    public short getNumReleases() { return getReleaseGroupRecord().getNumReleases(); }

    public boolean containsRelease(Integer releaseId) {
    	return associatedReleases.containsKey(releaseId);
    }
    public Vector<Integer> getReleaseIds() {
    	Vector<Integer> result = new Vector<Integer>(associatedReleases.size());
    	try {
    		getReleaseIdSem().startRead("getReleaseIds");
	    	Iterator<Integer> iter = associatedReleases.keySet().iterator();
	    	while (iter.hasNext())
	    		result.add(iter.next());
    	} catch (Exception e) {
    		log.error("getReleaseIds(): error", e);
    	} finally {
    		getReleaseIdSem().endRead();
    	}
    	return result;
    }

    public RWSemaphore getReleaseIdSem() {
    	if (releaseIdSem == null)
    		releaseIdSem = new RWSemaphore(-1);
    	return releaseIdSem;
    }

    // for serialization
	public Map<Integer, Object> getAssociatedReleases() { return associatedReleases; }

    /////////////
    // SETTERS //
    /////////////

    public void addRelease(Integer releaseId) {
    	try {
    		getRecord().getWriteLockSem().startRead("addRelease");
    		getReleaseIdSem().startWrite("addRelease");
    		associatedReleases.put(releaseId, null);
    	} catch (Exception e) {
    		log.error("addRelease(): error", e);
    	} finally {
    		getReleaseIdSem().endWrite();
    		getRecord().getWriteLockSem().endRead();
    	}
		getReleaseGroupRecord().setNumReleases((short)associatedReleases.size());
    }

    public void removeReleases(Vector<Integer> releaseIds) {
    	try {
    		getRecord().getWriteLockSem().startRead("removeRelease");
    		getReleaseIdSem().startWrite("removeRelease");
    		for (int releaseId : releaseIds)
    			associatedReleases.remove(releaseId);
    	} catch (Exception e) {
    		log.error("removeRelease(): error", e);
    	} finally {
    		getReleaseIdSem().endWrite();
    		getRecord().getWriteLockSem().endRead();
    	}
		getReleaseGroupRecord().setNumReleases((short)associatedReleases.size());
    }

    public void removeAssociatedReleasesBeyond(int releaseId) {
    	Vector<Integer> idsToRemove = new Vector<Integer>();
    	for (Entry<Integer, Object> entry : associatedReleases.entrySet()) {
    		if (entry.getKey() >= releaseId)
    			idsToRemove.add(entry.getKey());
    	}
    	for (Integer removedReleaseId : idsToRemove)
    		associatedReleases.remove(removedReleaseId);
    }

    public void removeRelease(Integer releaseId) {
    	try {
    		getRecord().getWriteLockSem().startRead("removeRelease");
    		getReleaseIdSem().startWrite("removeRelease");
    		associatedReleases.remove(releaseId);
    	} catch (Exception e) {
    		log.error("removeRelease(): error", e);
    	} finally {
    		getReleaseIdSem().endWrite();
    		getRecord().getWriteLockSem().endRead();
    	}
		getReleaseGroupRecord().setNumReleases((short)associatedReleases.size());
    }

    // for serialization
	public void setAssociatedReleases(Map<Integer, Object> associatedReleases) { this.associatedReleases = associatedReleases; }

    /////////////
    // METHODS //
    /////////////

	@Override
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> relatedRecords = super.mergeWith(profile);
		ReleaseGroupProfile groupProfile = (ReleaseGroupProfile)profile;
		// release
		try {
    		getReleaseIdSem().startWrite("mergeWith");
			Vector<Integer> otherReleaseIds = groupProfile.getReleaseIds();
			for (Integer otherReleaseId : otherReleaseIds) {
				if (!containsRelease(otherReleaseId))
					associatedReleases.put(otherReleaseId, null);
			}
		} catch (Exception e) { } finally {
			getReleaseIdSem().endWrite();
		}
		getReleaseGroupRecord().setNumReleases((short)associatedReleases.size());
		return relatedRecords;
	}

    public Vector<DegreeValue> computeStyleDegreesFromReleases() {
    	DegreeValueSetAverager stylesAverager = new DegreeValueSetAverager();
    	Vector<Integer> badReleaseIds = new Vector<Integer>();
    	try {
	    	getReleaseIdSem().startRead("computeStyleDegreesFromReleases");
	    	Iterator<Integer> releaseIdIter = associatedReleases.keySet().iterator();
	    	while (releaseIdIter.hasNext()) {
	    		Integer releaseId = releaseIdIter.next();
	    		SearchRecord searchRecord = Database.getReleaseIndex().getSearchRecord(releaseId);
	    		if (searchRecord != null) {
		    		Vector<DegreeValue> styleDegrees = searchRecord.getSourceStyleDegreeValues();
		    		if (styleDegrees.size() > 0)
		    			stylesAverager.addDegreeValueSet(styleDegrees, 1.0f);
	    		} else {
	    			if (log.isDebugEnabled())
	    				log.debug("computeMetadataFromReleases(): null release record for id=" + releaseId + ", identifier=" + Database.getReleaseIndex().getIdentifierFromUniqueId(releaseId));
	    			badReleaseIds.add(releaseId);
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("computeStyleDegreesFromReleases(): error", e);
    	} finally {
    		getReleaseIdSem().endRead();
    	}
    	if (badReleaseIds.size() > 0)
    		removeBadReleaseIds(badReleaseIds);
    	Vector<DegreeValue> result = stylesAverager.getDegrees();
    	for (int i = 0; i < result.size(); ++i) {
    		DegreeValue degree = result.get(i);
    		if (degree.getPercentage() < STYLE_DISCARD_THRESHOLD) {
    			result.remove(i);
    			--i;
    		}
    	}
    	return result;
    }

    public Vector<DegreeValue> computeTagDegreesFromReleases() {
    	DegreeValueSetAverager tagsAverager = new DegreeValueSetAverager();
    	Vector<Integer> badReleaseIds = new Vector<Integer>();
    	try {
	    	getReleaseIdSem().startRead("computeTagDegreesFromReleases");
	    	Iterator<Integer> releaseIdIter = associatedReleases.keySet().iterator();
	    	while (releaseIdIter.hasNext()) {
	    		Integer releaseId = releaseIdIter.next();
	    		SearchRecord searchRecord = Database.getReleaseIndex().getSearchRecord(releaseId);
	    		if (searchRecord != null) {
		    		Vector<DegreeValue> tagDegrees = searchRecord.getSourceTagDegreeValues();
		    		if (tagDegrees.size() > 0)
		    			tagsAverager.addDegreeValueSet(tagDegrees, 1.0f);
	    		} else {
	    			if (log.isDebugEnabled())
	    				log.debug("computeMetadataFromReleases(): null release record for id=" + releaseId + ", identifier=" + Database.getReleaseIndex().getIdentifierFromUniqueId(releaseId));
	    			badReleaseIds.add(releaseId);
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("computeAvgRatingFromReleases(): error", e);
    	} finally {
    		getReleaseIdSem().endRead();
    	}
    	if (badReleaseIds.size() > 0)
    		removeBadReleaseIds(badReleaseIds);
    	Vector<DegreeValue> result = tagsAverager.getDegrees();
    	for (int i = 0; i < result.size(); ++i) {
    		DegreeValue degree = result.get(i);
    		if (degree.getPercentage() < TAG_DISCARD_THRESHOLD) {
    			result.remove(i);
    			--i;
    		}
    	}
    	return result;
    }

    private void removeBadReleaseIds(Vector<Integer> badReleaseIds) {
    	try {
    		getReleaseIdSem().startWrite("removeBadReleaseIds");
    		for (int releaseId : badReleaseIds)
    			associatedReleases.remove(releaseId);
    	} catch (Exception e) {
    		log.error("removeBadReleaseIds(): error", e);
    	} finally {
    		getReleaseIdSem().endWrite();
    	}
    }

    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1"); // version
    	writer.writeLine(associatedReleases.size());
    	for (int uniqueId : associatedReleases.keySet())
    		writer.writeLine(uniqueId);
    }

}
