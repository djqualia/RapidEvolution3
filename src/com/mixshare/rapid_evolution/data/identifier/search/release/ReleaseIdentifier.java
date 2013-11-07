package com.mixshare.rapid_evolution.data.identifier.search.release;

import java.util.Arrays;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.SearchIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.ui.util.Translations;

public class ReleaseIdentifier extends SearchIdentifier {

    static private Logger log = Logger.getLogger(ReleaseIdentifier.class);	
    static private final long serialVersionUID = 0L;    

    static public String releaseCompilationArtistDescription;
    
    static public final String typeDescription = "release";
        
    static private String artistSeperator = "/artist/";
    
    static {
    	try {
    		releaseCompilationArtistDescription = Translations.get("release_compilation_artist_description"); // i.e. "Various"
    	} catch (java.lang.ExceptionInInitializerError e) {
    		log.error("static(): error", e);
    	} catch (Exception e) {
    		log.error("static(): error", e);
    	}
    }
     
    ////////////
    // FIELDS //
    ////////////
    
	private int[] artistIds; // uses IDs so if an artist is renamed, the related identifiers don't need to be updated
	private String releaseTitle;
			
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public ReleaseIdentifier() { }
	public ReleaseIdentifier(String releaseTitle, Vector<Integer> artistIds) {
		this.artistIds = new int[artistIds.size()];
		for (int a = 0; a < artistIds.size(); ++a)
			this.artistIds[a] = artistIds.get(a);
		java.util.Arrays.sort(this.artistIds);
		this.releaseTitle = releaseTitle;
	}
	public ReleaseIdentifier(Vector<String> artists, String releaseTitle) {
		artistIds = new int[artists.size()];
		for (int a = 0; a < artists.size(); ++a)
			artistIds[a] = Database.getArtistIndex().getUniqueIdFromIdentifier(new ArtistIdentifier(artists.get(a)));
		java.util.Arrays.sort(this.artistIds);
		this.releaseTitle = releaseTitle;
	}
	public ReleaseIdentifier(int[] artistIds, String releaseTitle) {
		this.artistIds = new int[artistIds.length];
		for (int a = 0; a < artistIds.length; ++a)
			this.artistIds[a] = artistIds[a];
		java.util.Arrays.sort(this.artistIds);
		this.releaseTitle = releaseTitle;
	}
	public ReleaseIdentifier(String[] artists, String releaseTitle) {
		artistIds = new int[artists.length];
		for (int a = 0; a < artists.length; ++a)
			artistIds[a] = Database.getArtistIndex().getUniqueIdFromIdentifier(new ArtistIdentifier(artists[a]));
		java.util.Arrays.sort(this.artistIds);
		this.releaseTitle = releaseTitle;
	}
	public ReleaseIdentifier(String artistName, String releaseTitle) {
		artistIds = new int[1];
		artistIds[0] = Database.getArtistIndex().getUniqueIdFromIdentifier(new ArtistIdentifier(artistName));
		this.releaseTitle = releaseTitle;
	}	
	public ReleaseIdentifier(String releaseTitle) { // compilation constructor
		artistIds = new int[0];
		this.releaseTitle = releaseTitle;
	}	
	
	/////////////
	// GETTERS //
	/////////////
	
	public byte getType() { return DATA_TYPE_RELEASES; }	
    public String getTypeDescription() { return "release"; }
			
    public int getNumArtists() { return artistIds.length; }
    public int[] getArtistIds() { return artistIds; }
	public String[] getArtistNames() {
		String[] artistNames = new String[artistIds.length];
		int a = 0;
		for (int artistId : artistIds)
			artistNames[a++] = Database.getArtistIndex().getIdentifierFromUniqueId(artistId).toString();	
		Arrays.sort(artistNames, String.CASE_INSENSITIVE_ORDER);	
		return artistNames;
	}
	static public String getArtistDescription(String[] artistNames) { 
		StringBuffer result = new StringBuffer();
		for (int a = 0; a < artistNames.length; ++a) {
			result.append(artistNames[a]);
			if (a + 1 < artistNames.length)
				result.append(", ");
		}
		if (result.length() == 0)
			result.append(releaseCompilationArtistDescription);
		return result.toString();
	}
	public String getArtistDescription() { return getArtistDescription(getArtistNames()); }
	
	public String getReleaseTitle() { return releaseTitle; }
	
	public boolean isValid() {
		return ((releaseTitle != null) && (releaseTitle.length() > 0));
	}
	
	public String getUniqueId() {
		try {
			StringBuffer uniqueId = new StringBuffer();
			uniqueId.append(typeDescription);
			uniqueId.append("/");
			uniqueId.append(releaseTitle);
			for (int a = 0; a < artistIds.length; ++a) {
				uniqueId.append(artistSeperator);
				uniqueId.append(String.valueOf(artistIds[a]));
			}
			return uniqueId.toString();
		} catch (Exception e) {
			log.error("getUniqueId(): error", e);
		}
		return null;
	}
			
	public String toString() { return toString(getArtistDescription(), releaseTitle); }	
	static public String toString(String artistDescription, String releaseTitle) {
		StringBuffer result = new StringBuffer();
		result.append(artistDescription);
		result.append(" - ");
		result.append(releaseTitle);
		return result.toString();		
	}

	/////////////
	// SETTERS //
	/////////////
	
	// for serialization
	public void setArtistIds(int[] artistIds) {
		this.artistIds = artistIds;
	}
	
	// for serialization
	public void setReleaseTitle(String releaseTitle) {
		this.releaseTitle = releaseTitle;
	}		
	
	/////////////
	// METHODS //
	/////////////	
	
	static public ReleaseIdentifier parseIdentifier(String uniqueId) {
		try {
			int artistIndex = uniqueId.indexOf(artistSeperator);
			String album = null;
			Vector<Integer> artistIds = new Vector<Integer>();
			if (artistIndex >= 0) {
				album = uniqueId.substring(typeDescription.length() + 1, artistIndex);
				int index = uniqueId.indexOf(artistSeperator);	
				while (index >= 0) {
					int nextIndex = uniqueId.indexOf(artistSeperator, index + artistSeperator.length());
					int artistId;
					if (nextIndex >= 0) {
						artistId = Integer.parseInt(uniqueId.substring(index + artistSeperator.length(), nextIndex));
					} else {
						artistId = Integer.parseInt(uniqueId.substring(index + artistSeperator.length()));
					}
					artistIds.add(artistId);
					index = nextIndex;
				}
			} else {
				album = uniqueId.substring(typeDescription.length() + 1);
			}
			return new ReleaseIdentifier(album, artistIds);
		} catch (Exception e) {
			log.error("parseIdentifier(): error, input=" + uniqueId, e);
		}
		return null;
	}
		
}