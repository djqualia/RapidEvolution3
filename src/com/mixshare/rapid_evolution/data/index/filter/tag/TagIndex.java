package com.mixshare.rapid_evolution.data.index.filter.tag;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.exceptions.UnknownErrorException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.index.filter.FilterIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.filter.tag.TagSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.tag.SubmittedTag;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.filter.tag.TagModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class TagIndex extends FilterIndex {

	static private Logger log = Logger.getLogger(TagIndex.class);
	
    static private final long serialVersionUID = 0L;    

    /////////////////
    // CONSTRUCTOR //
    /////////////////

	public TagIndex() {
		super();
	}
	public TagIndex(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}
	
	////////////
	// FIELDS //
	////////////
	
	/////////////
	// GETTERS //
	/////////////
		
	public byte getDataType() { return DATA_TYPE_TAGS; }
	
	public TagModelManager getTagModelManager() { return (TagModelManager)modelManager; }
	
	public TagRecord getTagRecord(int uniqueId) { return (TagRecord)getRecord(uniqueId); } 
	public TagRecord getTagRecord(Identifier id) { return (TagRecord)getRecord(id); } 

	public TagProfile getTagProfile(int uniqueId) { return (TagProfile)getProfile(uniqueId); } 
	public TagProfile getTagProfile(Identifier id) { return (TagProfile)getProfile(id); } 
	
	protected Profile getNewProfile(SubmittedProfile profile, int uniqueId) {
		return new TagProfile((TagIdentifier)profile.getIdentifier(), uniqueId);
	}
		
	protected HierarchicalRecord createRootRecord() {
		String rootTagName = "***ROOT TAG***";
		TagIdentifier tagId = new TagIdentifier(rootTagName);
		int uniqueId = imdb.getUniqueIdFromIdentifier(tagId);
		return new TagRecord(tagId, uniqueId, true);
	}
	protected HierarchicalRecord createRootRecord(LineReader lineReader) {
		return new TagRecord(lineReader);
	}
	
	public SearchParameters getNewSearchParameters() { return new TagSearchParameters(); }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setTagModelManager(TagModelManager modelManager) { this.modelManager = modelManager; }
	
	/////////////
	// METHODS //
	/////////////

	public TagProfile addTag(SubmittedTag submittedTag) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		return (TagProfile)add(submittedTag);
	}
	
	public ModelManagerInterface createModelManager() {
		TagModelManager result = new TagModelManager();
		result.initColumns();
		return result;
	}		
	
	protected void initProfile(Profile profile, SubmittedProfile initialValues) {		
		TagProfile tagProfile = (TagProfile)profile;
		SubmittedTag submittedTag = (SubmittedTag)initialValues;
		super.initProfile(profile, initialValues);
	}			

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
