package com.mixshare.rapid_evolution.audio.tags;


public interface TagConstants {

    public static int TAG_MODE_OVERWRITE = 0;
    public static int TAG_MODE_UPDATE = 1;
    
    public static int ID3_V_2_4 = 0;
    public static int ID3_V_2_3 = 1;
    public static int ID3_V_2_2 = 2;
    
    // id3 v2 frames
    public static String FRAME_ALBUM = "TALB";
    public static String FRAME_ALBUM_COVER = "APIC";
    public static String FRAME_BPM = "TBPM";
    public static String FRAME_COMMENTS = "COMM";
    public static String FRAME_COMPOSER = "TCOM";
    public static String FRAME_CONTENT_GROUP_DESCRIPTION = "TIT1";
    public static String FRAME_CONTENT_TYPE = "TCON";
    public static String FRAME_ENCODED_BY = "TENC";
    public static String FRAME_FILE_TYPE = "TFLT";
    public static String FRAME_GENRE = "TCON";
    public static String FRAME_GENERAL = "GEOB";
    public static String FRAME_KEY = "TKEY";    
    public static String FRAME_LANGUAGES = "TLAN";
    public static String FRAME_LYRICS = "SYLT";
    public static String FRAME_LEAD_PERFORMER = "TPE1";
    public static String FRAME_LENGTH = "TLEN";
    public static String FRAME_ORIGINAL_PERFORMER = "TOPE";
    public static String FRAME_PUBLISHER = "TPUB";
    public static String FRAME_REMIXER = "TPE4";
    public static String FRAME_REPLAYGAIN = "RVA2";
    public static String FRAME_REPLAY_VOLUME_ADJUST = "RVAD";
    public static String FRAME_SIZE = "TSIZ";
    public static String FRAME_SUBTITLE = "TIT3";
    public static String FRAME_TIME = "TIME";
    public static String FRAME_TITLE = "TIT2";
    public static String FRAME_TRACK = "TRCK";   
    public static String FRAME_TXXX = "TXXX";
    public static String FRAME_YEAR = "TYER";
        
    // custom id3 tag ids
    public static String TXXX_BEAT_INTENSITY = "beat_intensity";
    public static String TXXX_BPM = "bpm";
    public static String TXXX_BPM_ACCURACY = "bpm_accuracy";    
    public static String TXXX_BPM_START = "bpm_start";    
    public static String TXXX_BPM_END = "bpm_end";
    public static String TXXX_BPM_FINALSCRATCH = "fBPM";    
    public static String TXXX_CATALOG_ID = "catalogid";
    public static String TXXX_COMMENT = "comment";
    public static String TXXX_CUE_LABEL_ = "cue_label_";
    public static String TXXX_CUE_TIME_ = "cue_time_";
    public static String TXXX_CUE_TYPE_ = "cue_type_";
    public static String TXXX_DATE = "date";
    public static String TXXX_DISCOGSID = "discogsid";
    public static String TXXX_GENRE = "genre";
    public static String TXXX_IMAGE_COUNT = "image_count";
    public static String TXXX_KEY = "key";
    public static String TXXX_INITIALKEY = "initialkey";
    public static String TXXX_KEY_ACCURACY = "key_accuracy";
    public static String TXXX_KEY_INITIAL = "initialkey";
    public static String TXXX_KEY_START = "key_start";
    public static String TXXX_KEY_END = "key_end";
    public static String TXXX_KEY_FINALSCRATCH = "fKEY";
    public static String TXXX_LABEL = "label";
    public static String TXXX_NUM_CUEPOINTS = "num_cuepoints";
    public static String TXXX_POSITION = "position";
    public static String TXXX_RATING = "rating";
    public static String TXXX_REMIX = "remix";
    public static String TXXX_REPLAYGAIN_TRACK_GAIN = "replaygain_track_gain";
    public static String TXXX_REPLAYGAIN_TRACK_PEAK = "replaygain_track_peak";    
    public static String TXXX_STRIPE = "waveform";
    public static String TXXX_STYLES_PREFIX = "style_";
    public static String TXXX_TAGS_PREFIX = "tag_";
    public static String TXXX_TEMPO = "tempo";
    public static String TXXX_TIME = "time";
    public static String TXXX_TIME_SIGNATURE = "time_signature";
    public static String TXXX_TRACK_TOTAL = "tracktotal";
    
    // used in ogg/flac tag ids
    public static String TAG_CONTENT_GROUP_DESCRIPTION = "content_group_description";
    public static String TAG_CONTENT_TYPE = "content_type";
    public static String TAG_COVER_ART = "cover_art";
    public static String TAG_COVER_ART_MIME = "cover_art_mime";
    public static String TAG_ENCODED_BY = "encoded_by";
    public static String TAG_FILE_TYPE = "file_type";
    public static String TAG_LANGUAGES = "languages";
    public static String TAG_PUBLISHER = "publisher";
    public static String TAG_REMIX = "remix";
    public static String TAG_SIZE_IN_BYTES = "size_in_bytes";
    public static String TAG_COMMENTS = "comments";
        
}
