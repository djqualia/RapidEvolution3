package com.mixshare.rapid_evolution.ui.widgets.profile.tab;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.ui.dialogs.columns.SelectColumnsDialog;
import com.mixshare.rapid_evolution.ui.event.FilterHierarchyChangeListener;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.tree.CommonTreeView;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.MouseButton;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QStyle;
import com.trolltech.qt.gui.QStyleOptionViewItem;

abstract public class TabTreeView extends CommonTreeView implements AllColumns, DataConstants, FilterHierarchyChangeListener {

	static private Logger log = Logger.getLogger(TabTreeView.class);
	
	////////////
	// FIELDS //
	////////////
	
	protected TabTreeWidget tabTreeWidget;
	protected SearchProfile searchProfile;
	
    private QAction expandAllAction;
    private QAction collapseAllAction;
    private QAction separator;
    private QAction clearAllAction;
    protected QAction addSeparator;
    
    private TabTreeItemDelegate treeItemDelegate;
    
	transient protected boolean validMouseButtonUsed;
	transient private QColor selectionColor;
	
    static {
    	try{
    		// in order for the XMLEncoder to skip transient variables, this is needed
    		BeanInfo info = Introspector.getBeanInfo(TabTreeView.class);
    		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
    		for (int i = 0; i < propertyDescriptors.length; ++i) {
    			PropertyDescriptor pd = propertyDescriptors[i];
    			if (pd.getName().equals("validMouseButtonUsed") || pd.getName().equals("selectionColor")) {
    				pd.setValue("transient", Boolean.TRUE);
    			}
    		}
    	} catch (IntrospectionException e) {
    		log.error("static(): error", e);
    	}
    }
	
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
	public TabTreeView(TabTreeWidget tabTreeWidget, FilterModelManager modelManager) {
		super(modelManager);
		this.tabTreeWidget = tabTreeWidget;
		treeItemDelegate = new TabTreeItemDelegate(this, modelManager, this);
		setItemDelegate(treeItemDelegate);
        setSelectionBehavior(QAbstractItemView.SelectionBehavior.SelectRows);
        setSelectionMode(SelectionMode.NoSelection);
        setEditTriggers(QAbstractItemView.EditTrigger.AllEditTriggers);        
        header().setStretchLastSection(false);	
        setDragEnabled(false);
        
        expandAllAction = new QAction(Translations.get("expand_all_text"), this);
        expandAllAction.triggered.connect(this, "expandAll()");
        expandAllAction.setIcon(new QIcon(RE3Properties.getProperty("menu_expand_all_icon")));
        
        collapseAllAction = new QAction(Translations.get("collapse_all_text"), this);
        collapseAllAction.triggered.connect(this, "collapseAll()"); 	
        collapseAllAction.setIcon(new QIcon(RE3Properties.getProperty("menu_collapse_all_icon")));
        
        separator = new QAction("", this);
        separator.setSeparator(true);

        addSeparator = new QAction("", this);
        addSeparator.setSeparator(true);

        clearAllAction = new QAction(Translations.get("filter_menu_clear_selections"), this);
        clearAllAction.triggered.connect(this, "clearAllSelection()");
        clearAllAction.setIcon(new QIcon(RE3Properties.getProperty("menu_clear_selections_icon")));
        
        header().setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu);
        header().customContextMenuRequested.connect(this, "customContextMenuRequested(QPoint)");        
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setSearchProfile(SearchProfile searchProfile) {
		this.searchProfile = searchProfile;
		treeItemDelegate.setSearchProfile(searchProfile);		
	}
	
	/////////////////////
	// ABSTRACT METHOD //
	/////////////////////
	
	abstract protected float getFilterDegree(int profileUniqueId);
	abstract protected int getFilterNameSourceColumnIndex();
	abstract protected int getNumSelections();
	abstract protected boolean enablePersistentEditors();
	abstract public void invalidateProfileSelections();
	
	/////////////
	// METHODS //
	/////////////
	
