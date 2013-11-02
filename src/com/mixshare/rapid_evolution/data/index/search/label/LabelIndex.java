package com.mixshare.rapid_evolution.data.index.search.label;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.exceptions.UnknownErrorException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.index.search.SearchIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.label.SubmittedLabel;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.model.search.label.LabelModelManager;
import com.mixshare.rapid_evolution.ui.model.search.label.RecommendedLabelModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class LabelIndex extends SearchIndex {

    static private final long serialVersionUID = 0L;    
	
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public LabelIndex() {
		super();
	}
	public LabelIndex(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}
	
	
    /////////////
    // GETTERS //
    /////////////
	    
	public byte getDataType() { return DATA_TYPE_LABELS; }
	
	public LabelModelManager getLabelModelManager() { return (LabelModelManager)modelManager; }
	
	protected Profile getNewProfile(SubmittedProfile profile, int fileId) { return new LabelProfile((LabelIdentifier)profile.getIdentifier(), fileId); }

	public LabelRecord getLabelRecord(int uniqueId) { return (LabelRecord)getRecord(uniqueId); } 
	public LabelRecord getLabelRecord(Identifier id) { return (LabelRecord)getRecord(id); } 

	public LabelProfile getLabelProfile(int uniqueId) { return (LabelProfile)getProfile(uniqueId); } 
	public LabelProfile getLabelProfile(Identifier id) { return (LabelProfile)getProfile(id); } 
	
	public RecommendedLabelModelManager getRecommendedLabelModelManager() { return (RecommendedLabelModelManager)recommendedModelManager; }
	
	public SearchParameters getNewSearchParameters() { return new LabelSearchParameters(); }
	
	/////////////
	// SETTERS //
	/////////////
	
	// for serialization
	public void setLabelModelManager(LabelModelManager modelManager) { this.modelManager = modelManager; }
	public void setRecommendedLabelModelManager(RecommendedLabelModelManager recommendedModelManager) { this.recommendedModelManager = recommendedModelManager; }
	
	/////////////
	// METHODS //
	/////////////
		
	public void propertiesChanged() {
		LabelProfile.loadProperties();
	}	
	
	public LabelProfile addLabel(SubmittedLabel submittedLabel) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		return (LabelProfile)add(submittedLabel);
	}
	
	public Profile add(SubmittedProfile submittedProfile) throws AlreadyExistsException, InsufficientInformationException, UnknownErrorException {
		if ((RE3Properties.getInt("max_external_labels") == 0) && ((SubmittedLabel)submittedProfile).isExternalItem())
			return null;	
		return super.add(submittedProfile);
	}
	
	public ModelManagerInterface createModelManager() {
		LabelModelManager result = new LabelModelManager();
		result.initColumns();
		return result;
	}	
	
	protected SearchModelManager createRecommendedModelManager() {
		RecommendedLabelModelManager result = new RecommendedLabelModelManager();
		result.initColumns();
		return result;
	}
	
	protected void initProfile(Profile profile, SubmittedProfile initialValues) {
		super.initProfile(profile, initialValues);
		LabelProfile labelProfile = (LabelProfile)profile;
		SubmittedLabel submittedLabel = (SubmittedLabel)initialValues;
		if (submittedLabel.getInitialSongId() != null)
			labelProfile.addSong(Database.getSongIndex().getUniqueIdFromIdentifier(submittedLabel.getInitialSongId()));
	}
	
	protected void addRelationalItems(Record addedRecord) {
		super.addRelationalItems(addedRecord);
		// note: insert code after call to super
	}	
	protected void removeRelationalItems(Record removedRecord, boolean deleteRecords) {
		// note: insert code before call to super
		super.removeRelationalItems(removedRecord, deleteRecords);
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
