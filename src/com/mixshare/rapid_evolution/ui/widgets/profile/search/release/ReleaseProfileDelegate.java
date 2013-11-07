package com.mixshare.rapid_evolution.ui.widgets.profile.search.release;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmCommonProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.SimilarModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.profile.search.release.ReleaseDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.release.ReleaseSongModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.release.SimilarReleasesModelManager;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.search.release.ReleaseDetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.SearchProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;
import com.trolltech.qt.gui.QTabWidget;

public class ReleaseProfileDelegate extends SearchProfileDelegate {

	////////////
	// FIELDS //
	////////////
	
	private ReleaseProfile releaseProfile;
		
	private ReleaseSongModelManager releaseSongsModel; 
	private RecordTabTableWidget songsWidget;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public ReleaseProfileDelegate(ReleaseProfile releaseProfile, QTabWidget itemDetailTabsWidget) {
		super(itemDetailTabsWidget, releaseProfile);
		this.releaseProfile = releaseProfile;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public SearchProfile getSearchProfile() { return releaseProfile; }
	
	public String getTitleText() {
		StringBuffer result = new StringBuffer();
		Vector<ArtistRecord> artists = releaseProfile.getReleaseRecord().getArtists();
		for (ArtistRecord artist : artists) {
			if (result.length() > 0)
				result.append(" & ");
			ArtistIdentifier artistId = artist.getArtistIdentifier();
			result.append("<a href=\"");
			result.append(artistId.getUniqueId());
			result.append("\">");
			result.append(artistId.toString());
			result.append("</a>");
		}
		if (artists.size() == 0)
			result.append(Translations.get("release_compilation_artist_description"));
		result.append(" - ");		
		result.append(releaseProfile.getReleaseTitle());
		return result.toString();
	}
	
	public DetailsWidgetUI getDetailsWidget() {
		ReleaseDetailsModelManager releaseDetailsModel = (ReleaseDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ReleaseDetailsModelManager.class);
		releaseDetailsModel.setRelativeProfile(releaseProfile);
		return new ReleaseDetailsWidgetUI(releaseDetailsModel);		
	}
	
	public SimilarModelManagerInterface getSimilarModelInterface() {
		SimilarReleasesModelManager similarReleasesModel = (SimilarReleasesModelManager)Database.getRelativeModelFactory().getRelativeModelManager(SimilarReleasesModelManager.class);
		similarReleasesModel.setRelativeRelease(releaseProfile);
		return similarReleasesModel;
	}
	
	public RecordTabTableWidget getSimilarTableWidget(RecordTableModelManager tableModelManager, Column sortColumn) {
		return new ReleaseSimilarTableWidget(tableModelManager, sortColumn);
	}	
	
	public Vector<Tab> getTabs() {
		Vector<Tab> result = super.getTabs();
		
		// songs tab
		releaseSongsModel = (ReleaseSongModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ReleaseSongModelManager.class);
		releaseSongsModel.setRelativeRelease(releaseProfile);
		songsWidget = new ReleaseSongTabTableWidget(releaseSongsModel, COLUMN_TRACK);
		result.add(new Tab(Translations.get("tab_title_songs"), songsWidget));		
		
		return result;
	}
	
	public String getTabIndexTitle() {
		return UIProperties.getProperty("release_tab_index");
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setTabIndexTitle(String tabTitle) {
		UIProperties.setProperty("release_tab_index", tabTitle);
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void refresh() {
		super.refresh();

		// songs
		if (songsWidget.isLoaded())
			releaseSongsModel.reset();
		
	}
	
	public void unload() {
		super.unload();

	}
	
	protected void addInfoSections() {
		LastfmCommonProfile lastfmProfile = (LastfmCommonProfile)releaseProfile.getMinedProfile(DATA_SOURCE_LASTFM);
		if (lastfmProfile != null)
			infoWidget.addInfoSection(DataConstantsHelper.getDataSourceDescription(DATA_SOURCE_LASTFM), lastfmProfile.getWikiText());
	}
	
}
