package com.mixshare.rapid_evolution.ui.widgets.filter.playlists;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.ui.model.filter.FilterModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.FilterProxyModel;
import com.mixshare.rapid_evolution.ui.model.filter.playlist.PlaylistModelManager;
import com.mixshare.rapid_evolution.ui.model.filter.playlist.PlaylistProxyModel;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.mixshare.rapid_evolution.ui.util.TextInputSearchDelay;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterTreeView;
import com.mixshare.rapid_evolution.ui.widgets.filter.FilterWidgetUI;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QItemSelectionModel.SelectionFlag;

public class PlaylistsWidgetUI extends FilterWidgetUI {
	
	static private Logger log = Logger.getLogger(PlaylistsWidgetUI.class);
        
	static public PlaylistsWidgetUI instance = null;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public PlaylistsWidgetUI(PlaylistModelManager modelManager) {
    	super(modelManager);
    	instance = this;
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public FilterProxyModel getFilterProxyModel() { return new PlaylistProxyModel(this, modelManager, filterView, this); }    
    
    public PlaylistModelManager getModelManager() { return (PlaylistModelManager)modelManager; }
    
    /////////////
    // METHODS //
    /////////////
    
    protected TextInputSearchDelay getTextInputSearchDelay() {
    	return new PlaylistFilterTextInputSearchDelay();
    }
        
    public FilterTreeView createFilterTreeView(FilterModelManager modelManager) {
    	return new PlaylistsTreeView(modelManager);
    }
    
    public void selectPlaylist(PlaylistRecord playlistRecord) {
    	Vector<TreeHierarchyInstance> treeInstances = getFilterTreeView().getTreeModelManager().getMatchingInstances(playlistRecord);
    	for (TreeHierarchyInstance treeInstance : treeInstances) {
    		if (!treeInstance.isSelected()) {
	    		QModelIndex sourceIndex = getFilterTreeView().getTreeModelManager().getIndexOfInstance(treeInstance);
	    		if (sourceIndex != null) {
					QModelIndex proxyIndex = getFilterTreeView().getTreeModelManager().getProxyModel().mapFromSource(sourceIndex);
					if (proxyIndex != null) {
		    			for (int i = 0; i < modelManager.getNumColumns(); ++i) // used to be getNumVisibleColumns()    				
		    				filterView.selectionModel().select(getFilterTreeView().getTreeModelManager().getProxyModel().index(proxyIndex.row(), i, proxyIndex.parent()), SelectionFlag.Select);
					}
	    		}
    		}
    	}      	
    	playlistRecord.update();
    }
    
}