	public void updateActionMenu() {
		int numSelections = getNumSelections();
        removeAction(expandAllAction);
        removeAction(collapseAllAction);
        removeAction(clearAllAction);
    	if (numSelections == 0) {
            if (modelManager.getProxyModel().rowCount() > 0) {
            	addAction(expandAllAction);
            	addAction(collapseAllAction);
            }
    	} else if (numSelections == 1) {
            if (modelManager.getProxyModel().rowCount() > 0) {
            	addAction(expandAllAction);
            	addAction(collapseAllAction);
            	addAction(separator);
            }
            addAction(clearAllAction);            
    	} else if (numSelections > 1) {
            if (modelManager.getProxyModel().rowCount() > 0) {
            	addAction(expandAllAction);
            	addAction(collapseAllAction);            	
            	addAction(separator);
            }
            addAction(clearAllAction);                        
    	}    		
	}
	
	protected void executeSelectionChangeActions() { }
	
	public void setupEventListeners() {
		super.setupEventListeners();
		clicked.connect(this, "viewClicked(QModelIndex)");
	}    
	
	public void selectionChanged() {		
    	updateActionMenu();
    	executeSelectionChangeActions();
	}	
	
	/**
	 * This method is used to make the state always active so filters remain highlighted when the focus is changed.
	 * It also sets the background color based on the selection type...
	 */
	protected void drawRow(QPainter painter, QStyleOptionViewItem options, QModelIndex proxyIndex) {
		if (log.isTraceEnabled())
			log.trace("drawRow(): proxyIndex.row()=" + proxyIndex.row());
		QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(proxyIndex);
		QStandardItem sourceItem = ((QStandardItemModel)modelManager.getSourceModel()).itemFromIndex(sourceIndex);
		QStyle.State state = options.state();
		state.set(QStyle.StateFlag.State_Active);
		options.setState(state);
		FilterHierarchyInstance treeInstance = (FilterHierarchyInstance)sourceItem.data();
		float filterDegree = getFilterDegree(treeInstance.getFilterRecord().getUniqueId());
		if (filterDegree > 0.0f) {
			QPalette p = options.palette();
			QColor baseColor = p.color(QPalette.ColorRole.Base);
			selectionColor = p.color(QPalette.ColorGroup.Active, QPalette.ColorRole.Highlight);
			QColor degreeColor = new QColor(
					(int)(filterDegree * selectionColor.red() + (1.0f - filterDegree) * baseColor.red()),
					(int)(filterDegree * selectionColor.green() + (1.0f - filterDegree) * baseColor.green()),
					(int)(filterDegree * selectionColor.blue() + (1.0f - filterDegree) * baseColor.blue())
					);
			p.setColor(QPalette.ColorRole.Highlight, degreeColor);
			if (log.isTraceEnabled())
				log.trace("drawRow(): degreeColor=" + degreeColor);
			if (filterDegree < 0.5f)
				p.setColor(QPalette.ColorRole.HighlightedText, p.color(QPalette.ColorRole.Text));
			options.setPalette(p);
			state = options.state();
			if (log.isTraceEnabled())
				log.trace("drawRow(): current state=" + state);
			state.set(QStyle.StateFlag.State_Selected);
			options.setState(state);
		}		
		super.drawRow(painter, options, proxyIndex);
	}	
	
	public void setupPersistentEditors() {
		if (enablePersistentEditors()) {
			if (log.isTraceEnabled())
				log.trace("setupPersistentEditors(): starting...");
			if (!RE3Properties.getBoolean("lazy_search_mode")) {
		    	for (int c = 0; c < modelManager.getNumColumns(); ++c) {
		    		boolean setPersistent = false;
		    		Column column = modelManager.getSourceColumnType(c);
					if (column.getColumnId() == COLUMN_DEGREE.getColumnId())
						setPersistent = true;
		    		if (setPersistent) {
		    			for (int r = 0; r < getFilterModelManager().getIndex().getSize(); ++r) {
			    			QModelIndex proxyIndex = modelManager.getProxyModel().mapFromSource(modelManager.getSourceModel().index(r, c));
			    			if (proxyIndex != null) {	    				
			    				openPersistentEditor(proxyIndex);
			    				QStandardItem item = ((QStandardItemModel)modelManager.getSourceModel()).item(r);	    				
			    				setupPersistentEditorsRecursive(item, c);
			    			}
		    			}
		    		}
		    	}
			}
	    	updateActionMenu();
			if (log.isTraceEnabled())
				log.trace("setupPersistentEditors(): done");
		}
	}
	
