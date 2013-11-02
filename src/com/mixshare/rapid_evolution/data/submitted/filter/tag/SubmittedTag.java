package com.mixshare.rapid_evolution.data.submitted.filter.tag;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.SubmittedFilterProfile;

public class SubmittedTag extends SubmittedFilterProfile {

	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SubmittedTag(String tagName) {
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower"))
			tagName = tagName.toLowerCase();
		else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper"))
			tagName = tagName.toUpperCase();						
		identifier = new TagIdentifier(tagName);		
	}
	
	public SubmittedTag(TagProfile tagProfile) {
		super(tagProfile);
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
