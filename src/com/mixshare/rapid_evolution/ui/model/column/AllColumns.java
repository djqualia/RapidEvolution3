package com.mixshare.rapid_evolution.ui.model.column;

import com.mixshare.rapid_evolution.RE3Properties;

/**
 * All of the columns are defined here.
 *
 * NOTE: Not sure at this point whether its better they all exist like this in 1 class
 * or if they exist in the various classes in the object hierarchy...
 */
public interface AllColumns {

	// search columns
    static public final StaticTypeColumn COLUMN_LAST_MODIFIED = new StaticTypeColumn(0, "column_last_modified_title", "column_last_modified_description", true, RE3Properties.getShort("column_width_last_modified"));
    static public final StaticTypeColumn COLUMN_RATING_STARS = new StaticTypeColumn(1, "column_rating_title", "column_rating_description", false, RE3Properties.getShort("column_width_rating"));
    static public final StaticTypeColumn COLUMN_RATING_VALUE = new StaticTypeColumn(2, "column_rating_value_title", "column_rating_value_description", true, RE3Properties.getShort("column_width_rating_value"));
    static public final StaticTypeColumn COLUMN_COMMENTS = new StaticTypeColumn(3, "column_comments_title", "column_comments_description", false, RE3Properties.getShort("column_width_comments"));
    static public final StaticTypeColumn COLUMN_STYLES = new StaticTypeColumn(4, "column_styles_title", "column_styles_description", true, RE3Properties.getShort("column_width_styles"));
    static public final StaticTypeColumn COLUMN_TAGS = new StaticTypeColumn(5, "column_tags_title", "column_tags_description", true, RE3Properties.getShort("column_width_tags"));
    static public final StaticTypeColumn COLUMN_THUMBNAIL_IMAGE = new StaticTypeColumn(6, "column_image_title", "column_image_description", false, RE3Properties.getShort("column_width_image"));

    // song group fields
	static public final StaticTypeColumn COLUMN_NUM_SONGS = new StaticTypeColumn(7, "column_num_songs_title", "column_num_songs_description", false, RE3Properties.getShort("column_width_artist_num_songs"));

    // artist columns
	static public final StaticTypeColumn COLUMN_ARTIST_NAME = new StaticTypeColumn(8, "column_artist_name_title", "column_artist_name_description", false, RE3Properties.getShort("column_width_artist_name"), true);
	static public final StaticTypeColumn COLUMN_DISCOGS_ARTIST_NAME = new StaticTypeColumn(72, "column_discogs_artist_name_title", "column_discogs_artist_name_description", false, RE3Properties.getShort("column_width_artist_name"), true);
	static public final StaticTypeColumn COLUMN_LASTFM_ARTIST_NAME = new StaticTypeColumn(73, "column_lastfm_artist_name_title", "column_lastfm_artist_name_description", false, RE3Properties.getShort("column_width_artist_name"), true);
	static public final StaticTypeColumn COLUMN_MUSICBRAINZ_ID = new StaticTypeColumn(96, "column_musicbrainz_id_title", "column_musicbrainz_id_description", false, RE3Properties.getShort("column_width_artist_name"), true);

    // label columns
	static public final StaticTypeColumn COLUMN_LABEL_NAME = new StaticTypeColumn(9, "column_label_name_title", "column_label_name_description", false, RE3Properties.getShort("column_width_label_name"), true);
	static public final StaticTypeColumn COLUMN_DISCOGS_LABEL_NAME = new StaticTypeColumn(76, "column_discogs_label_name_title", "column_discogs_label_name_description", false, RE3Properties.getShort("column_width_label_name"), true);

	// shared (song + release)
	static public final StaticTypeColumn COLUMN_ARTIST_DESCRIPTION = new StaticTypeColumn(10, "column_artists_description_title", "column_artists_description_description", true, RE3Properties.getShort("column_width_release_artists_description"));
	static public final StaticTypeColumn COLUMN_LABELS = new StaticTypeColumn(11, "column_labels_description_title", "column_labels_description_description", false, RE3Properties.getShort("column_width_release_labels"));
	static public final StaticTypeColumn COLUMN_ORIGINAL_YEAR = new StaticTypeColumn(12, "column_original_year_title", "column_original_year_description", false, RE3Properties.getShort("column_width_release_year"));

