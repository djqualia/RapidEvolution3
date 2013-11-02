package com.mixshare.rapid_evolution.ui.widgets.profile.filter.style;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.profile.filter.style.StyleArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.style.StyleArtistTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.filter.style.StyleDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.style.StyleLabelModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.style.StyleLabelTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.filter.style.StyleReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.style.StyleReleaseTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.filter.style.StyleSongModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.style.StyleSongTabTableWidget;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.filter.style.StyleDetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.FilterProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;
import com.trolltech.qt.gui.QTabWidget;

public class StyleProfileDelegate extends FilterProfileDelegate {

	static private Logger log = Logger.getLogger(StyleProfileDelegate.class);
	
	////////////
	// FIELDS //
	////////////
	
	private StyleProfile styleProfile;
	
	private StyleSongModelManager styleSongsModel;
	private StyleSongTabTableWidget songsWidget;

	private StyleArtistModelManager styleArtistsModel;
	private RecordTabTableWidget artistsWidget;

	private StyleLabelModelManager styleLabelsModel;
	private RecordTabTableWidget labelsWidget;

	private StyleReleaseModelManager styleReleasesModel;
	private RecordTabTableWidget releasesWidget;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public StyleProfileDelegate(QTabWidget itemDetailTabsWidget, StyleProfile styleProfile) {
		super(itemDetailTabsWidget);
		this.styleProfile = styleProfile;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public String getTitleText() {
		return styleProfile.getStyleName();
	}
		
	public DetailsWidgetUI getDetailsWidget() {
		StyleDetailsModelManager styleDetailsModel = (StyleDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(StyleDetailsModelManager.class);
		styleDetailsModel.setRelativeProfile(styleProfile);
		return new StyleDetailsWidgetUI(styleDetailsModel);		
	}

	
	public String getTabIndexTitle() {
		return UIProperties.getProperty("style_tab_index");
	}	
	
	public Vector<Tab> getTabs() {
		Vector<Tab> result = super.getTabs();		

		// artists
		styleArtistsModel = (StyleArtistModelManager)Database.getRelativeModelFactory().getRelativeModelManager(StyleArtistModelManager.class);
		styleArtistsModel.setRelativeStyle(styleProfile);
		artistsWidget = new StyleArtistTabTableWidget(styleArtistsModel, COLUMN_DEGREE);
		result.add(new Tab(Translations.get("tab_title_artists"), artistsWidget));		

		// labels
		styleLabelsModel = (StyleLabelModelManager)Database.getRelativeModelFactory().getRelativeModelManager(StyleLabelModelManager.class);
		styleLabelsModel.setRelativeStyle(styleProfile);
		labelsWidget = new StyleLabelTabTableWidget(styleLabelsModel, COLUMN_DEGREE);
		result.add(new Tab(Translations.get("tab_title_labels"), labelsWidget));		

		// releases
		styleReleasesModel = (StyleReleaseModelManager)Database.getRelativeModelFactory().getRelativeModelManager(StyleReleaseModelManager.class);
		styleReleasesModel.setRelativeStyle(styleProfile);
		releasesWidget = new StyleReleaseTabTableWidget(styleReleasesModel, COLUMN_DEGREE);
		result.add(new Tab(Translations.get("tab_title_releases"), releasesWidget));		
		
		// songs
		styleSongsModel = (StyleSongModelManager)Database.getRelativeModelFactory().getRelativeModelManager(StyleSongModelManager.class);
		styleSongsModel.setRelativeStyle(styleProfile);
		songsWidget = new StyleSongTabTableWidget(styleSongsModel, COLUMN_DEGREE);
		result.add(new Tab(Translations.get("tab_title_songs"), songsWidget));		
		
		return result;
	}	
	
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setTabIndexTitle(String tabTitle) {
		UIProperties.setProperty("style_tab_index", tabTitle);
	}

	/////////////
	// METHODS //
	/////////////

	public void refresh() {
		super.refresh();	
		
		// artists
		if (artistsWidget.isLoaded()) {
			styleArtistsModel.reset();
			artistsWidget.updateFilter();
		}

		// labels
		if (labelsWidget.isLoaded()) {
			styleLabelsModel.reset();
			labelsWidget.updateFilter();
		}

		// releases
		if (releasesWidget.isLoaded()) {
			styleReleasesModel.reset();
			releasesWidget.updateFilter();
		}
		
		// songs
		if (songsWidget.isLoaded()) {
			styleSongsModel.reset();
			songsWidget.updateFilter();
		}

	}
	
}
