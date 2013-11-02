package com.mixshare.rapid_evolution.ui.widgets.profile.tab;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.ui.widgets.common.LazyLoadWidget;
import com.mixshare.rapid_evolution.ui.widgets.common.LazyLoaderInterface;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QDesktopServices;
import com.trolltech.qt.gui.QTextBrowser;
import com.trolltech.qt.gui.QToolBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class TabInfoWidget extends LazyLoadWidget implements DataConstants, LazyLoaderInterface {
	
	static private Logger log = Logger.getLogger(TabInfoWidget.class);
    		
    ////////////
    // FIELDS //
    ////////////
    
	private	QToolBox infoToolbox = new QToolBox();
	private boolean hasContent = false;
	private Vector<String> sections = new Vector<String>();   
	private Vector<QTextBrowser> textEditSections = new Vector<QTextBrowser>();
	
    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public TabInfoWidget() {
    	this.lazyLoader = this;    	
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public boolean hasContent() { return hasContent; }
    
    /////////////
    // METHODS //
    /////////////
    
    public void populateWidget(QWidget widget) {
    	if (log.isTraceEnabled())
    		log.trace("populateWidget(): populating...");
    	
    	QVBoxLayout mainLayout = new QVBoxLayout(this);
        mainLayout.addWidget(infoToolbox);
        setLayout(mainLayout);
    }
    
    public void addInfoSection(String sourceTitle, String text) {
    	if ((text != null) && (text.length() > 0)) {
    		boolean found = false;
    		int i = 0;
    		while ((i < sections.size()) && !found) {
    			String sectionTitle = sections.get(i);
    			if (sectionTitle.equals(sourceTitle))
    				found = true;
    			else
    				++i;
    		}
    		String processedText = StringUtil.fixSpecialXMLCharacters(text);
    		processedText = StringUtil.replace(processedText, "\n", "<br/>");
    		if (log.isDebugEnabled())
    			log.debug("addInfoSection(): sourceTitle=" + sourceTitle + ", processedText=" + processedText);
    		if (!found) {
    			QTextBrowser textEdit = new QTextBrowser(infoToolbox);    			
    			textEdit.setHtml(processedText);
    			textEdit.setReadOnly(true);
    			textEdit.anchorClicked.connect(this, "anchorClicked(QUrl)");
    			textEdit.setOpenExternalLinks(false);
    			textEdit.setOpenLinks(false);
    			infoToolbox.addItem(textEdit, sourceTitle);    			
    			sections.add(sourceTitle);
    			textEditSections.add(textEdit);
    		} else {
    			textEditSections.get(i).setHtml(processedText);
    		}
    		hasContent = true;
    	}
    }
    
    private void anchorClicked(QUrl value) {
    	if (log.isDebugEnabled())
    		log.debug("anchorClicked(): value=" + value);
    	QDesktopServices.openUrl(value);
    }
    
}
