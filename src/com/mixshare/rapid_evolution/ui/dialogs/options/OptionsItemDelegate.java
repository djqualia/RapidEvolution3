package com.mixshare.rapid_evolution.ui.dialogs.options;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.ui.dialogs.fields.TextFieldInputDialog;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.importers.filesystem.ImportFilesTask;
import com.mixshare.rapid_evolution.workflow.maintenance.ExternalItemRemovalTask;
import com.mixshare.rapid_evolution.workflow.maintenance.images.OrphanedImagesDeleterTask;
import com.mixshare.rapid_evolution.workflow.maintenance.search.SongTagReadTimesClearer;
import com.mixshare.rapid_evolution.workflow.user.mining.DataMiningDataDeleter;
import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.FocusPolicy;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QItemDelegate;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QStandardItemModel;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QWidget;

public class OptionsItemDelegate extends QItemDelegate implements AllColumns, DataConstants {

	static private Logger log = Logger.getLogger(OptionsItemDelegate.class);

	private final OptionsModelManager modelManager;
    private final QStandardItemModel model;
    private final OptionsProxyModel proxyModel;

    public OptionsItemDelegate(QWidget parent, QStandardItemModel model, OptionsProxyModel proxyModel, OptionsModelManager modelManager) {
        super(parent);
        this.model = model;
        this.proxyModel = proxyModel;
        this.modelManager = modelManager;
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
        QModelIndex sourceIndex = proxyModel.mapToSource(index);
        if (sourceIndex != null) {
	        QModelIndex idIndex = model.index(sourceIndex.row(), modelManager.getIdColumn(), sourceIndex.parent());
	        QModelIndex typeIndex = model.index(sourceIndex.row(), modelManager.getTypeColumn(), sourceIndex.parent());
	        QModelIndex valueIndex = model.index(sourceIndex.row(), modelManager.getValueColumn(), sourceIndex.parent());
	        Column column = modelManager.getSourceColumnType(sourceIndex.column());
	        String id = idIndex.data().toString();
	        String type = typeIndex.data().toString();
	    	if (column.getColumnId() == COLUMN_SETTING_VALUE.getColumnId()) {
		        if (type.equalsIgnoreCase("boolean")) {
		        	QCheckBox checkBox = new QCheckBox(parent);
		        	checkBox.setChecked(RE3Properties.getBoolean(id));
		        	checkBox.setAutoFillBackground(true);
		        	checkBox.setBackgroundRole(QPalette.ColorRole.NoRole);
		        	return checkBox;
		        } else if (type.equalsIgnoreCase("integer")) {
		        	QSpinBox spinBox = new QSpinBox(parent);
		        	spinBox.setMinimum(RE3Properties.getInt(id + "_min"));
		        	spinBox.setMaximum(RE3Properties.getInt(id + "_max"));
		        	spinBox.setSingleStep(RE3Properties.getInt(id + "_increment"));
		        	spinBox.setValue(RE3Properties.getInt(id));
		        	spinBox.setAutoFillBackground(true);
		        	return spinBox;
		        } else if (type.equalsIgnoreCase("choice")) {
		        	QComboBox comboBox = new QComboBox(parent);
		        	int currentIndex = 0;
		        	int i = 0;
		        	if (id.equals("organize_music_rename_pattern")) {
		        		int num = 1;
		        		String key = "organize_music_rename_pattern_choice_" + num;
		        		String pattern = RE3Properties.getProperty(key);
		        		while (pattern != null) {
		        			comboBox.addItem(pattern);
			        		if (pattern.equals(RE3Properties.getProperty(id)))
			        			currentIndex = i;
		        			++i;
		        			++num;
			        		key = "organize_music_rename_pattern_choice_" + num;
			        		pattern = RE3Properties.getProperty(key);
		        		}
		        	} else {
			        	StringTokenizer tokenizer = new StringTokenizer(RE3Properties.getProperty(id + "_choices"), ",");
			        	while (tokenizer.hasMoreTokens()) {
			        		String tokenValue = tokenizer.nextToken();
			        		String tokenName = Translations.get(id + "_" + tokenValue);
			        		comboBox.addItem(tokenName, tokenValue);
			        		if (tokenValue.equals(RE3Properties.getProperty(id)))
			        			currentIndex = i;
			        		++i;
			        	}
		        	}
		        	comboBox.setCurrentIndex(currentIndex);
		        	comboBox.setAutoFillBackground(true);
		        	return comboBox;
		        } else if (type.equalsIgnoreCase("decimal")) {
		        	QLineEdit textField = new QLineEdit(parent);
		        	textField.setText(RE3Properties.getProperty(id));
		        	textField.setAutoFillBackground(true);
		        	return textField;
		        } else if (type.equalsIgnoreCase("text")) {
		        	QLineEdit textField = new QLineEdit(parent);
		        	textField.setText(RE3Properties.getProperty(id));
		        	textField.setAutoFillBackground(true);
		        	return textField;
		        } else if (type.equalsIgnoreCase("directory")) {
		        	QLineEdit textField = new QLineEdit(parent);
		        	textField.setText(RE3Properties.getProperty(id));
		        	textField.setAutoFillBackground(true);
		        	return textField;
		        } else if (type.equalsIgnoreCase("file")) {
		        	QLineEdit textField = new QLineEdit(parent);
		        	textField.setText(RE3Properties.getProperty(id));
		        	textField.setAutoFillBackground(true);
		        	return textField;
		        }
	    	} else if (column.getColumnId() == COLUMN_SETTING_ACTION.getColumnId()) {
	    		if (type.equalsIgnoreCase("directory")) {
	        		QPushButton button = new QPushButton(parent);
	        		button.setText(Translations.get("browse_text"));
	        		button.clicked.connect(new FileSettingBrowser(parent, id, model, valueIndex, true), "browseFilename(Boolean)");
	        		button.setFocusPolicy(FocusPolicy.NoFocus);
	        		return button;
	    		} else if (type.equalsIgnoreCase("file")) {
	        		QPushButton button = new QPushButton(parent);
	        		button.setText(Translations.get("browse_text"));
	        		button.clicked.connect(new FileSettingBrowser(parent, id, model, valueIndex, false), "browseFilename(Boolean)");
	        		button.setFocusPolicy(FocusPolicy.NoFocus);
	        		return button;
	    		} else if (type.equals("choice") && id.equals("organize_music_rename_pattern")) {
	        		QPushButton button = new QPushButton(parent);
	        		button.setText(Translations.get("add_text"));
	        		button.clicked.connect(this, "addRenamePattern()");
	        		button.setFocusPolicy(FocusPolicy.NoFocus);
	        		return button;
	    		} else if (id.equals("automatically_scan_root_directory")) {
	        		QPushButton button = new QPushButton(parent);
	        		button.setText(Translations.get("scan_now_text"));
	        		button.clicked.connect(this, "scanNow()");
	        		button.setFocusPolicy(FocusPolicy.NoFocus);
	        		return button;
	    		} else if (id.equals("enable_tag_read_optimizations")) {
	        		QPushButton button = new QPushButton(parent);
	        		button.setText(Translations.get("tag_optimizations_reset_text"));
	        		button.clicked.connect(this, "resetTagReadTimes()");
	        		button.setFocusPolicy(FocusPolicy.NoFocus);
	        		return button;
	    		} else if (id.equals("max_external_artists")) {
	        		QPushButton button = new QPushButton(parent);
	        		button.setText(Translations.get("clear_external_text"));
	        		button.clicked.connect(this, "clearArtists()");
	        		button.setFocusPolicy(FocusPolicy.NoFocus);
	        		return button;
	    		} else if (id.equals("max_external_labels")) {
	        		QPushButton button = new QPushButton(parent);
	        		button.setText(Translations.get("clear_external_text"));
	        		button.clicked.connect(this, "clearLabels()");
	        		button.setFocusPolicy(FocusPolicy.NoFocus);
	        		return button;
	    		} else if (id.equals("max_external_releases")) {
	        		QPushButton button = new QPushButton(parent);
	        		button.setText(Translations.get("clear_external_text"));
	        		button.clicked.connect(this, "clearReleases()");
	        		button.setFocusPolicy(FocusPolicy.NoFocus);
	        		return button;
	    		} else if (id.equals("max_external_songs")) {
	        		QPushButton button = new QPushButton(parent);
	        		button.setText(Translations.get("clear_external_text"));
	        		button.clicked.connect(this, "clearSongs()");
	        		button.setFocusPolicy(FocusPolicy.NoFocus);
	        		return button;
	    		} else if (id.startsWith("enable_") && id.endsWith("_miners")) {
	        		QPushButton button = new QPushButton(parent);
	        		button.setText(Translations.get("delete_mining_data_source"));
	        		button.clicked.connect(new DataMinerClearer(id), "clearData()");
	        		button.setFocusPolicy(FocusPolicy.NoFocus);
	        		return button;
	    		} else if (id.equals("thumbnail_image_size")) {
	        		QPushButton button = new QPushButton(parent);
	        		button.setText(Translations.get("clean_orphaned_images_text"));
	        		button.clicked.connect(this, "cleanOrphanedImages()");
	        		button.setFocusPolicy(FocusPolicy.NoFocus);
	        		return button;
	    		}
	    	}
        }
        return null;
    }