	// release columns
	static public final StaticTypeColumn COLUMN_RELEASE_DESCRIPTION = new StaticTypeColumn(13, "column_release_description_title", "column_release_description_description", false, RE3Properties.getShort("column_width_release_description"), true);
	static public final StaticTypeColumn COLUMN_RELEASE_TITLE = new StaticTypeColumn(14, "column_release_title_title", "column_release_title_description", true, RE3Properties.getShort("column_width_release_title"));
	static public final StaticTypeColumn COLUMN_RELEASE_IS_COMPILATION = new StaticTypeColumn(63, "column_compilation_title", "column_compilation_description", true, RE3Properties.getShort("column_width_release_compilation"));

	// song columns
	static public final StaticTypeColumn COLUMN_SONG_DESCRIPTION = new StaticTypeColumn(15, "column_song_description_title", "column_song_description_description", false, RE3Properties.getShort("column_width_song_description"), true);
	static public final StaticTypeColumn COLUMN_RELEASE_TITLES = new StaticTypeColumn(16, "column_release_titles_title", "column_release_titles_description", true, RE3Properties.getShort("column_width_song_release"));
	static public final StaticTypeColumn COLUMN_TRACK = new StaticTypeColumn(61, "column_track_title", "column_track_description", true, RE3Properties.getShort("column_width_song_track"));
	static public final StaticTypeColumn COLUMN_TITLE = new StaticTypeColumn(17, "column_song_title_title", "column_song_title_description", true, RE3Properties.getShort("column_width_song_title"));
	static public final StaticTypeColumn COLUMN_REMIX = new StaticTypeColumn(18, "column_remix_title", "column_remix_description", true, RE3Properties.getShort("column_width_song_remix"));
	static public final StaticTypeColumn COLUMN_DURATION = new StaticTypeColumn(19, "column_duration_title", "column_duration_description", false, RE3Properties.getShort("column_width_song_duration"));
	static public final StaticTypeColumn COLUMN_TIME_SIGNATURE = new StaticTypeColumn(20, "column_time_sig_title", "column_time_sig_description", true, RE3Properties.getShort("column_width_song_time_sig"));
	static public final StaticTypeColumn COLUMN_BPM = new StaticTypeColumn(21, "column_bpm_title", "column_bpm_description", false, RE3Properties.getShort("column_width_song_bpm"));
	static public final StaticTypeColumn COLUMN_BPM_START = new StaticTypeColumn(22, "column_bpm_start_title", "column_bpm_start_description", true, RE3Properties.getShort("column_width_song_bpm_start"));
	static public final StaticTypeColumn COLUMN_BPM_END = new StaticTypeColumn(23, "column_bpm_end_title", "column_bpm_end_description", true, RE3Properties.getShort("column_width_song_bpm_end"));
	static public final StaticTypeColumn COLUMN_BPM_ACCURACY = new StaticTypeColumn(24, "column_bpm_accuracy_title", "column_bpm_accuracy_description", true, RE3Properties.getShort("column_width_song_bpm_accuracy"));
	static public final StaticTypeColumn COLUMN_KEY = new StaticTypeColumn(25, "column_key_title", "column_key_description", false, RE3Properties.getShort("column_width_song_key"));
	static public final StaticTypeColumn COLUMN_KEY_START = new StaticTypeColumn(26, "column_key_start_title", "column_key_start_description", true, RE3Properties.getShort("column_width_song_key_start"));
	static public final StaticTypeColumn COLUMN_KEY_END = new StaticTypeColumn(27, "column_key_end_title", "column_key_end_description", true, RE3Properties.getShort("column_width_song_key_end"));
	static public final StaticTypeColumn COLUMN_KEYCODE = new StaticTypeColumn(28, "column_key_code_title", "column_key_code_description", false, RE3Properties.getShort("column_width_song_key_code"));
	static public final StaticTypeColumn COLUMN_KEYCODE_START = new StaticTypeColumn(29, "column_key_code_start_title", "column_key_code_start_description", true, RE3Properties.getShort("column_width_song_key_code_start"));
	static public final StaticTypeColumn COLUMN_KEYCODE_END = new StaticTypeColumn(30, "column_key_code_end_title", "column_key_code_end_description", true, RE3Properties.getShort("column_width_song_key_code_end"));
	static public final StaticTypeColumn COLUMN_KEY_ACCURACY = new StaticTypeColumn(31, "column_key_accuracy_title", "column_key_accuracy_description", true, RE3Properties.getShort("column_width_song_key_accuracy"));
	static public final StaticTypeColumn COLUMN_BEAT_INTENSITY_DESCRIPTION = new StaticTypeColumn(32, "column_beat_intensity_title", "column_beat_intensity_description", true, RE3Properties.getShort("column_width_song_beat_intensity_description"));
	static public final StaticTypeColumn COLUMN_BEAT_INTENSITY_VALUE = new StaticTypeColumn(33, "column_beat_intensity_value_title", "column_beat_intensity_value_description", false, RE3Properties.getShort("column_width_song_beat_intensity"));
	static public final StaticTypeColumn COLUMN_NUM_PLAYS = new StaticTypeColumn(34, "column_num_plays_title", "column_num_plays_description", false, RE3Properties.getShort("column_width_song_num_plays"));
	static public final StaticTypeColumn COLUMN_FILENAME = new StaticTypeColumn(35, "column_file_name_title", "column_file_name_description", true, RE3Properties.getShort("column_width_song_title"));
	static public final StaticTypeColumn COLUMN_FILEPATH = new StaticTypeColumn(97, "column_file_path_title", "column_file_path_description", true, RE3Properties.getShort("column_width_song_filename"));
	static public final StaticTypeColumn COLUMN_DATE_ADDED = new StaticTypeColumn(36, "column_date_added_title", "column_date_added_description", false, RE3Properties.getShort("column_width_song_date_added"));
	static public final StaticTypeColumn COLUMN_NUM_MIXOUTS = new StaticTypeColumn(37, "column_num_mixouts_title", "column_num_mixouts_description", false, RE3Properties.getShort("column_width_song_num_mixouts"));
	static public final StaticTypeColumn COLUMN_REPLAY_GAIN = new StaticTypeColumn(129, "column_replay_gain_title", "column_replay_gain_description", false, RE3Properties.getShort("column_width_song_num_mixouts"));

