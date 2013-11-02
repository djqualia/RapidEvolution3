package com.mixshare.rapid_evolution.ui.widgets.profile.search.song;

import java.text.DecimalFormat;
import java.util.Vector;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstantsHelper;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmCommonProfile;
import com.mixshare.rapid_evolution.data.mined.lyricsfly.song.LyricsflySongProfile;
import com.mixshare.rapid_evolution.data.mined.lyricwiki.song.LyricwikiSongProfile;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.song.ReleaseInstance;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.SimilarModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.CompatibleSongsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.SimilarSongsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.SongDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.SongMixoutsModelManager;
import com.mixshare.rapid_evolution.ui.model.search.SearchProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.image.ImageViewer;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.search.song.SongDetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.search.SearchProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabInfoWidget;
import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QWidget;

public class SongProfileDelegate extends SearchProfileDelegate {

	static public DecimalFormat decimalFormat = new DecimalFormat("###.#");
	
	static public int MAX_DESCRIPTION_FIELD_LENGTH = RE3Properties.getInt("profile_title_max_field_display_length");
	
	////////////
	// FIELDS //
	////////////
	
	private SongProfile songProfile;
		
	private SongMixoutsModelManager songMixoutModel;
	private MixoutTableWidget mixoutsWidget;
	private CompatibleSongsModelManager compatibleSongsModel;
	private CompatibleSongTableWidget compatibleWidget;
	
	protected TabInfoWidget lyricsWidget;	
	
	protected Tab mixoutTab;
	
	//////////////////
	// CONSTRUCTORS //
	//////////////////
	
