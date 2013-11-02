package com.mixshare.rapid_evolution.data.profile.filter.playlist;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedPlaylist;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

abstract public class PlaylistProfile extends FilterProfile {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(PlaylistProfile.class);

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public PlaylistProfile() { super(); };
    public PlaylistProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }
    static public PlaylistProfile readPlaylistProfile(LineReader lineReader) {
    	int type = Integer.parseInt(lineReader.getNextLine());
    	if (type == 1)
    		return new CategoryPlaylistProfile(lineReader);
    	else if (type == 2)
    		return new DynamicPlaylistProfile(lineReader);
    	else if (type == 3)
    		return new OrderedPlaylistProfile(lineReader);
    	return null;
    }

    ////////////
    // FIELDS //
    ////////////

    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(PlaylistProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("playlistName")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }

    /////////////
    // GETTERS //
    /////////////

    public PlaylistRecord getPlaylistRecord() { return (PlaylistRecord)record; }

    public PlaylistIdentifier getPlaylistIdentifier() { return (getPlaylistRecord() != null) ? getPlaylistRecord().getPlaylistIdentifier() : null; }

    public String getPlaylistName() { return (getPlaylistRecord() != null) ? getPlaylistRecord().getPlaylistName() : ""; }

	/////////////
	// SETTERS //
	/////////////

    public void addSong(int songId) { getPlaylistRecord().addSong(songId); }

    public void setPlaylistName(String playlistName) throws AlreadyExistsException {
    	if (!getPlaylistName().equals(playlistName)) {
    		PlaylistIdentifier oldPlaylistId = getPlaylistIdentifier();
    		PlaylistIdentifier newPlaylistId = new PlaylistIdentifier(playlistName);
    		boolean unlocked = false;
    		try {
    			getRecord().getWriteLockSem().startRead("setPlaylistName");
    			updateIdentifier(newPlaylistId, oldPlaylistId);
    			getRecord().getWriteLockSem().endRead();
    			unlocked = true;
    		} catch (InterruptedException e) {
    		} finally {
    			if (!unlocked)
    				getRecord().getWriteLockSem().endRead();
    		}
    	}
    }

	/////////////
	// METHODS //
	/////////////

	@Override
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> relatedRecords = super.mergeWith(profile);
		return relatedRecords;
	}

	@Override
	public void update(SubmittedProfile submittedProfile, boolean overwrite) {
		super.update(submittedProfile, overwrite);
		SubmittedPlaylist submittedPlaylist = (SubmittedPlaylist)submittedProfile;
	}

    @Override
	public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1", "PlaylistProfile.version"); // version
    }

}