	// styles
	static public final StaticTypeColumn COLUMN_STYLE_NAME = new StaticTypeColumn(39, "column_style_name_title", "column_style_name_description", false, RE3Properties.getShort("column_width_style_name"), true);
	static public final StaticTypeColumn COLUMN_STYLE_DESCRIPTION = new StaticTypeColumn(99, "column_style_description_title", "column_style_description_description", false, RE3Properties.getShort("column_width_comments"), true);
	static public final StaticTypeColumn COLUMN_STYLE_CATEGORY_ONLY = new StaticTypeColumn(100, "column_style_category_only_title", "column_style_category_only_description", false, RE3Properties.getShort("column_width_disabled"), true);

	// tags
	static public final StaticTypeColumn COLUMN_TAG_NAME = new StaticTypeColumn(40, "column_tag_name_title", "column_tag_name_description", false, RE3Properties.getShort("column_width_tag_name"), true);
	static public final StaticTypeColumn COLUMN_TAG_CATEGORY_ONLY = new StaticTypeColumn(111, "column_tag_category_only_title", "column_tag_category_only_description", false, RE3Properties.getShort("column_width_disabled"), true);

	// playlist
	static public final StaticTypeColumn COLUMN_PLAYLIST_NAME = new StaticTypeColumn(41, "column_playlist_name_title", "column_playlist_name_description", false, RE3Properties.getShort("column_width_playlist_name"), true);

	// similar
	static public final StaticTypeColumn COLUMN_SIMILARITY = new StaticTypeColumn(42, "column_similarity_title", "column_similarity_description", false, RE3Properties.getShort("column_width_similarity"));

