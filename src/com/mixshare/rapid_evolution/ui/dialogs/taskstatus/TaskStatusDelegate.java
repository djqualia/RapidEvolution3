package com.mixshare.rapid_evolution.ui.dialogs.taskstatus;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.FocusPolicy;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QItemDelegate;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QWidget;

public class TaskStatusDelegate extends QItemDelegate implements AllColumns, DataConstants {
	
	static private Logger log = Logger.getLogger(TaskStatusDelegate.class);
	
	private boolean foreground;
	
    public TaskStatusDelegate(QWidget parent, boolean foreground) {
        super(parent);
        this.foreground = foreground;
    }

    @Override
    public void paint(QPainter painter, QStyleOptionViewItem option, QModelIndex index) {
        super.paint(painter, option, index);
    }

    @Override
    public QSize sizeHint(QStyleOptionViewItem option, QModelIndex index) {
        return super.sizeHint(option, index);
    }

    @Override
    public QWidget createEditor(QWidget parent, QStyleOptionViewItem item, QModelIndex index) {    	
		Task task = null;
		if (foreground)
			task = TaskManager.getCurrentForegroundTask(index.row());
		else 
			task = TaskManager.getCurrentBackgroundTask(index.row());
		boolean isCancellable = true;
		if ((task == null) || !task.isCancellable())
			isCancellable = false;
		boolean supportsProgress = true;
		if ((task == null) || task.isIndefiniteTask())
			supportsProgress = false;
		if (index.column() == 1) {
    		QPushButton button = new QPushButton(parent);
    		button.setText(Translations.get("task_progress_text"));
    		button.clicked.connect(new ProgressTask(parent, index.row(), foreground), "openProgressDialog(Boolean)");
    		button.setFocusPolicy(FocusPolicy.NoFocus);
    		if (!supportsProgress)
    			button.setDisabled(true);
    		return button;    			
    	} else if (index.column() == 2) {
    		QPushButton button = new QPushButton(parent);
    		button.setText(Translations.get("task_cancel_text"));
    		button.clicked.connect(new CancelTask(parent, index.row(), foreground), "cancelTask(Boolean)");
    		button.setFocusPolicy(FocusPolicy.NoFocus);
    		if (!isCancellable)
    			button.setDisabled(true);
    		return button;    		
    	}    	
        return null;
    }

    @Override
    public void setEditorData(QWidget editor, QModelIndex index) {
        super.setEditorData(editor, index);
    }

    @Override
    public void setModelData(QWidget editor, QAbstractItemModel proxyModel, QModelIndex index) {    	
    	super.setModelData(editor, proxyModel, index);
    }
    
    static private class CancelTask {
    	private QWidget widget;
    	private int row;
    	private boolean foreground;
    	public CancelTask(QWidget widget, int row, boolean foreground) {
    		this.widget = widget;
    		this.row = row;
    		this.foreground = foreground;
    	}
	    protected void cancelTask(Boolean checked) {
	    	try {
	    		Task task = null;
	    		if (foreground)
	    			task = TaskManager.getCurrentForegroundTask(row);
	    		else
	    			task = TaskManager.getCurrentBackgroundTask(row);
	    		if (task != null) {
					String text = Translations.get("task_cancel_description");
					text = StringUtil.replace(text, Translations.getPreferredCase("%taskDescription%"), task.toString());	    			
	    			if (QMessageBox.question(widget, Translations.get("task_cancel_title"), text, QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
	    				return;
	    			}			
	    			task.cancel();
	    		}
	    	} catch (Exception e) {
	    		log.error("cancelTask(): error", e);
	    	}
	    }
    }
    
    static private class ProgressTask {
    	private QWidget widget;
    	private int row;
    	private boolean foreground;
    	public ProgressTask(QWidget widget, int row, boolean foreground) {
    		this.widget = widget;
    		this.row = row;
    		this.foreground = foreground;
    	}
	    protected void openProgressDialog(Boolean checked) {
	    	try {
	    		Task task = null;
	    		if (foreground)
	    			task = TaskManager.getCurrentForegroundTask(row);
	    		else
	    			task = TaskManager.getCurrentBackgroundTask(row);
	    		if (task != null) {
            		QApplication.invokeLater(new TaskProgressLauncher(task));        		    			
	    		}
	    	} catch (Exception e) {
	    		log.error("cancelTask(): error", e);
	    	}
	    }
    }    
    
}