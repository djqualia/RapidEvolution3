package com.mixshare.rapid_evolution.ui.model.tree;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.CommonModelManager;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt.ItemDataRole;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QStandardItemModel;

/**
 * Allows overrides of the source model specific to the tree...
 */
public class TreeItemModel extends QStandardItemModel implements Serializable {

    static private final long serialVersionUID = 0L;    	
	
    static private Logger log = Logger.getLogger(TreeItemModel.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
    protected CommonModelManager modelManager;
    
	/////////////////
	// CONSTRUCTOR //
	/////////////////
    
    public TreeItemModel() { }
	public TreeItemModel(int numRows, int numColumns, QObject parent, CommonModelManager modelManager) {
		super(numRows, numColumns, parent);
		this.modelManager = modelManager;
	}

	/////////////
	// GETTERS //
	/////////////
	
    public CommonModelManager getModelManager() {
		return modelManager;
	}
    
    /////////////
    // SETTERS //
    /////////////
    
	public void setModelManager(TreeModelManager modelManager) {
		this.modelManager = modelManager;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public Object headerData(int section, Orientation orientation, int role) {
		// NOTE: It was necessary to override this method to get new column titles to show up 
		// after calling appendColumn on the model (perhaps there's a better way?)
		if (orientation == Orientation.Horizontal) {
            if (role == ItemDataRole.DisplayRole) {
            	if (section != -1)
            		return modelManager.getSourceColumnTitle(section);
            }
        }
        return super.headerData(section, orientation, role);
	}	    
	
    public void resetData() {
    	clear();
    }
    
}
