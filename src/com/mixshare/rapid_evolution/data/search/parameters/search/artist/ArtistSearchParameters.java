package com.mixshare.rapid_evolution.data.search.parameters.search.artist;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.artist.LastfmArtistProfile;
import com.mixshare.rapid_evolution.data.profile.ProfileManager;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.search.SearchSearchParameters;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityDescription;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityVariance;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensityVarianceDescription;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class ArtistSearchParameters extends SearchSearchParameters {

    static private Logger log = Logger.getLogger(ArtistSearchParameters.class);
    static private final long serialVersionUID = 0L;    		

    ////////////
    // FIELDS //
    ////////////
    
    private String artistSearchText;
    private String labelSearchText;    
    private long minedDiscogsReleasesCutoff;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public ArtistSearchParameters() { }
    public ArtistSearchParameters(ArtistSearchParameters copy) {
    	super(copy);
    	this.artistSearchText = copy.artistSearchText;
    	this.labelSearchText = copy.labelSearchText;
    	this.minedDiscogsReleasesCutoff = copy.minedDiscogsReleasesCutoff;
    }
    public ArtistSearchParameters(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	artistSearchText = lineReader.getNextLine();
    	labelSearchText = lineReader.getNextLine();
    	minedDiscogsReleasesCutoff = Long.parseLong(lineReader.getNextLine());		
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public byte getDataType() { return DATA_TYPE_ARTISTS; }
    
    public String getUniqueHash() {
    	StringBuffer result = new StringBuffer();
    	result.append(super.getUniqueHash());
    	// insert class specific parameters here...
    	result.append(queryKeySeperator);
    	result.append(artistSearchText != null ? artistSearchText : "");
    	result.append(queryKeySeperator);
    	result.append(labelSearchText != null ? labelSearchText : "");
    	result.append(queryKeySeperator);
    	result.append(String.valueOf(minedDiscogsReleasesCutoff));
    	return result.toString();
    }   
        
    public String getArtistSearchText() { return artistSearchText; }
	public String getLabelSearchText() { return labelSearchText; }
    public long getMinedDiscogsReleasesCutoff() { return minedDiscogsReleasesCutoff; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setArtistSearchText(String artistSearchText) { this.artistSearchText = artistSearchText; }
	public void setLabelSearchText(String labelSearchText) { this.labelSearchText = labelSearchText; }
	public void setMinedDiscogsReleasesCutoff(long minedDiscogsReleasesCutoff) { this.minedDiscogsReleasesCutoff = minedDiscogsReleasesCutoff; }
	
	/////////////
	// METHODS //
	/////////////
	
	public float matchesSub(Record record, boolean fullCheck) {
		float superScore = super.matchesSub(record, fullCheck);
		if (superScore > 0.0f) {
			ArtistRecord artist = (ArtistRecord)record;
			if ((getMinBeatIntensity() != null) && (getMinBeatIntensity().getBeatIntensityValue() > 0)) {
				if (!artist.getAvgBeatIntensity().isValid())
					return 0.0f;
				if (artist.getAvgBeatIntensity().getBeatIntensityValue() <= getMinBeatIntensity().getBeatIntensityValue())
					return 0.0f;				
			}
			if ((getMaxBeatIntensity() != null) && (getMaxBeatIntensity().getBeatIntensityValue() < 100)) {
				if (!artist.getAvgBeatIntensity().isValid())
					return 0.0f;
				if (artist.getAvgBeatIntensity().getBeatIntensityValue() > getMaxBeatIntensity().getBeatIntensityValue())
					return 0.0f;				
			}			
			if (fullCheck) {
				if ((getArtistSearchText() != null) && (getArtistSearchText().length() > 0)) {
					float match = getSearchFieldsMatch(record, new String[] { "name" }, getArtistSearchText());
					if (match == 0.0f)
						return 0.0f;
					superScore += match;
				}
				if ((getLabelSearchText() != null) && (getLabelSearchText().length() > 0)) {
					float match = getSearchFieldsMatch(record, new String[] { "labels" }, getLabelSearchText());
					if (match == 0.0f)
						return 0.0f;
					superScore += match;
				}							
			}						
			if (minedDiscogsReleasesCutoff != 0) {
				if (artist.hasMinedProfileHeader(DATA_SOURCE_DISCOGS)) {					
					// TODO: temporary hack for now, shouldn't be calling ProfileManager directly, but can't use Database. calls in HBase mode ...
					ArtistProfile artistProfile = (ArtistProfile)ProfileManager.getProfile(artist.getIdentifier(), artist.getUniqueId());
					if (artistProfile != null) {
						DiscogsArtistProfile discogsArtistProfile = (DiscogsArtistProfile)artistProfile.getMinedProfile(DATA_SOURCE_DISCOGS);
						if (discogsArtistProfile != null) {
							long lastFetched = discogsArtistProfile.getLastFetchedReleaseIds();
							if (lastFetched > minedDiscogsReleasesCutoff)
								return 0.0f;
						} else {
							return 0.0f;
						}
					} else {
						return 0.0f;
					}
				} else {
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
		}
		if (minedDiscogsReleasesCutoff != 0)
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
		if (labelSearchText != null)
			writer.writeLine(labelSearchText);
		else
			writer.writeLine("");
		writer.writeLine(minedDiscogsReleasesCutoff);		
	}	

	protected int compareSub(SearchResult r1, SearchResult r2, byte sortType) {
		ArtistRecord s1 = (ArtistRecord)r1.getRecord();
		ArtistRecord s2 = (ArtistRecord)r2.getRecord();
		if (sortType == SORT_BY_LASTFM_REACH) {
			ArtistProfile a1 = Database.getArtistIndex().getArtistProfile(s1.getUniqueId());
			ArtistProfile a2 = Database.getArtistIndex().getArtistProfile(s2.getUniqueId());
			float v1 = 0.0f;
			float v2 = 0.0f;
			if (a1 != null) {
				LastfmArtistProfile lastfmProfile = (LastfmArtistProfile)a1.getMinedProfile(DATA_SOURCE_LASTFM);
				if (lastfmProfile != null)
					v1 = lastfmProfile.getNumListeners();
			}
			if (a2 != null) {
				LastfmArtistProfile lastfmProfile = (LastfmArtistProfile)a2.getMinedProfile(DATA_SOURCE_LASTFM);
				if (lastfmProfile != null)
					v2 = lastfmProfile.getNumListeners();				
			}
			if (v1 > v2)
				return -1;
			if (v1 < v2)
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
		if (sortType == SORT_BY_NUM_RELEASES) {
			int a1 = s1.getNumReleases();
			int a2 = s2.getNumReleases();
			if (a1 > a2)
				return -1;
			if (a1 < a2)
				return 1;
			return 0;
		}		
		return super.compareSub(r1, r2, sortType);
	}
	
	public Index getIndex() { return Database.getArtistIndex(); }
	
	public void addSearchFields(Vector<String> searchFields) {
		super.addSearchFields(searchFields);
		searchFields.add("name");
	}		
	
}
