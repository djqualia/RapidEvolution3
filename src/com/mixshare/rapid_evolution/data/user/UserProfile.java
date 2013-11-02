package com.mixshare.rapid_evolution.data.user;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.workflow.Task;

abstract public class UserProfile implements Serializable, DataConstants {

	static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(UserProfile.class);

    static private float STYLE_PREFERENCE_WEIGHT;
    static private float TAG_PREFERENCE_WEIGHT;
    static private float ARTIST_PREFERENCE_WEIGHT;
    static private float LABEL_PREFERENCE_WEIGHT;
    static private float RELEASE_PREFERENCE_WEIGHT;
    static private float SONG_PREFERENCE_WEIGHT;
    static private boolean NORMALIZE_STYLE_PREFERENCES;
    static private boolean NORMALIZE_TAG_PREFERENCES;

    static {
    	initProperties();
    }

    static public void initProperties() {
        STYLE_PREFERENCE_WEIGHT = RE3Properties.getFloat("user_profile_style_preference_weight");
        TAG_PREFERENCE_WEIGHT = RE3Properties.getFloat("user_profile_tag_preference_weight");
        ARTIST_PREFERENCE_WEIGHT = RE3Properties.getFloat("user_profile_artist_preference_weight");
        LABEL_PREFERENCE_WEIGHT = RE3Properties.getFloat("user_profile_label_preference_weight");
        RELEASE_PREFERENCE_WEIGHT = RE3Properties.getFloat("user_profile_release_preference_weight");
        SONG_PREFERENCE_WEIGHT = RE3Properties.getFloat("user_profile_song_preference_weight");
        NORMALIZE_STYLE_PREFERENCES = RE3Properties.getBoolean("user_profile_normalize_style_preference");
        NORMALIZE_TAG_PREFERENCES = RE3Properties.getBoolean("user_profile_normalize_tag_preference");
    }

    ////////////
    // FIELDS //
    ////////////

    protected float avgArtistRating = 0.0f;
    protected int numRatedArtists = 0;

    protected float avgLabelRating = 0.0f;
    protected int numRatedLabels = 0;

    protected float avgReleaseRating = 0.0f;
    protected int numRatedReleases = 0;

    protected float avgSongRating = 0.0f;
    protected int numRatedSongs = 0;

    protected PreferenceMap stylePreferences = new PreferenceMap();
    protected PreferenceMap tagPreferences = new PreferenceMap();
    protected PreferenceMap artistPreferences = new PreferenceMap();
    protected PreferenceMap labelPreferences = new PreferenceMap();
    protected PreferenceMap releasePreferences = new PreferenceMap();
    protected PreferenceMap songPreferences = new PreferenceMap();

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public UserProfile() { }
    public UserProfile(LineReader lineReader) {
    	int version = Integer.parseInt(lineReader.getNextLine());
    	avgArtistRating = Float.parseFloat(lineReader.getNextLine());
    	numRatedArtists = Integer.parseInt(lineReader.getNextLine());
    	avgLabelRating = Float.parseFloat(lineReader.getNextLine());
    	numRatedLabels = Integer.parseInt(lineReader.getNextLine());
    	avgReleaseRating = Float.parseFloat(lineReader.getNextLine());
    	numRatedReleases = Integer.parseInt(lineReader.getNextLine());
    	avgSongRating = Float.parseFloat(lineReader.getNextLine());
    	numRatedSongs = Integer.parseInt(lineReader.getNextLine());
    	int prefsCount = Integer.parseInt(lineReader.getNextLine());
    	if (prefsCount > 0)
    		stylePreferences = new PreferenceMap(lineReader);
    	prefsCount = Integer.parseInt(lineReader.getNextLine());
    	if (prefsCount > 0)
    		tagPreferences = new PreferenceMap(lineReader);
    	prefsCount = Integer.parseInt(lineReader.getNextLine());
    	if (prefsCount > 0)
    		artistPreferences = new PreferenceMap(lineReader);
    	prefsCount = Integer.parseInt(lineReader.getNextLine());
    	if (prefsCount > 0)
    		labelPreferences = new PreferenceMap(lineReader);
    	prefsCount = Integer.parseInt(lineReader.getNextLine());
    	if (prefsCount > 0)
    		releasePreferences = new PreferenceMap(lineReader);
    	prefsCount = Integer.parseInt(lineReader.getNextLine());
    	if (prefsCount > 0)
    		songPreferences = new PreferenceMap(lineReader);
    }

    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////

