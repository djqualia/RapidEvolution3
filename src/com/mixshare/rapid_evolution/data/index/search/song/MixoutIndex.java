package com.mixshare.rapid_evolution.data.index.search.song;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.index.CommonIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.search.parameters.SearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.MixoutSearchParameters;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedMixout;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.search.song.MixoutModelManager;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class MixoutIndex extends CommonIndex {

    static private Logger log = Logger.getLogger(MixoutIndex.class);
    static private final long serialVersionUID = 0L;    
		
	public MixoutIndex() {
		super();
	}
	public MixoutIndex(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}
	
	protected Profile getNewProfile(SubmittedProfile profile, int fileId) { return new MixoutProfile((MixoutIdentifier)profile.getIdentifier(), fileId); }
	
	public ModelManagerInterface createModelManager() {
		MixoutModelManager result = new MixoutModelManager();
		result.initColumns();
		return result;
	}		
	
	public byte getDataType() { return DATA_TYPE_MIXOUTS; }
	public MixoutModelManager getMixoutModelManager() { return (MixoutModelManager)modelManager; }
	public void setMixoutModelManager(MixoutModelManager modelManager) { this.modelManager = modelManager; }
		
	protected void initProfile(Profile profile, SubmittedProfile initialValues) {
		super.initProfile(profile, initialValues);
		MixoutProfile mixoutProfile = (MixoutProfile)profile;
		SubmittedMixout submittedMixout = (SubmittedMixout)initialValues;
		mixoutProfile.setBpmDiff(submittedMixout.getBpmDiff());
		mixoutProfile.setType(submittedMixout.getType());
		mixoutProfile.setComments(submittedMixout.getComments());
	}
	
	protected void addRelationalItems(Record addedRecord) {
		MixoutRecord mixoutRecord = (MixoutRecord)addedRecord;
		MixoutProfile mixoutProfile = (MixoutProfile)Database.getMixoutIndex().getProfile(mixoutRecord.getUniqueId());
		int fromSongId = mixoutProfile.getMixoutIdentifier().getFromSongId();
		SongProfile songProfile = (SongProfile)Database.getSongIndex().getProfile(fromSongId);
		if (songProfile != null) {
			songProfile.addMixout(mixoutProfile);
			songProfile.save();
		}
	}
	
	protected void removeRelationalItems(Record removedRecord, boolean deleteRecords) {
		MixoutRecord mixoutRecord = (MixoutRecord)removedRecord;
		int fromSongId = mixoutRecord.getMixoutIdentifier().getFromSongId();
		SongProfile songProfile = (SongProfile)Database.getSongIndex().getProfile(fromSongId);
		if (songProfile != null) {
			songProfile.removeMixout(mixoutRecord.getUniqueId());
			songProfile.save();
		}		
	}
	
	public SearchParameters getNewSearchParameters() { return new MixoutSearchParameters(); }

	public MixoutRecord getMixoutRecord(int uniqueId) { return (MixoutRecord)getRecord(uniqueId); }
	public MixoutRecord getMixoutRecord(Identifier id) { return (MixoutRecord)getRecord(id); }
	public MixoutProfile getMixoutProfile(int uniqueId) { return (MixoutProfile)getProfile(uniqueId); }
	public MixoutProfile getMixoutProfile(Identifier id) { return (MixoutProfile)getProfile(id); }
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
