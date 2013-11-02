package com.mixshare.rapid_evolution.workflow.event;

import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;

public interface OrganizeSongListener {

	public void songOrganized(SongProfile song);
	public void songRenamed(SongProfile song);
	
}
