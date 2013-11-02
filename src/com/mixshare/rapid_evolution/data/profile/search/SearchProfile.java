package com.mixshare.rapid_evolution.data.profile.search;

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

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.profile.CommonProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.ProfileManager;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.data.profile.common.link.VideoLink;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.user.UserData;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.SubmittedSearchProfile;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * This defines profile data common to all the search types (artist/label/release/song).
 */
abstract public class SearchProfile extends CommonProfile {

    static private Logger log = Logger.getLogger(SearchProfile.class);

    static private float STYLE_DISCARD_THRESHOLD = RE3Properties.getFloat("style_discard_threshold");
    static private float TAG_DISCARD_THRESHOLD = RE3Properties.getFloat("tag_discard_threshold");

    ////////////
    // FIELDS //
    ////////////

	// the source variables identify where various pieces of information originated
    protected byte commentsSource = DATA_SOURCE_UNKNOWN;
    protected byte thumbnailImageFilenameSource = DATA_SOURCE_UNKNOWN;

    protected Map<MinedProfile, Long> minedProfiles = new HashMap<MinedProfile, Long>();

    protected Vector<Image> userImages = new Vector<Image>();
    protected Vector<Image> images = new Vector<Image>();
    protected Vector<Link> links = new Vector<Link>();
    protected Vector<VideoLink> videoLinks = new Vector<VideoLink>();

    transient protected Map<Byte, Object> fetchedDataSources;

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(SearchProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("styles") || pd.getName().equals("style") || pd.getName().equals("computedStyles") || pd.getName().equals("tags") || pd.getName().equals("tag")
    					 || pd.getName().equals("computedTags") || pd.getName().equals("score") || pd.getName().equals("comments") || pd.getName().equals("thumbnailImage") || pd.getName().equals("thumbnailImageFilename")
    					 || pd.getName().equals("userData") || pd.getName().equals("externalItem") || pd.getName().equals("numPlays") || pd.getName().equals("dateAdded")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    public SearchProfile() { super(); }
    public SearchProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	commentsSource = Byte.parseByte(lineReader.getNextLine());
    	thumbnailImageFilenameSource = Byte.parseByte(lineReader.getNextLine());
    	if (version < 2) {
    		int numMinedProfiles = Integer.parseInt(lineReader.getNextLine());
    		//minedProfiles = new HashMap<MinedProfile, Long>(numMinedProfiles);
    		for (int i = 0; i < numMinedProfiles; ++i) {
    			MinedProfile minedProfile = null; //MinedProfileSerializer.read(lineReader);
    			long time = Long.parseLong(lineReader.getNextLine());
    			//minedProfiles.put(minedProfile, time);
    		}
    	}
    	int numUserImages = Integer.parseInt(lineReader.getNextLine());
    	userImages = new Vector<Image>(numUserImages);
    	for (int i = 0; i < numUserImages; ++i) {
    		Image image = new Image(lineReader);
    		userImages.add(image);
    	}
    	int numImages = Integer.parseInt(lineReader.getNextLine());
    	images = new Vector<Image>(numImages);
    	for (int i = 0; i < numImages; ++i) {
    		Image image = new Image(lineReader);
    		images.add(image);
    	}
    	int numLinks = Integer.parseInt(lineReader.getNextLine());
    	links = new Vector<Link>(numLinks);
    	for (int i = 0; i < numLinks; ++i) {
    		Link link = new Link(lineReader);
    		links.add(link);
    	}
    	int numVideoLinks = Integer.parseInt(lineReader.getNextLine());
    	videoLinks = new Vector<VideoLink>(numVideoLinks);
    	for (int i = 0; i < numVideoLinks; ++i) {
    		VideoLink videoLink = new VideoLink(lineReader);
    		videoLinks.add(videoLink);
    	}
    }

    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    abstract public float getSimilarity(SearchRecord record);
    abstract public void clearSimilaritySearchTransients();

    abstract public void computeChanges(byte changedDataSource);
    abstract public void computeScore();
    abstract public void computePopularity();
    abstract public void computeTags();
    abstract public void computeStyles();
    abstract public void computeLinks();
    abstract public void computeVideoLinks();
    abstract public void computeImages();

    /////////////
    // GETTERS //
    /////////////

    public SearchRecord getSearchRecord() { return (SearchRecord)record; }

