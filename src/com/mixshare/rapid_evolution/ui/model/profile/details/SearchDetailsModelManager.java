package com.mixshare.rapid_evolution.ui.model.profile.details;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.user.UserData;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.UserDataColumn;
import com.mixshare.rapid_evolution.ui.model.search.SearchModelManager;
import com.mixshare.rapid_evolution.ui.updaters.model.table.DetailsTableModelColumnUpdater;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.gui.QApplication;


abstract public class SearchDetailsModelManager extends CommonDetailsModelManager {

    static private Logger log = Logger.getLogger(SearchDetailsModelManager.class);	
	
    ////////////
    // FIELDS //
    ////////////
    
	private SearchModelManager searchModelManager;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public SearchDetailsModelManager() { }
	public SearchDetailsModelManager(SearchModelManager searchModelManager) {
		this.searchModelManager = searchModelManager;
	}
	public SearchDetailsModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
		searchModelManager = SearchModelManager.readSearchModelManager(lineReader);
	}			
	
	/////////////
	// GETTERS //
	/////////////
	
	public Object getSourceData(short columnId, Object record) {
		SearchProfile searchProfile = (SearchProfile)record;
		if (COLUMN_COMMENTS.getColumnId() == columnId)
			return searchProfile.getComments();		
		for (UserDataColumn userColumn : searchModelManager.getUserDataTypeColumns()) {
			if (userColumn.getColumnId() == columnId) {
				Object userData = searchProfile.getUserData(userColumn.getUserDataType());
				if (userData != null) {
					if (userColumn.getUserDataType().getFieldType() == UserDataType.TYPE_TEXT_FIELD)
						return (String)userData;
					if (userColumn.getUserDataType().getFieldType() == UserDataType.TYPE_BOOLEAN_FLAG) {
						return (Boolean)userData;
					}
				} else {
					// default values
					if (userColumn.getUserDataType().getFieldType() == UserDataType.TYPE_TEXT_FIELD)
						return "";
					if (userColumn.getUserDataType().getFieldType() == UserDataType.TYPE_BOOLEAN_FLAG) {
						return Boolean.FALSE;
					}					
				}
			}		
		}
		return super.getSourceData(columnId, record);
	}

	public SearchModelManager getSearchModelManager() { return searchModelManager; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setFieldValue(Column column, Object value) {
		SearchProfile searchProfile = (SearchProfile)getRelativeProfile();
		setFieldValue(column, value, searchProfile);
	}

	public void setFieldValue(Column column, Object value, Profile profile) {
		if (log.isTraceEnabled())
			log.trace("setFieldValue(): column=" + column + ", value=" + value);
		try {
			SearchProfile searchProfile = (SearchProfile)profile;
			short type = column.getColumnId();
			if (COLUMN_COMMENTS.getColumnId() == type) {
				if (value == null)
					searchProfile.setComments("", DATA_SOURCE_USER);
				else
					searchProfile.setComments(value.toString(), DATA_SOURCE_USER);
			} else {
				boolean foundUserColumnMatch = false;
				for (UserDataColumn userColumn : searchModelManager.getUserDataTypeColumns()) {
					if (userColumn.getColumnId() == type) {
						foundUserColumnMatch = true;
						if (userColumn.getUserDataType().getFieldType() == UserDataType.TYPE_TEXT_FIELD) {
							if (value == null)
								searchProfile.setUserData(new UserData(userColumn.getUserDataType(), ""));
							else
								searchProfile.setUserData(new UserData(userColumn.getUserDataType(), value.toString()));
						} else if (userColumn.getUserDataType().getFieldType() == UserDataType.TYPE_BOOLEAN_FLAG)
							searchProfile.setUserData(new UserData(userColumn.getUserDataType(), (Boolean)value));
					}
				}
				if (!foundUserColumnMatch)
					super.setFieldValue(column, value, searchProfile);
			}
		} catch (Exception e) {
			log.error("setFieldValue(): error", e);
		}
	}

	public void setSearchModelManager(SearchModelManager searchModelManager) { this.searchModelManager = searchModelManager; }
	
	/////////////
	// METHODS //
	/////////////
	
	public void addUserColumn(UserDataColumn newColumn) {
		Column detailsColumn = newColumn.getInstance(true, -1);
		sourceColumns.add(detailsColumn);
		viewColumns.add(detailsColumn);
		if (RapidEvolution3UI.instance != null) {
			QApplication.invokeAndWait(new DetailsTableModelColumnUpdater(getDetailsTableItemModel(), DetailsTableModelColumnUpdater.ACTION_ADD));
		}
		int index = getNumColumns() - 1;
		if (getDetailsTableItemModel() != null) {
			getDetailsTableItemModel().setVerticalHeaderLabel(index, getSourceColumnTitle(index), getSourceColumnType(index).getColumnDescription());
			model.setData(index,0, getSourceColumnData(index, relativeProfile));
		}
	}
	
	/////////////////
	// SUB CLASSES //
	/////////////////
	
	protected class AlreadyExistsHandleThread extends Thread {
		private Profile profile;
		private Identifier id;
		public AlreadyExistsHandleThread(Profile profile, Identifier id) {
			this.profile = profile;
			this.id = id;
		}
		public void run() {
			handleAlreadyExists(profile, id);
		}
	}

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
		SearchModelManager.saveSearchModelManager(searchModelManager, writer);
	}
	
}
