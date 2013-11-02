package com.mixshare.rapid_evolution.ui.widgets.profile.tab;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.ui.dialogs.columns.SelectColumnsDialog;
import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.widgets.common.LazyLoadWidget;
import com.mixshare.rapid_evolution.ui.widgets.common.LazyLoaderInterface;
import com.mixshare.rapid_evolution.ui.widgets.common.search.SearchBarWidget;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;
import com.trolltech.qt.core.Qt.CaseSensitivity;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

abstract public class CommonTabTableWidget extends LazyLoadWidget implements DataConstants, LazyLoaderInterface {
	
	static private Logger log = Logger.getLogger(CommonTabTableWidget.class);
    			
    ////////////
    // FIELDS //
    ////////////
    
	protected SearchBarWidget searchBarWidget;
	protected TabTableTextInputSearchDelay textInputDelay;
	
	protected SortTableView itemView;
    protected SortFilterProxyModel proxyModel;
    
    protected TableModelManager modelManager;
    protected Column defaultSortColumn;
                    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public CommonTabTableWidget(TableModelManager modelManager, Column defaultSortColumn) {
    	this.modelManager = modelManager;
    	this.defaultSortColumn = defaultSortColumn;
    	this.lazyLoader = this;
    	
    	textInputDelay = new TabTableTextInputSearchDelay(this);
    }
    
    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////
        
    abstract public String[] getShowTypes();
    abstract public void setShowType();
    abstract public byte getShowType();
    
    abstract protected SortTableView createTableView(TableModelManager modelManager);
    
    abstract protected SortFilterProxyModel getSortFilterProxyModel(TableModelManager modelManager, SortTableView itemView);
    
    /////////////
    // GETTERS //
    /////////////
    
    public TableModelManager getModelManager() { return modelManager; }
    
    public SortTableView getView() { return itemView; }
    
    /////////////
    // METHODS //
    /////////////
    
    public void populateWidget(QWidget widget) {
    	if (log.isTraceEnabled())
    		log.trace("populateWidget(): populating...");
    	QSizePolicy searchWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
    	searchWidgetSizePolicy.setVerticalStretch((byte)1);
    	setSizePolicy(searchWidgetSizePolicy);    	    	    	
    	
    	QVBoxLayout searchLayout = new QVBoxLayout(this);    	
    	searchLayout.setMargin(0);    	
    	
    	searchBarWidget = new SearchBarWidget(true, 5);
        int i = 0;
        for (String showType : getShowTypes()) {
        	searchBarWidget.getFilterCombo().addItem(tr(showType), i);
        	++i;
        }        
        if (getShowTypes().length > 0) {
        	searchBarWidget.getFilterCombo().setCurrentIndex(getShowType());
        } else {
        	searchBarWidget.getFilterCombo().setVisible(false);
        }
        
        // search views
        itemView = createTableView(modelManager);
                        
        searchLayout.addWidget(searchBarWidget);                
        searchLayout.addWidget(itemView);
        
        // set up search tables        
        createModels(defaultSortColumn);
                        
        // setup signals (events)
        searchBarWidget.getFilterText().textChanged.connect(this, "searchTextChanged()");
        searchBarWidget.getConfigureColumnsButton().clicked.connect(this, "configureColumns()");    	
        searchBarWidget.getFilterCombo().currentIndexChanged.connect(this, "setShowType()");
    }

    protected void createModels(Column defaultSortColumn) {
    	modelManager.initialize(this);
    	// setup the proxy models (for sorting/filtering)
    	proxyModel = getSortFilterProxyModel(modelManager, itemView);
        proxyModel.setFilterCaseSensitivity(CaseSensitivity.CaseInsensitive);
        // set up tree views
        itemView.setModel(proxyModel);
        // init event listeners (selection changed, columns moved, etc)
        itemView.setupEventListeners();
        if (defaultSortColumn != null)
        	itemView.sortByColumn(defaultSortColumn.getColumnId());    
        setShowType();        
        // set initial column sizes
        modelManager.setSourceColumnSizes(itemView);
    }
    
    ////////////////////
    // SLOTS (EVENTS) //
    ////////////////////    
    
    private void searchTextChanged() {  
    	textInputDelay.searchTextChanged(searchBarWidget.getFilterText().text());
    }
    
    public void updateFilter() { 
    	proxyModel.setFilterFixedString(searchBarWidget.getFilterText().text());
    }
    
    private void configureColumns() {
    	SelectColumnsDialog selectColumnsDialog = new SelectColumnsDialog(this, modelManager);
    	if (selectColumnsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		selectColumnsDialog.saveSelections();
    		modelManager.setSourceColumnSizes(itemView);
    	}    	
    }
    
}
