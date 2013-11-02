package com.mixshare.rapid_evolution.ui.model.profile.details;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.profile.CommonProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.ui.RapidEvolution3UI;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.table.DetailsTableItemModel;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRating;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QTableView;

abstract public class CommonDetailsModelManager extends AbstractCommonDetailsModelManager {

	static private Logger log = Logger.getLogger(CommonDetailsModelManager.class);
	
	////////////
	// FIELDS //
	////////////
	
	protected Profile relativeProfile;
	
	transient private boolean disableDataChangedEvent;
	
	/////////////////
	// CONSTRUCTOR //	
	/////////////////
	
	public CommonDetailsModelManager() { }

	public CommonDetailsModelManager(LineReader lineReader) {
		super(lineReader);
		int version = Integer.parseInt(lineReader.getNextLine());
	}		

	/////////////
	// GETTERS //
	/////////////
	
	public DetailsTableItemModel getDetailsTableItemModel() { return (DetailsTableItemModel)model; }
	public DetailsProxyModel getDetailsProxyModel() { return (DetailsProxyModel)getProxyModel(); }
	
	public Profile getRelativeProfile() { return relativeProfile; }
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setRelativeProfile(Profile relativeProfile) { this.relativeProfile = relativeProfile; }
			
	/////////////
	// GETTERS //
	/////////////
	
	public Object getSourceData(short columnId, Object record) {
		if (log.isTraceEnabled())
			log.trace("getColumnData(): columnId=" + columnId + ", record=" + record);		
		CommonProfile commonProfile = (CommonProfile)record;
		if (COLUMN_RATING_STARS.getColumnId() == columnId)
			return new StarRating(commonProfile.getRating());			
		if (COLUMN_DISABLED.getColumnId() == columnId)
			return new Boolean(commonProfile.isDisabled());		
		if (COLUMN_UNIQUE_ID.getColumnId() == columnId)
			return commonProfile.getUniqueId();
		return null;
	}
	
	public boolean isDisableDataChangedEvent() {
		return disableDataChangedEvent;
	}
	
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
			CommonProfile commonProfile = (CommonProfile)profile;
			short type = column.getColumnId();
			if (COLUMN_RATING_STARS.getColumnId() == type) {
				if (value == null) {
					commonProfile.setRating(Rating.NO_RATING, DATA_SOURCE_USER);
				} else {					
					Rating newRating = ((StarRating)value).getRating();
					//if (!newRating.equals(searchProfile.getRating()))
					commonProfile.setRating(newRating, DATA_SOURCE_USER);
				}
			} else if (COLUMN_DISABLED.getColumnId() == type) {
				commonProfile.setDisabled((Boolean)value);
			}
		} catch (Exception e) {
			log.error("setFieldValue(): error", e);
		}
	}
	
	public void setSourceColumnVisibilities(QTableView tableView, String searchText) {
		int i = 0;
		for (Column column : getSourceColumnOrder()) {
			tableView.setRowHidden(i++, column.isHidden() || !StringUtil.substring(searchText, column.getColumnTitle()));
		}
	}
	
	public void setDisableDataChangedEvent(boolean disableDataChangedEvent) {
		this.disableDataChangedEvent = disableDataChangedEvent;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	protected void createSourceModel(QObject parent) {
    	model = new DetailsTableItemModel(getNumColumns(),2);    	
    	loadTable();
    	if (log.isDebugEnabled())
    		log.debug("createSourceModel(): connecting");
    	model.dataChanged.connect(this, "dataChanged(QModelIndex,QModelIndex)");
	}				
		
	protected void loadTable() {
		initViewColumns();
    	for (int i = 0; i < getNumColumns(); ++i) {
    		model.setData(i,0, getSourceColumnData(i, relativeProfile));
    		getDetailsTableItemModel().setVerticalHeaderLabel(i, getSourceColumnTitle(i), getSourceColumnType(i).getColumnDescription());
    	}		
	}
	
	public void refresh() {
		model.dataChanged.disconnect(this, "dataChanged(QModelIndex,QModelIndex)");
    	for (int i = 0; i < getNumColumns(); ++i) {
    		model.setData(i,0, getSourceColumnData(i, relativeProfile));
    	}		
    	model.dataChanged.connect(this, "dataChanged(QModelIndex,QModelIndex)");
	}
	
	/**
	 * Called when the view is constructed, to set the initial column sizes
	 */
	public void setSourceColumnSizes(QTableView tableView) {
		int index = 0;
		for (int c = 0; c < getNumColumns(); ++c) {
			Column column = getSourceColumnType(c);
			//if (!column.isHidden()) {
				if (column.getSize() != -1) {
					tableView.verticalHeader().sectionResized.disconnect();
					tableView.setRowHeight(index, column.getSize());
					tableView.verticalHeader().sectionResized.connect(this, "columnResized(Integer,Integer,Integer)");
				}
					//tableView.verticalHeader().resizeSection(index, column.getSize());
				++index;
			//}
		}
		int i = 0;
		for (Column column : getSourceColumnOrder())
			tableView.setRowHidden(i++, column.isHidden());				
	}	
	
	protected void handleAlreadyExists(Profile profile, Identifier newId) {
		Profile existingProfile = Database.getProfile(newId);
		if (existingProfile != null) {
			boolean skipPrompt = false;
			if (existingProfile instanceof SearchProfile)
				if (((SearchProfile)existingProfile).isExternalItem())
					skipPrompt = true;
			if (!skipPrompt) {
				String description = Translations.get("dialog_merge_profiles_text");
				String oldIdToken = Translations.getPreferredCase("%oldId%");
				String newIdToken = Translations.getPreferredCase("%newId%");
				description = StringUtil.replace(description, oldIdToken, profile.getIdentifier().toString());
				description = StringUtil.replace(description, newIdToken, newId.toString());					
				if (QMessageBox.question(RapidEvolution3UI.instance, Translations.get("dialog_merge_profiles_title"), description, QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
					profile.getRecord().update(); // refresh profile with old info 
					return;
				}
			}
			Database.mergeProfiles(existingProfile, profile);
		}
	}	
		
	////////////
	// EVENTS //
	////////////
	
    private void dataChanged(QModelIndex topLeft, QModelIndex bottomRight) {
    	Object data = proxyModel.sourceModel().data(topLeft.row(), 0);    	
    	if (data != null) {
    		if (log.isTraceEnabled())
    			log.trace("dataChanged(): row=" + topLeft.row() + ", data=" + data);
    		if (!disableDataChangedEvent)
    			setFieldValue(getSourceColumnType(topLeft.row()), data);
    	}
    }	
    
    public void resetModel() { loadTable(); }
	
	public void updatedRecord(Record record) { } // right now, profilewidgetUI handles this event, but it could possibly be handled here?

	public void write(LineWriter writer) {
		super.write(writer);
		writer.writeLine(1); // version
	}
	
}
