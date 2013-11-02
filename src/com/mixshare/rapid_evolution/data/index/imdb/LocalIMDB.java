package com.mixshare.rapid_evolution.data.index.imdb;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.search.SearchParser;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.data.util.table.UniqueIdTable;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.RWSemaphore;
import com.mixshare.rapid_evolution.util.timing.Semaphore;

public class LocalIMDB implements IMDBInterface, Serializable, DataConstants {

	static private Logger log = Logger.getLogger(LocalIMDB.class);
    static private final long serialVersionUID = 0L;

	static private long GET_IDS_SEM_TIMEOUT = RE3Properties.getLong("get_ids_sem_timeout_millis");
	static private long DUPLICATE_MAP_SEM_TIMEOUT = RE3Properties.getLong("duplicate_map_sem_timeout_millis");

	static public Version LUCENE_VERSION = Version.LUCENE_30;
	static private Analyzer analyzer;

	//////////////////
	// CONSTRUCTORS //
	//////////////////

	public LocalIMDB() { }
	public LocalIMDB(LineReader lineReader) {
		init(lineReader);
	}

	@Override
	public void init(LineReader lineReader) {
		int version = Integer.parseInt(lineReader.getNextLine());
		dataType = Byte.parseByte(lineReader.getNextLine());
		uniqueIdTable = new UniqueIdTable(lineReader);
		int numRecords = Integer.parseInt(lineReader.getNextLine());
		recordMap = new HashMap<Integer, Record>(numRecords);
		for (int i = 0; i < numRecords; ++i) {
			Record record = getRecord(lineReader);
			if (record != null)
				recordMap.put(record.getUniqueId(), record);
		}
		int numDuplicates = Integer.parseInt(lineReader.getNextLine());
		duplicateIdMap = new HashMap<Integer, Record>(numDuplicates);
		for (int i = 0; i < numDuplicates; ++i) {
			Integer uniqueId = null;
			if (version >= 2)
				uniqueId = Integer.parseInt(lineReader.getNextLine());
			Record record = getRecord(lineReader);
			if (record != null) {
				if (uniqueId == null)
					uniqueId = record.getUniqueId();
				duplicateIdMap.put(uniqueId, record);
			}
		}
		int numUserDataTypes = Integer.parseInt(lineReader.getNextLine());
		userDataTypes = new Vector<UserDataType>(numUserDataTypes);
		for (int i = 0; i < numUserDataTypes; ++i)
			userDataTypes.add(new UserDataType(lineReader));
		nextUserDataTypeId = Short.parseShort(lineReader.getNextLine());
		if (version >= 3) {
			String eol = lineReader.getNextLine(); // end of imdb line
			if (!eol.equals("## end of local imdb ##"))
				log.warn("init(): imdb out of sync, line=" + eol);
		}
	}

	private Record getRecord(LineReader lineReader) {
		Record record = null;
		if (dataType == DATA_TYPE_ARTISTS)
			record = new ArtistRecord(lineReader);
		else if (dataType == DATA_TYPE_LABELS)
			record = new LabelRecord(lineReader);
		else if (dataType == DATA_TYPE_RELEASES)
			record = new ReleaseRecord(lineReader);
		else if (dataType == DATA_TYPE_SONGS)
			record = new SongRecord(lineReader);
		else if (dataType == DATA_TYPE_MIXOUTS)
			record = new MixoutRecord(lineReader);
		else if (dataType == DATA_TYPE_STYLES)
			record = new StyleRecord(lineReader);
		else if (dataType == DATA_TYPE_TAGS)
			record = new TagRecord(lineReader);
		else if (dataType == DATA_TYPE_PLAYLISTS)
			record = PlaylistRecord.readPlaylistRecord(lineReader);
		return record;
	}

    ////////////
    // FIELDS //
    ////////////

	private byte dataType;

	protected UniqueIdTable uniqueIdTable = new UniqueIdTable(); // maps unique Ids to identifiers and vice versa

	private Map<Integer, Record> recordMap = new HashMap<Integer, Record>(); // keeps track of all records, accessed by unique id
    private Map<Integer, Record> duplicateIdMap = new HashMap<Integer, Record>(); // keeps track of duplicate ids, is separate so iterators don't pick up duplicatations...

