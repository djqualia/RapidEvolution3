package com.mixshare.rapid_evolution.data.profile.filter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.profile.HierarchicalProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class FilterProfile extends HierarchicalProfile {

    static private Logger log = Logger.getLogger(FilterProfile.class);

	////////////
	// FIELDS //
	////////////

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(FilterProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("artistRecords") || pd.getName().equals("labelRecords") || pd.getName().equals("releaseRecords") || pd.getName().equals("songRecords")) {
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

    public FilterProfile() { super(); }
    public FilterProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }

	/////////////
	// GETTERS //
	/////////////

	public FilterRecord getFilterRecord() { return (FilterRecord)record; }

	public Vector<SearchResult> getArtistRecords() { return (getFilterRecord() != null) ? getFilterRecord().getArtistRecords() : null; }
	public Vector<SearchResult> getLabelRecords() { return (getFilterRecord() != null) ? getFilterRecord().getLabelRecords() : null; }
	public Vector<SearchResult> getReleaseRecords() { return (getFilterRecord() != null) ? getFilterRecord().getReleaseRecords() : null; }
	public Vector<SearchResult> getSongRecords() { return (getFilterRecord() != null) ? getFilterRecord().getSongRecords() : null; }

	/////////////
	// METHODS //
	/////////////

	public boolean matches(SearchRecord searchRecord) {
		return getFilterRecord().matches(searchRecord);
	}

	@Override
	public void update(SubmittedProfile submittedProfile, boolean overwrite) {

	}

	@Override
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> result = super.mergeWith(profile);
		getFilterRecord().computeNumArtistRecords();
		getFilterRecord().computeNumLabelRecords();
		getFilterRecord().computeNumReleaseRecords();
		getFilterRecord().computeNumSongRecords();
		getFilterRecord().computeNumExternalArtistRecords();
		getFilterRecord().computeNumExternalLabelRecords();
		getFilterRecord().computeNumExternalReleaseRecords();
		getFilterRecord().computeNumExternalSongRecords();
		return result;
	}

    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1", "FilterProfile.version"); // version
    }

}
