package com.mixshare.rapid_evolution.audio.tags;

import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;

public interface TagEventListener {

	public void tagsRead(SubmittedSong song);
	
	public void tagsWritten(SongProfile song);
	
}