    // exact styles (before hierarchy & duplicates are considered)
    public int getNumSourceStyles() { return getSearchRecord().getNumSourceStyles(); }
    public Vector<DegreeValue> getSourceStyleDegrees() { return getSearchRecord().getSourceStyleDegreeValues(); }
    public float getSourceStyleDegreeFromUniqueId(int styleId) { return getSearchRecord().getSourceStyleDegreeFromUniqueId(styleId); }
    public float getMaxSourceStyleDegree(StyleRecord style) { return getSearchRecord().getMaxSourceStyleDegree(style); }
    public byte getMaxSourceStyleSource(StyleRecord style) { return getSearchRecord().getMaxSourceStyleSource(style); }
    public byte getSourceStyleSourceFromUniqueId(int styleId) { return getSearchRecord().getSourceStyleSourceFromUniqueId(styleId); }
    public boolean containsSourceStyle(short styleId) { return getSearchRecord().containsSourceStyle(styleId); }
    public boolean containsSourceStyle(String styleName) { return getSearchRecord().containsSourceStyle(styleName); }

    // actual styles (after hierarchy & duplicates are considered)
    public int getNumActualStyles() { return getSearchRecord().getNumActualStyles(); }
    public float getActualStyleDegree(String styleName) { return getSearchRecord().getActualStyleDegree(styleName); }
    public float getActualStyleDegreeFromUniqueId(int styleId) { return getSearchRecord().getActualStyleDegreeFromUniqueId(styleId); }
    public Vector<DegreeValue> getActualStyleDegrees() { return getSearchRecord().getActualStyleDegreeValues(); }
    public boolean containsActualStyle(short styleId) { return getSearchRecord().containsActualStyle(styleId); }
    public boolean containsActualStyle(String styleName) { return getSearchRecord().containsActualStyle(styleName); }

    // exact tags (before hierarchy & duplicates are considered)
    public int getNumSourceTags() { return getSearchRecord().getNumSourceTags(); }
    public Vector<DegreeValue> getSourceTagDegrees() { return getSearchRecord().getSourceTagDegreeValues(); }
    public float getSourceTagDegreeFromUniqueId(int tagId) { return getSearchRecord().getSourceTagDegreeFromUniqueId(tagId); }
    public float getMaxSourceTagDegree(TagRecord tag) { return getSearchRecord().getMaxSourceTagDegree(tag); }
    public byte getMaxSourceTagSource(TagRecord tag) { return getSearchRecord().getMaxSourceTagSource(tag); }
    public byte getSourceTagSourceFromUniqueId(int tagId) { return getSearchRecord().getSourceTagSourceFromUniqueId(tagId); }
    public boolean containsSourceTag(short tagId) { return getSearchRecord().containsSourceTag(tagId); }
    public boolean containsSourceTag(String tagName) { return getSearchRecord().containsSourceTag(tagName); }

    // actual tags (after hierarchy & duplicates are considered)
    public int getNumActualTags() { return getSearchRecord().getNumActualTags(); }
    public float getActualTagDegree(String tagName) { return getSearchRecord().getActualTagDegree(tagName); }
    public float getActualTagDegreeFromUniqueId(int tagId) { return getSearchRecord().getActualTagDegreeFromUniqueId(tagId); }
    public Vector<DegreeValue> getActualTagDegrees() { return getSearchRecord().getActualTagDegreeValues(); }
    public boolean containsActualTag(short tagId) { return getSearchRecord().containsActualTag(tagId); }
    public boolean containsActualTag(String tagName) { return getSearchRecord().containsActualTag(tagName); }

    public float getScore() { return getSearchRecord().getScore(); }

    public String getComments() { return getSearchRecord().getComments(); }
    public byte getCommentsSource() { return commentsSource; }

    public String getThumbnailImageFilename() { return getSearchRecord().getThumbnailImageFilename(); }
    public byte getThumbnailImageFilenameSource() { return thumbnailImageFilenameSource; }
    public boolean hasThumbnail() { return getSearchRecord().hasThumbnail(); }

    public Object getUserData(UserDataType type) { return getSearchRecord().getUserData(type); }

    public Vector<Image> getAllImages() { return images; }
    public Vector<Image> getImages() {
    	Vector<Image> actualImages = new Vector<Image>();
    	for (Image image : images)
    		if (!image.isDisabled())
    			actualImages.add(image);
    	return actualImages;
    }
    public Vector<Image> getUserImages() { return userImages; }

