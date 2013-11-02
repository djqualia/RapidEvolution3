package com.mixshare.rapid_evolution.ui.model.profile.tab;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableSortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;

public class TabTableProxyModel extends TableSortFilterProxyModel {

	static private Logger log = Logger.getLogger(TabTableProxyModel.class);
		
	////////////
	// FIELDS //
	////////////
		
	private RecordTabTableView tableView = null;
		
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public TabTableProxyModel(QObject parent, TableModelManager modelManager, RecordTabTableView tableView) {
		super(parent, modelManager);	
		this.tableView = tableView;
		setSupportedDragActions(Qt.DropAction.CopyAction);
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public RecordTableModelManager getTableModelManager() { return (RecordTableModelManager)modelManager; }
		
	/////////////
	// SETTERS //
	/////////////
		
	/////////////
	// METHODS //
	/////////////			
	
	public List<String> mimeTypes() {
		List<String> result = new ArrayList<String>();
		result.add(DragDropUtil.MIME_TYPE_FILENAME_LIST);
		return result;		
	}
	
	/**
	 * Lists the accepted mime types here...
	 */	
	public QMimeData mimeData(List<QModelIndex> indexes) {		
		return DragDropUtil.getMimeDataForSearchRecords(tableView.getSelectedRecords());
	}
	
}
