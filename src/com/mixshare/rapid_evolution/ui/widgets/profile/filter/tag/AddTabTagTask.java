package com.mixshare.rapid_evolution.ui.widgets.profile.filter.tag;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.tag.SubmittedTag;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.ui.model.profile.filter.tag.TagTabTreeProxyModel;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.TabTreeWidget;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class AddTabTagTask extends CommonTask {

    static private final long serialVersionUID = 0L;    	
    static private Logger log = Logger.getLogger(AddTabTagTask.class);
	
    private TagIdentifier tagId;
    private TagTabTreeView treeView;
    private float degree;
    
    public AddTabTagTask(TagIdentifier tagId, float degree, TagTabTreeView treeView) {
    	this.tagId = tagId;
    	this.treeView = treeView;
    	this.degree = degree;
    }
    
	public String toString() {
		return "Adding Tag=" + tagId + " to=" + ProfileWidgetUI.instance.getCurrentProfile();
	}		
    
	public void execute() {
		try {
    		TagRecord tag = (TagRecord)Database.getRecord(tagId);
    		if (tag == null) {
    			SubmittedTag newTag = new SubmittedTag(tagId.getName());
    			TagProfile tagProfile = (TagProfile)Database.add(newTag);
    			if (tagProfile != null)
    				tag = tagProfile.getTagRecord();
    		}
    		if (tag != null) {
    			SearchProfile searchProfile = (SearchProfile)ProfileWidgetUI.instance.getCurrentProfile();
    			if (log.isTraceEnabled())
    				log.trace("execute(): adding=" + tag.getTagName());
    			searchProfile.addTag(new DegreeValue(tag.getTagName(), degree, DATA_SOURCE_USER), true);
    			if (log.isTraceEnabled())
    				log.trace("execute(): saving profile=" + searchProfile);
    			ProfileWidgetUI.instance.setUpdateImmediately(true);
    			searchProfile.getSearchRecord().update();
    			if (log.isTraceEnabled())
    				log.trace("execute(): invalidating proxy");    			
    			QApplication.invokeAndWait(new UpdateUIThread(tag));
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
				log.trace("addTag(): setting up persistent listeners");
			treeView.setupPersistentEditors();
			treeView.ensureFilterIsVisible(filter);			
		}
	}
	
}
