package com.mixshare.rapid_evolution.ui.widgets.profile.details;

import java.util.List;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.user.UserDataType;
import com.mixshare.rapid_evolution.music.accuracy.Accuracy;
import com.mixshare.rapid_evolution.music.beatintensity.BeatIntensity;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.bpm.tapper.BpmTapper;
import com.mixshare.rapid_evolution.music.bpm.tapper.BpmTapperListener;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.ui.model.column.AllColumns;
import com.mixshare.rapid_evolution.ui.model.column.Column;
import com.mixshare.rapid_evolution.ui.model.column.UserDataColumn;
import com.mixshare.rapid_evolution.ui.model.profile.details.CommonDetailsModelManager;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarEditor;
import com.mixshare.rapid_evolution.ui.widgets.common.rating.StarRating;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.workflow.user.ProfileSaveTask;
import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.CheckState;
import com.trolltech.qt.core.Qt.FocusPolicy;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QCompleter;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QItemDelegate;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QStyle;
import com.trolltech.qt.gui.QStyleOptionViewItem;
import com.trolltech.qt.gui.QTextEdit;
import com.trolltech.qt.gui.QWidget;

public class ProfileDetailsItemDelegate extends QItemDelegate implements AllColumns, DataConstants {
	
	static private Logger log = Logger.getLogger(ProfileDetailsItemDelegate.class);
	
	private DetailsTableView tableView;
	private QCheckBox checkBoxForDrawing = new QCheckBox();
	private CommonDetailsModelManager detailsModelManager;
	private Profile relativeProfile;	
	
    public ProfileDetailsItemDelegate(DetailsTableView tableView, CommonDetailsModelManager detailsModelManager, Profile relativeProfile) {
        super(tableView);
        this.tableView = tableView;
        this.detailsModelManager = detailsModelManager;
        this.relativeProfile = relativeProfile;        
    }

    @Override
    public void paint(QPainter painter, QStyleOptionViewItem option, QModelIndex index) {
        Object data = index.data();
        if (data != null) {
            if (data instanceof StarRating) {
                if (option.state().isSet(QStyle.StateFlag.State_Selected) && option.state().isSet(QStyle.StateFlag.State_Active)) {
                    painter.fillRect(option.rect(), option.palette().highlight());
                }
                ((StarRating) data).paint(painter, option.rect(), option.palette(),
                                          StarRating.ReadOnly);
                return;
            }        	
        } 
        super.paint(painter, option, index);
    }

    @Override
    public QSize sizeHint(QStyleOptionViewItem option, QModelIndex index) {
        Object data = index.data();

        if (data instanceof StarRating)
            return ((StarRating) data).sizeHint();
        else
            return super.sizeHint(option, index);
    }

