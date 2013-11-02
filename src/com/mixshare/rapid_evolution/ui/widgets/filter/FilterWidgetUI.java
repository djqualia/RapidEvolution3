package com.mixshare.rapid_evolution.ui.widgets.filter;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.index.CommonIndex;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.filter.FilterSearchParameters;
import com.mixshare.rapid_evolution.ui.dialogs.columns.SelectColumnsDialog;
import com.mixshare.rapid_evolution.ui.model.SortFilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.column.ColumnOrdering;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.FilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.updaters.view.filter.FilterLazySearch;
import com.mixshare.rapid_evolution.ui.util.TextInputSearchDelay;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.search.SearchBarWidget;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

abstract public class FilterWidgetUI extends QWidget {
	
	static private Logger log = Logger.getLogger(FilterWidgetUI.class);
    		
    ////////////
    // FIELDS //
    ////////////
    
    protected FilterTreeView filterView;
    
    protected SearchBarWidget searchBarWidget;
    
    private QLabel enterSearchText;

    private FilterProxyModel proxyModel;    
    protected FilterModelManager modelManager;
    
    private TextInputSearchDelay searchDelay;
        
    transient private Vector<SearchResult> searchResults;
    transient private FilterSearchParameters searchParameters;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public FilterWidgetUI(FilterModelManager modelManager) {    	    	
    	this.modelManager = modelManager;

    	QSizePolicy searchWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
    	setSizePolicy(searchWidgetSizePolicy);
    	

    	QVBoxLayout searchLayout = new QVBoxLayout(this);    	
    	searchLayout.setMargin(0);    	    	
    	
    	searchBarWidget = new SearchBarWidget(false, 0);
    	
        // filter view
        filterView = createFilterTreeView(modelManager);    	
                
        enterSearchText = new QLabel(Translations.get("enter_search_text"));
        enterSearchText.setVisible(false);
        
        searchLayout.addWidget(searchBarWidget);
        searchLayout.addWidget(enterSearchText);
        searchLayout.setAlignment(enterSearchText, Qt.AlignmentFlag.AlignCenter);
        searchLayout.addWidget(filterView);

		if (modelManager.getNumVisibleColumns() == 1) {
			filterView.header().hide();
			filterView.header().setStretchLastSection(true);
		} else {
			filterView.header().show();
			filterView.header().setStretchLastSection(false);
		}
        
        createModels();

        filterView.initHideEmpty();
        
        searchDelay = getTextInputSearchDelay();
        
        searchBarWidget.getFilterText().textChanged.connect(this, "searchTextChanged()");
        searchBarWidget.getConfigureColumnsButton().clicked.connect(this, "configureColumns()");        
    }

    private void createModels() {
    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_initializing_prefix") + " "  + modelManager.getTypeDescription() + Translations.get("splash_screen_tree_suffix_plural"));
    	modelManager.initialize(this);
    	((CommonIndex)modelManager.getIndex()).addIndexChangeListener(modelManager);
    	((CommonIndex)modelManager.getIndex()).addProfilesMergedListener(modelManager);
        proxyModel = getFilterProxyModel();
        proxyModel.setUpdateSort(false);
        filterView.setModel(proxyModel);
        proxyModel.setUpdateSort(true);
        // set initial column sizes 
        modelManager.setSourceColumnSizes(filterView);        
        filterView.setupEventListeners();                      
        
        if ((modelManager.getSortOrdering() != null) && (modelManager.getSortOrdering().size() > 0)) {
	        if (log.isDebugEnabled())
	        	log.debug("createModels(): " + modelManager.getTypeDescription() + " sort ordering=" + modelManager.getSortOrdering());
	    	RapidEvolution3.updateSplashScreen(Translations.get("splash_screen_sorting_prefix") + " "  + modelManager.getTypeDescription() + " " + Translations.get("splash_screen_tree_suffix"));
	    	if (!(modelManager.isLazySearchSupported() && RE3Properties.getBoolean("lazy_search_mode")))
	    		filterView.sortByColumn(modelManager.getPrimarySortColumnOrdering().getColumnId());	    	
        }   
          
    }
            
    /////////////
    // GETTERS //
    /////////////

    public FilterSelection getFilterSelection() { return filterView.getFilterSelection(); }    
       
    public FilterTreeView getFilterTreeView() { return filterView; }
            