    public long getLastFetchedMinedProfile(MinedProfileHeader header) { return getSearchRecord().getLastFetchedMinedProfile(header.getDataSource()); }
    public MinedProfile getMinedProfile(byte dataSource) {
    	if (fetchedDataSources == null)
    		fetchedDataSources = new HashMap<Byte, Object>();
    	if (!fetchedDataSources.containsKey(dataSource)) {
    		MinedProfile minedProfile = ProfileManager.getMinedProfile(getIdentifier().getType(), dataSource, getUniqueId());
    		if (minedProfile != null)
    			minedProfiles.put(minedProfile, getSearchRecord().getLastFetchedMinedProfile(dataSource));
    		else
        		ProfileManager.deleteMinedProfile(getIdentifier().getType(), dataSource, getUniqueId());
    		fetchedDataSources.put(dataSource, null);
    	}
    	for (MinedProfile minedProfile : minedProfiles.keySet()) {
    		if (minedProfile.getHeader().getDataSource() == dataSource)
    			return minedProfile;
    	}
    	return null;
    }
    public boolean hasMinedProfileHeader(byte dataSource) { return getSearchRecord().hasMinedProfileHeader(dataSource); }
    public boolean hasMinedProfile(byte dataSource) {
    	if (!getSearchRecord().hasMinedProfileHeader(dataSource))
    		return false;
    	MinedProfile minedProfile  = getMinedProfile(dataSource);
    	if ((minedProfile != null) && minedProfile.isValid())
    		return true;
    	return false;
    }
    public void addMinedProfileHeader(MinedProfileHeader minedProfileHeader) { getSearchRecord().addMinedProfileHeader(minedProfileHeader); }
    public void addMinedProfileHeader(MinedProfileHeader minedProfileHeader, long lastUpdated) { getSearchRecord().addMinedProfileHeader(minedProfileHeader, lastUpdated); }

    public Vector<Link> getLinks() {
    	Vector<Link> actualLinks = new Vector<Link>();
    	for (Link link : links) {
    		if (!link.isDisabled())
    			actualLinks.add(link);
    	}
    	return actualLinks;
    }
    public Vector<VideoLink> getVideoLinks() {
    	Vector<VideoLink> actualVideos = new Vector<VideoLink>();
    	for (VideoLink videoLink : videoLinks)
    		if (!videoLink.isDisabled())
    			actualVideos.add(videoLink);
    	return actualVideos;
    }

    public boolean isExternalItem() { return (getSearchRecord() != null) ? getSearchRecord().isExternalItem() : false; }

	public long getPlayCount() { return (getSearchRecord() != null) ? getSearchRecord().getPlayCount() : 0; }

	public Date getDateAddedDate() { return (getSearchRecord() != null) ? getSearchRecord().getDateAddedDate() : null; }
	public long getDateAdded() { return (getSearchRecord() != null) ? getSearchRecord().getDateAdded() : null; }

	public Map<MinedProfile, Long> getAllMinedProfiles() {
		if (getSearchRecord().getMinedProfileSources() != null)
			for (byte dataSource : getSearchRecord().getMinedProfileSources())
				getMinedProfile(dataSource);
		Map<MinedProfile, Long> result = new HashMap<MinedProfile, Long>();
		for (Entry<MinedProfile, Long> entry : minedProfiles.entrySet())
			result.put(entry.getKey(), entry.getValue());
		return result;
	}

	// for serialization
	public Map<MinedProfile, Long> getMinedProfiles() { return minedProfiles; }

    /////////////
    // SETTERS //
    /////////////