    @Override
    public QWidget createEditor(QWidget parent, QStyleOptionViewItem item, QModelIndex index) {
    	if (log.isDebugEnabled())
    		log.debug("createEditor(): index row=" + index.row() + ", column=" + index.column());
    	Object data = index.data();        
        Column sourceColumn = detailsModelManager.getViewColumnType(detailsModelManager.getProxyModel().mapToSource(index).row());
        if (sourceColumn.getColumnId() == COLUMN_UNIQUE_ID.getColumnId())
        	return null;
        if (sourceColumn.getColumnId() == COLUMN_DUPLICATE_IDS.getColumnId())
        	return null;
    	if (index.column() == 1) {
    		if (sourceColumn.getColumnId() == COLUMN_BPM_START.getColumnId()) {
            	if (index.column() == 1) {
            		QPushButton button = new QPushButton(parent);
            		button.clicked.connect(new BpmTapper(new BpmTapResultProcessor(COLUMN_BPM_START)), "tap(Boolean)");
            		button.setText(Translations.get("bpm_tapper_tap_text"));
            		button.setFocusPolicy(FocusPolicy.NoFocus);
            		return button;
            	}
            } else if (sourceColumn.getColumnId() == COLUMN_BPM_END.getColumnId()) {
            	if (index.column() == 1) {
            		QPushButton button = new QPushButton(parent);
            		button.clicked.connect(new BpmTapper(new BpmTapResultProcessor(COLUMN_BPM_END)), "tap(Boolean)");
            		button.setText(Translations.get("bpm_tapper_tap_text"));
            		button.setFocusPolicy(FocusPolicy.NoFocus);
            		return button;
            	}
            } else if (sourceColumn.getColumnId() == COLUMN_FILEPATH.getColumnId()) {
            	if (index.column() == 1) {
            		QPushButton button = new QPushButton(parent);
            		button.setText(Translations.get("song_file_browse_text"));
            		button.clicked.connect(this, "browseFilename(Boolean)");
            		button.setFocusPolicy(FocusPolicy.NoFocus);
            		return button;
            	}
            } else if ((sourceColumn.getColumnId() == COLUMN_RATING_STARS.getColumnId()) || (sourceColumn.getColumnId() == COLUMN_RATING_VALUE.getColumnId())) {
            	if (index.column() == 1) {
            		QPushButton button = new QPushButton(parent);
            		button.setText(Translations.get("search_table_menu_fields_clear"));
            		button.clicked.connect(this, "clearRating(Boolean)");
            		button.setFocusPolicy(FocusPolicy.NoFocus);
            		return button;
            	}
            }
    		return null;
    	} else {
            if (data instanceof StarRating) {
                return new StarEditor(parent, (StarRating) data);
            } else if (data instanceof Accuracy) {
            	QSlider slider = new QSlider(parent);
            	slider.setMinimum(0);
            	slider.setMaximum(100);
            	slider.setTickInterval(1);
            	slider.setOrientation(Orientation.Horizontal);        	
            	slider.setValue(((Accuracy)data).getAccuracy());
            	slider.setAutoFillBackground(true);
            	slider.setMaximumWidth(200);
            	return slider;
            } else if (data instanceof BeatIntensity) {
            	QSlider slider = new QSlider(parent);
            	slider.setMinimum(0);
            	slider.setMaximum(100);
            	slider.setTickInterval(1);
            	slider.setOrientation(Orientation.Horizontal);        	
            	slider.setValue(((BeatIntensity)data).getBeatIntensityValue());
            	slider.setAutoFillBackground(true);
            	slider.setMaximumWidth(200);
            	return slider;
            } else if (data instanceof Boolean) {
            	QCheckBox checkbox = new QCheckBox(parent);
            	checkbox.setChecked(((Boolean)data).booleanValue());
            	checkbox.setAutoFillBackground(true);
            	return checkbox;
            } else if ((sourceColumn.getColumnId() == COLUMN_ARTIST_DESCRIPTION.getColumnId()) || 
            			(sourceColumn.getColumnId() == COLUMN_ARTIST_NAME.getColumnId())) {			
            	if (index.column() == 1)
            		return null;
            	if (relativeProfile instanceof ReleaseProfile) {
    				ReleaseProfile release = (ReleaseProfile)relativeProfile;
    				if (release.isCompilationRelease())
    					return null;
    			}
            	QLineEdit lineEdit = new QLineEdit(parent);
            	QCompleter completer = Database.getArtistModelManager().getArtistCompleter();
            	lineEdit.setCompleter(completer);
            	return lineEdit;
            } else if ((sourceColumn.getColumnId() == COLUMN_LABELS.getColumnId()) ||
            			(sourceColumn.getColumnId() == COLUMN_LABEL_NAME.getColumnId())) {
            	if (index.column() == 1)
            		return null;
            	QLineEdit lineEdit = new QLineEdit(parent);
            	QCompleter completer = Database.getLabelModelManager().getLabelCompleter();
            	lineEdit.setCompleter(completer);
            	return lineEdit;
            } else if (sourceColumn.getColumnId() == COLUMN_RELEASE_TITLE.getColumnId()) {			
            	if (index.column() == 1)
            		return null;
            	QLineEdit lineEdit = new QLineEdit(parent);
            	QCompleter completer = Database.getReleaseModelManager().getReleaseTitleCompleter();        	
            	lineEdit.setCompleter(completer);
            	return lineEdit;
            } else if ((sourceColumn.getColumnId() == COLUMN_COMMENTS.getColumnId()) || (sourceColumn.getColumnId() == COLUMN_LABEL_CONTACT_INFO.getColumnId())) {			
            	if (index.column() == 1)
            		return null;
            	QTextEdit lineEdit = new QTextEdit(parent);
            	return lineEdit;
            } else if (sourceColumn instanceof UserDataColumn) {
            	if (((UserDataColumn)sourceColumn).getUserDataType().getFieldType() == UserDataType.TYPE_BOOLEAN_FLAG) {
                	QCheckBox checkbox = new QCheckBox(parent);
                	checkbox.setChecked(false);
                	checkbox.setAutoFillBackground(true);
                	return checkbox;            		
            	}
            } else {
            	if (index.column() == 1)
            		return null;
            }
            return super.createEditor(parent, item, index);    		
    	}
    }

    @Override
    public void setEditorData(QWidget editor, QModelIndex index) {
        Column sourceColumn = detailsModelManager.getViewColumnType(detailsModelManager.getProxyModel().mapToSource(index).row());    	
        Object data = index.data();
        if (index.column() == 0) {
	        if (data instanceof StarRating)
	            ((StarEditor) editor).setStarRating((StarRating) data);
	        else if (data instanceof Accuracy)
	        	((QSlider) editor).setValue(((Accuracy)data).getAccuracy());
	        else if (data instanceof BeatIntensity)
	        	((QSlider) editor).setValue(((BeatIntensity)data).getBeatIntensityValue());
	        else if (data instanceof Boolean)
	        	((QCheckBox) editor).setChecked(((Boolean)data).booleanValue());
	        else {
	        	if ((sourceColumn.getColumnId() == COLUMN_COMMENTS.getColumnId()) || (sourceColumn.getColumnId() == COLUMN_LABEL_CONTACT_INFO.getColumnId())) {
	        		((QTextEdit) editor).setPlainText((String)data);
	        	} else {
	        		super.setEditorData(editor, index);
	        	}
	        }
        } else {
        	super.setEditorData(editor, index);
        }
    }

