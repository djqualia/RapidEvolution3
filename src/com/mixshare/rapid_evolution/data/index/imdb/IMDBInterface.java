package com.mixshare.rapid_evolution.data.index.imdb;

import java.util.Iterator;
import java.util.Vector;

import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public interface IMDBInterface {

	public void setDataType(byte dataType);
	
	public int getSize();
	
	public Vector<Integer> getIds();
	public Iterator<Integer> getIdsIterator();

	public int getNextAvailableUniqueId();
	
	public int getUniqueIdFromIdentifier(Identifier id);	
	public Identifier getIdentifierFromUniqueId(int uniqueId);
	public void setUniqueIdForIdentifier(int uniqueId, Identifier identifier);
	public void updateIdentifier(Identifier newId, Identifier oldId);	
	
	public Record get(Integer uniqueId);
	public void put(Record result);
	public void remove(int uniqueId);	
	
	public void update(Record record);
	
	public boolean doesExist(Integer uniqueId);
			
	public void addDuplicateMapping(int duplicateId, Record record);
	public void removeDuplicateId(int uniqueId);
		
	public Vector<SearchResult> searchRecords(SearchParameters searchParameters, int maxResults);
	public int searchCount(SearchParameters searchParameters);
	
	public short getNextUserDataTypeIdAndIncrement();
	public Vector<UserDataType> getUserDataTypes();
	public void addUserDataType(UserDataType userDataType);

	public void init(LineReader reader);
	public void write(LineWriter writer);
	
	public void computeSearchScores(SearchParameters searchParams);
	
	public void commit();
	public void close();
	
}
