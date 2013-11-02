package com.mixshare.rapid_evolution.data.identifier.search.song;

import java.util.Arrays;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.SearchIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;

public class SongIdentifier extends SearchIdentifier {

    static private Logger log = Logger.getLogger(SongIdentifier.class);	
    static private final long serialVersionUID = 0L;    
	
    static public final String typeDescription = "song";
    
    static private String artistSeperator = "/artist/";
        
    ////////////
    // FIELDS //
    ////////////
    
    protected int[] artistIds; // uses IDs so if an artist is renamed, the associated identifiers don't have to be updated
    private String songDescription; // has to be flexible to accommodate for release/track combos and title/remix combos, etc
    
	//////////////////
	// CONSTRUCTORS //
	//////////////////
    
	public SongIdentifier() { }
	public SongIdentifier(Vector<String> artists, String songDescription) {
		artistIds = new int[artists.size()];
		for (int a = 0; a < artists.size(); ++a)
			artistIds[a] = Database.getArtistIndex().getUniqueIdFromIdentifier(new ArtistIdentifier(artists.get(a)));
		java.util.Arrays.sort(this.artistIds);
		this.songDescription = (songDescription != null) ? songDescription : "";
	}
	public SongIdentifier(int[] artistIds, String songDescription) {
		this.artistIds = new int[artistIds.length];
		for (int a = 0; a < artistIds.length; ++a)
			this.artistIds[a] = artistIds[a];
		java.util.Arrays.sort(this.artistIds);
		this.songDescription = (songDescription != null) ? songDescription : "";
	}
	public SongIdentifier(String songDescription, Vector<Integer> artistIds) {
		this.artistIds = new int[artistIds.size()];
		for (int a = 0; a < artistIds.size(); ++a)
			this.artistIds[a] = artistIds.get(a);
		java.util.Arrays.sort(this.artistIds);
		this.songDescription = (songDescription != null) ? songDescription : "";
	}
	public SongIdentifier(String artistName, String songDescription) {
		if (artistName.length() == 0) {
			artistIds = new int[0];
		} else {
			artistIds = new int[1];
			artistIds[0] = Database.getArtistIndex().getUniqueIdFromIdentifier(new ArtistIdentifier(artistName));			
		}
		this.songDescription = (songDescription != null) ? songDescription : "";
	}
	public SongIdentifier(String songDescription) {
		artistIds = new int[0];
		this.songDescription = (songDescription != null) ? songDescription : "";
	}
	
	/////////////
	// GETTERS //
	/////////////	
	
	public byte getType() { return DATA_TYPE_SONGS; }	
    public String getTypeDescription() { return typeDescription; }				
		        
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
	public String[] getLastfmArtistNames() {
		String[] artistNames = new String[artistIds.length];
		int a = 0;
		for (int artistId : artistIds)
			artistNames[a++] = Database.getArtistIndex().getArtistRecord(artistId).getLastfmArtistName();
		Arrays.sort(artistNames, String.CASE_INSENSITIVE_ORDER);	
		return artistNames;		
	}
	public String[] getDiscogsArtistNames() {
		String[] artistNames = new String[artistIds.length];
		int a = 0;
		for (int artistId : artistIds)
			artistNames[a++] = Database.getArtistIndex().getArtistRecord(artistId).getDiscogsArtistName();
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
		return result.toString();
	}
	public String getArtistDescription() { return getArtistDescription(getArtistNames()); }
	public String getLastfmArtistDescription() { return getArtistDescription(getLastfmArtistNames()); }
	public String getDiscogsArtistDescription() { return getArtistDescription(getDiscogsArtistNames()); }
	static public String getArtistDescription(Vector<String> artists) {
		String[] artistNames = new String[artists.size()];
		for (int a = 0; a < artists.size(); ++a)
			artistNames[a] = artists.get(a);
		return getArtistDescription(artistNames);
	}		
	
	public String getSongDescription() { return songDescription; }
		
