package com.mixshare.rapid_evolution.ui.dialogs.columns;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.util.QWidgetUtil;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.CaseSensitivity;
import com.trolltech.qt.core.Qt.CheckState;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMoveEvent;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QSortFilterProxyModel;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QTreeView;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class SelectColumnsDialog extends QDialog {

	static private Logger log = Logger.getLogger(SelectColumnsDialog.class);
	
    private QTreeView treeView;
    private QLineEdit filterText;

    private ModelManagerInterface modelManager;
    private QStandardItemModel model;
    private QSortFilterProxyModel proxyModel;
    private QAction selectAll;
    private QAction deselectAll;
    
    public SelectColumnsDialog(ModelManagerInterface modelManager) {
        this.modelManager = modelManager;
        init();
    }

    public SelectColumnsDialog(QWidget parent, ModelManagerInterface modelManager) {
        super(parent);
        this.modelManager = modelManager;
        init();
    }

    private void init() {
    	try {    		
    		setWindowIcon(new QIcon(RE3Properties.getProperty("application_icon_filename")));
    		
	    	QSizePolicy optionsWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
	    	optionsWidgetSizePolicy.setVerticalStretch((byte)1);
	    	setSizePolicy(optionsWidgetSizePolicy);    	    	    	
	    	
	    	QVBoxLayout optionsLayout = new QVBoxLayout(this);    	
	    	optionsLayout.setMargin(10);    	    	
	    	
	    	QWidget filterInputWidget = new QWidget();    	
	        QHBoxLayout filterInputLayout = new QHBoxLayout(filterInputWidget);
	        filterInputLayout.setSpacing(4);
	        filterInputLayout.setMargin(0);        
	    	
	    	QIcon searchIcon = new QIcon(RE3Properties.getProperty("search_icon"));
	    	QPushButton searchButton = new QPushButton(this);
	    	searchButton.setMaximumWidth(25);
	        searchButton.setEnabled(false);	        
	        searchButton.setFlat(true);
	        searchButton.setIcon(searchIcon);        
	
	    	filterText = new QLineEdit();
	    	filterText.textChanged.connect(this, "updateFilter()");
	        
	        createModels();    	
	    		
	    	// setup context menu    	
	    	selectAll = new QAction(Translations.get("column_selection_select_all"), this);
	    	selectAll.triggered.connect(this, "selectAll()");              
	    	selectAll.setIcon(new QIcon(RE3Properties.getProperty("menu_select_all_icon")));
	    	deselectAll = new QAction(Translations.get("column_selection_clear_all"), this);
	    	deselectAll.triggered.connect(this, "deselectAll()");
	    	deselectAll.setIcon(new QIcon(RE3Properties.getProperty("menu_clear_selections_icon")));
	        treeView.setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);    	
	    	treeView.addAction(selectAll);
	    	treeView.addAction(deselectAll);    	
	    	
	        filterInputLayout.addWidget(searchButton);
	        filterInputLayout.addWidget(filterText);
	        //filterInputLayout.addWidget(optionsTypeCombo);
	    	
	        optionsLayout.addWidget(filterInputWidget);
	        optionsLayout.addWidget(treeView);    	
	    	
	        QDialogButtonBox buttonBox = new QDialogButtonBox();
	        buttonBox.setObjectName("buttonBox");
	        buttonBox.setGeometry(new QRect(20, 480, 401, 41));
	        buttonBox.setFocusPolicy(com.trolltech.qt.core.Qt.FocusPolicy.TabFocus);
	        buttonBox.setStandardButtons(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.createQFlags(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Ok));
	        buttonBox.setCenterButtons(true);
	        buttonBox.clicked.connect(this, "close()");	        
	        optionsLayout.addWidget(buttonBox);
	        
	        setWindowTitle(modelManager.getTypeDescription() + Translations.get("column_selection_window_title_suffix"));
	        
	        QWidgetUtil.setWidgetSize(this, "column_selection_" + modelManager.getTypeDescription(), 540, 530);
	        QWidgetUtil.setWidgetPosition(this, "column_selection_" + modelManager.getTypeDescription());

	        filterText.setFocus();
	        
    	} catch (Exception e) {
    		log.error("init(): error", e);
    	}
    }
    	
    private void createModels() {
    	int numColumns = 2;
    	model = new QStandardItemModel(0, numColumns, this);    	
    	model.setHeaderData(0, Qt.Orientation.Horizontal, Translations.get("column_selection_column_title"));		
    	model.setHeaderData(1, Qt.Orientation.Horizontal, Translations.get("column_selection_column_description"));
    	proxyModel = new QSortFilterProxyModel();
    	proxyModel.setDynamicSortFilter(true);
    	proxyModel.setSourceModel(model);
    	proxyModel.setFilterCaseSensitivity(CaseSensitivity.CaseInsensitive);
    	treeView = new QTreeView();
    	treeView.setModel(proxyModel);    	
    	treeView.header().setStretchLastSection(false);
    	treeView.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers);    	
        for (int c = 0; c < modelManager.getNumColumns(); ++c) {
        	Column column = modelManager.getViewColumnType(c);
        	
        	QStandardItem titleColumn = new QStandardItem();
        	titleColumn.setText(column.getColumnTitle());
        	
        	if (column.isIdColumn() && (modelManager instanceof FilterModelManager))
        		titleColumn.setCheckable(false);
        	else        	
        		titleColumn.setCheckable(true);
        	titleColumn.setCheckState(column.isHidden() ? CheckState.Unchecked : CheckState.Checked);        	
        	
        	QStandardItem descriptionColumn = new QStandardItem();
        	descriptionColumn.setText(column.getColumnDescription());
        	
        	ArrayList<QStandardItem> newRow = new ArrayList<QStandardItem>(numColumns);
        	newRow.add(titleColumn);
        	newRow.add(descriptionColumn);
        	model.appendRow(newRow);
        	
        	if (log.isTraceEnabled())
        		log.trace("added column=" + column.getColumnTitle());
        }       
        treeView.header().resizeSection(0, 200);
        treeView.header().resizeSection(1, 600);
    }    
    
    private void updateFilter() {    	
    	proxyModel.setFilterFixedString(filterText.text());
    }
    
    public void saveSelections() {
        for (int c = 0; c < modelManager.getNumColumns(); ++c) {
        	Column column = modelManager.getViewColumnType(c);
        	if (column != null) {
        		QStandardItem titleColumn = model.item(c);
        		if (titleColumn != null)
        			column.setHidden(titleColumn.checkState() == CheckState.Unchecked);
        		else
        			log.warn("saveSelections(): titleColumn is null, c=" + c);
        	} else {
        		log.warn("saveSelections(): view column is null, c=" + c); 
        	}
        }    	    	
    }
    
    public int exec() {
    	super.exec();
    	return QDialog.DialogCode.Accepted.value();
    }
        
    private void selectAll() {
        for (int c = 0; c < modelManager.getNumColumns(); ++c) {
        	Column column = modelManager.getViewColumnType(c);
        	if (column != null) {
        		QStandardItem titleColumn = model.item(c);
        		titleColumn.setCheckState(CheckState.Checked);
        	} else {
        		log.warn("selectAll(): view column is null, c=" + c); 
        	}
        }    	    	    	
    }
    
    private void deselectAll() {
        for (int c = 0; c < modelManager.getNumColumns(); ++c) {
        	Column column = modelManager.getViewColumnType(c);
        	if (column != null) {
        		QStandardItem titleColumn = model.item(c);
        		titleColumn.setCheckState(CheckState.Unchecked);
        	} else {
        		log.warn("deselectAll(): view column is null, c=" + c); 
        	}
        }    	
    }
    
    protected void resizeEvent(QResizeEvent re) {
    	super.resizeEvent(re);
    	UIProperties.setProperty("column_selection_" + modelManager.getTypeDescription() + "_width", String.valueOf(re.size().width()));
    	UIProperties.setProperty("column_selection_" + modelManager.getTypeDescription() + "_height", String.valueOf(re.size().height()));
    }
    
    protected void moveEvent(QMoveEvent me) {
    	super.moveEvent(me);
    	UIProperties.setProperty("column_selection_" + modelManager.getTypeDescription() + "_x", String.valueOf(me.pos().x()));
    	UIProperties.setProperty("column_selection_" + modelManager.getTypeDescription() + "_y", String.valueOf(me.pos().y()));
    }
        
}