    @Override
    public void setEditorData(QWidget editor, QModelIndex index) {
        super.setEditorData(editor, index);
    }

    @Override
    public void setModelData(QWidget editor, QAbstractItemModel proxyModel, QModelIndex proxyIndex) {
        QModelIndex sourceIndex = this.proxyModel.mapToSource(proxyIndex);
        Column column = modelManager.getSourceColumnType(sourceIndex.column());
    	if (column.getColumnId() != COLUMN_SETTING_VALUE.getColumnId())
    		return;
        QModelIndex idIndex = model.index(sourceIndex.row(), modelManager.getIdColumn(), sourceIndex.parent());
        QModelIndex typeIndex = model.index(sourceIndex.row(), modelManager.getTypeColumn(), sourceIndex.parent());
        String id = idIndex.data().toString();
        String type = typeIndex.data().toString();
        if (type.equalsIgnoreCase("boolean")) {
        	if (((QCheckBox)editor).isChecked())
        		RE3Properties.setProperty(id, "true");
        	else
        		RE3Properties.setProperty(id, "false");
        } else if (type.equalsIgnoreCase("integer")) {
        	RE3Properties.setProperty(id, String.valueOf(((QSpinBox)editor).value()));
        } else if (type.equalsIgnoreCase("choice")) {
        	String value = null;
        	if (((QComboBox)editor).itemData(((QComboBox)editor).currentIndex()) != null)
        		value = ((QComboBox)editor).itemData(((QComboBox)editor).currentIndex()).toString();
        	else
        		value = ((QComboBox)editor).itemText(((QComboBox)editor).currentIndex());
        	RE3Properties.setProperty(id, value);
            RE3Properties.fireChangedEvent();
            model.setData(sourceIndex, Translations.get(id + "_" + RE3Properties.getProperty(id)));
            return;
        } else if (type.equalsIgnoreCase("decimal")) {
        	try {
        		String text = ((QLineEdit)editor).text();
        		Float.parseFloat(text);
        		RE3Properties.setProperty(id, text);
        	} catch (Exception e) { }
        } else if (type.equalsIgnoreCase("text")) {
    		RE3Properties.setProperty(id, ((QLineEdit)editor).text());
        } else if (type.equalsIgnoreCase("directory")) {
    		RE3Properties.setProperty(id, ((QLineEdit)editor).text());
        } else if (type.equalsIgnoreCase("file")) {
    		RE3Properties.setProperty(id, ((QLineEdit)editor).text());
        }
        RE3Properties.fireChangedEvent();
        model.setData(sourceIndex, RE3Properties.getProperty(id.toString()));
    }

