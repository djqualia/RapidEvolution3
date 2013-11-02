package com.mixshare.rapid_evolution.data.index.filter;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.index.HierarchicalIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedHierarchicalProfile;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.util.normalization.DuplicateRecordMerger;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

/**
 * Abstracted ahead of time just in case...
 */
abstract public class FilterIndex extends HierarchicalIndex {

	static private Logger log = Logger.getLogger(FilterIndex.class);
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public FilterIndex() {
		super();
		addIndexChangeListener(new DuplicateRecordMerger(this));
	}
	public FilterIndex(LineReader lineReader) {
		super(lineReader);
		String version = lineReader.getNextLine();
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public FilterRecord getFilterRecord(int uniqueId) { return (FilterRecord)getRecord(uniqueId); }
	
	/////////////
	// METHODS //
	/////////////	
	
	protected void initProfile(Profile profile, SubmittedProfile initialValues) {
		FilterProfile filterProfile = (FilterProfile)profile;
		SubmittedHierarchicalProfile submittedHierarchicalProfile = (SubmittedHierarchicalProfile)initialValues;
		HierarchicalRecord hierarchicalRecord = (HierarchicalRecord)filterProfile.getRecord();
		if (submittedHierarchicalProfile.isChildOfRoot()) {
			if (!submittedHierarchicalProfile.doNotAddToHierarchy()) {
				// add to the root
				addRelationship(getRootRecord(), hierarchicalRecord);
			}
		} else {
			// parent instances were specified, add a new instance to each
			for (TreeHierarchyInstance hierarchyInstance : submittedHierarchicalProfile.getParentInstances()) {
				if (!submittedHierarchicalProfile.doNotAddToHierarchy()) {
					addRelationship(hierarchyInstance.getRecord(), hierarchicalRecord);
				}
			}
		}				
	}
	
	protected void addRelationalItems(Record addedRecord) {
		super.addRelationalItems(addedRecord);
	}	
	protected void removeRelationalItems(Record removedRecord, boolean deleteRecords) {
		super.removeRelationalItems(removedRecord, deleteRecords);
	}
		
	public void update(Record record) {
		try {
			FilterRecord filterRecord = (FilterRecord)record;
			filterRecord.resetCachedRecordCount();
			//filterRecord.computeNumArtistRecords();
			//filterRecord.computeNumLabelRecords();
			//filterRecord.computeNumReleaseRecords();
			//filterRecord.computeNumSongRecords();			
		} catch (Exception e) {
			log.error("update(): error", e);
		}
		super.update(record);
	}	

	public void mergeProfiles(Profile primaryProfile, Profile mergedProfile) {
		FilterProfile primaryFilter = (FilterProfile)primaryProfile;
		primaryFilter.getFilterRecord().resetCachedRecordCount(true);
		super.mergeProfiles(primaryProfile, mergedProfile);
	}
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
