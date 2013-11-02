package com.mixshare.rapid_evolution.ui.widgets.profile.filter.style;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.style.SubmittedStyle;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class AddTabStyleTask extends CommonTask {

    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(AddTabStyleTask.class);
	
    private StyleIdentifier styleId;
    private float degree;
    private StyleTabTreeView treeView;    
    
    public AddTabStyleTask(StyleIdentifier styleId, float degree, StyleTabTreeView treeView) {
    	this.styleId = styleId;
    	this.degree = degree;
    	this.treeView = treeView;
    }
    
	public String toString() {
		return "Adding Style=" + styleId + " to=" + ProfileWidgetUI.instance.getCurrentProfile();
	}		
    
	public void execute() {
		try {
    		StyleRecord style = (StyleRecord)Database.getRecord(styleId);
    		if (style == null) {
    			SubmittedStyle newStyle = new SubmittedStyle(styleId.getName());
    			StyleProfile styleProfile = (StyleProfile)Database.add(newStyle);
    			if (styleProfile != null)
    				style = styleProfile.getStyleRecord();
    		}
    		if (style != null) {
    			SearchProfile searchProfile = (SearchProfile)ProfileWidgetUI.instance.getCurrentProfile();
    			if (log.isTraceEnabled())
    				log.trace("addStyle(): adding=" + style.getStyleName());
    			searchProfile.addStyle(new DegreeValue(style.getStyleName(), degree, DATA_SOURCE_USER));
    			if (log.isTraceEnabled())
    				log.trace("addStyle(): saving profile=" + searchProfile);
    			ProfileWidgetUI.instance.setUpdateImmediately(true);
    			searchProfile.getSearchRecord().update();
    			if (log.isTraceEnabled())
    				log.trace("addStyle(): invalidating proxy");    			
    			QApplication.invokeAndWait(new UpdateUIThread(style));
				if (searchProfile.equals(ProfileWidgetUI.instance.getCurrentProfile()) && (searchProfile instanceof SongProfile))
					QApplication.invokeLater(new Thread() { public void run() { ProfileWidgetUI.instance.stageChanged(); } });								
    		}			
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}
	
	private class UpdateUIThread extends Thread {
		FilterRecord filter;
		public UpdateUIThread(FilterRecord filter) {
			this.filter = filter;
		}
		public void run() {
			treeView.getProxyModel().invalidate();
			if (log.isTraceEnabled())
				log.trace("addStyle(): setting up persistent listeners");
			treeView.setupPersistentEditors();
			treeView.ensureFilterIsVisible(filter);
		}
	}
	
}