    private void addRenamePattern() {
    	TextFieldInputDialog textFieldInputDialog = new TextFieldInputDialog(Translations.get("add_rename_pattern_title"), Translations.get("add_rename_pattern_text"), "", null);
    	if (textFieldInputDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		String newPattern = textFieldInputDialog.getFilterName();
    		int i = 0;
    		int num = 1;
    		String key = "organize_music_rename_pattern_choice_" + num;
    		String pattern = RE3Properties.getProperty(key);
    		while (pattern != null) {
    			++i;
    			++num;
        		key = "organize_music_rename_pattern_choice_" + num;
        		pattern = RE3Properties.getProperty(key);
    		}
    		RE3Properties.setProperty(key, newPattern);
    	}
    }

    static private class FileSettingBrowser {
    	private final QWidget parent;
    	private final String settingId;
    	private final QStandardItemModel model;
    	private final QModelIndex sourceIndex;
    	private final boolean directoryMode;
    	public FileSettingBrowser(QWidget parent, String settingId, QStandardItemModel model, QModelIndex sourceIndex, boolean directoryMode) {
    		this.settingId = settingId;
    		this.parent = parent;
    		this.model = model;
    		this.sourceIndex = sourceIndex;
    		this.directoryMode = directoryMode;
    	}
	    protected void browseFilename(Boolean checked) {
	    	try {
	    		QFileDialog fileDialog = new QFileDialog(parent);
	    		if (directoryMode)
	    			fileDialog.setFileMode(QFileDialog.FileMode.DirectoryOnly);
	    		else
	    			fileDialog.setFileMode(QFileDialog.FileMode.ExistingFile);
	    		fileDialog.setViewMode(QFileDialog.ViewMode.Detail);
	    	    if (fileDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	    	        List<String> filenames = fileDialog.selectedFiles();
	    	        String filename = filenames.get(0);
	    	        if (log.isDebugEnabled())
	    	        	log.debug("browseFilename(): selected filename=" + filename);
	    	        RE3Properties.setProperty(settingId, filename);
	    	        model.setData(sourceIndex, RE3Properties.getProperty(settingId));
	    	    }
	    	} catch (Exception e) {
	    		log.error("browseFilename(): error", e);
	    	}
	    }
    }

