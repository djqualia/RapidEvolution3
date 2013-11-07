package com.mixshare.rapid_evolution.data.submitted.search.release;

import java.util.Vector;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.submitted.search.SubmittedSongGroupProfile;

public class SubmittedRelease extends SubmittedSongGroupProfile {

	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SubmittedRelease(String releaseTitle) {
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower"))
			releaseTitle = releaseTitle.toLowerCase();
		else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper"))
			releaseTitle = releaseTitle.toUpperCase();								
		identifier = new ReleaseIdentifier(releaseTitle);
	}
	public SubmittedRelease(Vector<String> artistNames, String releaseTitle) {
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower")) {
			releaseTitle = releaseTitle.toLowerCase();
			for (int i = 0; i < artistNames.size(); ++i)
				artistNames.set(i, artistNames.get(i).toLowerCase());
		} else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper")) {
			releaseTitle = releaseTitle.toUpperCase();								
			for (int i = 0; i < artistNames.size(); ++i)
				artistNames.set(i, artistNames.get(i).toUpperCase());
		}
		identifier = new ReleaseIdentifier(artistNames, releaseTitle);
	}
	public SubmittedRelease(String[] artistNames, String releaseTitle) {
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower")) {
			releaseTitle = releaseTitle.toLowerCase();
			for (int i = 0; i < artistNames.length; ++i)
				artistNames[i] = artistNames[i].toLowerCase();
		} else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper")) {
			releaseTitle = releaseTitle.toUpperCase();								
			for (int i = 0; i < artistNames.length; ++i)
				artistNames[i] = artistNames[i].toUpperCase();
		}
		identifier = new ReleaseIdentifier(artistNames, releaseTitle);
	}
	public SubmittedRelease(int[] artistIds, String releaseTitle) {
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower")) {
			releaseTitle = releaseTitle.toLowerCase();
		} else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper")) {
			releaseTitle = releaseTitle.toUpperCase();								
		}
		identifier = new ReleaseIdentifier(artistIds, releaseTitle);
	}
	public SubmittedRelease(String artistName, String releaseTitle) {
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower")) {
			releaseTitle = releaseTitle.toLowerCase();
			artistName = artistName.toLowerCase();
		} else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper")) {
			releaseTitle = releaseTitle.toUpperCase();								
			artistName = artistName.toUpperCase();
		}
		identifier = new ReleaseIdentifier(artistName, releaseTitle);
	}
	public SubmittedRelease(ReleaseIdentifier releaseId) {		
		identifier = new SubmittedRelease(releaseId.getArtistIds(), releaseId.getReleaseTitle()).getIdentifier();
	}
	public SubmittedRelease(ReleaseProfile releaseProfile) {
		super(releaseProfile);
		
		labelNames = releaseProfile.getLabelNames();
		originalYearReleased = (short)releaseProfile.getOriginalYearReleased();
		originalYearReleasedSource = releaseProfile.getOriginalYearReleasedSource();
	}
	
	////////////
	// FIELDS //
	////////////
	
	protected Vector<String> labelNames;
	protected short originalYearReleased;
	protected byte originalYearReleasedSource;

	/////////////
	// GETTERS //
	/////////////
	
	public ReleaseIdentifier getReleaseIdentifier() { return (ReleaseIdentifier)identifier; }
	
	public Vector<String> getLabelNames() { return labelNames; }
	
	public short getOriginalYearReleased() { return originalYearReleased; }
	public byte getOriginalYearReleasedSource() { return originalYearReleasedSource; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setLabelNames(Vector<String> labelNames) { this.labelNames = labelNames; }
	public void setLabelName(String labelName) {
		labelNames = new Vector<String>(1);
		labelNames.add(labelName);
	}
	
	public void setOriginalYearReleased(short originalYearReleased, byte originalYearReleasedSource) {
		this.originalYearReleased = originalYearReleased;
		this.originalYearReleasedSource = originalYearReleasedSource;
	}
	
}
