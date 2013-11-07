package com.mixshare.rapid_evolution.data.search.parameters.search.release;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.ProfileManager;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityDescription;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityVariance;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityVarianceDescription;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class ReleaseSearchParameters extends SearchSearchParameters {

    static private Logger log = Logger.getLogger(ReleaseSearchParameters.class);
    static private final long serialVersionUID = 0L;    		

    ////////////
    // FIELDS //
    ////////////
    
    private String artistSearchText;
    private String titleSearchText;    
    private String labelSearchText;
    private short minYearReleased;
    private short maxYearReleased;
    private long minedDiscogsRecommendationCutoff;    
    private Vector<Map<Integer, Map<Integer, Float>>> artistSimilarityMap;
    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(ReleaseSearchParameters.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("relativeProfile")) {
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
    
	public ReleaseSearchParameters() { }
    
    public ReleaseSearchParameters(ReleaseSearchParameters copy) {
    	super(copy);
    	this.artistSearchText = copy.artistSearchText;
    	this.titleSearchText = copy.titleSearchText;
    	this.labelSearchText = copy.labelSearchText;
    	this.minYearReleased = copy.minYearReleased;
    	this.maxYearReleased = copy.maxYearReleased;
    	this.minedDiscogsRecommendationCutoff = copy.minedDiscogsRecommendationCutoff;
    	this.artistSimilarityMap = copy.artistSimilarityMap;
		// TODO: artist similarity map
    }
    
    public ReleaseSearchParameters(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	artistSearchText = lineReader.getNextLine();
    	titleSearchText = lineReader.getNextLine();
    	labelSearchText = lineReader.getNextLine();
    	minYearReleased = Short.parseShort(lineReader.getNextLine());
    	maxYearReleased = Short.parseShort(lineReader.getNextLine());
    	minedDiscogsRecommendationCutoff = Long.parseLong(lineReader.getNextLine());
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public byte getDataType() { return DATA_TYPE_RELEASES; }
    
    public String getUniqueHash() {
    	StringBuffer result = new StringBuffer();
    	result.append(super.getUniqueHash());
    	// insert class specific parameters here...
    	result.append(queryKeySeperator);
    	result.append(artistSearchText != null ? artistSearchText : "");
    	result.append(queryKeySeperator);
    	result.append(titleSearchText != null ? titleSearchText : "");
    	result.append(queryKeySeperator);
    	result.append(labelSearchText != null ? labelSearchText : "");
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(minYearReleased));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(maxYearReleased));
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(minedDiscogsRecommendationCutoff));
    	return result.toString();
    }   

	public String getArtistSearchText() { return artistSearchText; }
	public String getTitleSearchText() { return titleSearchText; }
	public String getLabelSearchText() { return labelSearchText; }	
	public short getMinYearReleased() { return minYearReleased; }
	public short getMaxYearReleased() { return maxYearReleased; }
    public long getMinedDiscogsRecommendationCutoff() { return minedDiscogsRecommendationCutoff; }
	public Vector<Map<Integer, Map<Integer, Float>>> getArtistSimilarityMap() { return artistSimilarityMap; }
    
    public Vector<SearchProfile> fetchRelativeProfiles() {
    	if (relativeProfiles == null) {
	    	Vector<SearchProfile> results = super.fetchRelativeProfiles();
	    	if ((results != null) && (results.size() > 0)) {
	    		int i = 0;
	    		for (SearchProfile result : results) {
	    			ReleaseProfile release = ((ReleaseProfile)result);
	        		if ((release.getArtistSimilarityMap() == null) && (artistSimilarityMap != null))
	        			release.setArtistSimilarityMap(artistSimilarityMap.get(i));
	        		++i;
	    		}
	    	}
	    	return results;
    	}
    	return relativeProfiles;
    }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setArtistSearchText(String artistSearchText) { this.artistSearchText = artistSearchText;		 }
	public void setTitleSearchText(String titleSearchText) { this.titleSearchText = titleSearchText; }
	public void setLabelSearchText(String labelSearchText) { this.labelSearchText = labelSearchText; 		 }
	public void setMinYearReleased(short minYearReleased) { this.minYearReleased = minYearReleased; }
	public void setMaxYearReleased(short maxYearReleased) { this.maxYearReleased = maxYearReleased; }   
	public void setMinedDiscogsRecommendationCutoff(long minedDiscogsRecommendationCutoff) { this.minedDiscogsRecommendationCutoff = minedDiscogsRecommendationCutoff; }
	public void setArtistSimilarityMap(Vector<Map<Integer, Map<Integer, Float>>> artistSimilarityMap) { this.artistSimilarityMap = artistSimilarityMap; }
    
	public void initRelativeProfile(SearchProfile relativeProfile) {
		super.initRelativeProfile(relativeProfile);
		ReleaseProfile relativeRelease = (ReleaseProfile)relativeProfile;
		artistSimilarityMap = new Vector<Map<Integer, Map<Integer, Float>>>(1);
		artistSimilarityMap.add(ReleaseProfile.computeArtistSimilarityMap(relativeRelease));
	}

	public void initRelativeProfiles(Vector<SearchProfile> relativeProfiles) {
		super.initRelativeProfiles(relativeProfiles);
		artistSimilarityMap = new Vector<Map<Integer, Map<Integer, Float>>>(relativeProfiles.size());
		for (SearchProfile searchProfile : relativeProfiles) {				
			ReleaseProfile relativeRelease = (ReleaseProfile)searchProfile;
			artistSimilarityMap.add(ReleaseProfile.computeArtistSimilarityMap(relativeRelease));
		}
	}	
	
	/////////////
	// METHODS //
	/////////////
	
	public float matchesSub(Record record, boolean fullCheck) {
		float superScore = super.matchesSub(record, fullCheck);
		if (superScore > 0.0f) {			
			ReleaseRecord releaseRecord = (ReleaseRecord)record;
			if ((getMinBeatIntensity() != null) && (getMinBeatIntensity().getBeatIntensityValue() > 0)) {
				if (!releaseRecord.getAvgBeatIntensity().isValid())
					return 0.0f;				
				if (releaseRecord.getAvgBeatIntensity().getBeatIntensityValue() <= getMinBeatIntensity().getBeatIntensityValue())
					return 0.0f;				
			}
			if ((getMaxBeatIntensity() != null) && (getMaxBeatIntensity().getBeatIntensityValue() < 100)) {
				if (!releaseRecord.getAvgBeatIntensity().isValid())
					return 0.0f;				
				if (releaseRecord.getAvgBeatIntensity().getBeatIntensityValue() > getMaxBeatIntensity().getBeatIntensityValue())
					return 0.0f;				
			}
			if (getMinYearReleased() != (short)0) {
				if (releaseRecord.getOriginalYearReleased() == (short)0)
					return 0.0f;
				if (releaseRecord.getOriginalYearReleased() < getMinYearReleased())
					return 0.0f;				
			}
			if (getMaxYearReleased() != (short)0) {
				if (releaseRecord.getOriginalYearReleased() == (short)0)
					return 0.0f;
				if (releaseRecord.getOriginalYearReleased() > getMaxYearReleased())
					return 0.0f;				
			}			
			if (fullCheck) {
				if ((getTitleSearchText() != null) && (getTitleSearchText().length() > 0)) {
					float match = getSearchFieldsMatch(record, new String[] { "title" }, getTitleSearchText());
					if (match == 0.0f)
						return 0.0f;
					superScore += match;
				}				
				if ((getArtistSearchText() != null) && (getArtistSearchText().length() > 0)) {
					float match = getSearchFieldsMatch(record, new String[] { "artist" }, getArtistSearchText());
					if (match == 0.0f)
						return 0.0f;
					superScore += match;
				}					
				if ((getLabelSearchText() != null) && (getLabelSearchText().length() > 0)) {
					float match = getSearchFieldsMatch(record, new String[] { "label" }, getLabelSearchText());
					if (match == 0.0f)
						return 0.0f;
					superScore += match;
				}
			}
			if (minedDiscogsRecommendationCutoff != 0) {
				if (releaseRecord.hasMinedProfileHeader(DATA_SOURCE_DISCOGS)) {
					// TODO: temporary hack for now, shouldn't be calling ProfileManager directly, but can't use Database. calls in HBase mode ...
					ReleaseProfile releaseProfile = (ReleaseProfile)ProfileManager.getProfile(releaseRecord.getIdentifier(), releaseRecord.getUniqueId());
					if (releaseProfile != null) {
						DiscogsReleaseProfile discogsReleaseProfile = (DiscogsReleaseProfile)releaseProfile.getMinedProfile(DATA_SOURCE_DISCOGS);
						if (discogsReleaseProfile != null) {
							long lastFetched = discogsReleaseProfile.getLastFetchedRecommendedReleases();
							if (lastFetched > minedDiscogsRecommendationCutoff)
								return 0.0f;
						} else {
							return 0.0f;
						}
					} else {
						return 0.0f;
					}
				} else {
					// only want to return ones who have a discogs profile
					return 0.0f;
				}
			}

			return superScore;
		} else {
			return 0.0f;
		}
	}	

	public boolean isEmpty(boolean countIndexed) {
		if (!super.isEmpty(countIndexed))
			return false;
		if (countIndexed) {
			if ((artistSearchText != null) && (artistSearchText.length() > 0))
				return false;
			if ((labelSearchText != null) && (labelSearchText.length() > 0))
				return false;		
			if ((titleSearchText != null) && (titleSearchText.length() > 0))
				return false;
		}
		if (minYearReleased != (short)0)
			return false;
		if (maxYearReleased != (short)0)
			return false;
		if (minedDiscogsRecommendationCutoff != 0)
			return false;
		return true;
	}	

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); //version		
		if (artistSearchText != null)
			writer.writeLine(artistSearchText);
		else
			writer.writeLine("");
		if (titleSearchText != null)
			writer.writeLine(titleSearchText);
		else
			writer.writeLine("");
		if (labelSearchText != null)
			writer.writeLine(labelSearchText);
		else
			writer.writeLine("");
		writer.writeLine(minYearReleased);		
		writer.writeLine(maxYearReleased);		
		writer.writeLine(minedDiscogsRecommendationCutoff);		
		// TODO: artist similarity map
	}	
	
	protected int compareSub(SearchResult r1, SearchResult r2, byte sortType) {
		ReleaseRecord s1 = (ReleaseRecord)r1.getRecord();
		ReleaseRecord s2 = (ReleaseRecord)r2.getRecord();
		if (sortType == SORT_BY_YEAR) {
			short y1 = s1.getOriginalYearReleased();
			short y2 = s2.getOriginalYearReleased();
			if ((y1 > 0) && (y2 > 0)) {
				if (y1 > y2)
					return -1;
				if (y2 > y1)
					return 1;
				return 0;
			}
			if (y1 > 0)
				return -1;
			if (y2 > 0)
				return 1;
			return 0;
		}
		if (sortType == SORT_BY_BEAT_INTENSITY_DESCRIPTION) {
			BeatIntensityDescription bi1 = s1.getAvgBeatIntensityDescription();
			BeatIntensityDescription bi2 = s2.getAvgBeatIntensityDescription();
			return bi1.compareTo(bi2);
		}
		if (sortType == SORT_BY_BEAT_INTENSITY_VALUE) {
			BeatIntensity bi1 = s1.getAvgBeatIntensity();
			BeatIntensity bi2 = s2.getAvgBeatIntensity();
			return bi1.compareTo(bi2);
		}		
		if (sortType == SORT_BY_BEAT_INTENSITY_VARIANCE_VALUE) {
			BeatIntensityVariance bi1 = s1.getBeatIntensityVariance();
			BeatIntensityVariance bi2 = s2.getBeatIntensityVariance();
			return bi1.compareTo(bi2);
		}
		if (sortType == SORT_BY_BEAT_INTENSITY_VARIANCE_DESCRIPTION) {
			BeatIntensityVarianceDescription bi1 = s1.getBeatIntensityVarianceDescription();
			BeatIntensityVarianceDescription bi2 = s2.getBeatIntensityVarianceDescription();
			return bi1.compareTo(bi2);
		}
		if (sortType == SORT_BY_COMPILATION) {
			String c1 = String.valueOf(s1.isCompilationRelease());
			String c2 = String.valueOf(s2.isCompilationRelease());
			return c1.compareTo(c2);
		}
		if (sortType == SORT_BY_ARTIST_DESCRIPTION) {
			String c1 = s1.getArtistsDescription();
			String c2 = s2.getArtistsDescription();
			return SmartString.compareStrings(c1, c2);			
		}		
		if (sortType == SORT_BY_LABEL_DESCRIPTION) {
			String c1 = s1.getLabelsDescription();
			String c2 = s2.getLabelsDescription();
			return SmartString.compareStrings(c1, c2);			
		}		
		if (sortType == SORT_BY_NUM_SONGS) {
			int a1 = s1.getNumSongs();
			int a2 = s2.getNumSongs();
			if (a1 > a2)
				return -1;
			if (a1 < a2)
				return 1;
			return 0;
		}
		if (sortType == SORT_BY_RELEASE_TITLE) {
			String c1 = s1.getReleaseTitle();
			String c2 = s2.getReleaseTitle();
			return SmartString.compareStrings(c1, c2);			
		}
		return super.compareSub(r1, r2, sortType);
	}	

	public Index getIndex() { return Database.getReleaseIndex(); }
	
	public void addSearchFields(Vector<String> searchFields) {
		super.addSearchFields(searchFields);
		searchFields.add("artist");
		searchFields.add("title");
		searchFields.add("label");
		searchFields.add("year");
	}		
	
}
