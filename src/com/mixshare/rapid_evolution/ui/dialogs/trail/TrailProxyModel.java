package com.mixshare.rapid_evolution.ui.dialogs.trail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.parameters.filter.playlist.PlaylistSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.filter.style.StyleSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.filter.tag.TagSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.artist.ArtistSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.label.LabelSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.release.ReleaseSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.profile.trail.TrailInstance;
import com.mixshare.rapid_evolution.ui.model.profile.trail.TrailModelManager;
import com.mixshare.rapid_evolution.ui.model.search.SearchItemModel;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;

public class TrailProxyModel extends SortFilterProxyModel implements AllColumns {

    static private Logger log = Logger.getLogger(TrailProxyModel.class);    
	
	private String searchText = "";
	private ArtistSearchParameters artistSearchParams;
	private LabelSearchParameters labelSearchParams;
	private ReleaseSearchParameters releaseSearchParams;
	private SongSearchParameters songSearchParams;
	private StyleSearchParameters styleSearchParams;
	private TagSearchParameters tagSearchParams;
	private PlaylistSearchParameters playlistSearchParams;

	public TrailProxyModel(QObject parent, TrailModelManager modelManager) {
		super(parent, modelManager);
		artistSearchParams = new ArtistSearchParameters();
		labelSearchParams = new LabelSearchParameters();
		releaseSearchParams = new ReleaseSearchParameters();
		songSearchParams = new SongSearchParameters();
		styleSearchParams = new StyleSearchParameters();
		tagSearchParams = new TagSearchParameters();
		playlistSearchParams = new PlaylistSearchParameters();
	}
	
	public void setSearchText(String text) {
		searchText = text;	
		artistSearchParams.setSearchText(text);
		labelSearchParams.setSearchText(text);
		releaseSearchParams.setSearchText(text);
		songSearchParams.setSearchText(text);
		styleSearchParams.setSearchText(text);
		tagSearchParams.setSearchText(text);
		playlistSearchParams.setSearchText(text);
	}
	
	protected void modelRefreshCallout() { // gives a model a chance to re-search when sorted (for lazy mode)
		
	}
	
	public boolean isLazySearchSupported() {
		return false;
	}
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceIndex) {
		Record record = ((TrailModelManager)modelManager).getRecordForRow(sourceRow);
		if (record != null) {
			if (record instanceof ArtistRecord)
				return (artistSearchParams.matches(record) > 0.0f);
			if (record instanceof LabelRecord)
				return (labelSearchParams.matches(record) > 0.0f);
			if (record instanceof ReleaseRecord)
				return (releaseSearchParams.matches(record) > 0.0f);
			if (record instanceof SongRecord)
				return (songSearchParams.matches(record) > 0.0f);
			if (record instanceof StyleRecord)
				return (styleSearchParams.matches(record) > 0.0f);
			if (record instanceof TagRecord)
				return (tagSearchParams.matches(record) > 0.0f);
			if (record instanceof PlaylistRecord)
				return (playlistSearchParams.matches(record) > 0.0f);
		}
		return false;
	}	

	public List<String> mimeTypes() {
		List<String> result = new ArrayList<String>();
		result.add(DragDropUtil.MIME_TYPE_TRAIL_INSTANCES_IDENTIFIER_LIST);
		return result;		
	}

	public Qt.ItemFlags flags(QModelIndex index) {
    	Qt.ItemFlags result = super.flags(index);    	
    	QModelIndex sourceIndex = mapToSource(index);
    	if (sourceIndex != null) {
    		if (SearchItemModel.isEditableSearchColumn(modelManager.getSourceColumnType(sourceIndex.column())))
    			result.set(Qt.ItemFlag.ItemIsEditable);	
    	}    	
    	return result;
    }	
	
	public QMimeData mimeData(List<QModelIndex> indexes) {
		Vector<TrailInstance> searchRecords = new Vector<TrailInstance>();
		Map<Integer, Object> processedRows = new HashMap<Integer, Object>();
		for (QModelIndex index : indexes) {
			QModelIndex sourceIndex = mapToSource(index);
			Integer row = sourceIndex.row();
			if (!processedRows.containsKey(row)) {
				processedRows.put(row, null);
				searchRecords.add(new TrailInstance((SearchRecord)((TrailModelManager)modelManager).getRecordForRow(row), row));
			}
		}		
		return DragDropUtil.getMimeDataForTrailInstances(searchRecords);
	}	
	
}