    abstract public void computeProfile(Task task);
    abstract public String getProfileId();

    /////////////
    // METHODS //
    /////////////

    @Override
	public String toString() {
    	StringBuffer result = new StringBuffer();

    	result.append("\navg. artist rating=");
    	result.append(avgArtistRating);
    	result.append("\n# artist ratings=");
    	result.append(numRatedArtists);

    	result.append("\navg. label rating=");
    	result.append(avgLabelRating);
    	result.append("\n# label ratings=");
    	result.append(numRatedLabels);

    	result.append("\navg. release rating=");
    	result.append(avgReleaseRating);
    	result.append("\n# release ratings=");
    	result.append(numRatedReleases);

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

    public float computePreference(SearchRecord searchRecord) {
		if (RE3Properties.getBoolean("skip_user_profile_computation"))
			return 0.0f;

    	float result = 0.0f;
    	try {
    		float stylePreference = 0.0f;
    		float tagPreference = 0.0f;
    		float artistPreference = 0.0f;
    		float labelPreference = 0.0f;
    		float releasePreference = 0.0f;
    		float songPreference = 0.0f;

    		float stylePreferenceWeight = STYLE_PREFERENCE_WEIGHT;
    		float tagPreferenceWeight = TAG_PREFERENCE_WEIGHT;
    		float artistPreferenceWeight = ARTIST_PREFERENCE_WEIGHT;
    		float labelPreferenceWeight = LABEL_PREFERENCE_WEIGHT;
    		float releasePreferenceWeight = RELEASE_PREFERENCE_WEIGHT;
    		float songPreferenceWeight = SONG_PREFERENCE_WEIGHT;

    		int[] styleIds = searchRecord.getActualStyleIds();
    		float[] styleDegrees = searchRecord.getActualStyleDegrees();
    		float totalStyleWeight = 0.0f;
    		for (int i = 0; i < styleIds.length; ++i) {
    			float preference = stylePreferences.getPreference(styleIds[i]);
				stylePreference += preference * styleDegrees[i];
				totalStyleWeight += Math.max(styleDegrees[i], preference);
    		}
    		if (NORMALIZE_STYLE_PREFERENCES && (totalStyleWeight > 0.0f))
    			stylePreference /= totalStyleWeight;

    		int[] tagIds = searchRecord.getActualTagIds();
    		float[] tagDegrees = searchRecord.getActualTagDegrees();
    		float totalTagWeight = 0.0f;
    		for (int i = 0; i < tagIds.length; ++i) {
    			float preference = tagPreferences.getPreference(tagIds[i]);
				tagPreference += preference * tagDegrees[i];
				totalTagWeight += Math.max(tagDegrees[i], preference);
    		}
    		if (NORMALIZE_TAG_PREFERENCES && (totalTagWeight > 0.0f))
    			tagPreference /= totalTagWeight;

    		if (searchRecord instanceof ArtistRecord) {
    			ArtistRecord artistRecord = (ArtistRecord)searchRecord;
    			artistPreference = artistPreferences.getPreference(artistRecord.getUniqueId());

    			int[] labelIds = artistRecord.getLabelIds();
    			if (labelIds != null) {
    				float[] labelDegrees = artistRecord.getLabelDegrees();
    				for (int i = 0; i < labelIds.length; ++i)
    					labelPreference += labelPreferences.getPreference(labelIds[i]) * labelDegrees[i];
    			}

    			releasePreferenceWeight = 0.0f;
    			songPreferenceWeight = 0.0f;

    		} else if (searchRecord instanceof LabelRecord) {
    			LabelRecord labelRecord = (LabelRecord)searchRecord;
    			labelPreference = labelPreferences.getPreference(labelRecord.getUniqueId());

    			int[] artistIds = labelRecord.getArtistIds();
    			if (artistIds != null) {
    				float[] artistDegrees = labelRecord.getArtistDegrees();
    				for (int i = 0; i < artistIds.length; ++i)
    					artistPreference += artistPreferences.getPreference(artistIds[i]) * artistDegrees[i];
    			}

    			releasePreferenceWeight = 0.0f;
    			songPreferenceWeight = 0.0f;

    		} else if (searchRecord instanceof ReleaseRecord) {
    			ReleaseRecord releaseRecord = (ReleaseRecord)searchRecord;
    			releasePreference = releasePreferences.getPreference(releaseRecord.getUniqueId());

    			int[] artistIds = releaseRecord.getArtistIds();
    			if (artistIds != null) {
    				for (int artistId : artistIds)
    					artistPreference += artistPreferences.getPreference(artistId);
    				if (artistIds.length > 0)
    					artistPreference /= artistIds.length;
    			}

    			int[] labelIds = releaseRecord.getLabelIds();
    			if (labelIds != null) {
    				for (int labelId : labelIds)
    					labelPreference += labelPreferences.getPreference(labelId);
    				if (labelIds.length > 0)
    					labelPreference /= labelIds.length;
    			}

    			songPreferenceWeight = 0.0f;

    		} else if (searchRecord instanceof SongRecord) {
    			SongRecord songRecord = (SongRecord)searchRecord;
    			songPreference = songPreferences.getPreference(songRecord.getUniqueId());

    			int[] artistIds = songRecord.getArtistIds();
    			if (artistIds != null) {
    				for (int artistId : artistIds)
    					artistPreference += artistPreferences.getPreference(artistId);
    				if (artistIds.length > 0)
    					artistPreference /= artistIds.length;
    			}

    			int[] labelIds = songRecord.getLabelIds();
    			if (labelIds != null) {
    				for (int labelId : labelIds)
    					labelPreference += labelPreferences.getPreference(labelId);
    				if (labelIds.length > 0)
    					labelPreference /= labelIds.length;
    			}

    			int[] releaseIds = songRecord.getReleaseIds();
    			if (releaseIds != null) {
    				for (int releaseId : releaseIds)
    					releasePreference += releasePreferences.getPreference(releaseId);
    				if (releaseIds.length > 0)
    					releasePreference /= releaseIds.length;
    			}

    		}

    		result = 0.0f;
    		result += stylePreference * stylePreferenceWeight;
    		result += tagPreference * tagPreferenceWeight;
    		result += artistPreference * artistPreferenceWeight;
    		result += labelPreference * labelPreferenceWeight;
    		result += releasePreference * releasePreferenceWeight;
    		result += songPreference * songPreferenceWeight;
    		result /= (stylePreferenceWeight + tagPreferenceWeight + artistPreferenceWeight + labelPreferenceWeight + releasePreferenceWeight + songPreferenceWeight);

    	} catch (Exception e) {
    		log.error("computePreference(): error", e);
    	}
    	return result;
    }

    public void toTextFile(LineWriter writer) {
    	writer.writeLine("num_artist_ratings=" + numRatedArtists);
    	writer.writeLine("avg_artist_rating=" + avgArtistRating);
    	writer.writeLine("num_label_ratings=" + numRatedLabels);
    	writer.writeLine("avg_label_rating=" + avgLabelRating);
    	writer.writeLine("num_release_ratings=" + numRatedReleases);
    	writer.writeLine("avg_release_rating=" + avgReleaseRating);
    	writer.writeLine("num_song_ratings=" + numRatedSongs);
    	writer.writeLine("avg_song_rating=" + avgSongRating);
    	if (stylePreferences != null)
    		writer.writeLine("style_preferences=" + stylePreferences.toString(Database.getStyleIndex()));
    	if (tagPreferences != null)
    		writer.writeLine("tag_preferences=" + tagPreferences.toString(Database.getTagIndex()));
    	if (artistPreferences != null)
    		writer.writeLine("artist_preferences=" + artistPreferences.toString(Database.getArtistIndex()));
    	if (labelPreferences != null)
    		writer.writeLine("label_preferences=" + labelPreferences.toString(Database.getLabelIndex()));
    	if (releasePreferences != null)
    		writer.writeLine("release_preferences=" + releasePreferences.toString(Database.getReleaseIndex()));
    	if (songPreferences != null)
    		writer.writeLine("song_preferences=" + songPreferences.toString(Database.getSongIndex()));
    }

    // for serialization
    public void write(LineWriter writer) {
    	writer.writeLine(1); //version
    	writer.writeLine(avgArtistRating);
    	writer.writeLine(numRatedArtists);
    	writer.writeLine(avgLabelRating);
    	writer.writeLine(numRatedLabels);
    	writer.writeLine(avgReleaseRating);
    	writer.writeLine(numRatedReleases);
    	writer.writeLine(avgSongRating);
    	writer.writeLine(numRatedSongs);
    	if (stylePreferences != null) {
    		writer.writeLine(1);
    		stylePreferences.write(writer);
    	} else {
    		writer.writeLine(0);
    	}
    	if (tagPreferences != null) {
    		writer.writeLine(1);
    		tagPreferences.write(writer);
    	} else {
    		writer.writeLine(0);
    	}
    	if (artistPreferences != null) {
    		writer.writeLine(1);
    		artistPreferences.write(writer);
    	} else {
    		writer.writeLine(0);
    	}
    	if (labelPreferences != null) {
    		writer.writeLine(1);
    		labelPreferences.write(writer);
    	} else {
    		writer.writeLine(0);
    	}
    	if (releasePreferences != null) {
    		writer.writeLine(1);
    		releasePreferences.write(writer);
    	} else {
    		writer.writeLine(0);
    	}
    	if (songPreferences != null) {
    		writer.writeLine(1);
    		songPreferences.write(writer);
    	} else {
    		writer.writeLine(0);
    	}
    }

    ///////////////////////
    // FOR SERIALIZATOIN //
    ///////////////////////

	public float getAvgArtistRating() {
		return avgArtistRating;
	}

	public void setAvgArtistRating(float avgArtistRating) {
		this.avgArtistRating = avgArtistRating;
	}

	public int getNumRatedArtists() {
		return numRatedArtists;
	}

	public void setNumRatedArtists(int numRatedArtists) {
		this.numRatedArtists = numRatedArtists;
	}

	public float getAvgLabelRating() {
		return avgLabelRating;
	}

	public void setAvgLabelRating(float avgLabelRating) {
		this.avgLabelRating = avgLabelRating;
	}

	public int getNumRatedLabels() {
		return numRatedLabels;
	}

	public void setNumRatedLabels(int numRatedLabels) {
		this.numRatedLabels = numRatedLabels;
	}

	public float getAvgReleaseRating() {
		return avgReleaseRating;
	}

	public void setAvgReleaseRating(float avgReleaseRating) {
		this.avgReleaseRating = avgReleaseRating;
	}

	public int getNumRatedReleases() {
		return numRatedReleases;
	}

	public void setNumRatedReleases(int numRatedReleases) {
		this.numRatedReleases = numRatedReleases;
	}

	public float getAvgSongRating() {
		return avgSongRating;
	}

	public void setAvgSongRating(float avgSongRating) {
		this.avgSongRating = avgSongRating;
	}

	public int getNumRatedSongs() {
		return numRatedSongs;
	}

	public void setNumRatedSongs(int numRatedSongs) {
		this.numRatedSongs = numRatedSongs;
	}

	public PreferenceMap getStylePreferences() {
		return stylePreferences;
	}

	public void setStylePreferences(PreferenceMap stylePreferences) {
		this.stylePreferences = stylePreferences;
	}

	public PreferenceMap getTagPreferences() {
		return tagPreferences;
	}

	public void setTagPreferences(PreferenceMap tagPreferences) {
		this.tagPreferences = tagPreferences;
	}

	public PreferenceMap getArtistPreferences() {
		return artistPreferences;
	}

	public void setArtistPreferences(PreferenceMap artistPreferences) {
		this.artistPreferences = artistPreferences;
	}

	public PreferenceMap getLabelPreferences() {
		return labelPreferences;
	}

	public void setLabelPreferences(PreferenceMap labelPreferences) {
		this.labelPreferences = labelPreferences;
	}

	public PreferenceMap getReleasePreferences() {
		return releasePreferences;
	}

	public void setReleasePreferences(PreferenceMap releasePreferences) {
		this.releasePreferences = releasePreferences;
	}

	public PreferenceMap getSongPreferences() {
		return songPreferences;
	}

	public void setSongPreferences(PreferenceMap songPreferences) {
		this.songPreferences = songPreferences;
	}

}
