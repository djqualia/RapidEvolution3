package com.mixshare.rapid_evolution.ui.widgets.common;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.song.MixoutIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.MixoutProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.ui.model.ModelManagerInterface;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.profile.search.VideoLinkModelManager;
import com.mixshare.rapid_evolution.ui.model.profile.search.song.SongMixoutTableItemModel;
import com.mixshare.rapid_evolution.ui.model.profile.trail.TrailModelManager;
import com.mixshare.rapid_evolution.ui.model.search.SearchItemModel;
import com.mixshare.rapid_evolution.ui.model.table.RecordTableModelManager;
import com.mixshare.rapid_evolution.ui.util.Color;
import com.mixshare.rapid_evolution.ui.util.ThumbnailImageFactory;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarEditor;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRating;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QItemDelegate;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QStyle;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QWidget;

public class ItemDelegate extends QItemDelegate implements AllColumns, DataConstants {
	
	static private Logger log = Logger.getLogger(ItemDelegate.class);
	
	static protected Color BROKEN_FILELINKS_COLOR = RE3Properties.getColor("broken_filelink_color");
	static protected Color EXTERNAL_ITEMS_COLOR = RE3Properties.getColor("external_items_color");
	
	protected ModelManagerInterface modelManager;
	
	public ItemDelegate(QWidget parent, ModelManagerInterface modelManager) {
        super(parent);
        this.modelManager = modelManager;
    }

    @Override
    public void paint(QPainter painter, QStyleOptionViewItem option, QModelIndex index) {
    	//if (log.isTraceEnabled())
    		//log.trace("paint(): index=" + index + ", option.palette().highlight()=" + option.palette().highlight());
		QStyle.State state = option.state();
		state.set(QStyle.StateFlag.State_Active);
		option.setState(state);        	
    	Object data = index.data();
    	QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(index);
    	Column sourceColumn = modelManager.getSourceColumnType(sourceIndex.column());
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
    	if (RE3Properties.getBoolean("highlight_external_items")) {
    		if (modelManager instanceof RecordTableModelManager) {
    			SearchRecord searchRecord = (SearchRecord)((RecordTableModelManager)modelManager).getRecordForRow(sourceIndex.row());
				if ((searchRecord != null) && (searchRecord.isExternalItem())) {
					if (!state.isSet(QStyle.StateFlag.State_Selected)) {
	    				state.set(QStyle.StateFlag.State_Selected);
	    				option.setState(state);					
	    				QPalette p = option.palette();
	    				p.setColor(QPalette.ColorRole.Highlight, EXTERNAL_ITEMS_COLOR.getQColor());
	    				option.setPalette(p);
					}
				}    				
    		}
    	}    	
    	if (data != null && data instanceof StarRating) {
            if (option.state().isSet(QStyle.StateFlag.State_Selected) && option.state().isSet(QStyle.StateFlag.State_Active)) {
                painter.fillRect(option.rect(), option.palette().highlight());
            }
            ((StarRating) data).paint(painter, option.rect(), option.palette(),
                                      StarRating.ReadOnly);
    	} else if (sourceColumn.getColumnId() == COLUMN_THUMBNAIL_IMAGE.getColumnId()) {
            if (option.state().isSet(QStyle.StateFlag.State_Selected) && option.state().isSet(QStyle.StateFlag.State_Active)) {
                painter.fillRect(option.rect(), option.palette().highlight());
            }
            SearchRecord searchRecord = null;
            if (modelManager instanceof RecordTableModelManager) 
            	searchRecord = (SearchRecord)((RecordTableModelManager)modelManager).getRecordForRow(sourceIndex.row());
            else if (modelManager instanceof TrailModelManager) {
            	searchRecord = (SearchRecord)((TrailModelManager)modelManager).getRecordForRow(sourceIndex.row());
            }
            if (searchRecord != null) {
    			if (log.isTraceEnabled())
    				log.trace("paint(): rendering thumbnail=" + searchRecord.getThumbnailImageFilename());
    			String thumbnailFilename = searchRecord.getThumbnailImageFilename();
    			if (!searchRecord.hasThumbnail()) {
    				if (searchRecord instanceof SongRecord)
    					thumbnailFilename = ((SongRecord)searchRecord).getBestRelatedThumbnailImageFilename();
    			}
    			QImage image = ThumbnailImageFactory.fetchThumbnailImage(thumbnailFilename);
    			if (image != null) {
    				QRect rect = option.rect();
    				if (image.size().width() < option.rect().width()) {
    					int shift = (option.rect().width() - image.size().width()) / 2;
    					rect.adjust(shift, 0, -shift, 0);
    				}
    				if (image.size().height() < option.rect().height()) {
    					int shift = (option.rect().height() - image.size().height()) / 2;
    					rect.adjust(0, shift, 0, -shift);
    				}
    				painter.drawImage(rect, image);
    			}
    		} else if (modelManager instanceof VideoLinkModelManager) {
    			super.paint(painter, option, index);
    		}
        } else {
            super.paint(painter, option, index);
        }
    }

    @Override
    public QSize sizeHint(QStyleOptionViewItem option, QModelIndex index) {
    	if (index != null) {
	        Object data = index.data();
	
	        if (data instanceof StarRating)
	            return ((StarRating) data).sizeHint();
	        else
	            return super.sizeHint(option, index);
    	} else {
    		return super.sizeHint(option, index);
    	}
    }