	// filter specific
	static public final StaticTypeColumn COLUMN_NUM_ARTISTS = new StaticTypeColumn(43, "column_num_artists_title", "column_num_artists_description", false, RE3Properties.getShort("column_width_artist_num_songs"));
	static public final StaticTypeColumn COLUMN_NUM_LABELS = new StaticTypeColumn(44, "column_num_labels_title", "column_num_labels_description", false, RE3Properties.getShort("column_width_artist_num_songs"));
	static public final StaticTypeColumn COLUMN_NUM_RELEASES = new StaticTypeColumn(45, "column_num_releases_title", "column_num_releases_description", false, RE3Properties.getShort("column_width_artist_num_songs"));

	// relative song columns
	static public final StaticTypeColumn COLUMN_BPM_SHIFT = new StaticTypeColumn(46, "column_bpm_percent_shift_title", "column_bpm_percent_shift_description", false, RE3Properties.getShort("column_width_bpm_shift"));
	static public final StaticTypeColumn COLUMN_BPM_DIFFERENCE = new StaticTypeColumn(47, "column_bpm_percent_diff_title", "column_bpm_percent_diff_description", false, RE3Properties.getShort("column_width_bpm_difference"));
	static public final StaticTypeColumn COLUMN_ACTUAL_KEY = new StaticTypeColumn(48, "column_key_actual_title", "column_key_actual_description", false, RE3Properties.getShort("column_width_actual_key"));
	static public final StaticTypeColumn COLUMN_ACTUAL_KEYCODE = new StaticTypeColumn(49, "column_key_code_actual_title", "column_key_code_actual_description", false, RE3Properties.getShort("column_width_actual_key_code"));
	static public final StaticTypeColumn COLUMN_KEY_LOCK = new StaticTypeColumn(50, "column_key_lock_title", "column_key_lock_description", false, RE3Properties.getShort("column_width_key_lock"));
	static public final StaticTypeColumn COLUMN_KEY_RELATION = new StaticTypeColumn(51, "column_key_relation_title", "column_key_relation_description", false, RE3Properties.getShort("column_width_key_relation"));
	static public final StaticTypeColumn COLUMN_PITCH_SHIFT = new StaticTypeColumn(52, "column_pitch_shift_title", "column_pitch_shift_description", false, RE3Properties.getShort("column_width_pitch_shift"));
	static public final StaticTypeColumn COLUMN_KEY_CLOSENESS = new StaticTypeColumn(132, "column_key_closeness_title", "column_key_closeness_description", false, RE3Properties.getShort("column_width_key_lock"));

	// degree
	static public final StaticTypeColumn COLUMN_DEGREE = new StaticTypeColumn(53, "column_degree_title", "column_degree_description", false, RE3Properties.getShort("column_width_degree"));
	static public final StaticTypeColumn COLUMN_SOURCE = new StaticTypeColumn(64, "column_source_title", "column_source_description", false, RE3Properties.getShort("column_width_source"));

	// mixouts
	static public final StaticTypeColumn COLUMN_FROM_SONG_DESCRIPTION = new StaticTypeColumn(54, "column_mixout_from_song_description_title", "column_mixout_from_song_description_description", false, RE3Properties.getShort("column_width_song_description"), true);
	static public final StaticTypeColumn COLUMN_TO_SONG_DESCRIPTION = new StaticTypeColumn(55, "column_mixout_to_song_description_title", "column_mixout_to_song_description_description", false, RE3Properties.getShort("column_width_song_description"), true);
	static public final StaticTypeColumn COLUMN_MIXOUT_BPM_DIFF = new StaticTypeColumn(56, "column_mixout_bpm_percent_diff_title", "column_mixout_bpm_percent_diff_description", false, RE3Properties.getShort("column_width_bpm_difference"), true);
	static public final StaticTypeColumn COLUMN_MIXOUT_TYPE = new StaticTypeColumn(57, "column_mixout_type_title", "column_mixout_type_description", false, RE3Properties.getShort("column_width_mixout_type"), true);
    static public final StaticTypeColumn COLUMN_MIXOUT_RATING_STARS = new StaticTypeColumn(58, "column_mixout_rating_title", "column_mixout_rating_description", false, RE3Properties.getShort("column_width_rating"));
    static public final StaticTypeColumn COLUMN_MIXOUT_RATING_VALUE = new StaticTypeColumn(59, "column_mixout_rating_value_title", "column_mixout_rating_value_description", true, RE3Properties.getShort("column_width_rating_value"));
    static public final StaticTypeColumn COLUMN_MIXOUT_COMMENTS = new StaticTypeColumn(60, "column_mixout_comments_title", "column_mixout_comments_description", false, RE3Properties.getShort("column_width_comments"));

