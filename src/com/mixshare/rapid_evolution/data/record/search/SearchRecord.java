package com.mixshare.rapid_evolution.data.record.search;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.json.JSONObject;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.util.FilterSemLock;
import com.mixshare.rapid_evolution.data.record.user.UserData;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.timing.SemaphoreFactory;

/**
 * This contains the common fields between the searchable items (artists, labels, releases, songs, music videos, etc).
 *
 * For some data elements, such as styles, tags, artists, labels, the data model distinguishes between "source" values
 * and "actual" values, after hierarchy and duplicate information is taken into consideration.
 */
abstract public class SearchRecord extends AbstractSearchRecord {

    static private Logger log = Logger.getLogger(SearchRecord.class);

    static private boolean DELAY_TAG_UPDATES = RE3Properties.getBoolean("enable_delayed_tag_updates");

	static public final String DEFAULT_THUMBNAIL_IMAGE = RE3Properties.getProperty("default_thumbnail_image_filename");

	static private final short MAX_STYLES_IN_DESCRIPTION = RE3Properties.getShort("max_styles_in_description");
	static private final short MAX_TAGS_IN_DESCRIPTION = RE3Properties.getShort("max_tags_in_description");

    static public int maxStylesToCheck = RE3Properties.getInt("style_similarity_max_comparisons");
    static public float minStyleDegreeThreshold = RE3Properties.getFloat("style_similarity_minimum_degree_threshold");

    static public int maxTagsToCheck = RE3Properties.getInt("tag_similarity_max_comparisons");
    static public float minTagDegreeThreshold = RE3Properties.getFloat("tag_similarity_minimum_degree_threshold");

    static private FilterSemLock sourceStyleSem = new FilterSemLock();
    static private FilterSemLock actualStyleSem = new FilterSemLock();
    static private FilterSemLock sourceTagSem = new FilterSemLock();
    static private FilterSemLock actualTagSem = new FilterSemLock();

    static private SemaphoreFactory userDataSem = new SemaphoreFactory();
    static private SemaphoreFactory minedDataSem = new SemaphoreFactory();

	////////////
	// FIELDS //
	////////////

    private int[] sourceStyleIds;
    private float[] sourceStyleDegrees;
    private byte[] sourceStyleSources; // did the styles come from the user, discogs, lastfm, etc
    private int[] actualStyleIds; // applies current hierarchy and filters duplicates...
    private float[] actualStyleDegrees; // applies current hierarchy and filters duplicates...

    private int[] sourceTagIds;
    private float[] sourceTagDegrees;
    protected byte[] sourceTagSources; // did the tags come from the user, discogs, lastfm, etc
    private int[] actualTagIds; // applies current hierarchy and filters duplicates...
    private float[] actualTagDegrees; // applies current hierarchy and filters duplicates...

    private float score;
    private float popularity;

    private String comments;

    private String thumbnailImageFilename;

    private short[] userDataTypes;
    private Object[] userData;

    private byte[] minedProfileSources;
    private long[] minedProfileSourcesLastUpdated;

    private boolean isExternalItem;

    private long playCount;

    protected long dateAdded;

