package com.mixshare.rapid_evolution.ui.model.profile.details;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QItemDelegate;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QWidget;

public class DetailsItemDelegate extends QItemDelegate implements AllColumns {
	
	static private Logger log = Logger.getLogger(DetailsItemDelegate.class);
	
	private CommonDetailsModelManager detailsModelManager;
	private Profile relativeProfile;
	
    public DetailsItemDelegate(QWidget parent, CommonDetailsModelManager detailsModelManager, Profile relativeProfile) {
        super(parent);
        this.detailsModelManager = detailsModelManager;
        this.relativeProfile = relativeProfile;
    }

    @Override
    public QWidget createEditor(QWidget parent, QStyleOptionViewItem item, QModelIndex index) {
		int sourceRow = index.row();
    	if (detailsModelManager.getSourceColumnType(sourceRow).equals(COLUMN_ARTIST_DESCRIPTION)) {
			// make the "artists" field un-editable when it is a compilation
			if (relativeProfile instanceof ReleaseProfile) {
				ReleaseProfile release = (ReleaseProfile)relativeProfile;
				if (release.isCompilationRelease())
					return null;
			}
		}		
    	return super.createEditor(parent, item, index);
    }
   
}