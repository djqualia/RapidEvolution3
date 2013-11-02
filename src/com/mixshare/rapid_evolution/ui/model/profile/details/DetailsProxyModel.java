package com.mixshare.rapid_evolution.ui.model.profile.details;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QSortFilterProxyModel;

public class DetailsProxyModel extends QSortFilterProxyModel implements AllColumns {

	static private Logger log = Logger.getLogger(DetailsProxyModel.class);
	
	////////////
	// FIELDS //
	////////////
	
	protected CommonDetailsModelManager detailsModelManager;
	protected Profile relativeProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public DetailsProxyModel(CommonDetailsModelManager detailsModelManager) {
		this.detailsModelManager = detailsModelManager;
		detailsModelManager.setProxyModel(this);
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setRelativeProfile(Profile relativeProfile) {
		this.relativeProfile = relativeProfile;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		//boolean result = super.filterAcceptsRow(sourceRow, sourceParent);
		//if (!result) {
			//result = StringUtil.substring(filterRegExp().pattern(), detailsModelManager.getSourceColumnTitle(sourceRow));
		//}
		//if (detailsModelManager.getSourceColumnType(sourceRow).isHidden())
			//result = false;
		//return result;
		return true;
	}
	
}
