package com.mixshare.rapid_evolution.data.submitted.search;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.user.UserData;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.util.DegreeValue;

public class SubmittedSearchProfile extends SubmittedProfile  {
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SubmittedSearchProfile() {
		super();
	}
	
	public SubmittedSearchProfile(SearchProfile searchProfile) {
		super(searchProfile);
		
		this.score = searchProfile.getSearchRecord().getScore();
		this.comments = searchProfile.getSearchRecord().getComments();
		this.commentsSource = searchProfile.getCommentsSource();
		this.externalItem = searchProfile.getSearchRecord().isExternalItem();
		for (MinedProfile minedProfile : searchProfile.getAllMinedProfiles().keySet())
			minedProfiles.add(minedProfile);
		this.playCount = searchProfile.getSearchRecord().getPlayCount();
	}
	
	//////////// 
	// FIELDS //
	////////////
	
	protected Vector<DegreeValue> styleDegreeValues;
	protected Vector<DegreeValue> tagDegreeValues;
	protected float score;	
	protected String comments;
	protected byte commentsSource;
	protected String thumbnailImageFilename;
	protected byte thumbnailImageFilenameSource;	
    protected Vector<Image> images = new Vector<Image>();
	protected long dateAdded = System.currentTimeMillis();
	protected Vector<UserData> userData = new Vector<UserData>();
	protected boolean externalItem;
	protected Vector<MinedProfile> minedProfiles = new Vector<MinedProfile>();
	protected long playCount;
	
	/////////////
	// GETTERS //
	/////////////
	
	public Vector<DegreeValue> getStyleDegreeValues() { return styleDegreeValues; }
	public Vector<DegreeValue> getTagDegreeValues() { return tagDegreeValues; }
	public float getScore() { return score; }
	public String getComments() { return comments; }
	public byte getCommentsSource() { return commentsSource; }
	public Vector<Image> getImages() { return images; }
	public String getThumbnailImageFilename() { return thumbnailImageFilename; }
	public byte getThumbnailImageFilenameSource() { return thumbnailImageFilenameSource; }
	public Vector<UserData> getUserData() { return userData; }
	public long getDateAdded() { return dateAdded; }
	public boolean isExternalItem() { return externalItem; }
	public Vector<MinedProfile> getMinedProfiles() { return minedProfiles; }
	public long getPlayCount() { return playCount; }
	
	/////////////
	// SETTERS //
	/////////////
	
	// styles
	public void addStyleDegreeValue(String styleName, float degree, byte source) {
		if (styleDegreeValues == null)
			styleDegreeValues = new Vector<DegreeValue>();
		styleDegreeValues.add(new DegreeValue(styleName, degree, source));
	}	
	public void setStyleDegrees(Vector<DegreeValue> styleDegrees) { styleDegreeValues = styleDegrees; }
	public void addStyleDegrees(Vector<DegreeValue> styleDegrees) {
		if (styleDegreeValues == null)
			styleDegreeValues = styleDegrees;
		else {
			for (DegreeValue degree : styleDegrees)
				styleDegreeValues.add(degree);
		}
	}
	
	// tags
	public void addTagDegreeValue(String tagName, float degree, byte source) {
		if (tagDegreeValues == null)
			tagDegreeValues = new Vector<DegreeValue>();
		tagDegreeValues.add(new DegreeValue(tagName, degree, source));
	}	
	public void setTagDegrees(Vector<DegreeValue> tagDegrees) { tagDegreeValues = tagDegrees; }
	
	public void setScore(float score) { this.score = score; }
	public void setComments(String comments, byte commentsSource) {
		this.comments = comments;
		this.commentsSource = commentsSource;
	}
	
	public void setThumbnailImage(String thumbnailFilename, byte source) {
		this.thumbnailImageFilename = thumbnailFilename; 
		this.thumbnailImageFilenameSource = source;
	}
	public void addImage(Image image, boolean thumbnail) {
		if (thumbnail) {
			this.thumbnailImageFilename = image.getImageFilename();
			this.thumbnailImageFilenameSource = image.getDataSource();			
		}
		images.add(image);
	}
	public void setImages(Vector<Image> images) { this.images = images; }
	public void addUserData(UserData userDataValue) { userData.add(userDataValue); }
	public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }	
	public void setExternalItem(boolean externalItem) {
		this.externalItem = externalItem;
		if (externalItem)
			dateAdded = 0;
	}
	
	public void addMinedProfile(MinedProfile profile) { minedProfiles.add(profile); }

	public void setPlayCount(long playCount) { this.playCount = playCount; }
	
}