    @Override
    public QWidget createEditor(QWidget parent, QStyleOptionViewItem item, QModelIndex index) {
        Object data = index.data();
    	QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(index);
    	Column sourceColumn = modelManager.getSourceColumnType(sourceIndex.column()); 
        if (data instanceof StarRating)
            return new StarEditor(parent, (StarRating) data);
        else if (sourceColumn.getColumnId() == COLUMN_MIXOUT_TYPE.getColumnId()) {
        	QComboBox cb = new QComboBox(parent);
        	String[] descriptions = MixoutRecord.getMixoutTypeDescriptions();
        	int currentIndex = 0;
        	for (int i = 0; i < descriptions.length; ++i) {
        		cb.addItem(descriptions[i]);
        		if (descriptions[i].equalsIgnoreCase(index.data().toString()))
        			currentIndex = i;
        	}        	
        	cb.setCurrentIndex(currentIndex);
        	return cb;
        } else if (sourceColumn.getColumnId() == COLUMN_MIXOUT_COMMENTS.getColumnId()) {
        	QLineEdit lineEdit = new QLineEdit(parent);
    		SongProfile mixoutFrom = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
        	SongRecord mixoutTo = (SongRecord)((RecordTableModelManager)modelManager).getRecordForRow(sourceIndex.row());
        	MixoutRecord mixout = Database.getMixoutIndex().getMixoutRecord(new MixoutIdentifier(mixoutFrom.getUniqueId(), mixoutTo.getUniqueId()));
        	if (mixout != null) {
        		lineEdit.setText(mixout.getComments());
        		lineEdit.selectAll();
        	}
        	return lineEdit;
        } else
            return super.createEditor(parent, item, index);
    }

    @Override
    public void setEditorData(QWidget editor, QModelIndex index) {
        Object data = index.data();
    	QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(index);
    	Column sourceColumn = modelManager.getSourceColumnType(sourceIndex.column());       
        if (data instanceof StarRating)
            ((StarEditor) editor).setStarRating((StarRating) data);
        else if (sourceColumn.getColumnId() != COLUMN_MIXOUT_COMMENTS.getColumnId()) // for some reason, calling setEditorData was clearing the field...
            super.setEditorData(editor, index);
    }

    @Override
    public void setModelData(QWidget editor, QAbstractItemModel model, QModelIndex index) {    	
    	QModelIndex sourceIndex = modelManager.getProxyModel().mapToSource(index);
    	Column sourceColumn = modelManager.getSourceColumnType(sourceIndex.column()); 
    	if (SongMixoutTableItemModel.isEditableMixoutColumn(sourceColumn)) {
    		try {
	    		SongProfile mixoutFrom = (SongProfile)ProfileWidgetUI.instance.getCurrentProfile();
	        	SongRecord mixoutTo = (SongRecord)((RecordTableModelManager)modelManager).getRecordForRow(sourceIndex.row());
	        	MixoutProfile editMixout = Database.getMixoutIndex().getMixoutProfile(new MixoutIdentifier(mixoutFrom.getUniqueId(), mixoutTo.getUniqueId()));
	        	if (editMixout != null) {
	        		if ((sourceColumn.getColumnId() != COLUMN_MIXOUT_RATING_STARS.getColumnId()) && (sourceColumn.getColumnId() != COLUMN_MIXOUT_TYPE.getColumnId()))
	        			super.setModelData(editor, model, index);
	        		if (editor instanceof StarEditor) {
	        			editMixout.setRating(((StarEditor) editor).starRating().getRating(), DATA_SOURCE_USER);
	        		} else if (editor instanceof QComboBox) {
	        			editMixout.setType(MixoutRecord.getMixoutType(((QComboBox)editor).currentIndex()));
	        			model.setData(index, MixoutRecord.getMixoutTypeDescription(MixoutRecord.getMixoutType(((QComboBox)editor).currentIndex())));
	        		} else if (index.data() instanceof String) {        			
		        		if (sourceColumn.getColumnId() == COLUMN_MIXOUT_RATING_VALUE.getColumnId())
		        			editMixout.setRating(Rating.getRating(Integer.parseInt((String)index.data())), DATA_SOURCE_USER);
		        		else if (sourceColumn.getColumnId() == COLUMN_MIXOUT_BPM_DIFF.getColumnId()) {
		        			String input = (String)index.data();
		        			if (input.endsWith("%"))
		        				input = input.substring(0, input.length() - 1);
		        			editMixout.setBpmDiff(Float.parseFloat(input));
		        		} else if (sourceColumn.getColumnId() == COLUMN_MIXOUT_COMMENTS.getColumnId())
		        			editMixout.setComments((String)index.data());
	        		}       
	        		editMixout.save();
	        	}
    		} catch (Exception e) {
    			log.error("setModelData(): error", e);
    		}
    	} else if (SearchItemModel.isEditableSearchColumn(sourceColumn)) {
    		if (editor instanceof StarEditor) {
    			SearchRecord searchRecord = null;
    			if (modelManager instanceof RecordTableModelManager) {
	    			searchRecord = (SearchRecord)((RecordTableModelManager)modelManager).getRecordForRow(sourceIndex.row());
    			} else {
    				searchRecord = (SearchRecord)((TrailModelManager)modelManager).getRecordForRow(sourceIndex.row());
    			}
    			SearchProfile searchProfile = (SearchProfile)Database.getProfile(searchRecord.getIdentifier());
    			if (searchProfile != null) {
    				searchProfile.setRating(((StarEditor) editor).starRating().getRating(), DATA_SOURCE_USER);
    				searchProfile.save();
    			}    			
    		}
    	} else {
    		super.setModelData(editor, model, index);
    	}
    }
        
}