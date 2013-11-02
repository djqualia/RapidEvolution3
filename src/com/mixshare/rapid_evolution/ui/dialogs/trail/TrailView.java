package com.mixshare.rapid_evolution.ui.dialogs.trail;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.ui.dialogs.columns.SelectColumnsDialog;
import com.mixshare.rapid_evolution.ui.model.profile.trail.TrailInstance;
import com.mixshare.rapid_evolution.ui.model.profile.trail.TrailModelManager;
import com.mixshare.rapid_evolution.ui.model.search.SearchItemModel;
import com.mixshare.rapid_evolution.ui.util.DragDropUtil;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QSortFilterProxyModel;
import com.trolltech.qt.gui.QTableView;
import com.trolltech.qt.gui.QItemSelectionModel.SelectionFlag;

public class TrailView extends QTableView {

	static private Logger log = Logger.getLogger(TrailView.class);
	
	private TrailModelManager modelManager;
	
	public TrailView(TrailModelManager modelManager) {
		super();
		this.modelManager = modelManager;
    	horizontalHeader().setStretchLastSection(false);
    	setEditTriggers(QAbstractItemView.EditTrigger.AllEditTriggers);
    	setDragEnabled(true);
    	//setDragDropMode(DragDropMode.InternalMove);    
    	setDropIndicatorShown(true);    	
    	setAcceptDrops(true);
    	//setDragDropOverwriteMode(false);
    	setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectRows);    	
    	setSelectionMode(SelectionMode.ExtendedSelection);
    	horizontalHeader().setDragEnabled(true);
    	horizontalHeader().setMovable(true);
    	setItemDelegate(new TrailItemDelegate(this, modelManager));
    	
        horizontalHeader().setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu);
        horizontalHeader().customContextMenuRequested.connect(this, "customContextMenuRequested(QPoint)");    	
	}
	
	public QSortFilterProxyModel getProxyModel() { return (QSortFilterProxyModel)model(); }
	
	public TrailModelManager getTrailModelManager() { return modelManager; }
	
	public void dropEvent(QDropEvent event) {				
		if (log.isTraceEnabled())
			log.trace("dropEvent(): event=" + event);
		Vector<TrailInstance> records = DragDropUtil.getTrailInstances(event.mimeData());
		QModelIndex proxyIndex = indexAt(event.pos());
		QModelIndex sourceIndex = null;
		TrailInstance record = null;
		int insertPosition = -1;
		Vector<Record> profileTrail = ProfileWidgetUI.instance.getProfileTrailCopy();
		if (proxyIndex != null) {
			sourceIndex = getProxyModel().mapToSource(proxyIndex);
			record = new TrailInstance(getTrailModelManager().getRecordForRow(sourceIndex.row()), sourceIndex.row());
			insertPosition = record.getPosition();
		}
		if (event.source() == this) {
			// rearranging
			int shift = 0;
			boolean before = false;
			for (TrailInstance removedRecord : records) {
				int removedIndex = removedRecord.getPosition();
				if (removedIndex < insertPosition) {
					++shift;
					before = true;
				}
			}
			Vector<Record> trimmedProfileTrail = new Vector<Record>(profileTrail.size());			
			int c = 0;
			for (Record profileTrailRecord : profileTrail) {
				TrailInstance inst = new TrailInstance(profileTrailRecord, c);
				boolean removed = false;
				for (TrailInstance removedRecord : records) {
					if (inst.equals(removedRecord)) {
						removed = true;
						break;
					}
				}
				if (!removed)
					trimmedProfileTrail.add(profileTrailRecord);
				++c;
			}
			profileTrail = trimmedProfileTrail;			
			insertPosition -= shift;
			if (before)
				++insertPosition;
			if (log.isDebugEnabled())
				log.debug("dropEvent(): re-arranging=" + records + ", insertPosition=" + insertPosition);
			int insertRow = insertPosition;
			if (insertPosition != -1) {
				for (TrailInstance removedRecord : records)
					profileTrail.insertElementAt(removedRecord.getRecord(), insertPosition++);
			} else {
				insertRow = profileTrail.size();
				for (TrailInstance removedRecord : records)
					profileTrail.add(removedRecord.getRecord());
			}
			if (log.isDebugEnabled())
				log.debug("dropEvent(): new trail=" + profileTrail);			
			ProfileWidgetUI.instance.setProfileTrail(profileTrail);
			selectionModel().clearSelection();
			// if sorting is allowed the following line will need to change
			for (int r = 0; r < records.size(); ++r) {
				int sourceRow = insertRow + r;
				for (c = 0; c < modelManager.getNumColumns(); ++c) {
					QModelIndex selSourceIndex = modelManager.getSourceModel().index(sourceRow, c);
					QModelIndex selProxyIndex = getProxyModel().mapFromSource(selSourceIndex);
					if (proxyIndex != null) 
						selectionModel().select(selProxyIndex, SelectionFlag.Select);					
				}
			}
			//selectionModel().select(new QItemSelection(getProxyModel().index(insertRow, 0), getProxyModel().index(insertRow + records.size() - 1, getProxyModel().columnCount())), SelectionFlag.Select);
		}
	}	
	
	public void scrollToCurrent(int currentRow) {
		scrollTo(getProxyModel().index(currentRow, 0));
	}

    protected void mouseDoubleClickEvent(QMouseEvent event) {		
    	QModelIndex proxyIndex = indexAt(event.pos());
    	QModelIndex sourceIndex = getProxyModel().mapToSource(proxyIndex);    	
    	if ((sourceIndex != null) && (SearchItemModel.isEditableSearchColumn(modelManager.getSourceColumnType(sourceIndex.column()))))
    		super.mouseDoubleClickEvent(event);
    	else
    		ProfileWidgetUI.instance.setProfileIndex(sourceIndex.row());
    }   

    protected void keyPressEvent(QKeyEvent keyEvent) {
    	super.keyPressEvent(keyEvent);
		if (keyEvent.key() == Qt.Key.Key_Delete.value())
			TrailDialog.instance.removeSelected();		
    }    

	////////////
	// EVENTS //
	////////////
	
	public void customContextMenuRequested(QPoint point) {
		if (log.isDebugEnabled())
			log.debug("customContextMenuRequested(): point=" + point);
    	SelectColumnsDialog selectColumnsDialog = new SelectColumnsDialog(this, modelManager);
    	if (selectColumnsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		selectColumnsDialog.saveSelections();
    		modelManager.setSourceColumnSizes(this);
    	}    	

	}
    
}
