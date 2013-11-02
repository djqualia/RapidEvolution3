package com.mixshare.rapid_evolution.data.profile;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.identifier.Identifiable;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.record.CommonRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class CommonProfile extends AbstractProfile implements Identifiable, Serializable, DataConstants, Comparable<Profile> {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(CommonProfile.class);

    ////////////
    // FIELDS //
    ////////////

    protected Record record;

    protected byte ratingSource = DATA_SOURCE_UNKNOWN; // keeps track of where the rating came from (user, calculated, discogs, etc)

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(CommonProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("rating") || pd.getName().equals("lastModified") || pd.getName().equals("disabled")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public CommonProfile() { }
    public CommonProfile(LineReader lineReader) {
    	int version = Integer.parseInt(lineReader.getNextLine());
    	record = readRecord(lineReader);
    	ratingSource = Byte.parseByte(lineReader.getNextLine());
    }

    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    abstract protected Record readRecord(LineReader lineReader);

    /////////////
    // GETTERS //
    /////////////

    @Override
	public Identifier getIdentifier() { return (record != null) ? record.getIdentifier() : null; }
	@Override
	public int getUniqueId() { return (record != null) ? record.getUniqueId() : null; }

	@Override
	public Record getRecord() { return record; }
	public CommonRecord getCommonRecord() { return (CommonRecord)record; }

	@Override
	public int getNumDuplicateIds() { return (record != null) ? record.getNumDuplicateIds() : null; }
	@Override
	public int getDuplicateId(int index) { return (record != null) ? record.getDuplicateId(index) : null; }

    @Override
	public Rating getRating() { return (record != null) ? getCommonRecord().getRatingValue() : null; }
    @Override
	public byte getRatingSource() { return ratingSource; }

    public Date getLastModifiedDate() { return (record != null) ? getCommonRecord().getLastModifiedDate() : null; }
    @Override
	public long getLastModified() { return (record != null) ? record.getLastModified() : null; }

    @Override
	public boolean isDisabled() { return (record != null) ? record.isDisabled() : false; }

    @Override
	public String toString() { return (record != null) ? record.toString() : null; }

    /////////////
    // SETTERS //
    /////////////

    /**
     * This method is only used during the Profile loading process, and shouldn't be called otherwise.
     */
	@Override
	public void setRecord(Record record) { this.record = record; }

	@Override
	public void setRating(Rating rating, byte source) {
		getCommonRecord().setRatingValue(rating);
		ratingSource = source;
	}

	public void addDuplicateIdentifier(int duplicateId) { getCommonRecord().addDuplicateIdentifier(duplicateId); }
	public void removeDuplicateIdentifier(int duplicateId) { getCommonRecord().removeDuplicateIdentifier(duplicateId); }

	@Override
	public void setLastModified() { record.setLastModified(); }

	@Override
	public void setDisabled(boolean disabled) { record.setDisabled(disabled); }

	// for serialization
	public void setRatingSource(byte ratingSource) {
		this.ratingSource = ratingSource;
	}

	/////////////
	// METHODS //
	/////////////

	@Override
	protected void updateIdentifier(Identifier newId, Identifier oldId) throws AlreadyExistsException {
		if (newId.equals(oldId)) {
			getCommonRecord().getIndex().updateIdentifierEquivalent(this, newId, oldId);
			getCommonRecord().update();
		} else {
			getCommonRecord().getIndex().updateIdentifier(this, newId, oldId);
			getCommonRecord().update();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CommonProfile) {
			CommonProfile c = (CommonProfile)o;
			return getRecord().equals(c.getRecord());
		}
		return false;
	}

	@Override
	public int hashCode() { return getRecord().hashCode(); }

	@Override
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> relatedRecords = new HashMap<Record, Object>();
		record.mergeWith(profile.getRecord(), relatedRecords);
		return relatedRecords;
	}

	/**
	 * Call this method after making any changes to save to disk and update the UI.
	 */
	@Override
	public boolean save() {
		boolean result = false;
		try {
			record.update();
			record.getWriteLockSem().startWrite("save");
			result = ProfileManager.saveProfile(this);
		} catch (Exception e) {
			log.error("save(): error", e);
		} finally {
			record.getWriteLockSem().endWrite();
		}
		return result;
	}

	@Override
	public int compareTo(Profile r) { return toString().compareToIgnoreCase(r.toString()); }

    @Override
	public void write(LineWriter writer) {
    	writer.writeLine("1", "CommonProfile.version"); // version
    	record.write(writer);
    	writer.writeLine(String.valueOf(ratingSource), "ratingSource");
    }

}
