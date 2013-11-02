package com.mixshare.rapid_evolution.ui.widgets.profile.search;

import java.util.List;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.profile.common.link.Link;
import com.mixshare.rapid_evolution.ui.model.profile.search.LinkModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.table.CommonTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDesktopServices;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QItemSelection;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QMouseEvent;

public class LinkTableView extends CommonTableView {

	static private Logger log = Logger.getLogger(LinkTableView.class);

	////////////
	// FIELDS //
	////////////
	
	private QAction removeAction;
	
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
	public LinkTableView(LinkModelManager modelManager) {
		super(modelManager);				        
        setDragEnabled(false);

        setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);
        
        removeAction = new QAction(Translations.get("remove_text"), this);
        removeAction.triggered.connect(this, "removeLinks()");
        removeAction.setIcon(new QIcon(RE3Properties.getProperty("menu_remove_icon")));
                
	}	    

	/////////////
	// GETTERS //
	/////////////
	
	public LinkModelManager getLinkModelManager() { return (LinkModelManager)this.getTableModelManager(); }
	
	////////////////////
	// SLOTS (EVENTS) //
	////////////////////

	protected void selectionChanged(QItemSelection selected, QItemSelection deselected) {
		super.selectionChanged(selected, deselected);
		int numSelectedRows = selectionModel().selectedRows().size();
		if (numSelectedRows > 0) {
			addAction(removeAction);
		} else {
			removeAction(removeAction);
		}
	}
	
	public void removeLinks() {
		if (QMessageBox.question(this, Translations.get("dialog_remove_link_title"), Translations.get("dialog_remove_link_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}    			
    	List<QModelIndex> selectedIndexes = selectionModel().selectedRows();
    	for (QModelIndex index : selectedIndexes) {
    		QModelIndex sourceIndex = getProxyModel().mapToSource(index);    		
    		Link link = (Link)getLinkModelManager().getLinkForRow(sourceIndex.row());
    		link.setDisabled(true);
    	}
    	ProfileWidgetUI.instance.getCurrentProfile().save();
    	selectionModel().clearSelection();
	}
	
    protected void mouseDoubleClickEvent(QMouseEvent event) {
    	try {
	    	QModelIndex proxyIndex = indexAt(event.pos());
	    	QModelIndex sourceIndex = getProxyModel().mapToSource(proxyIndex);
	    	Link link = (Link)getTableModelManager().getObjectForRow(sourceIndex.row());
	    	if (log.isDebugEnabled())
	    		log.debug("mouseDoubleClickEvent(): opening link=" + link);
	    	QDesktopServices.openUrl(new QUrl(link.getUrl()));
    	} catch (Exception e) {
    		log.error("mouseDoubleClickEvent(): error", e);
    	}    	
    }
    
    
}