    //////////////////////
    // ABSTRACT METHODS //
    //////////////////////
    
    abstract protected TextInputSearchDelay getTextInputSearchDelay();    
    
    abstract public FilterProxyModel getFilterProxyModel();
    
    abstract public FilterTreeView createFilterTreeView(FilterModelManager modelManager);
    
    /////////////
    // METHODS //
    /////////////
    
    public void clearSelections() {
    	filterView.clearSelection();
    }
    
    ///////////
    // SLOTS //
    ///////////
              
    public void searchTextChanged() {
    	searchDelay.searchTextChanged(searchBarWidget.getFilterText().text());
    }
    
    public void updateFilter() {
    	if (log.isTraceEnabled())
    		log.trace("updateFilter(): invalidating...");
        proxyModel.setSearchText(searchBarWidget.getFilterText().text());
        if (SortFilterProxyModel.EMPTY_INITIAL_RESULTS_MODE && modelManager.isLazySearchSupported()) {
        	filterView.setEnabled(false);
        	TaskManager.runForegroundTask(new FilterLazySearch(this, searchBarWidget.getFilterText().text()));
        } else {        	
        	proxyModel.applyFilter();
        }
        filterView.addMissingSelections();
        filterView.updateActionMenu();
    }    
    
    public void lazySearch(String searchText) {
    	searchParameters = proxyModel.getNewFilterSearchParameters();
		// right now there's only searching by text...
		searchParameters.setSearchText(searchText);
		Vector<ColumnOrdering> sortOrdering = modelManager.getSortOrdering();
		if (sortOrdering != null) {
			byte[] sortTypes = new byte[sortOrdering.size()];
			boolean[] sortDescending = new boolean[sortOrdering.size()];
			int i = 0;
			for (ColumnOrdering ordering : sortOrdering) {
				if (log.isTraceEnabled())
					log.trace("lazySearch(): type=" + DataConstantsHelper.getDataTypeDescription(modelManager.getFilterIndex().getDataType()) + "s, ordering=" + ordering);
				sortDescending[i] = !ordering.isAscending();
				sortTypes[i] = CommonSearchParameters.getSortTypeFromColumnId(ordering.getColumnId());
				++i;
			}
			searchParameters.setSortType(sortTypes);
			searchParameters.setSortDescending(sortDescending);
		}    
		searchResults = new Vector<SearchResult>();
		if (!searchParameters.isEmpty() || RE3Properties.getBoolean("show_lazy_filters_on_empty_parameters"))
			searchResults = proxyModel.getFilterModelManager().getIndex().searchRecords(searchParameters);
		if (log.isTraceEnabled())			
			log.trace("lazySearch(): results=" + searchResults);
		QApplication.invokeAndWait(new Runnable() { public void run() {
			lazySearchPostResults();
		}});
    }
    
    public void lazySearchPostResults() {    	
		proxyModel.getFilterModelManager().loadData(searchResults, searchParameters);
		modelManager.setSourceColumnSizes(filterView);
    	filterView.setEnabled(true);
    	if (searchParameters.isEmpty() && !RE3Properties.getBoolean("show_lazy_filters_on_empty_parameters"))
    		enterSearchText.setVisible(true);
    	else
    		enterSearchText.setVisible(false);
    }
    
    private void configureColumns() {
    	SelectColumnsDialog selectColumnsDialog = new SelectColumnsDialog(this, modelManager);
    	if (selectColumnsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		selectColumnsDialog.saveSelections();
    		updateVisibleColumns();
    	}       	
    }

    public void updateVisibleColumns() {
		if (modelManager.getNumVisibleColumns() == 1) {
			filterView.header().hide();
	        filterView.header().sectionResized.disconnect(modelManager, "columnResized(Integer,Integer,Integer)");
	        filterView.header().sectionMoved.disconnect(modelManager, "columnMoved(Integer,Integer,Integer)");
	        filterView.header().setStretchLastSection(true);
		} else {
			filterView.header().show();    			    			
	        filterView.header().sectionResized.connect(modelManager, "columnResized(Integer,Integer,Integer)");
	        filterView.header().sectionMoved.connect(modelManager, "columnMoved(Integer,Integer,Integer)");
	        filterView.header().setStretchLastSection(false);
		}
		proxyModel.applyFilter();
		modelManager.setSourceColumnSizes(filterView);    	
    }
    
}
