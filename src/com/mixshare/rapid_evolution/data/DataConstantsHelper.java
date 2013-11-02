package com.mixshare.rapid_evolution.data;

import com.mixshare.rapid_evolution.ui.util.Translations;

public class DataConstantsHelper implements DataConstants {

	static public String getDataTypeDescription(byte type) {
		return Translations.getPreferredCase(getDataTypeDescriptionSource(type));
	}

	static public String getDataTypeDescriptionForFiles(byte type) {
		return getDataTypeDescriptionSource(type).toLowerCase();
	}

	static public String getDataTypeDescriptionSource(byte type) {
		if (type == DATA_TYPE_ARTISTS)
			return "Artist";
		if (type == DATA_TYPE_LABELS)
			return "Label";
		if (type == DATA_TYPE_RELEASES)
			return "Release";
		if (type == DATA_TYPE_SONGS)
			return "Song";
		if (type == DATA_TYPE_MIXOUTS)
			return "Mixout";
		if (type == DATA_TYPE_PLAYLISTS)
			return "Playlist";
		if (type == DATA_TYPE_STYLES)
			return "Style";
		if (type == DATA_TYPE_TAGS)
			return "Tag";
		return "Unknown";
	}

	static public String getDataSourceDescription(byte source) {
		return Translations.getPreferredCase(getDataSourceDescriptionSource(source));
	}

	static public String getDataSourceDescriptionSource(byte source) {
		if (source == DATA_SOURCE_USER)
			return "User";
		if (source == DATA_SOURCE_FILE_TAGS)
			return "File Tag";
		if (source == DATA_SOURCE_MIXSHARE)
			return "Mixshare";
		if (source == DATA_SOURCE_DISCOGS)
			return "Discogs";
		if (source == DATA_SOURCE_LASTFM)
			return "Lastfm";
		if (source == DATA_SOURCE_COMPUTED)
			return "Computed";
		if (source == DATA_SOURCE_ECHONEST)
			return "Echonest";
		if (source == DATA_SOURCE_LYRICSFLY)
			return "LyricsFly";
		if (source == DATA_SOURCE_LYRICWIKI)
			return "LyricWiki";
		if (source == DATA_SOURCE_MUSICBRAINZ)
			return "Musicbrainz";
		if (source == DATA_SOURCE_YAHOO)
			return "Yahoo";
		if (source == DATA_SOURCE_BILLBOARD)
			return "Billboard";
		if (source == DATA_SOURCE_BBC)
			return "BBC";
		if (source == DATA_SOURCE_IDIOMAG)
			return "Idiomag";
		if (source == DATA_SOURCE_WIKIPEDIA)
			return "Wikipedia";
		if (source == DATA_SOURCE_YOUTUBE)
			return "YouTube";
		if (source == DATA_SOURCE_MIXMEISTER)
			return "Mixmeister";
		return "Unknown";
	}

	static public byte getDataSourceFromDescription(String description) {
		// not the most efficient method, but not called in important places right now
		for (byte b = 0; b <= MAX_DATA_SOURCE_VALUE; ++b) {
			String dataSource = getDataSourceDescriptionSource(b);
			if (dataSource.equalsIgnoreCase(description))
				return b;
		}
		return DATA_SOURCE_UNKNOWN;
	}

}
