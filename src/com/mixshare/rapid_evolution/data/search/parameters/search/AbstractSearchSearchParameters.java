package com.mixshare.rapid_evolution.data.search.parameters.search;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.util.io.LineReader;

public abstract class AbstractSearchSearchParameters extends CommonSearchParameters {
	
	public AbstractSearchSearchParameters() { super(); }
	public AbstractSearchSearchParameters(AbstractSearchSearchParameters copy) {
		super(copy);
	}
	public AbstractSearchSearchParameters(LineReader lineReader) {
		super(lineReader);
	}
	
	abstract public Vector<SearchProfile> fetchRelativeProfiles();
	abstract public void initRelativeProfile(SearchProfile relativeProfile);

}
