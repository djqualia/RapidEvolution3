package com.mixshare.rapid_evolution.ui.dialogs.taskprogress;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.util.QWidgetUtil;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.TaskProgressListener;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMoveEvent;
import com.trolltech.qt.gui.QProgressBar;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class TaskProgressDialog extends QDialog implements TaskProgressListener {

	static private Logger log = Logger.getLogger(TaskProgressDialog.class);

	private Task task;
	private QProgressBar progressBar;
    
    public TaskProgressDialog(Task task) {
        super();
        this.task = task;
        init();
    }
    
    public TaskProgressDialog(QWidget parent, Task task) {
        super(parent);
        this.task = task;
        init();
    }

    private void init() {
    	try {    	
	        setWindowTitle(task.toString());
	        setWindowIcon(new QIcon(RE3Properties.getProperty("application_icon_filename")));

	    	QSizePolicy optionsWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
	    	optionsWidgetSizePolicy.setVerticalStretch((byte)1);
	    	optionsWidgetSizePolicy.setHorizontalStretch((byte)1);
	    	setSizePolicy(optionsWidgetSizePolicy);    	    	    	
	    	
	    	QVBoxLayout optionsLayout = new QVBoxLayout(this);    	
	    	optionsLayout.setMargin(10);    
	    	
	    	progressBar = new QProgressBar();	    	
	    	if (task.isIndefiniteTask()) {
	    		progressBar.setMaximum(0);
	    		progressBar.setMinimum(0);
	    		progressBar.setTextVisible(false);	    		
	    	} else {
	    		progressBar.setMaximum(100);
	    		progressBar.setMinimum(0);
	    		progressBar.setTextVisible(true);
	    	}
	    	task.addProgressListener(this);
	    	
	    	optionsLayout.addWidget(progressBar);

	        QDialogButtonBox buttonBox = new QDialogButtonBox();
	        buttonBox.setObjectName("buttonBox");
	        buttonBox.setGeometry(new QRect(20, 480, 401, 41));
	        buttonBox.setFocusPolicy(com.trolltech.qt.core.Qt.FocusPolicy.TabFocus);
	        buttonBox.setStandardButtons(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.createQFlags(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Cancel));
	        buttonBox.setCenterButtons(true);
	        buttonBox.clicked.connect(this, "cancelTask()");	        
	        optionsLayout.addWidget(buttonBox);
	        
	        QWidgetUtil.setWidgetSize(this, getUIID(), 450, 110);
	        QWidgetUtil.setWidgetPosition(this, getUIID());
	        
    	} catch (Exception e) {
    		log.error("init(): error", e);
    	}
    }

    protected void cancelTask() {
    	if (task != null)
    		task.cancel();
    	setVisible(false);
    }
    
    public void isComplete() {
    	QApplication.invokeLater(new Completer(this));
    }
    
    public QProgressBar getProgressBar() { return progressBar; }
    
    protected void closeEvent(QCloseEvent event) {    	
    	task.removeProgressListener(this);
    	super.closeEvent(event);
    }
    
    private String getUIID() { return "task_progress_" + task.getClass().getSimpleName(); }
    private String getWidthString() {
    	return getUIID() + "_width";
    }
    
    private String getHeightString() {
    	return getUIID() + "_height";
    }
    
    private String getXPosition() {
    	return getUIID() + "_x";	
    }
    
    private String getYPosition() {
    	return getUIID() + "_y";
    }    
    
    protected void resizeEvent(QResizeEvent re) {
    	super.resizeEvent(re);
    	UIProperties.setProperty(getWidthString(), String.valueOf(re.size().width()));
    	UIProperties.setProperty(getHeightString(), String.valueOf(re.size().height()));
    }
    
    protected void moveEvent(QMoveEvent me) {
    	super.moveEvent(me);
    	UIProperties.setProperty(getXPosition(), String.valueOf(me.pos().x()));
    	UIProperties.setProperty(getYPosition(), String.valueOf(me.pos().y()));
    }
    
    public void setProgress(float progress) {
    	QApplication.invokeLater(new ProgressSetter(this, progress));
    }    
    
    static private class ProgressSetter extends Thread {
    	private TaskProgressDialog dialog;
    	private float progress;
    	public ProgressSetter(TaskProgressDialog dialog, float progress) {
    		this.dialog = dialog;
    		this.progress = progress;    	
    	}
    	public void run() {
    		try {
    			int val = (int)(progress * 100.0f);
    			dialog.getProgressBar().setValue(val);
    		} catch (Exception e) { 
    			log.error("run(): error", e);
    		}
    	}
    }
    
    static private class Completer extends Thread {
    	private TaskProgressDialog dialog;
    	public Completer(TaskProgressDialog dialog) {
    		this.dialog = dialog;
    	}
    	public void run() {
    		try {
    			dialog.setVisible(false);
    		} catch (Exception e) { 
    			log.error("run(): error", e);
    		}
    	}    	
    }
     
}
