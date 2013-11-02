package com.mixshare.rapid_evolution.data.record.search.song;

import java.util.Map;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.CommonRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class MixoutRecord extends CommonRecord {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(MixoutRecord.class);

	static public final byte TYPE_TRANSITION = 0;
	static public final byte TYPE_ADDON = 1;

	static public String getMixoutTypeDescription(byte type) {
		if (type == TYPE_TRANSITION)
			return Translations.get("mixout_transition_text");
		if (type == TYPE_ADDON)
			return Translations.get("mixout_addon_text");
		return "";
	}
	static public String[] getMixoutTypeDescriptions() { return new String[] { Translations.get("mixout_transition_text"), Translations.get("mixout_addon_text") }; }
	static public byte getMixoutType(int index) {
		if (index == 0)
			return TYPE_TRANSITION;
		return TYPE_ADDON;
	}

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public MixoutRecord() { };
    public MixoutRecord(MixoutIdentifier mixoutId, int uniqueId) {
    	this.id = mixoutId;
    	this.uniqueId = uniqueId;
    }
    public MixoutRecord(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    	bpmDiff = Float.parseFloat(lineReader.getNextLine());
    	type = Byte.parseByte(lineReader.getNextLine());
    	isSyncedWithMixshare = Boolean.parseBoolean(lineReader.getNextLine());
    	comments = lineReader.getNextLine();
    }

    ////////////
    // FIELDS //
    ////////////

	private float bpmDiff;
	private byte type;
    private boolean isSyncedWithMixshare;
	private String comments;

    /////////////
    // GETTERS //
    /////////////

	public int getFromSongUniqueId() { return getMixoutIdentifier().getFromSongId(); }
	public SongRecord getFromSong() {
		return Database.getSongIndex().getSongRecord(getMixoutIdentifier().getFromSongId());
	}

	public int getToSongUniqueId() { return getMixoutIdentifier().getToSongId(); }
	public SongRecord getToSong() {
		return Database.getSongIndex().getSongRecord(getMixoutIdentifier().getToSongId());
	}

	@Override
	public byte getDataType() { return DATA_TYPE_MIXOUTS; }

    @Override
	public Index getIndex() { return Database.getMixoutIndex(); }

    public MixoutIdentifier getMixoutIdentifier() { return (MixoutIdentifier)id; }

	public float getBpmDiff() { return bpmDiff; }
	public boolean isSyncedWithMixshare() { return isSyncedWithMixshare; }
	public String getComments() { return comments; }

	public byte getType() { return type; }
	public String getMixoutTypeDescription() { return getMixoutTypeDescription(type); }

    public ModelManagerInterface getModelManager() { return Database.getMixoutModelManager(); }

    /////////////
    // SETTERS //
    /////////////

	public void setBpmDiff(float bpmDiff) { this.bpmDiff = bpmDiff; }
	public void setType(byte type) { this.type = type; }
	public void setSyncedWithMixshare(boolean isSyncedWithMixshare) { this.isSyncedWithMixshare = isSyncedWithMixshare; }
	public void setComments(String comments) { this.comments = comments; }

    /////////////
    // METHODS //
    /////////////

	@Override
	public String toString() {
		MixoutIdentifier mixoutId = getMixoutIdentifier();
		SongIdentifier originalFromSongId = mixoutId.getFromSongIdentifier();
		SongIdentifier originalToSongId = mixoutId.getToSongIdentifier();
		if ((originalFromSongId == null) && (originalToSongId == null))
			return "";
		SongIdentifier actualFromSongId = (SongIdentifier)Database.getSongIndex().getRecord(originalFromSongId).getIdentifier();
		SongIdentifier actualToSongId = (SongIdentifier)Database.getSongIndex().getRecord(originalToSongId).getIdentifier();
		return MixoutIdentifier.toString(actualFromSongId, actualToSongId);
	}

    @Override
	public void mergeWith(Record record, Map<Record, Object> recordsToRefresh) {
    	super.mergeWith(record, recordsToRefresh);
    	MixoutRecord mixoutRecord = (MixoutRecord)record;
    	// TODO: implement?
    }

    @Override
	public void write(LineWriter textWriter) {
    	super.write(textWriter);
    	textWriter.writeLine(1); //version
    	textWriter.writeLine(bpmDiff);
    	textWriter.writeLine(type);
    	textWriter.writeLine(isSyncedWithMixshare);
    	textWriter.writeLine(comments);
    }

}