	public String getUniqueId() {
		try {
			StringBuffer result = new StringBuffer();
			result.append(typeDescription);
			result.append("/");
			result.append(songDescription);
			for (int a = 0; a < artistIds.length; ++a) {
				result.append(artistSeperator);
				result.append(String.valueOf(artistIds[a]));
			}			
			return result.toString();
		} catch (Exception e) {
			log.error("getRestId(): error", e);
		}
		return null;
	}	

	public boolean isValid() {
		return !(((songDescription == null) || (songDescription.length() == 0)) &&
				((artistIds == null) || (artistIds.length == 0)));
	}	
		
	public String toString() { return toString(getArtistDescription(), songDescription); }		
	static public String toString(String artistDescription, String songDescription) {
		StringBuffer outputstring = new StringBuffer();
		if (!artistDescription.equals(""))
			outputstring.append(artistDescription);
		if (!songDescription.equals("")) {
			if (outputstring.length() > 0) {
				outputstring.append(" - ");
			}
			outputstring.append(songDescription);
		}
		return outputstring.toString();
	}
	static public String toString(String artistDescription, String release, String track, String title, String remix) {
		StringBuffer outputstring = new StringBuffer();
		if (RE3Properties.getBoolean("song_display_format_show_artist")) {
			if (!artistDescription.equals(""))
				outputstring.append(artistDescription);
		}
		if (RE3Properties.getBoolean("song_display_format_show_release")) {
			if (!release.equals("")) {
				if (outputstring.length() > 0)
					outputstring.append(" - ");
				outputstring.append(release);
			}
		}
		if (RE3Properties.getBoolean("song_display_format_show_track")) {
			if (!track.equals("")) {
				if (outputstring.length() > 0)
					outputstring.append("  ");
				outputstring.append("[");
				outputstring.append(track);
				outputstring.append("]");
			}
		}
		if (RE3Properties.getBoolean("song_display_format_show_title")) {				
			if ((title != null) && !title.equals("")) {
				if (outputstring.length() > 0) {
					if (!track.equals("") && (RE3Properties.getBoolean("song_display_format_show_release") || RE3Properties.getBoolean("song_display_format_show_track")))
						outputstring.append("  ");
					else
						outputstring.append(" - ");
				}
				outputstring.append(title);
			}
		}
		if (RE3Properties.getBoolean("song_display_format_show_remix")) {
			if ((remix != null) && !remix.equals("")) {
				if (outputstring.length() > 0)
					outputstring.append(" ");
				outputstring.append("(");
				outputstring.append(remix);
				outputstring.append(")");
			}
		}
		return outputstring.toString();
	}		
	
	/////////////
	// SETTERS //
	/////////////
		
	// for serialization
	public void setArtistIds(int[] artistIds) {
		this.artistIds = artistIds;
	}
	
	// for serialization
	public void setSongDescription(String songDescription) {
		this.songDescription = songDescription;
	}
	
	/////////////
	// METHODS //
	/////////////	
	
	static public String getSongDescriptionFromTitleAndRemix(String title, String remix) {
		StringBuffer result = new StringBuffer();
		result.append(title);
		if ((remix != null) && (remix.length() > 0)) {
			result.append(" (");
			result.append(remix);
			result.append(")");
		}
		return result.toString();
	}
	
	static public String getSongDescriptionFromReleaseAndTrack(String release, String track) {
		StringBuffer result = new StringBuffer();
		result.append(release);
		result.append("  [");
		result.append(track);
		result.append("]");
		return result.toString();
	}	
	
	static public SongIdentifier parseIdentifier(String uniqueId) {
		try {
			int artistIndex = uniqueId.indexOf(artistSeperator);
			String songDescription = null;
			if (artistIndex >= 0)
				songDescription = uniqueId.substring(typeDescription.length() + 1, artistIndex);
			else
				songDescription = uniqueId.substring(typeDescription.length() + 1);
			Vector<Integer> artistIds = new Vector<Integer>();
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
			return new SongIdentifier(songDescription, artistIds);
		} catch (Exception e) {
			log.error("parseIdentifier(): error, input=" + uniqueId, e);
		}
		return null;
	}
	
}