	public void setStyles(Vector<DegreeValue> styleDegreeValues) { getSearchRecord().setStyles(styleDegreeValues); }
	public void setStyle(DegreeValue styleDegree) {
		if (getSearchRecord().containsSourceStyle(styleDegree.getName())) {
			// updating
			Vector<DegreeValue> sourceDegrees = getSourceStyleDegrees();
			for (DegreeValue sourceDegree : sourceDegrees) {
				if (sourceDegree.getName().equalsIgnoreCase(styleDegree.getName())) {
					if (!((sourceDegree.getSource() == DATA_SOURCE_USER) && (styleDegree.getSource() != DATA_SOURCE_USER))) {
						sourceDegree.setPercentage(styleDegree.getPercentage());
						sourceDegree.setSource(styleDegree.getSource());
						setStyles(sourceDegrees);
						break;
					}
				}
			}
		} else {
			// adding
			Vector<DegreeValue> sourceDegrees = getSourceStyleDegrees();
			sourceDegrees.add(styleDegree);
			setStyles(sourceDegrees);
		}
	}
	public void addStyle(DegreeValue styleDegree) {
		Vector<DegreeValue> styleDegrees = new Vector<DegreeValue>(1);
		styleDegrees.add(styleDegree);
		addStyles(styleDegrees);
	}
	public void addStyles(Vector<DegreeValue> newStyleDegreeValues) {
		Vector<DegreeValue> existingStyleDegrees = getSearchRecord().getSourceStyleDegreeValues();
		for (DegreeValue newStyleDegree : newStyleDegreeValues) {
			boolean found = false;
			int i = 0;
			while ((i < existingStyleDegrees.size()) && !found) {
				DegreeValue existingStyleDegree = existingStyleDegrees.get(i);
				if (existingStyleDegree.getName().equalsIgnoreCase(newStyleDegree.getName())) {
					found = true;
					// updating
					if (!((existingStyleDegree.getSource() == DATA_SOURCE_USER) && (newStyleDegree.getSource() != DATA_SOURCE_USER))) {
						existingStyleDegree.setPercentage(newStyleDegree.getPercentage());
						existingStyleDegree.setSource(newStyleDegree.getSource());
					}
					break;
				}
				++i;
			}
			if (!found) {
				// adding
				existingStyleDegrees.add(newStyleDegree);
			}
		}
		setStyles(existingStyleDegrees);
	}
	public void removeStyle(String styleName) {
		Vector<DegreeValue> existingStyleDegrees = getSearchRecord().getSourceStyleDegreeValues();
		boolean found = false;
		int i = 0;
		while ((i < existingStyleDegrees.size()) && !found) {
			DegreeValue existingStyleDegree = existingStyleDegrees.get(i);
			if (existingStyleDegree.getName().equalsIgnoreCase(styleName)) {
				found = true;
				existingStyleDegrees.remove(i);
				break;
			}
			++i;
		}
		if (found)
			setStyles(existingStyleDegrees);
	}
	public void setComputedStyles(Vector<DegreeValue> styleDegrees) {
		if (log.isTraceEnabled())
			log.trace("setComputedStyles(): styleDegrees=" + styleDegrees);
		Vector<DegreeValue> existingDegrees = getSearchRecord().getSourceStyleDegreeValues();
		if (log.isTraceEnabled())
			log.trace("setComputedStyles(): existing style degrees=" + existingDegrees);
		for (int i = 0; i < existingDegrees.size(); ++i) {
			DegreeValue degreeValue = existingDegrees.get(i);
			if (degreeValue.getSource() == DATA_SOURCE_COMPUTED) {
				existingDegrees.remove(i);
				--i;
			}
		}
		if (log.isTraceEnabled())
			log.trace("setComputedStyles(): existingDegrees after removing computed=" + existingDegrees);
		for (DegreeValue styleDegree : styleDegrees) {
			boolean found = false;
			int i = 0;
			while ((i < existingDegrees.size()) && !found) {
				DegreeValue existingDegree = existingDegrees.get(i);
				if (existingDegree.getName().equalsIgnoreCase(styleDegree.getName())) {
					found = true;
					if (!((existingDegree.getSource() == DATA_SOURCE_USER) && (styleDegree.getSource() != DATA_SOURCE_USER))) {
						existingDegree.setPercentage(styleDegree.getPercentage());
						existingDegree.setSource(styleDegree.getSource());
						if (log.isTraceEnabled())
							log.trace("setComputedStyles(): updating percentage/src for style=" + existingDegree);
					}
				}
				++i;
			}
			if (!found) {
				existingDegrees.add(styleDegree);
				if (log.isTraceEnabled())
					log.trace("setComputedStyles(): adding new style=" + styleDegree);
			}
		}
    	for (int i = 0; i < existingDegrees.size(); ++i) {
    		DegreeValue degree = existingDegrees.get(i);
    		if (degree.getPercentage() < STYLE_DISCARD_THRESHOLD) {
    			existingDegrees.remove(i);
    			--i;
    		}
    	}
		if (log.isTraceEnabled())
			log.trace("setComputedStyles(): existingDegrees after removing low threshold=" + existingDegrees);
		setStyles(existingDegrees);
	}

