package com.mixshare.rapid_evolution.data.submitted.search.label;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.submitted.search.SubmittedSongGroupProfile;

public class SubmittedLabel extends SubmittedSongGroupProfile {

	public SubmittedLabel(String labelName) {
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower"))
			labelName = labelName.toLowerCase();
		else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper"))
			labelName = labelName.toUpperCase();						
		identifier = new LabelIdentifier(labelName);		
	}
	public SubmittedLabel(LabelProfile labelProfile) {
		super(labelProfile);
	}
	
	////////////
	// FIELDS //
	////////////
	

	/////////////
	// GETTERS //
	/////////////
	

	/////////////
	// SETTERS //
	/////////////
	
	
}
