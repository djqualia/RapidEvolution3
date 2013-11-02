package com.mixshare.rapid_evolution.ui.widgets.profile.tab;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.index.CommonIndex;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.ui.dialogs.columns.SelectColumnsDialog;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.tab.TabTreeProxyModel;
import com.mixshare.rapid_evolution.ui.model.tree.TreeModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.LazyLoadWidget;
import com.mixshare.rapid_evolution.ui.widgets.common.LazyLoaderInterface;
import com.mixshare.rapid_evolution.ui.widgets.common.search.SearchBarWidget;
import com.mixshare.rapid_evolution.ui.widgets.filter.styles.StylesWidgetUI;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt.CaseSensitivity;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QShowEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

abstract public class TabTreeWidget extends LazyLoadWidget implements DataConstants, LazyLoaderInterface {
	
	static private Logger log = Logger.getLogger(TabTreeWidget.class);
    		
	static public byte SHOW_SELECTED = 0;
	static public byte SHOW_ALL = 1;
	
	
    ////////////
    // FIELDS //
    ////////////
    
	protected SearchBarWidget searchBarWidget;

    private TabTreeView itemView;    
    private TabTreeProxyModel proxyModel;
    
    private FilterModelManager modelManager;
    private Column defaultSortColumn;
	protected SearchProfile searchProfile;    
	
	private boolean sorted = false;
                
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public TabTreeWidget(FilterModelManager modelManager, Column defaultSortColumn) {
    	this.modelManager = modelManager;
    	this.defaultSortColumn = defaultSortColumn;
    	this.lazyLoader = this;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public FilterModelManager getModelManager() { return modelManager; }
    
    public TabTreeView getTreeView() { return itemView; }
    
    /////////////
    // SETTERS //
    /////////////
    
    protected void setShowType() {
    	byte type = ((Byte)searchBarWidget.getFilterCombo().itemData(searchBarWidget.getFilterCombo().currentIndex())).byteValue();
    	if (log.isDebugEnabled())
    		log.debug("setShowType(): type=" + type);
    	proxyModel.setShowType(type);    
    	itemView.setupPersistentEditors();
    	if (((Byte)searchBarWidget.getFilterCombo().itemData(searchBarWidget.getFilterCombo().currentIndex())).byteValue() == SHOW_SELECTED) {
    		itemView.expandAll();
    	} else {
    		itemView.collapseAll();
    	}
    }
    
    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////
    
    abstract protected TabTreeView getTreeView(FilterModelManager modelManager);
    abstract protected TabTreeProxyModel getTabTreeProxyModel(QObject parent, TreeModelManager modelManager, TabTreeView treeView);
    abstract protected void searchTextChanged();
    
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
    	
    	searchBarWidget.getFilterCombo().addItem(Translations.get("text_show_selected"), SHOW_SELECTED);
    	searchBarWidget.getFilterCombo().addItem(Translations.get("text_show_all"), SHOW_ALL);
                
        // search views
        itemView = getTreeView(modelManager);
        itemView.setSearchProfile(searchProfile);
                        
        searchLayout.addWidget(searchBarWidget);                
        searchLayout.addWidget(itemView);
        
        // set up search tables        
        createModels(defaultSortColumn);

        // setup signals (events)
        searchBarWidget.getFilterText().textChanged.connect(this, "searchTextChanged()");
        searchBarWidget.getFilterCombo().currentIndexChanged.connect(this, "setShowType()");        
        searchBarWidget.getConfigureColumnsButton().clicked.connect(this, "configureColumns()");                
        
        StylesWidgetUI.instance.getFilterTreeView().addFilterHierarchyChangeListener(itemView);
    }
	
    /////////////
    // GETTERS //
    /////////////

    public boolean isShowAll() {
    	return (searchBarWidget.getFilterCombo().currentIndex() == 1);
    }
    
    /////////////
    // METHODS //
    /////////////
    
    public void unload() {
    	//StylesWidgetUI.instance.getFilterTreeView().removeFilterHierarchyChangeListener(itemView);
    	itemView.unload();
    }
    
    public void init(SearchProfile searchProfile) {
    	setIsUnloading(false);
    	sorted = false;
    	this.searchProfile = searchProfile;
    	if (itemView != null) {
    		itemView.setSearchProfile(searchProfile);    		
    		itemView.invalidateProfileSelections();  
    		proxyModel.setSearchProfile(searchProfile);
    		modelManager.refresh();
    	}
    }
    
	public void showEvent(QShowEvent e) {
		if (loaded && !isUnloading) {
    		setShowType();
    		if (!sorted) {
    			if (defaultSortColumn != null)
    				itemView.sortByColumn(defaultSortColumn.getColumnId());
    			sorted = true;
    		}
		}
		super.showEvent(e);
	}    
    
    protected void createModels(Column defaultSortColumn) {
    	modelManager.initialize(this);
    	((CommonIndex)modelManager.getIndex()).addIndexChangeListener(modelManager);
    	// setup the proxy models (for sorting/filtering)
        proxyModel = getTabTreeProxyModel(this, modelManager, itemView);
        proxyModel.setSearchProfile(searchProfile);
        proxyModel.setFilterCaseSensitivity(CaseSensitivity.CaseInsensitive);
        // set up tree views
        itemView.setModel(proxyModel);
        // set initial column sizes
        modelManager.setSourceColumnSizes(itemView);
        // init event listeners (selection changed, columns moved, etc)
        itemView.setupEventListeners();
        searchBarWidget.getFilterCombo().setCurrentIndex(proxyModel.getShowType());
        setShowType();
    }
           
    ////////////////////
    // SLOTS (EVENTS) //
    ////////////////////    
    
    public void updateFilter() {
    	proxyModel.setSearchText(searchBarWidget.getFilterText().text());
    	proxyModel.invalidate();
    	itemView.setupPersistentEditors();    	
    }
    
    private void configureColumns() {
    	SelectColumnsDialog selectColumnsDialog = new SelectColumnsDialog(this, modelManager);
    	if (selectColumnsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		selectColumnsDialog.saveSelections();
    		modelManager.setSourceColumnSizes(itemView);
    		itemView.setupPersistentEditors();
    	}    	    	
    }
    
}
