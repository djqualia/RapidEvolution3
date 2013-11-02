package com.mixshare.rapid_evolution.ui.widgets.profile.search;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.ProfileStyleModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.ProfileTagModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.LinkModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.SimilarModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.profile.search.VideoLinkModelManager;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.style.StyleTabTreeWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.filter.tag.TagTabTreeWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeWidget;
import com.trolltech.qt.gui.QTabWidget;

abstract public class SearchProfileDelegate extends AbstractSearchProfileDelegate {

	static private Logger log = Logger.getLogger(SearchProfileDelegate.class);
	
	////////////
	// FIELDS //
	////////////
	
	protected SearchProfile searchProfile;
	private TabTreeWidget stylesWidget;
	private TabTreeWidget tagsWidget;	
	protected RecordTabTableWidget similarWidget;
	private LinkTableWidget linksWidget;
	private LinkModelManager linksModel;
	private LinkTableWidget videolinksWidget;
	private VideoLinkModelManager videolinksModel;
	private SimilarModelManagerInterface similarInterface;
		
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public SearchProfileDelegate(QTabWidget itemDetailTabsWidget, SearchProfile searchProfile) {
		super(itemDetailTabsWidget);
		this.searchProfile = searchProfile;
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public SimilarModelManagerInterface getSimilarModelInterface();
	abstract public RecordTabTableWidget getSimilarTableWidget(RecordTableModelManager tableModelManager, Column sortColumn);
	
	/////////////
	// GETTERS //
	/////////////
	
	public Vector<Tab> getTabs() {
		Vector<Tab> result = super.getTabs();		
		
		// links
		linksModel = (LinkModelManager)Database.getRelativeModelFactory().getRelativeModelManager(LinkModelManager.class);
		linksModel.setLinks(searchProfile.getLinks());
		linksWidget = new LinkTableWidget(linksModel, COLUMN_LINK_TITLE);
		result.add(new Tab(Translations.get("tab_title_links"), linksWidget));		

		// video links
		videolinksModel = (VideoLinkModelManager)Database.getRelativeModelFactory().getRelativeModelManager(VideoLinkModelManager.class);
		videolinksModel.setVideoLinks(searchProfile.getVideoLinks());
		videolinksWidget = new LinkTableWidget(videolinksModel, COLUMN_LINK_TITLE);
		if (searchProfile.getVideoLinks().size() > 0)
			result.add(new Tab(Translations.get("tab_title_videos"), videolinksWidget));				
		
		// styles tab
		ProfileStyleModelManager stylesModel = (ProfileStyleModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ProfileStyleModelManager.class);
		stylesModel.setRelativeProfile(searchProfile);		
		stylesWidget = StyleTabTreeWidgetUI.instance;
		if (stylesWidget == null)
			stylesWidget = new StyleTabTreeWidgetUI(stylesModel, COLUMN_DEGREE);
		stylesWidget.init(searchProfile);
		result.add(new Tab(Translations.get("tab_title_styles"), stylesWidget));
		stylesModel.setSourceColumnSizes(stylesWidget.getTreeView());
		
		// tags tab
		ProfileTagModelManager tagsModel = (ProfileTagModelManager)Database.getRelativeModelFactory().getRelativeModelManager(ProfileTagModelManager.class);
		tagsModel.setRelativeProfile(searchProfile);		
		tagsWidget = TagTabTreeWidgetUI.instance;
		if (tagsWidget == null)
			tagsWidget = new TagTabTreeWidgetUI(tagsModel, COLUMN_DEGREE);
		tagsWidget.init(searchProfile);		
		result.add(new Tab(Translations.get("tab_title_tags"), tagsWidget));
		tagsModel.setSourceColumnSizes(tagsWidget.getTreeView());
		
		// similar
		similarInterface = getSimilarModelInterface();
		if (similarInterface != null) {
			similarWidget = getSimilarTableWidget(similarInterface.getTableModelManager(), null);
			if (!searchProfile.isExternalItem())
				result.add(new Tab(Translations.get("tab_title_similar"), similarWidget));
		}
	
		return result;
	}
	
	/////////////
	// METHODS //
	/////////////
	
	public void refresh() {
		super.refresh();
		
		if (log.isDebugEnabled())
			log.debug("refresh(): profile=" + getSearchProfile());
		
		// styles
		if (stylesWidget.isLoaded()) {
			stylesWidget.getModelManager().refresh();
			stylesWidget.getModelManager().getProxyModel().invalidate();
			stylesWidget.getTreeView().setupPersistentEditors();
			stylesWidget.getModelManager().setSourceColumnSizes(stylesWidget.getTreeView());			
		}
		
		// tags
		if (tagsWidget.isLoaded()) {
			tagsWidget.getModelManager().refresh();
			tagsWidget.getModelManager().getProxyModel().invalidate();
			tagsWidget.getTreeView().setupPersistentEditors();
			tagsWidget.getModelManager().setSourceColumnSizes(tagsWidget.getTreeView());
		}
		
		// similar
		if ((similarWidget != null) && (similarWidget.isLoaded()))
			similarInterface.reset();
		
		// links
		linksModel.setLinks(searchProfile.getLinks());
		if (linksWidget.isLoaded())
			linksModel.reset();
		
		// videos
		boolean visible = videolinksModel.getSize() > 0;
		videolinksModel.setVideoLinks(searchProfile.getVideoLinks());		
		if (videolinksWidget.isLoaded())
			videolinksModel.reset();
		else if (!visible && (searchProfile.getVideoLinks().size() > 0))
			itemDetailTabsWidget.insertTab(3, videolinksWidget, Translations.get("tab_title_videos"));
	}
	
	public void unload() {
		super.unload();
		if ((stylesWidget != null) && (stylesWidget.isLoaded()))
			stylesWidget.unload();
		if ((tagsWidget != null) && (tagsWidget.isLoaded()))
			tagsWidget.unload();
	}
	
}
