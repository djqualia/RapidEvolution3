package com.mixshare.rapid_evolution.data.submitted.filter.style;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.SubmittedFilterProfile;

public class SubmittedStyle extends SubmittedFilterProfile {

	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SubmittedStyle(String styleName) {
		super();
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower"))
			styleName = styleName.toLowerCase();
		else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper"))
			styleName = styleName.toUpperCase();								
		identifier = new StyleIdentifier(styleName);		
	}
	public SubmittedStyle(StyleProfile style) {
		super(style);
		this.isCategoryOnly = style.isCategoryOnly();
		this.description = style.getDescription();
	}
		
	////////////
	// FIELDS //
	////////////
	
	protected boolean isCategoryOnly;
	protected String description;
	
	/////////////
	// GETTERS //
	/////////////
	
	public StyleIdentifier getStyleIdentifier() { return (StyleIdentifier)identifier; }
	
	public String getDescription() { return description; }
	public boolean isCategoryOnly() { return isCategoryOnly; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setDescription(String description) { this.description = description; }
	public void setCategoryOnly(boolean isCategoryOnly) { this.isCategoryOnly = isCategoryOnly; }
	
}
