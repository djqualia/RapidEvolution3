package com.mixshare.rapid_evolution.data.profile.filter.playlist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.playlist.CategoryPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedCategoryPlaylist;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class CategoryPlaylistProfile extends PlaylistProfile {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(CategoryPlaylistProfile.class);

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public CategoryPlaylistProfile() { };
    public CategoryPlaylistProfile(PlaylistIdentifier playlistId, int uniqueId) {
    	record = new CategoryPlaylistRecord(playlistId, uniqueId);
    }
    public CategoryPlaylistProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }

    /////////////
    // GETTERS //
    /////////////

    public CategoryPlaylistRecord getCategoryPlaylistRecord() { return (CategoryPlaylistRecord)record; }

    /////////////
    // SETTERS //
    /////////////

    /////////////
    // METHODS //
    /////////////

	@Override
	public void update(SubmittedProfile submittedProfile, boolean overwrite) {
		super.update(submittedProfile, overwrite);
		SubmittedCategoryPlaylist submittedPlaylist = (SubmittedCategoryPlaylist)submittedProfile;
	}

    @Override
	protected Record readRecord(LineReader lineReader) {
    	return PlaylistRecord.readPlaylistRecord(lineReader);
    	//return new CategoryPlaylistRecord(lineReader);
    }

    @Override
	public void write(LineWriter writer) {
    	writer.writeLine("1", "CategoryPlaylistProfile.type"); // type
    	super.write(writer);
    	writer.writeLine("1", "CategoryPlaylistProfile.version"); // version
    }

}