    @Override
    public void setModelData(QWidget editor, QAbstractItemModel model, QModelIndex index) {
        Column sourceColumn = detailsModelManager.getViewColumnType(detailsModelManager.getProxyModel().mapToSource(index).row());    	
    	if (index.column() == 0) {
	        if (index.data() instanceof StarRating) {
	        	// NOTE: calling super before model.setData is a hack, the dataChanged signal wasn't getting sent otherwise, and this causes
	        	// 2 dataChanged signals to fire, the first with a null value, and this has to be ignored.
	        	super.setModelData(editor, model, index);
	            model.setData(index, ((StarEditor) editor).starRating());            
	        } else if (index.data() instanceof Accuracy) {
	        	model.setData(index, Accuracy.getAccuracy(((QSlider) editor).value()));
	        } else if (index.data() instanceof BeatIntensity) {
	        	model.setData(index, BeatIntensity.getBeatIntensity(((QSlider) editor).value()));	        
	        } else if (index.data() instanceof Boolean) {
	        	super.setModelData(editor, model, index);
	        	model.setData(index, new Boolean(((QCheckBox) editor).checkState() == CheckState.Checked));
	        } else {
	        	if ((sourceColumn.getColumnId() == COLUMN_COMMENTS.getColumnId()) || (sourceColumn.getColumnId() == COLUMN_LABEL_CONTACT_INFO.getColumnId())) {
	        		model.setData(index, ((QTextEdit)editor).toPlainText());
	        	} else {
	        		super.setModelData(editor, model, index);
	        	}
	        }
    	} else {
    		super.setModelData(editor, model, index);
    	}
    }
    
    protected void clearRating(Boolean checked) {
    	try {
    		SongProfile songProfile = (SongProfile)relativeProfile;
    		songProfile.setRating(Rating.NO_RATING, DATA_SOURCE_USER);
	        songProfile.save();
    	} catch (Exception e) {
    		log.error("clearRating(): error", e);
    	}    	
    }
    
    protected void browseFilename(Boolean checked) {
    	try {
    		QFileDialog fileDialog = new QFileDialog(tableView);    		
    		fileDialog.setFileMode(QFileDialog.FileMode.ExistingFile);
    		fileDialog.setViewMode(QFileDialog.ViewMode.Detail);
    		fileDialog.setFilters(FileUtil.getSupportedFileFilters());
    		SongProfile songProfile = (SongProfile)relativeProfile;
    		fileDialog.setDirectory(FileUtil.getDirectoryFromFilename(songProfile.getSongFilename()));
    	    if (fileDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    	        List<String> filenames = fileDialog.selectedFiles();
    	        String filename = filenames.get(0);
    	        log.debug("browseFilename(): selected filename=" + filename);
    	        songProfile.setSongFilename(filename);
    	        songProfile.save();
    	    }
    	} catch (Exception e) {
    		log.error("browseFilename(): error", e);
    	}
    }
    
    private class BpmTapResultProcessor implements BpmTapperListener {
    	
    	private Column column;
    	
    	public BpmTapResultProcessor(Column column) {
    		this.column = column;
    	}
    	
    	public void finalBpm(double bpm) {
    		if (log.isTraceEnabled())
    			log.trace("setBpm(); bpm=" + bpm);    		
    		SongProfile songProfile = (SongProfile)relativeProfile;
    		if (column.getColumnId() == COLUMN_BPM_START.getColumnId()) {
    			songProfile.setBpm(new Bpm(bpm), songProfile.getBpmEnd(), songProfile.getBpmAccuracy(), DATA_SOURCE_USER);
    		} else if (column.getColumnId() == COLUMN_BPM_END.getColumnId()) {
    			songProfile.setBpm(songProfile.getBpmStart(), new Bpm(bpm), songProfile.getBpmAccuracy(), DATA_SOURCE_USER);
    		}    		
    		ProfileWidgetUI.instance.getStageWidget().setCurrentSong(songProfile);
    		ProfileSaveTask.save(songProfile);	
    	}
    	
    	public void setBpm(double bpm) {
    		if (log.isTraceEnabled())
    			log.trace("setBpm(); bpm=" + bpm);    		
    		SongProfile songProfile = (SongProfile)relativeProfile;
    		if (column.getColumnId() == COLUMN_BPM_START.getColumnId()) {
    			songProfile.setBpm(new Bpm(bpm), songProfile.getBpmEnd(), songProfile.getBpmAccuracy(), DATA_SOURCE_USER);
    		} else if (column.getColumnId() == COLUMN_BPM_END.getColumnId()) {
    			songProfile.setBpm(songProfile.getBpmStart(), new Bpm(bpm), songProfile.getBpmAccuracy(), DATA_SOURCE_USER);
    		} 
    		detailsModelManager.setDisableDataChangedEvent(true);
    		QModelIndex sourceIndex = detailsModelManager.getSourceModel().index(detailsModelManager.getSourceColumnIndex(column), 0);
    		detailsModelManager.getSourceModel().setData(sourceIndex, String.valueOf(bpm));
    		detailsModelManager.setDisableDataChangedEvent(false);
    	}

    	public void resetBpm() { }    	
    	
    }
    
}