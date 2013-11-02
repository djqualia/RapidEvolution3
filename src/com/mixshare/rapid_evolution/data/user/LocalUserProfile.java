package com.mixshare.rapid_evolution.data.user;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.DegreeValueSetAverager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.workflow.Task;

public class LocalUserProfile extends UserProfile {

	static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(LocalUserProfile.class);	

    ////////////
    // FIELDS //
    ////////////
    
    private int numArtistsInCollection = 0;
    private int numLabelsInCollection = 0;
    private int numReleasesInCollection = 0;    
    private int numSongsInCollection = 0;    
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public LocalUserProfile() {
    	super();
    }
    public LocalUserProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	numArtistsInCollection = Integer.parseInt(lineReader.getNextLine());
    	numLabelsInCollection = Integer.parseInt(lineReader.getNextLine());
    	numReleasesInCollection = Integer.parseInt(lineReader.getNextLine());
    	numSongsInCollection = Integer.parseInt(lineReader.getNextLine());
    }
    
    /////////////
    // METHODS //
    /////////////
    
    public String toString() {
    	StringBuffer result = new StringBuffer();
    	
    	result.append("\n# artists=");
    	result.append(numArtistsInCollection);
    	result.append("\navg. artist rating=");
    	result.append(avgArtistRating);
    	result.append("\n# artist ratings=");
    	result.append(numRatedArtists);

    	result.append("\n# labels=");
    	result.append(numLabelsInCollection);
    	result.append("\navg. label rating=");
    	result.append(avgLabelRating);
    	result.append("\n# label ratings=");
    	result.append(numRatedLabels);
    	
    	result.append("\n# releases=");
    	result.append(numReleasesInCollection);
    	result.append("\navg. release rating=");
    	result.append(avgReleaseRating);
    	result.append("\n# release ratings=");
    	result.append(numRatedReleases);
    	
    	result.append("\n# songs=");
    	result.append(numSongsInCollection);
    	result.append("\navg. song rating=");
    	result.append(avgSongRating);
    	result.append("\n# song ratings=");
    	result.append(numRatedSongs);

    	result.append("\nstyle preferences=");
    	result.append(stylePreferences.toString(Database.getStyleIndex()));
    	result.append("\ntag preferences=");
    	result.append(tagPreferences.toString(Database.getTagIndex()));

    	result.append("\nartist preferences=");
    	result.append(artistPreferences.toString(Database.getArtistIndex()));
    	result.append("\nlabel preferences=");
    	result.append(labelPreferences.toString(Database.getLabelIndex()));
    	result.append("\nrelease preferences=");
    	result.append(releasePreferences.toString(Database.getReleaseIndex()));
    	result.append("\nsong preferences=");
    	result.append(songPreferences.toString(Database.getSongIndex()));
    	
    	return result.toString();
    }   
    
    
    public String getProfileId() { return "Local User"; }
    public void computeProfile(Task task) {
    	try {
    		if (RE3Properties.getBoolean("skip_user_profile_computation"))
    			return;			    		
    		
    		if (log.isTraceEnabled())
    			log.trace("computeProfile(): starting...");

    		// artist avgs
    		numArtistsInCollection = Database.getArtistIndex().getSizeInternalItems();
    		float avgRating = 0.0f;
    		int numRatings = 0;
    		for (int artistId : Database.getArtistIndex().getIds()) {
    			ArtistRecord artistRecord = Database.getArtistIndex().getArtistRecord(artistId);
    			if ((artistRecord != null) && !artistRecord.isExternalItem() && artistRecord.getRatingValue().isValid()) {
					++numRatings;
					avgRating += artistRecord.getRatingValue().getRatingStarsFloat();
    			}
    		}
    		if (numRatings > 0) {
    			avgArtistRating = avgRating / numRatings;
    			numRatedArtists = numRatings;
    		}
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;

    		// label avgs
    		numLabelsInCollection = Database.getLabelIndex().getSizeInternalItems();
    		avgRating = 0.0f;
    		numRatings = 0;
    		for (int labelId : Database.getLabelIndex().getIds()) {
    			LabelRecord labelRecord = Database.getLabelIndex().getLabelRecord(labelId);
    			if ((labelRecord != null) && !labelRecord.isExternalItem() && labelRecord.getRatingValue().isValid()) {
					++numRatings;
					avgRating += labelRecord.getRatingValue().getRatingStarsFloat();
    			}
    		}
    		if (numRatings > 0) {
    			avgLabelRating = avgRating / numRatings;
    			numRatedLabels = numRatings;
    		}
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;

    		// release avgs
    		numReleasesInCollection = Database.getReleaseIndex().getSizeInternalItems();
    		avgRating = 0.0f;
    		numRatings = 0;
    		for (int releaseId : Database.getReleaseIndex().getIds()) {
    			ReleaseRecord releaseRecord = Database.getReleaseIndex().getReleaseRecord(releaseId);
    			if ((releaseRecord != null) && !releaseRecord.isExternalItem() && releaseRecord.getRatingValue().isValid()) {
					++numRatings;
					avgRating += releaseRecord.getRatingValue().getRatingStarsFloat();
    			}
    		}
    		if (numRatings > 0) {
    			avgReleaseRating = avgRating / numRatings;
    			numRatedReleases = numRatings;
    		}
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		
    		// song avgs
    		numSongsInCollection = Database.getSongIndex().getSizeInternalItems();
    		avgRating = 0.0f;
    		numRatings = 0;
    		for (int songId : Database.getSongIndex().getIds()) {
    			SongRecord songRecord = Database.getSongIndex().getSongRecord(songId);
    			if ((songRecord != null) && !songRecord.isExternalItem() && songRecord.getRatingValue().isValid()) {
					++numRatings;
					avgRating += songRecord.getRatingValue().getRatingStarsFloat();
    			}
    		}
    		if (numRatings > 0) {
    			avgSongRating = avgRating / numRatings;
    			numRatedSongs = numRatings;
    		}
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;

    		if (log.isTraceEnabled())
    			log.trace("computeProfile(): computing user style preferences");
    		PreferenceMap songStyleRatingPrefs = computeStylePreferencesFromRatings(Database.getSongIndex(), avgSongRating, task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		PreferenceMap songStyleFrequencyPrefs = computeStylePreferencesFromFrequency(Database.getSongIndex(), task); 
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		PreferenceMap artistStyleRatingPrefs = computeStylePreferencesFromRatings(Database.getArtistIndex(), avgArtistRating, task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		PreferenceMap artistStyleFrequencyPrefs = computeStylePreferencesFromFrequency(Database.getArtistIndex(), task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		
    		if (log.isTraceEnabled())
    			log.trace("computeProfile(): computing user tag preferences");
    		PreferenceMap songTagRatingPrefs = computeTagPreferencesFromRatings(Database.getSongIndex(), avgSongRating, task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		PreferenceMap songTagFrequencyPrefs = computeTagPreferencesFromFrequency(Database.getSongIndex(), task); 
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		PreferenceMap artistTagRatingPrefs = computeTagPreferencesFromRatings(Database.getArtistIndex(), avgArtistRating, task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		PreferenceMap artistTagFrequencyPrefs = computeTagPreferencesFromFrequency(Database.getArtistIndex(), task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		    		
    		if (log.isTraceEnabled())
    			log.trace("computeProfile(): computing user artist preferences");
    		PreferenceMap artistRatingPrefs = computeSearchPreferencesFromRatings(Database.getArtistIndex(), avgArtistRating, task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		PreferenceMap artistFrequencyPrefs = computeSearchPreferencesFromFrequency(Database.getArtistIndex(), task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		
    		if (log.isTraceEnabled())
    			log.trace("computeProfile(): computing user label preferences");
    		PreferenceMap labelRatingPrefs = computeSearchPreferencesFromRatings(Database.getLabelIndex(), avgLabelRating, task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		PreferenceMap labelFrequencyPrefs = computeSearchPreferencesFromFrequency(Database.getLabelIndex(), task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		
    		if (log.isTraceEnabled())
    			log.trace("computeProfile(): computing user release preferences");
    		PreferenceMap releaseRatingPrefs = computeSearchPreferencesFromRatings(Database.getReleaseIndex(), avgReleaseRating, task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		PreferenceMap releaseFrequencyPrefs = computeSearchPreferencesFromFrequency(Database.getReleaseIndex(), task);
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		
    		if (log.isTraceEnabled())
    			log.trace("computeProfile(): computing user song preferences");
    		PreferenceMap songRatingPrefs = computeSearchPreferencesFromRatings(Database.getSongIndex(), avgSongRating, task);
    		if (log.isTraceEnabled()) {
    			PreferenceMap tempMap = new PreferenceMap();
    			Vector<DegreeValue> degrees = songRatingPrefs.getPreferenceDegrees();
    			java.util.Collections.sort(degrees);
    			tempMap.setPreferenceDegrees(degrees);
    			if (log.isTraceEnabled())
    				log.trace("computeProfile(): songRatingPrefs=" + tempMap.toString(Database.getSongIndex()));
    		}
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		
    		PreferenceMap songFrequencyPrefs = computeSearchPreferencesFromFrequency(Database.getSongIndex(), task);
    		if (log.isTraceEnabled()) {
    			PreferenceMap tempMap = new PreferenceMap();
    			Vector<DegreeValue> degrees = songFrequencyPrefs.getPreferenceDegrees();
    			java.util.Collections.sort(degrees);
    			tempMap.setPreferenceDegrees(degrees);    		
    			if (log.isTraceEnabled())
    				log.trace("computeProfile(): songFrequencyPrefs=" + tempMap.toString(Database.getSongIndex()));
    		}
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
    		    		
    		if (log.isTraceEnabled())
    			log.trace("computeProfile(): normalizing results");
    		
			float ratingsWeight = RE3Properties.getFloat("user_preferences_ratings_weight");
			float frequencyWeight = RE3Properties.getFloat("user_preferences_frequency_weight");
    		
			float artistRatingsWeight = 1.0f;
			float labelRatingWeight = 1.0f;
			float releaseRatingWeight = 1.0f;
			float songRatingsWeight = 1.0f;

			int numRatingsAlpha = RE3Properties.getInt("user_preference_num_ratings_alpha");			
			if (numRatedArtists < numRatingsAlpha)
				artistRatingsWeight *= ((float)numRatedArtists) / numRatingsAlpha;
			if (numRatedLabels < numRatingsAlpha)
				labelRatingWeight *= ((float)numRatedLabels) / numRatingsAlpha;
			if (numRatedReleases < numRatingsAlpha)
				releaseRatingWeight *= ((float)numRatedReleases) / numRatingsAlpha;
			if (numRatedSongs < numRatingsAlpha)
				songRatingsWeight *= ((float)numRatedSongs) / numRatingsAlpha;						
			
			DegreeValueSetAverager styleDegrees = new DegreeValueSetAverager();					
			styleDegrees.addDegreeValueSet(songStyleRatingPrefs.getPreferenceDegrees(), songRatingsWeight * ratingsWeight);
			styleDegrees.addDegreeValueSet(artistStyleRatingPrefs.getPreferenceDegrees(), artistRatingsWeight * ratingsWeight);
			styleDegrees.addDegreeValueSet(songStyleFrequencyPrefs.getPreferenceDegrees(), frequencyWeight);
			styleDegrees.addDegreeValueSet(artistStyleFrequencyPrefs.getPreferenceDegrees(), frequencyWeight);
			
			DegreeValueSetAverager tagDegrees = new DegreeValueSetAverager();
			tagDegrees.addDegreeValueSet(songTagRatingPrefs.getPreferenceDegrees(), songRatingsWeight * ratingsWeight);
			tagDegrees.addDegreeValueSet(artistTagRatingPrefs.getPreferenceDegrees(), artistRatingsWeight * ratingsWeight);
			tagDegrees.addDegreeValueSet(songTagFrequencyPrefs.getPreferenceDegrees(), frequencyWeight);
			tagDegrees.addDegreeValueSet(artistTagFrequencyPrefs.getPreferenceDegrees(), frequencyWeight);
			
			DegreeValueSetAverager artistDegrees = new DegreeValueSetAverager();
			artistDegrees.addDegreeValueSet(artistRatingPrefs.getPreferenceDegrees(), artistRatingsWeight * ratingsWeight);
			artistDegrees.addDegreeValueSet(artistFrequencyPrefs.getPreferenceDegrees(), frequencyWeight);
			
			DegreeValueSetAverager labelDegrees = new DegreeValueSetAverager();
			labelDegrees.addDegreeValueSet(labelRatingPrefs.getPreferenceDegrees(), labelRatingWeight * ratingsWeight);
			labelDegrees.addDegreeValueSet(labelFrequencyPrefs.getPreferenceDegrees(), frequencyWeight);
			
			DegreeValueSetAverager releaseDegrees = new DegreeValueSetAverager();
			releaseDegrees.addDegreeValueSet(releaseRatingPrefs.getPreferenceDegrees(), releaseRatingWeight * ratingsWeight);
			releaseDegrees.addDegreeValueSet(releaseFrequencyPrefs.getPreferenceDegrees(), frequencyWeight);
			
			DegreeValueSetAverager songDegrees = new DegreeValueSetAverager();
			songDegrees.addDegreeValueSet(songRatingPrefs.getPreferenceDegrees(), songRatingsWeight * ratingsWeight);
			songDegrees.addDegreeValueSet(songFrequencyPrefs.getPreferenceDegrees(), frequencyWeight);
			
			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
			
			Vector<DegreeValue> finalStyleDegrees = styleDegrees.getDegrees();
			Vector<DegreeValue> finalTagDegrees = tagDegrees.getDegrees();

			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
			
			Vector<DegreeValue> finalArtistDegrees = artistDegrees.getDegrees();
			Vector<DegreeValue> finalLabelDegrees = labelDegrees.getDegrees();
			Vector<DegreeValue> finalReleaseDegrees = releaseDegrees.getDegrees();
			Vector<DegreeValue> finalSongDegrees = songDegrees.getDegrees();

			if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled()))
				return;
			
			stylePreferences = new PreferenceMap();
			stylePreferences.setPreferenceDegrees(finalStyleDegrees);
			stylePreferences.normalize();
			
			tagPreferences = new PreferenceMap();
			tagPreferences.setPreferenceDegrees(finalTagDegrees);
			tagPreferences.normalize();
			
			artistPreferences = new PreferenceMap();
			artistPreferences.setPreferenceDegrees(finalArtistDegrees);
			artistPreferences.normalize();

			labelPreferences = new PreferenceMap();
			labelPreferences.setPreferenceDegrees(finalLabelDegrees);
			labelPreferences.normalize();
			
			releasePreferences = new PreferenceMap();
			releasePreferences.setPreferenceDegrees(finalReleaseDegrees);
			releasePreferences.normalize();
			
			songPreferences = new PreferenceMap();
			songPreferences.setPreferenceDegrees(finalSongDegrees);
			songPreferences.normalize();			
			
    	} catch (Exception e) {
    		log.error("computeProfile(): error", e);
    	}
		if (log.isTraceEnabled())
			log.trace("computeProfile(): result=" + this);    	
    }

    private Vector<RatedSearchRecord> getRatedSearchRecords(SearchIndex index) {
    	Vector<RatedSearchRecord> result = new Vector<RatedSearchRecord>();
		for (int searchId : index.getIds()) {
			SearchRecord searchRecord = index.getSearchRecord(searchId);
			if ((searchRecord != null) && !searchRecord.isExternalItem() && searchRecord.getRatingValue().isValid())
				result.add(new RatedSearchRecord(searchRecord, searchRecord.getRatingValue()));
		}
		return result;
    }
    
    public PreferenceMap computeStylePreferencesFromRatings(SearchIndex index, float avgRating, Task task) {    	
		return UserProfileUtil.computeStylePreferencesFromRatings(getRatedSearchRecords(index), avgRating, task);
    }
        
    public PreferenceMap computeTagPreferencesFromRatings(SearchIndex index, float avgRating, Task task) {
    	return UserProfileUtil.computeTagPreferencesFromRatings(getRatedSearchRecords(index), avgRating, task);
    }    

    public PreferenceMap computeSearchPreferencesFromRatings(SearchIndex index, float avgRating, Task task) {
    	return UserProfileUtil.computeSearchPreferencesFromRatings(getRatedSearchRecords(index), avgRating, task);
    }
    
    private Vector<WeightedSearchRecord> getWeightedSearchRecords(SearchIndex index) {
    	Vector<WeightedSearchRecord> result = new Vector<WeightedSearchRecord>();
		for (int searchId : index.getIds()) {
			SearchRecord searchRecord = index.getSearchRecord(searchId);
			if ((searchRecord != null) && !searchRecord.isExternalItem())
				result.add(new WeightedSearchRecord(searchRecord, 1.0f + searchRecord.getPlayCount()));
		}
		return result;
    }
    
    public PreferenceMap computeStylePreferencesFromFrequency(SearchIndex index, Task task) {
    	return UserProfileUtil.computeStylePreferencesFromFrequency(getWeightedSearchRecords(index), task);
    }   
    
    public PreferenceMap computeTagPreferencesFromFrequency(SearchIndex index, Task task) {
    	return UserProfileUtil.computeTagPreferencesFromFrequency(getWeightedSearchRecords(index), task);
    }       

    public PreferenceMap computeSearchPreferencesFromFrequency(SearchIndex index, Task task) {
    	return UserProfileUtil.computeSearchPreferencesFromFrequency(getWeightedSearchRecords(index), task);
    }

    // for serialization
    public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine(1); //version
    	writer.writeLine(numArtistsInCollection);
    	writer.writeLine(numLabelsInCollection);
    	writer.writeLine(numReleasesInCollection);
    	writer.writeLine(numSongsInCollection);
    }    
    
    ///////////////////////
    // FOR SERIALIZATOIN //
    ///////////////////////
    
	public int getNumArtistsInCollection() {
		return numArtistsInCollection;
	}

	public void setNumArtistsInCollection(int numArtistsInCollection) {
		this.numArtistsInCollection = numArtistsInCollection;
	}

	public int getNumLabelsInCollection() {
		return numLabelsInCollection;
	}

	public void setNumLabelsInCollection(int numLabelsInCollection) {
		this.numLabelsInCollection = numLabelsInCollection;
	}

	public int getNumReleasesInCollection() {
		return numReleasesInCollection;
	}

	public void setNumReleasesInCollection(int numReleasesInCollection) {
		this.numReleasesInCollection = numReleasesInCollection;
	}

	public int getNumSongsInCollection() {
		return numSongsInCollection;
	}

	public void setNumSongsInCollection(int numSongsInCollection) {
		this.numSongsInCollection = numSongsInCollection;
	}


    
    
}