    static public final StaticTypeColumn COLUMN_DISABLED = new StaticTypeColumn(62, "column_disabled_title", "column_disabled_description", false, RE3Properties.getShort("column_width_disabled"));

	static public final StaticTypeColumn COLUMN_AVERAGE_BEAT_INTENSITY_VALUE = new StaticTypeColumn(65, "column_beat_intensity_avg_value_title", "column_beat_intensity_avg_value_description", false, RE3Properties.getShort("column_width_song_beat_intensity"));
	static public final StaticTypeColumn COLUMN_BEAT_INTENSITY_VARIANCE_VALUE = new StaticTypeColumn(66, "column_beat_intensity_variance_value_title", "column_beat_intensity_variance_value_description", false, RE3Properties.getShort("column_width_song_beat_intensity"));
	static public final StaticTypeColumn COLUMN_AVERAGE_BEAT_INTENSITY_DESCRIPTION = new StaticTypeColumn(67, "column_beat_intensity_avg_title", "column_beat_intensity_avg_description", true, RE3Properties.getShort("column_width_song_beat_intensity_description"));
	static public final StaticTypeColumn COLUMN_BEAT_INTENSITY_VARIANCE_DESCRIPTION = new StaticTypeColumn(68, "column_beat_intensity_variance_title", "column_beat_intensity_variance_description", false, RE3Properties.getShort("column_width_song_beat_intensity_description"));

	static public final StaticTypeColumn COLUMN_TAGS_MATCH = new StaticTypeColumn(69, "column_tags_match_title", "column_tags_match_description", false, RE3Properties.getShort("column_width_degree"));
	static public final StaticTypeColumn COLUMN_STYLES_MATCH = new StaticTypeColumn(70, "column_styles_match_title", "column_styles_match_description", false, RE3Properties.getShort("column_width_degree"));
	static public final StaticTypeColumn COLUMN_FILTERS_MATCH = new StaticTypeColumn(71, "column_filters_match_title", "column_filters_match_description", false, RE3Properties.getShort("column_width_degree"));

	static public final StaticTypeColumn COLUMN_SCORE = new StaticTypeColumn(74, "column_score_title", "column_score_description", false, RE3Properties.getShort("column_width_score"));
	static public final StaticTypeColumn COLUMN_POPULARITY = new StaticTypeColumn(75, "column_popularity_title", "column_popularity_description", false, RE3Properties.getShort("column_width_popularity"));

	static public final StaticTypeColumn COLUMN_LINK_TITLE = new StaticTypeColumn(77, "column_link_title_title", "column_link_title_description", false, RE3Properties.getShort("column_width_link_title"));
	static public final StaticTypeColumn COLUMN_LINK_DESCRIPTION = new StaticTypeColumn(78, "column_link_description_title", "column_link_description_description", false, RE3Properties.getShort("column_width_link_description"));
	static public final StaticTypeColumn COLUMN_LINK_TYPE = new StaticTypeColumn(79, "column_link_type_title", "column_link_type_description", false, RE3Properties.getShort("column_width_link_type"));
	static public final StaticTypeColumn COLUMN_LINK_URL = new StaticTypeColumn(80, "column_link_url_title", "column_link_url_description", false, RE3Properties.getShort("column_width_link_url"));
	static public final StaticTypeColumn COLUMN_LINK_SOURCE = new StaticTypeColumn(81, "column_link_source_title", "column_link_source_description", false, RE3Properties.getShort("column_width_source"));

