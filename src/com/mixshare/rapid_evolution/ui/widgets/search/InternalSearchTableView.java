package com.mixshare.rapid_evolution.ui.widgets.search;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.dialogs.filter.AddFilterDegreeDialog;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.details.SearchDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.search.util.FieldClearTrigger;
import com.mixshare.rapid_evolution.ui.widgets.search.util.FieldSetTrigger;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.importers.filesystem.ImportFilesTask;
import com.mixshare.rapid_evolution.workflow.maintenance.search.SearchRecordComputer;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QMenu;

abstract public class InternalSearchTableView extends SearchTableView {

	static private Logger log = Logger.getLogger(InternalSearchTableView.class);

	////////////
	// FIELDS //
	////////////

    protected QAction playAction;
    protected QAction separator1;
    protected QAction editAction;
    protected QAction separator2;
    protected QAction separator3;
    protected QAction addAction;
    protected QAction deleteAction;

    protected QMenu fieldsMenu;
    protected QAction fieldsMenuAction;

    protected QMenu addToMenu;
    protected QAction addToMenuAction;

    protected QAction addToStyleAction;
    protected QAction addToTagAction;
    protected QAction addToPlaylistAction;

    protected QMenu fieldsSetMenu;
    protected QAction fieldsSetMenuAction;

    protected QMenu fieldsClearMenu;
    protected QAction fieldsClearMenuAction;

    protected QMenu fieldsParseMenu;
    protected QAction fieldsParseMenuAction;

    //protected QAction fieldReplaceAction;
    //protected QAction fieldCopyAction;
    //protected QAction fieldPrependAction;
    //protected QAction fieldAppendAction;


	/////////////////
	// CONSTRUCTOR //
	/////////////////