	protected void setupPersistentEditorsRecursive(QStandardItem item, int column) {
		for (int c = 0; c < item.rowCount(); ++c) {
			QStandardItem childItem = item.child(c);
			QModelIndex proxyIndex = modelManager.getProxyModel().mapFromSource(childItem.index());
			if (proxyIndex != null) {				
				openPersistentEditor(modelManager.getProxyModel().index(proxyIndex.row(), column, proxyIndex.parent()));
				setupPersistentEditorsRecursive(childItem, column);
			}
		}	    						
	}
	
	public void updateHierarchy(TreeHierarchyInstance sourceInstance, TreeHierarchyInstance destinationInstance, boolean copy) {		
		TreeHierarchyInstance actualSourceInstance = getFilterModelManager().getActualTreeHierarchyInstance(sourceInstance);
		TreeHierarchyInstance actualDestinationInstance = getFilterModelManager().getActualTreeHierarchyInstance(destinationInstance);
		if (actualSourceInstance != null)
			getFilterModelManager().updateHierarchy(actualSourceInstance, actualDestinationInstance, copy);
	}
	
	////////////
	// EVENTS //
	////////////
	
	public void viewClicked(QModelIndex proxyIndex) {
		if (validMouseButtonUsed && (proxyIndex != null)) {
			QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(proxyIndex);			
			if (sourceIndex.column() == getFilterNameSourceColumnIndex()) { // only allow clicks on filter name column 
				QStandardItem sourceItem = ((QStandardItemModel)modelManager.getSourceModel()).itemFromIndex(sourceIndex);
				FilterHierarchyInstance treeInstance = (FilterHierarchyInstance)sourceItem.data();
				treeInstance.setNeedsRefresh(true);
				FilterRecord filterRecord = treeInstance.getFilterRecord();
				if (log.isTraceEnabled())
					log.trace("viewClicked(): filterRecord=" + filterRecord);				
				float degree = 0.0f;
				if (filterRecord instanceof StyleRecord) {
					StyleRecord styleRecord = (StyleRecord)filterRecord;
					degree = (searchProfile.getSourceStyleDegreeFromUniqueId(styleRecord.getUniqueId()) > 0.0f) ? 0.0f : 1.0f;
					searchProfile.setStyle(new DegreeValue(styleRecord.getStyleName(), degree, DATA_SOURCE_USER));
				} else if (filterRecord instanceof TagRecord) {
					TagRecord tagRecord = (TagRecord)filterRecord;
					degree = (searchProfile.getSourceTagDegreeFromUniqueId(tagRecord.getUniqueId()) > 0.0f) ? 0.0f : 1.0f;
					searchProfile.setTag(new DegreeValue(tagRecord.getTagName(), degree, DATA_SOURCE_USER), true);
				}			
				if (log.isTraceEnabled())
					log.trace("viewClicked(): new degree=" + degree);
				ProfileWidgetUI.instance.setUpdateImmediately(true);
				TaskManager.runForegroundTask(new TreeUpdateTask());
			}
		}		
	}
	
	private class TreeUpdateTask extends CommonTask {
		public String toString() {
			return "Updating Tab Tree";
		}		
		public void execute() {
			searchProfile.getSearchRecord().update();
			QApplication.invokeLater(new ActionMenuUpdateThread());			
		}
	}
	
	private class ActionMenuUpdateThread extends Thread {
		public void run() {
			updateActionMenu();
		}
	}
			
    protected void mousePressEvent(QMouseEvent event) {
    	if (event.buttons().isSet(MouseButton.LeftButton))
    		validMouseButtonUsed = true;
    	else 
    		validMouseButtonUsed = false;
    	super.mousePressEvent(event);
    }
	
	public void unload() {
		//((CommonIndex)getFilterModelManager().getIndex()).removeIndexChangeListener(this);
		invalidateProfileSelections();
	}

	protected void highlightFiltersMatchingSelection(FilterRecord filter, QStyleOptionViewItem options) {		 }	

	////////////
	// EVENTS //
	////////////
	
	public void customContextMenuRequested(QPoint point) {
		if (log.isDebugEnabled())
			log.debug("customContextMenuRequested(): point=" + point);
    	SelectColumnsDialog selectColumnsDialog = new SelectColumnsDialog(this, modelManager);
    	if (selectColumnsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		selectColumnsDialog.saveSelections();
    		((FilterModelManager)modelManager).setSourceColumnSizes(this);
    	}    	
	}
	
}
