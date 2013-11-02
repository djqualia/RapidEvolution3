package com.mixshare.rapid_evolution.ui.widgets.profile.search.artist;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.bbc.artist.BBCArtistProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmCommonProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.SimilarModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.profile.search.artist.ArtistDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.artist.ArtistLabelModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.artist.ArtistLabelTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.search.artist.ArtistReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.artist.ArtistSongModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.artist.SimilarArtistsModelManager;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.search.artist.ArtistDetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.SearchProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;
import com.trolltech.qt.gui.QTabWidget;

public class ArtistProfileDelegate extends SearchProfileDelegate {

	////////////
	// FIELDS //
	////////////

	private final ArtistProfile artistProfile;

	private ArtistReleaseModelManager artistReleasesModel;
	private RecordTabTableWidget releasesWidget;

	private ArtistSongModelManager artistSongsModel;
	private RecordTabTableWidget songsWidget;

	private ArtistLabelModelManager artistLabelsModel;
	private RecordTabTableWidget labelsWidget;

	//////////////////
	// CONSTRUCTORS //
	//////////////////

	public ArtistProfileDelegate(ArtistProfile artistProfile, QTabWidget itemDetailTabsWidget) {
		super(itemDetailTabsWidget, artistProfile);
		this.artistProfile = artistProfile;
	}

	/////////////
	// GETTERS //
	/////////////

	@Override
	public SearchProfile getSearchProfile() { return artistProfile; }

	@Override
	public String getTitleText() { return artistProfile.toString(); }

	@Override
	public DetailsWidgetUI getDetailsWidget() {
		ArtistDetailsModelManager artistDetailsModel = (ArtistDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ArtistDetailsModelManager.class);
		artistDetailsModel.setRelativeProfile(artistProfile);
		return new ArtistDetailsWidgetUI(artistDetailsModel);
	}

	@Override
	public SimilarModelManagerInterface getSimilarModelInterface() {
		SimilarArtistsModelManager similarArtistsModel = (SimilarArtistsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(SimilarArtistsModelManager.class);
		similarArtistsModel.setRelativeArtist(artistProfile);
		return similarArtistsModel;
	}

	@Override
	public RecordTabTableWidget getSimilarTableWidget(RecordTableModelManager tableModelManager, Column sortColumn) {
		return new ArtistSimilarTableWidget(tableModelManager, sortColumn);
	}

	@Override
	public Vector<Tab> getTabs() {
		Vector<Tab> result = super.getTabs();

		// labels
		artistLabelsModel = (ArtistLabelModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ArtistLabelModelManager.class);
		artistLabelsModel.setRelativeArtist(artistProfile);
		labelsWidget = new ArtistLabelTabTableWidget(artistLabelsModel, COLUMN_DEGREE);
		result.add(new Tab(Translations.get("tab_title_labels"), labelsWidget));

		// releases
		artistReleasesModel = (ArtistReleaseModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ArtistReleaseModelManager.class);
		artistReleasesModel.setRelativeArtist(artistProfile);
		releasesWidget = new ArtistReleaseTabTableWidget(artistReleasesModel, COLUMN_RELEASE_DESCRIPTION);
		result.add(new Tab(Translations.get("tab_title_releases"), releasesWidget));

		// songs
		artistSongsModel = (ArtistSongModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ArtistSongModelManager.class);
		artistSongsModel.setRelativeArtist(artistProfile);
		songsWidget = new ArtistSongTabTableWidget(artistSongsModel, COLUMN_SONG_DESCRIPTION);
		result.add(new Tab(Translations.get("tab_title_songs"), songsWidget));

		return result;
	}

	@Override
	public String getTabIndexTitle() {
		return UIProperties.getProperty("artist_tab_index");
	}

	/////////////
	// SETTERS //
	/////////////

	@Override
	public void setTabIndexTitle(String tabTitle) {
		UIProperties.setProperty("artist_tab_index", tabTitle);
	}

	/////////////
	// METHODS //
	/////////////

	@Override
	public void refresh() {
		super.refresh();

		// releases
		if (releasesWidget.isLoaded())
			artistReleasesModel.reset();

		// songs
		if (songsWidget.isLoaded())
			artistSongsModel.reset();

		// labels
		if (labelsWidget.isLoaded())
			artistLabelsModel.reset();

	}

	@Override
	public void unload() {
		super.unload();

	}

	@Override
	protected void addInfoSections() {
		LastfmCommonProfile lastfmProfile = (LastfmCommonProfile)artistProfile.getMinedProfile(DATA_SOURCE_LASTFM);
		if (lastfmProfile != null)
			infoWidget.addInfoSection(DataConstantsHelper.getDataSourceDescription(DATA_SOURCE_LASTFM), lastfmProfile.getWikiText());
		DiscogsArtistProfile discogsProfile = (DiscogsArtistProfile)artistProfile.getMinedProfile(DATA_SOURCE_DISCOGS);
		if (discogsProfile != null)
			infoWidget.addInfoSection(DataConstantsHelper.getDataSourceDescription(DATA_SOURCE_DISCOGS), discogsProfile.getProfile());
		BBCArtistProfile bbcProfile = (BBCArtistProfile)artistProfile.getMinedProfile(DATA_SOURCE_BBC);
		if (bbcProfile != null)
			infoWidget.addInfoSection(DataConstantsHelper.getDataSourceDescription(DATA_SOURCE_BBC), bbcProfile.getWikipediaContent());
	}

}
