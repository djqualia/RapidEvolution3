package com.mixshare.rapid_evolution.data.index.event;

import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;

public interface IndexChangeListener {

	public void addedRecord(Record record, SubmittedProfile submittedProfile);
	public void updatedRecord(Record record);
	public void removedRecord(Record record);
	
}
