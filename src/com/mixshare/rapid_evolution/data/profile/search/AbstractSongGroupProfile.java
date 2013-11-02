package com.mixshare.rapid_evolution.data.profile.search;

import com.mixshare.rapid_evolution.util.io.LineReader;

abstract public class AbstractSongGroupProfile extends SearchProfile {
	
	public AbstractSongGroupProfile() { super(); }
	public AbstractSongGroupProfile(LineReader lineReader) { super(lineReader); }
	
	abstract public void computeMetadataFromSongs();

}
