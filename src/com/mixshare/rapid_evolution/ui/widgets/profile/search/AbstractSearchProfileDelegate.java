package com.mixshare.rapid_evolution.ui.widgets.profile.search;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.image.ImageViewer;
import com.mixshare.rapid_evolution.ui.widgets.profile.CommonProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabInfoWidget;
import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QWidget;

abstract public class AbstractSearchProfileDelegate extends CommonProfileDelegate {

	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public AbstractSearchProfileDelegate(QTabWidget itemDetailTabsWidget) {
		super(itemDetailTabsWidget);
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public SearchProfile getSearchProfile();		
	abstract protected void addInfoSections();

	/////////////
	// GETTERS //
	/////////////
	
	public Vector<Tab> getTabs() {
		Vector<Tab> result = new Vector<Tab>();
		
		// info tab
		infoWidget = new TabInfoWidget();
		addInfoSections();
		if (infoWidget.hasContent())
			result.add(new Tab(Translations.get("tab_title_info"), infoWidget));		
		
		// details table
		detailsWidget = getDetailsWidget();
		result.add(new Tab(Translations.get("tab_title_details"), detailsWidget));					
		
		return result;
	}	
	
	/////////////
	// METHODS //
	/////////////
	
	public QWidget createImageViewerWidget(QWidget parent, Profile profile) {
		SearchProfile searchProfile = (SearchProfile)profile;
		imageViewer = new ImageViewer(parent, searchProfile);
		if (getSearchProfile().getImages().size() > 0)
			imageViewer.setImages(getSearchProfile().getImages());
		return imageViewer;
	}

	public void refresh() {		
		// main image viewer
		if (!getSearchProfile().getThumbnailImageFilename().equals(SearchRecord.DEFAULT_THUMBNAIL_IMAGE))
			imageViewer.setImages(getSearchProfile().getImages(), false);
		
		// info tab
		boolean infoVisible = infoWidget.hasContent();
		addInfoSections();
		if (infoWidget.hasContent() && !infoVisible)
			itemDetailTabsWidget.insertTab(0, infoWidget, Translations.get("tab_title_info"));
		
		super.refresh();
	}
	
}
