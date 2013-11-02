package com.mixshare.rapid_evolution.ui.dialogs.options;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.audio.detection.bpm.BpmDetector;
import com.mixshare.rapid_evolution.audio.tags.TagManager;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.util.QWidgetUtil;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.song.SongProfileDelegate;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.webaccess.WebServerManager;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.ui.SongDescriptionUpdateTask;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.CaseSensitivity;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMoveEvent;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QStandardItem;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QTreeView;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class OptionsDialog extends QDialog {

	static private Logger log = Logger.getLogger(OptionsDialog.class);

	private OptionsModelManager modelManager;
    private QTreeView treeView;
    private QLineEdit filterText;
    private QComboBox optionsTypeCombo;

    private QAction expandAll;
    private QAction collapseAll;

    transient private String currentSongDescriptionHash;

    public OptionsDialog() {
        super();
        init();
    }

    public OptionsDialog(QWidget parent) {
        super(parent);
        init();
    }

    private void init() {
    	try {
	        setWindowTitle(Translations.get("settings_dialog_title"));

	    	QSizePolicy optionsWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
	    	optionsWidgetSizePolicy.setVerticalStretch((byte)1);
	    	setSizePolicy(optionsWidgetSizePolicy);

	    	QVBoxLayout optionsLayout = new QVBoxLayout(this);
	    	optionsLayout.setMargin(10);

	    	QWidget filterInputWidget = new QWidget();
	        QHBoxLayout filterInputLayout = new QHBoxLayout(filterInputWidget);
	        filterInputLayout.setSpacing(4);
	        filterInputLayout.setMargin(0);

	    	QIcon searchIcon = new QIcon(RE3Properties.getProperty("search_icon"));
	    	QPushButton searchButton = new QPushButton(this);
	    	searchButton.setMaximumWidth(25);
	        searchButton.setEnabled(false);
	        searchButton.setFlat(true);
	        searchButton.setIcon(searchIcon);

	        filterText = new QLineEdit();

	        createModels();

	        /*
	        optionsTypeCombo = new QComboBox();
	        int count = 1;
	        String categoryKey = RE3Properties.getProperty("settings_category_" + count);
	        while (categoryKey != null) {
	        	String descriptionKey = "options_" + categoryKey + "_description";
	        	optionsTypeCombo.addItem(Translations.get(descriptionKey), count);
	        	++count;
	        	categoryKey = RE3Properties.getProperty("settings_category_" + count);
	        }
	        optionsTypeCombo.setCurrentIndex(0);
	        */

	        filterInputLayout.addWidget(searchButton);
	        filterInputLayout.addWidget(filterText);
	        //filterInputLayout.addWidget(optionsTypeCombo);

	        optionsLayout.addWidget(filterInputWidget);
	        optionsLayout.addWidget(treeView);

	        QDialogButtonBox buttonBox = new QDialogButtonBox();
	        buttonBox.setObjectName("buttonBox");
	        buttonBox.setGeometry(new QRect(20, 480, 401, 41));
	        buttonBox.setFocusPolicy(com.trolltech.qt.core.Qt.FocusPolicy.TabFocus);
	        buttonBox.setStandardButtons(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.createQFlags(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Ok));
	        buttonBox.setCenterButtons(true);
	        buttonBox.clicked.connect(this, "closeOptions()");
	        optionsLayout.addWidget(buttonBox);

	        filterText.textChanged.connect(this, "updateFilter()");

	    	// setup context menu
	        treeView.setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);

			expandAll = new QAction(Translations.get("expand_all_text"), this);
			expandAll.triggered.connect(this, "expandAll()");
			expandAll.setIcon(new QIcon(RE3Properties.getProperty("menu_expand_all_icon")));

			collapseAll = new QAction(Translations.get("collapse_all_text"), this);
			collapseAll.triggered.connect(this, "collapseAll()");
	        collapseAll.setIcon(new QIcon(RE3Properties.getProperty("menu_collapse_all_icon")));

			treeView.addAction(expandAll);
			treeView.addAction(collapseAll);

	        filterText.setFocus();

	        QWidgetUtil.setWidgetSize(this, "options_dialog", 700, 400);
	        QWidgetUtil.setWidgetPosition(this, "options_dialog");

			currentSongDescriptionHash = getSongDescriptionFormatHash();

    	} catch (Exception e) {
    		log.error("init(): error", e);
    	}
    }

    @Override
	protected void closeEvent(QCloseEvent closeEvent) {
    	try {
			ArtistProfile.loadProperties();
			LabelProfile.loadProperties();
			ReleaseProfile.loadProperties();
			SongProfile.loadProperties();
			boolean useDebug = RE3Properties.getBoolean("user_enable_debug_mode");
			if (useDebug) {
				Logger.getRootLogger().setLevel(Level.DEBUG);
			} else {
				Logger.getRootLogger().setLevel(Level.INFO);
			}
			if (currentSongDescriptionHash != null) {
				String songDescriptionHashAfter = getSongDescriptionFormatHash();
				if (!songDescriptionHashAfter.equals(currentSongDescriptionHash)) {
					TaskManager.runForegroundTask(new SongDescriptionUpdateTask());
					currentSongDescriptionHash = songDescriptionHashAfter;
				}
			}
			if (RE3Properties.getBoolean("web_access_enabled"))
				WebServerManager.startWebAccess();
			else
				WebServerManager.stopWebAccess();
			TagManager.initTagManagerOptions();
			try { BpmDetector.BPM_DETECTOR_QUALITY = RE3Properties.getInt("bpm_detector_quality"); } catch (NumberFormatException nfe) { }
			SongProfileDelegate.MAX_DESCRIPTION_FIELD_LENGTH = RE3Properties.getInt("profile_title_max_field_display_length");
			OSHelper.initMusicDirectory();
    	} catch (Exception e) {
    		log.error("closeEvent(): error", e);
    	}
		super.closeEvent(closeEvent);
    }

	private String getSongDescriptionFormatHash() {
		StringBuffer result = new StringBuffer();
		result.append(RE3Properties.getBoolean("song_display_format_show_artist"));
		result.append(RE3Properties.getBoolean("song_display_format_show_release"));
		result.append(RE3Properties.getBoolean("song_display_format_show_track"));
		result.append(RE3Properties.getBoolean("song_display_format_show_title"));
		result.append(RE3Properties.getBoolean("song_display_format_show_remix"));
		result.append(RE3Properties.getBoolean("song_display_format_show_key"));
		result.append(RE3Properties.getBoolean("song_display_format_show_bpm"));
		result.append(RE3Properties.getBoolean("song_display_format_show_duration"));
		return result.toString();
	}

    protected void closeOptions() {
    	this.close();
    	//setVisible(false);
    }

	public void setupEventListeners() {
        treeView.header().sectionResized.connect(modelManager, "columnResized(Integer,Integer,Integer)");
        treeView.header().sectionMoved.connect(modelManager, "columnMoved(Integer,Integer,Integer)");
	}

    private void createModels() {
    	modelManager = (OptionsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(OptionsModelManager.class);
    	modelManager.initialize(this);
    	// setup the proxy model
    	OptionsProxyModel proxyModel = new OptionsProxyModel(modelManager);
    	proxyModel.setDynamicSortFilter(true);
    	proxyModel.setSourceModel(modelManager.getSourceModel());
    	proxyModel.setFilterCaseSensitivity(CaseSensitivity.CaseInsensitive);
    	modelManager.setProxyModel(proxyModel);
        // set up tree view
    	treeView = new QTreeView();
    	treeView.setModel(proxyModel);
    	treeView.header().setStretchLastSection(false);
    	treeView.setEditTriggers(QAbstractItemView.EditTrigger.AllEditTriggers);
    	treeView.setItemDelegate(new OptionsItemDelegate(treeView, ((QStandardItemModel)modelManager.getSourceModel()), proxyModel, modelManager));
    	treeView.setDragEnabled(false);
    	treeView.setSelectionMode(SelectionMode.NoSelection);
    	treeView.header().setDragEnabled(false);
		treeView.setUniformRowHeights(true);
        // set initial column sizes
    	modelManager.setSourceColumnSizes(treeView);
        // init event listeners (selection changed, columns moved, etc)
        setupEventListeners();
        setupPersistentEditors();
    }


    @Override
	protected void resizeEvent(QResizeEvent re) {
    	super.resizeEvent(re);
    	UIProperties.setProperty("options_dialog_width", String.valueOf(re.size().width()));
    	UIProperties.setProperty("options_dialog_height", String.valueOf(re.size().height()));
    }

    @Override
	protected void moveEvent(QMoveEvent me) {
    	super.moveEvent(me);
    	UIProperties.setProperty("options_dialog_x", String.valueOf(me.pos().x()));
    	UIProperties.setProperty("options_dialog_y", String.valueOf(me.pos().y()));
    }

    private void updateFilter() {
    	modelManager.getOptionsProxyModel().setSearchText(filterText.text());
    	modelManager.getOptionsProxyModel().invalidate();
    	setupPersistentEditors();
    }

    private void expandAll() {
    	treeView.expandAll();
    }

    private void collapseAll() {
    	treeView.collapseAll();
    }

	private void setupPersistentEditors() {
		OptionsProxyModel proxyModel = modelManager.getOptionsProxyModel();
		for (int i = 0; i < proxyModel.rowCount(); ++i) {
			QModelIndex sourceIndex = proxyModel.mapToSource(proxyModel.index(i, 0));
			String id = ((QStandardItemModel)modelManager.getSourceModel()).index(sourceIndex.row(), modelManager.getIdColumn()).data().toString();
			String type = ((QStandardItemModel)modelManager.getSourceModel()).index(sourceIndex.row(), modelManager.getTypeColumn()).data().toString();
			if (type.equalsIgnoreCase("boolean")) {
				QModelIndex valueIndex = ((QStandardItemModel)modelManager.getSourceModel()).index(sourceIndex.row(), modelManager.getValueColumn());
				QModelIndex proxyIndex = proxyModel.mapFromSource(valueIndex);
				treeView.openPersistentEditor(proxyIndex);
			} else if (type.equalsIgnoreCase("directory") || type.equalsIgnoreCase("file")) {
				QModelIndex actionIndex = ((QStandardItemModel)modelManager.getSourceModel()).index(sourceIndex.row(), modelManager.getActionColumn());
				QModelIndex proxyIndex = proxyModel.mapFromSource(actionIndex);
				treeView.openPersistentEditor(proxyIndex);
			}
			QModelIndex nameIndex = ((QStandardItemModel)modelManager.getSourceModel()).index(sourceIndex.row(), modelManager.getNameColumn());
			QStandardItem item = ((QStandardItemModel)modelManager.getSourceModel()).itemFromIndex(nameIndex);
			setupPersistentEditors(item);
		}
	}

	private void setupPersistentEditors(QStandardItem item) {
		OptionsProxyModel proxyModel = modelManager.getOptionsProxyModel();
		for (int i = 0; i < item.rowCount(); ++i) {
			QModelIndex sourceIndex = ((QStandardItemModel)modelManager.getSourceModel()).index(i, 0, item.index());
			String id = ((QStandardItemModel)modelManager.getSourceModel()).index(sourceIndex.row(), modelManager.getIdColumn(), sourceIndex.parent()).data().toString();
			String type = ((QStandardItemModel)modelManager.getSourceModel()).index(sourceIndex.row(), modelManager.getTypeColumn(), sourceIndex.parent()).data().toString();
			if (type.equalsIgnoreCase("boolean")) {
				QModelIndex valueIndex = ((QStandardItemModel)modelManager.getSourceModel()).index(sourceIndex.row(), modelManager.getValueColumn(), sourceIndex.parent());
				QModelIndex proxyIndex = proxyModel.mapFromSource(valueIndex);
				if (proxyIndex != null)
					treeView.openPersistentEditor(proxyIndex);
			} else if ((type.equalsIgnoreCase("directory") || type.equalsIgnoreCase("file")) || (type.equals("choice") && id.equals("organize_music_rename_pattern"))) {
				QModelIndex actionIndex = ((QStandardItemModel)modelManager.getSourceModel()).index(sourceIndex.row(), modelManager.getActionColumn(), sourceIndex.parent());
				QModelIndex proxyIndex = proxyModel.mapFromSource(actionIndex);
				if (proxyIndex != null)
					treeView.openPersistentEditor(proxyIndex);
			}
			if (id.equals("automatically_scan_root_directory")
					|| id.equals("max_external_artists")
					|| id.equals("max_external_labels")
					|| id.equals("max_external_releases")
					|| id.equals("max_external_songs")
					|| id.equals("thumbnail_image_size")
					|| (id.startsWith("enable_") && id.endsWith("_miners"))
					|| id.equals("enable_tag_read_optimizations")) {
				QModelIndex actionIndex = ((QStandardItemModel)modelManager.getSourceModel()).index(sourceIndex.row(), modelManager.getActionColumn(), sourceIndex.parent());
				QModelIndex proxyIndex = proxyModel.mapFromSource(actionIndex);
				if (proxyIndex != null)
					treeView.openPersistentEditor(proxyIndex);
			}
			QModelIndex nameIndex = ((QStandardItemModel)modelManager.getSourceModel()).index(sourceIndex.row(), modelManager.getNameColumn(), sourceIndex.parent());
			QStandardItem childItem = ((QStandardItemModel)modelManager.getSourceModel()).itemFromIndex(nameIndex);
			setupPersistentEditors(childItem);
		}
	}

}
