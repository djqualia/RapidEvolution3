package com.mixshare.rapid_evolution.data.profile.filter.tag;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class TagProfile extends FilterProfile {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(TagProfile.class);

    ////////////
    // FIELDS //
    ////////////

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(TagProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("tagName")) {
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

    public TagProfile() { };
    public TagProfile(TagIdentifier tagId, int uniqueId) {
    	record = new TagRecord(tagId, uniqueId);
    }
    public TagProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }

    /////////////
    // GETTERS //
    /////////////

    public TagRecord getTagRecord() { return (TagRecord)record; }

    public TagIdentifier getTagIdentifier() { return (getTagRecord() != null) ? getTagRecord().getTagIdentifier() : null; }

    public String getTagName() { return (getTagRecord() != null) ? getTagRecord().getTagName() : ""; }

	public boolean isCategoryOnly() { return (getTagRecord() != null) ? getTagRecord().isCategoryOnly() : false; }

	/////////////
	// SETTERS //
	/////////////

    public void setTagName(String tagName) throws AlreadyExistsException {
    	if (!getTagName().equals(tagName)) {
    		TagIdentifier oldTagId = getTagIdentifier();
    		TagIdentifier newTagId = new TagIdentifier(tagName);
    		boolean unlocked = false;
    		try {
    			getRecord().getWriteLockSem().startRead("setTagName");
    			updateIdentifier(newTagId, oldTagId);
    			getRecord().getWriteLockSem().endRead();
    			unlocked = true;
    		} catch (InterruptedException e) {
    		} finally {
    			if (!unlocked)
    				getRecord().getWriteLockSem().endRead();
    		}
    	}
    }

	public void setCategoryOnly(boolean categoryOnly) { if (getTagRecord() != null) getTagRecord().setCategoryOnly(categoryOnly); }

	/////////////
	// METHODS //
	/////////////

	@Override
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> relatedRecords = super.mergeWith(profile);
		return relatedRecords;
	}

    @Override
	protected Record readRecord(LineReader lineReader) {
    	return new TagRecord(lineReader);
    }

    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1", "TagProfile.version"); // version
    }

}
