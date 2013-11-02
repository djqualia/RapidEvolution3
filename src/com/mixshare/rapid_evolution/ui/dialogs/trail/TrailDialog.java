package com.mixshare.rapid_evolution.ui.dialogs.trail;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.SongGroupRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.player.PlayerManager;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.dialogs.columns.SelectColumnsDialog;
import com.mixshare.rapid_evolution.ui.model.profile.trail.TrailInstance;
import com.mixshare.rapid_evolution.ui.model.profile.trail.TrailModelManager;
import com.mixshare.rapid_evolution.ui.util.QWidgetUtil;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.ui.widgets.common.search.SearchBarWidget;
import com.mixshare.rapid_evolution.ui.widgets.profile.ProfileWidgetUI;
import com.mixshare.rapid_evolution.ui.widgets.profile.event.ProfileWidgetChangeListener;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.CaseSensitivity;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QMoveEvent;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class TrailDialog extends QDialog implements ProfileWidgetChangeListener {

	static private Logger log = Logger.getLogger(TrailDialog.class);
	
	static public TrailDialog instance = null;
	
	private SearchBarWidget searchBar;
	private TrailModelManager modelManager;
    private TrailView trailView;
    private TrailProxyModel proxyModel;
        
    private QAction saveAsPlaylistAction;
    private QAction clear;
    private QAction playAction;
    private QAction editAction;
    private QAction removeAction;
    private QAction separator1;
    
    transient private String currentPlaylistName;
    
	public TrailDialog() {
        super();
        init();
    }
    
    public TrailDialog(QWidget parent) {
        super(parent);
        init();
    }

    private void init() {
    	try {    		
    		instance = this; 
    		
	        setWindowTitle(Translations.get("trail_dialog_title"));
	        setWindowIcon(new QIcon(RE3Properties.getProperty("application_icon_filename")));

	    	QSizePolicy trailWidgetSizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Expanding, com.trolltech.qt.gui.QSizePolicy.Policy.Expanding);
	    	trailWidgetSizePolicy.setVerticalStretch((byte)1);
	    	setSizePolicy(trailWidgetSizePolicy);    	    	    	
	    	
	    	QVBoxLayout trailLayout = new QVBoxLayout(this);    	
	    	trailLayout.setMargin(10);    	    	
	    	
	        searchBar = new SearchBarWidget(false, 4);
	        
	        createModels();	       
	    	
	        trailLayout.addWidget(searchBar);
	        trailLayout.addWidget(trailView);    	
	    	
	        QDialogButtonBox buttonBox = new QDialogButtonBox();
	        buttonBox.setObjectName("buttonBox");
	        buttonBox.setGeometry(new QRect(20, 480, 401, 41));
	        buttonBox.setFocusPolicy(com.trolltech.qt.core.Qt.FocusPolicy.TabFocus);
	        buttonBox.setStandardButtons(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.createQFlags(com.trolltech.qt.gui.QDialogButtonBox.StandardButton.Ok));
	        buttonBox.setCenterButtons(true);
	        buttonBox.clicked.connect(this, "closeOptions()");	        
	        trailLayout.addWidget(buttonBox);	        
	        
	        searchBar.getFilterText().textChanged.connect(this, "updateFilter()");  
	        searchBar.getConfigureColumnsButton().clicked.connect(this, "configureColumns()");
	        
	    	// setup context menu    	
	        trailView.setContextMenuPolicy(Qt.ContextMenuPolicy.ActionsContextMenu);    	
	        
	    	saveAsPlaylistAction = new QAction(Translations.get("save_trail_as_playlist_text"), this);
	    	saveAsPlaylistAction.triggered.connect(this, "saveAsPlaylist()");              
	    	saveAsPlaylistAction.setIcon(new QIcon(RE3Properties.getProperty("menu_save_as_playlist_icon")));
	    		        
			clear = new QAction(Translations.get("trail_clear_text"), this);
			clear.triggered.connect(this, "clear()");		
			clear.setIcon(new QIcon(RE3Properties.getProperty("trail_clear_icon")));
			
	        playAction = new QAction(Translations.get("play_text"), this);
	        playAction.triggered.connect(this, "playSelected()");
	        playAction.setIcon(new QIcon(RE3Properties.getProperty("menu_play_icon")));

	        separator1 = new QAction("", this);
	        separator1.setSeparator(true);

	        editAction = new QAction(Translations.get("search_table_menu_edit"), this);
	        editAction.triggered.connect(this, "editSelected()");
	        editAction.setIcon(new QIcon(RE3Properties.getProperty("menu_edit_icon")));

	        removeAction = new QAction(Translations.get("remove_text"), this);
	        removeAction.triggered.connect(this, "removeSelected()");
	        removeAction.setIcon(new QIcon(RE3Properties.getProperty("menu_remove_icon")));
	        
			trailView.addAction(saveAsPlaylistAction);
	        trailView.addAction(clear);
				        	        	        
	        searchBar.getFilterText().setFocus();
	        
	        QWidgetUtil.setWidgetSize(this, "trail_dialog", 700, 400);
	        QWidgetUtil.setWidgetPosition(this, "trail_dialog");
	        	        	        
	        ProfileWidgetUI.instance.addCurrentProfileChangedListener(this);
	        
	        trailView.selectionModel().selectionChanged.connect(this, "selectionChanged()");
	        
    	} catch (Exception e) {
    		log.error("init(): error", e);
    	}
    }
    
    private void rowsRemoved() {
    	updateActions();
    }
    
    private void rowsInserted() {
    	updateActions();
    }
    
    private void selectionChanged() {
    	updateActions();
    }
    
    private void updateActions() {
    	trailView.removeAction(playAction);
    	trailView.removeAction(editAction);
    	trailView.removeAction(removeAction);
    	trailView.removeAction(saveAsPlaylistAction);
    	trailView.removeAction(clear);
    	trailView.removeAction(separator1);
    	int numSelected = trailView.selectionModel().selectedRows(0).size();
    	int rowCount = modelManager.getSourceModel().rowCount();
    	if (numSelected == 0) {
    		
    	} else if (numSelected == 1) {
    		trailView.addAction(playAction);
    		trailView.addAction(editAction);
    		trailView.addAction(removeAction);    		
    		trailView.addAction(separator1);    		
    	} else if (numSelected > 1) {
    		trailView.addAction(playAction);
    		trailView.addAction(removeAction);    		
    		trailView.addAction(separator1);    		    		
    	}
    	trailView.addAction(saveAsPlaylistAction);
    	if (rowCount > 1)
    		trailView.addAction(clear);
    	
    }
    
    protected void closeOptions() {
    	this.close();
    	//setVisible(false);
    }
    
	public void setupEventListeners() {
        trailView.horizontalHeader().sectionResized.connect(modelManager, "columnResized(Integer,Integer,Integer)");
        trailView.horizontalHeader().sectionMoved.connect(modelManager, "columnMoved(Integer,Integer,Integer)");    
	}    		
	
    private void createModels() {
    	modelManager = (TrailModelManager)Database.getRelativeModelFactory().getRelativeModelManager(TrailModelManager.class);
    	modelManager.setProfileTrailRecords(ProfileWidgetUI.instance.getProfileTrail());
    	modelManager.initialize(this);
    	// setup the proxy model
    	proxyModel = new TrailProxyModel(this, modelManager);
    	proxyModel.setDynamicSortFilter(false);
    	proxyModel.setSourceModel(modelManager.getSourceModel());
    	proxyModel.setFilterCaseSensitivity(CaseSensitivity.CaseInsensitive);
    	modelManager.setProxyModel(proxyModel);
        // set up tree view    	
    	trailView = new TrailView(modelManager);
    	trailView.setModel(proxyModel);
    	modelManager.getSourceModel().rowsInserted.connect(this, "rowsInserted()");
    	modelManager.getSourceModel().rowsRemoved.connect(this, "rowsRemoved()");
    	
        // set initial column sizes 
    	modelManager.setSourceColumnSizes(trailView);
        // init event listeners (selection changed, columns moved, etc)    	
        setupEventListeners();
    }
	
    
    protected void resizeEvent(QResizeEvent re) {
    	super.resizeEvent(re);
    	UIProperties.setProperty("trail_dialog_width", String.valueOf(re.size().width()));
    	UIProperties.setProperty("trail_dialog_height", String.valueOf(re.size().height()));
    }
    
    protected void moveEvent(QMoveEvent me) {
    	super.moveEvent(me);
    	UIProperties.setProperty("trail_dialog_x", String.valueOf(me.pos().x()));
    	UIProperties.setProperty("trail_dialog_y", String.valueOf(me.pos().y()));
    }
        
    private void updateFilter() {
    	proxyModel.setSearchText(searchBar.getFilterText().text());
    	proxyModel.invalidate();
    }

    private void configureColumns() {
    	SelectColumnsDialog selectColumnsDialog = new SelectColumnsDialog(this, modelManager);
    	if (selectColumnsDialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		selectColumnsDialog.saveSelections();
    		proxyModel.invalidate();
    		modelManager.setSourceColumnSizes(trailView);
    	}    	
    }
    
    private void clear() {
    	ProfileWidgetUI.instance.clearTrail();
    	currentPlaylistName = null;
    }
	
    private void saveAsPlaylist() {
    	currentPlaylistName = ProfileWidgetUI.instance.saveAsPlaylist(currentPlaylistName, false);
    }
    
    public void refresh() {
		if (log.isDebugEnabled())
			log.debug("refresh(): called");
    	modelManager.setProfileTrailRecords(ProfileWidgetUI.instance.getProfileTrail());
    	modelManager.refresh();
    }

    public void currentProfileChanged() {
    	proxyModel.invalidate();
    	trailView.scrollToCurrent(ProfileWidgetUI.instance.getProfileIndex());
    	//trailView.repaint();
    }
    
    public void profileTrailChanged() {
    	refresh();
    }
    
    public Vector<TrailInstance> getSelectedInstances() {
    	Vector<TrailInstance> result = new Vector<TrailInstance>();
    	List<QModelIndex> selectedRows = trailView.selectionModel().selectedRows(0);
    	for (QModelIndex selectedRow : selectedRows) {
    		QModelIndex sourceIndex = proxyModel.mapToSource(selectedRow);
    		result.add(new TrailInstance(modelManager.getRecordForRow(sourceIndex.row()), sourceIndex.row()));
    	}
    	return result;
    }
    
    protected void playSelected() {
    	Vector<Integer> playlist = new Vector<Integer>();
    	for (TrailInstance trailInstance : getSelectedInstances()) {
    		Record record = trailInstance.getRecord();
    		if (record instanceof SongGroupRecord) {
    			SongGroupRecord groupRecord = (SongGroupRecord)record;    			
    			Vector<SongRecord> groupSongs = groupRecord.getSongs();
    			for (SongRecord groupSong : groupSongs) {
    				Integer songId = groupSong.getUniqueId();
    				if (!playlist.contains(songId))
    					playlist.add(songId);
    			}
    		} else {
    			// song
    			SongRecord song = (SongRecord)record;
    			Integer songId = song.getUniqueId();
				if (!playlist.contains(songId))
					playlist.add(songId);
    		}
    	}
    	PlayerManager.playSongs(playlist);
    }
    
    protected void editSelected() {
    	ProfileWidgetUI.instance.setProfileIndex(getSelectedInstances().get(0).getPosition());
    }
    
    protected void removeSelected() {    	
		if (QMessageBox.question(this, Translations.get("trail_remove_selected_title"), Translations.get("trail_remove_selected_text"), QMessageBox.StandardButton.Yes, QMessageBox.StandardButton.No) != QMessageBox.StandardButton.Yes.value()) {
			return;
		}
    	
    	Vector<Record> profileTrail = ProfileWidgetUI.instance.getProfileTrailCopy();
    	Vector<TrailInstance> instanceTrail = new Vector<TrailInstance>(profileTrail.size());
    	for (int r = 0; r < profileTrail.size(); ++r)
    		instanceTrail.add(new TrailInstance(profileTrail.get(r), r));
    	for (TrailInstance removedInstance : getSelectedInstances()) {
    		for (TrailInstance existingInstance : instanceTrail) {
    			if (existingInstance.equals(removedInstance)) {
    				instanceTrail.remove(existingInstance);
    				break;
    			}
    		}
    	}
    	Vector<Record> newProfileTrail = new Vector<Record>(instanceTrail.size());
    	for (TrailInstance i : instanceTrail)
    		newProfileTrail.add(i.getRecord());
    	ProfileWidgetUI.instance.setProfileTrail(newProfileTrail);
    }

	public String getCurrentPlaylistName() {
		return currentPlaylistName;
	}

	public void setCurrentPlaylistName(String currentPlaylistName) {
		this.currentPlaylistName = currentPlaylistName;
	}

}
