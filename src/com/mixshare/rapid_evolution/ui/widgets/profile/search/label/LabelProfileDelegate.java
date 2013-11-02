package com.mixshare.rapid_evolution.ui.widgets.profile.search.label;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabelProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmCommonProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.SimilarModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.LabelArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.LabelArtistTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.LabelDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.LabelReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.LabelSongModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.label.SimilarLabelsModelManager;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.search.label.LabelDetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.SearchProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;
import com.trolltech.qt.gui.QTabWidget;

public class LabelProfileDelegate extends SearchProfileDelegate {

	////////////
	// FIELDS //
	////////////
	
	private LabelProfile labelProfile;
		
	private LabelReleaseModelManager labelReleasesModel;
	private RecordTabTableWidget releasesWidget;
	
	private LabelSongModelManager labelSongsModel; 
	private RecordTabTableWidget songsWidget;
	
	private LabelArtistModelManager labelArtistsModel;
	private RecordTabTableWidget artistsWidget;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public LabelProfileDelegate(LabelProfile labelProfile, QTabWidget itemDetailTabsWidget) {
		super(itemDetailTabsWidget, labelProfile);
		this.labelProfile = labelProfile;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public SearchProfile getSearchProfile() { return labelProfile; }
	
	public String getTitleText() { return labelProfile.toString(); }
	
	public DetailsWidgetUI getDetailsWidget() {
		LabelDetailsModelManager labelDetailsModel = (LabelDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(LabelDetailsModelManager.class);
		labelDetailsModel.setRelativeProfile(labelProfile);
		return new LabelDetailsWidgetUI(labelDetailsModel);		
	}
	
	public SimilarModelManagerInterface getSimilarModelInterface() {
		SimilarLabelsModelManager similarLabelsModel = (SimilarLabelsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(SimilarLabelsModelManager.class);
		similarLabelsModel.setRelativeLabel(labelProfile);
		return similarLabelsModel;
	}
	
	public RecordTabTableWidget getSimilarTableWidget(RecordTableModelManager tableModelManager, Column sortColumn) {
		return new LabelSimilarTableWidget(tableModelManager, sortColumn);
	}
	
	public Vector<Tab> getTabs() {
		Vector<Tab> result = super.getTabs();					
		
		// artists
		labelArtistsModel = (LabelArtistModelManager)Database.getRelativeModelFactory().getRelativeModelManager(LabelArtistModelManager.class);
		labelArtistsModel.setRelativeLabel(labelProfile);
		artistsWidget = new LabelArtistTabTableWidget(labelArtistsModel, COLUMN_DEGREE);
		result.add(new Tab(Translations.get("tab_title_artists"), artistsWidget));	
		
		// releases
		labelReleasesModel = (LabelReleaseModelManager)Database.getRelativeModelFactory().getRelativeModelManager(LabelReleaseModelManager.class);
		labelReleasesModel.setRelativeLabel(labelProfile);
		releasesWidget = new LabelReleaseTabTableWidget(labelReleasesModel, COLUMN_RELEASE_DESCRIPTION);
		result.add(new Tab(Translations.get("tab_title_releases"), releasesWidget));		
		
		// songs tab
		labelSongsModel = (LabelSongModelManager)Database.getRelativeModelFactory().getRelativeModelManager(LabelSongModelManager.class);
		labelSongsModel.setRelativeLabel(labelProfile);
		songsWidget = new LabelSongTabTableWidget(labelSongsModel, COLUMN_SONG_DESCRIPTION);
		result.add(new Tab(Translations.get("tab_title_songs"), songsWidget));		
		
		return result;
	}
	
	public String getTabIndexTitle() {
		return UIProperties.getProperty("label_tab_index");
	}
	
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setTabIndexTitle(String tabTitle) {
		UIProperties.setProperty("label_tab_index", tabTitle);
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void refresh() {
		super.refresh();

		// releases
		if (releasesWidget.isLoaded())
			labelReleasesModel.reset();		
		
		// songs
		if (songsWidget.isLoaded())
			labelSongsModel.reset();
		
		// artists
		if (artistsWidget.isLoaded())
			labelArtistsModel.reset();		
		
	}
	
	public void unload() {
		super.unload();

	}
	
	protected void addInfoSections() {
		LastfmCommonProfile lastfmProfile = (LastfmCommonProfile)labelProfile.getMinedProfile(DATA_SOURCE_LASTFM);
		if (lastfmProfile != null)
			infoWidget.addInfoSection(DataConstantsHelper.getDataSourceDescription(DATA_SOURCE_LASTFM), lastfmProfile.getWikiText());
		DiscogsLabelProfile discogsProfile = (DiscogsLabelProfile)labelProfile.getMinedProfile(DATA_SOURCE_DISCOGS);
		if (discogsProfile != null)
			infoWidget.addInfoSection(DataConstantsHelper.getDataSourceDescription(DATA_SOURCE_DISCOGS), discogsProfile.getProfile());
	}
	
}
