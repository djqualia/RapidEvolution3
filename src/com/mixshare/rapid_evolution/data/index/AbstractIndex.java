package com.mixshare.rapid_evolution.data.index;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.exceptions.UnknownErrorException;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class AbstractIndex implements Index {

	abstract public Profile add(SubmittedProfile submittedProfile) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException;
	abstract public Profile addOrUpdate(SubmittedProfile submittedProfile) throws InsufficientInformationException, UnknownErrorException;
	
	abstract public boolean delete(Integer id);
	
	abstract protected void initProfile(Profile profile, SubmittedProfile initialValues);

	abstract public void mergeProfiles(Profile primaryProfile, Profile mergedProfile);
		
	abstract public void update(Record record);
	
	abstract public Vector<SearchResult> searchRecords(SearchParameters searchParameters);
	
	abstract public void write(LineWriter writer);
	
	
}
