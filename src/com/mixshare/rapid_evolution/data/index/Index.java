package com.mixshare.rapid_evolution.data.index;

import java.util.Iterator;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.exceptions.UnknownErrorException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * An index provides uniform access to all records of a certain type and their corresponding profiles.
 * Access to records will be fast since they should reside in memory, while profiles will be stored in the hard disk...
 */
public interface Index {

	public Record getRecord(Integer uniqueId);	
	public Record getRecord(Identifier id);
	public Profile getProfile(Integer uniqueId);
	public Profile getProfile(Identifier id);
	
	public Profile addOrUpdate(SubmittedProfile submittedProfile) throws InsufficientInformationException, UnknownErrorException;
	public Profile add(SubmittedProfile submittedProfile) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException;
	public boolean delete(Identifier id);
	public boolean delete(Integer uniqueId);
	public void update(Record record);
	
	public void mergeProfiles(Profile primaryProfile, Profile mergedProfile);
	
	public int getSize();
	
	// accessors for the index's collection
	public Vector<Integer> getIds();
	public Iterator<Integer> getIdsIterator();

	public int getUniqueIdFromIdentifier(Identifier id);	
	public Identifier getIdentifierFromUniqueId(int uniqueId);		
	
	public void updateIdentifier(Profile profile, Identifier newId, Identifier oldId) throws AlreadyExistsException;
	public void updateIdentifierEquivalent(Profile profile, Identifier newId, Identifier oldId) throws AlreadyExistsException;
	
	/**
	 * Determines if a record with the specified identifier exists.
	 */
	public boolean doesExist(Identifier id);
	public boolean doesExist(Integer uniqueId);
	public boolean doesExist(SubmittedProfile submittedProfile);
		
	/**
	 * Each index will have an associated model manager, so data model updates can be propagated to the UI...
	 */
	public ModelManagerInterface getModelManager();

	/** Used to be able to present normalized search scores in non-lazy mode */
	public void computeSearchScores(SearchParameters searchParams);
	
	public Vector<SearchResult> searchRecords(SearchParameters searchParameters);
	public Vector<SearchResult> searchRecords(SearchParameters searchParameters, int maxResults);
	
	public SearchParameters getNewSearchParameters();
	
	public void write(LineWriter writer);
	
}
