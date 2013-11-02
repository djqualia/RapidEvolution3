package com.mixshare.rapid_evolution.data.profile.filter.playlist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.playlist.DynamicPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedDynamicPlaylist;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class DynamicPlaylistProfile extends PlaylistProfile {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(DynamicPlaylistProfile.class);

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public DynamicPlaylistProfile() { };
    public DynamicPlaylistProfile(PlaylistIdentifier playlistId, int uniqueId) {
    	record = new DynamicPlaylistRecord(playlistId, uniqueId);
    }
    public DynamicPlaylistProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }

    /////////////
    // GETTERS //
    /////////////

    public DynamicPlaylistRecord getDynamicPlaylistRecord() { return (DynamicPlaylistRecord)record; }

    /////////////
    // SETTERS //
    /////////////

    /////////////
    // METHODS //
    /////////////

	@Override
	public void update(SubmittedProfile submittedProfile, boolean overwrite) {
		super.update(submittedProfile, overwrite);
		SubmittedDynamicPlaylist submittedPlaylist = (SubmittedDynamicPlaylist)submittedProfile;
		for (int songId : submittedPlaylist.getSongIds())
			this.addSong(songId);
	}

    @Override
	protected Record readRecord(LineReader lineReader) {
    	//return new DynamicPlaylistRecord(lineReader);
    	return PlaylistRecord.readPlaylistRecord(lineReader);
    }

    @Override
	public void write(LineWriter writer) {
    	writer.writeLine("2", "DynamicPlaylistProfile.type"); // type
    	super.write(writer);
    	writer.writeLine("1", "DynamicPlaylistProfile.version"); // version
    }

}
