package test;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.discogs.DiscogsAPIWrapper;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;

public class RE3TestCase extends TestCase implements DataConstants {

	static private Logger log = Logger.getLogger(RE3TestCase.class);

	static {
		RapidEvolution3.loadLog4J();		
	}
	
	private Database database;
	
    //////////////////////
    // JUNIT LIFE-CYCLE //
    //////////////////////
    
    protected void setUp() throws Exception {        
        super.setUp();
        RE3Properties.loadUserProperties();
        RE3Properties.setProperty("application_data_directory_name", OSHelper.getWorkingDirectory("Rapid Evolution 3 Unit Test"));
        RE3Properties.setProperty("junit_test_mode", "true");
        RE3Properties.setProperty("enable_delayed_tag_updates", "false");
        RE3Properties.setProperty("imdb_impl_class", "com.mixshare.rapid_evolution.data.index.imdb.LocalIMDB");
        RE3Properties.setProperty("attempt_to_automatically_merge_duplicates", "true");
        RE3Properties.setProperty("profile_manager_io_implementation", "com.mixshare.rapid_evolution.data.profile.io.LocalProfileIO");
        RE3Properties.setProperty("file_system_implementation", "com.mixshare.rapid_evolution.data.util.filesystem.LocalFileSystem");
        DiscogsAPIWrapper.UNIT_TEST_MODE = true;
        FileUtil.deleteDirectory(OSHelper.getWorkingDirectory() + "/index/" + DataConstantsHelper.getDataTypeDescription(DATA_TYPE_ARTISTS).toLowerCase());
        FileUtil.deleteDirectory(OSHelper.getWorkingDirectory() + "/index/" + DataConstantsHelper.getDataTypeDescription(DATA_TYPE_LABELS).toLowerCase());
        FileUtil.deleteDirectory(OSHelper.getWorkingDirectory() + "/index/" + DataConstantsHelper.getDataTypeDescription(DATA_TYPE_RELEASES).toLowerCase());
        FileUtil.deleteDirectory(OSHelper.getWorkingDirectory() + "/index/" + DataConstantsHelper.getDataTypeDescription(DATA_TYPE_SONGS).toLowerCase());
        FileUtil.deleteDirectory(OSHelper.getWorkingDirectory() + "/index/" + DataConstantsHelper.getDataTypeDescription(DATA_TYPE_STYLES).toLowerCase());
        FileUtil.deleteDirectory(OSHelper.getWorkingDirectory() + "/index/" + DataConstantsHelper.getDataTypeDescription(DATA_TYPE_PLAYLISTS).toLowerCase());
        FileUtil.deleteDirectory(OSHelper.getWorkingDirectory() + "/index/" + DataConstantsHelper.getDataTypeDescription(DATA_TYPE_TAGS).toLowerCase());
        FileUtil.deleteDirectory(OSHelper.getWorkingDirectory() + "/index/" + DataConstantsHelper.getDataTypeDescription(DATA_TYPE_MIXOUTS).toLowerCase());
		database = new Database();
		database.instanceInit();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    	Database.close();        
    }
    
    ////////////////////
    // HELPER METHODS //
    ////////////////////
    
	static public Vector<DegreeValue> getVector(DegreeValue[] params) {
		Vector<DegreeValue> result = new Vector<DegreeValue>(params.length);
		for (int p = 0; p < params.length; ++p) {
			result.add(params[p]);
		}
		return result;
	}
	
    static public boolean areObjectsEqual(Object obj1, Object obj2) {
    	if ((obj1 == null) && (obj2 == null))
    		return true;
    	if (obj1 == null)
    		return false;
    	if (obj2 == null)
    		return false;
		return (obj1.equals(obj2));
    }

    /**
     * Order does not matter
     */
    static public boolean areCollectionsEquivalent(Collection col1, String[] col2) {
    	Vector col2v = new Vector(col2.length);
    	for (int i = 0; i < col2.length; ++i)
    		col2v.add(col2[i]);
    	return areCollectionsEquivalent(col1, col2v);    	
    }
    static public boolean areCollectionsEquivalent(Collection col1, Collection col2) {
    	if ((col1 == null) && (col2 == null))
    		return true;
    	if (col1 == null)
    		return false;
    	if (col2 == null)
    		return false;
    	if (col1.size() != col2.size())
    		return false;
    	Iterator iter1 = col1.iterator();
    	while (iter1.hasNext()) {
    		if (!col2.contains(iter1.next()))
    			return false;
    	}
    	return true;
    }
    	
