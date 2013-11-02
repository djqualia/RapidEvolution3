package com.mixshare.rapid_evolution.workflow.importers.re2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.exceptions.AlreadyExistsException;
import com.mixshare.rapid_evolution.data.exceptions.InsufficientInformationException;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.Exclude;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.record.user.UserData;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.data.submitted.filter.style.SubmittedStyle;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedMixout;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.music.timesig.TimeSig;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.ui.model.profile.ProfileStyleModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.ProfileTagModelManager;
import com.mixshare.rapid_evolution.ui.util.FileNameSelectThread;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.readers.PlainTextLineReader;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class RE2DatabaseImporterTask extends CommonTask implements DataConstants {
	
    static private Logger log = Logger.getLogger(RE2DatabaseImporterTask.class);
    static private final long serialVersionUID = 0L;    	

    static private final int TEST_MAX_IMPORT_LIMIT = RE3Properties.getInt("re2_max_songs_to_import");
    static private final float DEFAULT_STYLE_DEGREE = 1.0f; // should this be 0.5f instead?
    
    static private final int USER_FIELD_TYPE_NONE = 0;
    static private final int USER_FIELD_TYPE_CATALOG_ID = 1;
    static private final int USER_FIELD_TYPE_GROUP = 2;
    static private final int USER_FIELD_TYPE_CONTENT_TYPE = 3;
    static private final int USER_FIELD_TYPE_ENCODED_BY = 4;
    static private final int USER_FIELD_TYPE_FILE_TYPE = 5;
    static private final int USER_FIELD_TYPE_LABEL = 6;
    static private final int USER_FIELD_TYPE_LANGUAGES = 7;
    static private final int USER_FIELD_TYPE_BYTE_SIZE = 8;
    static private final int USER_FIELD_TYPE_YEAR = 9;

    static private int songsImported = 0;
    static private int NUM_STYLES;
    static private Vector<RE2Style> styles = new Vector<RE2Style>();
        
    private boolean silentMode = true;
    
    public RE2DatabaseImporterTask() { }
    public RE2DatabaseImporterTask(boolean silentMode) {
    	this.silentMode = silentMode;
    }
    
    public String toString() { return "Importing From RE2"; }
    
    public void execute() {
        try {
        	Map<Long, Identifier> uniqueSongIdMap = new HashMap<Long, Identifier>();
        	// load skin.dat
        	Map<String, String> skinDatMap = new HashMap<String, String>();
        	Map<String, Vector<String>> albumCoverMap = new HashMap<String, Vector<String>>();
        	LineReader lineReader = new PlainTextLineReader(OSHelper.getWorkingDirectory("Rapid Evolution 2") + "/skin.dat");
        	String line = lineReader.getNextLine();
        	while (line != null) {
        		String id = line;
        		String value = lineReader.getNextLine();
        		skinDatMap.put(id, value);
        		line = lineReader.getNextLine();
        	}
        	lineReader.close();
        	String userLabel1 = null;
        	String userLabel2 = null;
        	String userLabel3 = null;
        	String userLabel4 = null;
        	// load music_database.xml
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = null;
            String xmlFilename = OSHelper.getWorkingDirectory("Rapid Evolution 2") + "/music_database.xml";
            File test = new File(xmlFilename);
            if (!test.exists() && !silentMode) {
        		FileNameSelectThread filenameSelected = new FileNameSelectThread(Translations.get("import_re2_filter_text"), "(*.xml)");        		
        		QApplication.invokeAndWait(filenameSelected);
        		if (filenameSelected.getFilename() != null) {
        			xmlFilename = filenameSelected.getFilename();
        	        if (log.isTraceEnabled())
        	        	log.trace("execute(): selected filename=" + xmlFilename);
        	    }        		            	
            }
            try {
                document = builder.parse(new File(xmlFilename).toURI().toString());
            } catch (java.io.FileNotFoundException fnfe) {
            	if (log.isDebugEnabled())
            		log.debug("execute(): music_database.xml not found");
                return;
            }
            if (!silentMode)
            	if (RE3Properties.getBoolean("show_progress_bars"))
            		QApplication.invokeLater(new TaskProgressLauncher(this));        	            	
            Element elem = document.getDocumentElement();
            float version = Float.parseFloat(elem.getAttribute("version"));
            NodeList rootnodes = elem.getChildNodes();
            for (int rootnode_iter = 0; rootnode_iter < rootnodes.getLength(); ++rootnode_iter) {
                Node rootnode = rootnodes.item(rootnode_iter);
                if (rootnode.getNodeName().equalsIgnoreCase("config")) {
                    NodeList confignodes = rootnode.getChildNodes();
                    for (int confignode_iter = 0; confignode_iter < confignodes.getLength(); ++confignode_iter) {
                        Node confignode = confignodes.item(confignode_iter);
                        if (confignode.getNodeName().equalsIgnoreCase("settings")) {
                            NodeList settings = confignode.getChildNodes();
                            int searchHistoryCount = 1;
                            for (int setting_iter = 0; setting_iter < settings.getLength(); ++setting_iter) {
                                Node setting = settings.item(setting_iter);                                
	                            if (setting.getNodeName().equalsIgnoreCase("custom_field_1_label")) {
	                            	userLabel1 = getFirstValue(setting);
	                            } else if (setting.getNodeName().equalsIgnoreCase("custom_field_2_label")) {
	                            	userLabel2 = getFirstValue(setting);
	                            } else if (setting.getNodeName().equalsIgnoreCase("custom_field_3_label")) {
	                            	userLabel3 = getFirstValue(setting);
	                            } else if (setting.getNodeName().equalsIgnoreCase("custom_field_4_label")) {
	                            	userLabel4 = getFirstValue(setting);
	                            }                                
                                /*
                                if (setting.getNodeName().equalsIgnoreCase("sync_needed")) {
                                	RapidEvolution.getMixshareClient().setOutOfSync(false);
                                    if (getFirstValue(setting).equalsIgnoreCase("yes"))
                                        RapidEvolution.getMixshareClient().setOutOfSync(true);
                                } else if (setting.getNodeName().equalsIgnoreCase("sync_in_progress")) {
                                    RapidEvolution.getMixshareClient().setIsSyncing(false);
                                    if (getFirstValue(setting).equalsIgnoreCase("yes"))
                                        RapidEvolution.getMixshareClient().setIsSyncing(true);
                                } else if (setting.getNodeName().equalsIgnoreCase("id3_tag_library")) {
                                    OptionsUI.instance.id3writer.setSelectedIndex(0);
                                    OptionsUI.instance.id3reader.setSelectedIndex(0);
                                } else if (setting.getNodeName().equalsIgnoreCase("root_music_directory")) {
                                	OptionsUI.instance.rootMusicDirectory.setText(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("tag_library_writer")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.id3writer.setSelectedIndex(value);
                                } else if (setting.getNodeName().equalsIgnoreCase("tag_library_reader")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.id3reader.setSelectedIndex(value);
                                } else if (setting.getNodeName().equalsIgnoreCase("key_format")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.keyformatcombo.setSelectedIndex(value);
                                } else if (setting.getNodeName().equalsIgnoreCase("rename_file_pattern")) {
                                    RenameFilesUI.instance.renamepatternfield.setText(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_1_tag")) {
                                    OptionsUI.instance.custom_field_1_tag_combo.setSelectedItem(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_2_tag")) {
                                    OptionsUI.instance.custom_field_2_tag_combo.setSelectedItem(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_3_tag")) {
                                    OptionsUI.instance.custom_field_3_tag_combo.setSelectedItem(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_4_tag")) {
                                    OptionsUI.instance.custom_field_4_tag_combo.setSelectedItem(getFirstValue(setting));                                    
                                } else if (setting.getNodeName().equalsIgnoreCase("style_require_shortcut")) {
                                    OptionsUI.instance.style_require_shortcut_combobox.setSelectedItem(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("style_exclude_shortcut")) {
                                    OptionsUI.instance.style_exclude_shortcut_combobox.setSelectedItem(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("bpm_slider_tickmark")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    RapidEvolutionUI.instance.bpmslider_tickmark_index = value;                                    
                                } else if (setting.getNodeName().equalsIgnoreCase("bpm_slider_range")) {
                                    SearchPane.bpmrangespinner.getModel().setValue(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("os_media_player_path")) {
                                    OptionsUI.OSMediaPlayer = getFirstValue(setting);
                                } else if (setting.getNodeName().equalsIgnoreCase("time_pitch_shift_quality")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.timepitchshiftquality.setValue(value);
                                } else if (setting.getNodeName().equalsIgnoreCase("audio_processing_cpu_utilization")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.cpuutilization.setValue(value);
                                } else if (setting.getNodeName().equalsIgnoreCase("bpm_detection_quality")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.bpmdetectionquality.setValue(value);
                                } else if (setting.getNodeName().equalsIgnoreCase("key_detection_quality")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.keydetectionquality.setValue(value);
                                } else if (setting.getNodeName().equalsIgnoreCase("bpm_detection_min_bpm")) {
                                    Bpm.minbpm = Double.parseDouble(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("bpm_detection_max_bpm")) {
                                    Bpm.maxbpm = Double.parseDouble(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("album_cover_thumbnail_size")) {
                                    myImageIcon.icon_size = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.albumcoverthumbwidth.setText(String.valueOf(myImageIcon.icon_size));
                                } else if (setting.getNodeName().equalsIgnoreCase("piano_octaves")) {
                                    Integer octaves = new Integer(Integer.parseInt(getFirstValue(setting)));
                                    ((SpinnerNumberModel)MIDIPiano.instance.octavesspinner).setValue(octaves);
                                    MIDIPiano.instance.setkeyboardshift(octaves.intValue());
                                    MIDIPiano.instance.num_keys = 12 * octaves.intValue();
                                    MIDIPiano.instance.keyspressed = new boolean[MIDIPiano.instance.num_keys];
                                    MIDIPiano.instance.keyspressed2 = new int[MIDIPiano.instance.num_keys];
                                    MIDIPiano.instance.masterkeyspressed = new int[MIDIPiano.instance.num_keys];
                                    for (int i = 0; i < MIDIPiano.instance.num_keys; ++i) {
                                      MIDIPiano.instance.keyspressed[i] = false;
                                      MIDIPiano.instance.keyspressed2[i] = 0;
                                      MIDIPiano.instance.masterkeyspressed[i] = 0;
                                    }                                    
                                } else if (setting.getNodeName().equalsIgnoreCase("piano_shift")) {
                                    ((SpinnerNumberModel)MIDIPiano.instance.shiftspinner).setValue(new Integer(Integer.parseInt(getFirstValue(setting))));
                                } else if (setting.getNodeName().equalsIgnoreCase("training_speed")) {
                                    ((SpinnerNumberModel)MIDIPiano.instance.speedspinner).setValue(new Integer(Integer.parseInt(getFirstValue(setting))));
                                } else if (setting.getNodeName().equalsIgnoreCase("piano_midi_device")) {
                                    MIDIPiano.instance.initialcombovalue = -1;
                                    MIDIPiano.instance.initialmidivalue = getFirstValue(setting);
                                } else if (setting.getNodeName().equalsIgnoreCase("piano_row_1_chord_type")) {
                                    OptionsUI.keychordtype1 = Integer.parseInt(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("piano_row_2_chord_type")) {
                                    OptionsUI.keychordtype2 = Integer.parseInt(getFirstValue(setting));
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_1_label")) {
                                    String label = getFirstValue(setting);
                                    if (label.equalsIgnoreCase(SkinManager.instance.getMessageText("column_title_rating")))
                                        label = "old_" + label;
                                    OptionsUI.instance.customfieldtext1.setText(label);
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_2_label")) {
                                    String label = getFirstValue(setting);
                                    if (label.equalsIgnoreCase(SkinManager.instance.getMessageText("column_title_rating")))
                                        label = "old_" + label;
                                    OptionsUI.instance.customfieldtext2.setText(label);
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_3_label")) {
                                    String label = getFirstValue(setting);
                                    if (label.equalsIgnoreCase(SkinManager.instance.getMessageText("column_title_rating")))
                                        label = "old_" + label;
                                    OptionsUI.instance.customfieldtext3.setText(label);
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_4_label")) {
                                    String label = getFirstValue(setting);
                                    if (label.equalsIgnoreCase(SkinManager.instance.getMessageText("column_title_rating")))
                                        label = "old_" + label;
                                    OptionsUI.instance.customfieldtext4.setText(label);
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_1_sort_as")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.custom1sortas.setSelectedIndex(value);
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_2_sort_as")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.custom2sortas.setSelectedIndex(value);
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_3_sort_as")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.custom3sortas.setSelectedIndex(value);
                                } else if (setting.getNodeName().equalsIgnoreCase("custom_field_4_sort_as")) {
                                    int value = Integer.parseInt(getFirstValue(setting));
                                    OptionsUI.instance.custom4sortas.setSelectedIndex(value);
                                } else if (setting.getNodeName().equalsIgnoreCase("options_song_filter_1_combo")) {                                    
                                    OptionsUI.instance.filter1preset = getFirstValue(setting);
                                } else if (setting.getNodeName().equalsIgnoreCase("options_song_filter_2_combo")) {                                    
                                    OptionsUI.instance.filter2preset = getFirstValue(setting);
                                } else if (setting.getNodeName().equalsIgnoreCase("options_song_filter_3_combo")) {                                    
                                    OptionsUI.instance.filter3preset = getFirstValue(setting);
                                } else if (setting.getNodeName().equalsIgnoreCase("options_organized_directory")) {
                                    rapid_evolution.ui.OrganizeFilesUI.organizedPersistDir = getFirstValue(setting);
                                } else if (setting.getNodeName().equalsIgnoreCase("options_organized_backup_directory")) {
                                    rapid_evolution.ui.OrganizeFilesUI.organizedBackupPersistDir = getFirstValue(setting);
                                } else if (setting.getNodeName().equalsIgnoreCase("search_history_" + searchHistoryCount)) {
                                    SearchPane.instance.searchhistorybutton.getPreviousSearches().add(getFirstValue(setting));
                                    ++searchHistoryCount;
                                }
                                */                                    
                            }
                        } else if (confignode.getNodeName().equalsIgnoreCase("columns")) {
                            int total = Integer.parseInt(confignode.getAttributes().getNamedItem("total").getNodeValue());
                            NodeList column_nodelist = confignode.getChildNodes();
                            for (int columnnode_iter = 0; columnnode_iter < column_nodelist.getLength(); ++columnnode_iter) {
                                Node columnnode = column_nodelist.item(columnnode_iter);
                                if (columnnode.getNodeName().equalsIgnoreCase("default_order")) {
                                    NodeList order_nodelist = columnnode.getChildNodes();
                                    for (int order_iter = 0; order_iter < order_nodelist.getLength(); ++order_iter) {
                                        Node order = order_nodelist.item(order_iter);
                                        if (order.getNodeName().equalsIgnoreCase("search")) {
                                            int num_columns = Integer.parseInt(order.getAttributes().getNamedItem("num_columns").getNodeValue());
                                            NodeList searchindices = order.getChildNodes();                                            
                                            int[] columnindices = new int[num_columns];
                                            int cindex = 0;
                                            for (int c = 0; c < searchindices.getLength(); ++c) {
                                                Node searchindex = searchindices.item(c);
                                                if (searchindex.getNodeName().equalsIgnoreCase("index")) {
                                                    columnindices[cindex++] = Integer.parseInt(getFirstValue(searchindex));
                                                }                                                
                                            }
                                            //if (columnindices.length == SearchPane.instance.defaultColumnIndices.length) SearchPane.instance.defaultColumnIndices = columnindices;
                                            //else SearchPane.instance.defaultColumnIndices = SearchPane.instance.determineOrdering(SearchPane.instance.defaultColumnIndices, columnindices, false);
                                        } else if (order.getNodeName().equalsIgnoreCase("mixouts")) {
                                            int num_columns = Integer.parseInt(order.getAttributes().getNamedItem("num_columns").getNodeValue());
                                            NodeList mixoutsindices = order.getChildNodes();
                                            int[] columnindices = new int[num_columns];
                                            int cindex = 0;
                                            for (int c = 0; c < mixoutsindices.getLength(); ++c) {
                                                Node mixoutindex = mixoutsindices.item(c);
                                                if (mixoutindex.getNodeName().equalsIgnoreCase("index")) {
                                                    columnindices[cindex++] = Integer.parseInt(getFirstValue(mixoutindex));
                                                }
                                            }
                                            //if (columnindices.length == MixoutPane.instance.defaultColumnIndices.length) MixoutPane.instance.defaultColumnIndices = columnindices;
                                            //else MixoutPane.instance.defaultColumnIndices = MixoutPane.instance.determineOrdering(MixoutPane.instance.defaultColumnIndices, columnindices, false);
                                        }
                                    }                                    
                                } else if (columnnode.getNodeName().equalsIgnoreCase("column")) {
                                    int columnid = Integer.parseInt(columnnode.getAttributes().getNamedItem("id").getNodeValue());
                                    NodeList table_nodes = columnnode.getChildNodes();
                                    for (int t = 0; t < table_nodes.getLength(); ++t) {
                                        Node tablenode = table_nodes.item(t);
                                        /*
                                        if (tablenode.getNodeName().equalsIgnoreCase("search_width")) {
                                            SearchPane.instance.searchcolumnconfig.setDefaultWidth(columnid, Integer.parseInt(getFirstValue(tablenode)));
                                        } else if (tablenode.getNodeName().equalsIgnoreCase("mixouts_width")) {
                                            MixoutPane.instance.mixoutcolumnconfig.setDefaultWidth(columnid, Integer.parseInt(getFirstValue(tablenode)));
                                        } else if (tablenode.getNodeName().equalsIgnoreCase("excludes_width")) {
                                            ExcludeUI.instance.excludecolumnconfig.setDefaultWidth(columnid, Integer.parseInt(getFirstValue(tablenode)));
                                        } else if (tablenode.getNodeName().equalsIgnoreCase("roots_width")) {
                                            RootsUI.instance.rootscolumnconfig.setDefaultWidth(columnid, Integer.parseInt(getFirstValue(tablenode)));
                                        } else if (tablenode.getNodeName().equalsIgnoreCase("suggested_mixouts_width")) {
                                            SuggestedMixesUI.instance.suggestedcolumnconfig.setDefaultWidth(columnid, Integer.parseInt(getFirstValue(tablenode)));
                                        } else if (tablenode.getNodeName().equalsIgnoreCase("sync_width")) {
                                            SyncUI.instance.synccolumnconfig.setDefaultWidth(columnid, Integer.parseInt(getFirstValue(tablenode)));
                                        } else if (tablenode.getNodeName().equalsIgnoreCase("add_song_query_width")) {
                                            AddMatchQueryUI.instance.matchcolumnconfig.setDefaultWidth(columnid, Integer.parseInt(getFirstValue(tablenode)));
                                        } else if (tablenode.getNodeName().equalsIgnoreCase("edit_song_query_width")) {
                                            EditMatchQueryUI.instance.matchcolumnconfig2.setDefaultWidth(columnid, Integer.parseInt(getFirstValue(tablenode)));
                                        }
                                        */
                                    }
                                    
                                    
                                }
                            }
                        } else if (confignode.getNodeName().equalsIgnoreCase("tables")) {
                            NodeList tablenodes = confignode.getChildNodes();
                            for (int table_iter = 0; table_iter < tablenodes.getLength(); ++table_iter) {
                                Node tablenode = tablenodes.item(table_iter);
                                if (tablenode.getNodeName().equalsIgnoreCase("table")) {
                                    String name = tablenode.getAttributes().getNamedItem("name").getNodeValue();                                
                                    int num_columns = Integer.parseInt(tablenode.getAttributes().getNamedItem("num_columns").getNodeValue());
                                    /*
                                    ColumnConfig columnconfig = null;
                                    if (name.equalsIgnoreCase("search")) columnconfig = SearchPane.instance.searchcolumnconfig;
                                    else if (name.equalsIgnoreCase("mixouts")) columnconfig = MixoutPane.instance.mixoutcolumnconfig;
                                    else if (name.equalsIgnoreCase("excludes")) columnconfig = ExcludeUI.instance.excludecolumnconfig;
                                    else if (name.equalsIgnoreCase("roots")) columnconfig = RootsUI.instance.rootscolumnconfig;
                                    else if (name.equalsIgnoreCase("suggested_mixouts")) columnconfig = SuggestedMixesUI.instance.suggestedcolumnconfig;
                                    else if (name.equalsIgnoreCase("sync")) columnconfig = SyncUI.instance.synccolumnconfig;
                                    else if (name.equalsIgnoreCase("add_song_query")) columnconfig = AddMatchQueryUI.instance.matchcolumnconfig;
                                    else if (name.equalsIgnoreCase("edit_song_query")) columnconfig = EditMatchQueryUI.instance.matchcolumnconfig2;                                
                                    columnconfig.num_columns = num_columns;
                                    columnconfig.columnindex = new int[columnconfig.num_columns];
                                    columnconfig.columntitles = new String[columnconfig.num_columns];
                                    columnconfig.setPreferredWidth(new int[columnconfig.num_columns]);                                                                    
                                    try {
                                        columnconfig.primary_sort_column = Integer.parseInt(tablenode.getAttributes().getNamedItem("primary_sort_column").getNodeValue());
                                        columnconfig.primary_sort_ascending = tablenode.getAttributes().getNamedItem("primary_sort_ascending").getNodeValue().equals("yes") ? true : false;
                                    } catch (Exception e) { }
                                    try {
                                        columnconfig.secondary_sort_column = Integer.parseInt(tablenode.getAttributes().getNamedItem("secondary_sort_column").getNodeValue());
                                        columnconfig.secondary_sort_ascending = tablenode.getAttributes().getNamedItem("secondary_sort_ascending").getNodeValue().equals("yes") ? true : false;
                                    } catch (Exception e) { }
                                    try {
                                        columnconfig.tertiary_sort_column = Integer.parseInt(tablenode.getAttributes().getNamedItem("tertiary_sort_column").getNodeValue());
                                        columnconfig.tertiary_sort_ascending = tablenode.getAttributes().getNamedItem("tertiary_sort_ascending").getNodeValue().equals("yes") ? true : false;
                                    } catch (Exception e) { }
                                    
                                    NodeList columns = tablenode.getChildNodes();
                                    int cindex = 0;
                                    for (int c = 0; c < columns.getLength(); ++c) {
                                        Node column = columns.item(c);
                                        if (column.getNodeName().equalsIgnoreCase("column")) {
                                            int id = Integer.parseInt(column.getAttributes().getNamedItem("column_id").getNodeValue());
                                            int width = Integer.parseInt(column.getAttributes().getNamedItem("width").getNodeValue());
                                            columnconfig.columnindex[cindex] = id;                                        
                                            columnconfig.setPreferredWidth(cindex, width);
                                            columnconfig.columntitles[cindex] = columnconfig.getKeyword(columnconfig.columnindex[cindex]);
                                            ++cindex;
                                        }                                    
                                    }
                                    if (columnconfig == SearchPane.instance.searchcolumnconfig) {
                                        for (int i = 0; i < SearchPane.instance.searchcolumnconfig.num_columns; ++i) {
                                            int c = SearchPane.instance.searchcolumnconfig.columnindex[i];
                                            OptionsUI.instance.SetSelectedSearchColumn(c);
                                        }
                                    } else if (columnconfig == MixoutPane.instance.mixoutcolumnconfig) {
                                        for (int i = 0; i < MixoutPane.instance.mixoutcolumnconfig.num_columns; ++i) {
                                            int c = MixoutPane.instance.mixoutcolumnconfig.columnindex[i];
                                            OptionsUI.instance.SetSelectedMixoutColumn(c);
                                        }                                        
                                    }
                                    */
                                }
                            }
                        } else if (confignode.getNodeName().equalsIgnoreCase("user")) {
                            NodeList usernodes = confignode.getChildNodes();
                            for (int u = 0; u < usernodes.getLength(); ++u) {
                                Node usernode = usernodes.item(u);
                                if (usernode.getNodeName().equalsIgnoreCase("name")) {
                                    //OptionsUI.instance.username.setText(getFirstValue(usernode));                                    
                                    //OptionsUI.instance.username.setEnabled(false);
                                } else if (usernode.getNodeName().equalsIgnoreCase("password")) {
                                    //OptionsUI.instance.password.setText(getFirstValue(usernode));
                                } else if (usernode.getNodeName().equalsIgnoreCase("email")) {
                                    //OptionsUI.instance.emailaddress.setText(getFirstValue(usernode));
                                } else if (usernode.getNodeName().equalsIgnoreCase("website")) {
                                    //OptionsUI.instance.userwebsite.setText(getFirstValue(usernode));
                                }
                            }
                        }
                    }
                } else if (rootnode.getNodeName().equalsIgnoreCase("styles")) {
                	NUM_STYLES = Integer.parseInt(rootnode.getAttributes().getNamedItem("num_styles").getNodeValue());
                    int sindex = 0;
                    NodeList stylenodes = rootnode.getChildNodes();
                    for (int stylenode_iter = 0; stylenode_iter < stylenodes.getLength(); ++stylenode_iter) {
                        Node stylenode = stylenodes.item(stylenode_iter);
                        if (stylenode.getNodeName().equalsIgnoreCase("style")) {
                            String stylename = stylenode.getAttributes().getNamedItem("name").getNodeValue();
                            String description = "";
                            try { description = stylenode.getAttributes().getNamedItem("description").getNodeValue(); } catch (Exception e) { }
                            boolean categoryOnly = false;
                            try {
                                if (stylenode.getAttributes().getNamedItem("category_only").getNodeValue().equals("yes"))
                                    categoryOnly = true;
                            } catch (Exception e) { }      
                            
                            // create the RE3 style
                            try {
	                            SubmittedStyle submittedStyle = new SubmittedStyle(stylename);
	                            submittedStyle.setDescription(description);
	                            submittedStyle.setCategoryOnly(categoryOnly);
	                            submittedStyle.setDoNotAddToHierarchy(true);
	                            if (!Database.getStyleIndex().doesExist(new StyleIdentifier(stylename)))
	                            	Database.getStyleIndex().add(submittedStyle);
                            } catch (AlreadyExistsException e) { }
                            
                            // load the old RE2 style data, which will be converted...
                        	RE2Style thisStyle = new RE2Style();
                            styles.add(thisStyle);
                            thisStyle.setName(stylename);
                            thisStyle.setDescription(description);
                            thisStyle.setCategoryOnly(categoryOnly);                            
                            try {
                            	int styleid = Integer.parseInt(stylenode.getAttributes().getNamedItem("id").getNodeValue());
                            	thisStyle.setStyleId(styleid);
                            } catch (Exception e) { }
                            thisStyle.setSIndex(sindex++);
                        	if (log.isTraceEnabled())
                        		log.trace("execute(): added re2 style=" + thisStyle);
                            String parent_styleids = null;
                            try { parent_styleids = stylenode.getAttributes().getNamedItem("parent_ids").getNodeValue(); } catch (Exception e) { }
                            if ((parent_styleids == null) || parent_styleids.equals("")) {
                                // no parents (root)
                            	thisStyle.addParentStyle(RE2Style.ROOT_STYLE_ID);
                            } else {
                                StringTokenizer tokenizer = new StringTokenizer(parent_styleids, ",");
                                while (tokenizer.hasMoreTokens()) {
                                    int parent_styleid = new Integer(tokenizer.nextToken()).intValue();
                                    thisStyle.addParentStyle(parent_styleid);
                                }
                            }
                            String child_styleids = null;
                            try { child_styleids = stylenode.getAttributes().getNamedItem("child_ids").getNodeValue(); } catch (Exception e) { }
                            if ((child_styleids == null) || child_styleids.equals("")) {
                                // no children
                            } else {
                                StringTokenizer tokenizer = new StringTokenizer(child_styleids, ",");
                                while (tokenizer.hasMoreTokens()) {
                                    int child_styleid = new Integer(tokenizer.nextToken()).intValue();
                                    thisStyle.addChildStyle(child_styleid);
                                }
                            }                            
                            
                            NodeList pnodes = stylenode.getChildNodes();
                            for (int p = 0; p < pnodes.getLength(); ++p) {
                                Node pnode = pnodes.item(p);
                                if (pnode.getNodeName().equalsIgnoreCase("include") || pnode.getNodeName().equalsIgnoreCase("exclude")) {
                                    boolean include = false;
                                    if (pnode.getNodeName().equalsIgnoreCase("include")) include = true;
                                    NodeList inodes = pnode.getChildNodes();
                                    for (int i = 0; i < inodes.getLength(); ++i) {
                                        Node inode = inodes.item(i);
                                        if (inode.getNodeName().equalsIgnoreCase("keywords")) {
                                            NodeList knodes = inode.getChildNodes();
                                            for (int k = 0; k < knodes.getLength(); ++k) {
                                                Node knode = knodes.item(k);
                                                if (knode.getNodeName().equalsIgnoreCase("keyword")) {
                                                    //if (include)
                                                        //lastaddedstyle.insertKeyword(getFirstValue(knode));
                                                    //else lastaddedstyle.insertExcludeKeyword(getFirstValue(knode));
                                                }
                                            }                                                                                     
                                        } else if (inode.getNodeName().equalsIgnoreCase("songs")) {
                                            NodeList snodes = inode.getChildNodes();
                                            for (int s = 0; s < snodes.getLength(); ++s) {
                                                Node snode = snodes.item(s);
                                                if (snode.getNodeName().equalsIgnoreCase("song")) {
                                                    //if (include)
                                                        //lastaddedstyle.insertSong(Long.parseLong(getFirstValue(snode)));
                                                    //else lastaddedstyle.insertExcludeSong(Long.parseLong(getFirstValue(snode)));
                                                }
                                            }                                            
                                        }
                                    }
                                } else if (pnode.getNodeName().equalsIgnoreCase("include_display") || pnode.getNodeName().equalsIgnoreCase("exclude_display")) {
                                    boolean include = false;
                                    if (pnode.getNodeName().equalsIgnoreCase("include_display")) include = true;
                                    NodeList enodes = pnode.getChildNodes();
                                    for (int e = 0; e < enodes.getLength(); ++e) {
                                        Node enode = enodes.item(e);
                                        if (enode.getNodeName().equalsIgnoreCase("entry")) {
                                            String display = enode.getAttributes().getNamedItem("display").getNodeValue();
                                            String value = enode.getAttributes().getNamedItem("value").getNodeValue();
                                            if (include) {
                                                //lastaddedstyle.styleincludedisplayvector.add(display);
                                                try {
                                                    //lastaddedstyle.styleincludevector.add(new Long(Long.parseLong(value)));
                                                } catch (Exception e2) {
                                                    //lastaddedstyle.styleincludevector.add(value);
                                                }
                                            } else {
                                                //lastaddedstyle.styleexcludedisplayvector.add(display);
                                                try {
                                                    //lastaddedstyle.styleexcludevector.add(new Long(Long.parseLong(value)));
                                                } catch (Exception e2) {
                                                    //lastaddedstyle.styleexcludevector.add(value);
                                                }                                                
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                	if (Database.getNumImportedRE2Songs() == 0) {
	                    Database.doUpdateFilterAndParentsOnHierarchyChanged = false;
	                    try {
		                    // create hierarchy in RE3 data model...
		                    for (RE2Style re2Style : styles) {
		                    	StyleRecord re3Style = (StyleRecord)Database.getStyleIndex().getRecord(new StyleIdentifier(re2Style.getName()));
		                    	Iterator<Integer> iter = re2Style.getParentStyleIter();
		                    	boolean first = true;
		                    	while (iter.hasNext()) {
		                    		int re2ParentId = iter.next();
		                    		if (re2ParentId != RE2Style.ROOT_STYLE_ID) {
			                    		RE2Style re2parent = getStyleWithId(re2ParentId);
			                    		if (re2parent != null) {
			                    			StyleRecord re3parent = (StyleRecord)Database.getStyleIndex().getRecord(new StyleIdentifier(re2parent.getName()));		                    			
			                    			Database.getStyleIndex().addRelationship(re3parent, re3Style);
				                            if (log.isDebugEnabled())
				                            	log.debug("execute(): added parent style=" + re3parent + ", to style=" + re3Style);
			                    		} else {
			                    			log.warn("execute(): parent style missing with id=" + re2ParentId);
			                    		}
			                    		first = false;
		                    		} else {
		                    			Database.getStyleIndex().addRelationship(Database.getStyleIndex().getRootRecord(), re3Style);
		                    		}
		                    	}
		                    	iter = re2Style.getChildStyleIter();
		                    	first = true;
		                    	while (iter.hasNext()) {
		                    		int re2ChildId = iter.next();
		                    		RE2Style re2child = getStyleWithId(re2ChildId);
		                    		if (re2child != null) {
		                    			StyleRecord re3child = (StyleRecord)Database.getStyleIndex().getRecord(new StyleIdentifier(re2child.getName()));
		                    			Database.getStyleIndex().addRelationship(re3Style, re3child);
			                            if (log.isDebugEnabled())
			                            	log.debug("execute(): added child style=" + re3child + ", to style=" + re3Style);
		                    		} else {
		                    			log.warn("execute(): child style missing with id=" + re2ChildId);
		                    		}
		                    		first = false;
		                    	}                    	
		                    }
		                    Database.getStyleModelManager().reset();
		        			((ProfileStyleModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ProfileStyleModelManager.class)).reset();					
		        			((ProfileTagModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ProfileTagModelManager.class)).reset();	                    
	                    } catch (Exception e) {
	                    	log.error("execute(): error migrating style hierarchy", e);
	                    }
	                    Database.doUpdateFilterAndParentsOnHierarchyChanged = true;
                    }
                } else if (rootnode.getNodeName().equalsIgnoreCase("songs")) {                                   	
                	SimpleDateFormat masterdateformat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss z");

                	int user1TypeCombo = USER_FIELD_TYPE_NONE;
                	int user2TypeCombo = USER_FIELD_TYPE_NONE;
                	int user3TypeCombo = USER_FIELD_TYPE_NONE;
                	int user4TypeCombo = USER_FIELD_TYPE_NONE;                	                	
                	
                	try { user1TypeCombo = Integer.parseInt(skinDatMap.get("options_song_custom_field_1_tag_combo")); } catch (NumberFormatException nfe) { }
                	try { user2TypeCombo = Integer.parseInt(skinDatMap.get("options_song_custom_field_2_tag_combo")); } catch (NumberFormatException nfe) { }
                	try { user3TypeCombo = Integer.parseInt(skinDatMap.get("options_song_custom_field_3_tag_combo")); } catch (NumberFormatException nfe) { }
                	try { user4TypeCombo = Integer.parseInt(skinDatMap.get("options_song_custom_field_4_tag_combo")); } catch (NumberFormatException nfe) { }                	

                	if (user1TypeCombo == USER_FIELD_TYPE_NONE) {
                		if (userLabel1.equalsIgnoreCase("label") || userLabel1.equalsIgnoreCase("record label"))
                			user1TypeCombo = USER_FIELD_TYPE_LABEL;
                	} 
                	if (user2TypeCombo == USER_FIELD_TYPE_NONE) {
                		if (userLabel2.equalsIgnoreCase("label") || userLabel2.equalsIgnoreCase("record label"))
                			user2TypeCombo = USER_FIELD_TYPE_LABEL;
                	} 
                	if (user3TypeCombo == USER_FIELD_TYPE_NONE) {
                		if (userLabel3.equalsIgnoreCase("label") || userLabel3.equalsIgnoreCase("record label"))
                			user3TypeCombo = USER_FIELD_TYPE_LABEL;
                	} 
                	if (user4TypeCombo == USER_FIELD_TYPE_NONE) {
                		if (userLabel4.equalsIgnoreCase("label") || userLabel4.equalsIgnoreCase("record label"))
                			user4TypeCombo = USER_FIELD_TYPE_LABEL;
                	} 

                	if (user1TypeCombo == USER_FIELD_TYPE_NONE) {
                		if (userLabel1.equalsIgnoreCase("year") || userLabel1.equalsIgnoreCase("original year"))
                			user1TypeCombo = USER_FIELD_TYPE_YEAR;
                	} 
                	if (user2TypeCombo == USER_FIELD_TYPE_NONE) {
                		if (userLabel2.equalsIgnoreCase("year") || userLabel2.equalsIgnoreCase("original year"))
                			user2TypeCombo = USER_FIELD_TYPE_YEAR;
                	} 
                	if (user3TypeCombo == USER_FIELD_TYPE_NONE) {
                		if (userLabel3.equalsIgnoreCase("year") || userLabel3.equalsIgnoreCase("original year"))
                			user3TypeCombo = USER_FIELD_TYPE_YEAR;
                	} 
                	if (user4TypeCombo == USER_FIELD_TYPE_NONE) {
                		if (userLabel4.equalsIgnoreCase("year") || userLabel4.equalsIgnoreCase("original year"))
                			user4TypeCombo = USER_FIELD_TYPE_YEAR;
                	} 
                	
                	Database.getSongIndex().addUserDataType("Analog", UserDataType.TYPE_BOOLEAN_FLAG);
                	Database.getSongIndex().addUserDataType("Digital", UserDataType.TYPE_BOOLEAN_FLAG);
                	
                	if ((user1TypeCombo != USER_FIELD_TYPE_YEAR) && (user1TypeCombo != USER_FIELD_TYPE_LABEL) && (userLabel1.length() > 0))
                		Database.getSongIndex().addUserDataType(userLabel1, UserDataType.TYPE_TEXT_FIELD);
                	if ((user2TypeCombo != USER_FIELD_TYPE_YEAR) && (user2TypeCombo != USER_FIELD_TYPE_LABEL) && (userLabel2.length() > 0))
                		Database.getSongIndex().addUserDataType(userLabel2, UserDataType.TYPE_TEXT_FIELD);
                	if ((user3TypeCombo != USER_FIELD_TYPE_YEAR) && (user3TypeCombo != USER_FIELD_TYPE_LABEL) && (userLabel3.length() > 0))
                		Database.getSongIndex().addUserDataType(userLabel3, UserDataType.TYPE_TEXT_FIELD);
                	if ((user4TypeCombo != USER_FIELD_TYPE_YEAR) && (user4TypeCombo != USER_FIELD_TYPE_LABEL) && (userLabel4.length() > 0))
                		Database.getSongIndex().addUserDataType(userLabel4, UserDataType.TYPE_TEXT_FIELD);                	
                	
                    NodeList songnodes = rootnode.getChildNodes();
                    int totalNumSongs = songnodes.getLength();
                    int song_iter = 0;
                    boolean doneAddingSongs = false;
                    while ((song_iter < songnodes.getLength()) && !doneAddingSongs) {
                    	if (RapidEvolution3.isTerminated || isCancelled())
                    		return;
                    	setProgress(((float)song_iter) / totalNumSongs);
                        Node songnode = songnodes.item(song_iter);                        
                        if (songnode.getNodeName().equalsIgnoreCase("song")) {
                            long uniqueid = 0;
                            String album = "";
                            String artist = "";
                            String track = "";
                            String title = "";
                            String remix = "";
                            String comments = "";
                            String time = "";
                            String timesig = "";
                            String user1 = "";
                            String user2 = "";
                            String user3 = "";
                            String user4 = "";
                            String filename = "";
                            boolean digital = false;
                            boolean analog = false;
                            boolean disabled = false;
                            boolean synced = false;
                            String startkey = "";
                            String endkey = "";
                            int numplays = 0;
                            String itunesid = "";
                            int keyaccuracy = 0;
                            int beatintensity = 0;
                            float startbpm = 0.0f;
                            float endbpm = 0.0f;
                            float replay_gain = Float.MAX_VALUE;
                            Boolean compilation = null;
                            int bpmaccuracy = 0;
                            java.util.Date dateadded = new java.util.Date();
                            String stylesbitmask = "";                            
                            String rating = "0";
                                                    
                            NodeList songattr_nodelist = songnode.getChildNodes();
                            for (int attr_iter = 0; attr_iter < songattr_nodelist.getLength(); ++attr_iter) {
                                Node attr = songattr_nodelist.item(attr_iter);
                                if (attr.getNodeName().equalsIgnoreCase("unique_id")) {
                                    uniqueid = Integer.parseInt(getFirstValue(attr));
                                } else if (attr.getNodeName().equalsIgnoreCase("artist")) {
                                    artist = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("album")) {
                                    album = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("track")) {
                                    track = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("title")) {
                                    title = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("remix")) {
                                    remix = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("comments")) {
                                    comments = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("time")) {
                                    time = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("time_signature")) {
                                    timesig = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("custom1")) {
                                    user1 = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("custom2")) {
                                    user2 = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("custom3")) {
                                    user3 = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("custom4")) {
                                    user4 = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("filename")) {
                                    filename = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("digital_only")) {
                                    if (getFirstValue(attr).equalsIgnoreCase("yes")) digital = true;
                                } else if (attr.getNodeName().equalsIgnoreCase("analog_only")) {
                                    if (getFirstValue(attr).equalsIgnoreCase("yes")) analog = true;
                                } else if (attr.getNodeName().equalsIgnoreCase("disabled")) {
                                    if (getFirstValue(attr).equalsIgnoreCase("yes")) disabled = true;
                                } else if (attr.getNodeName().equalsIgnoreCase("synced")) {
                                    if (getFirstValue(attr).equalsIgnoreCase("yes")) synced = true;
                                } else if (attr.getNodeName().equalsIgnoreCase("compilation")) {
                                    if (getFirstValue(attr).equalsIgnoreCase("yes")) compilation = new Boolean(true);
                                    else if (getFirstValue(attr).equalsIgnoreCase("no")) compilation = new Boolean(false);
                                } else if (attr.getNodeName().equalsIgnoreCase("key_start")) {
                                    startkey = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("key_end")) {
                                    endkey = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("key_accuracy")) {
                                    keyaccuracy = Integer.parseInt(getFirstValue(attr));
                                } else if (attr.getNodeName().equalsIgnoreCase("bpm_start")) {
                                    startbpm = Float.parseFloat(getFirstValue(attr));
                                } else if (attr.getNodeName().equalsIgnoreCase("bpm_end")) {
                                    endbpm = Float.parseFloat(getFirstValue(attr));
                                } else if (attr.getNodeName().equalsIgnoreCase("bpm_accuracy")) {
                                    bpmaccuracy = Integer.parseInt(getFirstValue(attr));
                                } else if (attr.getNodeName().equalsIgnoreCase("beat_intensity")) {
                                    beatintensity = Integer.parseInt(getFirstValue(attr));
                                } else if (attr.getNodeName().equalsIgnoreCase("num_plays")) {
                                    numplays = Integer.parseInt(getFirstValue(attr));
                                } else if (attr.getNodeName().equalsIgnoreCase("replay_gain")) {
                                    replay_gain = Float.parseFloat(getFirstValue(attr));
                                } else if (attr.getNodeName().equalsIgnoreCase("itunes_id")) {
                                    itunesid = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("date_added")) {
                                    dateadded = masterdateformat.parse(getFirstValue(attr));
                                } else if (attr.getNodeName().equalsIgnoreCase("styles_bitmask")) {
                                    stylesbitmask = getFirstValue(attr);
                                } else if (attr.getNodeName().equalsIgnoreCase("rating")) {
                                    rating = getFirstValue(attr);
                                }                                                                                                
                            }
                                                        
                            Vector<String> artists = new Vector(1);
                            artists.add(artist.trim());                                                        
                                                     
                            byte ratingByte = 0;
                            if (rating.equals("5")) ratingByte = 100;
                            else if (rating.equals("4")) ratingByte = 80;
                            else if (rating.equals("3")) ratingByte = 60;
                            else if (rating.equals("2")) ratingByte = 40;
                            else if (rating.equals("1")) ratingByte = 20;                            
                            
                            String labelName = null;
                            if (USER_FIELD_TYPE_LABEL == user1TypeCombo) { 
                            	labelName = user1.trim();
                            	user1 = "";
                            } else if (USER_FIELD_TYPE_LABEL == user2TypeCombo) {
                            	labelName = user2.trim();
                            	user2 = "";
                            } else if (USER_FIELD_TYPE_LABEL == user3TypeCombo) {
                            	labelName = user3.trim();
                            	user3 = "";
                            } else if (USER_FIELD_TYPE_LABEL == user4TypeCombo) {
                            	labelName = user4.trim();
                            	user4 = "";
                            }
                            
                            short originalYearReleased = 0;
                            try {                            	
	                            if (USER_FIELD_TYPE_YEAR == user1TypeCombo) {
	                            	try { originalYearReleased = Short.parseShort(user1.trim()); } catch (Exception e) { }	                            	
	                            	user1 = "";
	                            } else if (USER_FIELD_TYPE_YEAR == user2TypeCombo) {
	                            	try { originalYearReleased = Short.parseShort(user2.trim()); } catch (Exception e) { }	                            	
	                            	user2 = "";
	                            } else if (USER_FIELD_TYPE_YEAR == user3TypeCombo) {
	                            	try { originalYearReleased = Short.parseShort(user3.trim()); } catch (Exception e) { }	                            	
	                            	user3 = "";
	                            } else if (USER_FIELD_TYPE_YEAR == user4TypeCombo) {
	                            	try { originalYearReleased = Short.parseShort(user4.trim()); } catch (Exception e) { }	                            	
	                            	user4 = "";
	                            }
                            } catch (Exception e) { }
                            
                            SubmittedSong newSong = new SubmittedSong(artists, album.trim(), track.trim(), title.trim(), remix.trim());                            

                            if (album.trim().length() > 0) {
                            	boolean isCompilation = (compilation != null) ? compilation.booleanValue() : false;
                            	newSong.setCompilationFlag(isCompilation);
                            }                            
                            
                            for (int z = 0; z < NUM_STYLES; ++z) {
                                if (stylesbitmask.charAt(z) == '1') {
                                	RE2Style style = getStyleWithSIndex(z);
                                	if (style != null)
                                		newSong.addStyleDegreeValue(style.getName(), DEFAULT_STYLE_DEGREE, DATA_SOURCE_USER);
                                	else
                                		log.warn("execute(): style not found with sindex=" + z);
                                }                                
                            }    
                            if (log.isTraceEnabled())
                            	log.trace("execute(): newSong=" + newSong + ", styles=" + newSong.getStyleDegreeValues());
                            newSong.setRating(Rating.getRating(ratingByte), DATA_SOURCE_USER);                            
                            newSong.setComments(comments, DATA_SOURCE_UNKNOWN);
                            Vector<String> labelNames = new Vector<String>();
                            if ((labelName != null) && (labelName.length() > 0))
                            	labelNames.add(labelName);
                            newSong.setLabelNames(labelNames);
                            newSong.setKey(Key.getKey(startkey), Key.getKey(endkey), (byte)keyaccuracy, keyaccuracy == 100 ? DATA_SOURCE_USER : DATA_SOURCE_UNKNOWN);
                            newSong.setBpm(new Bpm(startbpm), new Bpm(endbpm), (byte)bpmaccuracy, bpmaccuracy == 100 ? DATA_SOURCE_USER : DATA_SOURCE_UNKNOWN);
                            newSong.setTimeSig(TimeSig.getTimeSig(timesig), DATA_SOURCE_UNKNOWN);
                            newSong.setBeatIntensity(BeatIntensity.getBeatIntensity(beatintensity), DATA_SOURCE_COMPUTED);
                            newSong.setDuration(new Duration(time), DATA_SOURCE_UNKNOWN);
                            newSong.setSongFilename(filename);
                            newSong.setOriginalYearReleased(originalYearReleased, DATA_SOURCE_UNKNOWN);
                            newSong.setDateAdded(dateadded.getTime());
                            newSong.setReplayGain(replay_gain, DATA_SOURCE_COMPUTED);
                            newSong.setPlayCount(numplays);
                            newSong.addUserData(new UserData(Database.getSongIndex().getUserDataType("Analog"), analog));
                            newSong.addUserData(new UserData(Database.getSongIndex().getUserDataType("Digital"), digital));
                            if ((userLabel1.length() > 0) && (user1 != null) && (user1.length() > 0))
                            	newSong.addUserData(new UserData(Database.getSongIndex().getUserDataType(userLabel1), user1));
                            if ((userLabel2.length() > 0) && (user2 != null) && (user2.length() > 0))
                            	newSong.addUserData(new UserData(Database.getSongIndex().getUserDataType(userLabel2), user2));
                            if ((userLabel3.length() > 0) && (user3 != null) && (user3.length() > 0))
                            	newSong.addUserData(new UserData(Database.getSongIndex().getUserDataType(userLabel3), user3));
                            if ((userLabel4.length() > 0) && (user4 != null) && (user4.length() > 0))
                            	newSong.addUserData(new UserData(Database.getSongIndex().getUserDataType(userLabel4), user4));
                            newSong.setITunesID(itunesid);
                            newSong.setSyncedWithMixshare(synced);                            
                            newSong.setDisabled(disabled);	 
                            
                            try {
                            	if (song_iter >= Database.getNumImportedRE2Songs()) {                    		
                            		SongProfile profile = (SongProfile)Database.getSongIndex().addOrUpdate(newSong);
                            		if (profile != null) {
                            			uniqueSongIdMap.put(uniqueid, profile.getIdentifier());
                            		}
                            	} else {
                            		SongRecord record = Database.getSongIndex().getSongRecord(newSong.getIdentifier());
                            		if (record != null) {
                            			uniqueSongIdMap.put(uniqueid, record.getIdentifier());
                            		}
                            	}
                            } catch (InsufficientInformationException ie) {
                            	log.warn("execute(): insufficient information for song=" + newSong);
                            } catch (Exception e) {
                            	log.error("execute(): error adding song=" + newSong, e);
                            }
                            
                            ++songsImported;
                            if ((TEST_MAX_IMPORT_LIMIT != -1) && (songsImported >= TEST_MAX_IMPORT_LIMIT))
                            	doneAddingSongs = true;
                            
                            if (song_iter >= Database.getNumImportedRE2Songs())
                            	Database.incrementImportedRE2Songs();
                    	}
                        ++song_iter;                        
                    }
                } else if (rootnode.getNodeName().equalsIgnoreCase("mixouts")) {
                	try {
	                    NodeList mixoutnodes = rootnode.getChildNodes();
	                    for (int mixout_iter = 0; mixout_iter < mixoutnodes.getLength(); ++mixout_iter) {
	                        Node mixoutnode = mixoutnodes.item(mixout_iter);
	                        if (mixoutnode.getNodeName().equalsIgnoreCase("mixout")) {
	                            
	                            long from_uniqueid = -1;
	                            long to_uniqueid = -1;
	                            float bpmdiff = 0.0f;
	                            int rank = 0;
	                            boolean synced = false;
	                            String comments = "";
	                            boolean addon = false;
	                            
	                            NodeList mixoutattr_nodelist = mixoutnode.getChildNodes();
	                            for (int attr_iter = 0; attr_iter < mixoutattr_nodelist.getLength(); ++attr_iter) {
	                                Node attr = mixoutattr_nodelist.item(attr_iter);
	                                if (attr.getNodeName().equalsIgnoreCase("from_unique_id")) {
	                                    from_uniqueid = Long.parseLong(getFirstValue(attr));
	                                } else if (attr.getNodeName().equalsIgnoreCase("to_unique_id")) {
	                                    to_uniqueid = Long.parseLong(getFirstValue(attr));                                
	                                } else if (attr.getNodeName().equalsIgnoreCase("bpm_diff")) {
	                                    bpmdiff = Float.parseFloat(getFirstValue(attr));
	                                } else if (attr.getNodeName().equalsIgnoreCase("rank")) {
	                                    rank = Integer.parseInt(getFirstValue(attr));
	                                } else if (attr.getNodeName().equalsIgnoreCase("synced")) {
	                                    if (getFirstValue(attr).equalsIgnoreCase("yes")) synced = true;
	                                } else if (attr.getNodeName().equalsIgnoreCase("comments")) {
	                                    comments = getFirstValue(attr);
	                                } else if (attr.getNodeName().equalsIgnoreCase("addon")) {
	                                    if (getFirstValue(attr).equalsIgnoreCase("yes")) addon = true;
	                                }
	                            }
	                            
	                            SongIdentifier fromSongId = (SongIdentifier)uniqueSongIdMap.get(from_uniqueid);
	                            SongIdentifier toSongId = (SongIdentifier)uniqueSongIdMap.get(to_uniqueid);
	                            if ((fromSongId != null) && (toSongId != null)) {
	                            	try {
		                            	MixoutIdentifier mixoutId = new MixoutIdentifier(Database.getSongIndex().getUniqueIdFromIdentifier(fromSongId), Database.getSongIndex().getUniqueIdFromIdentifier(toSongId));
		                            	if (!Database.getMixoutIndex().doesExist(mixoutId)) {
			                            	SubmittedMixout submittedMixout = new SubmittedMixout(mixoutId, bpmdiff);
			                            	submittedMixout.setComments(comments);	                            	
			                            	submittedMixout.setRating(Rating.getRating(rank), DATA_SOURCE_USER);
			                            	submittedMixout.setType(addon ? MixoutRecord.TYPE_ADDON : MixoutRecord.TYPE_TRANSITION);
			                            	submittedMixout.setSyncedWithMixshare(synced);
			                            	MixoutProfile mixoutProfile = (MixoutProfile)Database.add(submittedMixout);  
		                            		if (log.isDebugEnabled())
		                            			log.debug("execute(): imported mixout=" + mixoutProfile);
		                            		mixoutProfile.save(); // not sure if this is necessary but playing it safe after another bug
		                            	}
	                            	} catch (AlreadyExistsException ae) {
	                            	} catch (Exception e) {
	                            		log.error("execute(): error adding mixout", e);
	                            	}
	                            } else {
	                            	log.warn("execute(): couldn't add mixout, fromSongId=" + fromSongId + ", toSongId=" + toSongId);
	                            }
	                                                 
	                        }
	                    }
                	} catch (Exception e) {
                		log.error("execute(): error", e);
                	}
                } else if (rootnode.getNodeName().equalsIgnoreCase("excludes")) {
                    NodeList excludenodes = rootnode.getChildNodes();
                    for (int exclude_iter = 0; exclude_iter < excludenodes.getLength(); ++exclude_iter) {
                        Node excludenode = excludenodes.item(exclude_iter);
                        if (excludenode.getNodeName().equalsIgnoreCase("exclude")) {
                            
                            long from_uniqueid = -1;
                            long to_uniqueid = -1;
                            
                            NodeList excludeattr_nodelist = excludenode.getChildNodes();
                            for (int attr_iter = 0; attr_iter < excludeattr_nodelist.getLength(); ++attr_iter) {
                                Node attr = excludeattr_nodelist.item(attr_iter);
                                if (attr.getNodeName().equalsIgnoreCase("from_unique_id")) {
                                    from_uniqueid = Long.parseLong(getFirstValue(attr));
                                } else if (attr.getNodeName().equalsIgnoreCase("to_unique_id")) {
                                    to_uniqueid = Long.parseLong(getFirstValue(attr));                                
                                }
                            }
                            
                            SongIdentifier fromSongId = (SongIdentifier)uniqueSongIdMap.get(from_uniqueid);
                            SongIdentifier toSongId = (SongIdentifier)uniqueSongIdMap.get(to_uniqueid);
                            if ((fromSongId != null) && (toSongId != null)) {
                            	Exclude exclude = new Exclude(Database.getSongIndex().getUniqueIdFromIdentifier(fromSongId), Database.getSongIndex().getUniqueIdFromIdentifier(toSongId));
                            	SongProfile songProfile = (SongProfile)Database.getSongIndex().getProfile(fromSongId);
                            	if (songProfile != null) {
                            		songProfile.addExclude(exclude);
                            		songProfile.save();
                            	}
                            }
                                                        
                        }
                    }                    
                } else if (rootnode.getNodeName().equalsIgnoreCase("artists")) {
                	// skipped -- artist info will be re-computed
                } else if (rootnode.getNodeName().equalsIgnoreCase("albumcovers")) {
                    NodeList albumcovernodes = rootnode.getChildNodes();
                    for (int albumcovernode_iter = 0; albumcovernode_iter < albumcovernodes.getLength(); ++albumcovernode_iter) {
                        Node albumcovernode = albumcovernodes.item(albumcovernode_iter);
                        if (albumcovernode.getNodeName().equalsIgnoreCase("albumcover")) {                            
                            String id = albumcovernode.getAttributes().getNamedItem("id").getNodeValue();
                            Vector<String> images = new Vector<String>();
                            images.add(albumcovernode.getAttributes().getNamedItem("thumbnail").getNodeValue());
                            NodeList attrlist = albumcovernode.getChildNodes();
                            for (int attr_iter = 0; attr_iter < attrlist.getLength(); ++attr_iter) {
                                Node attr = attrlist.item(attr_iter);
                                if (attr.getNodeName().equalsIgnoreCase("image")) {
                                	images.add(attr.getAttributes().getNamedItem("filename").getNodeValue());
                                }
                            }        
                            albumCoverMap.put(id, images);
                        }                        
                    }                
                }
            }
            
            Iterator<Integer> releaseIter = Database.getReleaseIndex().getIdsIterator();
            while (releaseIter.hasNext()) {
            	ReleaseIdentifier releaseId = (ReleaseIdentifier)Database.getReleaseIndex().getIdentifierFromUniqueId(releaseIter.next());
            	String oldAlbumId = getAlbumID(releaseId.getArtistDescription(), releaseId.getReleaseTitle());
            	Vector<String> images = albumCoverMap.get(oldAlbumId);
            	if (images != null) {
            		ReleaseProfile releaseProfile = (ReleaseProfile)Database.getReleaseIndex().getProfile(releaseId);
            		if (releaseProfile != null) {
            			if (log.isDebugEnabled())
            				log.debug("execute(): imported album cover for release=" + releaseId);
            			for (int i = 0; i < images.size(); ++i) {
            				try {
            					String originalImageFilename = images.get(i);
            					BufferedImage img = ImageIO.read(new File(originalImageFilename));
            					String newFilename = "/data/albumcovers/" + FileUtil.getFilenameMinusDirectory(originalImageFilename);
            					FileSystemAccess.getFileSystem().saveImage(newFilename, img, false);
            					releaseProfile.addUserImage(new Image(newFilename, newFilename, DATA_SOURCE_UNKNOWN));            				
            				} catch (InvalidImageException ie) { } catch (Exception e) { }
            			}
            			releaseProfile.save();            			
            		}
            	}
            }
            
            if (log.isDebugEnabled())
            	log.debug("execute(): successfully imported RE2 database");
            Database.setHasImportedFromRE2();
        } catch (Exception e) {
            log.error("execute(): error loading database", e);
        }
    }
    
    public int getTaskPriority() { return RE3Properties.getInt("re2_import_task_priority"); }
    
    public Object getResult() { return null; }

    private static String getFirstValue(Node node) {
        Node child = node.getFirstChild();
        if (child != null) {
            String value = child.getNodeValue();
            if (value != null) return value;
        }
        return "";
    }

    private static RE2Style getStyleWithId(int id) {
    	for (RE2Style style : styles)
    		if (style.getStyleId() == id)
    			return style;    	
    	return null;
    }
    
    private static RE2Style getStyleWithSIndex(int sIndex) {
    	for (RE2Style style : styles)
    		if (style.getSIndex() == sIndex)
    			return style;    	
    	return null;
    }
    
    private static String getAlbumID(String artist, String album) {
        if ((artist == null) || artist.equals(""))
        	return "various - " + album.toLowerCase();
        return artist.toLowerCase() + " - " + album.toLowerCase();
    }
    
    
    
}
