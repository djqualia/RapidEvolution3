package com.mixshare.rapid_evolution.data.profile.filter.playlist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.playlist.OrderedPlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedOrderedPlaylist;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class OrderedPlaylistProfile extends PlaylistProfile {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(OrderedPlaylistProfile.class);

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public OrderedPlaylistProfile() { };
    public OrderedPlaylistProfile(PlaylistIdentifier playlistId, int uniqueId) {
    	record = new OrderedPlaylistRecord(playlistId, uniqueId);
    }
    public OrderedPlaylistProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }


    /////////////
    // GETTERS //
    /////////////

    public OrderedPlaylistRecord getOrderedPlaylistRecord() { return (OrderedPlaylistRecord)record; }

    /////////////
    // SETTERS //
    /////////////

    /////////////
    // METHODS //
    /////////////

	@Override
	public void update(SubmittedProfile submittedProfile, boolean overwrite) {
		super.update(submittedProfile, overwrite);
		SubmittedOrderedPlaylist submittedPlaylist = (SubmittedOrderedPlaylist)submittedProfile;
		//for (int songId : submittedPlaylist.getSongIds())
			//this.addSong(songId);
	}

    @Override
	protected Record readRecord(LineReader lineReader) {
    	//return new OrderedPlaylistRecord(lineReader);
    	return PlaylistRecord.readPlaylistRecord(lineReader);
    }

    @Override
	public void write(LineWriter writer) {
    	writer.writeLine("3", "OrderedPlaylistProfile.type"); // type
    	super.write(writer);
    	writer.writeLine("1", "OrderedPlaylistProfile.version"); // version
    }

}