	public void setTags(Vector<DegreeValue> tagDegreeValues) { setTags(tagDegreeValues, false); }
	public void setTags(Vector<DegreeValue> tagDegreeValues, boolean updateNow) { getSearchRecord().setTags(tagDegreeValues, updateNow); }
	public void setTag(DegreeValue tagDegree) { setTag(tagDegree, false); }
	public void setTag(DegreeValue tagDegree, boolean updateNow) {
		if (getSearchRecord().containsSourceTag(tagDegree.getName())) {
			// updating
			Vector<DegreeValue> sourceDegrees = getSourceTagDegrees();
			for (DegreeValue sourceDegree : sourceDegrees) {
				if (sourceDegree.getName().equalsIgnoreCase(tagDegree.getName())) {
					if (!((sourceDegree.getSource() == DATA_SOURCE_USER) && (tagDegree.getSource() != DATA_SOURCE_USER))) {
						sourceDegree.setPercentage(tagDegree.getPercentage());
						sourceDegree.setSource(tagDegree.getSource());
						setTags(sourceDegrees, updateNow);
						break;
					}
				}
			}
		} else {
			// adding
			Vector<DegreeValue> sourceDegrees = getSourceTagDegrees();
			sourceDegrees.add(tagDegree);
			setTags(sourceDegrees, updateNow);
		}
	}
	public void addTag(DegreeValue tagDegree) { addTag(tagDegree, false); }
	public void addTag(DegreeValue tagDegree, boolean updateNow) {
		Vector<DegreeValue> tagDegrees = new Vector<DegreeValue>(1);
		tagDegrees.add(tagDegree);
		addTags(tagDegrees, updateNow);
	}
	public void addTags(Vector<DegreeValue> newTagDegreeValues) { addTags(newTagDegreeValues, false); }
	public void addTags(Vector<DegreeValue> newTagDegreeValues, boolean updateNow) {
		Vector<DegreeValue> existingTagDegrees = getSearchRecord().getSourceTagDegreeValues();
		for (DegreeValue newTagDegree : newTagDegreeValues) {
			boolean found = false;
			int i = 0;
			while ((i < existingTagDegrees.size()) && !found) {
				DegreeValue existingTagDegree = existingTagDegrees.get(i);
				if (existingTagDegree.getName().equalsIgnoreCase(newTagDegree.getName())) {
					found = true;
					// updating
					if (!((existingTagDegree.getSource() == DATA_SOURCE_USER) && (newTagDegree.getSource() != DATA_SOURCE_USER))) {
						existingTagDegree.setPercentage(newTagDegree.getPercentage());
						existingTagDegree.setSource(newTagDegree.getSource());
					}
					break;
				}
				++i;
			}
			if (!found) {
				// adding
				existingTagDegrees.add(newTagDegree);
			}
		}
		setTags(existingTagDegrees, updateNow);
	}
	public void removeTag(String tagName) {
		Vector<DegreeValue> existingTagDegrees = getSearchRecord().getSourceTagDegreeValues();
		boolean found = false;
		int i = 0;
		while ((i < existingTagDegrees.size()) && !found) {
			DegreeValue existingTagDegree = existingTagDegrees.get(i);
			if (existingTagDegree.getName().equalsIgnoreCase(tagName)) {
				found = true;
				existingTagDegrees.remove(i);
				break;
			}
			++i;
		}
		if (found)
			setTags(existingTagDegrees);
	}
	public void setComputedTags(Vector<DegreeValue> tagDegrees) {
		Vector<DegreeValue> existingDegrees = getSearchRecord().getSourceTagDegreeValues();
		for (int i = 0; i < existingDegrees.size(); ++i) {
			DegreeValue degreeValue = existingDegrees.get(i);
			if (degreeValue.getSource() == DATA_SOURCE_COMPUTED) {
				existingDegrees.remove(i);
				--i;
			}
		}
		for (DegreeValue tagDegree : tagDegrees) {
			boolean found = false;
			int i = 0;
			while ((i < existingDegrees.size()) && !found) {
				DegreeValue existingDegree = existingDegrees.get(i);
				if (existingDegree.getName().equalsIgnoreCase(tagDegree.getName())) {
					found = true;
					if (!((existingDegree.getSource() == DATA_SOURCE_USER) && (tagDegree.getSource() != DATA_SOURCE_USER))) {
						existingDegree.setPercentage(tagDegree.getPercentage());
						existingDegree.setSource(tagDegree.getSource());
					}
				}
				++i;
			}
			if (!found)
				existingDegrees.add(tagDegree);
		}
    	for (int i = 0; i < existingDegrees.size(); ++i) {
    		DegreeValue degree = existingDegrees.get(i);
    		if (degree.getPercentage() < TAG_DISCARD_THRESHOLD) {
    			existingDegrees.remove(i);
    			--i;
    		}
    	}
		setTags(existingDegrees);
	}