    private Vector<UserDataType> userDataTypes = new Vector<UserDataType>(); // will track each unique user data type specific to this index
	private short nextUserDataTypeId = Short.MIN_VALUE;

    // transients
    transient private Vector<Integer> allIds; // used by iterators and for caching purposes
    transient private Semaphore getIdsSem;
    transient private RWSemaphore duplicateIdMapSem;

    transient private IndexWriter indexWriter;
    transient private IndexSearcher indexSearcher;
    transient private IndexReader indexReader;
    transient private Semaphore indexWriterSem;
    transient private RWSemaphore indexSearcherSem;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(LocalIMDB.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("allIds") || pd.getName().equals("duplicateIdMapSem") || pd.getName().equals("getIdsSem")) {
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

    @Override
	public int getSize() { return recordMap.size(); }

	/**
	 * Returns all of the unique ids for each record in the index.  A copy must be returned so iterating over while
	 * records are being added/removed does not create concurrent modification exceptions.  The result is cached however
	 * and recomputed only when records are added/removed.
	 */
	@Override
	public Vector<Integer> getIds() {
		Vector<Integer> result = null;
		try {
			getIdsSem().tryAcquire("getIds", GET_IDS_SEM_TIMEOUT);
			if (allIds == null) {
	    		allIds = new Vector<Integer>(recordMap.size());
	    		Iterator<Integer> iter = recordMap.keySet().iterator();
	    		while (iter.hasNext())
	    			allIds.add(iter.next());
			}
			result = allIds;
		} catch (java.util.ConcurrentModificationException cme) {
			log.warn("getIds(): concurrent modification exception");
		} catch (Exception e) {
			log.error("getIds(): error", e);
		} finally {
			getIdsSem().release();
		}
		if (result == null)
			return getIds();
    	return result;
	}
	@Override
	public Iterator<Integer> getIdsIterator() { return getIds().iterator(); }

	@Override
	public int getUniqueIdFromIdentifier(Identifier id) { return uniqueIdTable.getUniqueIdFromIdentifier(id); }
	@Override
	public Identifier getIdentifierFromUniqueId(int uniqueId) { return uniqueIdTable.getIdentifierFromUniqueId(uniqueId); }

	/**
	 * Getting records is O(1), and only when necessary should the record's profile be retrieved.
	 */
	@Override
	public Record get(Integer uniqueId) {
		Record result = recordMap.get(uniqueId);
		if (result == null)
			result = duplicateIdMap.get(uniqueId);
		return result;
	}

	@Override
	public int getNextAvailableUniqueId() { return uniqueIdTable.getNextAvailableUniqueId(); }

    public short getNextUserDataTypeId() { return nextUserDataTypeId; }
	@Override
	public Vector<UserDataType> getUserDataTypes() { return userDataTypes; }
	@Override
	public short getNextUserDataTypeIdAndIncrement() { return nextUserDataTypeId++; }

	private Semaphore getIdsSem() {
		if (getIdsSem == null)
			getIdsSem = new Semaphore(1);
		return getIdsSem;
	}

	private RWSemaphore getDuplicateIdMapSem() {
		if (duplicateIdMapSem == null)
			duplicateIdMapSem = new RWSemaphore(DUPLICATE_MAP_SEM_TIMEOUT);
		return duplicateIdMapSem;
	}

	public byte getDataType() { return dataType; }

	public Semaphore getIndexWriterSem() {
		if (indexWriterSem == null)
			indexWriterSem = new Semaphore(1);
		return indexWriterSem;
	}

	public RWSemaphore getIndexSearcherSem() {
		if (indexSearcherSem == null)
			indexSearcherSem = new RWSemaphore(10000);
		return indexSearcherSem;
	}

	public IndexWriter getIndexWriter() {
		return getIndexWriter(true);
	}

	public IndexWriter getIndexWriter(boolean createIfNecessary) {
		try {
			if (log.isTraceEnabled())
				log.trace("getIndexWriter(): called");
			getIndexWriterSem().acquire();
			if ((indexWriter == null) && createIfNecessary) {
				String indexFilename = OSHelper.getWorkingDirectory() + "/index/" + DataConstantsHelper.getDataTypeDescription(getDataType()).toLowerCase();
				File indexFile = new File(indexFilename);
				boolean populate = false;
				if (!indexFile.exists())
					populate = true;
				indexWriter = new IndexWriter(FSDirectory.open(indexFile), new StandardAnalyzer(LUCENE_VERSION), !indexFile.exists(), IndexWriter.MaxFieldLength.UNLIMITED);
				if (log.isTraceEnabled())
					log.trace("getIndexWriter(): # docs=" + indexWriter.maxDoc());
				if (populate) {
					if (log.isDebugEnabled())
						log.debug("getIndexWriter(): building up fresh index...");
					Vector<Integer> ids = allIds;
					if (ids != null) {
						for (int id : ids) {
							Record record = get(id);
							if (record != null) {
								Document doc = record.getDocument();
								if (doc != null)
									indexWriter.updateDocument(new Term("id", String.valueOf(record.getUniqueId())), doc);
							}
						}
					}
				}
			}
			return indexWriter;
		} catch (Exception e) {
			log.error("getIndexWriter(): error", e);
		} finally {
			getIndexWriterSem().release();
		}
		return null;
	}

	static public Analyzer getAnalyzer() {
		if (analyzer == null)
			analyzer = new StandardAnalyzer(LUCENE_VERSION);
		return analyzer;
	}

	public IndexSearcher getIndexSearcher() {
		try {
			if (indexSearcher == null)
				indexSearcher = new IndexSearcher(getIndexReader());
			return indexSearcher;
		} catch (Exception e) {
			log.error("getIndexSearcher(): error", e);
		}
		return null;
	}

	public void closeIndexSearcher() {
		try {
			getIndexSearcherSem().startRead("closeIndexSearcher()");
			if (indexSearcher != null) {
				indexSearcher.close();
				indexSearcher = null;
			}
			if (indexReader != null) {
				indexReader.close();
				indexReader = null;
			}
		} catch (Exception e) {
			log.error("closeIndexSearcher(): error", e);
		} finally {
			getIndexSearcherSem().endRead();
		}
	}

	public IndexReader getIndexReader() {
		try {
			if (indexReader == null) {
				indexReader = getIndexWriter().getReader();
			} else {
				if (!indexReader.isCurrent())
					indexReader.reopen();
			}
			return indexReader;
		} catch (Exception e) {
			log.error("getIndexReader(): error", e);
		}
		return null;
	}

	// for serialization
	public UniqueIdTable getUniqueIdTable() { return uniqueIdTable; }
	public Map<Integer, Record> getRecordMap() { return recordMap; }
	public Map<Integer, Record> getDuplicateIdMap() { return duplicateIdMap; }

	/////////////
	// SETTERS //
	/////////////

	@Override
	public void addUserDataType(UserDataType userDataType) { userDataTypes.add(userDataType); }

	public void setNextUserDataTypeId(short nextUserDataTypeId) { this.nextUserDataTypeId = nextUserDataTypeId; }
	public void setUserDataTypes(Vector<UserDataType> userDataTypes) { this.userDataTypes = userDataTypes; }

	@Override
	public void setDataType(byte dataType) { this.dataType = dataType; }

	// for serialization
	public void setDuplicateIdMap(Map<Integer, Record> duplicateIdMap) { this.duplicateIdMap = duplicateIdMap; }
	public void setUniqueIdTable(UniqueIdTable uniqueIdTable) { this.uniqueIdTable = uniqueIdTable; }
	public void setRecordMap(Map<Integer, Record> recordMap) { this.recordMap = recordMap; }

	/////////////
	// METHODS //
	/////////////

	@Override
	public boolean doesExist(Integer uniqueId) { return recordMap.containsKey(uniqueId) || duplicateIdMap.containsKey(uniqueId); }

	@Override
	public void setUniqueIdForIdentifier(int uniqueId, Identifier identifier) { uniqueIdTable.setUniqueIdForIdentifier(uniqueId, identifier); }
	@Override
	public void updateIdentifier(Identifier newId, Identifier oldId) { uniqueIdTable.updateIdentifier(newId, oldId); }

	@Override
	public void put(Record result) {
		// profile is created, update indexes/file system...
		try {
			getIdsSem().tryAcquire("put", GET_IDS_SEM_TIMEOUT);
			recordMap.put(result.getUniqueId(), result);
			allIds = null;
			Document doc = result.getDocument();
			if (doc != null) {
				getIndexWriter().updateDocument(new Term("id", String.valueOf(result.getUniqueId())), doc);
				closeIndexSearcher();
			}
		} catch (Exception e) { } finally {
			getIdsSem().release();
		}
	}
	@Override
	public void update(Record record) {
		try {
			if (log.isTraceEnabled())
				log.trace("update(): called");
			Document doc = record.getDocument();
			if (doc != null) {
				if (log.isDebugEnabled())
					log.debug("update(): updating record=" + record);
				getIndexWriter().updateDocument(new Term("id", String.valueOf(record.getUniqueId())), doc);
				closeIndexSearcher();
			}
			if (log.isTraceEnabled())
				log.trace("update(): done");
		} catch (Exception e) {
			log.error("update(): error", e);
		}
	}
	@Override
	public void remove(int uniqueId) {
		try {
			getIdsSem().tryAcquire("remove", GET_IDS_SEM_TIMEOUT);
			recordMap.remove(uniqueId);
			allIds = null;
			getIndexWriter().deleteDocuments(new TermQuery(new Term("id", String.valueOf(uniqueId))));
			closeIndexSearcher();
		} catch (Exception e) { } finally {
			getIdsSem().release();
		}
	}

	@Override
	public void addDuplicateMapping(int duplicateId, Record record) {
		try {
			getDuplicateIdMapSem().startRead("addDuplicateMapping");
			duplicateIdMap.put(duplicateId, record);
		} catch (Exception e) { } finally {
			getDuplicateIdMapSem().endRead();
		}


	}
	@Override
	public void removeDuplicateId(int uniqueId) {
		try {
			getDuplicateIdMapSem().startRead("removeDuplicateId");
			duplicateIdMap.remove(uniqueId);
		} catch (Exception e) { } finally {
			getDuplicateIdMapSem().endRead();
		}
	}

	@Override
	public void close() {
		try {
			IndexWriter iw = getIndexWriter(false);
			if (iw != null) {
				iw.optimize();
				iw.close();
			}
		} catch (Exception e) {
			log.error("close(): error", e);
		}
	}

	@Override
	public void commit() {
		try {
			getIndexWriter().commit();
		} catch (Exception e) {
			log.error("commit(): error", e);
		}
	}

	private class LuceneQuery {
		private BooleanQuery booleanQuery;
		private boolean hasCriteria;
		public LuceneQuery(BooleanQuery booleanQuery, boolean hasCriteria) {
			this.booleanQuery = booleanQuery;
			this.hasCriteria = hasCriteria;
		}
		public BooleanQuery getBooleanQuery() { return booleanQuery; }
		public void setBooleanQuery(BooleanQuery booleanQuery) { this.booleanQuery = booleanQuery; }
		public boolean hasCriteria() { return hasCriteria; }
		public void setHasCriteria(boolean hasCriteria) { this.hasCriteria = hasCriteria; }
	}

	private String searchModifier(String input) {
		if ((input == null) || (input.length() == 0))
			return input;
		StringBuffer result = new StringBuffer(input);
		boolean isStrict = ((input.startsWith("\"")) || (input.endsWith("\"")));
		boolean isWild = input.endsWith("*");
		if (RE3Properties.getBoolean("strict_match_searching")) {
			if (!isStrict) {
				result.insert(0, "\"");
				result.append("\"");
			}
			return result.toString(); // allowing both on at the same time produced strange results
		}
		if (RE3Properties.getBoolean("add_wildcard_to_searches")) {
			if (!isWild)
				result.append("*");
		}
		return result.toString();
	}

	private LuceneQuery getLuceneQuery(SearchParameters searchParameters) {
		if (log.isTraceEnabled())
			log.trace("getBooleanQuery(): searchParameters=" + searchParameters);
		BooleanQuery booleanQuery = new BooleanQuery();
		boolean hasCriteria = false;
		if (searchParameters instanceof CommonSearchParameters) {
			CommonSearchParameters searchParams = (CommonSearchParameters)searchParameters;
			String searchText = searchParams.getSearchText();
			if ((searchText != null) && (searchText.length() > 0)) {
				booleanQuery.add(new SearchParser(searchText).getQuery(searchParameters.getSearchFields()), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
			if (!searchParams.isShowDisabled()) {
				try {
					QueryParser parser = new MultiFieldQueryParser(LUCENE_VERSION, new String[] { "disabled" }, getAnalyzer());
					Query query = parser.parse("0");
					booleanQuery.add(query, BooleanClause.Occur.MUST);
				} catch (ParseException pe) { }
			}
		}
		if (searchParameters instanceof SearchSearchParameters) {
			SearchSearchParameters searchParams = (SearchSearchParameters)searchParameters;
			if (searchParams.isInternalItemsOnly()) {
				try {
					QueryParser parser = new MultiFieldQueryParser(LUCENE_VERSION, new String[] { "is_external" }, getAnalyzer());
					Query query = parser.parse("0");
					booleanQuery.add(query, BooleanClause.Occur.MUST);
				} catch (ParseException pe) { }
			}
			if (searchParams.isExternalItemsOnly()) {
				try {
					QueryParser parser = new MultiFieldQueryParser(LUCENE_VERSION, new String[] { "is_external" }, getAnalyzer());
					Query query = parser.parse("1");
					booleanQuery.add(query, BooleanClause.Occur.MUST);
				} catch (ParseException pe) { }
			}
			String styleText = searchParams.getStyleSearchText();
			if ((styleText != null) && (styleText.length() > 0)) {
				booleanQuery.add(new SearchParser(styleText).getQuery(new String[] { "style" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
			String tagText = searchParams.getTagSearchText();
			if ((tagText != null) && (tagText.length() > 0)) {
				booleanQuery.add(new SearchParser(tagText).getQuery(new String[] { "tag" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
		}
		if (searchParameters instanceof SongSearchParameters) {
			SongSearchParameters songParams = (SongSearchParameters)searchParameters;
			String artistText = songParams.getArtistSearchText();
			if ((artistText != null) && (artistText.length() > 0)) {
				booleanQuery.add(new SearchParser(artistText).getQuery(new String[] { "artist", "featuring" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
			String releaseText = songParams.getReleaseSearchText();
			if ((releaseText != null) && (releaseText.length() > 0)) {
				booleanQuery.add(new SearchParser(releaseText).getQuery(new String[] { "release", "release_instances" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
			String titleText = songParams.getTitleSearchText();
			if ((titleText != null) && (titleText.length() > 0)) {
				booleanQuery.add(new SearchParser(titleText).getQuery(new String[] { "title", "remix" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
			String labelText = songParams.getLabelSearchText();
			if ((labelText != null) && (labelText.length() > 0)) {
				booleanQuery.add(new SearchParser(labelText).getQuery(new String[] { "label" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
		} else if (searchParameters instanceof ReleaseSearchParameters) {
			ReleaseSearchParameters releaseParams = (ReleaseSearchParameters)searchParameters;
			String artistText = releaseParams.getArtistSearchText();
			if ((artistText != null) && (artistText.length() > 0)) {
				booleanQuery.add(new SearchParser(artistText).getQuery(new String[] { "artist" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
			String titleText = releaseParams.getTitleSearchText();
			if ((titleText != null) && (titleText.length() > 0)) {
				booleanQuery.add(new SearchParser(titleText).getQuery(new String[] { "title" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
			String labelText = releaseParams.getLabelSearchText();
			if ((labelText != null) && (labelText.length() > 0)) {
				booleanQuery.add(new SearchParser(labelText).getQuery(new String[] { "label" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
		} else if (searchParameters instanceof LabelSearchParameters) {
			LabelSearchParameters labelParams = (LabelSearchParameters)searchParameters;
			String artistText = labelParams.getArtistSearchText();
			if ((artistText != null) && (artistText.length() > 0)) {
				booleanQuery.add(new SearchParser(artistText).getQuery(new String[] { "artists" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
			String labelText = labelParams.getLabelSearchText();
			if ((labelText != null) && (labelText.length() > 0)) {
				booleanQuery.add(new SearchParser(labelText).getQuery(new String[] { "name" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
		} else if (searchParameters instanceof ArtistSearchParameters) {
			ArtistSearchParameters artistParams = (ArtistSearchParameters)searchParameters;
			String artistText = artistParams.getArtistSearchText();
			if ((artistText != null) && (artistText.length() > 0)) {
				booleanQuery.add(new SearchParser(artistText).getQuery(new String[] { "name" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
			String labelText = artistParams.getLabelSearchText();
			if ((labelText != null) && (labelText.length() > 0)) {
				booleanQuery.add(new SearchParser(labelText).getQuery(new String[] { "labels" }), BooleanClause.Occur.MUST);
				hasCriteria = true;
			}
		}
		return new LuceneQuery(booleanQuery, hasCriteria);
	}

	private Map<Integer, SearchResult> getPartialRE3Results(SearchParameters searchParameters) {
		if (searchParameters.isEmpty(false))
			return null;
		Map<Integer, SearchResult> result = new HashMap<Integer, SearchResult>();
		Vector<Integer> ids = getIds();
		float maxScore = 0.0f;
		for (int j = 0; j < ids.size(); ++j) {
			if (RapidEvolution3.isTerminated)
				return result;
			Integer id = ids.get(j);
			Record record = get(id);
			if (record != null) {
				float score = searchParameters.matches(record, false);
				if (score > 0.0f) {
					result.put(record.getUniqueId(), new SearchResult(record, score));
					if (score > maxScore)
						maxScore = score;
					if (record instanceof FilterRecord) {
						for (int parentId : ((FilterRecord)record).getParentIds()) {
							if (!result.containsKey(parentId)) {
								Record parent = get(parentId);
								if (parent != null)
									result.put(parentId, new SearchResult(parent, score));
							}
						}
					}
				}
			}
		}
		// normalize the scores
		if (maxScore > 0.0f) {
			for (SearchResult searchResult : result.values())
				searchResult.setScore(searchResult.getScore() / maxScore);
		}
		return result;
	}
	/**
	 * Optimized version for just getting the # count
	 */
	private Map<Integer, Object> getPartialRE3ResultsForCount(SearchParameters searchParameters) {
		if (searchParameters.isEmpty(false))
			return null;
		Map<Integer, Object> result = new HashMap<Integer, Object>();
		Vector<Integer> ids = getIds();
		for (int j = 0; j < ids.size(); ++j) {
			if (RapidEvolution3.isTerminated)
				return result;
			Integer id = ids.get(j);
			Record record = get(id);
			if (record != null) {
				float score = searchParameters.matches(record, false);
				if (score > 0.0f) {
					result.put(record.getUniqueId(), null);
					if (record instanceof FilterRecord) {
						for (int parentId : ((FilterRecord)record).getParentIds())
							result.put(parentId, null);
					}
				}
			}
		}
		return result;
	}

	private Map<Integer, SearchResult> getPartialLuceneResults(SearchParameters searchParameters) {
		LuceneQuery luceneQuery = getLuceneQuery(searchParameters);
		if (luceneQuery.hasCriteria()) {
			BooleanQuery booleanQuery = luceneQuery.getBooleanQuery();
			Map<Integer, SearchResult> result = new HashMap<Integer, SearchResult>();
			try {
				getIndexSearcherSem().startWrite("getPartialLuceneResults()");
				TopDocs topDocs = getIndexSearcher().search(booleanQuery, Integer.MAX_VALUE);
				if (topDocs != null) {
					if (log.isDebugEnabled())
						log.debug("searchRecords(): # results=" + topDocs.totalHits);
					// get the max to normalize the scores
					float maxScore = 0.0f;
					for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
						if (scoreDoc.score > maxScore)
							maxScore = scoreDoc.score;
					}
					for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
						Document doc = getIndexReader().document(scoreDoc.doc);
						if (doc != null) {
							Integer uniqueId = Integer.parseInt(doc.get("id"));
							Record record = get(uniqueId);
							if (record != null) {
								if (log.isTraceEnabled())
									log.trace("searchRecords(): \trecord=" + record + ", score=" + scoreDoc.score);
								float score = (maxScore > 0.0f) ? scoreDoc.score / maxScore : 0.0f;
								if (searchParameters instanceof SearchSearchParameters) {
									float randomness = ((SearchSearchParameters)searchParameters).getRandomness();
									if (randomness > 0.0f)
										score = (float)((1.0f - randomness) * score + randomness * Math.random());
								}
								result.put(record.getUniqueId(), new SearchResult(record, score));
								if (record instanceof FilterRecord) {
									for (int parentId : ((FilterRecord)record).getParentIds()) {
										if (!result.containsKey(parentId)) {
											Record parent = get(parentId);
											if (parent != null)
												result.put(parentId, new SearchResult(parent, score));
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("searchRecords(): error", e);
			} finally {
				getIndexSearcherSem().endWrite();
			}
			return result;
		} else {
			return null;
		}
	}

	/**
	 * Optimized version for counting
	 */
	private Map<Integer, Object> getPartialLuceneResultsForCount(SearchParameters searchParameters) {
		LuceneQuery luceneQuery = getLuceneQuery(searchParameters);
		if (luceneQuery.hasCriteria()) {
			BooleanQuery booleanQuery = luceneQuery.getBooleanQuery();
			Map<Integer, Object> result = new HashMap<Integer, Object>();
			try {
				getIndexSearcherSem().startWrite("getPartialLuceneResultsForCount()");
				TopDocs topDocs = getIndexSearcher().search(booleanQuery, Integer.MAX_VALUE);
				if (topDocs != null) {
					if (log.isDebugEnabled())
						log.debug("searchRecords(): # results=" + topDocs.totalHits);
					for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
						Document doc = getIndexReader().document(scoreDoc.doc);
						if (doc != null) {
							Integer uniqueId = Integer.parseInt(doc.get("id"));
							result.put(uniqueId, null);
							Record record = get(uniqueId);
							if (record != null) {
								if (record instanceof FilterRecord) {
									for (int parentId : ((FilterRecord)record).getParentIds())
										result.put(parentId, null);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("searchRecords(): error", e);
			} finally {
				getIndexSearcherSem().endWrite();
			}
			return result;
		} else {
			return null;
		}
	}

	private Vector<SearchResult> getMergedResults(SearchParameters searchParameters) {
		Vector<SearchResult> result = new Vector<SearchResult>();
		try {
			Map<Integer, SearchResult> partial1 = getPartialLuceneResults(searchParameters);
			Map<Integer, SearchResult> partial2 = getPartialRE3Results(searchParameters);
			if ((partial1 != null) && (partial2 != null)) {
				if (log.isDebugEnabled())
					log.debug("getMergedResults(): merging partial results, partial1 size=" + partial1.size() + ", partial2 size=" + partial2.size());
				if (partial1.size() < partial2.size()) {
					for (SearchResult searchResult : partial1.values()) {
						SearchResult result2 = partial2.get(searchResult.getRecord().getUniqueId());
						if (result2 != null) {
							searchResult.setScore((searchResult.getScore() + result2.getScore()) / 2.0f);
							result.add(searchResult);
						}
					}
				} else {
					for (SearchResult searchResult : partial2.values()) {
						SearchResult result2 = partial1.get(searchResult.getRecord().getUniqueId());
						if (result2 != null) {
							searchResult.setScore((searchResult.getScore() + result2.getScore()) / 2.0f);
							result.add(searchResult);
						}
					}
				}
			} else if (partial1 != null) {
				if (log.isDebugEnabled())
					log.debug("getMergedResults(): using partial1");
				for (SearchResult searchResult : partial1.values())
					result.add(searchResult);
			} else if (partial2 != null) {
				if (log.isDebugEnabled())
					log.debug("getMergedResults(): using partial2");
				for (SearchResult searchResult : partial2.values())
					result.add(searchResult);
			} else {
				for (int id : getIds())
					result.add(new SearchResult(get(id), 1.0f));
			}
		} catch (Exception e) {
			log.error("getMergedResults(): error", e);
		}
		return result;
	}

	private int getMergedResultsCount(SearchParameters searchParameters) {
		int count = 0;
		try {
			Map<Integer, Object> partial1 = getPartialLuceneResultsForCount(searchParameters);
			Map<Integer, Object> partial2 = getPartialRE3ResultsForCount(searchParameters);
			if ((partial1 != null) && (partial2 != null)) {
				if (log.isDebugEnabled())
					log.debug("getMergedResults(): merging partial results, partial1 size=" + partial1.size() + ", partial2 size=" + partial2.size());
				if (partial1.size() < partial2.size()) {
					for (int pId : partial1.keySet())
						if (partial2.containsKey(pId))
							++count;
				} else {
					for (int pId : partial2.keySet()) {
						if (partial1.containsKey(pId))
							++count;
					}
				}
			} else if (partial1 != null) {
				count = partial1.size();
			} else if (partial2 != null) {
				count = partial2.size();
			} else {
				count = getSize();
			}
		} catch (Exception e) {
			log.error("getMergedResults(): error", e);
		}
		return count;
	}

	@Override
	public int searchCount(SearchParameters searchParameters) {
		if (log.isTraceEnabled())
			log.trace("searchCount(): searchParameters=" + searchParameters.getUniqueHash());
		if (searchParameters.isEmpty())
			return getSize();
		int result = getMergedResultsCount(searchParameters);
		if (log.isTraceEnabled())
			log.trace("searchCount(): result=" + result);
		return result;
	}
	@Override
	public Vector<SearchResult> searchRecords(SearchParameters searchParameters, int maxResults) {
		if (log.isDebugEnabled())
			log.debug("searchRecords(): searching=" + searchParameters.getUniqueHash());
		Vector<SearchResult> result = null;
		if (searchParameters.isEmpty()) {
			result = new Vector<SearchResult>(getSize());
			for (int id : getIds()) {
				Record record = get(id);
				if (record != null)
					result.add(new SearchResult(record, 1.0f));
			}
		} else {
			result = getMergedResults(searchParameters);
		}
		searchParameters.clearLastSearchResultScores();
		searchParameters.initLastSearchResultScore(result.size());
		for (SearchResult searchResult : result)
			searchParameters.addLastSearchResultScore(searchResult.getRecord().getUniqueId(), searchResult.getScore());
		if (maxResults > 0) {
			// limited search
			ArrayList<SearchResult> sortedResults = new ArrayList<SearchResult>(maxResults + 1);
			for (SearchResult searchResult : result) {
				int index = Collections.binarySearch(sortedResults, searchResult, ((CommonSearchParameters)searchParameters));
				if (index < 0) {
					int insertionPoint = -(index + 1);
					if (insertionPoint < maxResults)
						sortedResults.add(insertionPoint, searchResult);
				} else {
					SearchResult existingResult = sortedResults.get(index);
					if (!existingResult.getRecord().equals(searchResult.getRecord()))
						sortedResults.add(index, searchResult);
					else {
						if (log.isDebugEnabled())
							log.debug("searchRecords(): entry already found=" + searchResult);
					}
				}
				while (sortedResults.size() > maxResults)
					sortedResults.remove(sortedResults.size() - 1);
			}
			result = new Vector<SearchResult>(sortedResults.size());
			for (SearchResult sortedResult : sortedResults)
				result.add(sortedResult);
		} else {
			Collections.sort(result, ((CommonSearchParameters)searchParameters));
		}
		return result;
	}

	@Override
	public void write(LineWriter writer) {
		writer.writeLine(3); // version
		writer.writeLine(dataType);
		uniqueIdTable.write(writer);
		writer.writeLine(recordMap.size());
		for (Entry<Integer, Record> entry : recordMap.entrySet())
			entry.getValue().write(writer);
		writer.writeLine(duplicateIdMap.size());
		for (Entry<Integer, Record> entry : duplicateIdMap.entrySet()) {
			writer.writeLine(entry.getKey());
			entry.getValue().write(writer);
		}
		writer.writeLine((userDataTypes != null) ? userDataTypes.size() : 0);
		if (userDataTypes != null) {
			for (UserDataType userType : userDataTypes)
				userType.write(writer);
		}
		writer.writeLine(nextUserDataTypeId);
		writer.writeLine("## end of local imdb ##");
	}

	@Override
	public void computeSearchScores(SearchParameters searchParameters) {
		Vector<SearchResult> result = getMergedResults(searchParameters);
		searchParameters.clearLastSearchResultScores();
		searchParameters.initLastSearchResultScore(result.size());
		for (SearchResult searchResult : result)
			searchParameters.addLastSearchResultScore(searchResult.getRecord().getUniqueId(), searchResult.getScore());
	}

}