	public InternalSearchTableView(SearchModelManager modelManager) {
		super(modelManager);

        playAction = new QAction(Translations.get("play_text"), this);
        playAction.triggered.connect(this, "playRecords()");
        playAction.setIcon(new QIcon(RE3Properties.getProperty("menu_play_icon")));

        separator1 = new QAction("", this);
        separator1.setSeparator(true);

        editAction = new QAction(Translations.get("search_table_menu_edit"), this);
        editAction.triggered.connect(this, "editRecord()");
        editAction.setIcon(new QIcon(RE3Properties.getProperty("menu_edit_icon")));

        separator2 = new QAction("", this);
        separator2.setSeparator(true);

        separator3 = new QAction("", this);
        separator3.setSeparator(true);

        addAction = new QAction(Translations.get("search_table_menu_add"), this);
        addAction.triggered.connect(this, "addRecords()");
        addAction.setIcon(new QIcon(RE3Properties.getProperty("menu_add_icon")));

        deleteAction = new QAction(Translations.get("search_table_menu_delete"), this);
        deleteAction.triggered.connect(this, "deleteRecords()");
        deleteAction.setIcon(new QIcon(RE3Properties.getProperty("menu_remove_icon")));

        // fields set menu
        fieldsSetMenu = new QMenu(Translations.get("search_table_menu_fields_set"), this);
        SearchDetailsModelManager detailsModelManager = getDetailsModelManager();
        Vector<Column> sortedColumns = detailsModelManager.getSourceColumnOrderCopy();
        for (int i = 0; i < sortedColumns.size(); ++i) {
        	if (!sortedColumns.get(i).isGroupEditable()) {
        		sortedColumns.remove(i);
        		--i;
        	}
        }
        java.util.Collections.sort(sortedColumns, new Comparator<Column>() {
        	@Override
			public int compare(Column c1, Column c2) {
        		return c1.getColumnTitle().compareToIgnoreCase(c2.getColumnTitle());
        	}
        });
        for (Column column : sortedColumns) {
        	QAction columnSetAction = new QAction(column.getColumnTitle(), this);
        	columnSetAction.triggered.connect(new FieldSetTrigger(column, this, detailsModelManager), "setFields()");
        	fieldsSetMenu.addAction(columnSetAction);
        }
        fieldsSetMenuAction = new QAction(Translations.get("search_table_menu_fields_set"), this);
        fieldsSetMenuAction.setMenu(fieldsSetMenu);
        fieldsSetMenuAction.setIcon(new QIcon(RE3Properties.getProperty("menu_set_fields_icon")));

        // fields clear menu
        fieldsClearMenu = new QMenu(Translations.get("search_table_menu_fields_clear"), this);
        fieldsClearMenu.setIcon(new QIcon(RE3Properties.getProperty("menu_clear_icon")));
        sortedColumns = detailsModelManager.getSourceColumnOrderCopy();
        for (int i = 0; i < sortedColumns.size(); ++i) {
        	if (!sortedColumns.get(i).isClearable()) {
        		sortedColumns.remove(i);
        		--i;
        	}
        }
        java.util.Collections.sort(sortedColumns, new Comparator<Column>() {
        	@Override
			public int compare(Column c1, Column c2) {
        		return c1.getColumnTitle().compareToIgnoreCase(c2.getColumnTitle());
        	}
        });
        for (Column column : sortedColumns) {
        	QAction columnClearAction = new QAction(column.getColumnTitle(), this);
        	columnClearAction.triggered.connect(new FieldClearTrigger(this, column, this, detailsModelManager), "clearFields()");
        	fieldsClearMenu.addAction(columnClearAction);
        }
        fieldsClearMenuAction = new QAction(Translations.get("search_table_menu_fields_clear"), this);
        fieldsClearMenuAction.setMenu(fieldsClearMenu);
        fieldsClearMenuAction.setIcon(new QIcon(RE3Properties.getProperty("menu_clear_icon")));

        fieldsParseMenu = new QMenu(Translations.get("search_table_menu_fields_parse"), this);
        fieldsParseMenuAction = new QAction(Translations.get("search_table_menu_fields_parse"), this);
        fieldsParseMenuAction.setMenu(fieldsParseMenu);
        fieldsParseMenuAction.setIcon(new QIcon(RE3Properties.getProperty("menu_parse_fields_icon")));
        fieldsParseMenuAction.setVisible(false);

        fieldsMenu = new QMenu(Translations.get("search_table_menu_fields"), this);
        fieldsMenu.addAction(fieldsClearMenuAction);
        fieldsMenu.addAction(fieldsSetMenuAction);
        fieldsMenu.addAction(fieldsParseMenuAction);
        fieldsMenuAction = new QAction(Translations.get("search_table_menu_fields"), this);
        fieldsMenuAction.setMenu(fieldsMenu);
        fieldsMenuAction.setIcon(new QIcon(RE3Properties.getProperty("menu_fields_icon")));

        addToStyleAction = new QAction(Translations.get("text_style"), this);
        addToStyleAction.triggered.connect(this, "addToStyle()");
        addToTagAction = new QAction(Translations.get("text_tag"), this);
        addToTagAction.triggered.connect(this, "addToTag()");
        addToPlaylistAction = new QAction(Translations.get("text_playlist"), this);
        addToPlaylistAction.triggered.connect(this, "addToPlaylist()");

        addToMenu = new QMenu(Translations.get("search_table_menu_addto"), this);
        addToMenu.addAction(addToStyleAction);
        addToMenu.addAction(addToTagAction);
        addToMenu.addAction(addToPlaylistAction);
        addToMenuAction = new QAction(Translations.get("search_table_menu_addto"), this);
        addToMenuAction.setMenu(addToMenu);
        addToMenuAction.setIcon(new QIcon(RE3Properties.getProperty("menu_add_icon")));
	}

	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////

	abstract protected SearchDetailsModelManager getDetailsModelManager();

	abstract protected void deleteRecords();

	////////////////////
	// SLOTS (EVENTS) //
	////////////////////

    @Override
	protected void keyPressEvent(QKeyEvent keyEvent) {
    	super.keyPressEvent(keyEvent);
		if (keyEvent.key() == Qt.Key.Key_Delete.value()) {
			if (getSelectedRecords().size() > 0)
				deleteRecords();
		}
    }


