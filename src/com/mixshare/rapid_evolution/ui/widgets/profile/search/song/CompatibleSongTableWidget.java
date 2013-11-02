package com.mixshare.rapid_evolution.ui.widgets.profile.search.song;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.CompatibleSongsModel;
import com.mixshare.rapid_evolution.ui.model.profile.tab.RecordTabTableProxyModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.model.table.TableModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.table.SortTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableView;
import com.mixshare.rapid_evolution.ui.widgets.profile.tab.RecordTabTableWidget;

public class CompatibleSongTableWidget extends RecordTabTableWidget {

	static private Logger log = Logger.getLogger(CompatibleSongTableWidget.class);    			
	
	static public CompatibleSongTableWidget instance = null;
	
	static public byte COMPATIBLE_TYPE_ALL_HARMONIC = 0;
	static public byte COMPATIBLE_TYPE_KEYLOCK_ONLY = 1;
	static public byte COMPATIBLE_TYPE_NO_KEYLOCK = 2;
	static public byte COMPATIBLE_TYPE_BPM_ONLY = 3;
	
	private CompatibleSongsModel model;
	private CompatibleSongProxyModel proxyModel;
	
	public CompatibleSongTableWidget(RecordTableModelManager modelManager, Column defaultSortColumn) {
		super(modelManager, defaultSortColumn);
		this.model = (CompatibleSongsModel)modelManager.getModelPopulator();
		instance = this;
	}
	
	public RecordTabTableProxyModel getTabTableProxyModel(RecordTabTableWidget tabTableWidget, RecordTableModelManager tableManager, RecordTabTableView tabTableView) {	
		proxyModel = new CompatibleSongProxyModel(this, modelManager, (RecordTabTableView)getView(), this);
		return proxyModel;
	}
	
    protected SortTableView createTableView(TableModelManager modelManager) {
    	SubSongTabTableView result = new CompatibleSongView((RecordTableModelManager)modelManager);
    	result.setItemDelegate(new HarmonicColoringItemDelegate(this, modelManager));
    	return result;
    }		
	
    public String[] getShowTypes() { return new String[] { Translations.get("compatible_filter_all_harmonic"), Translations.get("compatible_filter_keylock_only"), Translations.get("compatible_filter_no_keylock"), Translations.get("compatible_filter_bpm_only") }; }
    public byte getShowType() { 
		if (UIProperties.hasProperty("compatible_songs_show_type"))
			return UIProperties.getByte("compatible_songs_show_type");
		return (byte)0;    	
    }

    public void setShowType() {
		if (searchBarWidget.getFilterCombo().currentIndex() == 0)
			model.getSearchParameters().setCompatibleMatchType(COMPATIBLE_TYPE_ALL_HARMONIC);
		else if (searchBarWidget.getFilterCombo().currentIndex() == 1)
			model.getSearchParameters().setCompatibleMatchType(COMPATIBLE_TYPE_KEYLOCK_ONLY);
		else if (searchBarWidget.getFilterCombo().currentIndex() == 2)
			model.getSearchParameters().setCompatibleMatchType(COMPATIBLE_TYPE_NO_KEYLOCK);
		else if (searchBarWidget.getFilterCombo().currentIndex() == 3)
			model.getSearchParameters().setCompatibleMatchType(COMPATIBLE_TYPE_BPM_ONLY);
		UIProperties.setProperty("compatible_songs_show_type", String.valueOf(searchBarWidget.getFilterCombo().currentIndex()));
    	updateFilter();
    }
	
    public void updateFilter() {
    	if (log.isTraceEnabled())
    		log.trace("updateFilter(): invalidating...");
    	if (searchBarWidget != null) {
	    	model.getSearchParameters().setSearchText(searchBarWidget.getFilterText().text());
	    	if (model.computeCompatibleRecords()) {
	    		modelManager.refresh();
	    		proxyModel.invalidate();
	    	}
    	}
    }
    
}
