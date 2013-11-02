package com.mixshare.rapid_evolution.ui.widgets.profile.filter.tag;

import java.util.Vector;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagArtistModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagArtistTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagDetailsModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagLabelModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagLabelTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagReleaseModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagReleaseTabTableWidget;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagSongModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagSongTabTableWidget;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.filter.tag.TagDetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.FilterProfileDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;
import com.trolltech.qt.gui.QTabWidget;

public class TagProfileDelegate extends FilterProfileDelegate {

	////////////
	// FIELDS //
	////////////
	
	private TagProfile tagProfile;
	
	private TagSongModelManager tagSongsModel;
	private RecordTabTableWidget songsWidget;

	private TagArtistModelManager tagArtistsModel;
	private RecordTabTableWidget artistsWidget;

	private TagLabelModelManager tagLabelsModel;
	private RecordTabTableWidget labelsWidget;

	private TagReleaseModelManager tagReleasesModel;
	private RecordTabTableWidget releasesWidget;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public TagProfileDelegate(QTabWidget itemDetailTabsWidget, TagProfile tagProfile) {
		super(itemDetailTabsWidget);
		this.tagProfile = tagProfile;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public String getTitleText() {
		return tagProfile.getTagName();
	}
		
	public DetailsWidgetUI getDetailsWidget() {
		TagDetailsModelManager tagDetailsModel = (TagDetailsModelManager)Database.getRelativeModelFactory().getRelativeModelManager(TagDetailsModelManager.class);
		tagDetailsModel.setRelativeProfile(tagProfile);
		return new TagDetailsWidgetUI(tagDetailsModel);		
	}

	
	public String getTabIndexTitle() {
		return UIProperties.getProperty("tag_tab_index");
	}	
	
	public Vector<Tab> getTabs() {
		Vector<Tab> result = super.getTabs();		

		// artists
		tagArtistsModel = (TagArtistModelManager)Database.getRelativeModelFactory().getRelativeModelManager(TagArtistModelManager.class);
		tagArtistsModel.setRelativeTag(tagProfile);
		artistsWidget = new TagArtistTabTableWidget(tagArtistsModel, COLUMN_DEGREE);
		result.add(new Tab(Translations.get("tab_title_artists"), artistsWidget));		

		// labels
		tagLabelsModel = (TagLabelModelManager)Database.getRelativeModelFactory().getRelativeModelManager(TagLabelModelManager.class);
		tagLabelsModel.setRelativeTag(tagProfile);
		labelsWidget = new TagLabelTabTableWidget(tagLabelsModel, COLUMN_DEGREE);
		result.add(new Tab(Translations.get("tab_title_labels"), labelsWidget));		
		
		// releases
		tagReleasesModel = (TagReleaseModelManager)Database.getRelativeModelFactory().getRelativeModelManager(TagReleaseModelManager.class);
		tagReleasesModel.setRelativeTag(tagProfile);
		releasesWidget = new TagReleaseTabTableWidget(tagReleasesModel, COLUMN_DEGREE);
		result.add(new Tab(Translations.get("tab_title_releases"), releasesWidget));		
		
		// songs
		tagSongsModel = (TagSongModelManager)Database.getRelativeModelFactory().getRelativeModelManager(TagSongModelManager.class);
		tagSongsModel.setRelativeTag(tagProfile);
		songsWidget = new TagSongTabTableWidget(tagSongsModel, COLUMN_DEGREE);
		result.add(new Tab(Translations.get("tab_title_songs"), songsWidget));		
		
		return result;
	}	
	
	/////////////
	// SETTERS //
	/////////////
	
	public void setTabIndexTitle(String tabTitle) {
		UIProperties.setProperty("tag_tab_index", tabTitle);
	}

	/////////////
	// METHODS //
	/////////////

	public void refresh() {
		super.refresh();	
		
		// songs
		if (songsWidget.isLoaded()) {
			tagSongsModel.reset();
			songsWidget.updateFilter();
		}

		// artists
		if (artistsWidget.isLoaded()) {
			tagArtistsModel.reset();
			artistsWidget.updateFilter();
		}

		// labels
		if (labelsWidget.isLoaded()) {
			tagLabelsModel.reset();
			labelsWidget.updateFilter();
		}

		// releases
		if (releasesWidget.isLoaded()) {
			tagReleasesModel.reset();
			releasesWidget.updateFilter();
		}
		
		
	}
	
}
