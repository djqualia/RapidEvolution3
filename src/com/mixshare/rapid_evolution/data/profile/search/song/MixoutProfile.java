package com.mixshare.rapid_evolution.data.profile.search.song;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.profile.CommonProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class MixoutProfile extends CommonProfile {
	
    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(MixoutProfile.class);    
		
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public MixoutProfile() { };
    public MixoutProfile(MixoutIdentifier mixoutId, int fileId) {
    	record = new MixoutRecord(mixoutId, fileId);
    }
    public MixoutProfile(MixoutRecord record) {
    	this.record = record;
    }
    public MixoutProfile(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }        
    
	////////////
	// FIELDS //
	////////////
	    
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(MixoutProfile.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("mixoutIdentifier") || pd.getName().equals("fromSongUniqueId") || pd.getName().equals("fromSong") || pd.getName().equals("toSongUniqueId")
    					|| pd.getName().equals("toSong") || pd.getName().equals("bpmDiff") || pd.getName().equals("type") || pd.getName().equals("syncedWithMixshare") || pd.getName().equals("comments")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
    
	/////////////
	// GETTERS //
	/////////////
	
    public MixoutRecord getMixoutRecord() { return (MixoutRecord)record; }
    
    public MixoutIdentifier getMixoutIdentifier() { return getMixoutRecord().getMixoutIdentifier(); }
    
	public int getFromSongUniqueId() { return getMixoutRecord().getFromSongUniqueId(); }
	public SongRecord getFromSong() { return getMixoutRecord().getFromSong(); }

	public int getToSongUniqueId() { return getMixoutRecord().getToSongUniqueId(); }
	public SongRecord getToSong() { return getMixoutRecord().getToSong(); }
    
    
	public float getBpmDiff() { return (getMixoutRecord() != null) ? getMixoutRecord().getBpmDiff() : Float.NaN; }
	public byte getType() { return (getMixoutRecord() != null) ? getMixoutRecord().getType() : Byte.MIN_VALUE; }
	public boolean isSyncedWithMixshare() { return (getMixoutRecord() != null) ? getMixoutRecord().isSyncedWithMixshare() : false; }	
    
	public String getComments() { return (getMixoutRecord() != null) ? getMixoutRecord().getComments() : null; }	

	/////////////
	// SETTERS //
	/////////////
	
	public void setBpmDiff(float bpmDiff) { if (getMixoutRecord() != null) getMixoutRecord().setBpmDiff(bpmDiff); }
	public void setType(byte type) { if (getMixoutRecord() != null) getMixoutRecord().setType(type); }
	public void setSyncedWithMixshare(boolean isSyncedWithMixshare) { if (getMixoutRecord() != null) getMixoutRecord().setSyncedWithMixshare(isSyncedWithMixshare); }		
	public void setComments(String comments) { if (getMixoutRecord() != null) getMixoutRecord().setComments(comments); }
	
	/////////////
	// METHODS //
	/////////////
	
	public Map<Record, Object> mergeWith(Profile profile) {
		Map<Record, Object> relatedRecords = super.mergeWith(profile);
		MixoutProfile mixoutProfile = (MixoutProfile)profile;
		// TODO: implement		
		return relatedRecords;
	}    
	
    public float getSimilarity(SearchRecord record) {
    	// TODO: implemlent
    	return 0.0f;
    }    
    
    public void update(SubmittedProfile submittedProfile, boolean overwrite) { }

    public void write(LineWriter writer) {
    	super.write(writer);
    	writer.writeLine("1"); // version
    }

    protected Record readRecord(LineReader lineReader) {
    	return new MixoutRecord(lineReader);
    }
    
}
