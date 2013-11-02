package com.mixshare.rapid_evolution.data;

public interface DataConstants {

	static public final byte DATA_TYPE_UNKNOWN = 0;
	static public final byte DATA_TYPE_ARTISTS = 1;
	static public final byte DATA_TYPE_LABELS = 2;
	static public final byte DATA_TYPE_RELEASES = 3;
	static public final byte DATA_TYPE_SONGS = 4;
	static public final byte DATA_TYPE_MIXOUTS = 5;
	static public final byte DATA_TYPE_PLAYLISTS = 6;
	static public final byte DATA_TYPE_STYLES = 7;
	static public final byte DATA_TYPE_TAGS = 8;

	static public final byte[] ALL_DATA_TYPES = {
		DATA_TYPE_UNKNOWN,
		DATA_TYPE_ARTISTS,
		DATA_TYPE_LABELS,
		DATA_TYPE_RELEASES,
		DATA_TYPE_SONGS,
		DATA_TYPE_MIXOUTS,
		DATA_TYPE_PLAYLISTS,
		DATA_TYPE_STYLES,
		DATA_TYPE_TAGS,
	};

	static public final byte DATA_SOURCE_UNKNOWN = 0;
	static public final byte DATA_SOURCE_USER = 1;
	static public final byte DATA_SOURCE_FILE_TAGS = 2;
	static public final byte DATA_SOURCE_MIXSHARE = 3;
	static public final byte DATA_SOURCE_DISCOGS = 4;
	static public final byte DATA_SOURCE_LASTFM = 5;
	static public final byte DATA_SOURCE_COMPUTED = 6;
	static public final byte DATA_SOURCE_ECHONEST = 7;
	static public final byte DATA_SOURCE_LYRICSFLY = 8;
	static public final byte DATA_SOURCE_LYRICWIKI = 9;
	static public final byte DATA_SOURCE_MUSICBRAINZ = 10;
	static public final byte DATA_SOURCE_YAHOO = 11;
	static public final byte DATA_SOURCE_BILLBOARD = 12;
	static public final byte DATA_SOURCE_BBC = 13;
	static public final byte DATA_SOURCE_IDIOMAG = 14;
	static public final byte DATA_SOURCE_WIKIPEDIA = 15;
	static public final byte DATA_SOURCE_TWITTER = 18;
	static public final byte DATA_SOURCE_YOUTUBE = 20;
	static public final byte DATA_SOURCE_MIXMEISTER = 29;

	static public final byte MAX_DATA_SOURCE_VALUE = 30;

}
