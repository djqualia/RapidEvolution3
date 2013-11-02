package com.mixshare.rapid_evolution.ui.model.profile.search;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.data.profile.common.link.VideoLink;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.StaticTypeColumn;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;

public class VideoLinkModelManager extends LinkModelManager {

	static private Logger log = Logger.getLogger(VideoLinkModelManager.class);	
    static private final long serialVersionUID = 0L;    
    
    static public StaticTypeColumn[] ALL_COLUMNS = {     	
    	COLUMN_LINK_TITLE.getInstance(true),
    	COLUMN_THUMBNAIL_IMAGE.getInstance(true),
    	COLUMN_LINK_DESCRIPTION.getInstance(false),
    	COLUMN_LINK_TYPE.getInstance(true),
    	COLUMN_LINK_URL.getInstance(true),
    	COLUMN_LINK_SOURCE.getInstance(false)    	
    };
    	
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
	public VideoLinkModelManager() { }
	
	public VideoLinkModelManager(LineReader lineReader) {
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
	
	public String getTypeDescription() { return "Video Link"; }
    
	public Object getSourceData(short columnId, Object obj) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", obj=" + obj);		
		VideoLink link = (VideoLink)obj;
		if (columnId == COLUMN_THUMBNAIL_IMAGE.getColumnId())
			return link.getImage();
		return super.getSourceData(columnId, obj);
	}    
	
	public void setVideoLinks(Vector<VideoLink> videoLinks) {
		Vector<Link> links = new Vector<Link>(videoLinks.size());
		for (VideoLink videoLink : videoLinks)
			links.add(videoLink);
		super.setLinks(links);		
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}	
	
}
