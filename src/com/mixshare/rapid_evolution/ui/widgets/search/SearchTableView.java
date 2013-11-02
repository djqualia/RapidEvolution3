package com.mixshare.rapid_evolution.ui.widgets.search;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.index.CommonIndex;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.ui.dialogs.merge.MergeRecordsDialog;
import com.mixshare.rapid_evolution.ui.model.search.SearchItemModel;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.updaters.view.table.TableViewUpdater;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.table.CommonRecordTableView;
import com.mixshare.rapid_evolution.ui.widgets.filter.styles.StylesWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.filter.tags.TagsWidgetUI;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QItemSelection;
import com.trolltech.qt.gui.QMouseEvent;

public class SearchTableView extends CommonRecordTableView implements IndexChangeListener {

	static private Logger log = Logger.getLogger(SearchTableView.class);
	
	////////////
	// FIELDS //
	////////////
	
    protected QAction mergeAction;
	    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
	public SearchTableView(SearchModelManager modelManager) {
		super(modelManager);						
				
		((CommonIndex)modelManager.getIndex()).addIndexChangeListener(this);
		setEditTriggers(QAbstractItemView.EditTrigger.DoubleClicked);
		
        setDragEnabled(true);
        
        // setup actions
        mergeAction = new QAction(Translations.get("search_table_menu_merge"), this);
        mergeAction.triggered.connect(this, "mergeRecord()");
        mergeAction.setIcon(new QIcon(RE3Properties.getProperty("menu_merge_icon")));
        
	}	     	
	
	////////////////////
	// SLOTS (EVENTS) //
	////////////////////
	
	protected void selectionChanged(QItemSelection selected, QItemSelection deselected) {
		super.selectionChanged(selected, deselected);
    	if (RE3Properties.getBoolean("highlight_filters_of_selected_search_items")) {
    		if (StylesWidgetUI.instance.isVisible())
    			StylesWidgetUI.instance.getFilterTreeView().viewport().update();
    		if (TagsWidgetUI.instance.isVisible())
    			TagsWidgetUI.instance.getFilterTreeView().viewport().update();    	
    	}
	}
	
	protected void rowsInserted(QModelIndex parent, int start, int end) {		
		super.rowsInserted(parent, start, end);
		if (this == SearchWidgetUI.instance.getCurrentSearchView())
			SearchWidgetUI.instance.updateResultLabel();
	}
	
	protected void selectionChanged() {
    	Vector<SearchRecord> selectedStyles = getSelectedRecords();
        removeAction(mergeAction);
    	if (selectedStyles.size() == 0) {
    	} else if (selectedStyles.size() == 1) {
    	} else if (selectedStyles.size() > 1) {
            addAction(mergeAction);
    	}
    	SearchWidgetUI.instance.updateSelectedLabel(selectedStyles.size());
    }	    
        
    private void mergeRecord() {
    	Vector<SearchRecord> selectedSearchRecords = getSelectedRecords();
    	Vector<Record> selectedRecords = new Vector<Record>(selectedSearchRecords.size());
    	for (SearchRecord searchRecord : selectedSearchRecords)
    		selectedRecords.add((Record)searchRecord);
    	MergeRecordsDialog mergeRecordsDialog = new MergeRecordsDialog(this, selectedRecords, modelManager.getTypeDescription());
    	if (mergeRecordsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		mergeRecordsDialog.mergeRecords();
    	}    	    	
    }    
    
	public void addedRecord(Record record, SubmittedProfile submittedProfile) { }
	public void removedRecord(Record record) { 
		QApplication.invokeLater(new DeleteUpdateResultsThread(this));
	}

	private class DeleteUpdateResultsThread extends Thread {
		private SearchTableView searchTable;
		public DeleteUpdateResultsThread(SearchTableView searchTable) {
			this.searchTable = searchTable;
		}
		public void run() {
			if (searchTable == SearchWidgetUI.instance.getCurrentSearchView()) {				
				SearchWidgetUI.instance.updateResultLabel(searchTable.model().rowCount() - 1);			
			}
		}
	}
	
	/**
	 * This method calls update on the widget and all child widgets.  It was found that this was
	 * needed to force the repaint of elements that would otherwise wait until the object received focus....
	 */
	public void updatedRecord(Record record) {
		if (RapidEvolution3.isTerminated)
			return;
		QApplication.invokeAndWait(new TableViewUpdater(this));
	}
	
    protected void mouseDoubleClickEvent(QMouseEvent event) {
    	QModelIndex proxyIndex = indexAt(event.pos());
    	QModelIndex sourceIndex = getProxyModel().mapToSource(proxyIndex);
    	if ((sourceIndex != null) && (SearchItemModel.isEditableSearchColumn(modelManager.getSourceColumnType(sourceIndex.column()))))
    		super.mouseDoubleClickEvent(event, true);
    	else
    		super.mouseDoubleClickEvent(event);
    }

}
