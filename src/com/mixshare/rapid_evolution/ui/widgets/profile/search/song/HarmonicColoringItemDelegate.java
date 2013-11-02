package com.mixshare.rapid_evolution.ui.widgets.profile.search.song;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.music.key.KeyRelation;
import com.mixshare.rapid_evolution.music.key.SongKeyRelation;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Color;
import com.mixshare.rapid_evolution.ui.widgets.CentralWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.common.ItemDelegate;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QStyle;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QWidget;

public class HarmonicColoringItemDelegate extends ItemDelegate {

	static private Logger log = Logger.getLogger(HarmonicColoringItemDelegate.class);
	
	static private Color harmonicMatchColor = RE3Properties.getColor("harmonic_match_color");
	static private Color mixoutColor = RE3Properties.getColor("mixout_color");
	
	private QWidget parent;
	
    public HarmonicColoringItemDelegate(QWidget parent, ModelManagerInterface modelManager) {
    	super(parent, modelManager);
    	this.parent = parent;
    }

    @Override
    public void paint(QPainter painter, QStyleOptionViewItem option, QModelIndex index) {
    	//if (log.isTraceEnabled())
    		//log.trace("paint(): index=" + index + ", option.palette().highlight()=" + option.palette().highlight());
    	Object data = index.data();
    	QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(index);
		SongRecord songRecord = (SongRecord)((RecordTableModelManager)modelManager).getRecordForRow(sourceIndex.row());
		if (ProfileWidgetUI.instance.getCurrentProfile() instanceof SongProfile) {
			SongProfile relativeProfile = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
			QStyle.State state = option.state();
	    	if (RE3Properties.getBoolean("highlight_broken_file_links")) {
	    		if (modelManager instanceof RecordTableModelManager) {
	    			SearchRecord searchRecord = (SearchRecord)((RecordTableModelManager)modelManager).getRecordForRow(sourceIndex.row());
	    			if (searchRecord instanceof SongRecord) {
	    				SongRecord song = (SongRecord)searchRecord;
	    				if (!song.isExternalItem() && !song.hasValidSongFilename()) {
	    					if (!state.isSet(QStyle.StateFlag.State_Selected)) {
			    				state.set(QStyle.StateFlag.State_Selected);
			    				option.setState(state);					
			    				QPalette p = option.palette();
			    				p.setColor(QPalette.ColorRole.Highlight, BROKEN_FILELINKS_COLOR.getQColor());
			    				option.setPalette(p);
	    					}
	    				}    				
	    			}
	    		}
	    	}
	    	if (relativeProfile != null) {
				boolean isMixout = false;
				float mixoutRating = 0.0f;
				for (MixoutRecord mixout : relativeProfile.getMixouts()) {
					if (mixout.getToSongUniqueId() == songRecord.getUniqueId()) {
						isMixout = true;
						mixoutRating = mixout.getRatingValue().getRatingNormalized();
						break;
					}
				}
				if (isMixout && !(parent instanceof MixoutTableWidget)) {
					if (!option.state().isSet(QStyle.StateFlag.State_Selected)) {
						mixoutRating = 0.75f * mixoutRating + 0.25f;
						Color baseColor = new Color(option.palette().color(QPalette.ColorRole.Base));        	
						painter.fillRect(option.rect(), mixoutColor.getLinearGradient(baseColor, mixoutRating).getQColor());
					}			
				} else {		
					// target bpm/key/time sig
					Bpm targetBpm = ProfileWidgetUI.instance.getStageWidget().getCurrentBpm();
					if (!targetBpm.isValid())
						targetBpm = relativeProfile.getBpmEnd();
					if (!targetBpm.isValid())
						targetBpm = relativeProfile.getBpmStart();
					Key targetKey = ProfileWidgetUI.instance.getStageWidget().getCurrentKey();
					if (!targetKey.isValid())
						targetKey = relativeProfile.getEndKey();
					if (!targetKey.isValid())
						targetKey = relativeProfile.getStartKey();
									
					if (parent instanceof CompatibleSongTableWidget) {
						CompatibleSongTableWidget compatibleWidget = (CompatibleSongTableWidget)parent;
						if ((compatibleWidget.getShowType() == CompatibleSongTableWidget.COMPATIBLE_TYPE_ALL_HARMONIC) || (compatibleWidget.getShowType() == CompatibleSongTableWidget.COMPATIBLE_TYPE_BPM_ONLY)) {
							SongKeyRelation keyRelation = SongKeyRelation.getSongKeyRelation(songRecord, targetBpm, targetKey);
							if (keyRelation.isCompatible()) {
								float percentInKey = (0.5f - Math.abs(keyRelation.getBestKeyRelation().getDifference())) * 2.0f;
								if (log.isTraceEnabled())
									log.trace("paint(): percentInKey=" + percentInKey); 
								if (!option.state().isSet(QStyle.StateFlag.State_Selected)) {
									Color baseColor = new Color(option.palette().color(QPalette.ColorRole.Base));        	
									painter.fillRect(option.rect(), harmonicMatchColor.getLinearGradient(baseColor, percentInKey).getQColor());
								}
							}
						} else if (compatibleWidget.getShowType() == CompatibleSongTableWidget.COMPATIBLE_TYPE_KEYLOCK_ONLY) {
							KeyRelation keyRelation = Key.getClosestKeyRelation(targetBpm.getBpmValue(), songRecord.getStartKey(), targetBpm.getBpmValue(), targetKey);
							if (keyRelation.isCompatible()) {
								float percentInKey = (0.5f - Math.abs(keyRelation.getDifference())) * 2.0f;
								if (log.isTraceEnabled())
									log.trace("paint(): percentInKey=" + percentInKey); 
								if (!option.state().isSet(QStyle.StateFlag.State_Selected)) {
									Color baseColor = new Color(option.palette().color(QPalette.ColorRole.Base));        	
									painter.fillRect(option.rect(), harmonicMatchColor.getLinearGradient(baseColor, percentInKey).getQColor());
								}
							}				
						} else if (compatibleWidget.getShowType() == CompatibleSongTableWidget.COMPATIBLE_TYPE_NO_KEYLOCK) {
							KeyRelation keyRelation = Key.getClosestKeyRelation(songRecord.getStartBpm(), songRecord.getStartKey(), targetBpm.getBpmValue(), targetKey);
							if (keyRelation.isCompatible()) {
								float percentInKey = (0.5f - Math.abs(keyRelation.getDifference())) * 2.0f;
								if (log.isTraceEnabled())
									log.trace("paint(): percentInKey=" + percentInKey); 
								if (!option.state().isSet(QStyle.StateFlag.State_Selected)) {
									Color baseColor = new Color(option.palette().color(QPalette.ColorRole.Base));        	
									painter.fillRect(option.rect(), harmonicMatchColor.getLinearGradient(baseColor, percentInKey).getQColor());
								}
							}					
						}
					} else {
						if (RE3Properties.getBoolean("enable_harmonic_coloring_in_search_table") && CentralWidgetUI.instance.isDetailsTabOpen()) { 
							SongKeyRelation keyRelation = SongKeyRelation.getSongKeyRelation(songRecord, targetBpm, targetKey);
							if (keyRelation.isCompatible()) {
								float percentInKey = (0.5f - Math.abs(keyRelation.getBestKeyRelation().getDifference())) * 2.0f;
								if (log.isTraceEnabled())
									log.trace("paint(): percentInKey=" + percentInKey); 
								if (!option.state().isSet(QStyle.StateFlag.State_Selected)) {
									Color baseColor = new Color(option.palette().color(QPalette.ColorRole.Base));        	
									painter.fillRect(option.rect(), harmonicMatchColor.getLinearGradient(baseColor, percentInKey).getQColor());
								}
							}
						}
					}
				}	
	    	}
		}
		super.paint(painter, option, index);
    }
    
}
