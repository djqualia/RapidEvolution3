package com.mixshare.rapid_evolution.ui.model.filter.playlist;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.FilterIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.SubmittedFilterProfile;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedDynamicPlaylist;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.gui.QCompleter;

public class PlaylistModelManager extends FilterModelManager {

    static private Logger log = Logger.getLogger(PlaylistModelManager.class);	
    static private final long serialVersionUID = 0L;    
	
    static public StaticTypeColumn[] ALL_COLUMNS = {
    	COLUMN_PLAYLIST_NAME.getInstance(true),
    	COLUMN_NUM_ARTISTS.getInstance(false),
    	COLUMN_NUM_LABELS.getInstance(false),
    	COLUMN_NUM_RELEASES.getInstance(false),
    	COLUMN_NUM_SONGS.getInstance(false)
    };
    
    ////////////
    // FIELDS //
    ////////////
    
    transient private PlaylistCompleterFactory playlistCompleterFactory;    
    
    //////////////////
    // CONSTRUCTION //
    //////////////////
    
	public PlaylistModelManager() {
		setPrimarySortColumn(COLUMN_PLAYLIST_NAME.getColumnId());
	}	
	public PlaylistModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}			
	
	public void initColumns() {
		sourceColumns.clear();
		for (Column column : ALL_COLUMNS)
			sourceColumns.add(column);		
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public StaticTypeColumn[] getAllStaticColumns() { return ALL_COLUMNS; }
	public String getTypeDescription() { return Translations.get("text_playlist"); }
	public FilterIdentifier getFilterIdentifier(String filterName) { return new PlaylistIdentifier(filterName); }
	public SubmittedFilterProfile getNewSubmittedFilter(String filterName) { return new SubmittedDynamicPlaylist(filterName); }
	public String[] getFilterMimeType() { return new String[] { DragDropUtil.MIME_TYPE_PLAYLIST_INSTANCE_LIST };}

	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		PlaylistRecord playlistRecord = (PlaylistRecord)record;
		if (columnId == COLUMN_PLAYLIST_NAME.getColumnId())
			return new SmartString(playlistRecord.getPlaylistName());
		return super.getSourceData(columnId, record);
	}

	public TreeHierarchyInstance getTreeHierarchyInstance(HierarchicalRecord obj, TreeHierarchyInstance parentInstance) {
		if (obj != null)
			return new PlaylistHierarchyInstance(obj, parentInstance);
		return null;
	}
	
	public Index getIndex() { return Database.getPlaylistIndex(); }	

	public QCompleter getPlaylistCompleter() {
		if (playlistCompleterFactory == null)
			playlistCompleterFactory = new PlaylistCompleterFactory();
		return playlistCompleterFactory.getCompleter();
	} 
	
	public boolean isLazySearchSupported() { return RE3Properties.getBoolean("playlist_model_supports_lazy"); }

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