	static public final StaticTypeColumn COLUMN_ARTIST_REAL_NAME = new StaticTypeColumn(82, "column_artist_real_names_title", "column_artist_real_names_description", false, RE3Properties.getShort("column_width_artist_name"));
	static public final StaticTypeColumn COLUMN_ARTIST_NAME_VARIATIONS = new StaticTypeColumn(83, "column_artist_name_variations_title", "column_artist_name_variations_description", false, RE3Properties.getShort("column_width_artist_name"));
	static public final StaticTypeColumn COLUMN_ARTIST_ALIASES = new StaticTypeColumn(84, "column_artist_aliases_title", "column_artist_aliases_description", false, RE3Properties.getShort("column_width_artist_name"));
	static public final StaticTypeColumn COLUMN_ARTIST_TYPE = new StaticTypeColumn(85, "column_artist_type_title", "column_artist_type_description", false, RE3Properties.getShort("column_width_artist_name"));

	static public final StaticTypeColumn COLUMN_LIFESPAN_BEGIN = new StaticTypeColumn(86, "column_lifespan_begin_title", "column_lifespan_begin_description", false, RE3Properties.getShort("column_width_artist_name"));
	static public final StaticTypeColumn COLUMN_LIFESPAN_END = new StaticTypeColumn(87, "column_lifespan_end_title", "column_lifespan_end_description", false, RE3Properties.getShort("column_width_artist_name"));

	static public final StaticTypeColumn COLUMN_LABEL_CONTACT_INFO = new StaticTypeColumn(88, "column_label_contact_info_title", "column_label_contact_info_description", false, RE3Properties.getShort("column_width_label_name"));
	static public final StaticTypeColumn COLUMN_LABEL_PARENT_LABELS = new StaticTypeColumn(89, "column_label_parent_label_title", "column_label_parent_label_description", false, RE3Properties.getShort("column_width_label_name"));
	static public final StaticTypeColumn COLUMN_LABEL_SUB_LABELS = new StaticTypeColumn(90, "column_label_sub_labels_title", "column_label_sub_labels_description", false, RE3Properties.getShort("column_width_label_name"));
	static public final StaticTypeColumn COLUMN_LABEL_NAME_VARIATIONS = new StaticTypeColumn(91, "column_label_name_variations_title", "column_label_name_variations_description", false, RE3Properties.getShort("column_width_label_name"));
	static public final StaticTypeColumn COLUMN_LABEL_TYPE = new StaticTypeColumn(92, "column_label_type_title", "column_label_type_description", false, RE3Properties.getShort("column_width_label_name"));
	static public final StaticTypeColumn COLUMN_LABEL_CODE = new StaticTypeColumn(93, "column_label_code_title", "column_label_code_description", false, RE3Properties.getShort("column_width_label_name"));
	static public final StaticTypeColumn COLUMN_LABEL_COUNTRY = new StaticTypeColumn(94, "column_label_country_title", "column_label_country_description", false, RE3Properties.getShort("column_width_label_name"));

	static public final StaticTypeColumn COLUMN_RELEASE_TYPE = new StaticTypeColumn(95, "column_release_type_title", "column_release_type_description", false, RE3Properties.getShort("column_width_release_description"));

	static public final StaticTypeColumn COLUMN_PREFERENCE = new StaticTypeColumn(98, "column_preference_title", "column_preference_description", false, RE3Properties.getShort("column_width_score"));

	static public final StaticTypeColumn COLUMN_PLAYLIST_POSITION = new StaticTypeColumn(101, "column_playlist_position_title", "column_playlist_position_description", false, RE3Properties.getShort("column_width_song_position"));

	// options columns
	static public final StaticTypeColumn COLUMN_SETTING_NAME = new StaticTypeColumn(102, "column_setting_name_title", "column_setting_name_description", false, RE3Properties.getShort("column_width_setting_name"));
	static public final StaticTypeColumn COLUMN_SETTING_VALUE = new StaticTypeColumn(103, "column_setting_value_title", "column_setting_value_description", false, RE3Properties.getShort("column_width_setting_value"));
	static public final StaticTypeColumn COLUMN_SETTING_ACTION = new StaticTypeColumn(104, "column_setting_action_title", "column_setting_action_description", false, RE3Properties.getShort("column_width_setting_action"));
	static public final StaticTypeColumn COLUMN_SETTING_DESCRIPTION = new StaticTypeColumn(105, "column_setting_description_title", "column_setting_description_description", false, RE3Properties.getShort("column_width_setting_description"));
	static public final StaticTypeColumn COLUMN_SETTING_ID = new StaticTypeColumn(106, "column_setting_id_title", "column_setting_id_description", false, (short)0);
	static public final StaticTypeColumn COLUMN_SETTING_TYPE = new StaticTypeColumn(107, "column_setting_type_title", "column_setting_type_description", false, (short)0);