    private void scanNow() {
    	String directory = OSHelper.getMusicDirectory();
    	if (directory.length() > 0) {
    		ImportFilesTask importTask = new ImportFilesTask(directory, false, false);
    		importTask.setImportPlaylists(RE3Properties.getBoolean("add_playlists_during_import"));
    		TaskManager.runBackgroundTask(importTask);
    	}
    }

    private void cleanOrphanedImages() {
    	TaskManager.runForegroundTask(new OrphanedImagesDeleterTask());
    }

    private class DataMinerClearer {
    	final String id;
    	DataMinerClearer(String id) {
    		this.id = id;
    	}
    	void clearData() {
    		Byte dataSource = DataMiningDataDeleter.getDataSourceFromId(id);
    		if (dataSource == null) {
    			QMessageBox.warning(RapidEvolution3UI.instance, Translations.get("clear_data_source_problem_title"), Translations.get("clear_data_source_problem_text"));
    		}
    		String dataSourceName = DataConstantsHelper.getDataSourceDescription(dataSource);
    		if (QMessageBox.question(RapidEvolution3UI.instance, Translations.get("clear_data_source_title"), Translations.get("clear_data_source_text", "%source%", dataSourceName), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
    			return;
    		}
    		TaskManager.runForegroundTask(new DataMiningDataDeleter(dataSource));
    	}
    }

    private void resetTagReadTimes() { TaskManager.runForegroundTask(new SongTagReadTimesClearer()); }

    private void clearArtists() { TaskManager.runForegroundTask(new ExternalItemRemovalTask(Database.getArtistIndex())); }
    private void clearLabels() { TaskManager.runForegroundTask(new ExternalItemRemovalTask(Database.getLabelIndex())); }
    private void clearReleases() { TaskManager.runForegroundTask(new ExternalItemRemovalTask(Database.getReleaseIndex())); }
    private void clearSongs() { TaskManager.runForegroundTask(new ExternalItemRemovalTask(Database.getSongIndex())); }

}