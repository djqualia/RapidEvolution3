package com.mixshare.rapid_evolution.data.profile.filter.style;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class StyleProfile extends FilterProfile {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(StyleProfile.class);

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public StyleProfile() { };
    public StyleProfile(StyleIdentifier styleId, int uniqueId) {
    	record = new StyleRecord(styleId, uniqueId);
    }
    public StyleProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	description = lineReader.getNextLine();
    }

    ////////////
    // FIELDS //
    ////////////

    private String description;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(StyleProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("styleName") || pd.getName().equals("categoryOnly")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    /////////////
    // GETTERS //
    /////////////

    public StyleIdentifier getStyleIdentifier() { return (getStyleRecord() != null) ? getStyleRecord().getStyleIdentifier() : null; }

    public StyleRecord getStyleRecord() { return (StyleRecord)record; }

    public String getStyleName() { return (getStyleRecord() != null) ? getStyleRecord().getStyleName() : ""; }

	public boolean isCategoryOnly() { return (getStyleRecord() != null) ? getStyleRecord().isCategoryOnly() : false; }

	public String getDescription() {
		if (description == null)
			return "";
		return description;
	}

	/////////////
	// SETTERS //
	/////////////

	public void setCategoryOnly(boolean categoryOnly) { getStyleRecord().setCategoryOnly(categoryOnly); }

	public void setDescription(String description) { this.description = description; }

    public void setStyleName(String styleName) throws AlreadyExistsException {
    	if (!getStyleName().equals(styleName)) {
    		StyleIdentifier oldStyleId = getStyleIdentifier();
    		StyleIdentifier newStyleId = new StyleIdentifier(styleName);
    		boolean unlocked = false;
    		try {
    			getRecord().getWriteLockSem().startRead("setStyleName");
    			updateIdentifier(newStyleId, oldStyleId);
    			getRecord().getWriteLockSem().endRead();
    			unlocked = true;
    		} catch (InterruptedException e) {
    		} finally {
    			if (!unlocked)
    				getRecord().getWriteLockSem().endRead();
    		}
    	}
    }


	/////////////
	// METHODS //
	/////////////

	@Override
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> relatedRecords = super.mergeWith(profile);
		StyleProfile styleProfile = (StyleProfile)profile;
		// description
    	if (styleProfile.getDescription().length() > 0) {
    		if (description.length() > 0)
    			description += ", ";
    		description += styleProfile.getDescription();
    	}
    	return relatedRecords;
	}

    @Override
	protected Record readRecord(LineReader lineReader) {
    	return new StyleRecord(lineReader);
    }

    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1", "StyleProfile.version"); // version
    	writer.writeLine(description);
    }

}
