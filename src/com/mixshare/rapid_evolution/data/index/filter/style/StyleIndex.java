package com.mixshare.rapid_evolution.data.index.filter.style;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.exceptions.UnknownErrorException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.index.filter.FilterIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.filter.style.StyleSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.style.SubmittedStyle;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.filter.style.StyleModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class StyleIndex extends FilterIndex {

	static private Logger log = Logger.getLogger(StyleIndex.class);
	
    static private final long serialVersionUID = 0L;    

    /////////////////
    // CONSTRUCTOR //
    /////////////////

	public StyleIndex() {
		super();
	}
	public StyleIndex(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}

	////////////
	// FIELDS //
	////////////	
	    
	/////////////
	// GETTERS //
	/////////////
	
	public byte getDataType() { return DATA_TYPE_STYLES; }
	
	public StyleModelManager getStyleModelManager() { return (StyleModelManager)modelManager; }
	
	public StyleRecord getStyleRecord(int uniqueId) { return (StyleRecord)getRecord(uniqueId); } 
	public StyleRecord getStyleRecord(Identifier id) { return (StyleRecord)getRecord(id); } 

	public StyleProfile getStyleProfile(int uniqueId) { return (StyleProfile)getProfile(uniqueId); } 
	public StyleProfile getStyleProfile(Identifier id) { return (StyleProfile)getProfile(id); } 
	
	protected Profile getNewProfile(SubmittedProfile profile, int fileId) {
		return new StyleProfile((StyleIdentifier)profile.getIdentifier(), fileId);
	}
		
	protected HierarchicalRecord createRootRecord() {
		String rootStyleName = "***ROOT STYLE***";
		StyleIdentifier styleId = new StyleIdentifier(rootStyleName);
		int uniqueId = imdb.getUniqueIdFromIdentifier(styleId);
		return new StyleRecord(styleId, uniqueId, true);
	}
	protected HierarchicalRecord createRootRecord(LineReader lineReader) {
		return new StyleRecord(lineReader);
	}
	
	
	public SearchParameters getNewSearchParameters() { return new StyleSearchParameters(); }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setStyleModelManager(StyleModelManager modelManager) { this.modelManager = modelManager; }
	
	/////////////
	// METHODS //
	/////////////
	
	public StyleProfile addStyle(SubmittedStyle submittedStyle) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		return (StyleProfile)add(submittedStyle);
	}
	
	public ModelManagerInterface createModelManager() {
		StyleModelManager result = new StyleModelManager();
		result.initColumns();
		return result;
	}	
	
	protected void initProfile(Profile profile, SubmittedProfile initialValues) {		
		StyleProfile styleProfile = (StyleProfile)profile;
		SubmittedStyle submittedStyle = (SubmittedStyle)initialValues;
		styleProfile.setCategoryOnly(submittedStyle.isCategoryOnly());
		styleProfile.setDescription(submittedStyle.getDescription());
		super.initProfile(profile, initialValues);
	}			
	
	protected void addRelationalItems(Record addedRecord) {
		super.addRelationalItems(addedRecord);
		// note: insert code after call to super
	}	
	protected void removeRelationalItems(Record removedRecord, boolean deleteRecords) {
		StyleRecord styleRecord = (StyleRecord)removedRecord;
		for (HierarchicalRecord child : styleRecord.getChildRecords()) {
			StyleRecord childStyle = (StyleRecord)child;
			HierarchicalRecord[] childParents = childStyle.getParentRecords();
			if (childParents != null) {
				if (deleteRecords && (childParents.length == 1) && (childParents[0] != null) && (childParents[0].equals(styleRecord))) {
					// this child will be orphaned when the parent is removed, so remove it...
					if (log.isDebugEnabled())
						log.debug("removeRelationalItems(): removing orphaned child=" + childStyle);
					Database.delete(childStyle.getIdentifier());
				}
			}
		}
		// note: insert code before call to super		
		super.removeRelationalItems(removedRecord, deleteRecords);
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
