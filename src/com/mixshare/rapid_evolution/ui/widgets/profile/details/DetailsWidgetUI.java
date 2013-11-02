package com.mixshare.rapid_evolution.ui.widgets.profile.details;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.ui.dialogs.columns.SelectColumnsDialog;
import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.details.DetailsProxyModel;
import com.mixshare.rapid_evolution.ui.widgets.common.LazyLoadWidget;
import com.mixshare.rapid_evolution.ui.widgets.common.LazyLoaderInterface;
import com.mixshare.rapid_evolution.ui.widgets.common.search.SearchBarWidget;
import com.trolltech.qt.core.Qt.CaseSensitivity;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

abstract public class DetailsWidgetUI extends LazyLoadWidget implements DataConstants, LazyLoaderInterface {
	
	static private Logger log = Logger.getLogger(DetailsWidgetUI.class);
    		
    ////////////
    // FIELDS //
    ////////////
    
	private SearchBarWidget searchBarWidget;

    private DetailsTableView detailsView;    
    private DetailsProxyModel proxyModel;
    
    private CommonDetailsModelManager modelManager;              
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public DetailsWidgetUI(CommonDetailsModelManager modelManager) {
    	this.lazyLoader = this;
    	this.modelManager = modelManager;
    	   
    }
    
    //////////////////////
    // ABSTRACT METHODS //    
    //////////////////////
    
    abstract protected DetailsTableView createTableView(CommonDetailsModelManager modelManager);
    
    /////////////
    // GETTERS //
    /////////////
    
    public CommonDetailsModelManager getModelManager() { return modelManager; }
    
    /////////////
    // METHODS //
    /////////////
    
    protected void resizeEvent(QResizeEvent resizeEvent) {
    	super.resizeEvent(resizeEvent);
    	if (detailsView != null)
    		detailsView.setColumnWidths(width());
    }
    
    public void populateWidget(QWidget widget) {
    	if (log.isTraceEnabled())
    		log.trace("populateWidget(): populating...");
    	QSizePolicy searchWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
    	searchWidgetSizePolicy.setVerticalStretch((byte)1);
    	setSizePolicy(searchWidgetSizePolicy);    	    	    	
    	
    	QVBoxLayout searchLayout = new QVBoxLayout(this);    	
    	searchLayout.setMargin(0);    	
    	
    	searchBarWidget = new SearchBarWidget(false, 5);
        
        // search views
        detailsView = createTableView(modelManager);
                        
        searchLayout.addWidget(searchBarWidget);                
        searchLayout.addWidget(detailsView);
        
        // set up search tables        
        createModels();
                        
        // setup signals (events)
        searchBarWidget.getFilterText().textChanged.connect(this, "searchTextChanged()");
        searchBarWidget.getConfigureColumnsButton().clicked.connect(this, "configureColumns()");
    }

    private void createModels() {
    	// create source model
    	modelManager.initialize(this);
    	// setup the proxy model (for sorting/filtering)
        proxyModel = new DetailsProxyModel(modelManager);
        proxyModel.setRelativeProfile(modelManager.getRelativeProfile());
        proxyModel.setFilterCaseSensitivity(CaseSensitivity.CaseInsensitive);
        proxyModel.setSourceModel(modelManager.getSourceModel());
        // set up tree views
        detailsView.setModel(proxyModel);
        // set initial column sizes 
        modelManager.setSourceColumnSizes(detailsView);
        // init event listeners (selection changed, columns moved, etc)
        detailsView.setupEventListeners();
        detailsView.setupPersistentEditors();
        detailsView.setColumnWidth(0, 265);
        detailsView.setColumnWidth(1, 50);
    }    
        
    ////////////////////
    // SLOTS (EVENTS) //
    ////////////////////    
    	
    private void searchTextChanged() {    	
    	proxyModel.setFilterFixedString(searchBarWidget.getFilterText().text());    	
        detailsView.setupPersistentEditors();        
        modelManager.setSourceColumnVisibilities(detailsView, searchBarWidget.getFilterText().text());
        proxyModel.invalidate();
    }
    
    private void configureColumns() {
    	SelectColumnsDialog selectColumnsDialog = new SelectColumnsDialog(this, modelManager);
    	if (selectColumnsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		selectColumnsDialog.saveSelections();    		
    		modelManager.setSourceColumnSizes(detailsView);
    		detailsView.setupPersistentEditors();
    	}    	
    }
    
        
}
