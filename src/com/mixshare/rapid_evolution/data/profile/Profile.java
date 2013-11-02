package com.mixshare.rapid_evolution.data.profile;

import java.util.Map;

import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * A profile provides access to all information for the various types of items (songs, artists, releases, labels, styles, tags, playlists, etc).
 *
 * A profile will be saved to the hard disk, and a corresponding record will be held in memory which stores the required data for searching and looking up the profile...
 */
public interface Profile {

	public int getUniqueId();
	public Identifier getIdentifier();

	public int getNumDuplicateIds();
	public int getDuplicateId(int index);

	/**
	 * Don't call directly, call the database/index method mergeProfiles(...)
	 */
	public Map<Record, Object> mergeWith(Profile profile);

	public Record getRecord();
	public void setRecord(Record record);

    public Rating getRating();
    public void setRating(Rating rating, byte source);
    public byte getRatingSource();

    public boolean isDisabled();
    public void setDisabled(boolean disabled);

    public long getLastModified();
    public void setLastModified();

    public void update(SubmittedProfile submittedProfile, boolean overwrite);

    /**
     * This will save the profile to the hard disk and push any changes to the UI.
     */
    public boolean save();

    public void write(LineWriter textWriter);

}
