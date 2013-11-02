package com.mixshare.rapid_evolution.ui.dialogs.merge;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.MergeRecordsTask;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.CaseSensitivity;
import com.trolltech.qt.core.Qt.CheckState;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QSortFilterProxyModel;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QWidget;

public class MergeRecordsDialog extends QDialog {

	static private Logger log = Logger.getLogger(MergeRecordsDialog.class);
	
	////////////
	// FIELDS //
	////////////
	
    private Ui_MergeRecordsDialog ui = new Ui_MergeRecordsDialog();
    private QStandardItemModel model;
    private QSortFilterProxyModel proxyModel;
    private Vector<Record> mergedRecords;
    private String recordType;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public MergeRecordsDialog(Vector<Record> mergedRecords, String recordType) {
        ui.setupUi(this);
        this.mergedRecords = mergedRecords;
        this.recordType = recordType;
        init();
    }

    public MergeRecordsDialog(QWidget parent, Vector<Record> mergedRecords, String recordType) {
        super(parent);
        ui.setupUi(this);
        this.mergedRecords = mergedRecords;
        this.recordType = recordType;
        init();
    }
    
    private void init() {
    	int numColumns = 1;
    	model = new QStandardItemModel(0, numColumns, this);    	
    	model.setHeaderData(0, Qt.Orientation.Horizontal, recordType + " " + Translations.get("merge_profiles_description_suffix"));		
    	proxyModel = new QSortFilterProxyModel();
    	proxyModel.setDynamicSortFilter(true);
    	proxyModel.setSourceModel(model);
    	proxyModel.setFilterCaseSensitivity(CaseSensitivity.CaseInsensitive);
    	ui.recordsView.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers);
    	ui.recordsView.setModel(proxyModel);     
    	boolean first = true;
    	for (Record record : mergedRecords) {
	    	// add first column
	    	QStandardItem titleColumn = new QStandardItem();
	    	titleColumn.setText(record.toString());
	    	titleColumn.setCheckable(true);    		    	
	    	titleColumn.setCheckState(first ? CheckState.Checked : CheckState.Unchecked);
	    	first = false;
	    	
	    	ArrayList<QStandardItem> newRow = new ArrayList<QStandardItem>(numColumns);
	    	newRow.add(titleColumn);
	    	model.appendRow(newRow);
	    	if (log.isTraceEnabled())
	    		log.trace("added record=" + record);
    	}    	
    	ui.recordsView.clicked.connect(this, "viewClicked(QModelIndex)");
        setWindowTitle(Translations.get("merge_profiles_prefix") + " " + recordType + "s");
        setFixedSize(size()); // disallows resizing (couldn't figure out how to do that from the GUI editor)
        ui.label.setText(Translations.get("merge_profiles_description"));
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public Record getSelectedPrimaryRecord() {
    	Record checkedRecord = null;
    	if (getNumSelections() == 1) {
	        for (int c = 0; c < mergedRecords.size(); ++c) {
	        	Record record = mergedRecords.get(c);
	        	QStandardItem titleColumn = model.item(c);
	        	if (titleColumn.checkState() == CheckState.Checked)
	        		checkedRecord = record;        		        	
	        }
    	}
    	return checkedRecord;
    }
    
    public Vector<Record> getMergedRecords() { return mergedRecords; }
    
    private int getNumSelections() {
    	int count = 0;
        for (int c = 0; c < mergedRecords.size(); ++c) {
        	QStandardItem titleColumn = model.item(c);
        	if (titleColumn.checkState() == CheckState.Checked)
        		++count; 		        	
        }
        return count;
    }
    
    /////////////
    // SETTERS //
    /////////////
    
    private void setSelectedRecord(Record setRecord) {
        for (int c = 0; c < mergedRecords.size(); ++c) {
        	Record record = mergedRecords.get(c);
        	QStandardItem titleColumn = model.item(c);
        	if (record.equals(setRecord))
        		titleColumn.setCheckState(CheckState.Checked);
        	else
        		titleColumn.setCheckState(CheckState.Unchecked);        		
        }    	
    }
    
    /////////////
    // METHODS //
    /////////////    

    public void mergeRecords() {
		Record primaryRecord = getSelectedPrimaryRecord();    		
		if (primaryRecord != null) {
			TaskManager.runForegroundTask(new MergeRecordsTask(primaryRecord, getMergedRecords()));			
		}    	    	
    }
    
    ////////////
    // EVENTS //
    ////////////
    
    private void viewClicked(QModelIndex clicked) {
    	QModelIndex sourceIndex = proxyModel.mapToSource(clicked);
    	Record record = mergedRecords.get(sourceIndex.row());
    	setSelectedRecord(record); 
    }
    
}
