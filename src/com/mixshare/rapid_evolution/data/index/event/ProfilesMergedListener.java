package com.mixshare.rapid_evolution.data.index.event;

import com.mixshare.rapid_evolution.data.profile.Profile;

public interface ProfilesMergedListener {

	public void profilesMerged(Profile primaryProfile, Profile mergedProfile);
	
}