	@Override
	protected void selectionChanged() {
    	Vector<SearchRecord> selectedStyles = getSelectedRecords();
        removeAction(playAction);
        removeAction(addAction);
        removeAction(deleteAction);
        removeAction(editAction);
        removeAction(mergeAction);
        removeAction(separator1);
        removeAction(separator2);
        removeAction(separator3);
        removeAction(fieldsMenuAction);
        removeAction(addToMenuAction);
    	if (selectedStyles.size() == 0) {
            addAction(addAction);
    	} else if (selectedStyles.size() == 1) {
            addAction(playAction);
            addAction(separator1);
            addAction(editAction);
            addAction(separator3);
            addAction(addToMenuAction);
            addAction(fieldsMenuAction);
            addAction(separator2);
            addAction(addAction);
            addAction(deleteAction);
    	} else if (selectedStyles.size() > 1) {
            addAction(playAction);
            addAction(separator1);
            addAction(addToMenuAction);
            addAction(fieldsMenuAction);
            addAction(mergeAction);
            addAction(separator3);
            addAction(separator2);
            addAction(addAction);
            addAction(deleteAction);
    	}
    	SearchWidgetUI.instance.updateSelectedLabel(selectedStyles.size());

    }

    public void addRecords() {
		QFileDialog fileDialog = new QFileDialog(this);
		fileDialog.setFileMode(QFileDialog.FileMode.ExistingFiles);
		fileDialog.setViewMode(QFileDialog.ViewMode.Detail);
		fileDialog.setFilters(FileUtil.getSupportedFileFilters());
		if (Database.getProperty("last_added_records_directory") != null)
			fileDialog.setDirectory((String)Database.getProperty("last_added_records_directory"));
	    if (fileDialog.exec() == QDialog.DialogCode.Accepted.value()) {
	        List<String> filenames = fileDialog.selectedFiles();
	        String filename = filenames.get(0);
	        Database.setProperty("last_added_records_directory", FileUtil.getDirectoryFromFilename(filename));
	        TaskManager.runForegroundTask(new ImportFilesTask(filenames));
	    }
    }

    private void editRecord() {
    	Vector<SearchRecord> selectedSearchRecords = getSelectedRecords();
    	Profile profile = Database.getProfile(selectedSearchRecords.get(0).getIdentifier());
    	ProfileWidgetUI.instance.showProfile(profile);

    }

    private void addToStyle() {
    	try {
        	Vector<SearchRecord> selectedSearchRecords = getSelectedRecords();
			AddFilterDegreeDialog addFilterDialog = new AddFilterDegreeDialog(Database.getStyleModelManager().getStyleCompleter(), Database.getStyleModelManager(), selectedSearchRecords, false);
	    	addFilterDialog.setTitle(Translations.get("add_style_text"));
	    	addFilterDialog.setLabel(Translations.get("style_name_text"));
	    	addFilterDialog.show();
	    	addFilterDialog.raise();
	    	addFilterDialog.activateWindow();
    	} catch (Exception e) {
    		log.error("addToStyle(): error", e);
    	}
    }

    private void addToTag() {
    	try {
        	Vector<SearchRecord> selectedSearchRecords = getSelectedRecords();
			AddFilterDegreeDialog addFilterDialog = new AddFilterDegreeDialog(Database.getTagModelManager().getTagCompleter(), Database.getTagModelManager(), selectedSearchRecords, false);
	    	addFilterDialog.setTitle(Translations.get("add_tag_text"));
	    	addFilterDialog.setLabel(Translations.get("tag_name_text"));
	    	addFilterDialog.show();
	    	addFilterDialog.raise();
	    	addFilterDialog.activateWindow();
    	} catch (Exception e) {
    		log.error("addToTag(): error", e);
    	}
    }

    private void addToPlaylist() {
    	try {
        	Vector<SearchRecord> selectedSearchRecords = getSelectedRecords();
			AddFilterDegreeDialog addFilterDialog = new AddFilterDegreeDialog(Database.getPlaylistModelManager().getPlaylistCompleter(), Database.getPlaylistModelManager(), selectedSearchRecords, true);
	    	addFilterDialog.setTitle(Translations.get("add_playlist_text"));
	    	addFilterDialog.setLabel(Translations.get("playlist_name_text"));
	    	addFilterDialog.show();
	    	addFilterDialog.raise();
	    	addFilterDialog.activateWindow();
    	} catch (Exception e) {
    		log.error("addToPlaylist(): error", e);
    	}
    }

    private void recomputeRecords() {
    	Vector<SearchRecord> selectedSearchRecords = getSelectedRecords();
    	TaskManager.runForegroundTask(new SearchRecordComputer(selectedSearchRecords));
    }

}
