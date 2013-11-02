package com.mixshare.rapid_evolution.ui.dialogs.taskstatus;

import java.io.File;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.util.QWidgetUtil;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.io.TextFileReader;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMoveEvent;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QTextEdit;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class RE3LogFileDialog extends QDialog {

	static private Logger log = Logger.getLogger(RE3LogFileDialog.class);

	private QTextEdit textEdit;
	
    public RE3LogFileDialog(QWidget parent) {
        super(parent);
        init();
    }

    private void init() {
    	try {    	   		
	        setWindowTitle("RE3 Log File");
	        setWindowIcon(new QIcon(RE3Properties.getProperty("application_icon_filename")));

	    	QSizePolicy optionsWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
	    	optionsWidgetSizePolicy.setVerticalStretch((byte)1);
	    	setSizePolicy(optionsWidgetSizePolicy);    	    	    	
	    	
	    	QVBoxLayout optionsLayout = new QVBoxLayout(this);    	
	    	optionsLayout.setMargin(10);    	    	
	    	
	    	File re3LogFile = new File(OSHelper.getWorkingDirectory() + "/re3.log");
	    	if (!re3LogFile.exists())
	    		re3LogFile = new File("re3.log");
	    	TextFileReader fileReader = new TextFileReader(re3LogFile.getAbsolutePath());
	    	textEdit = new QTextEdit();
	    	textEdit.setReadOnly(true);
	    	textEdit.setText(fileReader.getText());
	    	
	        optionsLayout.addWidget(textEdit);

	        QDialogButtonBox buttonBox = new QDialogButtonBox();
	        buttonBox.setObjectName("buttonBox");
	        buttonBox.setGeometry(new QRect(20, 480, 401, 41));
	        buttonBox.setFocusPolicy(com.trolltech.qt.core.Qt.FocusPolicy.TabFocus);
	        buttonBox.setStandardButtons(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.createQFlags(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Ok));
	        buttonBox.setCenterButtons(true);
	        buttonBox.clicked.connect(this, "closeOptions()");	        
	        optionsLayout.addWidget(buttonBox);
	        
	        QWidgetUtil.setWidgetSize(this, "re3logfile_dialog", 700, 400);
	        QWidgetUtil.setWidgetPosition(this, "re3logfile_dialog");
	        
    	} catch (Exception e) {
    		log.error("init(): error", e);
    	}
    }
	
    protected void closeOptions() {
    	setVisible(false);
    }
    	
    protected void resizeEvent(QResizeEvent re) {
    	super.resizeEvent(re);
    	UIProperties.setProperty("re3logfile_dialog_width", String.valueOf(re.size().width()));
    	UIProperties.setProperty("re3logfile_dialog_height", String.valueOf(re.size().height()));
    }
    
    protected void moveEvent(QMoveEvent me) {
    	super.moveEvent(me);
    	UIProperties.setProperty("re3logfile_dialog_x", String.valueOf(me.pos().x()));
    	UIProperties.setProperty("re3logfile_dialog_y", String.valueOf(me.pos().y()));
    }
        
}
