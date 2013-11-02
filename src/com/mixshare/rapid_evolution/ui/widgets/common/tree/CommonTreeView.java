package com.mixshare.rapid_evolution.ui.widgets.common.tree;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.index.CommonIndex;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.record.HierarchicalRecord;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.ui.model.tree.RecordTreeModelManager;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.updaters.view.tree.TreeViewUpdater;
import com.mixshare.rapid_evolution.ui.util.Color;
import com.mixshare.rapid_evolution.ui.widgets.common.ItemDelegate;
import com.mixshare.rapid_evolution.ui.widgets.search.SearchWidgetUI;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.KeyboardModifier;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QItemSelection;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QStyle;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QItemSelectionModel.SelectionFlag;

abstract public class CommonTreeView extends SortTreeView implements IndexChangeListener {

	static private Logger log = Logger.getLogger(CommonTreeView.class);
	
	static private Color SELECTION_EXCLUDE_COLOR = RE3Properties.getColor("selection_exclude_color");
	static private Color SELECTION_REQUIRE_COLOR = RE3Properties.getColor("selection_require_color");
	static private Color SELECTION_OPTIONAL_COLOR = RE3Properties.getColor("selection_optional_color");
	
	////////////
	// FIELDS //
	////////////
	
    protected byte targetSelectionState = FilterHierarchyInstance.SELECTION_STATE_OR;
    protected byte targetSelectionMode = FilterHierarchyInstance.SELECTION_MODE_TOGGLE;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public CommonTreeView(FilterModelManager modelManager) {
		super(modelManager);				
		
		// setup default tree behavior
		setUniformRowHeights(true);
    	setItemDelegate(new ItemDelegate(this, modelManager));    	
        setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers);
        setSelectionMode(SelectionMode.MultiSelection);
        setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectRows);
        setAlternatingRowColors(false);        
        setSortingEnabled(true);
        setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);
        sortByColumn(0, Qt.SortOrder.AscendingOrder);               
        setDragEnabled(true);
        setAcceptDrops(true);        
        setDropIndicatorShown(true);  
        
        addChangeListeners();
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public void updateActionMenu();
	abstract protected void executeSelectionChangeActions();
	abstract protected void highlightFiltersMatchingSelection(FilterRecord filter, QStyleOptionViewItem options);
	abstract protected void selectedFilter(FilterHierarchyInstance filterInstance);
	
	/////////////
	// GETTERS //
	/////////////
    
	public RecordTreeModelManager getTreeModelManager() { return (RecordTreeModelManager)modelManager; }
    public FilterModelManager getFilterModelManager() { return (FilterModelManager)getModelManager(); }
    
    public FilterSelection getFilterSelection() { return getFilterModelManager().getFilterSelection(); }
		    
    /**
     * The "current" selected instances are those after the filter has been applied, and does not
     * represent all selected filters...
     */
    public Vector<FilterHierarchyInstance> getCurrentSelectedInstances() {
    	//List<QModelIndex> selectedIndexes = selectionModel().selectedRows();
    	List<QModelIndex> selectedIndexes = selectionModel().selectedIndexes(); // there was a certain scenario where selectedRows wasn't returning the right result...
    	if (log.isTraceEnabled())
    		log.trace("getCurrentSelectedInstances(): # selected indices=" + selectedIndexes.size() + ", selectedIndexes=" + selectedIndexes);
    	Vector<FilterHierarchyInstance> selectedInstances = new Vector<FilterHierarchyInstance>();
    	for (QModelIndex index : selectedIndexes) {    		    		
    		if (index.column() == 0) {
	    		QModelIndex sourceIndex = getProxyModel().mapToSource(index);
	    		QStandardItem sourceItem = ((QStandardItemModel)modelManager.getSourceModel()).itemFromIndex(sourceIndex);
	    		FilterHierarchyInstance filterInstance = (FilterHierarchyInstance)sourceItem.data();
	    		selectedInstances.add(filterInstance);
    		}
    	}
    	if (log.isTraceEnabled())
    		log.trace("getCurrentSelectedInstances(): selectedInstances=" + selectedInstances);
    	return selectedInstances;
    }

    public Vector<FilterHierarchyInstance> getAllSelectedInstances() { return getFilterModelManager().getSelectedInstances(); }
    
    /////////////
	// METHODS //
	/////////////
    
	public void setupEventListeners() {
        selectionModel().selectionChanged.connect(this, "selectionChanged()");
        getProxyModel().rowsInserted.connect(this, "rowsInsertedEvent(QModelIndex,Integer,Integer)");
        getProxyModel().rowsRemoved.connect(this, "rowsRemovedEvent(QModelIndex,Integer,Integer)");
		if (modelManager.getNumColumns() > 1) { // used to be getNumVisibleColumns
	        header().sectionResized.connect(modelManager, "columnResized(Integer,Integer,Integer)");
	        header().sectionMoved.connect(modelManager, "columnMoved(Integer,Integer,Integer)");			
		}        
        selectionChanged(); // will trigger the context menu to be setup initially        
	}    

    public void ensureFilterIsVisible(FilterRecord filter) {
    	Vector<TreeHierarchyInstance> treeInstances = getFilterModelManager().getMatchingInstances(filter);
    	for (TreeHierarchyInstance treeInstance : treeInstances) {
    		QModelIndex sourceIndex = getFilterModelManager().getIndexOfInstance(treeInstance);
    		QModelIndex proxyModel = getProxyModel().mapFromSource(sourceIndex);
    		if (proxyModel != null) {
    			scrollTo(proxyModel);
    			return;
    		}
    	}
    }	
	
	protected void clearAllSelection() {
    	for (FilterHierarchyInstance filterInstance : getAllSelectedInstances()) {
    		filterInstance.setSelectionState(FilterHierarchyInstance.SELECTION_STATE_NONE);
    	}		
		clearSelection();
	}	
	
	protected void disableSelectionChangedListener() {
		selectionModel().selectionChanged.disconnect(this, "selectionChanged()");
	}
	
	protected void enableSelectionChangedListener() {
		selectionModel().selectionChanged.connect(this, "selectionChanged()");
	}
	
	protected void addChangeListeners() {
		((CommonIndex)getFilterModelManager().getIndex()).addIndexChangeListener(this);
	}
		
	public void addMissingSelections() {
		if (log.isTraceEnabled())
			log.trace("addMissingSelections(): starting...");
		disableSelectionChangedListener();
		Vector<FilterHierarchyInstance> selectedInstances = getAllSelectedInstances();
		if (log.isTraceEnabled())
			log.trace("addMissingSelections(): selectedInstances=" + selectedInstances);
		for (FilterHierarchyInstance filterInstance :  selectedInstances) {
			QModelIndex sourceFilterIndex = getTreeModelManager().getIndexOfInstance(filterInstance);
			QModelIndex proxyFilterIndex = modelManager.getProxyModel().mapFromSource(sourceFilterIndex);
			if (proxyFilterIndex != null) {
				QModelIndex proxyFilterIndex1 = model().index(proxyFilterIndex.row(), 0, proxyFilterIndex.parent());
				QModelIndex proxyFilterIndex2 = model().index(proxyFilterIndex.row(), modelManager.getNumColumns() - 1, proxyFilterIndex.parent()); // used to be getNumVisibleColumns()
				selectionModel().select(new QItemSelection(proxyFilterIndex1, proxyFilterIndex2), SelectionFlag.Select);				
			}
		}
		enableSelectionChangedListener();
		if (log.isTraceEnabled())
			log.trace("addMissingSelections(): done");
	}		
	
	/**
	 * This method is used to make the state always active so filters remain highlighted when the focus is changed.
	 * It also sets the background color based on the selection type...
	 */
	protected void drawRow(QPainter painter, QStyleOptionViewItem options, QModelIndex proxyIndex) {
		
		if (true)
			super.drawRow(painter, options, proxyIndex);
		
		QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(proxyIndex);
		QStandardItem sourceItem = ((QStandardItemModel)modelManager.getSourceModel()).itemFromIndex(sourceIndex);
		if (sourceItem == null)
			return;
		FilterHierarchyInstance treeInstance = (FilterHierarchyInstance)sourceItem.data();
		QStyle.State state = options.state();
		state.set(QStyle.StateFlag.State_Active);
		options.setState(state);
		if (treeInstance != null) {
			if (treeInstance.isSelected()) {
				QPalette p = options.palette();
				if (treeInstance.getSelectionState() == FilterHierarchyInstance.SELECTION_STATE_NOT)
					p.setColor(QPalette.ColorRole.Highlight, SELECTION_EXCLUDE_COLOR.getQColor());
				else if (treeInstance.getSelectionState() == FilterHierarchyInstance.SELECTION_STATE_AND)
					p.setColor(QPalette.ColorRole.Highlight, SELECTION_REQUIRE_COLOR.getQColor());
				else if (treeInstance.getSelectionState() == FilterHierarchyInstance.SELECTION_STATE_OR)
					; //p.setColor(QPalette.ColorRole.Highlight, SELECTION_OPTIONAL_COLOR.getQColor());
				options.setPalette(p);
			} else {				
				if (RE3Properties.getBoolean("highlight_filters_of_selected_search_items") && (SearchWidgetUI.instance != null) && (SearchWidgetUI.instance.getLastNumSelected() == 1)) {
					FilterRecord filter = treeInstance.getFilterRecord();
					highlightFiltersMatchingSelection(filter, options);
				}
			}
			super.drawRow(painter, options, proxyIndex);
		}		
	}
	
	////////////
	// EVENTS //
	////////////
	
	public void selectionChanged() {
		FilterHierarchyInstance selectedInstance = null;
    	Vector<FilterHierarchyInstance> selectedInstances = getCurrentSelectedInstances();
    	if (targetSelectionMode == FilterHierarchyInstance.SELECTION_MODE_TOGGLE) {
	    	for (FilterHierarchyInstance filterInstance : selectedInstances) {
	    		if (!filterInstance.isSelected()) {
	        		if (log.isDebugEnabled())
	        			log.debug("selectionChanged(): selected=" + filterInstance);
	        		filterInstance.setSelectionState(targetSelectionState);
	        		selectedInstance = filterInstance;
	    		}
	    	}
	    	for (FilterHierarchyInstance filterInstance : getAllSelectedInstances()) {
	    		QModelIndex sourceIndex = getTreeModelManager().getIndexOfInstance(filterInstance);
	    		QModelIndex proxyIndex = modelManager.getProxyModel().mapFromSource(sourceIndex);
	    		if (proxyIndex != null) { // makes sure the filter is visible, will avoid de-selecting filters that are not visible at the time of selection
					boolean found = false;
					for (TreeHierarchyInstance treeInstance : selectedInstances) {
						if (treeInstance.equals(filterInstance)) {
							found = true;
							break;
						}
					}
					if (!found) {
		        		if (log.isDebugEnabled())
		        			log.debug("selectionChanged(): de-selected=" + filterInstance);    				
		        		filterInstance.setSelectionState(FilterHierarchyInstance.SELECTION_STATE_NONE);
					}
	    		}
			}
    	} else if (targetSelectionMode == FilterHierarchyInstance.SELECTION_MODE_NORMAL) {
	    	for (FilterHierarchyInstance filterInstance : selectedInstances) {
	    		if (!filterInstance.isSelected()) {
	        		if (log.isDebugEnabled())
	        			log.debug("selectionChanged(): selected=" + filterInstance);
	        		selectedInstance = filterInstance;
	    		}
	    	}    		
	    	if (selectedInstance != null)
	    		clearOtherSelections(selectedInstance.getRecord());
	    	else {
	        	Vector<FilterHierarchyInstance> allSelectedInstances = getAllSelectedInstances();
	        	for (FilterHierarchyInstance filterInstance : allSelectedInstances)
	        		filterInstance.setSelectionState(FilterHierarchyInstance.SELECTION_STATE_NONE);
	        	selectionModel().clearSelection();	        	
	    	}
    	}
    	if (selectedInstance != null)
    		selectedFilter(selectedInstance);
    	updateActionMenu();
    	executeSelectionChangeActions();
	}
	
    private void rowsInsertedEvent(QModelIndex p1, Integer first, Integer last) { updateActionMenu(); }    
    private void rowsRemovedEvent(QModelIndex index, Integer first, Integer last) { updateActionMenu(); }	
	
    protected void mousePressEvent(QMouseEvent event) {
    	if (event.modifiers().isSet(KeyboardModifier.ControlModifier)) {
    		targetSelectionState = FilterHierarchyInstance.SELECTION_STATE_AND;
    		targetSelectionMode = FilterHierarchyInstance.SELECTION_MODE_TOGGLE;
    	} else if (event.modifiers().isSet(KeyboardModifier.AltModifier)) {
    		targetSelectionState = FilterHierarchyInstance.SELECTION_STATE_NOT;
    		if (!RE3Properties.getBoolean("enable_toggle_selection"))
    			targetSelectionMode = FilterHierarchyInstance.SELECTION_MODE_NORMAL;
    		else
    			targetSelectionMode = FilterHierarchyInstance.SELECTION_MODE_TOGGLE;
    	} else if (event.modifiers().isSet(KeyboardModifier.ShiftModifier)) {
    		targetSelectionState = FilterHierarchyInstance.SELECTION_STATE_OR;
    		targetSelectionMode = FilterHierarchyInstance.SELECTION_MODE_TOGGLE;
    	} else {
    		targetSelectionState = FilterHierarchyInstance.SELECTION_STATE_OR;
    		if (!RE3Properties.getBoolean("enable_toggle_selection"))
    			targetSelectionMode = FilterHierarchyInstance.SELECTION_MODE_NORMAL;
    		else
    			targetSelectionMode = FilterHierarchyInstance.SELECTION_MODE_TOGGLE;
    	}
    	super.mousePressEvent(event);
    }
    
    protected void clearOtherSelections(HierarchicalRecord record) {
    	Vector<FilterHierarchyInstance> selectedInstances = getAllSelectedInstances();
    	for (FilterHierarchyInstance filterInstance : selectedInstances)
    		filterInstance.setSelectionState(FilterHierarchyInstance.SELECTION_STATE_NONE);
    	selectionModel().clearSelection();
    	Vector<TreeHierarchyInstance> treeInstances = getTreeModelManager().getMatchingInstances(record);
    	for (TreeHierarchyInstance treeInstance : treeInstances) {
    		if (treeInstance.isSelected())
    			treeInstance.setSelectionState(FilterHierarchyInstance.SELECTION_STATE_NONE);
    		else {
    			treeInstance.setSelectionState(targetSelectionState);
	    		QModelIndex sourceIndex = getTreeModelManager().getIndexOfInstance(treeInstance);
	    		if (sourceIndex != null) {
					QModelIndex proxyIndex = getTreeModelManager().getProxyModel().mapFromSource(sourceIndex);
					if (proxyIndex != null) {
		    			for (int i = 0; i < modelManager.getNumColumns(); ++i) // used to be getNumVisibleColumns()    				
		    				selectionModel().select(getTreeModelManager().getProxyModel().index(proxyIndex.row(), i, proxyIndex.parent()), SelectionFlag.Select);
					}
	    		}
    		}
    	}      	
    }
    
    public void setTargetSelectionState(byte targetSelectionState) { this.targetSelectionState = targetSelectionState; }
    
	public void mouseMoveEvent(QMouseEvent event) {
		// can make it so only 1 item can be dragged at a time here, if desired
		//if (selectionModel().selectedRows().size() != 1)
			//return;
		super.mouseMoveEvent(event);
	}	    
    
	public void addedRecord(Record record, SubmittedProfile submittedProfile) { }
	public void removedRecord(Record record) { }
	
	/**
	 * This method calls update on the widget and all child widgets.  It was found that this was
	 * needed to force the repaint of elements that would otherwise wait until the object received focus....
	 */
	public void updatedRecord(Record record) {
		if (!RapidEvolution3.isTerminated)
			QApplication.invokeAndWait(new TreeViewUpdater(this));
	}    
}
