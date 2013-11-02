package com.mixshare.rapid_evolution.ui.dialogs.taskstatus;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.audio.xuggle.XuggleUtil;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.util.QWidgetUtil;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QMoveEvent;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QTableView;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class TaskStatusDialog extends QDialog {

	static private Logger log = Logger.getLogger(TaskStatusDialog.class);

	static public TaskStatusDialog instance = null;
	static private RefreshThread refreshInstance = null;
	static private int REFRESH_INTERVAL_MILLIS = 2000;

	private QLabel infoLabel;
	private QTableView foregroundView;
	private QTableView backgroundView;
	private QLabel libInfo;
	private QStandardItemModel foregroundModel;
	private QStandardItemModel backgroundModel;

    public TaskStatusDialog() {
        super();
        init();
    }

    public TaskStatusDialog(QWidget parent) {
        super(parent);
        init();
    }

    private void init() {
    	try {
    		instance = this;

	        setWindowTitle(Translations.get("task_status_dialog_title"));
	        setWindowIcon(new QIcon(RE3Properties.getProperty("application_icon_filename")));

	    	QSizePolicy optionsWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
	    	optionsWidgetSizePolicy.setVerticalStretch((byte)1);
	    	setSizePolicy(optionsWidgetSizePolicy);

	    	QVBoxLayout optionsLayout = new QVBoxLayout(this);
	    	optionsLayout.setMargin(10);

	    	infoLabel = new QLabel();
	    	StringBuffer infoLabelBuffer = new StringBuffer();
	    	infoLabelBuffer.append("RE3 Version: " + RapidEvolution3.RAPID_EVOLUTION_VERSION);
	    	infoLabelBuffer.append("\nDetected OS: " + OSHelper.getPlatformAsString());
	    	infoLabelBuffer.append("\nJVM Version: " + System.getProperty("java.version"));
	        try {
	            String QTVersion = QTUtil.getVersionString();
	            if (QTVersion != null)
	            	infoLabelBuffer.append("\n" + QTVersion);
	        } catch (java.lang.Error e) {
	        } catch (Exception e) { }
	        infoLabelBuffer.append("\nXuggle Version: " + XuggleUtil.getVersion());
	        infoLabelBuffer.append("\nMax Memory: " + String.valueOf(Runtime.getRuntime().maxMemory() / 1048576) + "mb");
	        //infoLabelBuffer.append("\nUsed Memory: " + String.valueOf((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "mb");
	        infoLabel.setText(infoLabelBuffer.toString());

	        libInfo = new QLabel();
	        libInfo.setAlignment(Qt.AlignmentFlag.AlignRight);
	        setLibInfoText();

	        QWidget textWidget = new QWidget(this);
	    	QHBoxLayout textSection = new QHBoxLayout(textWidget);
	    	textSection.setMargin(10);
	    	textSection.addWidget(infoLabel);
	    	textSection.addWidget(libInfo);

	    	foregroundView = new QTableView();
	    	foregroundView.setItemDelegate(new TaskStatusDelegate(this, true));
	    	foregroundView.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers);
	    	foregroundView.setDragEnabled(false);
	    	backgroundView = new QTableView();
	    	backgroundView.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers);
	    	backgroundView.setDragEnabled(false);
	    	backgroundView.setItemDelegate(new TaskStatusDelegate(this, false));

	    	refreshTaskStatus();

	        // set initial column sizes
	    	foregroundView.setColumnWidth(0, 500);
	    	backgroundView.setColumnWidth(0, 500);

	    	// init event listeners (selection changed, columns moved, etc)
	        setupEventListeners();

	        optionsLayout.addWidget(textWidget);

	        QPushButton re3LogButton = new QPushButton();
	        re3LogButton.setText("Open RE3 Log File");
	        re3LogButton.clicked.connect(this, "openRE3Log()");
	        optionsLayout.addWidget(re3LogButton);

	        optionsLayout.addWidget(foregroundView);
	        optionsLayout.addWidget(backgroundView);

	        QDialogButtonBox buttonBox = new QDialogButtonBox();
	        buttonBox.setObjectName("buttonBox");
	        buttonBox.setGeometry(new QRect(20, 480, 401, 41));
	        buttonBox.setFocusPolicy(com.trolltech.qt.core.Qt.FocusPolicy.TabFocus);
	        buttonBox.setStandardButtons(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.createQFlags(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Ok));
	        buttonBox.setCenterButtons(true);
	        buttonBox.clicked.connect(this, "closeOptions()");
	        optionsLayout.addWidget(buttonBox);

	        QWidgetUtil.setWidgetSize(this, "taskstatus_dialog", 700, 500);
	        QWidgetUtil.setWidgetPosition(this, "taskstatus_dialog");

    	} catch (Exception e) {
    		log.error("init(): error", e);
    	}
    }

	public void setupEventListeners() {

	}

    protected void closeOptions() {
    	setVisible(false);
    }

    protected void openRE3Log() {
    	new RE3LogFileDialog(this).show();
    }

    private void refreshTaskStatus() {
    	if (log.isTraceEnabled())
    		log.trace("refreshTaskStatus(): refreshing...");

    	int numColumns = 3;
    	foregroundModel = new QStandardItemModel(0, numColumns, this);
    	foregroundModel.setHeaderData(0, Qt.Orientation.Horizontal, Translations.get("taskstatus_foreground_name") + " (" + SandmanThread.getNumSleepingForegroundTasks() + " sleeping tasks)");
    	foregroundModel.setHeaderData(1, Qt.Orientation.Horizontal, "");
    	foregroundModel.setHeaderData(2, Qt.Orientation.Horizontal, "");
    	for (String status : TaskManager.getCurrentForegroundStatuses()) {
    		QStandardItem statusColumn = new QStandardItem();
    		statusColumn.setText(status);
        	ArrayList<QStandardItem> newRow = new ArrayList<QStandardItem>(numColumns);
        	newRow.add(statusColumn);
        	foregroundModel.appendRow(newRow);
    	}
    	foregroundView.setModel(foregroundModel);


    	backgroundModel = new QStandardItemModel(0, numColumns, this);
    	backgroundModel.setHeaderData(0, Qt.Orientation.Horizontal, Translations.get("taskstatus_background_name") + " (" + SandmanThread.getNumSleepingBackgroundTasks() + " sleeping tasks)");
    	backgroundModel.setHeaderData(1, Qt.Orientation.Horizontal, "");
    	backgroundModel.setHeaderData(2, Qt.Orientation.Horizontal, "");
    	for (String status : TaskManager.getCurrentBackgroundStatuses()) {
    		QStandardItem statusColumn = new QStandardItem();
    		statusColumn.setText(status);
        	ArrayList<QStandardItem> newRow = new ArrayList<QStandardItem>(numColumns);
        	newRow.add(statusColumn);
        	backgroundModel.appendRow(newRow);
    	}
    	backgroundView.setModel(backgroundModel);

    	setupPersistentEditors();

    	setLibInfoText();
    }


    @Override
	protected void resizeEvent(QResizeEvent re) {
    	super.resizeEvent(re);
    	UIProperties.setProperty("taskstatus_dialog_width", String.valueOf(re.size().width()));
    	UIProperties.setProperty("taskstatus_dialog_height", String.valueOf(re.size().height()));
    }

    @Override
	protected void moveEvent(QMoveEvent me) {
    	super.moveEvent(me);
    	UIProperties.setProperty("taskstatus_dialog_x", String.valueOf(me.pos().x()));
    	UIProperties.setProperty("taskstatus_dialog_y", String.valueOf(me.pos().y()));
    }

    private void setLibInfoText() {
    	StringBuffer libInfoBuffer = new StringBuffer();
    	libInfoBuffer.append("# Artists: " + Database.getArtistIndex().getSize() + "  (" + Database.getArtistIndex().getSizeInternalItems() + " / " + Database.getArtistIndex().getSizeExternalItems() + ")");
    	libInfoBuffer.append("\n# Labels: " + Database.getLabelIndex().getSize() + "  (" + Database.getLabelIndex().getSizeInternalItems() + " / " + Database.getLabelIndex().getSizeExternalItems() + ")");
    	libInfoBuffer.append("\n# Releases: " + Database.getReleaseIndex().getSize() + "  (" + Database.getReleaseIndex().getSizeInternalItems() + " / " + Database.getReleaseIndex().getSizeExternalItems() + ")");
    	libInfoBuffer.append("\n# Songs: " + Database.getSongIndex().getSize() + "  (" + Database.getSongIndex().getSizeInternalItems() + " / " + Database.getSongIndex().getSizeExternalItems() + ")");
    	libInfoBuffer.append("\n# Mixouts: " + Database.getMixoutIndex().getSize());
    	libInfoBuffer.append("\n# Styles: " + Database.getStyleIndex().getSize());
    	libInfoBuffer.append("\n# Tags: " + Database.getTagIndex().getSize());
    	libInfoBuffer.append("\n# Playlists: " + Database.getPlaylistIndex().getSize());
    	libInfo.setText(libInfoBuffer.toString());
    }

	private void setupPersistentEditors() {
		for (int i = 0; i < foregroundModel.rowCount(); ++i) {
			foregroundView.openPersistentEditor(foregroundModel.index(i, 1));
			foregroundView.openPersistentEditor(foregroundModel.index(i, 2));
		}
		for (int i = 0; i < backgroundModel.rowCount(); ++i) {
			backgroundView.openPersistentEditor(backgroundModel.index(i, 1));
			backgroundView.openPersistentEditor(backgroundModel.index(i, 2));
		}
	}

    public void startRefreshTask() {
    	if (log.isDebugEnabled())
    		log.debug("startRefreshTask(): starting refresh task");
    	refreshTaskStatus();
    	QApplication.invokeLater(REFRESH_INTERVAL_MILLIS, new RefreshThread());
    }

    static private class RefreshThread extends Thread {
    	public RefreshThread() {
    		refreshInstance = this;
    	}
    	@Override
		public void run() {
			try {
				if (TaskStatusDialog.instance.isVisible()) {
   					TaskStatusDialog.instance.refreshTaskStatus();
   					QApplication.invokeLater(REFRESH_INTERVAL_MILLIS, this);
				} else {
					refreshInstance = null;
					if (log.isDebugEnabled())
						log.debug("run(): stopping");
				}
			} catch (Exception e) {
				log.error("run(): error", e);
			}
    	}
    }

}