	static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SearchRecord.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("thumbnailImageLoaded")) {
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

    public SearchRecord() { super(); }
    public SearchRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	int numSourceStyles = Integer.parseInt(lineReader.getNextLine());
    	sourceStyleIds = new int[numSourceStyles];
    	sourceStyleDegrees = new float[numSourceStyles];
    	sourceStyleSources = new byte[numSourceStyles];
    	for (int i = 0; i < numSourceStyles; ++i)
    		sourceStyleIds[i] = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numSourceStyles; ++i)
    		sourceStyleDegrees[i] = Float.parseFloat(lineReader.getNextLine());
    	for (int i = 0; i < numSourceStyles; ++i)
    		sourceStyleSources[i] = Byte.parseByte(lineReader.getNextLine());
    	int numActualStyles = Integer.parseInt(lineReader.getNextLine());
    	actualStyleIds = new int[numActualStyles];
    	actualStyleDegrees = new float[numActualStyles];
    	for (int i = 0; i < numActualStyles; ++i)
    		actualStyleIds[i] = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numActualStyles; ++i)
    		actualStyleDegrees[i] = Float.parseFloat(lineReader.getNextLine());
    	int numSourceTags = Integer.parseInt(lineReader.getNextLine());
    	sourceTagIds = new int[numSourceTags];
    	sourceTagDegrees = new float[numSourceTags];
    	sourceTagSources = new byte[numSourceTags];
    	for (int i = 0; i < numSourceTags; ++i)
    		sourceTagIds[i] = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numSourceTags; ++i)
    		sourceTagDegrees[i] = Float.parseFloat(lineReader.getNextLine());
    	for (int i = 0; i < numSourceTags; ++i)
    		sourceTagSources[i] = Byte.parseByte(lineReader.getNextLine());
    	int numActualTags = Integer.parseInt(lineReader.getNextLine());
    	actualTagIds = new int[numActualTags];
    	actualTagDegrees = new float[numActualTags];
    	for (int i = 0; i < numActualTags; ++i)
    		actualTagIds[i] = Integer.parseInt(lineReader.getNextLine());
    	for (int i = 0; i < numActualTags; ++i)
    		actualTagDegrees[i] = Float.parseFloat(lineReader.getNextLine());
    	score = Float.parseFloat(lineReader.getNextLine());
    	popularity = Float.parseFloat(lineReader.getNextLine());
    	int numLines = Integer.parseInt(lineReader.getNextLine());
    	StringBuffer commentsBuffer = new StringBuffer();
    	for (int i = 0; i < numLines; ++i) {
    		if (commentsBuffer.length() > 0)
    			commentsBuffer.append("\n");
    		commentsBuffer.append(lineReader.getNextLine());
    	}
    	comments = commentsBuffer.toString();
    	thumbnailImageFilename = lineReader.getNextLine();
    	int numUserDataTypes = Integer.parseInt(lineReader.getNextLine());
    	userDataTypes = new short[numUserDataTypes];
    	userData = new Object[numUserDataTypes];
    	for (int i = 0; i < numUserDataTypes; ++i)
    		userDataTypes[i] = Short.parseShort(lineReader.getNextLine());
    	for (int i = 0; i < numUserDataTypes; ++i) {
    		int type = Integer.parseInt(lineReader.getNextLine());
    		if (type == 1)
    			userData[i] = Boolean.parseBoolean(lineReader.getNextLine());
    		else if (type == 3)
    			userData[i] = Integer.parseInt(lineReader.getNextLine());
    		else
    			userData[i] = lineReader.getNextLine();
    	}
    	int numMinedDataSources = Integer.parseInt(lineReader.getNextLine());
    	minedProfileSources = new byte[numMinedDataSources];
    	minedProfileSourcesLastUpdated = new long[numMinedDataSources];
    	for (int i = 0; i < numMinedDataSources; ++i)
    		minedProfileSources[i] = Byte.parseByte(lineReader.getNextLine());
    	for (int i = 0; i < numMinedDataSources; ++i)
    		minedProfileSourcesLastUpdated[i] = Long.parseLong(lineReader.getNextLine());
    	isExternalItem = Boolean.parseBoolean(lineReader.getNextLine());
    	String playsLine = lineReader.getNextLine();
    	playCount = Long.parseLong(playsLine);
    	if (version >= 2) {
    		dateAdded = Long.parseLong(lineReader.getNextLine());
    		if (dateAdded == 0)
    			dateAdded = System.currentTimeMillis();
    	}
    }

    /////////////
    // GETTERS //
    /////////////

    // source styles (exact input from user/sources, before hierarchy & duplicates are taken into account)
    public int getNumSourceStyles() {
    	return (sourceStyleIds != null) ? sourceStyleIds.length : 0;
    }
    public int[] getSourceStyleIds() { return sourceStyleIds; }
    public float[] getSourceStyleDegrees() { return sourceStyleDegrees; }
    public Vector<DegreeValue> getSourceStyleDegreeValues() {
    	Vector<DegreeValue> result = new Vector<DegreeValue>(getNumSourceStyles());
    	try {
    		sourceStyleSem.startRead(getDataType(), uniqueId);
    		for (int s = 0; s < getNumSourceStyles(); ++s) {
    			Identifier id = Database.getStyleIndex().getIdentifierFromUniqueId(sourceStyleIds[s]);
    			if (id != null) {
    				String styleName = id.toString();
    				result.add(new DegreeValue(styleName, sourceStyleDegrees[s], sourceStyleSources[s]));
    			} else {
    				log.warn("getSourceStyleDegreeValues(): missing identifier for source style id=" + sourceStyleIds[s]);
    			}
    		}
    	} catch (Exception e) {
    		log.error("getSourceStyleDegreeValues(): error", e);
    	} finally {
    		sourceStyleSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public float getMaxSourceStyleDegree(StyleRecord style) {
    	float result = 0.0f;
    	try {
    		sourceStyleSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumSourceStyles(); ++s) {
	    		if (sourceStyleIds[s] == style.getUniqueId()) {
	    			if (sourceStyleDegrees[s] > result)
	    				result = sourceStyleDegrees[s];
	    		} else if (style.getDuplicateIds() != null) {
	    			for (int dupId : style.getDuplicateIds()) {
	    				if (sourceStyleIds[s] == dupId) {
	    	    			if (sourceStyleDegrees[s] > result)
	    	    				result = sourceStyleDegrees[s];
	    				}
	    			}
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getMaxSourceStyleDegree(): error", e);
    	} finally {
    		sourceStyleSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }

    public float getSourceStyleDegreeFromUniqueId(int styleId) {
    	float result = 0.0f;
    	try {
    		sourceStyleSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumSourceStyles(); ++s) {
	    		if (sourceStyleIds[s] == styleId) {
	    			result = sourceStyleDegrees[s];
	    			break;
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getSourceStyleDegreeFromUniqueId(): error", e);
    	} finally {
    		sourceStyleSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public byte getMaxSourceStyleSource(StyleRecord style) {
    	float match = 0.0f;
    	byte source = DATA_SOURCE_UNKNOWN;
    	try {
    		sourceStyleSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumSourceStyles(); ++s) {
	    		if (sourceStyleIds[s] == style.getUniqueId()) {
	    			if (sourceStyleDegrees[s] > match) {
	    				match = sourceStyleDegrees[s];
	    				source = sourceStyleSources[s];
	    			}
	    		} else if (style.getDuplicateIds() != null) {
	    			for (int dupId : style.getDuplicateIds()) {
	    				if (sourceStyleIds[s] == dupId) {
	    	    			if (sourceStyleDegrees[s] > match) {
	    	    				match = sourceStyleDegrees[s];
	    	    				source = sourceStyleSources[s];
	    	    			}
	    				}
	    			}
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getMaxSourceStyleDegree(): error", e);
    	} finally {
    		sourceStyleSem.endRead(getDataType(), uniqueId);
    	}
    	return source;
    }
    public byte getSourceStyleSourceFromUniqueId(int styleId) {
    	byte result = DATA_SOURCE_UNKNOWN;
    	try {
    		sourceStyleSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumSourceStyles(); ++s) {
	    		if (sourceStyleIds[s] == styleId) {
	    			result = sourceStyleSources[s];
	    			break;
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getSourceStyleSourceFromUniqueId(): error", e);
    	} finally {
    		sourceStyleSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public String getSourceStyleDescription() {
    	StringBuffer result = new StringBuffer();
    	try {
    		sourceStyleSem.startRead(getDataType(), uniqueId);
	    	int added = 0;
	    	for (int i = 0; i < getNumSourceStyles(); ++i) {
	    		String styleName = Database.getStyleIndex().getIdentifierFromUniqueId(sourceStyleIds[i]).toString();
	    		StyleRecord style = (StyleRecord)Database.getStyleIndex().getRecord(new StyleIdentifier(styleName));
	    		if ((style != null) && !style.isCategoryOnly()) {
		    		if (result.length() > 0)
		    			result.append("; ");
		    		result.append(styleName);
		    		result.append(" (");
		    		result.append(String.valueOf(Math.round(sourceStyleDegrees[i] * 100.0f)));
		    		result.append("%)");
		    		++added;
		    		if (added >= MAX_STYLES_IN_DESCRIPTION)
		    			break;
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getSourceStyleDescription(): error", e);
    	} finally {
    		sourceStyleSem.endRead(getDataType(), uniqueId);
    	}
    	return result.toString();
    }
    public boolean containsSourceStyle(int styleId) {
    	boolean result = false;
    	if (sourceStyleIds != null) {
    		try {
    			sourceStyleSem.startRead(getDataType(), uniqueId);
		    	for (int id : sourceStyleIds) {
		    		if (id == styleId) {
		    			result = true;
		    			break;
		    		}
		    	}
    		} catch (Exception e) {
    			log.error("containsSourceStyle(): error", e);
    		} finally {
    			sourceStyleSem.endRead(getDataType(), uniqueId);
    		}
    	}
    	return result;
    }
    public boolean containsSourceStyle(String styleName) {
    	int styleId = Database.getStyleIndex().getUniqueIdFromIdentifier(new StyleIdentifier(styleName));
    	return containsSourceStyle(styleId);
    }

    // actual styles (after hierarchy & duplicates are taken into account)
    public int getNumActualStyles() {
    	checkActualStyles();
    	return (actualStyleIds != null) ? actualStyleIds.length : 0;
    }
    public int[] getActualStyleIds() {
    	checkActualStyles();
    	return actualStyleIds;
    }
    public float[] getActualStyleDegrees() {
    	checkActualStyles();
    	return actualStyleDegrees;
    }
    public Vector<DegreeValue> getActualStyleDegreeValues() {
    	checkActualStyles();
    	Vector<DegreeValue> result = new Vector<DegreeValue>(getNumActualStyles());
    	try {
    		actualStyleSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumActualStyles(); ++s) {
	    		String styleName = Database.getStyleIndex().getIdentifierFromUniqueId(actualStyleIds[s]).toString();
	    		result.add(new DegreeValue(styleName, actualStyleDegrees[s], DATA_SOURCE_COMPUTED));
	    	}
    	} catch (Exception e) {
    		log.error("getActualStyleDegreeValues(): error", e);
    	} finally {
    		actualStyleSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public float getActualStyleDegreeFromUniqueId(int styleId) {
    	checkActualStyles();
    	float result = 0.0f;
    	try {
    		actualStyleSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumActualStyles(); ++s) {
	    		if (actualStyleIds[s] == styleId) {
	    			result = actualStyleDegrees[s];
	    			break;
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getActualStyleDegreeFromUniqueId(): error", e);
    	} finally {
    		actualStyleSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public float getActualStyleDegree(String styleName) {
    	int styleId = Database.getStyleIndex().getUniqueIdFromIdentifier(new StyleIdentifier(styleName));
    	return getActualStyleDegreeFromUniqueId(styleId);
    }
    public float getActualStyleDegree(int styleId) {
    	return getActualStyleDegreeFromUniqueId(styleId);
    }
    public String getActualStyleDescription() {
    	return getActualStyleDescription(RE3Properties.getBoolean("show_style_degrees_in_description"), false, MAX_STYLES_IN_DESCRIPTION);
    }
    public String getActualStyleDescription(boolean showDegrees) {
    	return getActualStyleDescription(showDegrees, false, MAX_STYLES_IN_DESCRIPTION);
    }
    public String getActualStyleDescription(boolean showDegrees, int maxStyles) {
    	return getActualStyleDescription(showDegrees, false, maxStyles);
    }
    public String getActualStyleDescription(boolean showDegrees, boolean includeDuplicates, int maxStyles) {
    	checkActualStyles();
    	StringBuffer result = new StringBuffer();
    	try {
    		actualStyleSem.startRead(getDataType(), uniqueId);
	    	int added = 0;
	    	for (int i = 0; i < getNumActualStyles(); ++i) {
	    		Identifier id = Database.getStyleIndex().getIdentifierFromUniqueId(actualStyleIds[i]);
	    		if (id != null) {
		    		String styleName = id.toString();
		    		StyleRecord style = (StyleRecord)Database.getStyleIndex().getRecord(new StyleIdentifier(styleName));
		    		if ((style != null) && !style.isCategoryOnly() && (actualStyleDegrees[i] > 0.0f)) {
			    		if (result.length() > 0)
			    			result.append("; ");
			    		result.append(style.getStyleName());
			    		if (showDegrees) {
			    			result.append(" (");
			    			result.append(String.valueOf(Math.round(actualStyleDegrees[i] * 100.0f)));
			    			result.append("%)");
			    		}
			    		if (includeDuplicates) {
			    			for (int n = 0; n < style.getNumDuplicateIds(); ++n) {
			    				int duplicateId = style.getDuplicateId(n);
			    				StyleIdentifier styleId = (StyleIdentifier)Database.getStyleIndex().getIdentifierFromUniqueId(duplicateId);
			    				if (styleId != null) {
			    					result.append("; ");
			    					result.append(styleId.toString());
						    		if (showDegrees) {
						    			result.append(" (");
						    			result.append(String.valueOf(Math.round(actualStyleDegrees[i] * 100.0f)));
						    			result.append("%)");
						    		}
			    				}
			    			}
			    		}
			    		++added;
			    		if (added >= maxStyles)
			    			break;
		    		}
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getActualStyleDescription(): error", e);
    	} finally {
    		actualStyleSem.endRead(getDataType(), uniqueId);
    	}
    	return result.toString();
    }
    public boolean containsActualStyle(int styleId) {
    	checkActualStyles();
    	boolean result = false;
    	try {
    		actualStyleSem.startRead(getDataType(), uniqueId);
    		if (actualStyleIds != null) {
    			int i = 0;
	    		for (int id : actualStyleIds) {
	    			if ((id == styleId) && (actualStyleDegrees[i] > 0.0f)) {
	    				result = true;
	    				break;
	    			}
	    			++i;
	    		}
    		}
    	} catch (Exception e) {
    		log.error("containsActualStyle(): error", e);
    	} finally {
    		actualStyleSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public boolean containsActualStyle(String styleName) {
    	int styleId = Database.getStyleIndex().getUniqueIdFromIdentifier(new StyleIdentifier(styleName));
    	return containsActualStyle(styleId);
    }

    // source tags (before hierarchy & duplicates are taken into account)
    public int getNumSourceTags() {
    	return (sourceTagIds != null) ? sourceTagIds.length : 0;
    }
    public int[] getSourceTagIds() { return sourceTagIds; }
    public float[] getSourceTagDegrees() { return sourceTagDegrees; }
    public Vector<DegreeValue> getSourceTagDegreeValues() {
    	Vector<DegreeValue> result = new Vector<DegreeValue>(getNumSourceTags());
    	try {
    		sourceTagSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumSourceTags(); ++s) {
	    		Identifier id = Database.getTagIndex().getIdentifierFromUniqueId(sourceTagIds[s]);
	    		if (id != null) {
		    		String tagName = id.toString();
		    		result.add(new DegreeValue(tagName, sourceTagDegrees[s], sourceTagSources[s]));
	    		} else {
	    			log.warn("getSourceTagDegreeValues(): missing identifier for source tag id=" + sourceTagIds[s]);
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getSourceTagDegreeValues(): error", e);
    	} finally {
    		sourceTagSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public float getMaxSourceTagDegree(TagRecord tag) {
    	float result = 0.0f;
    	try {
    		sourceTagSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumSourceTags(); ++s) {
	    		if (sourceTagIds[s] == tag.getUniqueId()) {
	    			if (sourceTagDegrees[s] > result)
	    				result = sourceTagDegrees[s];
	    		} else if (tag.getDuplicateIds() != null) {
	    			for (int dupId : tag.getDuplicateIds()) {
	    				if (sourceTagIds[s] == dupId) {
	    	    			if (sourceTagDegrees[s] > result)
	    	    				result = sourceTagDegrees[s];
	    				}
	    			}
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getMaxSourceTagDegree(): error", e);
    	} finally {
    		sourceTagSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public float getSourceTagDegreeFromUniqueId(int tagId) {
    	float result = 0.0f;
    	try {
    		sourceTagSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumSourceTags(); ++s) {
	    		if (sourceTagIds[s] == tagId) {
	    			result = sourceTagDegrees[s];
	    			break;
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getSourceTagDegreeFromUniqueId(): error", e);
    	} finally {
    		sourceTagSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public byte getMaxSourceTagSource(TagRecord tag) {
    	float match = 0.0f;
    	byte source = DATA_SOURCE_UNKNOWN;
    	try {
    		sourceTagSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumSourceTags(); ++s) {
	    		if (sourceTagIds[s] == tag.getUniqueId()) {
	    			if (sourceTagDegrees[s] > match) {
	    				match = sourceTagDegrees[s];
	    				source = sourceTagSources[s];
	    			}
	    		} else if (tag.getDuplicateIds() != null) {
	    			for (int dupId : tag.getDuplicateIds()) {
	    				if (sourceTagIds[s] == dupId) {
	    	    			if (sourceTagDegrees[s] > match) {
	    	    				match = sourceTagDegrees[s];
	    	    				source = sourceTagSources[s];
	    	    			}
	    				}
	    			}
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getMaxSourceTagDegree(): error", e);
    	} finally {
    		sourceTagSem.endRead(getDataType(), uniqueId);
    	}
    	return source;
    }
    public byte getSourceTagSourceFromUniqueId(int tagId) {
    	byte result = DATA_SOURCE_UNKNOWN;
    	try {
    		sourceTagSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumSourceTags(); ++s) {
	    		if (sourceTagIds[s] == tagId) {
	    			result = sourceTagSources[s];
	    			break;
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getSourceTagSourceFromUniqueId(): error", e);
    	} finally {
    		sourceTagSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public String getSourceTagDescription() {
    	StringBuffer result = new StringBuffer();
    	try {
    		sourceTagSem.startRead(getDataType(), uniqueId);
	    	int added = 0;
	    	for (int i = 0; i < getNumSourceTags(); ++i) {
	    		String tagName = Database.getTagIndex().getIdentifierFromUniqueId(sourceTagIds[i]).toString();
	    		TagRecord tag = (TagRecord)Database.getTagIndex().getRecord(new TagIdentifier(tagName));
	    		if ((tag != null) && !tag.isCategoryOnly()) {
		    		if (result.length() > 0)
		    			result.append("; ");
		    		result.append(tagName);
		    		result.append(" (");
		    		result.append(String.valueOf(Math.round(sourceTagDegrees[i] * 100.0f)));
		    		result.append("%)");
		    		++added;
		    		if (added >= MAX_TAGS_IN_DESCRIPTION)
		    			break;
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getSourceTagDescription(): error", e);
    	} finally {
    		sourceTagSem.endRead(getDataType(), uniqueId);
    	}
    	return result.toString();
    }
    public boolean containsSourceTag(int tagId) {
    	boolean result = false;
    	if (sourceTagIds != null) {
    		try {
    			sourceTagSem.startRead(getDataType(), uniqueId);
	    		for (int id : sourceTagIds) {
		    		if (id == tagId) {
		    			result = true;
		    			break;
		    		}
	    		}
    		} catch (Exception e) {
    			log.error("containsSourceTag(): error", e);
    		} finally {
    			sourceTagSem.endRead(getDataType(), uniqueId);
    		}
    	}
    	return result;
    }
    public boolean containsSourceTag(String tagName) {
    	int tagId = Database.getTagIndex().getUniqueIdFromIdentifier(new TagIdentifier(tagName));
    	return containsSourceTag(tagId);
    }

    // actual tags (after hierarchy & duplicates are taken into account)
    public int getNumActualTags() {
    	checkActualTags();
    	return (actualTagIds != null) ? actualTagIds.length : 0;
    }
    public int[] getActualTagIds() {
    	checkActualTags();
    	return actualTagIds;
    }
    public float[] getActualTagDegrees() {
    	checkActualTags();
    	return actualTagDegrees;
    }
    public Vector<DegreeValue> getActualTagDegreeValues() {
    	checkActualTags();
    	Vector<DegreeValue> result = new Vector<DegreeValue>(getNumActualTags());
    	try {
    		actualTagSem.startRead(getDataType(), uniqueId);
	    	for (int s = 0; s < getNumActualTags(); ++s) {
	    		TagIdentifier tagId = (TagIdentifier)Database.getTagIndex().getIdentifierFromUniqueId(actualTagIds[s]);
	    		if (tagId != null) {
	    			String tagName = tagId.toString();
	    			result.add(new DegreeValue(tagName, actualTagDegrees[s], DATA_SOURCE_COMPUTED));
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getActualTagDegreeValues(): error", e);
    	} finally {
    		actualTagSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public float getActualTagDegreeFromUniqueId(int tagId) {
    	checkActualTags();
    	float result = 0.0f;
    	try {
    		actualTagSem.startRead(getDataType(), uniqueId);
    		for (int s = 0; s < getNumActualTags(); ++s) {
    			if (actualTagIds[s] == tagId) {
    				result = actualTagDegrees[s];
    				break;
    			}
    		}
    	} catch (Exception e) {
    		log.error("getActualTagDegreeFromUniqueId(): error", e);
    	} finally {
    		actualTagSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public float getActualTagDegree(String tagName) {
    	int tagId = Database.getTagIndex().getUniqueIdFromIdentifier(new TagIdentifier(tagName));
    	return getActualTagDegreeFromUniqueId(tagId);
    }
    public float getActualTagDegree(int tagId) {
    	return getActualTagDegreeFromUniqueId(tagId);
    }
    public String getActualTagDescription() {
    	return getActualTagDescription(RE3Properties.getBoolean("show_tag_degrees_in_description"), false, MAX_TAGS_IN_DESCRIPTION);
    }
    public String getActualTagDescription(boolean showDegrees) {
    	return getActualTagDescription(showDegrees, false, MAX_TAGS_IN_DESCRIPTION);
    }
    public String getActualTagDescription(boolean showDegrees, int maxTags) {
    	return getActualTagDescription(showDegrees, false, maxTags);
    }
    public String getActualTagDescription(boolean showDegrees, boolean includeDuplicates, int maxTags) {
    	checkActualTags();
    	StringBuffer result = new StringBuffer();
    	try {
    		actualTagSem.startRead(getDataType(), uniqueId);
	    	int added = 0;
	    	for (int i = 0; i < getNumActualTags(); ++i) {
	    		Identifier id = Database.getTagIndex().getIdentifierFromUniqueId(actualTagIds[i]);
	    		if (id != null) {
		    		String tagName = id.toString();
		    		TagRecord tag = (TagRecord)Database.getTagIndex().getRecord(new TagIdentifier(tagName));
		    		if ((tag != null) && !tag.isCategoryOnly() && (actualTagDegrees[i] > 0.0f)) {
			    		if (result.length() > 0)
			    			result.append("; ");
			    		result.append(tag.getTagName());
			    		if (showDegrees) {
			    			result.append(" (");
			    			result.append(String.valueOf(Math.round(actualTagDegrees[i] * 100.0f)));
			    			result.append("%)");
			    		}
			    		if (includeDuplicates) {
			    			for (int n = 0; n < tag.getNumDuplicateIds(); ++n) {
			    				int duplicateId = tag.getDuplicateId(n);
			    				TagIdentifier tagId = (TagIdentifier)Database.getTagIndex().getIdentifierFromUniqueId(duplicateId);
			    				if (tagId != null) {
			    					result.append("; ");
			    					result.append(tagId.toString());
						    		if (showDegrees) {
						    			result.append(" (");
						    			result.append(String.valueOf(Math.round(actualTagDegrees[i] * 100.0f)));
						    			result.append("%)");
						    		}
			    				}
			    			}
			    		}
			    		++added;
			    		if (added >= maxTags)
			    			break;
		    		}
	    		}
	    	}
    	} catch (Exception e) {
    		log.error("getActualTagDescription(): error", e);
    	} finally {
    		actualTagSem.endRead(getDataType(), uniqueId);
    	}
    	return result.toString();
    }
    public boolean containsActualTag(int tagId) {
    	checkActualTags();
    	boolean result = false;
    	try {
    		actualTagSem.startRead(getDataType(), uniqueId);
    		if (actualTagIds != null) {
    			int i = 0;
	    		for (int id : actualTagIds) {
	    			if ((id == tagId) && (actualTagDegrees[i] > 0.0f)) {
	    				result = true;
	    				break;
	    			}
	    			++i;
	    		}
    		}
    	} catch (Exception e) {
    		log.error("containsActualTag(): error", e);
    	} finally {
    		actualTagSem.endRead(getDataType(), uniqueId);
    	}
    	return result;
    }
    public boolean containsActualTag(String tagName) {
    	int tagId = Database.getTagIndex().getUniqueIdFromIdentifier(new TagIdentifier(tagName));
    	return containsActualTag(tagId);
    }

    public float getScore() {
    	if (Float.isNaN(score))
    		return 0.0f;
    	return score;
    }
    public float getPopularity() {
    	if (Float.isNaN(popularity))
    		return 0.0f;
    	return popularity;
    }

	public String getComments() {
		if (comments == null)
			return "";
		return comments;
	}

    public String getThumbnailImageFilename() {
    	if ((thumbnailImageFilename == null) || (thumbnailImageFilename.length() == 0))
    		return DEFAULT_THUMBNAIL_IMAGE;
    	return thumbnailImageFilename;
    }
    public boolean hasThumbnail() {
    	if ((thumbnailImageFilename == null) || (thumbnailImageFilename.length() == 0))
    		return false;
    	return true;
    }

    public Object getUserData(UserDataType type) {
    	if (userData == null)
    		return null;
    	for (int i = 0; i < userDataTypes.length; ++i) {
    		if (type.getId() == userDataTypes[i])
    			return userData[i];
    	}
    	return null;
    }

    public long getLastFetchedMinedProfile(byte dataSource) {
    	if (minedProfileSources != null) {
    		for (int i = 0; i < minedProfileSources.length; ++i) {
    			if (minedProfileSources[i] == dataSource)
    					return minedProfileSourcesLastUpdated[i];
    		}
    	}
    	return 0;
    }

    public boolean hasMinedProfileHeader(byte dataSource) {
    	if (minedProfileSources != null)
    		for (int i = 0; i < minedProfileSources.length; ++i)
    			if (minedProfileSources[i] == dataSource)
    				return true;
    	return false;
    }

    public boolean isExternalItem() { return isExternalItem; }

    public long getPlayCount() { return playCount; }

	public Date getDateAddedDate() { return new Date(dateAdded); }
	public long getDateAdded() { return dateAdded; }

	// getters
    public byte[] getSourceStyleSources() { return sourceStyleSources; }
	public byte[] getSourceTagSources() { return sourceTagSources; }
	public short[] getUserDataTypes() { return userDataTypes; }
	public Object[] getUserData() { return userData; }
	public byte[] getMinedProfileSources() { return minedProfileSources; }
	public long[] getMinedProfileSourcesLastUpdated() { return minedProfileSourcesLastUpdated; }

    /////////////
    // SETTERS //
    /////////////

    /**
     * This is the main method for setting a record's styles.
     * (should be the only method that alters the styleIds/styleDegrees arrays)
     */
	public void setStyles(Vector<DegreeValue> styleDegreeValues) {
		java.util.Collections.sort(styleDegreeValues);
		if (log.isTraceEnabled())
			log.trace("setStyles(): on=" + toString() + ", styleDegreeValues=" + styleDegreeValues);
		if (RE3Properties.getBoolean("do_not_add_styles_automatically")) {
			for (int i = 0; i < styleDegreeValues.size(); ++i) {
				DegreeValue degree = styleDegreeValues.get(i);
				if (degree.getSource() != DATA_SOURCE_USER) {
					styleDegreeValues.remove(i);
					--i;
				}
			}
		}
		Vector<String> sourceStylesToUpdate = new Vector<String>();
		try {
			getWriteLockSem().startRead("setStyles");
			sourceStyleSem.startWrite(getDataType(), uniqueId);
			// construct maps for old/new values
			Map<String, Object> oldStyles = new HashMap<String, Object>(getNumSourceStyles());
			for (int i = 0; i < getNumSourceStyles(); ++i) {
				StyleIdentifier styleId = (StyleIdentifier)Database.getStyleIndex().getIdentifierFromUniqueId(sourceStyleIds[i]);
				if (styleId != null)
					oldStyles.put(styleId.toString(), null);
			}
			Map<String, Object> newStyles = new HashMap<String, Object>(styleDegreeValues.size());
			for (DegreeValue degree : styleDegreeValues)
				newStyles.put(degree.getName(), null);
			for (String oldStyle : oldStyles.keySet())
				if (!newStyles.containsKey(oldStyle))
					sourceStylesToUpdate.add(oldStyle); // style was removed
			for (String newStyle : newStyles.keySet())
				if (!oldStyles.containsKey(newStyle))
					sourceStylesToUpdate.add(newStyle); // style was added
			// set new values
			sourceStyleIds = new int[styleDegreeValues.size()];
			sourceStyleDegrees = new float[styleDegreeValues.size()];
			sourceStyleSources = new byte[styleDegreeValues.size()];
			int i = 0;
			for (DegreeValue styleDegree : styleDegreeValues) {
				StyleIdentifier styleId = new StyleIdentifier(styleDegree.getName());
				sourceStyleIds[i] = Database.getStyleIndex().getUniqueIdFromIdentifier(styleId);
				sourceStyleDegrees[i] = styleDegree.getPercentage();
				sourceStyleSources[i] = styleDegree.getSource();
				++i;
			}
		} catch (Exception e) {
			log.error("setStyles(): error", e);
		} finally {
			sourceStyleSem.endWrite(getDataType(), uniqueId);
			getWriteLockSem().endRead();
		}
		invalidateActualStyles();
		Vector<StyleRecord> actualStylesToUpdate = new Vector<StyleRecord>(sourceStylesToUpdate.size());
		// update any styles added or removed
		for (String styleName : sourceStylesToUpdate) {
			StyleRecord style = Database.getStyleIndex().getStyleRecord(new StyleIdentifier(styleName));
			if (style != null)
				addActualStylesFromSourceStyles(actualStylesToUpdate, style);
		}
		for (StyleRecord style : actualStylesToUpdate)
			style.update();
		setRelationalItemsChanged(true);
	}

	private void addActualStylesFromSourceStyles(Vector<StyleRecord> actualStyles, StyleRecord style) {
		if (style != null) {
			if (!actualStyles.contains(style))
				actualStyles.add(style);
			for (HierarchicalRecord parentRecord : style.getParentRecords()) {
				addActualStylesFromSourceStyles(actualStyles, (StyleRecord)parentRecord);
			}
		}
	}

    public void removeStyleIdsBeyond(int maxStyleId) {
    	if (sourceStyleIds != null) {
    		int removed = 0;
    		for (int styleId : sourceStyleIds)
    			if (styleId >= maxStyleId)
    				++removed;
    		if (removed > 0) {
    			int[] newSourceStyleIds = new int[sourceStyleIds.length - removed];
    			float[] newSourceStyleDegrees = new float[sourceStyleIds.length - removed];
    			byte[] newSourceStyleSources = new byte[sourceStyleIds.length - removed];
    			int i = 0;
    			int c = 0;
    			for (int styleId : sourceStyleIds) {
    				if (styleId < maxStyleId) {
    					newSourceStyleIds[i] = sourceStyleIds[c];
    					newSourceStyleDegrees[i] = sourceStyleDegrees[c];
    					newSourceStyleSources[i] = sourceStyleSources[c];
    					++i;
    				}
    				++c;
    			}
    			sourceStyleIds = newSourceStyleIds;
    			sourceStyleDegrees = newSourceStyleDegrees;
    			sourceStyleSources = newSourceStyleSources;
    		}
    	}
    	if (actualStyleIds != null) {
    		int removed = 0;
    		for (int styleId : actualStyleIds)
    			if (styleId >= maxStyleId)
    				++removed;
    		if (removed >0) {
    			int[] newActualStyleIds = new int[actualStyleIds.length - removed];
    			float[] newActualStyleDegrees = new float[actualStyleIds.length - removed];
    			int i = 0;
    			int c = 0;
    			for (int styleId : actualStyleIds) {
    				if (styleId < maxStyleId) {
    					newActualStyleIds[i] = actualStyleIds[c];
    					newActualStyleDegrees[i] = actualStyleDegrees[c];
    					++i;
    				}
    				++c;
    			}
    			actualStyleIds = newActualStyleIds;
    			actualStyleDegrees = newActualStyleDegrees;
    		}
    	}
    }

    public void invalidateActualStyles() {
    	try {
    		getWriteLockSem().startRead("invalidateActualStyles");
    		if (log.isTraceEnabled())
    			log.trace("invalidateActualStyles(): called...");
    		actualStyleIds = null;
    	} catch (Exception e) {
    		log.error("invalidateActualStyles(): error", e);
    	} finally {
    		getWriteLockSem().endRead();
    	}
    }


    /**
     * This is the main method for setting a record's tags.
     * (should be the only method that alters the tagIds/tagDegrees arrays)
     */
    public void setTags(Vector<DegreeValue> tagDegreeValues) { setTags(tagDegreeValues, false); }
	public void setTags(Vector<DegreeValue> tagDegreeValues, boolean updateNow) {
		java.util.Collections.sort(tagDegreeValues);
		if (log.isTraceEnabled())
			log.trace("setTags(): on=" + toString() + ", tagDegreeValues=" + tagDegreeValues);
		if (RE3Properties.getBoolean("do_not_add_tags_automatically")) {
			for (int i = 0; i < tagDegreeValues.size(); ++i) {
				DegreeValue degree = tagDegreeValues.get(i);
				if (degree.getSource() != DATA_SOURCE_USER) {
					tagDegreeValues.remove(i);
					--i;
				}
			}
		}
		Vector<String> sourceTagsToUpdate = new Vector<String>();
		try {
			getWriteLockSem().startRead("setTags");
			sourceTagSem.startWrite(getDataType(), uniqueId);
			// construct maps for old/new values
			Map<String, Object> oldTags = new HashMap<String, Object>(getNumSourceTags());
			for (int i = 0; i < getNumSourceTags(); ++i) {
				TagIdentifier tagId = (TagIdentifier)Database.getTagIndex().getIdentifierFromUniqueId(sourceTagIds[i]);
				if (tagId != null)
					oldTags.put(tagId.toString(), null);
			}
			Map<String, Object> newTags = new HashMap<String, Object>(tagDegreeValues.size());
			for (DegreeValue degree : tagDegreeValues)
				newTags.put(degree.getName(), null);
			for (String oldTag : oldTags.keySet())
				if (!newTags.containsKey(oldTag))
					sourceTagsToUpdate.add(oldTag); // tag was removed
			for (String newTag : newTags.keySet())
				if (!oldTags.containsKey(newTag))
					sourceTagsToUpdate.add(newTag); // tag was added
			// set new values
			sourceTagIds = new int[tagDegreeValues.size()];
			sourceTagDegrees = new float[tagDegreeValues.size()];
			sourceTagSources = new byte[tagDegreeValues.size()];
			int i = 0;
			for (DegreeValue tagDegree : tagDegreeValues) {
				TagIdentifier tagId = new TagIdentifier(tagDegree.getName());
				sourceTagIds[i] = Database.getTagIndex().getUniqueIdFromIdentifier(tagId);
				sourceTagDegrees[i] = tagDegree.getPercentage();
				sourceTagSources[i] = tagDegree.getSource();
				++i;
			}
		} catch (Exception e) {
			log.error("setTags(): error", e);
		} finally {
			sourceTagSem.endWrite(getDataType(), uniqueId);
			getWriteLockSem().endRead();
		}
		invalidateActualTags();
		Vector<TagRecord> actualTagsToUpdate = new Vector<TagRecord>(sourceTagsToUpdate.size());
		// update any tags added or removed
		for (String tagName : sourceTagsToUpdate) {
			TagRecord tag = Database.getTagIndex().getTagRecord(new TagIdentifier(tagName));
			if (tag != null)
				addActualTagsFromSourceTags(actualTagsToUpdate, tag);
		}
		for (TagRecord tag : actualTagsToUpdate) {
			if (DELAY_TAG_UPDATES && !updateNow)
				tag.setNeedsUpdate(true);
			else
				tag.update();
		}
		setRelationalItemsChanged(true);
	}

	private void addActualTagsFromSourceTags(Vector<TagRecord> actualTags, TagRecord tag) {
		if (tag != null) {
			if (!actualTags.contains(tag))
				actualTags.add(tag);
			for (HierarchicalRecord parentRecord : tag.getParentRecords()) {
				addActualTagsFromSourceTags(actualTags, (TagRecord)parentRecord);
			}
		}
	}

    public void invalidateActualTags() {
		try {
			getWriteLockSem().startRead("invalidateActualTags");
    		if (log.isTraceEnabled())
    			log.trace("invalidateActualTags(): called...");
			actualTagIds = null;
		} catch (Exception e) {
			log.error("invalidateActualTags(): error", e);
		} finally {
			getWriteLockSem().endRead();
		}
    }

    public void removeTagIdsBeyond(int maxTagId) {
    	if (sourceTagIds != null) {
    		int removed = 0;
    		for (int tagId : sourceTagIds)
    			if (tagId >= maxTagId)
    				++removed;
    		if (removed > 0) {
    			int[] newSourceTagIds = new int[sourceTagIds.length - removed];
    			float[] newSourceTagDegrees = new float[sourceTagIds.length - removed];
    			byte[] newSourceTagSources = new byte[sourceTagIds.length - removed];
    			int i = 0;
    			int c = 0;
    			for (int tagId : sourceTagIds) {
    				if (tagId < maxTagId) {
    					newSourceTagIds[i] = sourceTagIds[c];
    					newSourceTagDegrees[i] = sourceTagDegrees[c];
    					newSourceTagSources[i] = sourceTagSources[c];
    					++i;
    				}
    				++c;
    			}
    			sourceTagIds = newSourceTagIds;
    			sourceTagDegrees = newSourceTagDegrees;
    			sourceTagSources = newSourceTagSources;
    		}
    	}
    	if (actualTagIds != null) {
    		int removed = 0;
    		for (int tagId : actualTagIds)
    			if (tagId >= maxTagId)
    				++removed;
    		if (removed >0) {
    			int[] newActualTagIds = new int[actualTagIds.length - removed];
    			float[] newActualTagDegrees = new float[actualTagIds.length - removed];
    			int i = 0;
    			int c = 0;
    			for (int tagId : actualTagIds) {
    				if (tagId < maxTagId) {
    					newActualTagIds[i] = actualTagIds[c];
    					newActualTagDegrees[i] = actualTagDegrees[c];
    					++i;
    				}
    				++c;
    			}
    			actualTagIds = newActualTagIds;
    			actualTagDegrees = newActualTagDegrees;
    		}
    	}
    }

	public void setScore(float score) { this.score = score; }
	public void setPopularity(float popularity) { this.popularity = popularity; }

    public void setComments(String comments) { this.comments = comments; }

	public void setThumbnailImageFilename(String thumbnailImageFilename) {
		this.thumbnailImageFilename = thumbnailImageFilename;
	}

	public void setUserData(UserData userDataValue) {
		if (userDataValue == null)
			return;
		try {
			userDataSem.acquire(uniqueId << 4 + getDataType());
			getWriteLockSem().startRead("setUserData");
			if (userData == null) {
				userDataTypes = new short[1];
				userData = new Object[1];
				userDataTypes[0] = userDataValue.getUserDataType().getId();
				userData[0] = userDataValue.getData();
			} else {
				boolean updated = false;
				for (int i = 0; i < userData.length; ++i) {
					if (userDataTypes[i] == userDataValue.getUserDataType().getId()) {
						userData[i] = userDataValue.getData();
						updated = true;
						break;
					}
				}
				if (!updated) {
					short[] newUserDataTypes = new short[userDataTypes.length + 1];
					Object[] newUserData = new Object[userData.length + 1];
					for (int i = 0; i < userData.length; ++i) {
						newUserDataTypes[i] = userDataTypes[i];
						newUserData[i] = userData[i];
					}
					newUserDataTypes[userData.length] = userDataValue.getUserDataType().getId();
					newUserData[userData.length] = userDataValue.getData();
					userDataTypes = newUserDataTypes;
					userData = newUserData;
				}
			}
		} catch (Exception e) {
			log.error("setUserData(): error", e);
		} finally {
			getWriteLockSem().endRead();
			userDataSem.release(uniqueId << 4 + getDataType());
		}
	}

	public void setDateAdded(long dateAdded) {
		this.dateAdded = dateAdded;
	}

	public void removeMinedProfileHeader(byte dataSource) {
		try {
			minedDataSem.acquire(uniqueId << 4 + getDataType());
			getWriteLockSem().startRead("removeMinedProfileHeader");
			if (minedProfileSources != null) {
				for (int i = 0; i < minedProfileSources.length; ++i) {
					if (minedProfileSources[i] == dataSource) {
						// remove at index i
						byte[] newMinedProfileHeaders = new byte[minedProfileSources.length - 1];
						long[] newMinedProfileHeadersLastUpdated = new long[minedProfileSources.length - 1];
						int c = 0;
						for (int j = 0; j < minedProfileSources.length; ++j) {
							if (j != i) {
								newMinedProfileHeaders[c] = minedProfileSources[j];
								newMinedProfileHeadersLastUpdated[c] = minedProfileSourcesLastUpdated[j];
								++c;
							}
						}
						minedProfileSources = newMinedProfileHeaders;
						minedProfileSourcesLastUpdated = newMinedProfileHeadersLastUpdated;
						return;
					}
				}
			}
		} catch (Exception e) {
			log.error("removeMinedProfileHeader(): error", e);
		} finally {
			getWriteLockSem().endRead();
			minedDataSem.release(uniqueId << 4 + getDataType());
		}
	}

	public void addMinedProfileHeader(MinedProfileHeader minedProfileHeader) { addMinedProfileHeader(minedProfileHeader, false); }
	public void addMinedProfileHeader(MinedProfileHeader minedProfileHeader, boolean flagForRefresh) { addMinedProfileHeader(minedProfileHeader, flagForRefresh ? 1 : System.currentTimeMillis()); }
	public void addMinedProfileHeader(MinedProfileHeader minedProfileHeader, long lastUpdated) {
		try {
			minedDataSem.acquire(uniqueId << 4 + getDataType());
			getWriteLockSem().startRead("addMinedProfileHeader");
			if (minedProfileSources == null) {
				minedProfileSources = new byte[1];
				minedProfileSourcesLastUpdated = new long[1];
				minedProfileSources[0] = minedProfileHeader.getDataSource();
				minedProfileSourcesLastUpdated[0] = lastUpdated;
			} else {
				boolean found = false;
				int i = 0;
				while ((i < minedProfileSources.length) && !found) {
					if (minedProfileSources[i] == minedProfileHeader.getDataSource()) {
						// update
						minedProfileSources[i] = minedProfileHeader.getDataSource();
						minedProfileSourcesLastUpdated[i] = lastUpdated;
						found = true;
					}
					++i;
				}
				if (!found) {
					byte[] newMinedProfileHeaders = new byte[1 + minedProfileSources.length];
					long[] newMinedProfileHeadersLastUpdated = new long[1 + minedProfileSources.length];
					i = 0;
					for (i = 0; i < minedProfileSources.length; ++i) {
						newMinedProfileHeaders[i] = minedProfileSources[i];
						newMinedProfileHeadersLastUpdated[i] = minedProfileSourcesLastUpdated[i];
					}
					newMinedProfileHeaders[minedProfileSources.length] = minedProfileHeader.getDataSource();
					newMinedProfileHeadersLastUpdated[minedProfileSources.length] = lastUpdated;
					minedProfileSources = newMinedProfileHeaders;
					minedProfileSourcesLastUpdated = newMinedProfileHeadersLastUpdated;
				}
			}
		} catch (Exception e) {
			log.error("addMinedProfileHeader(): error", e);
		} finally {
			getWriteLockSem().endRead();
			minedDataSem.release(uniqueId << 4 + getDataType());
		}
	}

	public void setExternalItem(boolean isExternalItem) { this.isExternalItem = isExternalItem; }

	public void setNumPlays(int numPlays) { this.playCount = numPlays; }
	@Override
	public void setPlayCount(long playCount) { this.playCount = playCount; }
	@Override
	public void incrementPlayCount(long increment) { playCount += increment; }

	// setters
	public void setSourceStyleSources(byte[] sourceStyleSources) { this.sourceStyleSources = sourceStyleSources; }
	public void setSourceTagSources(byte[] sourceTagSources) { this.sourceTagSources = sourceTagSources; }
	public void setUserDataTypes(short[] userDataTypes) { this.userDataTypes = userDataTypes; }
	public void setUserDataTypes(byte[] userDataTypes) {
		short[] converted = new short[userDataTypes.length];
		for (int i = 0; i < userDataTypes.length; ++i)
			converted[i] = userDataTypes[i];
		setUserDataTypes(converted);
	}
	public void setUserData(Object[] userData) { this.userData = userData; }
	public void setMinedProfileSources(byte[] minedProfileSources) { this.minedProfileSources = minedProfileSources; }
	public void setMinedProfileSourcesLastUpdated(long[] minedProfileSourcesLastUpdated) { this.minedProfileSourcesLastUpdated = minedProfileSourcesLastUpdated; }
	public void setSourceStyleIds(int[] sourceStyleIds) { this.sourceStyleIds = sourceStyleIds; }
	public void setSourceStyleDegrees(float[] sourceStyleDegrees) { this.sourceStyleDegrees = sourceStyleDegrees; }
	public void setActualStyleIds(int[] actualStyleIds) { this.actualStyleIds = actualStyleIds; }
	public void setActualStyleDegrees(float[] actualStyleDegrees) { this.actualStyleDegrees = actualStyleDegrees; }
	public void setSourceTagIds(int[] sourceTagIds) { this.sourceTagIds = sourceTagIds; }
	public void setSourceTagDegrees(float[] sourceTagDegrees) { this.sourceTagDegrees = sourceTagDegrees; }
	public void setActualTagIds(int[] actualTagIds) { this.actualTagIds = actualTagIds; }
	public void setActualTagDegrees(float[] actualTagDegrees) { this.actualTagDegrees = actualTagDegrees; }

	/////////////
	// METHODS //
	/////////////

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);
    	SearchRecord searchRecord = (SearchRecord)record;
    	// external flag
    	if (isExternalItem() && !searchRecord.isExternalItem())
    		setExternalItem(false);
    	// styles
    	Vector<DegreeValue> styleDegrees = getSourceStyleDegreeValues();
    	Vector<DegreeValue> otherStyleDegrees = searchRecord.getSourceStyleDegreeValues();
    	for (DegreeValue otherStyleDegree : otherStyleDegrees) {
    		int i = 0;
    		boolean found = false;
    		while ((i < styleDegrees.size()) && !found) {
    			DegreeValue styleDegree = styleDegrees.get(i);
    			if (styleDegree.getName().equals(otherStyleDegree.getName())) {
    				found = true;
    				// average the degrees
    				styleDegree.setPercentage((styleDegree.getPercentage() + otherStyleDegree.getPercentage()) / 2.0f);
    			}
    			++i;
    		}
    		if (!found) {
    			styleDegrees.add(otherStyleDegree);
    		}
    	}
    	setStyles(styleDegrees);
    	// tags
    	Vector<DegreeValue> tagDegrees = getSourceTagDegreeValues();
    	Vector<DegreeValue> otherTagDegrees = searchRecord.getSourceTagDegreeValues();
    	for (DegreeValue otherTagDegree : otherTagDegrees) {
    		int i = 0;
    		boolean found = false;
    		while ((i < tagDegrees.size()) && !found) {
    			DegreeValue tagDegree = tagDegrees.get(i);
    			if (tagDegree.getName().equals(otherTagDegree.getName())) {
    				found = true;
    				// average the degrees
    				tagDegree.setPercentage((tagDegree.getPercentage() + otherTagDegree.getPercentage()) / 2.0f);
    			}
    			++i;
    		}
    		if (!found) {
    			tagDegrees.add(otherTagDegree);
    		}
    	}
    	setTags(tagDegrees);
    	// score
    	if ((score != 0.0f) && (searchRecord.getScore() != 0.0f)) {
    		// average
    		score = (score + searchRecord.getScore()) / 2.0f;
    	} else if (searchRecord.getScore() != 0.0f) {
    		score = searchRecord.getScore();
    	}
    	// comments
    	if (searchRecord.getComments().length() > 0) {
    		if ((comments != null) && (comments.length() > 0))
    			comments += "; ";
    		comments += searchRecord.getComments();
    	}
    	// thumbnail
    	if ((thumbnailImageFilename == null) || (thumbnailImageFilename.length() == 0)) {
    		thumbnailImageFilename = searchRecord.thumbnailImageFilename;
    	}
    	// # plays
        playCount += searchRecord.playCount;
        // user data
    	if (searchRecord.userDataTypes != null) {
    		for (int i = 0; i < searchRecord.userDataTypes.length; ++i) {
    			short type = searchRecord.userDataTypes[i];
    			boolean found = false;
    			if (userDataTypes != null) {
    				int j = 0;
    				while ((j < userDataTypes.length) && !found) {
    					if (userDataTypes[j] == type) {
    						found = true;
    					}
    					++j;
    				}
    			}
    			if (!found) {
    				// add
    				if (userDataTypes == null) {
    					userDataTypes = new short[1];
    					userDataTypes[0] = type;
    					userData = new Object[1];
    					userData[0] = searchRecord.userData[i];
    				} else {
    					short[] newUserDataTypes = new short[userDataTypes.length + 1];
    					Object[] newUserData = new Object[userDataTypes.length + 1];
    					for (int j = 0; j < userDataTypes.length; ++j) {
    						newUserDataTypes[j] = userDataTypes[j];
    						newUserData[j] = userData[j];
    					}
    					newUserDataTypes[userDataTypes.length] = type;
    					newUserData[userDataTypes.length] = searchRecord.userData[i];
    					userDataTypes = newUserDataTypes;
    					userData = newUserData;
    				}
    			}
    		}
    	}
        // date added
        if ((dateAdded != 0) && (searchRecord.dateAdded != 0)) {
        	if (isExternalItem() && !searchRecord.isExternalItem())
        		dateAdded = searchRecord.dateAdded;
        	else if ((isExternalItem() && searchRecord.isExternalItem()) || (!isExternalItem() && !searchRecord.isExternalItem()))
        		dateAdded = Math.min(dateAdded, searchRecord.dateAdded);
        }
    }

    /**
     * This computes the "Pearson Correlation Coefficient" between 2 profiles,
     * using style degree information.
     *
     * Note: this implementation assumes the style degrees are sorted greatest to least...
     */
    public float computeStyleSimilarity(SearchRecord record) {
        //return PearsonSimilarity.computeSimilarity(getActualStyleIds(), actualStyleDegrees, record.getActualStyleIds(), record.actualStyleDegrees, maxStylesToCheck, minStyleDegreeThreshold);
        int[] ids1 = getActualStyleIds();
        float[] degrees1 = actualStyleDegrees;
        int[] ids2 = record.getActualStyleIds();
        float[] degrees2 = record.actualStyleDegrees;
        int maxIdsToCheck = maxStylesToCheck;
        float minDegreeThreshold = minStyleDegreeThreshold;
        float numerator = 0.0f;
        float denom1 = 0.0f;
        float denom2 = 0.0f;
        int amt = Math.min((ids1 != null) ? ids1.length : 0, maxIdsToCheck);
        int amt2 = Math.min((ids2 != null) ? ids2.length : 0, maxIdsToCheck);
        int s = 0;
        boolean stop = false;
        while ((s < amt) && !stop) {
        	if (degrees1[s] >= minDegreeThreshold) {
	            boolean found = false;
	            int s2 = 0;
	            while ((s2 < amt2) && !found) {
	                if (ids1[s] == ids2[s2]) {
	                    numerator += degrees1[s] * degrees2[s2];
	                    denom1 += degrees1[s] * degrees1[s];
	                    denom2 += degrees2[s2] * degrees2[s2];
	                    found = true;
	                }
	                ++s2;
	            }
	            if (!found) {
	                denom1 += degrees1[s] * degrees1[s];
	            }
        	} else {
        		stop = true;
        	}
        	++s;
        }
        s = 0;
        stop = false;
        while ((s < amt2) && !stop) {
        	if (degrees2[s] >= minDegreeThreshold) {
	        	boolean alreadyProcessed = false;
	            int s2 = 0;
	            while ((s2 < amt) && !alreadyProcessed) {
	                if (ids1[s2] == ids2[s])
	                    alreadyProcessed = true;
	                ++s2;
	            }
	            if (!alreadyProcessed) {
        			denom2 += degrees2[s] * degrees2[s];
	            }
        	} else {
        		stop = true;
        	}
        	++s;
        }
        float similarity = (float)(numerator / Math.sqrt(denom1 * denom2));
        return similarity;
    }


    /**
     * This computes the "Pearson Correlation Coefficient" between 2 profiles,
     * using tag degree information
     *
     * Note: this implementation assumes the style degrees are sorted greatest to least...
     */
    public float computeTagSimilarity(SearchRecord record) {
        //return PearsonSimilarity.computeSimilarity(getActualTagIds(), actualTagDegrees, record.getActualTagIds(), record.actualTagDegrees, maxTagsToCheck, minTagDegreeThreshold);
        int[] ids1 = getActualTagIds();
        float[] degrees1 = actualTagDegrees;
        int[] ids2 = record.getActualTagIds();
        float[] degrees2 = record.actualTagDegrees;
        int maxIdsToCheck = maxTagsToCheck;
        float minDegreeThreshold = minTagDegreeThreshold;
        float numerator = 0.0f;
        float denom1 = 0.0f;
        float denom2 = 0.0f;
        int amt = Math.min((ids1 != null) ? ids1.length : 0, maxIdsToCheck);
        int amt2 = Math.min((ids2 != null) ? ids2.length : 0, maxIdsToCheck);
        int s = 0;
        boolean stop = false;
        while ((s < amt) && !stop) {
        	if (degrees1[s] >= minDegreeThreshold) {
	            boolean found = false;
	            int s2 = 0;
	            while ((s2 < amt2) && !found) {
	                if (ids1[s] == ids2[s2]) {
	                    numerator += degrees1[s] * degrees2[s2];
	                    denom1 += degrees1[s] * degrees1[s];
	                    denom2 += degrees2[s2] * degrees2[s2];
	                    found = true;
	                }
	                ++s2;
	            }
	            if (!found) {
	                denom1 += degrees1[s] * degrees1[s];
	            }
        	} else {
        		stop = true;
        	}
        	++s;
        }
        s = 0;
        stop = false;
        while ((s < amt2) && !stop) {
        	if (degrees2[s] >= minDegreeThreshold) {
	        	boolean alreadyProcessed = false;
	            int s2 = 0;
	            while ((s2 < amt) && !alreadyProcessed) {
	                if (ids1[s2] == ids2[s])
	                    alreadyProcessed = true;
	                ++s2;
	            }
	            if (!alreadyProcessed) {
        			denom2 += degrees2[s] * degrees2[s];
	            }
        	} else {
        		stop = true;
        	}
        	++s;
        }
        float similarity = (float)(numerator / Math.sqrt(denom1 * denom2));
        return similarity;
    }

    public void checkActualStyles() {
    	if (actualStyleIds == null)
    		computeActualStyles();
    }
    public void computeActualStyles() {
    	if (log.isTraceEnabled())
    		log.trace("computeActualStyles(): started, this=" + this);
    	Map<Integer, Float> uniqueStyles = new HashMap<Integer, Float>(getNumSourceStyles());
    	try {
    		sourceStyleSem.startRead(getDataType(), uniqueId);
    		for (int s = 0; s < getNumSourceStyles(); ++s)
    			mergeStyleAndAllParents(sourceStyleIds[s], sourceStyleDegrees[s], uniqueStyles);
    	} catch (Exception e) {
    		log.error("computeActualStyles(): error", e);
    	} finally {
    		sourceStyleSem.endRead(getDataType(), uniqueId);
    	}
    	Vector<DegreeValue> result = new Vector<DegreeValue>(uniqueStyles.size());
    	float maxValue = 0.0f;
    	if (RE3Properties.getBoolean("normalize_actual_styles")) {
	    	for (Entry<Integer, Float> entry : uniqueStyles.entrySet())
	    		if (entry.getValue() > maxValue)
	    			maxValue = entry.getValue();
    	}
    	for (Entry<Integer, Float> entry : uniqueStyles.entrySet())
    		result.add(new DegreeValue(entry.getKey(), (maxValue > 0.0f) ? entry.getValue() / maxValue : entry.getValue(), DATA_SOURCE_COMPUTED));
		java.util.Collections.sort(result);
		try {
			actualStyleSem.startWrite(getDataType(), uniqueId);
    		actualStyleIds = new int[result.size()];
    		actualStyleDegrees = new float[result.size()];
    		int i = 0;
    		for (DegreeValue styleDegree : result) {
    			actualStyleIds[i] = (Integer)styleDegree.getObject();
    			actualStyleDegrees[i] = styleDegree.getPercentage();
    			++i;
    		}
		} catch (Exception e) {
			log.error("checkActualStyles(): error", e);
		} finally {
			actualStyleSem.endWrite(getDataType(), uniqueId);
		}
    	if (log.isTraceEnabled())
    		log.trace("computeActualStyles(): done");
    }
    private void mergeStyleAndAllParents(int styleId, float styleDegree, Map<Integer, Float> uniqueStyles) {
    	StyleRecord actualStyleRecord = Database.getStyleIndex().getStyleRecord(styleId);
    	if ((actualStyleRecord != null) && !actualStyleRecord.isDisabled()) { // && !actualStyleRecord.isCategoryOnly()) { // excluding categories prevents proper filtering
        	Float existingPercentage = uniqueStyles.get(actualStyleRecord.getUniqueId());
        	boolean addParents = false;
        	if (existingPercentage == null) {
        		existingPercentage = styleDegree;
        		addParents = true;
        	} else {
        		if (styleDegree > existingPercentage)
        			addParents = true;
        		existingPercentage = Math.max(existingPercentage, styleDegree);
        	}
    		uniqueStyles.put(actualStyleRecord.getUniqueId(), existingPercentage);
    		if (addParents) {
    			HierarchicalRecord[] parentStyles = actualStyleRecord.getParentRecords();
    			for (HierarchicalRecord parentStyle : parentStyles) {
    				if ((parentStyle != null) && !parentStyle.isRoot())
    					mergeStyleAndAllParents(parentStyle.getUniqueId(), styleDegree, uniqueStyles);
    			}
    		}
    	}
    }

    public void checkActualTags() {
    	if (actualTagIds == null)
    		computeActualTags();
    }
    public void computeActualTags() {
    	if (log.isTraceEnabled())
    		log.trace("computeActualTags(): started, this=" + this);
    	Map<Integer, Float> uniqueTags = new HashMap<Integer, Float>(getNumSourceTags());
    	try {
    		sourceTagSem.startRead(getDataType(), uniqueId);
    		for (int s = 0; s < getNumSourceTags(); ++s)
    			mergeTagAndAllParents(sourceTagIds[s], sourceTagDegrees[s], uniqueTags);
    	} catch (Exception e) {
    		log.error("computeActualTags(): error", e);
    	} finally {
    		sourceTagSem.endRead(getDataType(), uniqueId);
    	}
    	Vector<DegreeValue> result = new Vector<DegreeValue>(uniqueTags.size());
    	float maxValue = 0.0f;
    	if (RE3Properties.getBoolean("normalize_actual_tags")) {
	    	for (Entry<Integer, Float> entry : uniqueTags.entrySet())
	    		if (entry.getValue() > maxValue)
	    			maxValue = entry.getValue();
    	}
    	for (Entry<Integer, Float> entry : uniqueTags.entrySet())
    		result.add(new DegreeValue(entry.getKey(), (maxValue > 0.0f) ? entry.getValue() / maxValue : entry.getValue(), DATA_SOURCE_COMPUTED));
		java.util.Collections.sort(result);
		try {
			actualTagSem.startWrite(getDataType(), uniqueId);
    		actualTagIds = new int[result.size()];
    		actualTagDegrees = new float[result.size()];
    		int i = 0;
    		for (DegreeValue tagDegree : result) {
    			actualTagIds[i] = (Integer)tagDegree.getObject();
    			actualTagDegrees[i] = tagDegree.getPercentage();
    			++i;
    		}
		} catch (Exception e) {
			log.error("checkActualTags(): error", e);
		} finally {
			actualTagSem.endWrite(getDataType(), uniqueId);
		}
    	if (log.isTraceEnabled())
    		log.trace("computeActualTags(): done");
    }
    private void mergeTagAndAllParents(int tagId, float tagDegree, Map<Integer, Float> uniqueTags) {
    	TagRecord actualTagRecord = Database.getTagIndex().getTagRecord(tagId);
    	if ((actualTagRecord != null) && !actualTagRecord.isDisabled()) { // && !actualTagRecord.isCategoryOnly()) { // excluding categories causes filtering problems
        	Float existingPercentage = uniqueTags.get(actualTagRecord.getUniqueId());
        	boolean addParents = false;
        	if (existingPercentage == null) {
        		existingPercentage = tagDegree;
        		addParents = true;
        	} else {
        		if (tagDegree > existingPercentage)
        			addParents = true;
        		existingPercentage = Math.max(existingPercentage, tagDegree);
        	}
    		uniqueTags.put(actualTagRecord.getUniqueId(), existingPercentage);
    		if (addParents) {
    			HierarchicalRecord[] parentTags = actualTagRecord.getParentRecords();
    			for (HierarchicalRecord parentTag : parentTags) {
    				if ((parentTag != null) && !parentTag.isRoot())
    					mergeTagAndAllParents(parentTag.getUniqueId(), tagDegree, uniqueTags);
    			}
    		}
    	}
    }

    public void computeScore() {

    }

    public void computePopularity() {

    }

    @Override
	public void write(LineWriter textWriter) {
    	super.write(textWriter);
    	textWriter.writeLine(2); // version
    	if (sourceStyleIds != null) {
    		textWriter.writeLine(sourceStyleIds.length);
    		for (int sourceStyleId : sourceStyleIds)
    			textWriter.writeLine(sourceStyleId);
    		for (float sourceStyleDegree : sourceStyleDegrees)
    			textWriter.writeLine(sourceStyleDegree);
    		for (byte sourceStyleSource : sourceStyleSources)
    			textWriter.writeLine(sourceStyleSource);
    	} else {
    		textWriter.writeLine(0);
    	}
    	if (actualStyleIds != null) {
    		textWriter.writeLine(actualStyleIds.length);
    		for (int actualStyleId : actualStyleIds)
    			textWriter.writeLine(actualStyleId);
    		for (float actualStyleDegree : actualStyleDegrees)
    			textWriter.writeLine(actualStyleDegree);
    	} else {
    		textWriter.writeLine(0);
    	}
    	if (sourceTagIds != null) {
    		textWriter.writeLine(sourceTagIds.length);
    		for (int sourceTagId : sourceTagIds)
    			textWriter.writeLine(sourceTagId);
    		for (float sourceTagDegree : sourceTagDegrees)
    			textWriter.writeLine(sourceTagDegree);
    		for (byte sourceTagSource : sourceTagSources)
    			textWriter.writeLine(sourceTagSource);
    	} else {
    		textWriter.writeLine(0);
    	}
    	if (actualTagIds != null) {
    		textWriter.writeLine(actualTagIds.length);
    		for (int actualTagId : actualTagIds)
    			textWriter.writeLine(actualTagId);
    		for (float actualTagDegree : actualTagDegrees)
    			textWriter.writeLine(actualTagDegree);
    	} else {
    		textWriter.writeLine(0);
    	}
    	textWriter.writeLine(score);
    	textWriter.writeLine(popularity);
    	String[] splitComments = StringUtil.getLines(this.comments);
    	textWriter.writeLine(splitComments.length);
    	for (String comment : splitComments)
    		textWriter.writeLine(comment);
    	textWriter.writeLine(thumbnailImageFilename);
    	if (userDataTypes != null) {
    		textWriter.writeLine(userDataTypes.length);
    		for (short userDataType : userDataTypes)
    			textWriter.writeLine(userDataType);
    		for (Object data : userData) {
    			if (data instanceof Boolean) {
    				textWriter.writeLine(1);
    				textWriter.writeLine((Boolean)data);
    			} else if (data instanceof Integer) {
    				textWriter.writeLine(3);
    				textWriter.writeLine((Integer)data);
    			} else {
    				textWriter.writeLine(2);
    				textWriter.writeLine((String)data);
    			}
    		}
    	} else {
    		textWriter.writeLine(0);
    	}
    	if (minedProfileSources != null) {
    		textWriter.writeLine(minedProfileSources.length);
    		for (byte minedProfileSource : minedProfileSources)
    			textWriter.writeLine(minedProfileSource);
    		for (long minedProfileSourceLastUpdated : minedProfileSourcesLastUpdated)
    			textWriter.writeLine(minedProfileSourceLastUpdated);
    	} else {
    		textWriter.writeLine(0);
    	}
    	textWriter.writeLine(isExternalItem);
    	textWriter.writeLine(playCount);
    	textWriter.writeLine(dateAdded);
    }

    @Override
	public void addFieldsToDocument(Document document) {
    	super.addFieldsToDocument(document);
    	Field styleField = new Field("style", getActualStyleDescription(false, true, Integer.MAX_VALUE), Field.Store.NO, Field.Index.ANALYZED);
    	styleField.setBoost(RE3Properties.getFloat("style_field_boost"));
    	document.add(styleField);
    	Field tagField = new Field("tag", getActualTagDescription(false, true, Integer.MAX_VALUE), Field.Store.NO, Field.Index.ANALYZED);
    	tagField.setBoost(RE3Properties.getFloat("tag_field_boost"));
    	document.add(tagField);
    	document.add(new Field("comment", getComments(), Field.Store.NO, Field.Index.ANALYZED));
    	document.add(new Field("is_external", isExternalItem() ? "1" : "0", Field.Store.NO, Field.Index.ANALYZED));
    	for (UserDataType userType : ((SearchIndex)getIndex()).getUserDataTypes()) {
    		if (userType.getFieldType() == UserDataType.TYPE_TEXT_FIELD) {
    			Object userData = getUserData(userType);
    			if (userData != null)
    				document.add(new Field(userType.getTitle().toLowerCase(), userData.toString(), Field.Store.NO, Field.Index.ANALYZED));
    		}
    	}
    }

    @Override
	public JSONObject getJSON(ModelManagerInterface modelManager) {
    	try {
			JSONObject result = new JSONObject();
			result.put("id", getUniqueId());
			result.put("name", this.toString());
			for (int c = 0; c < modelManager.getNumColumns(); ++c) {
				Column column = modelManager.getViewColumnType(c);
				result.put(column.getColumnTitleId(), modelManager.getSourceData(column.getColumnId(), this));
			}
			return result;
    	} catch (Exception e) {
    		log.error("getJSONSong(): error", e);
    	}
    	return null;
    }
}