	public void setScore(float score) { getSearchRecord().setScore(score); }

	public void setComments(String comments, byte source) {
		getSearchRecord().setComments(comments);
		commentsSource = source;
	}

	public void setThumbnailImage(Image image) {
		images.remove(image);
		images.insertElementAt(image, 0);
		setThumbnailImageFilename(image.getImageFilename(), image.getDataSource());
	}
	public void setThumbnailImageFilename(String thumbnailImageFilename, byte source) {
		getSearchRecord().setThumbnailImageFilename(thumbnailImageFilename);
		thumbnailImageFilenameSource = source;
	}

    public void setUserData(UserData userData) { getSearchRecord().setUserData(userData); }

    public void addMinedProfile(MinedProfile profile) { addMinedProfile(profile, false); }
    public void addMinedProfile(MinedProfile profile, boolean flagForRefresh) {
    	try {
    		getRecord().getWriteLockSem().startRead("addMinedProfile");
    		if (profile != null) {
    			minedProfiles.put(profile, flagForRefresh ? 0 : System.currentTimeMillis());
    			getSearchRecord().addMinedProfileHeader(profile.getHeader(), flagForRefresh);
    			if (fetchedDataSources == null)
    				fetchedDataSources = new HashMap<Byte, Object>();
    			fetchedDataSources.put(profile.getHeader().getDataSource(), null);
    		}
    	} catch (Exception e) {
    		log.error("addMinedProfile(): error", e);
    	} finally {
    		getRecord().getWriteLockSem().endRead();
    	}
    	computeChanges(profile.getHeader().getDataSource());
    }

    public void removeMinedProfile(byte dataSource) {
    	MinedProfile profile = getMinedProfile(dataSource);
		if (profile == null)
			return;
    	try {
    		getRecord().getWriteLockSem().startRead("removeMinedProfile");
    		minedProfiles.remove(profile);
    		ProfileManager.deleteMinedProfile(getIdentifier().getType(), dataSource, getUniqueId());
    		if (fetchedDataSources != null)
    			fetchedDataSources.remove(dataSource);
    		getSearchRecord().addMinedProfileHeader(profile.getHeader(), true);
    	} catch (Exception e) {
    		log.error("removeMinedProfile(): error", e);
    	} finally {
    		getRecord().getWriteLockSem().endRead();
    	}
    	computeChanges(profile.getHeader().getDataSource());
    }

    public void removeMinedDataAfter(long time) {
    	Vector<Byte> removedSources = new Vector<Byte>();
    	for (Entry<MinedProfile, Long> entry : getAllMinedProfiles().entrySet()) {
    		if (entry.getValue() > time) {
    			removedSources.add(entry.getKey().getHeader().getDataSource());
    		}
    	}
    	for (byte removedSource : removedSources)
    		removeMinedProfile(removedSource);
    }

    public void addUserImage(Image image) {
    	if (!userImages.contains(image)) {
    		userImages.add(image);
    		computeImages();
    	}
    }

	public void setExternalItem(boolean isExternalItem) { if (getSearchRecord() != null) getSearchRecord().setExternalItem(isExternalItem); }

	public void setNumPlays(int numPlays) { if (getSearchRecord() != null) getSearchRecord().setPlayCount(numPlays); }
	public void setPlayCount(long playCount) { if (getSearchRecord() != null) getSearchRecord().setPlayCount(playCount); }

	public void setDateAdded(long dateAdded) { if (getSearchRecord() != null) getSearchRecord().setDateAdded(dateAdded); }

	public void addImage(Image image) { addImage(image, !hasThumbnail()); }
	public void addImage(Image image, boolean thumbnail) {
		if ((image == null) || (image.getImageFilename().equalsIgnoreCase(RE3Properties.getProperty("default_thumbnail_image_filename"))))
			return;
		if (!images.contains(image))
			images.add(image);
		if (thumbnail)
			setThumbnailImage(image);
	}
	public int getValidImageCount() {
		int count = 0;
		for (Image image : images) {
			if (!image.isDisabled())
				++count;
		}
		return count;
	}