	public SongProfileDelegate(SongProfile songProfile, QTabWidget itemDetailTabsWidget) {
		super(itemDetailTabsWidget, songProfile);
		this.songProfile = songProfile;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public SearchProfile getSearchProfile() { return songProfile; }
	
	public String getTitleText() {
		StringBuffer result = new StringBuffer();
		if (RE3Properties.getBoolean("song_display_format_show_artist")) {
			Vector<ArtistRecord> artists = songProfile.getSongRecord().getArtists();
			int numArtists = 0;
			for (ArtistRecord artist : artists) {
				if (result.length() > 0)
					result.append(" & ");
				ArtistIdentifier artistId = artist.getArtistIdentifier();
				result.append("<a href=\"");
				result.append(artistId.getUniqueId());
				result.append("\">");
				if (artistId.toString().length() > MAX_DESCRIPTION_FIELD_LENGTH)
					result.append(artistId.toString().substring(0, MAX_DESCRIPTION_FIELD_LENGTH) + "...");
				else
					result.append(artistId.toString());
				result.append("</a>");
				++numArtists;
				if (numArtists >= 3)
					break;
			}
		}
		Vector<ReleaseInstance> releases = new Vector<ReleaseInstance>(0);
		if (RE3Properties.getBoolean("song_display_format_show_release")) {
			releases = songProfile.getSongRecord().getReleaseInstances();
			if (releases.size() > 0) {
				ReleaseIdentifier releaseId = releases.get(0).getReleaseIdentifier();
				if (releaseId != null) {
					if (result.length() > 0)
						result.append(" - ");
					result.append("<a href=\"");
					result.append(releaseId.getUniqueId());
					result.append("\">");
					if (releaseId.getReleaseTitle().length() > MAX_DESCRIPTION_FIELD_LENGTH)
						result.append(releaseId.getReleaseTitle().substring(0, MAX_DESCRIPTION_FIELD_LENGTH) + "...");
					else
						result.append(releaseId.getReleaseTitle());
					result.append("</a>");
				}
			}
		}
		String track = "";
		if (RE3Properties.getBoolean("song_display_format_show_track")) {
			track = songProfile.getTrack();
			if (((track == null) || (track.length() == 0)) && (releases.size() > 0))
				track = releases.get(0).getTrack();
			if (track.length() > 0) {
				if (result.length() > 0)
					result.append("  ");
				result.append("[");
				result.append(track);
				result.append("]");
			}			
		}
		if (RE3Properties.getBoolean("song_display_format_show_title")) {
			if (!songProfile.getTitle().equals("")) {
				if (result.length() > 0) {
					if (!track.equals(""))
						result.append("  ");
					else
						result.append(" - ");
				}
				if (songProfile.getTitle().length() > MAX_DESCRIPTION_FIELD_LENGTH)
					result.append(songProfile.getTitle().substring(0, MAX_DESCRIPTION_FIELD_LENGTH) + "...");
				else
					result.append(songProfile.getTitle());				
			}
		}
		if (RE3Properties.getBoolean("song_display_format_show_remix")) {
			if (!songProfile.getRemix().equals("")) {
				if (result.length() > 0)
					result.append(" ");
				result.append("(");
				if (songProfile.getRemix().length() > MAX_DESCRIPTION_FIELD_LENGTH)
					result.append(songProfile.getRemix().substring(0, MAX_DESCRIPTION_FIELD_LENGTH) + "...");
				else
					result.append(songProfile.getRemix());					
				result.append(")");
			}
		}
		if ((songProfile.getStartKey().isValid() && RE3Properties.getBoolean("song_display_format_show_key")) || (songProfile.getBpmStart().isValid() && RE3Properties.getBoolean("song_display_format_show_bpm")) || (songProfile.getDuration().isValid() && RE3Properties.getBoolean("song_display_format_show_duration"))) {
			result.append("   [");
			StringBuffer subResult = new StringBuffer();
			if (RE3Properties.getBoolean("song_display_format_show_bpm")) {
				if (songProfile.getBpmStart().isValid()) {
					float bpm = ProfileWidgetUI.instance.getStageWidget().getCurrentBpm().getBpmValue();
					boolean color = false;
					if (bpm == 0.0f)
						bpm = songProfile.getStartBpm();
					else if (bpm != songProfile.getStartBpm())
						color = true;
					if (color)
						subResult.append("<font color='red'>");											
					subResult.append(decimalFormat.format(bpm));
					if (color)
						subResult.append("</font>");
					subResult.append("bpm");
				}
			}
			if (RE3Properties.getBoolean("song_display_format_show_key")) {
				if (songProfile.getStartKey().isValid()) {
					if (subResult.length() > 0)
						subResult.append(", ");
					Key currentKey = ProfileWidgetUI.instance.getStageWidget().getCurrentKey();
					Key songKey = songProfile.getEndKey();
					if (!songKey.isValid())
						songKey = songProfile.getStartKey();
					boolean color = false;
					if (!currentKey.isValid())
						currentKey = songKey;
					else if (!currentKey.equals(songKey))
						color = true;
					if (color)
						subResult.append("<font color='red'>");						
					subResult.append(currentKey);
					if (color)
						subResult.append("</font>");
				}
			}
			if (RE3Properties.getBoolean("song_display_format_show_duration")) {
				if (songProfile.getDuration().isValid()) {
					if (subResult.length() > 0)
						subResult.append(", ");
					subResult.append(songProfile.getDuration());
				}
			}
			result.append(subResult);
			result.append("]");
		}
		return result.toString();
	}
	
	public DetailsWidgetUI getDetailsWidget() {
		SongDetailsModelManager songDetailsModel = (SongDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(SongDetailsModelManager.class);
		songDetailsModel.setRelativeProfile(songProfile);
		return new SongDetailsWidgetUI(songDetailsModel);
	}	
	
	public SimilarModelManagerInterface getSimilarModelInterface() {
		SimilarSongsModelManager similarSongsModel = (SimilarSongsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(SimilarSongsModelManager.class);
		similarSongsModel.setRelativeSong(songProfile);
		return similarSongsModel;
	}
	
	public RecordTabTableWidget getSimilarTableWidget(RecordTableModelManager tableModelManager, Column sortColumn) {
		return new SongSimilarTableWidget(tableModelManager, sortColumn);
	}	
	
	public Vector<Tab> getTabs() {
		Vector<Tab> result = super.getTabs();
		
		// mixouts tab
		songMixoutModel = (SongMixoutsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(SongMixoutsModelManager.class);
		songMixoutModel.setRelativeSong(songProfile);
		mixoutsWidget = new MixoutTableWidget(songMixoutModel, COLUMN_MIXOUT_RATING_STARS);
		String label = Translations.get("tab_title_mixouts");
		if (songProfile.getMixouts().size() > 0)
			label += " (" + songProfile.getMixouts().size() + ")";
		mixoutTab = new Tab(label, mixoutsWidget);
		result.add(mixoutTab);				
		
		// compatible tab
		if (songProfile.getStartKey().isValid() || songProfile.getEndKey().isValid()) {
			if (songProfile.getBpmStart().isValid() || songProfile.getBpmEnd().isValid()) {
				compatibleSongsModel = (CompatibleSongsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(CompatibleSongsModelManager.class);
				compatibleSongsModel.setRelativeSong(songProfile);
				compatibleWidget = new CompatibleSongTableWidget(compatibleSongsModel, null);
				result.add(new Tab(Translations.get("tab_title_compatible"), compatibleWidget));				
			}
		}
		
		// lyrics
		lyricsWidget = new TabInfoWidget();
		addLyricsSections();
		if (lyricsWidget.hasContent())
			result.add(new Tab(Translations.get("tab_title_lyrics"), lyricsWidget));		
		
		return result;
	}

	public String getTabIndexTitle() {
		return UIProperties.getProperty("song_tab_index");
	}
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setTabIndexTitle(String tabTitle) {
		UIProperties.setProperty("song_tab_index", tabTitle);
	}
	
	/////////////
	// METHODS //
	/////////////
		
	public void refresh() {
		super.refresh();
		
		if (mixoutsWidget.isLoaded()) {
			songMixoutModel.reset();
		}
		String label = Translations.get("tab_title_mixouts");
		if (songProfile.getMixouts().size() > 0)
			label += " (" + songProfile.getMixouts().size() + ")";
		mixoutTab.setName(label);
		itemDetailTabsWidget.setTabText(itemDetailTabsWidget.indexOf(mixoutTab.getContent()), label);
		
		if (compatibleWidget == null) {
			if ((songProfile.getStartKey().isValid() || songProfile.getEndKey().isValid()) && (songProfile.getBpmStart().isValid() || songProfile.getBpmEnd().isValid())) {
				compatibleSongsModel = (CompatibleSongsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(CompatibleSongsModelManager.class);
				compatibleSongsModel.setRelativeSong(songProfile);
				compatibleWidget = new CompatibleSongTableWidget(compatibleSongsModel, null);
				itemDetailTabsWidget.addTab(compatibleWidget, Translations.get("tab_title_compatible"));								
			}
		} else {
			if ((compatibleWidget != null) && compatibleWidget.isLoaded())
				compatibleSongsModel.reset();
		}
		
		// lyrics tab
		boolean lyricsVisible = lyricsWidget.hasContent();
		addLyricsSections();
		if (lyricsWidget.hasContent() && !lyricsVisible)
			itemDetailTabsWidget.addTab(lyricsWidget, Translations.get("tab_title_lyrics"));
		
	}
	
	public void unload() {
		super.unload();
	}
	
	protected void addInfoSections() {
		LastfmCommonProfile lastfmProfile = (LastfmCommonProfile)songProfile.getMinedProfile(DATA_SOURCE_LASTFM);
		if (lastfmProfile != null)
			infoWidget.addInfoSection(DataConstantsHelper.getDataSourceDescription(DATA_SOURCE_LASTFM), lastfmProfile.getWikiText());
	}
	
	protected void addLyricsSections() { 
		LyricsflySongProfile lyricsflyProfile = (LyricsflySongProfile)songProfile.getMinedProfile(DATA_SOURCE_LYRICSFLY);
		if (lyricsflyProfile != null)
			lyricsWidget.addInfoSection(DataConstantsHelper.getDataSourceDescription(DATA_SOURCE_LYRICSFLY), lyricsflyProfile.getLyricsText());
		LyricwikiSongProfile lyricwikiProfile = (LyricwikiSongProfile)songProfile.getMinedProfile(DATA_SOURCE_LYRICWIKI);
		if (lyricwikiProfile != null)
			lyricsWidget.addInfoSection(DataConstantsHelper.getDataSourceDescription(DATA_SOURCE_LYRICWIKI), lyricwikiProfile.getLyricsText());		
	}
	
	public QWidget createImageViewerWidget(QWidget parent, Profile profile) {
		SearchProfile searchProfile = (SearchProfile)profile;
		imageViewer = new ImageViewer(parent, searchProfile);
		if (getSearchProfile().getImages().size() > 0)
			imageViewer.setImages(getSearchProfile().getImages());
		else {
			String bestThumbnail = ((SongProfile)profile).getSongRecord().getBestRelatedThumbnailImageFilename();
			if ((bestThumbnail != null) && (bestThumbnail.length() > 0)) {
				try {
					Vector<Image> images = new Vector<Image>(1);
					images.add(new Image(bestThumbnail, bestThumbnail, DATA_SOURCE_UNKNOWN));
					imageViewer.setImages(images);
				} catch (InvalidImageException ie) { }
			}			
		}
		return imageViewer;
	}
	
	public void updateRelativeSongWidgets() {
		updateRelativeSongModel(compatibleWidget);		
		updateRelativeSongModel(similarWidget);
		updateRelativeSongModel(mixoutsWidget);
	}
	
	private void updateRelativeSongModel(RecordTabTableWidget widget) {
		if (widget != null) {
			if (!((RecordTableModelManager)widget.getModelManager()).isLazySearchSupported() || !SearchProxyModel.EMPTY_INITIAL_RESULTS_MODE) {
				if (widget.getModelManager().isColumnVisible(COLUMN_BPM_SHIFT))
					((RecordTableModelManager)widget.getModelManager()).refreshColumn(COLUMN_BPM_SHIFT);
				if (widget.getModelManager().isColumnVisible(COLUMN_BPM_DIFFERENCE))
					((RecordTableModelManager)widget.getModelManager()).refreshColumn(COLUMN_BPM_DIFFERENCE); // this won't change all the time but we'll do it any way just in case
				if (widget.getModelManager().isColumnVisible(COLUMN_ACTUAL_KEY))
					((RecordTableModelManager)widget.getModelManager()).refreshColumn(COLUMN_ACTUAL_KEY);
				if (widget.getModelManager().isColumnVisible(COLUMN_ACTUAL_KEYCODE))
					((RecordTableModelManager)widget.getModelManager()).refreshColumn(COLUMN_ACTUAL_KEYCODE);
				if (widget.getModelManager().isColumnVisible(COLUMN_KEY_RELATION))
					((RecordTableModelManager)widget.getModelManager()).refreshColumn(COLUMN_KEY_RELATION);
				if (widget.getModelManager().isColumnVisible(COLUMN_KEY_LOCK))
					((RecordTableModelManager)widget.getModelManager()).refreshColumn(COLUMN_KEY_LOCK);
				if (widget.getModelManager().isColumnVisible(COLUMN_KEY_CLOSENESS))
					((RecordTableModelManager)widget.getModelManager()).refreshColumn(COLUMN_KEY_CLOSENESS);
				if (widget.getModelManager().isColumnVisible(COLUMN_PITCH_SHIFT))
					((RecordTableModelManager)widget.getModelManager()).refreshColumn(COLUMN_PITCH_SHIFT);				
			}
			widget.updateFilter();
		}
	}
	
	public void updateTitle() {
		titleLabel.setText(getTitleText());
	}
	
}
