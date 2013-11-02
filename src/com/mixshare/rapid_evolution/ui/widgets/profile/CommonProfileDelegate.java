package com.mixshare.rapid_evolution.ui.widgets.profile;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.Identifier;
import com.mixshare.rapid_evolution.data.identifier.IdentifierParser;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.util.Tab;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.image.ImageViewer;
import com.mixshare.rapid_evolution.ui.widgets.common.label.DraggableLabel;
import com.mixshare.rapid_evolution.ui.widgets.profile.details.DetailsWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabInfoWidget;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QWidget;

abstract public class CommonProfileDelegate extends AbstractCommonProfileDelegate implements AllColumns, DataConstants {

	static private Logger log = Logger.getLogger(CommonProfileDelegate.class);
	
	////////////
	// FIELDS //
	////////////
	
	private QWidget titleWidget;
	protected QLabel titleLabel;
	protected ImageViewer imageViewer;
	protected QTabWidget itemDetailTabsWidget;
	
	// common tabs
	protected TabInfoWidget infoWidget;
	protected DetailsWidgetUI detailsWidget;
	
	transient private Vector<Tab> cachedTabs;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public CommonProfileDelegate(QTabWidget itemDetailTabsWidget) {
		this.itemDetailTabsWidget = itemDetailTabsWidget;
	}
	
	//////////////////////
	// ABSTRACT METHODS //
	//////////////////////
	
	abstract public String getTitleText();
		
	abstract public DetailsWidgetUI getDetailsWidget();
	
	abstract public QWidget createImageViewerWidget(QWidget parent, Profile profile);
	
	/////////////
	// GETTERS //
	/////////////
		
	public QWidget getTitleWidget() {
		if (titleWidget == null) {
			titleWidget = new QWidget();
			QHBoxLayout titleLayout  = new QHBoxLayout(titleWidget);
			//titleLayout.setMargin(0);
			titleLabel = new DraggableLabel();
			titleLabel.linkActivated.connect(this, "titleClicked(String)");
			titleLabel.setWordWrap(true);
			titleLabel.setAlignment(Qt.AlignmentFlag.AlignCenter);
			String text = getTitleText();
			if (log.isTraceEnabled())
				log.trace("getTitleWidget(): text=" + text);
			titleLabel.setText(text);			
			titleLayout.addWidget(titleLabel);
						
			QFont font = new QFont();
			font.setPointSize(RE3Properties.getInt("profile_title_font_point_size"));
			font.setBold(true);			
			titleLabel.setFont(font);
			
	    	QSizePolicy titleWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Minimum, com.trolltech.qt.gui.QSizePolicy.Policy.Minimum);
	    	titleWidgetSizePolicy.setHorizontalStretch((byte)0);
	    	titleWidgetSizePolicy.setVerticalStretch((byte)0);
	    	titleWidget.setSizePolicy(titleWidgetSizePolicy);	    				
		}
		return titleWidget;
	}
	
	public QWidget getImageViewerWidget() { return imageViewer; }
	
	public void refresh() {
		
		titleLabel.setText(getTitleText());
		
		// details
		if (detailsWidget.isLoaded())
			detailsWidget.getModelManager().refresh();
		
	}
	
	public void unload() {
		
	}
	
	public Vector<Tab> getTabsCached() {
		if (cachedTabs == null)
			cachedTabs = getTabs();
		return cachedTabs;
	}
	
	public Vector<Tab> getTabs() {
		Vector<Tab> result = new Vector<Tab>();
		
		// details table
		detailsWidget = getDetailsWidget();
		result.add(new Tab(Translations.get("tab_title_details"), detailsWidget));					
		
		return result;
	}	
	
	/////////////
	// METHODS //
	/////////////
	
	private void titleClicked(String link) {
		log.debug("titleClicked(): link=" + link);
		Identifier id = IdentifierParser.getIdentifier(link);
		if (id != null) {
	    	Profile profile = Database.getProfile(id);
	    	if (profile != null) 	    	
	    		ProfileWidgetUI.instance.showProfile(profile);	    				
		}
	}
}