	// for serialization
	public void setMinedProfiles(Map<MinedProfile, Long> minedProfiles) { this.minedProfiles = minedProfiles; }
	public void setCommentsSource(byte commentsSource) { this.commentsSource = commentsSource; }
	public void setThumbnailImageFilenameSource(byte thumbnailImageFilenameSource) { this.thumbnailImageFilenameSource = thumbnailImageFilenameSource; }
	public void setUserImages(Vector<Image> userImages) { this.userImages = userImages; }
	public void setImages(Vector<Image> images) { this.images = images; }
	public void setLinks(Vector<Link> links) { this.links = links; }
	public void setVideoLinks(Vector<VideoLink> videoLinks) { this.videoLinks = videoLinks; }

    /////////////
    // METHODS //
    /////////////

	@Override
	protected void updateIdentifier(Identifier newId, Identifier oldId) throws AlreadyExistsException {
		super.updateIdentifier(newId, oldId);
		Vector<Byte> minedProfilesToRemove = new Vector<Byte>();
		for (MinedProfile minedProfile : getAllMinedProfiles().keySet())
			minedProfilesToRemove.add(minedProfile.getHeader().getDataSource());
		for (byte dataSource : minedProfilesToRemove)
			removeMinedProfile(dataSource);
		computeChanges(DATA_SOURCE_UNKNOWN);
	}

	@Override
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> relatedRecords = super.mergeWith(profile);
		SearchProfile searchProfile = (SearchProfile)profile;
		// images
		for (Image image : searchProfile.getImages())
			addImage(image);
		return relatedRecords;
	}

	@Override
	public void update(SubmittedProfile submittedProfile, boolean overwrite) {
		SubmittedSearchProfile submittedSearchProfile = (SubmittedSearchProfile)submittedProfile;
		if ((submittedSearchProfile.getRating() != null) && ((getRating().getRatingValue() == 0) || overwrite))
			setRating(submittedSearchProfile.getRating(), DATA_SOURCE_USER);
		if (submittedSearchProfile.getStyleDegreeValues() != null)
			addStyles(submittedSearchProfile.getStyleDegreeValues());
		if (submittedSearchProfile.getTagDegreeValues() != null)
			addTags(submittedSearchProfile.getTagDegreeValues());
		if ((getComments() == null) || getComments().equals("") || getComments().equals("null") || overwrite)
			setComments(submittedSearchProfile.getComments(), DATA_SOURCE_USER);
		if (!hasThumbnail() || overwrite)
			setThumbnailImageFilename(FileUtil.stripWorkingDirectory(submittedSearchProfile.getThumbnailImageFilename()), submittedSearchProfile.getThumbnailImageFilenameSource());
		for (Image image : submittedSearchProfile.getImages())
			addImage(image);
    	if (getDateAdded() == 0)
    		setDateAdded(submittedSearchProfile.getDateAdded());
		if (isExternalItem() && !submittedSearchProfile.isExternalItem()) {
			this.setExternalItem(false);
			this.setDisabled(false);
			setDateAdded(submittedSearchProfile.getDateAdded());
		}
		setPlayCount(Math.max(submittedSearchProfile.getPlayCount(), getPlayCount()));
		for (UserData userData : submittedSearchProfile.getUserData())
			setUserData(userData);
		for (MinedProfile minedProfile : submittedSearchProfile.getMinedProfiles()) {
			if (!hasMinedProfileHeader(minedProfile.getHeader().getDataSource()) || overwrite)
				addMinedProfile(minedProfile, true);
		}
	}

    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("2"); // version

    	writer.writeLine(commentsSource);
    	writer.writeLine(thumbnailImageFilenameSource);

    	writer.writeLine(userImages.size());
    	for (Image userImage : userImages)
    		userImage.write(writer);

    	writer.writeLine(images.size());
    	for (Image image : images)
    		image.write(writer);

    	writer.writeLine(links.size());
    	for (Link link : links)
    		link.write(writer);

    	writer.writeLine(videoLinks.size());
    	for (VideoLink videoLink : videoLinks)
    		videoLink.write(writer);
    }

}