    /**
     * Order matters
     */
    static public boolean areCollectionsEqual(String[] col1, String[] col2) {
    	if ((col1 == null) && (col2 == null))
    		return true;
    	if (col1 == null)
    		return false;
    	if (col2 == null)
    		return false;
    	if (col1.length != col2.length)
    		return false;
    	int i1 = 0;
    	int i2 = 0;
    	while ((i1 < col1.length) && (i2 < col2.length)) {
			String value1 = col1[i1];
			String value2 = col2[i2];
			if (!value1.equals(value2))
				return false;
			++i1;
			++i2;
    	}    	    	
    	return true;
    }
    static public boolean areCollectionsEqual(Collection col1, String[] col2) {
    	Vector col2v = new Vector(col2.length);
    	for (int i = 0; i < col2.length; ++i)
    		col2v.add(col2[i]);
    	return areCollectionsEqual(col1, col2v);
    }
    static public boolean areCollectionsEqual(Collection col1, Collection col2) {
    	return areCollectionsEqual(col1, col2, false);
    }
    static public boolean areCollectionsEqual(Collection col1, Collection col2, boolean debug) {
    	if ((col1 == null) && (col2 == null))
    		return true;
    	if (col1 == null)
    		return false;
    	if (col2 == null)
    		return false;
    	if (col1.size() != col2.size())
    		return false;
    	Iterator iter1 = col1.iterator();
    	Iterator iter2 = col2.iterator();
    	while (iter1.hasNext() && iter2.hasNext()) {
			Object value1 = iter1.next();
			Object value2 = iter2.next();
			if ((value1 instanceof Map) && (value2 instanceof Map)) {
				if (!areMapsEqual((Map)value1, (Map)value2, debug)) {
	    			if (debug)
	    				log.debug("areCollectionsEqual(): maps not equal, value1=" + value1 + ", value2=" + value2);					
					return false;
				}
			} else if ((value1 instanceof Collection) && (value2 instanceof Collection)) {
				if (!areCollectionsEqual((Collection)value1, (Collection)value2, debug)) {
	    			if (debug)
	    				log.debug("areCollectionsEqual(): collections not equal, value1=" + value1 + ", value2=" + value2);					
					return false;
				}
			} else {
				if (!areObjectsEqual(value1, value2)) {
	    			if (debug)
	    				log.debug("areCollectionsEqual(): objects not equal, value1=" + value1 + ", value2=" + value2);    										
					return false;
				}
			}
    	}    	    	
    	if (iter1.hasNext())
    		return false;
    	if (iter2.hasNext())
    		return false;
    	return true;
    }
    
    static public boolean areMapsEqual(Map map1, Map map2) {
    	return areMapsEqual(map1, map2, false);
    }
    static public boolean areMapsEqual(Map map1, Map map2, boolean debug) {
    	if ((map1 == null) && (map2 == null))
    		return true;
    	if (map1 == null)
    		return false;
    	if (map2 == null)
    		return false;
    	if (map1.size() != map2.size()) {
    		if (debug)
    			log.debug("areMapsEqual(): sizes not equal, size1=" + map1.size() + ", size2=" + map2.size());
    		return false;
    	}
    	Iterator iter1 = map1.entrySet().iterator();
    	Iterator iter2 = map2.entrySet().iterator();
    	while (iter1.hasNext() && iter2.hasNext()) {
    		Entry entry1 = (Entry)iter1.next();
    		Entry entry2 = (Entry)iter2.next();
    		Object key1 = entry1.getKey();
    		Object key2 = entry2.getKey();
    		if (!key1.equals(key2)) {
    			if (debug)
    				log.debug("areMapsEqual(): keys not equal, key1=" + key1 + ", key2=" + key2);
    			return false;
    		}
			Object value1 = entry1.getValue();
			Object value2 = entry2.getValue();
			if ((value1 instanceof Map) && (value2 instanceof Map)) {
				if (!areMapsEqual((Map)value1, (Map)value2, debug)) {
	    			if (debug)
	    				log.debug("areMapsEqual(): maps not equal, key=" + key1 + ", value1=" + value1 + ", value2=" + value2);    					
					return false;
				}
			} else if ((value1 instanceof Collection) && (value2 instanceof Collection)) {
				if (!areCollectionsEqual((Collection)value1, (Collection)value2, debug)) {
	    			if (debug)
	    				log.debug("areMapsEqual(): collections not equal, key=" + key1 + ", value1=" + value1 + ", value2=" + value2);    					
					return false;
				}
			} else {
				if (!areObjectsEqual(value1, value2)) {
	    			if (debug)
	    				log.debug("areMapsEqual(): values not equal, key=" + key1 + ", value1=" + value1 + ", value2=" + value2);    					
					return false;
				}
			}
    	}
    	if (iter1.hasNext())
    		return false;
    	if (iter2.hasNext())
    		return false;
    	return true;
    }    

    protected void copyTestFileToUserLibrary(String... relativePaths) throws IOException {
    	try {
	    	for (String relativePath : relativePaths) {
	    		FileUtil.copy(relativePath, OSHelper.getWorkingDirectory() + "/" + relativePath);
	    	}
    	} catch (IOException e) {
    		fail(e.toString());
    	}
    }
}