	static public final StaticTypeColumn COLUMN_EXTERNAL_ITEM = new StaticTypeColumn(108, "column_external_item_title", "column_external_item_description", false, RE3Properties.getShort("column_width_disabled"));

	static public final StaticTypeColumn COLUMN_UNIQUE_ID = new StaticTypeColumn(109, "column_unique_id_title", "column_unique_id_description", false, RE3Properties.getShort("column_width_song_duration"));
	static public final StaticTypeColumn COLUMN_DUPLICATE_IDS = new StaticTypeColumn(110, "column_duplicate_ids_title", "column_duplicate_ids_description", false, RE3Properties.getShort("column_width_song_duration"));

	static public final StaticTypeColumn COLUMN_HAS_LASTFM_PROFILE = new StaticTypeColumn(112, "column_has_lastfm_profile_title", "column_has_lastfm_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));
	static public final StaticTypeColumn COLUMN_HAS_ECHONEST_PROFILE = new StaticTypeColumn(113, "column_has_echonest_profile_title", "column_has_echonest_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));
	static public final StaticTypeColumn COLUMN_HAS_IDIOMAG_PROFILE = new StaticTypeColumn(114, "column_has_idiomag_profile_title", "column_has_idiomag_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));
	static public final StaticTypeColumn COLUMN_HAS_MUSICBRAINZ_PROFILE = new StaticTypeColumn(115, "column_has_musicbrainz_profile_title", "column_has_musicbrainz_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));
	static public final StaticTypeColumn COLUMN_HAS_BBC_PROFILE = new StaticTypeColumn(116, "column_has_bbc_profile_title", "column_has_bbc_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));
	static public final StaticTypeColumn COLUMN_HAS_BILLBOARD_PROFILE = new StaticTypeColumn(117, "column_has_billboard_profile_title", "column_has_billboard_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));
	static public final StaticTypeColumn COLUMN_HAS_DISCOGS_PROFILE = new StaticTypeColumn(118, "column_has_discogs_profile_title", "column_has_discogs_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));
	static public final StaticTypeColumn COLUMN_HAS_GIGJUNKIE_PROFILE = new StaticTypeColumn(119, "column_has_gigjunkie_profile_title", "column_has_gigjunkie_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));
	static public final StaticTypeColumn COLUMN_HAS_LYRICSFLY_PROFILE = new StaticTypeColumn(120, "column_has_lyricsfly_profile_title", "column_has_lyricsfly_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));
	static public final StaticTypeColumn COLUMN_HAS_LYRICWIKI_PROFILE = new StaticTypeColumn(121, "column_has_lyricwiki_profile_title", "column_has_lyricwiki_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));
	static public final StaticTypeColumn COLUMN_HAS_MIXSHARE_PROFILE = new StaticTypeColumn(122, "column_has_mixshare_profile_title", "column_has_mixshare_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));
	static public final StaticTypeColumn COLUMN_HAS_YAHOO_PROFILE = new StaticTypeColumn(123, "column_has_yahoo_profile_title", "column_has_yahoo_profile_description", false, RE3Properties.getShort("column_width_release_compilation"));

	static public final StaticTypeColumn COLUMN_FEATURING_ARTISTS = new StaticTypeColumn(124, "column_featuring_artists_title", "column_featuring_artists_description", false, RE3Properties.getShort("column_width_artist_name"));

	static public final StaticTypeColumn COLUMN_NAME = new StaticTypeColumn(130, "column_name_title", "column_name_description", false, RE3Properties.getShort("column_width_song_description"));
	static public final StaticTypeColumn COLUMN_TYPE = new StaticTypeColumn(131, "column_type_title", "column_type_description", false, RE3Properties.getShort("column_width_song_key"));

	// note: highest column @132 (i.e. next should be 133)

}
