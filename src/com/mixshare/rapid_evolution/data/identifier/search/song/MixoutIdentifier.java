package com.mixshare.rapid_evolution.data.identifier.search.song;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.CommonIdentifier;

public class MixoutIdentifier extends CommonIdentifier {

    static private Logger log = Logger.getLogger(MixoutIdentifier.class);	
    static private final long serialVersionUID = 0L;    
	
    static public final String typeDescription = "mixout";
            
    static private String songSeperator = "/toSongID/";
    
    ////////////
    // FIELDS //
    ////////////
    
    private int fromSongId;
    private int toSongId;
    
	//////////////////
	// CONSTRUCTORS //
	//////////////////
    
    public MixoutIdentifier() { }
	public MixoutIdentifier(int fromSongId, int toSongId) {
		this.fromSongId = fromSongId;
		this.toSongId = toSongId;
	}
	
	/////////////
	// GETTERS //
	/////////////	
	
	public byte getType() { return DATA_TYPE_MIXOUTS; }	
    public String getTypeDescription() { return typeDescription; }				

    public int getFromSongId() { return fromSongId; }
    public SongIdentifier getFromSongIdentifier() { return (SongIdentifier)Database.getSongIndex().getIdentifierFromUniqueId(fromSongId); }

    public int getToSongId() { return toSongId; }
    public SongIdentifier getToSongIdentifier() { return (SongIdentifier)Database.getSongIndex().getIdentifierFromUniqueId(toSongId); }

	public String getUniqueId() {
		try {
			StringBuffer result = new StringBuffer();
			result.append(typeDescription);
			result.append("/");
			result.append(String.valueOf(fromSongId));
			result.append(songSeperator);
			result.append(String.valueOf(toSongId));
			return result.toString();
		} catch (Exception e) {
			log.error("getRestId(): error", e);
		}
		return null;
	}	

	public boolean isValid() {
		return Database.getSongIndex().doesExist(fromSongId) && Database.getSongIndex().doesExist(toSongId);
	}	
		
	public String toString() { return toString((SongIdentifier)Database.getSongIndex().getIdentifierFromUniqueId(fromSongId), (SongIdentifier)Database.getSongIndex().getIdentifierFromUniqueId(toSongId)); }
	static public String toString(SongIdentifier fromSongId, SongIdentifier toSongId) { return fromSongId.toString() + "->" + toSongId.toString(); }

	/////////////
	// SETTERS //
	/////////////
	
	// for serialization
	public void setFromSongId(int fromSongId) {
		this.fromSongId = fromSongId;
	}
	
	// for serialization
	public void setToSongId(int toSongId) {
		this.toSongId = toSongId;
	}
	
	/////////////
	// METHODS //
	/////////////	
	
	static public MixoutIdentifier parseIdentifier(String uniqueId) {
		try {
			int separatorIndex = uniqueId.indexOf(songSeperator);
			int songId1 = Integer.parseInt(uniqueId.substring(typeDescription.length() + 1, separatorIndex));
			int songId2 = Integer.parseInt(uniqueId.substring(separatorIndex + songSeperator.length()));
			return new MixoutIdentifier(songId1, songId2);
		} catch (Exception e) {
			log.error("parseIdentifier(): error, input=" + uniqueId, e);
		}
		return null;
	}
	
}
