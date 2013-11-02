package com.mixshare.rapid_evolution.ui.model.tree;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.model.CommonModelManager;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QTreeView;
import com.trolltech.qt.gui.QHeaderView.ResizeMode;

abstract public class TreeModelManager extends CommonModelManager {

    static private Logger log = Logger.getLogger(TreeModelManager.class);    
       
    ////////////
    // FIELDS //
    ////////////
    
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////
	
    public TreeModelManager() { super(); }
    public TreeModelManager(LineReader lineReader) {
    	super(lineReader);
    	int version = Integer.parseInt(lineReader.getNextLine());
    }
    
    ////////////////////
    // INITIALIZATION //
    ////////////////////
    
	public void initialize(QObject parent) {
		// if the primary id column was moved, move it to the first one (there seems to be a problem where you could not
		// expand rows if the primary id column was moved from the first position).. this is a fix for that
		if (viewColumns.size() != 0) {
			for (Column staticColumn : getAllStaticColumns()) {
				if (staticColumn.isIdColumn()) {
					int index = viewColumns.indexOf(staticColumn);
					Column removed = viewColumns.remove(index);
					viewColumns.insertElementAt(removed, 0);
				}
			}
		}
		super.initialize(parent);
	}		
			
    /////////////
    // GETTERS //
    /////////////    
	
	public TreeItemModel getTreeItemModel() { return (TreeItemModel)model; }		
	
	/////////////
	// SETTERS //
	/////////////
	
	/**
	 * Called when the view is constructed, to set the initial column sizes
	 */
	public void setSourceColumnSizes(QTreeView treeView) {
		treeView.header().setResizeMode(ResizeMode.Fixed);		
		int index = 0;
		for (int c = 0; c < getNumColumns(); ++c) {
			//if (!getSourceColumnType(c).isHidden()) {
				if (log.isTraceEnabled())
					log.trace("setSourceColumnSizes(): setting size=" + getSourceColumnType(c).getSize() + ", for column=" + getSourceColumnType(c));
				//treeView.header().resizeSection(index, getSourceColumnType(c).getSize());
				treeView.header().sectionResized.disconnect();
				treeView.setColumnWidth(index, getSourceColumnType(c).getSize());
				treeView.header().sectionResized.connect(this, "columnResized(Integer,Integer,Integer)");
				++index;
			//}
		}
		treeView.header().setResizeMode(ResizeMode.Interactive);
		int i = 0;
		for (Column column : getSourceColumnOrder())
			treeView.setColumnHidden(i++, column.isHidden());		
	}
	
	/////////////
	// METHODS //
	/////////////

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); //version
	}
	
}
